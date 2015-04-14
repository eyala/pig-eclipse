package org.apache.pig.contrib.eclipse;

import java.util.List;

import org.apache.pig.contrib.eclipse.editors.PigEditor;
import org.apache.pig.contrib.eclipse.utils.StringUtil;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Handler instead of action, as in original org.apache.pig.contrib.eclipse.actions.PigComment
 * 
 * (see commit ed9f2c37cc4dba767aeff6c535a1560632cae315 )
 */
public class ToggleCommentHandler extends AbstractHandler  {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);

		// be defensive, possibly unnecessary
		if (activeEditor instanceof PigEditor) {
			PigEditor pigEditor = (PigEditor)activeEditor;
			
			IDocumentProvider documentProvider = pigEditor.getDocumentProvider();
	
			if (documentProvider != null) {
				IDocument doc = documentProvider.getDocument(pigEditor.getEditorInput());
				
				if (doc != null) {
					ISelection iSelection = HandlerUtil.getCurrentSelection(event);
					
					if (iSelection instanceof TextSelection) {
						TextSelection selection = (TextSelection)iSelection;
						
						int startLine = selection.getStartLine();
						int endLine = selection.getEndLine();
	
						try {
							IRegion startRegion = doc.getLineInformation(startLine);
							IRegion endRegion = doc.getLineInformation(endLine);
							int startPos=startRegion.getOffset();
							int endPos=endRegion.getOffset()+endRegion.getLength();
							
							String selectionText=doc.get(startPos, endPos-startPos);
						
							List<String> lines=StringUtil.splitInLines(selectionText);
							StringBuilder commentTextBuilder=new StringBuilder();
							
							Boolean hasComment = null;
							
							for (String line : lines){
								
								if (hasComment == null) {
									hasComment = line.startsWith("--");
								}
								
								if (hasComment) {
									if (line.startsWith("--")) {
										commentTextBuilder.append(line.substring(2));
									}
								} else {
									commentTextBuilder.append("--"+line);
								}
							}
							
							String commentText=commentTextBuilder.toString();
							
							doc.replace(startPos, endPos-startPos, commentText);
							selection.isEmpty();
						} catch (BadLocationException ble) {
							PigLogger.info("BadLocationException while toggling comment");
						}
					}
				} else {
					PigLogger.debug("Pig ToggleComment triggered, but no document available");
				}
			} else {
				PigLogger.debug("No documentProvider in event");
			}
		} else {
			PigLogger.debug("Pig ToggleComment triggered, but not in PigEditor");
		}
		
		return null;
	}

}