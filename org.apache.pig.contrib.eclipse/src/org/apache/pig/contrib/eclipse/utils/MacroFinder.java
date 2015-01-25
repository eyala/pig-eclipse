package org.apache.pig.contrib.eclipse.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;

public class MacroFinder implements IResourceProxyVisitor {

	private final Set<String> imports;
	private final Pattern find_macro;
	private final Set<String> result = new HashSet<String>();
	
	private int folders = 0;
	private int files = 0;
	
	private boolean justOneResult;

	/**
	 * Add check if projects are referenced
	 * 
	 * @param imports
	 * @param find_macro
	 * @param result
	 */
	public MacroFinder(Set<String> imports, Pattern find_macro, boolean justOneResult) {
		this.imports = imports;
		this.find_macro = find_macro;
		this.justOneResult = justOneResult;
	}
	
	@Override
	public boolean visit(IResourceProxy proxy) throws CoreException {
		
		// quickly stop searching if you're only looking for one result and you already have it
		if (justOneResult && ! result.isEmpty()) {
			return false;
		}
		
		int type = proxy.getType();
		
		if (type != IResource.FILE) {
			folders++;
			return ! proxy.isDerived() && proxy.isAccessible();
		} else if (proxy.isAccessible() && imports.contains(proxy.getName())) {
			files++;
			
			IFile member = (IFile) proxy.requestResource();
			
			PigLogger.debug("Reading file " + member.getFullPath());
			
			BufferedReader reader = null;
			InputStream contents = null;
			
			try {
				contents = member.getContents();
			} catch (CoreException ce) {
				PigLogger.warn("Encountered and ignored core exception while preparing stream from file '" + member.getName() + "'", ce);
				
				try {
					member.refreshLocal(0, null);
					contents = member.getContents();
				} catch (CoreException e) {
					PigLogger.warn("Attempted to solve exception while preparing stream for file '" + member.getName() + "' by refreshing resource, but encountered exception", e);
					return false;
				}
			}
			
			try {
				reader = new BufferedReader(new InputStreamReader(contents));
				
				StringBuilder file = new StringBuilder();
				String line;
				
				while ((reader.ready())) {
					line = reader.readLine();
					file.append(line);
				}
					/*
					Matcher m = FIND_IMPORTS.matcher(line);
					
					if (m.matches()) {
						String tooltip = m.group(1);
						PigLogger.debug("Found more imports for " + tooltip + " within " + member.getName());
						//return tooltip;
					}
					*/

				Matcher m2 = find_macro.matcher(file);
				while (m2.find()) {
					String match = m2.group(1);
					PigLogger.debug("Found macro definition for '" + match + "' within " + member.getName());
					result.add(match);
					
					if (justOneResult) {
						return true;
					}
				}
			} catch (MalformedURLException e) {
				PigLogger.warn("Encountered exception while processing file", e);
			} catch (IOException e) {
				PigLogger.warn("Encountered exception while processing file", e);
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					PigLogger.warn("Encountered exception while closing file", e);
				}
			}
		}

		return false;
	}

	public Set<String> getResult() {
		return result;
	}

	public int getFoldersSearched() {
		return folders ;
	}
	
	public int getFilesSearched() {
		return files ;
	}

}
