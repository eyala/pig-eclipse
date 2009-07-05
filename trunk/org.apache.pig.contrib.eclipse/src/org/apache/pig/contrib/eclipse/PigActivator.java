package org.apache.pig.contrib.eclipse;

import org.apache.pig.contrib.eclipse.editors.PigColorProvider;
import org.apache.pig.contrib.eclipse.editors.PigScriptScanner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PigActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.apache.pig.contrib.eclipse";

	// The shared instance
	private static PigActivator plugin;

	private PigColorProvider colorProvider;

	private PigScriptScanner codeScanner;

	/**
	 * The constructor
	 */
	public PigActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static PigActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public PigColorProvider getColorProvider() {
		if (colorProvider == null)
			colorProvider = new PigColorProvider();
		return colorProvider;
	}

	public PigScriptScanner getCodeScanner() {
		if (codeScanner == null) {
			codeScanner = new PigScriptScanner(getColorProvider());
		}
		return codeScanner;
	}
}
