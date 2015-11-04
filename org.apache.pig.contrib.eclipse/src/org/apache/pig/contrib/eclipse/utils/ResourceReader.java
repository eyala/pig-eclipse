package org.apache.pig.contrib.eclipse.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Read Pig language definition files
 * 
 * Because not everything changes in between versions, we map between a version and the last change in the relevant category
 */
public class ResourceReader {

	private final static Map<String, String> LAST_CHANGE_IN_DATA_TYPES;
	private final static Map<String, String> LAST_CHANGE_IN_BUILTINS;
	private final static Map<String, String> LAST_CHANGE_IN_KEYWORDS;
	
	/*
	 * The 1st field in these maps is the selected pig version, and the 2nd field is which file has the last change in that part of pig
	 */
	static {
		String[][] last_change_in_data_types = {{"0.11", "0.11"}, {"0.12", "0.12"}, {"0.13", "0.12"}, {"0.14", "0.12"}, {"0.15", "0.12"}};
		LAST_CHANGE_IN_DATA_TYPES = makeMap(last_change_in_data_types);
		
		String[][] last_change_in_builtins = {{"0.11", "0.11"}, {"0.12", "0.12"}, {"0.13", "0.13"}, {"0.14", "0.14"}, {"0.15", "0.15"}};
		LAST_CHANGE_IN_BUILTINS = makeMap(last_change_in_builtins);

		String[][] last_change_in_keywords = {{"0.11", "0.11"}, {"0.12", "0.12"}, {"0.13", "0.12"}, {"0.14", "0.12"}, {"0.15", "0.12"}};
		LAST_CHANGE_IN_KEYWORDS = makeMap(last_change_in_keywords);
	}
	
	public static Set<String> readDataTypes(String version) {
		return read("data_types_" + LAST_CHANGE_IN_DATA_TYPES.get(version) + ".txt");
	}

	public static Set<String> readBuiltIns(String version) {
		return read("builtin_fun_" + LAST_CHANGE_IN_BUILTINS.get(version) + ".txt");
	}

	public static Set<String> readKeywords(String version) {
		return read("keywords_" + LAST_CHANGE_IN_KEYWORDS.get(version) + ".txt");
	}

	public static Set<String> readTooltips(String version) {
		return read("tooltips_0.14" + ".txt");
	}
	
	/**
	 * Given a path, reads a file, removing duplicate lines
	 */
	private static Set<String> read(String path) {
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

	private static Map<String, String> makeMap (String[][] array) {
		Map<String, String> result = new HashMap<String, String>();
		
		for (String[] i : array) {
			result.put(i[0], i[1]);
		}
		
		return result;
	}
}
