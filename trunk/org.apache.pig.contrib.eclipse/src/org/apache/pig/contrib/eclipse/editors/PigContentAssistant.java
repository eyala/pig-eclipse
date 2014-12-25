package org.apache.pig.contrib.eclipse.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.pig.contrib.eclipse.PigActivator;
import org.apache.pig.contrib.eclipse.PigPreferences;
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
 * Does static auto completion proposals based on Pig's keywords and builtin functions
 * 
 * Keywords are returned in upper or lower case according to preferences
 * 
 * Builtins are searched for ignoring case, but returned in the correct case
 */
public class PigContentAssistant implements IContentAssistProcessor{

	private static String[] RESULTS_ARRAY;	// case varies based on preferences
	private static String[] SEARCH_ARRAY;	// always lowercase

	private static Collection<String> KEYWORDS = Collections.emptyList();
	private static Collection<String> BUILTINS = Collections.emptyList();
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		
		List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

        String prefix = getPrefix(viewer.getDocument(), offset).toLowerCase();

        int replacementOffset = offset - prefix.length();
        int replacementLength = prefix.length();
        
        for (int i = 0; i < SEARCH_ARRAY.length; i++) {
        	if (SEARCH_ARRAY[i].startsWith(prefix)) {
        		result.add(new CompletionProposal(RESULTS_ARRAY[i], replacementOffset, replacementLength, RESULTS_ARRAY[i].length()));
        	}
        }
        
        return result.toArray(new ICompletionProposal[result.size()]);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
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
				if (!Character.isLetter(c)) {
					return doc.get(n + 1, offset - n - 1);
				}
			}
			if (n == -1) {
				return doc.get(n + 1, offset - n - 1);
			}
		} catch (BadLocationException e) {}
		
		return "";
	}

	public static void setKeywords(Collection<String> k) {
		KEYWORDS = k;
		prepareCompletionsArray();
	}
	
	public static void setBuiltins(Collection<String> b) {
		BUILTINS = b;
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
}
