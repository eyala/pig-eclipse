package org.apache.pig.contrib.eclipse.editors;

import java.util.HashSet;
import java.util.Set;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class PigEditor extends TextEditor {

	public final static String EDITOR_ID = "org.apache.pig.contrib.eclipse.pigEditor";
	
	private final static String MATCH_BRACKETS = "match.brackets";
	private final static String MATCH_BRACKETS_COLOR = "match.brackets.color";

	private static String[] PIG_FILE_ASSOCIATIONS = null;
	
	private final static String[] DEFAULT_PIG_FILE_ASSOCIATIONS = { "*.pig" };

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
	
	/**
	 * Lazily calculates, based on user preferences, which file extensions are associated with this editor.
	 * 
	 * If none are found, returns *.pig as default
	 * 
	 * Note that a change in preferences will not be discovered until Eclipse is reopened
	 */
	public static String[] getPigFileAssociations() {
		if (PIG_FILE_ASSOCIATIONS == null) {
			PigLogger.info("About to calculate Pig file associations");
			
			IFileEditorMapping[] mappings = PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();
			
			if (mappings == null) {
				PIG_FILE_ASSOCIATIONS = DEFAULT_PIG_FILE_ASSOCIATIONS;
				
				PigLogger.info("Couldn't find PigEditor file association preferences - using default (*.pig)");
				
			} else { 
				Set<String> editors = new HashSet<String>();
				
				for (IFileEditorMapping mapping : mappings) {
					
					IEditorDescriptor editor = mapping.getDefaultEditor();
					
					if (editor != null && PigEditor.EDITOR_ID.equals(editor.getId())) {
						editors.add("*." + mapping.getExtension());
						
						PigLogger.info("Discovered PigEditor file association: *." + mapping.getExtension());
					}
				}
				
				PIG_FILE_ASSOCIATIONS = editors.toArray(new String[0]);
			}
		}
		
		return PIG_FILE_ASSOCIATIONS;
	}
}
