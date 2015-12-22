package com.pixelandtag.cmp.ejb.subscription;

public interface FreeLoaderEJBI {

	public void removeFromFreeloaderList(String msisdn) throws Exception;

	public boolean isInFreeloaderList(String msisdn) throws Exception;

}
