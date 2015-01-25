package org.apache.pig.contrib.eclipse.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pig.contrib.eclipse.PigLogger;

public class RegexUtils {

	// Beginning of macro definition - just the "define" and whitespace
	private static final String MACRO_DEF_PREFIX = "(?i)define\\s+";

	// Parameters of macro definition - everything after the macro name, not including closing brace --> (param1, param2) returns retval1, retval2 
	private static final String MACRO_DEF_PARAMS = "\\s*\\((\\s*\\w*\\s*,?\\s*)*\\)\\s*(?i)returns\\s*.+?\\s*";

	// Used for finding macro definitions for auto completion
	public static final Pattern FIND_DEFINES = Pattern.compile(MACRO_DEF_PREFIX + "(\\w+?)" + MACRO_DEF_PARAMS + "\\{");
	
	// Used for finding imports for searching workspace
	private static final Pattern FIND_IMPORTS = Pattern.compile("(?i)import\\s+'(.+?)'");

	 //  Captures only the file name, ignoring relative and absolute paths.
	 //  We don't want to use File.separator because the editor might run on a different system from the Pig script.
	private static final Pattern GET_FILENAME_FROM_IMPORT = Pattern.compile("(?:.*[\\/\\\\])?(.+)");

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

	public static Set<String> findDefines(String text) {
		return findThings(text, FIND_DEFINES, 1);
	}

	public static Pattern findDefinesForHoverInfoPattern(String name) {
		return Pattern.compile("(" + MACRO_DEF_PREFIX + "(?-i)" + name + MACRO_DEF_PARAMS + ")\\{");
		
		// For capturing javadoc-style comments for hover text -> doesn't work nicely because hover text omits newlines
		//return Pattern.compile("((\\/\\*(?s).*?\\*\\/\\s*)?" + MACRO_DEF_PREFIX + "(?-i)" + name + MACRO_DEF_PARAMS + ")\\{");
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
