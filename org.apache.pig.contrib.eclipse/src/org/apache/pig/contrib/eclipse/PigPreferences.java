package org.apache.pig.contrib.eclipse;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PigPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public final static String COLOR_KEYWORDS = "color.keywords";
	public final static String COLOR_CONSTANTS = "color.constants";
	public final static String COLOR_COMMENTS = "color.comments";
	public final static String COLOR_BUILTINS = "color.builtins";
	public final static String COLOR_DATATYPES = "color.datatypes";
	public final static String COLOR_DEFAULT = "color.default";
	
	public final static String MATCH_BRACKETS = "match.brackets";
	public final static String MATCH_BRACKETS_COLOR = "match.brackets.color";
	
	public final static String PIG_VERSION = "pig.version";
	
	public static final String AUTO_COMPLETE_UPPER_CASE = "auto.complete.upper.case";
	
	public PigPreferences() {
		super(FieldEditorPreferencePage.GRID);
		setPreferenceStore(PigActivator.getDefault().getPreferenceStore());
	}
	
	@Override
	public void init(IWorkbench arg0) {
		setDescription("If you change a preference, Eclipse will have to be restarted in order to see the changes.\n");
	}

	@Override
	protected void createFieldEditors() {
		
		
		final ColorFieldEditor defaultEditor = new ColorFieldEditor(COLOR_DEFAULT, "Default", getFieldEditorParent());
		addField(defaultEditor);

		final ColorFieldEditor keywordsEditor = new ColorFieldEditor(COLOR_KEYWORDS, "Keywords", getFieldEditorParent());
		addField(keywordsEditor);
		
		final ColorFieldEditor constantsEditor = new ColorFieldEditor(COLOR_CONSTANTS, "Constants", getFieldEditorParent());
		addField(constantsEditor);

		final ColorFieldEditor commentsEditor = new ColorFieldEditor(COLOR_COMMENTS, "Comments", getFieldEditorParent());
		addField(commentsEditor);

		final ColorFieldEditor builtinsEditor = new ColorFieldEditor(COLOR_BUILTINS, "Built in functions", getFieldEditorParent());
		addField(builtinsEditor);

		final ColorFieldEditor dataTypesEditor = new ColorFieldEditor(COLOR_DATATYPES, "Data types", getFieldEditorParent());
		addField(dataTypesEditor);
		
		final ColorFieldEditor matchBracketsColorEditor = new ColorFieldEditor(MATCH_BRACKETS_COLOR, "Matching brackets color", getFieldEditorParent());
		addField(matchBracketsColorEditor);

		final BooleanFieldEditor matchBracketsEditor = new BooleanFieldEditor(MATCH_BRACKETS, "Match brackets", getFieldEditorParent());
		addField(matchBracketsEditor);

		final BooleanFieldEditor autoCompleteCaseEditor = new BooleanFieldEditor(AUTO_COMPLETE_UPPER_CASE, "Auto complete reserved words in capitals", getFieldEditorParent());
		addField(autoCompleteCaseEditor);

		String[][] versions = {{"0.11", "0.11"},{"0.12", "0.12"}};
		
		final RadioGroupFieldEditor pigVersionEditor = new RadioGroupFieldEditor("pig.version", "Pig version", 2, versions , getFieldEditorParent());
		addField(pigVersionEditor);
		
		adjustGridLayout();
	}
}

