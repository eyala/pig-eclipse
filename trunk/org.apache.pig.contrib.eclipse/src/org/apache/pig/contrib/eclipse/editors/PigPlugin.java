package org.apache.pig.contrib.eclipse.editors;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class PigPlugin extends AbstractUIPlugin {

	private static PigPlugin instance = null;

	private static PigColorProvider colorProvider;

	private static PigScriptScanner codeScanner;

	static {
		instance = new PigPlugin();
		colorProvider = new PigColorProvider();
		codeScanner = new PigScriptScanner(colorProvider);
	}

	public static PigPlugin getDefault() {
		// TODO Auto-generated method stub
		return instance;
	}

	public static PigColorProvider getColorProvider() {
		return colorProvider;
	}

	public static PigScriptScanner getCodeScanner() {
		return codeScanner;
	}
}
