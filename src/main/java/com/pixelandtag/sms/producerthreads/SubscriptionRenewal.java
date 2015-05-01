package com.pixelandtag.sms.producerthreads;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.inmobia.util.StopWatch;
import com.pixelandtag.cmp.ejb.CMPResourceBeanRemote;
import com.pixelandtag.cmp.ejb.subscription.SubscriptionBeanI;
import com.pixelandtag.cmp.entities.MOProcessorE;
import com.pixelandtag.cmp.entities.SMSService;
import com.pixelandtag.cmp.entities.subscription.Subscription;
import com.pixelandtag.sms.mt.workerthreads.HttpBillingWorker;
import com.pixelandtag.sms.mt.workerthreads.SubscriptionBillingWorker;
import com.pixelandtag.util.FileUtils;

public class SubscriptionRenewal extends Thread {
	public static Logger logger = Logger.getLogger(SubscriptionRenewal.class);
	private static Semaphore semaphore = new Semaphore(1, true);
	private StopWatch watch = new StopWatch();;
	private boolean run = true;
	private CMPResourceBeanRemote cmpbean;
	private SubscriptionBeanI subscriptio_nejb;
	private Map<Long, SMSService> sms_serviceCache = new HashMap<Long, SMSService>();
	private Map<Long, MOProcessorE> mo_processorCache = new HashMap<Long, MOProcessorE>();
	private volatile static BlockingDeque<Billable> billableQ = new LinkedBlockingDeque<Billable>();
	public volatile  BlockingQueue<SubscriptionBillingWorker> billingsubscriptionWorkers = new LinkedBlockingDeque<SubscriptionBillingWorker>();
	
	private ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
	private Context context = null;
	private int queueSize = 1000;
	private int workers = 1;
	private  TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] certificate, String authType) {
            return true;
        }
    };
	private int billables_per_batch = 1000;
	private SubscriptionRenewal instance;
	private Properties log4J;
	private Properties mtsenderprops;
	private HttpClient httpsclient;
	
	
	public SubscriptionRenewal() throws Exception {
		if (queueSize <= 0)
			billableQ = new LinkedBlockingDeque<Billable>();
		else
			billableQ = new LinkedBlockingDeque<Billable>(queueSize);

		initEJB();
		initWorkers();
		instance = this;
	}

	private void initWorkers() throws Exception {
		log4J = FileUtils.getPropertyFile("log4j.billing.properties");
		PropertyConfigurator.configure(log4J);
		mtsenderprops = FileUtils.getPropertyFile("mtsender.properties");

		try {
			billables_per_batch = Integer.valueOf(mtsenderprops
					.getProperty("billables_per_batch"));
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		try {
			workers = Integer.valueOf(mtsenderprops
					.getProperty("billing_workers"));
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		SSLSocketFactory sf = new SSLSocketFactory(acceptingTrustStrategy,
				SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", 8443, sf));
		cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setDefaultMaxPerRoute(workers);
		cm.setMaxTotal(workers * 2);// http connections that are equal to the
									// worker threads.

		httpsclient = new DefaultHttpClient(cm);

		Thread t1;
		for (int i = 0; i < this.workers; i++) {
			SubscriptionBillingWorker worker;
			worker = new SubscriptionBillingWorker("THREAD_WORKER_#_" + i, httpsclient, cmpbean);
			t1 = new Thread(worker);
			t1.start();
			billingsubscriptionWorkers.add(worker);
		}
		// wake all thread's because we're done initializing.
		for (SubscriptionBillingWorker worker : billingsubscriptionWorkers)
			worker.rezume();
	}

	public void initEJB() throws NamingException {
		String JBOSS_CONTEXT = "org.jboss.naming.remote.client.InitialContextFactory";
		;
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_CONTEXT);
		props.put(Context.PROVIDER_URL, "remote://localhost:4447");
		props.put(Context.SECURITY_PRINCIPAL, "testuser");
		props.put(Context.SECURITY_CREDENTIALS, "testpassword123!");
		props.put("jboss.naming.client.ejb.context", true);
		context = new InitialContext(props);
		cmpbean = (CMPResourceBeanRemote) context
				.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.CMPResourceBeanRemote");
		subscriptio_nejb = (SubscriptionBeanI) context
				.lookup("cmp/CMPResourceBean!com.pixelandtag.cmp.ejb.subscription.SubscriptionEJB");
		System.out
				.println("Successfully initialized EJB CMPResourceBeanRemote !!");
	}

	public void run() {

		try {
			while (run) {
				try {
					populateQueue();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				} finally {
					watch.start();
				}
			}
		} catch (OutOfMemoryError e) {
			logger.error(
					"NEEDS RESTART: MEM_USAGE: " + MTProducer.getMemoryUsage()
							+ " >> " + e.getMessage(), e);
		} finally {
			myfinalize();
		}
		logger.info("producer shut down!");
		System.exit(0);
	}

	private void populateQueue() {

		List<Subscription> subsl = subscriptio_nejb
				.getExpiredSubscriptions(Long.valueOf(billables_per_batch));

		for (Subscription sub : subsl) {

			Long sms_service_id = sub.getSms_service_id_fk();
			SMSService service = sms_serviceCache.get(sms_service_id);
			if (service == null) {
				try {
					service = cmpbean.find(SMSService.class, sms_service_id);
				} catch (Exception e) {
					logger.warn("Couldn't find service with id "
							+ sms_service_id);
				}
			}

			if (service != null) {

				MOProcessorE processor = mo_processorCache.get(service
						.getMo_processorFK());

				if (processor == null) {
					try {
						processor = cmpbean.find(MOProcessorE.class,
								service.getMo_processorFK());
					} catch (Exception exp) {

					}
				}

				BigInteger transaction_id = BigInteger.valueOf(cmpbean
						.generateNextTxId());

				Billable billable = new Billable();
				billable.setCp_id("CONTENT360_KE");
				billable.setCp_tx_id(transaction_id);
				billable.setDiscount_applied("0");
				billable.setIn_outgoing_queue(0l);
				billable.setKeyword(service.getCmd());
				billable.setMaxRetriesAllowed(1L);
				billable.setMsisdn(sub.getMsisdn());
				billable.setOperation(BigDecimal.valueOf(service.getPrice())
						.compareTo(BigDecimal.ZERO) > 0 ? Operation.debit
						.toString() : Operation.credit.toString());
				billable.setPrice(BigDecimal.valueOf(service.getPrice()));
				billable.setPriority(0l);
				billable.setProcessed(0L);
				billable.setRetry_count(0L);
				billable.setService_id(service.getCmd());
				billable.setShortcode(processor.getShortcode());
				billable.setEvent_type((EventType.get(service.getEvent_type()) != null ? EventType
						.get(service.getEvent_type())
						: EventType.SUBSCRIPTION_PURCHASE));
				billable.setPricePointKeyword(service.getPrice_point_keyword());
				logger.debug(" before queue " + billable.getId());

				if (queueSize == 0) {// if we can add as many objects to the
										// queue, then we just keep adding

					// adds to the last of the queue.
					// We don't have limitations of the size of the queue,
					// so its least likely for there to be no space to add an
					// element.
					try {

						// Inserts the specified element at the end of this
						// deque if it is possible to do so immediately without
						// violating
						// capacity restrictions, returning true upon success
						// and false if no
						// space is currently available. When using a
						// capacity-restricted deque,
						// this method is generally preferable to the addLast
						// method, which can fail
						// to insert an element only by throwing an exception.
						billableQ.offerLast(billable);

						billable.setIn_outgoing_queue(1L);
						cmpbean.saveOrUpdate(billable);
						// celcomAPI.markInQueue(mtsms.getId());//change at 11th
						// March 2012 - I later realzed we still sending SMS
						// twice!!!!!!

					} catch (Exception e) {

						logger.error(e.getMessage(), e);

					}

				} else {// putLast waits for queue to have empty space before
						// adding new elements.

					// We have a restriction so we have to wait until
					// The queue has space in it to add an element.
					try {

						billableQ.putLast(billable);// if we've got a limit to
													// the queue

						billable.setIn_outgoing_queue(1L);
						cmpbean.saveOrUpdate(billable);
						// celcomAPI.markInQueue(mtsms.getId());//change at 11th
						// March 2012 - I fucking later realzed we still sending
						// SMS twice!!!!!!

						// addedToqueue = true;

					} catch (InterruptedException e) {

						logger.error(e.getMessage(), e);

					} catch (Exception e) {

						logger.error(e.getMessage(), e);

					}
				}

			}

		}

	}

	private void setRun(boolean b) {
		this.run = b;

	}

	public void myfinalize() {

		try {

			if (context != null)
				context.close();

		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
		} catch (RejectedExecutionException e1) {
			logger.error(e1.getMessage(), e1);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}
	}

}
