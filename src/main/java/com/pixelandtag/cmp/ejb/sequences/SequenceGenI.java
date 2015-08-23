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
	 * Gets a sequence, increments the value
	 * @param name - The name of sequence you want to find
	 * @return com.pixelandtag.cmp.entities.CMPSequence
	 */
	public CMPSequence getOrCreateNextSequence(String name) throws CMPSequenceException;
}
