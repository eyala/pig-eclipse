package org.apache.pig.contrib.eclipse.utils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.eclipse.core.resources.ResourcesPlugin;

public class RegexUtils {

	// Beginning of macro definition - just the "define" and whitespace
	private static final String DEFINE_PREFIX = "(?i)DEFINE\\s+";

	// Parameters of macro definition - everything after the macro name, not including closing brace --> (param1, param2) returns retval1, retval2 
	private static final String MACRO_DEF_PARAMS = "\\s*\\((\\s*\\w*\\s*,?\\s*)*\\)\\s*(?i)RETURNS\\s*.+?\\s*";

	// Used for finding macro/udf/streaming definitions for auto completion
	public static final Pattern FIND_DEFINES = Pattern.compile(DEFINE_PREFIX + "(\\w+)");
	
	// Used for finding imports for searching workspace
	public static final Pattern FIND_IMPORTS = Pattern.compile("(?i)IMPORT\\s+'(.+?)'");

	// Used for finding some of the relations defined (not those in SPLIT; also suggests those within macros that aren't in scope)
	private static final Pattern FIND_RELATIONS = Pattern.compile(";?\\s*(\\w+?)\\s*=");

	// Used for finding a SPLIT command and the first and last relations defined within
	private static final Pattern FIND_SPLIT = Pattern.compile("(?m);?(?i)SPLIT\\s*\\w+\\s*INTO\\s*(\\w+)\\s*IF\\s*.*?(?:,\\s*\\w+\\s*IF\\s*.*?)*(?:,\\s*(\\w+)\\s*OTHERWISE)?;");

	// Used for finding the other relations defined within a SPLIT command
	private static final Pattern FIND_RELATIONS_IN_SPLIT = Pattern.compile(",\\s*(\\w+)\\s*IF");

	 //  Captures only the file name, ignoring relative and absolute paths.
	 //  We don't want to use File.separator because the editor might run on a different system from the Pig script.
	public static final Pattern GET_FILENAME_FROM_IMPORT = Pattern.compile("(?:.*[\\/\\\\])?(.+)");
	
	/**
	 * Return the imports defined in a given string
	 */
	public static Set<String> findImports(String text) {
		Set<String> rawImports = findThings(text, FIND_IMPORTS, 1);
		
		Set<String> result = new HashSet<String>();
		
		for (String i : rawImports) {
			Matcher m = GET_FILENAME_FROM_IMPORT.matcher(i);
			
			if (!m.find()) {
				PigLogger.info("Encountered unexpected import string: " + i);
			} else {
				result.add(m.group(1));
			}
		}
		
		return result;
	}

	/**
	 * Attempts to find macros that are locally defined in a given string
	 */
	public static Set<String> findDefines(String text) {
		return findThings(text, FIND_DEFINES, 1);
	}

	/**
	 * Attempts to find simple relations defined in a given string. 
	 */
	public static Set<String> findRelations(String mostOfDoc) {
		return findThings(mostOfDoc, FIND_RELATIONS, 1);
	}

	/**
	 * Attempts to find simple relations defined in a given string. 
	 */
	public static Set<String> findRelationsFromSplit(String mostOfDoc) {
		Matcher m = FIND_SPLIT.matcher(mostOfDoc);
		
		Set<String> results = new HashSet<String>();
		
		while (m.find()) {
			// add first relation and relation from OTHERWISE, if it exists
			results.add(m.group(1));
			
			if (m.groupCount()>1) {
				results.add(m.group(2));
			}
			
			// now add all the other relations 
			Matcher innerMatcher = FIND_RELATIONS_IN_SPLIT.matcher(m.group());
			
			while (innerMatcher.find()) {
				results.add(innerMatcher.group(1));	
			}
		}
		
		return results;
	}
	
	// This is the pattern for finding macro definitions in imports
	public static Pattern findMacroDefinesForHoverInfoPattern(String name) {
		return Pattern.compile("(" + DEFINE_PREFIX + "(?-i)" + name + MACRO_DEF_PARAMS + ")\\{");
	}

	// This is the pattern for finding local DEFINES
	public static Pattern findNonMacroDefinesForHoverInfoPattern(String name) {
		return Pattern.compile(DEFINE_PREFIX + "(?-i)" + name + "\\s*(.+?);");
	}

	private static Set<String> findThings(String text, Pattern pattern, int groupNum) {
		Matcher m = pattern.matcher(text);
	
		Set<String> results = new HashSet<String>();
		
		while (m.find()) {
			results.add(m.group(groupNum));
		}
		
		return results;
	}
}
