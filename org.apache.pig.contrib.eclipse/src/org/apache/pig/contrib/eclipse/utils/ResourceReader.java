package org.apache.pig.contrib.eclipse.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Read Pig language definition files for the plugin
 */
public class ResourceReader {

	/**
	 * Given a path, reads a file, removing duplicate lines
	 */
	public static Set<String> read(String path) {
		Set<String> result= new HashSet<String>();
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new InputStreamReader(FileLocator.openStream(Platform.getBundle("org.apache.pig.contrib.eclipse"), new Path("data" + File.separator + path ), false)));

			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() != 0) {
					result.add(line);
				}
			}
			
		} catch (IOException e) {
			System.err.println("Failed to read resources: " + path);
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					System.err.println("Unable to close data files");
					e.printStackTrace();
				}
			}
		}

		return result;
	}

}
