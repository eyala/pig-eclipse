package org.apache.pig.contrib.eclipse.utils;

import org.eclipse.core.resources.IFile;

/**
 * Concrete class representing either a local search result or one from a given IFile 
 */
public class SourceSearchResult extends SearchResult {

	private final IFile file;
	
	public SourceSearchResult(int start, int length, IFile file, String text) {
		super(start, length, text);
		this.file = file;
	}
	
	public SourceSearchResult(int start, IFile file, String text) {
		this(start, text.length(), file, text);
	}

	/**
	 * Used by @WorkspaceSearcher to prefer results from src folders over others
	 */
	String getPath() {
		return file.getProjectRelativePath().toString();
	}
	
	@Override
	public boolean go() {
		return go(file);
	}
}