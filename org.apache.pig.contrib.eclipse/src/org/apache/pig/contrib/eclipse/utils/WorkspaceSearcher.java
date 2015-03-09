package org.apache.pig.contrib.eclipse.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class WorkspaceSearcher {
	
	static int numOfSearches = 0;
	static int totalTimeOfSearches = 0;
	
	private static Set<SearchResult> EMPTY_SET = new HashSet<SearchResult>();

	/**
	 * Finds the first match for a given regular expression in a set of files in the workspace
	 * 
	 * @param files a list of files to be searched for
	 * @param find_macro the regex of what we're looking for
	 * @param recursive whether the search should be recursive - currently ignored
	 */
	public SearchResult find(Set<String> files, Pattern find_macro, boolean recursive) {
		Set<SearchResult> result = innerFind(files, find_macro, false, recursive);
				
		return result.isEmpty() ? null : result.iterator().next();
	}

	/**
	 * Finds all the matches for a given regular expression in a set of files in the workspace
	 * 
	 * @param files a list of files to be searched for
	 * @param find_macro the regex of what we're looking for
	 * @param recursive whether the search should be recursive - currently ignored
	 */
	public Set<String> findAll(Set<String> files, Pattern find_macro, boolean recursive) {
		Set<SearchResult> innerFindInFiles = innerFind(files, find_macro, false, recursive);
		
		Set<String> result = new HashSet<String>();
		
		for (SearchResult i : innerFindInFiles) {
			result.add(i.getText());
		}
		
		return result;
	}

	/**
	 * Find a java file that matches a given fully qualified class name
	 * 
	 * @param udf the fully qualified name of the udf
	 * @param originalDef a string to be used in case the javadoc can't be found
	 */
	public SearchResult findUdf(String udf, String originalDef) {
		long start = System.currentTimeMillis();
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		UdfFinder visitor = new UdfFinder(udf, originalDef);
		
		try {
			root.accept(visitor, 0);
		} catch (CoreException e) {
			PigLogger.warn("Caught exception from visitor", e);
		}
		
		long end = System.currentTimeMillis();

		numOfSearches++;
		totalTimeOfSearches += (end-start);

		PigLogger.debug("Searching workspace took " + (end-start) + " ms (" + (totalTimeOfSearches / numOfSearches) + " avg time)");
		
		return visitor.getResult();
	}

	private Set<SearchResult> innerFind(Set<String> files, Pattern find_macro, boolean justOneResult, boolean recursive) {

		if (files.isEmpty()) {
			return EMPTY_SET;
		}

		long start = System.currentTimeMillis();
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		MacroFinder visitor = new MacroFinder(files, find_macro, justOneResult);
		
		try {
			root.accept(visitor, 0);
		} catch (CoreException e) {
			PigLogger.warn("Caught exception from visitor", e);
		}
		
		long end = System.currentTimeMillis();

		numOfSearches++;
		totalTimeOfSearches += (end-start);

		PigLogger.debug("Searching workspace took " + (end-start) + " ms (" + (totalTimeOfSearches / numOfSearches) + " avg time) after searching " + visitor.getFoldersSearched() + " folders and " + visitor.getFilesSearched() + " files");
		
		return visitor.getResult();
	}
}
