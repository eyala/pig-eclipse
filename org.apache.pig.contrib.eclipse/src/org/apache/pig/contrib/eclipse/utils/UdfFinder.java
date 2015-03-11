package org.apache.pig.contrib.eclipse.utils;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

public class UdfFinder implements IResourceProxyVisitor {

	private SearchResult result = null;
	private final String udfName;
	private final String originalDef;
	

	public UdfFinder(String name, String def) {
		udfName = name;
		originalDef = def;
	}
	
	@Override
	public boolean visit(IResourceProxy proxy) throws CoreException {
		
		// stop searching after the first result
		if (result != null) {
			return false;
		} else if (proxy.getType() == IResource.ROOT) {
			return true;
		} else if (proxy.getType() == IResource.PROJECT && proxy.isAccessible()) {
			IProject p = (IProject) proxy.requestResource();

			if (p.hasNature(JavaCore.NATURE_ID)) {
				
				PigLogger.debug("Searching java project " + proxy.getName() + " for udf class " + udfName);

				IJavaProject jp = JavaCore.create(p);

				IType foundUdf = jp.findType(udfName);
				
				if (foundUdf != null) {
					PigLogger.debug("Found class " + udfName + " within project " + proxy.getName());

					result = new JavaSearchResult(foundUdf, originalDef);
				}
		    }
		}
		
		return false;
	}

	public SearchResult getResult() {
		return result;
	}
}
