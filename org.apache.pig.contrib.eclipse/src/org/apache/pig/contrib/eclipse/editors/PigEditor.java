package org.apache.pig.contrib.eclipse.editors;
import org.eclipse.ui.editors.text.TextEditor;



public class PigEditor extends TextEditor {

	public PigEditor() {
		// TODO Auto-generated constructor stub
	}


	@Override
	protected void initializeEditor() {
		// TODO Auto-generated method stub
		super.initializeEditor();
		setSourceViewerConfiguration(new PigViewerConfiguration());
	}
}
