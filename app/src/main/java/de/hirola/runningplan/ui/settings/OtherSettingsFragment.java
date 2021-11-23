package de.hirola.runningplan.ui.settings;

import de.hirola.runningplan.R;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

public class OtherSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.other_settings_preferences, rootKey);
    }
}