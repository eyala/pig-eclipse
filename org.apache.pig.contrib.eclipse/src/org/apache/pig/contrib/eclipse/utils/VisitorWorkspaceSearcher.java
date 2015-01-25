package org.apache.pig.contrib.eclipse.utils;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class VisitorWorkspaceSearcher implements WorkspaceSearcher {
	
	@Override
	public String findInFiles(Set<String> files, Pattern find_macro, boolean recursive) {
		Set<String> result = innerFindInFiles(files, find_macro, false, recursive);
				
		return result.isEmpty() ? null : result.iterator().next();
	}

	@Override
	public Set<String> findAllInFiles(Set<String> files, Pattern find_macro, boolean recursive) {
		return innerFindInFiles(files, find_macro, false, recursive);
	}

	private Set<String> innerFindInFiles(Set<String> files, Pattern find_macro, boolean justOneResult, boolean recursive) {
		
		long start = System.currentTimeMillis();
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		MacroFinder visitor = new MacroFinder(files, find_macro, justOneResult);
		
		try {
			root.accept(visitor, 0);
		} catch (CoreException e) {
			PigLogger.warn("Caught exception from visitor", e);
		}
		
		long end = System.currentTimeMillis();
			
		PigLogger.info("Searching workspace took " + (end-start) + " ms\n after searching " + visitor.getFoldersSearched() + " folders and " + visitor.getFilesSearched() + " files\n");
		
		return visitor.getResult();
	}
}
