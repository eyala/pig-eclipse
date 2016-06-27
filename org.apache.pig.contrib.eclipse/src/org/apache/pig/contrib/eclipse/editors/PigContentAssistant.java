package org.apache.pig.contrib.eclipse.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pig.contrib.eclipse.PigActivator;
import org.apache.pig.contrib.eclipse.PigLogger;
import org.apache.pig.contrib.eclipse.PigPreferences;
import org.apache.pig.contrib.eclipse.utils.RegexUtils;
import org.apache.pig.contrib.eclipse.utils.WorkspaceSearcher;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * Provides completion proposals based on Pig's keywords, builtin functions, and macros that are in context
 * 
 * Keywords are returned in upper or lower case according to preferences
 * 
 * Builtins are searched for ignoring case, but returned in the correct case
 * 
 * A static cache in this class saves workspace search results to save time
 */
public class PigContentAssistant implements IContentAssistProcessor{

	private static String[] RESULTS_ARRAY;	// case varies based on preferences
	private static String[] SEARCH_ARRAY;	// always lowercase

	private static Collection<String> KEYWORDS = Collections.emptyList();
	private static Collection<String> BUILTINS = Collections.emptyList();
	
	private static CompletionProposalComparator COMPLETION_COMPARATOR = new CompletionProposalComparator();
	
	// these static members are for the cache
	private static Set<String> IMPORTS_USED_IN_CACHE = null;
	private static Set<String> CACHED_EXTERNAL_DEFINES = null;
	private static long LAST_CALL_TIME = 0;

	// How long cache results are valid. It's not likely someone will change a macro in a different file and call auto-complete within a minute
	private static final long AUTO_COMPLETE_CACHE_TIME = 60000;
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		
		List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

        String prefix = getPrefix(viewer.getDocument(), offset).toLowerCase();

        int replacementOffset = offset - prefix.length();
        int replacementLength = prefix.length();
        
        // Add constant suggestions
        for (int i = 0; i < SEARCH_ARRAY.length; i++) {
        	if (SEARCH_ARRAY[i].startsWith(prefix)) {
        		result.add(new CompletionProposal(RESULTS_ARRAY[i], replacementOffset, replacementLength, RESULTS_ARRAY[i].length()));
        	}
        }

        // Scan all of the current document (up to this point) for import statements, to prune the list of pig files to read
		String mostOfDoc = "";
		
		try {
			mostOfDoc = viewer.getDocument().get(0, offset);
		} catch (BadLocationException ble) {
			PigLogger.warn("Not adding dynamic content assist because of BadLocationException with offset " + offset, ble);
		}
		
		Set<String> dynamic_completions = new HashSet<String>();
		
		Set<String> imports = WorkspaceSearcher.findRecursiveImports(mostOfDoc);
		
		long now = System.currentTimeMillis();
		
		// When to use the cache instead of searching the workspace - if the same files being searched, and within a given (small) amount of time
		// this brings the execution time of successive auto completes down from around 50-100 ms to less than 5 ms
		if (now - LAST_CALL_TIME > AUTO_COMPLETE_CACHE_TIME || ! imports.equals(IMPORTS_USED_IN_CACHE)) {

			// Scan the entire workspace for defines (macros, etc) in the imported files
			CACHED_EXTERNAL_DEFINES = new WorkspaceSearcher().findAll(imports, RegexUtils.FIND_DEFINES);
			IMPORTS_USED_IN_CACHE = imports;
			LAST_CALL_TIME = now;
		}
		
		dynamic_completions.addAll(CACHED_EXTERNAL_DEFINES);
		
		// Add defines (macros, udfs, etc) from the current file
		dynamic_completions.addAll(RegexUtils.findDefines(mostOfDoc));

		// Add relations defined in the current file
		dynamic_completions.addAll(RegexUtils.findRelations(mostOfDoc));

		// Add relations defined in SPLIT commands in the current file
		dynamic_completions.addAll(RegexUtils.findRelationsFromSplit(mostOfDoc));
		
        for (String i : dynamic_completions ) {
        	if (i != null && i.toLowerCase().startsWith(prefix)) {
        		result.add(new CompletionProposal(i, replacementOffset, replacementLength, i.length()));
        	}
        }

        ICompletionProposal[] resultArray = result.toArray(new ICompletionProposal[result.size()]);
        
       	Arrays.sort(resultArray, COMPLETION_COMPARATOR);
		
        return resultArray;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	private String getPrefix (IDocument doc, int offset) {
		try {
			int n = 0;
			for (n = offset - 1; n >= 0; n--) {
				char c = doc.getChar(n);
				if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_') {
					return doc.get(n + 1, offset - n - 1);
				}
			}
			if (n == -1) {
				return doc.get(n + 1, offset - n - 1);
			}
		} catch (BadLocationException ble) {
			PigLogger.info("Ignoring BadLocationException in getPrefix with offset " + offset);
		}
		
		return "";
	}

	public static void setResources(Collection<String> keywords, Collection<String> builtins) {
		KEYWORDS = keywords;
		BUILTINS = builtins;
		
		prepareCompletionsArray();
	}

	/**
	 * Joins the keywords and builtin functions into two sorted list with case according to user preferences
	 */
	private static void prepareCompletionsArray() {
		List<String> searchList = new ArrayList<String>();
		List<String> resultsList = new ArrayList<String>();
		
		for (String i : BUILTINS) {
			searchList.add(i.toLowerCase());
			resultsList.add(i);
		}

		IPreferenceStore store = PigActivator.getDefault().getPreferenceStore();
		boolean autoSuggestUpperCase = store.getBoolean(PigPreferences.AUTO_COMPLETE_UPPER_CASE);
		
		for (String i : KEYWORDS) {
			searchList.add(i.toLowerCase());
			
			if (autoSuggestUpperCase) {
				resultsList.add(i.toUpperCase());
			} else {
				resultsList.add(i);
			}
		}
		
		Collections.sort(searchList, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(resultsList, String.CASE_INSENSITIVE_ORDER);
		
		SEARCH_ARRAY = new String[searchList.size()];
		RESULTS_ARRAY = new String[resultsList.size()];
		
		searchList.toArray(SEARCH_ARRAY);
		resultsList.toArray(RESULTS_ARRAY);
	}
	
	/**
	 * For sorting completions based on what the user sees
	 */
	private static class CompletionProposalComparator implements Comparator<ICompletionProposal> {

		@Override
		public int compare(ICompletionProposal o1, ICompletionProposal o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getDisplayString(), o2.getDisplayString());
		}
		
	}
}
