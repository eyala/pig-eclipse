package org.apache.pig.contrib.eclipse.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
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
	 */
	public SearchResult find(Set<String> files, Pattern find_macro) {
		Set<SearchResult> result = innerFind(files, find_macro);
		
		if (result.isEmpty()) {
			return null;
		}
		
		Iterator<SearchResult> iterator = result.iterator();
		
		SearchResult best = iterator.next();
		
		while (iterator.hasNext()) {
			SourceSearchResult candidate = (SourceSearchResult)iterator.next();
			
			// this is a heuristic to prefer files from a src folder over others. I wish I had an API for this.
			if(candidate.getPath().contains("src/")) {
				best = candidate;
			}
		}
		
		return best;
	}

	/**
	 * Finds all the matches for a given regular expression in a set of files in the workspace
	 * 
	 * @param files a list of files to be searched for
	 * @param find_macro the regex of what we're looking for
	 * @param recursive whether the search should be recursive - currently ignored
	 */
	public Set<String> findAll(Set<String> files, Pattern find_macro) {
		Set<SearchResult> innerFindInFiles = innerFind(files, find_macro);
		
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
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		UdfFinder visitor = new UdfFinder(udf, originalDef);
		
		try {
			root.accept(visitor, 0);
		} catch (CoreException e) {
			PigLogger.warn("Caught exception from visitor", e);
		}
		
		return visitor.getResult();
	}

	private Set<SearchResult> innerFind(Set<String> files, Pattern find_macro) {
		if (files.isEmpty()) {
			return EMPTY_SET;
		}

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		MacroFinder visitor = new MacroFinder(files, find_macro);
		
		try {
			root.accept(visitor, 0);
		} catch (CoreException e) {
			PigLogger.warn("Caught exception from visitor", e);
		}
		
		return visitor.getResult();
	}
	
	/**
	 * Return the imports defined in a given string and all imports in hierarchy
	 */
	public static Set<String> findRecursiveImports(String text) {
		
		Set<String> result = RegexUtils.findImports(text);
		Set<String> visited = new HashSet<String>();
		getRecursiveImports(result, result, visited);
		return result;
	}
	
	private static void getRecursiveImports(Set<String> newFiles, Set<String> result, Set<String> visited) {
		Set<String> filtered = new HashSet<String>();
		for(String s : newFiles)
		{
			if(!visited.contains(s))
			{
				filtered.add(s);
				visited.add(s);
			}
		}
		
		Set<String> rawNewNames = new WorkspaceSearcher().findAll(filtered, RegexUtils.FIND_IMPORTS);
		Set<String> newNames = new HashSet<String>();
		
		for (String i : rawNewNames) 
		{
			Matcher m = RegexUtils.GET_FILENAME_FROM_IMPORT.matcher(i);
			
			if (!m.find()) 
			{
				PigLogger.info("Encountered unexpected import string: " + i);
			} else {
				newNames.add(m.group(1));
			}
		}
		
		result.addAll(newNames);
		
		// Keep recursive call as last command for compiler optimization
		if(newNames.size() > 0)
		{
			getRecursiveImports(newNames, result, visited);
		}
	}

}
