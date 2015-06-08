package org.apache.pig.contrib.eclipse;

import java.util.Set;

import org.apache.pig.contrib.eclipse.editors.PigEditor;
import org.apache.pig.contrib.eclipse.utils.PigWordDetector;
import org.apache.pig.contrib.eclipse.utils.RegexUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class FindReferencesHandler extends AbstractHandler  {

	private static final String[] PIG_PATTERNS = { "*.pig" };

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
					// Get the word we're on
					String word = PigWordDetector.INSTANCE.getWord(document, offset);
					
					if (word != null && ! word.trim().isEmpty()) {
						try {
							FileTextSearchScope scope= FileTextSearchScope.newWorkspaceScope(PIG_PATTERNS, false);
							
							// discouraged access, but works
							// use TextSearchQueryProvider.getPreferred().createQuery instead
							ISearchQuery query = new FileSearchQuery(word, false, true, scope);
							
							NewSearchUI.runQueryInBackground(query);
						} catch (IllegalArgumentException iae) {
							// TODO Auto-generated catch block
							iae.printStackTrace();
						}
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

}