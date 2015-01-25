package org.apache.pig.contrib.eclipse;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class PigLogger {

	final static ILog LOG = PigActivator.getDefault().getLog();
	
	public static void debug(String msg) {
		LOG.log(new Status(IStatus.OK, PigActivator.PLUGIN_ID, msg));
	}

	public static void info(String msg) {
		LOG.log(new Status(IStatus.INFO, PigActivator.PLUGIN_ID, msg));
	}

	public static void warn(String msg, Throwable te) {
		LOG.log(new Status(IStatus.WARNING, PigActivator.PLUGIN_ID, msg, te));
	}
}
