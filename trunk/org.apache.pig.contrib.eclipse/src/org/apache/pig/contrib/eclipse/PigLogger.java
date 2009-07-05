package org.apache.pig.contrib.eclipse;

import org.eclipse.core.runtime.IStatus;

public class PigLogger {

	public static void log(IStatus status) {
		PigActivator.getDefault().getLog().log(status);
	}
}
