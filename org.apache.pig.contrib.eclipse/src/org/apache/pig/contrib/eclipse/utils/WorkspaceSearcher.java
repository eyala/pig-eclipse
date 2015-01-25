package org.apache.pig.contrib.eclipse.utils;

import java.util.Set;
import java.util.regex.Pattern;

public interface WorkspaceSearcher {

	/**
	 * Finds the first match for a given regular expression in a set of files in the workspace
	 * 
	 * @param files a list of files to be searched for
	 * @param find_macro the regex of what we're looking for
	 * @param recursive whether the search should be recursive - currently ignored
	 */
	String findInFiles(Set<String> files, Pattern find_macro, boolean recursive);

	/**
	 * Finds all the matches for a given regular expression in a set of files in the workspace
	 * 
	 * @param files a list of files to be searched for
	 * @param find_macro the regex of what we're looking for
	 * @param recursive whether the search should be recursive - currently ignored
	 */
	Set<String> findAllInFiles(Set<String> files, Pattern find_macro, boolean recursive);

}
