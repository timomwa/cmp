package com.pixelandtag.cmp.service;

import java.util.List;
/**
 * 
 * @author Mike Bavon.
 *
 */
public interface JsonService {
	/**
	 * 
	 * @param clazz
	 * @param data
	 * @return
	 */
	public <T> List<T> deserialize(Class<T> clazz, String data);
    /**
     * 
     * @param clazz
     * @param data
     * @param rootNode
     * @return
     */
	public <T> List<T> deserialize(Class<T> clazz, String data, String rootNode);

}
