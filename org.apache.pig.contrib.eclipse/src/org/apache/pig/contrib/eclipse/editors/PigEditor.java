package org.apache.pig.contrib.eclipse.editors;

import org.apache.pig.contrib.eclipse.PigActivator;
import org.apache.pig.contrib.eclipse.PigLogger;
import org.apache.pig.contrib.eclipse.PigPreferences;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class PigEditor extends TextEditor {

	public PigEditor() {
		setPreferenceStore(PigActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new PigViewerConfiguration());
	}

	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);      

		char[] matchChars = {'(', ')', '[', ']', '{', '}'}; 
	    ICharacterPairMatcher matcher = new DefaultCharacterPairMatcher(matchChars);
	    support.setCharacterPairMatcher(matcher);
	    
	    support.setMatchingCharacterPainterPreferenceKeys(PigPreferences.MATCH_BRACKETS, PigPreferences.MATCH_BRACKETS_COLOR);
	}
	
	/**
	 * Returns the current offset, or zero if it cannot be calculated
	 */
	public int getOffset() {
		ISourceViewer sourceViewer = getSourceViewer();
		
		if( sourceViewer != null) {
			Point selectedRange = sourceViewer.getSelectedRange();
			
			if (selectedRange != null) {
				return selectedRange.x;
			}
		} else {
			PigLogger.debug("SourceViewer is null");
		}
		
		return 0;
	}
}
