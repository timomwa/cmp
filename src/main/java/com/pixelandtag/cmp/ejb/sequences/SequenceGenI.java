package com.pixelandtag.cmp.ejb.sequences;

import com.pixelandtag.cmp.entities.CMPSequence;
import com.pixelandtag.cmp.exceptions.CMPSequenceException;
/**
 * Sequence gen local interface
 * @author Timothy
 *
 */
public interface SequenceGenI {
	
	
	/**
	 * @param name - the name of the sequence you want to find or create
	 * @param name - The name of sequence you want to find or create
	 * @return com.pixelandtag.cmp.entities.CMPSequence
	 * @throws CMPSequenceException
	 */
	//public CMPSequence getSequenceCreateIfNotExists(String name)  throws CMPSequenceException;
	/**
	 * 
	 * @param name - The name of sequence you want to find
	 * @return com.pixelandtag.cmp.entities.CMPSequence
	 */
	//public CMPSequence findSequenceByName(String name);
	
	
	/**
	 * Gets a sequence, increments the value
	 * @param name - The name of sequence you want to find
	 * @return com.pixelandtag.cmp.entities.CMPSequence
	 */
	public CMPSequence getOrCreateNextSequence(String name) throws CMPSequenceException;
}
