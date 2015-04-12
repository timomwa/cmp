package com.pixelandtag.cmp.ejb;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

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

import com.pixelandtag.dating.entities.Location;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileLocation;

/**
 * 
 * @author Timothy Mwangi Gikonyo
 * date created 12th April 2015
 * Deals with crud and search for location
 * 
 *
 */
@Stateless
@Remote
@TransactionManagement(TransactionManagementType.BEAN)
public class LocationEJB extends BaseEntityBean implements LocationBeanI{
	
	public Logger logger = Logger.getLogger(LocationEJB.class);
	
	
	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	

	@Resource
	private UserTransaction utx;
	
	public LocationEJB() throws KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyStoreException {
		super();
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.LocationBeanI#createLocation(java.lang.Long, java.lang.Long, java.lang.String, com.pixelandtag.dating.entities.PersonDatingProfile)
	 */
	public ProfileLocation findOrCreateLocation(Long cellid, Long locationId, String locationName,
			PersonDatingProfile profile) throws Exception {
		
		Location location = findLocation(cellid,locationId,locationName);
				
		if(location==null){
			location = new Location();
			location.setCellid(cellid);
			location.setLocation_id(locationId);
			location.setLocationName(locationName);
			location = saveOrUpdate(location);
		}
		
		ProfileLocation profLoc = findProfileLocation(location,profile);
		
		if(profLoc==null){
			profLoc = new ProfileLocation();
			profLoc.setLocation(location);
			profLoc.setProfile(profile);
			profLoc = saveOrUpdate(profLoc);
		}
		return profLoc;
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.LocationBeanI#findProfileLocation(com.pixelandtag.dating.entities.Location, com.pixelandtag.dating.entities.PersonDatingProfile)
	 */
	public ProfileLocation findProfileLocation(Location location,
			PersonDatingProfile profile) throws Exception{
		
		ProfileLocation profileLocation = null;
		try{
			Query query = em.createQuery("from ProfileLocation pl WHERE pl.location=:location AND pl.profile=:profile ");
		
			query.setFirstResult(0);
			query.setMaxResults(1);
			
			query.setParameter("location", location);
			query.setParameter("profile", profile);
			
			profileLocation = (ProfileLocation) query.getSingleResult();
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage());
			throw new Exception("Problem finding location",exp);
		}
		return profileLocation;
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.LocationBeanI#findLocation(java.lang.Long, java.lang.Long, java.lang.String)
	 */
	public Location findLocation(Long cellid, Long locationId,
			String locationName) throws Exception{
		
		Location location = null;
		
		try{
			Query query = em.createQuery("from Location l WHERE l.cellid=:cellid AND l.location_id=:locationid AND l.locationName=:locationName order by l.timeStamp desc");
			query.setFirstResult(0);
			query.setMaxResults(1);
			
			query.setParameter("cellid", cellid);
			query.setParameter("locationid", locationId);
			query.setParameter("locationName", locationName);
			
			location = (Location) query.getSingleResult();
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage());
			throw new Exception("Problem finding location",exp);
		}
		
		return location;
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.LocationBeanI#findLocation(com.pixelandtag.dating.entities.PersonDatingProfile)
	 */
	public ProfileLocation findProfileLocation(PersonDatingProfile profile) throws Exception{
		ProfileLocation profLoc = null;
		try{
			
			Query query = em.createQuery("from ProfileLocation pl WHERE pl.profile=:profile order by pl.timeStamp desc");
			query.setFirstResult(0);
			query.setMaxResults(1);
			
			query.setParameter("profile", profile);
			
			profLoc = (ProfileLocation) query.getSingleResult();
			
		}catch(javax.persistence.NoResultException ex){
			logger.error(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage());
			throw new Exception("Problem finding profile location", exp);
		}
		
		return profLoc;
	}

}
