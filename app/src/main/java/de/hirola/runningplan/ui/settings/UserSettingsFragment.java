package de.hirola.runningplan.ui.settings;

import de.hirola.runningplan.R;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

public class UserSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.user_settings_preferences, rootKey);
    }
}