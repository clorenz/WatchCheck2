package de.uhrenbastler.watchcheck;

import android.app.Activity;
import android.os.Bundle;

import de.uhrenbastler.watchcheck.SettingsFragment;

/**
 * Created by clorenz on 17.03.15.
 */
public class SettingsActivity extends WatchCheckActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

}
