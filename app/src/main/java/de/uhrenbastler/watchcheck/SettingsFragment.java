package de.uhrenbastler.watchcheck;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by clorenz on 17.03.15.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
