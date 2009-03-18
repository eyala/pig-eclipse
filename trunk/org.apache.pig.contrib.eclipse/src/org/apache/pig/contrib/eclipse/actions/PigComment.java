package org.apache.pig.contrib.eclipse.actions;

import java.util.List;

import org.apache.pig.contrib.eclipse.StringUtil;
import org.apache.pig.contrib.eclipse.editors.PigEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.EditorPluginAction;

public class PigComment implements IEditorActionDelegate {

	private PigEditor editor;

	private IDocument doc;

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		this.editor = (PigEditor) targetEditor;
		this.doc = this.editor.getDocumentProvider().getDocument(
				editor.getEditorInput());
	}

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub

		if (action instanceof EditorPluginAction) {

			try {
				EditorPluginAction editorAction = (EditorPluginAction) action;
				TextSelection selection = (TextSelection) editorAction
						.getSelection();
				int startLine = selection.getStartLine();
				int endLine = selection.getEndLine();

				IRegion startRegion = doc.getLineInformation(startLine);
				IRegion endRegion = doc.getLineInformation(endLine);
				int startPos=startRegion.getOffset();
				int endPos=endRegion.getOffset()+endRegion.getLength();
				
				String selectionText=doc.get(startPos, endPos-startPos);
				List<String> lines=StringUtil.splitInLines(selectionText);
				StringBuilder commentTextBuilder=new StringBuilder();
				for (int i=0;i<lines.size();++i){
					String line=lines.get(i);
					if (line.startsWith("--")){
						commentTextBuilder.append(line.substring(2));
					}else{
						commentTextBuilder.append("--"+line);
					}
				}
				
				String commentText=commentTextBuilder.toString();
				
				this.doc.replace(startPos, endPos-startPos, commentText);
				selection.isEmpty();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// action.get
		// ITextEditor editor=
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
