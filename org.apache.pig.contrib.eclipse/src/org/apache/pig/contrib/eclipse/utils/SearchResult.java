package org.apache.pig.contrib.eclipse.utils;

import org.eclipse.core.resources.IFile;

public class SearchResult {

	/**
	 * offset from original text of beginning of match
	 */
	public final int start;
	
	/**
	 * file where match was found 
	 */
	public final IFile file;

	/**
	 * matching text
	 */
	public final String text;
	
	public SearchResult(int start, IFile file, String text) {
		this.start = start;
		this.file = file;
		this.text = text;
	}
}