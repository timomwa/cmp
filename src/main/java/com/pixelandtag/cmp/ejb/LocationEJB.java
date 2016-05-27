package com.pixelandtag.cmp.ejb;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.pixelandtag.dating.entities.Location;
import com.pixelandtag.dating.entities.Person;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileLocation;
import com.pixelandtag.web.beans.RequestObject;

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
public class LocationEJB extends BaseEntityBean implements LocationBeanI{
	
	public Logger logger = Logger.getLogger(LocationEJB.class);
	
	
	@Resource
	@PersistenceContext(unitName = "EjbComponentPU4")
	private EntityManager em;
	
	@EJB
	DatingServiceI dating_ejb;
	
	
	public LocationEJB() throws KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyStoreException {
		super();
	}
	
	public CellIdRanges getCellIdRangesByLocationId(Long location_id) throws Exception{
		
		CellIdRanges cellRanges = null;
		
		try{
			Query query = null;
			
			try{
				query = em.createQuery("from CellIdRanges r WHERE r.location_id=:location_id ");
				query.setFirstResult(0);
				query.setMaxResults(1);
				query.setParameter("location_id", location_id);
				cellRanges = (CellIdRanges) query.getSingleResult();
			}catch(javax.persistence.NoResultException ex){
				logger.warn(ex.getMessage());
			}catch(Exception exp){
				logger.error(exp.getMessage(),exp);
			}
			
			if(cellRanges==null){
				query = em.createQuery("SELECT coalesce(max(loc.cellid),0), coalesce(min(loc.cellid),0) from Location loc WHERE loc.location_id=:location_id ");
				query.setFirstResult(0);
				query.setMaxResults(1);
				query.setParameter("location_id", location_id);
				Object[] o =  (Object[]) query.getSingleResult();
				
				Long max_cell_id = (Long) o[0];
				Long min_cell_id = (Long) o[1];
				logger.info("MAX_CELL_ID :"+max_cell_id.intValue()+", MIN_CELL_ID : "+min_cell_id.intValue()+", LOCATION ID? "+location_id);
				if(max_cell_id!=null){
					cellRanges = new CellIdRanges();
					cellRanges.setMax_cell_id(max_cell_id);
					cellRanges.setMin_cell_id(min_cell_id);
					cellRanges.setLocation_id(location_id);
					cellRanges = saveOrUpdate(cellRanges);
				}
			}
			
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage());
			throw new Exception("Problem finding location",exp);
		}
		
		return cellRanges;
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.LocationBeanI#createLocation(java.lang.Long, java.lang.Long, java.lang.String, com.pixelandtag.dating.entities.PersonDatingProfile, boolean)
	 */
	public ProfileLocation findOrCreateLocation(Long cellid, Long locationId, String locationName,
			PersonDatingProfile profile, boolean authoritative) throws Exception {
		
		try{
		
			Location location = findLocation(cellid,locationId);
			
			ProfileLocation profLoc = findProfileLocation(location,profile);
			
			if(profLoc!=null)
				return profLoc;
			
			
			if(location==null){
				location = new Location();
				location.setCellid(cellid);
				location.setLocation_id(locationId);
				if(locationName==null || locationName.trim().isEmpty()){
					Location interimLoc = getLastKnownLocationWithNameUsingLac(locationId, cellid);
					if(interimLoc!=null)
						location.setLocationName(interimLoc.getLocationName());
					else
						location.setLocationName(profile.getLocation());
				}else{
					location.setLocationName(locationName);
				}
			}
			
			location.setAuthoritative(authoritative);
			location = saveOrUpdate(location);
			
			if(profLoc==null){
				profLoc = new ProfileLocation();
				profLoc.setLocation(location);
				profLoc.setProfile(profile);
				profLoc = saveOrUpdate(profLoc);
			}
			return profLoc;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	
	@Override
	public void deleteProfileLocations(PersonDatingProfile profile){
		try{
			Query query = em.createQuery("delete from ProfileLocation pl WHERE pl.profile=:profile");
			query.setParameter("profile", profile);
			query.executeUpdate();
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage());
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.LocationBeanI#getLastKnownLocationWithNameUsingLac(java.lang.Long)
	 */
	@Override
	public Location getLastKnownLocationWithNameUsingLac(Long locationId, Long cellid) {
		
		Location location = null;
		
		try{
			Query query = em.createQuery("from Location l WHERE l.locationName is not null AND l.locationName <> '' AND l.authoritative=:authoritative AND l.location_id=:location_id AND l.cellid =:cellid order by l.timeStamp desc");
			query.setFirstResult(0);
			query.setMaxResults(1);
			query.setParameter("location_id", locationId);
			query.setParameter("cellid", cellid);
			query.setParameter("authoritative", Boolean.TRUE);
			location = (Location) query.getSingleResult();
			
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage());
		}
		
		return location;
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
			logger.warn(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage());
			throw new Exception("Problem finding location",exp);
		}
		return profileLocation;
	}

	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.LocationBeanI#findLocation(java.lang.Long, java.lang.Long, java.lang.String)
	 */
	@Override
	public Location findLocation(Long cellid, Long locationId) throws Exception{
		
		Location location = null;
		
		try{
			Query query = em.createQuery("from Location l WHERE l.cellid=:cellid AND l.location_id=:locationid order by l.timeStamp desc");
			query.setFirstResult(0);
			query.setMaxResults(1);
			
			query.setParameter("cellid", cellid);
			query.setParameter("locationid", locationId);
			
			location = (Location) query.getSingleResult();
			
		}catch(javax.persistence.NoResultException ex){
			logger.warn(ex.getMessage());
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
			logger.warn(ex.getMessage());
		}catch(Exception exp){
			logger.error(exp.getMessage(),exp);
			throw new Exception("Problem finding profile location", exp);
		}
		
		return profLoc;
	}
	
	
	/* (non-Javadoc)
	 * @see com.pixelandtag.cmp.ejb.LocationBeanI#updateSubscriberLocation(com.pixelandtag.web.beans.RequestObject)
	 */
	public void updateSubscriberLocation(RequestObject ro){
		try {
			Long cellid = Long.valueOf(ro.getCellid());
			Long locationid = Long.valueOf(ro.getLac());
			PersonDatingProfile prof =  dating_ejb.getProfile(ro.getMsisdn());
			if(prof!=null && prof.getProfileComplete())
				findOrCreateLocation(cellid,locationid,ro.getLocation(),prof,false);
			
		} catch (Exception exp) {
			logger.error(exp.getMessage(),exp);
		}
		
	}

}
