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
			// currently, this never seems to find anything, so its commented out
			// javadoc = foundUdf.getAttachedJavadoc(null);
			
			// if (javadoc == null) {
				javadoc = getJavadocFromSource(foundUdf.getSource());
			//}
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
	
	/**
	 * Determines whether java source contains a class-level javadoc, and makes it
	 * presentable for the Eclipse's hoverInfo, which expects something html-like
	 */
	private String getJavadocFromSource(String source) {

		if (source != null) {
			int start = source.indexOf("/**");
			int end = source.indexOf("*/", start);
			int public_index = source.indexOf("public", end);
			int class_index = source.indexOf("class", public_index);

			// these conditions are sanity checks that the javadoc is really on the class level
			if (end > start && public_index > end && class_index > public_index) {
				String javadoc = source.substring(start, end + 3); // add the closing of the multiline comment
				
				// if there are no <br> tags, we need to replace newlines so the javadoc looks normal
				if (! javadoc.contains("<br/>")) {
					javadoc = javadoc.replaceAll("\n", "<br/>"); 
				}
				
				// add the full package name in the top, like a real javadoc
				return "<b>" + className + "</b><br/><br/>" + javadoc;
			}
		}

		return null;
	}

}