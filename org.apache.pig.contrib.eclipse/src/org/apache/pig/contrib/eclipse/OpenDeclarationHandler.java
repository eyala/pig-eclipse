package org.apache.pig.contrib.eclipse;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pig.contrib.eclipse.editors.PigEditor;
import org.apache.pig.contrib.eclipse.utils.PigWordDetector;
import org.apache.pig.contrib.eclipse.utils.RegexUtils;
import org.apache.pig.contrib.eclipse.utils.SearchResult;
import org.apache.pig.contrib.eclipse.utils.SourceSearchResult;
import org.apache.pig.contrib.eclipse.utils.WorkspaceSearcher;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class OpenDeclarationHandler extends AbstractHandler {

	// Used for searching for builtin functions
	private static Set<String> BUILTINS;
	
	// Used to provide tooltips for keywords from the Pig documentation
	private static Map<String, String> KEYWORD_TOOLTIPS;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		
		// be defensive, possibly unnecessary
		if (activeEditor instanceof PigEditor) {
			PigEditor pigEditor = (PigEditor)activeEditor;
			
			IDocumentProvider documentProvider = pigEditor.getDocumentProvider();
	
			if (documentProvider != null) {
				int offset = pigEditor.getOffset();
		
				IDocument document = documentProvider.getDocument(pigEditor.getEditorInput());
				
				if (document != null) {
					SearchResult result = findDeclaration(document, offset);
					
					if (result != null) {
						result.go();
					} else {
						PigLogger.debug("Couldn't find definition");
					}
				} else {
					PigLogger.debug("Pig OpenDeclaration triggered, but no document available");
				}
			} else {
				PigLogger.debug("No documentProvider in event");
			}
		} else {
			PigLogger.debug("Pig OpenDeclaration triggered, but not in PigEditor");
		}
		
		return null;
	}

	/**
	 * Given a document and an offset, tries to find the declaration for the word we are on
	 */
	public static SearchResult findDeclaration(IDocument doc, int offset) {

		// Get the word we're on
		String word = PigWordDetector.INSTANCE.getWord(doc, offset);
		
		if (word != null && ! word.trim().isEmpty()) {

			PigLogger.debug("Searching for definition for '" + word + "'");

			// If there are periods in it, consider it a UDF
			if (word.indexOf(".") != -1) {
				return new WorkspaceSearcher().findUdf(word, null);
			}

			// If it is a keyword, use the keywords tooltip map
			String lowerCaseWord = word.toLowerCase();
			if (KEYWORD_TOOLTIPS.containsKey(lowerCaseWord)) {
				return new SourceSearchResult(offset,0,null,KEYWORD_TOOLTIPS.get(lowerCaseWord));
			}

			// If it is a builtin function, add its package name and search for it
			// TODO: fallback to using builtins_map
			if (BUILTINS.contains(word)) {
				return new WorkspaceSearcher().findUdf("org.apache.pig.builtin." + word, null);
			}

			// Prepare a regular expression for finding macro definitions for the current word 
			Pattern macro_defines = RegexUtils.findMacroDefinesForHoverInfoPattern(word);

			// Get all of the current document (up to this point)
			String mostOfDoc = "";

			try {
				mostOfDoc = doc.get(0, offset);
			} catch (BadLocationException ble) {
				PigLogger.warn("BadLocationException while getting document from start to " + offset, ble); // this shouldn't happen, but does
			}

			Matcher m = macro_defines.matcher(mostOfDoc);
			
			if (m.find()) {
				PigLogger.debug("Found local macro definition for '" + word + "'");
				
				return new SourceSearchResult(m.start(), null, m.group(1) );
			}

			// Prepare a regular expression for finding non macro definitions and use it 
			Pattern local_defines = RegexUtils.findNonMacroDefinesForHoverInfoPattern(word);

			Matcher m2 = local_defines.matcher(mostOfDoc);
			
			if (m2.find()) {
				PigLogger.debug("Found local non-macro definition for '" + word + "'");

				String defineTarget = m2.group();
				
				String udfName = m2.group(1);
				
				int end = udfName.indexOf("(");
				
				if (end > 0) {
					udfName = udfName.substring(0,end); 
				}
				
				SearchResult result = new WorkspaceSearcher().findUdf(udfName, defineTarget);
					
				if (result != null) {
					return result; // this means we found something outside the current file
				}
				
				return new SourceSearchResult(m2.start(), null, defineTarget ); // fallback to using the local define
			}

			// Scan all of the current document (up to this point) for import statements, to prune the list of pig files to read
			Set<String> imports = WorkspaceSearcher.findRecursiveImports(mostOfDoc);

			// Try to find a matching macro definition elsewhere in the workspace
			return new WorkspaceSearcher().find(imports, macro_defines);
		} else {
			PigLogger.debug("Pig OpenDeclaration triggered, but not word found at offset " + offset);
		}
		
		return null;
	}

	public static void setBuiltins(Set<String> builtins) {
		BUILTINS = builtins;
	}
	
	public static void setKeywordTooltips(Map<String,String> keywords) {
		KEYWORD_TOOLTIPS = keywords;
	}
}
