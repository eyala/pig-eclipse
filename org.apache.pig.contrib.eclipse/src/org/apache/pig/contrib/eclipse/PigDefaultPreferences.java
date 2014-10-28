package org.apache.pig.contrib.eclipse;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

public class PigDefaultPreferences extends AbstractPreferenceInitializer {
    public void initializeDefaultPreferences() {
        IPreferenceStore store = PigActivator.getDefault().getPreferenceStore();
        
        PreferenceConverter.setDefault(store, PigPreferences.COLOR_KEYWORDS, new RGB(127, 0, 85));
        PreferenceConverter.setDefault(store, PigPreferences.COLOR_CONSTANTS, new RGB(135, 206, 235));
        PreferenceConverter.setDefault(store, PigPreferences.COLOR_COMMENTS, new RGB(0, 0, 192));
        PreferenceConverter.setDefault(store, PigPreferences.COLOR_BUILTINS, new RGB(237, 145, 33));
        PreferenceConverter.setDefault(store, PigPreferences.COLOR_DATATYPES, new RGB(217, 45, 33));
        PreferenceConverter.setDefault(store, PigPreferences.COLOR_DEFAULT, new RGB(0, 0, 0));
    }
}
