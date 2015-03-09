package org.apache.pig.contrib.eclipse.utils;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class SearchResult {

	protected final int start;
	protected final int length;
	protected final String text;
	
	protected SearchResult(int start, int length, String text) {
		this.start = start;
		this.length = length;
		this.text = text;
	}

	/**
	 * matching text
	 */
	public String getText() {
		return text;
	}

	/**
	 * offset from original text of beginning of match
	 */
	public int getOffset() {
		return start;
	}

	/**
	 * length of text to highlight, if applicable
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Go to the location where this result was found
	 * 
	 * @return true if the UI was changed to where this result was found
	 */
	public abstract boolean go();

	/**
	 * Default implementation of going to the result's offset using an IFile and the instance's offset/length properties
	 */
	protected boolean go(IFile file){
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		ITextEditor editor;
		try {
			if (file != null) {
				FileEditorInput input = new FileEditorInput(file);
				
				IEditorDescriptor editorDesc = IDE.getEditorDescriptor(file);
				
				IEditorPart foundEditor = page.findEditor(input);
				
				if (foundEditor != null) {
					editor = (ITextEditor)foundEditor; // if the editor is already open
					page.activate(editor);
				} else {
					editor = (ITextEditor)page.openEditor(input, editorDesc.getId()); // otherwise open a new editor
				}

			} else {
				editor = (ITextEditor) page.getActiveEditor(); // this means the declaration was found locally
			}
			
			if (editor == null) {
				PigLogger.info("Could not open editor for file " + file.getName() + " with definition " + text + " and offset " + getOffset());
			} else {
				editor.selectAndReveal(getOffset(), getLength());
				return true;
			}
		} catch (PartInitException e) {
			PigLogger.warn("Exception while changing selection to definition of " + text, e);
		}
		
		return false;
	}
}
