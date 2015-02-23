package org.apache.pig.contrib.eclipse;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pig.contrib.eclipse.editors.PigEditor;
import org.apache.pig.contrib.eclipse.utils.PigWordDetector;
import org.apache.pig.contrib.eclipse.utils.RegexUtils;
import org.apache.pig.contrib.eclipse.utils.SearchResult;
import org.apache.pig.contrib.eclipse.utils.WorkspaceSearcher;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenDeclarationHandler extends AbstractHandler {

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
						changeSelection(result.file, result.text, result.start);
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

		// 1. Get the word we're on
		String word = PigWordDetector.INSTANCE.getWord(doc, offset);
		
		if (word != null && ! word.trim().isEmpty()) {

			PigLogger.debug("Searching for definition for '" + word + "'");

			// 2. Prepare a regular expression for finding macro definitions for the current word 
			Pattern macro_defines = RegexUtils.findMacroDefinesForHoverInfoPattern(word);

			// 3. Get all of the current document (up to this point)
			String mostOfDoc = "";

			try {
				mostOfDoc = doc.get(0, offset);
			} catch (BadLocationException ble) {
				PigLogger.warn("BadLocationException while getting document from start to " + offset, ble); // this shouldn't happen
			}

			Matcher m = macro_defines.matcher(mostOfDoc);
			
			if (m.find()) {
				return new SearchResult(m.start(), null, m.group(1) );
			}

			// 4. Prepare a regular expression for finding non macro definitions and use it 
			Pattern local_defines = RegexUtils.findNonMacroDefinesForHoverInfoPattern(word);

			Matcher m2 = local_defines.matcher(mostOfDoc);
			
			if (m2.find()) {
				return new SearchResult(m2.start(), null, m2.group() );
			}

			// 5. Scan all of the current document (up to this point) for import statements, to prune the list of pig files to read
			Set<String> imports = RegexUtils.findImports(mostOfDoc);

			// 6. Try to find a matching macro definition elsewhere in the workspace
			return new WorkspaceSearcher().find(imports, macro_defines, false);
		} else {
			PigLogger.debug("Pig OpenDeclaration triggered, but not word found at offset " + offset);
		}
		
		return null;
	}
	
	private void changeSelection(IFile file, String text, int offset) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		ITextEditor editor;
		try {
			if (file != null) {
				editor = (ITextEditor) IDE.openEditor(page, file); // otherwise open a new one
			} else {
				editor = (ITextEditor) page.getActiveEditor(); // this means the declaration was found locally
			}
			
			if (editor == null) {
				PigLogger.info("Could not open editor for file " + file.getName() + " with definition " + text + " and offset " + offset);
			} else {
				editor.selectAndReveal(offset, text.length());	
			}
		} catch (PartInitException e) {
			PigLogger.warn("Exception while changing selection to definition of " + text, e);
		}
	}
}
