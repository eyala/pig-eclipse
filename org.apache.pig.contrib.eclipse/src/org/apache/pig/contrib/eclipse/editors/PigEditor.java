package org.apache.pig.contrib.eclipse.editors;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class PigEditor extends TextEditor {

	private final static String MATCH_BRACKETS = "match.brackets";
	private final static String MATCH_BRACKETS_COLOR = "match.brackets.color";

	public PigEditor() {
		super();
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new PigViewerConfiguration());
	}
	
	/**
	 * This adds bracket matching
	 */
	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);      

		char[] matchChars = {'(', ')', '[', ']', '{', '}'}; 
	    ICharacterPairMatcher matcher = new DefaultCharacterPairMatcher(matchChars);
	    support.setCharacterPairMatcher(matcher);

		IPreferenceStore store = getPreferenceStore();

        store.setDefault(MATCH_BRACKETS, true);
        store.setDefault(MATCH_BRACKETS_COLOR, "127,0,0");

	    support.setMatchingCharacterPainterPreferenceKeys(MATCH_BRACKETS, MATCH_BRACKETS_COLOR);
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
