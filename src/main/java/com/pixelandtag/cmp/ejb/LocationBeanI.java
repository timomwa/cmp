package com.pixelandtag.cmp.ejb;

import com.pixelandtag.dating.entities.Location;
import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileLocation;
import com.pixelandtag.web.beans.RequestObject;

public interface LocationBeanI extends BaseEntityI {
	
	/**
	 * When a subscriber makes a request and the operator
	 * has provided us with the location parameters, we 
	 * save the location using this method.
	 * 
	 * @param cellid - java.lang.Long - operator provided cell id
	 * @param locationId -  java.lang.Long - operator provided location id
	 * @param locationName - java.lang.String - location name as provided by the subscriber
	 * @param profile - com.pixelandtag.dating.entities.PersonDatingProfile 
	 * @return ProfileLocation
	 * @throws Exception
	 */
	public ProfileLocation findOrCreateLocation(Long cellid, Long locationId, String locationName,PersonDatingProfile profile) throws Exception;

	/**
	 * Finds Location using the cellid
	 * location id and location name
	 * @param cellid - java.lang.Long - operator provided cell id
	 * @param locationId - java.lang.Long - operator provided location id
	 * @param locationName - java.lang.String - location name as provided by the subscriber
	 * @return
	 * @throws Exception
	 */
	public Location findLocation(Long cellid, Long locationId,
			String locationName) throws Exception;
	
	/**
	 * Gets the profile's location object
	 * 
	 * @param location - com.pixelandtag.dating.entities.Location
	 * @param profile - com.pixelandtag.dating.entities.PersonDatingProfile
	 * @return com.pixelandtag.dating.entities.ProfileLocation
	 * @throws Exception
	 */
	public ProfileLocation findProfileLocation(Location location,
			PersonDatingProfile profile) throws Exception;

	/**
	 * Searches the latest subscriber's 
	 * location - 
	 * @param profile - com.pixelandtag.dating.entities.PersonDatingProfile
	 * @return - com.pixelandtag.dating.entities.ProfileLocation
	 * @throws Exception
	 */
	public ProfileLocation findProfileLocation(PersonDatingProfile profile) throws Exception;
	
	
	/**
	 * Straight from the ussd request.
	 * this one updates
	 * @param ro
	 */
	public void updateSubscriberLocation(RequestObject ro);
	
	/**
	 * Gets the cell id ranges for a given location id.
	 * @param location_id
	 * @return
	 * @throws Exception
	 */
	public CellIdRanges getCellIdRangesByLocationId(Long location_id) throws Exception;
	
	/**
	 * 
	 * @param locationId
	 * @return
	 */
	public Location getLastKnownLocationWithNameUsingLac(Long locationId) ;
	
}

