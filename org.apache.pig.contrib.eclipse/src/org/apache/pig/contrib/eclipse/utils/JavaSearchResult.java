package org.apache.pig.contrib.eclipse.utils;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.PartInitException;

public class JavaSearchResult extends SearchResult {

	private final IType foundUdf;
	
	/**
	 * the fully qualified class name
	 */
	private final String className;

	public JavaSearchResult(IType foundUdf, String originalDef) {
		super(0, 0, originalDef);
		
		this.foundUdf = foundUdf;
		className = foundUdf.getFullyQualifiedName();
	}

	@Override
	public String getText() {
		String javadoc = null;
		
		try {
			javadoc = foundUdf.getAttachedJavadoc(null);
			
			if (javadoc == null) {
				PigLogger.debug("Searching in class file for javadoc for " + className);
				
				IClassFile classFile = foundUdf.getClassFile();
				
				if (classFile != null) {
					javadoc = classFile.getAttachedJavadoc(null);
	
					if (javadoc == null) {
						javadoc = getJavadocFromSource(classFile.getSource(), true);
					}
				} else {
					javadoc = getJavadocFromSource(foundUdf.getSource(), false);
				}
			}
		} catch (JavaModelException e) {
			PigLogger.warn("Exception while trying to get javadoc for " + className, e);
		}
		
		if (javadoc != null && ! javadoc.isEmpty()) {
			PigLogger.debug("Found javadoc for " + className);
			return javadoc;
		} else {
			return super.getText();
		}
	}
	
	@Override
	public boolean go() {
		IResource resource = foundUdf.getResource();
		
		// if we have an IFIle, use the default implementation
		if (resource != null && resource.getType() == IResource.FILE) {
			PigLogger.debug("Found IFile for " + className);
			return go((IFile)resource);
		}
	
		IClassFile classFile = foundUdf.getClassFile();
		
		if (classFile != null) {
			IType type = classFile.getType();
		
			PigLogger.debug("Attempting to open using JAVA API: " + className);
			
			try {
				JavaUI.openInEditor(type);
				return true;
			} catch (PartInitException e) {
				PigLogger.warn("Exception while trying to go to file of " + className, e);
			} catch (JavaModelException e) {
				PigLogger.warn("Exception while trying to go to file of " + className, e);
			}
		}
		
		return false;
	}
	
	private String getJavadocFromSource(String source, boolean replaceNewlines) {
		PigLogger.debug("Trying to retrieve javadoc from source for " + className);

		if (source != null) {
			int start = source.indexOf("/**");
			int end = source.indexOf("*/", start);
			
			if (end > start) {
				String javadoc = source.substring(start, end);
				
				return replaceNewlines ? javadoc.replaceAll("\n", "<br/>") : javadoc;
			}
		}

		return null;
	}

}