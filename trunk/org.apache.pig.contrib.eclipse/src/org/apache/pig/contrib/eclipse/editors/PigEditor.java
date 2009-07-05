package org.apache.pig.contrib.eclipse.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class PigEditor extends TextEditor {

	public PigEditor() {
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new PigViewerConfiguration());
	}
}
