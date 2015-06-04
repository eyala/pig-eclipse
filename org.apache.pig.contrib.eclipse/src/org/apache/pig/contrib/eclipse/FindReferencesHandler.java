package org.apache.pig.contrib.eclipse;

import org.apache.pig.contrib.eclipse.editors.PigEditor;
import org.apache.pig.contrib.eclipse.utils.PigWordDetector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.eclipse.search.ui.text.TextSearchQueryProvider.TextSearchInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class FindReferencesHandler extends AbstractHandler  {

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
							ISearchQuery query = TextSearchQueryProvider.getPreferred().createQuery(new PigSearchInput(word)); 
							
							NewSearchUI.runQueryInBackground(query);
						} catch (IllegalArgumentException iae) {
							// TODO Auto-generated catch block
							iae.printStackTrace();
						} catch (CoreException ce) {
							PigLogger.warn("Couldn't create query for " + word, ce);
							ce.printStackTrace();
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
	
	private static class PigSearchInput extends TextSearchInput {

		private final String fSearchText;

		// pros - gets rid of some false positives
		// cons - the label of the search results becomes an ugly regex
		public PigSearchInput(String searchText) {
			fSearchText= "[^\\w](" + searchText + ")\\s*\\(";
		}

		public String getSearchText() {
			return fSearchText;
		}

		public boolean isCaseSensitiveSearch() {
			return true;
		}

		public boolean isRegExSearch() {
			return true;
		}

		public boolean isWholeWordSearch() {
			return false;
		}

		public FileTextSearchScope getScope() {
			return FileTextSearchScope.newWorkspaceScope(PigEditor.getPigFileAssociations(), false);
		}
	}
}