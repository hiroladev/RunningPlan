package de.hirola.runningplan.ui.settings;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import de.hirola.runningplan.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}