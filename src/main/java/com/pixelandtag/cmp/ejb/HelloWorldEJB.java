package com.pixelandtag.cmp.ejb;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.pixelandtag.sms.producerthreads.HelloWorldData;

@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class HelloWorldEJB extends BaseEntityBean implements HelloWorldI {

	public HelloWorldEJB() throws KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyStoreException {
		super();
		// TODO Auto-generated constructor stub
	}

	public Logger logger = Logger.getLogger(DatingServiceBean.class);

	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;

	@Resource
	private UserTransaction utx;

	@Override
	public String reply(String msisdn, String moMsg) {
		String responce = "No previous message.";
		Query hwdq = em
				.createQuery("from HelloWorldData order by timeStamp desc");
		hwdq.setFirstResult(0);
		hwdq.setMaxResults(1);

		List<HelloWorldData> list = hwdq.getResultList();
		if (list != null && list.size() != 0) {
			responce = list.get(0).getMo();
		}

		HelloWorldData hwd = new HelloWorldData();
		hwd.setMsisdn(msisdn);
		hwd.setMo(moMsg);
		try {
			hwd = saveOrUpdate(hwd);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return responce;
	}
}
