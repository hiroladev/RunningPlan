package de.hirola.runningplan.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.preference.*;
import de.hirola.runningplan.R;
import de.hirola.sportslibrary.Global;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // set the preference ui elements
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        // load the preferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        // get all preferences from screen
        ArrayList<Preference> preferenceList = getPreferenceList(getPreferenceScreen(), new ArrayList<Preference>());
        for (Preference preference : preferenceList) {
            if (preference != null) {
                String key = preference.getKey();
                // boolean values
                if (preference instanceof SwitchPreferenceCompat) {
                    try {
                        // preference ui key == shared preference key
                        ((SwitchPreferenceCompat) preference)
                                .setChecked(sharedPreferences.getBoolean(preference.getKey(), false));
                    } catch (ClassCastException exception) {
                        if (Global.DEBUG) {
                            // TODO: Logging
                            exception.printStackTrace();
                        }
                        ((SwitchPreferenceCompat) preference).setChecked(false);
                    }
                } else if (preference instanceof ListPreference) {
                    if (key.equalsIgnoreCase("user_gender")) {
                        // gender: fill the list dynamically with values
                        // Integer = entryValues, String = entries
                        Map<Integer, String> genderValues = Global.TrainingParameter.genderValues;
                        Iterator<String> entriesIterator = genderValues.values().iterator();
                        int arrayCount = genderValues.size();
                        String[] entries = new String[arrayCount];
                        int index = 0;
                        while (entriesIterator.hasNext()) {
                            String entry = entriesIterator.next();
                            // the list contains the strings for resources
                            // load the strings
                            if (entry.length() > 0) {
                                try {
                                    int resourceStringId = requireContext().getResources().getIdentifier(entry,
                                            "string", requireContext().getPackageName());
                                    entry = requireContext().getString(resourceStringId);
                                } catch (Resources.NotFoundException exception) {
                                    entry = getString(R.string.preference_not_found);
                                    if (Global.DEBUG) {
                                        // TODO: Logging
                                    }
                                }
                            }
                            entries[index] = entry;
                            index++;
                        }
                        ((ListPreference) preference).setEntries(entries);
                        Iterator<Integer> entryValuesIterator = genderValues.keySet().iterator();
                        String[] entryValues = new String[arrayCount];
                        index = 0;
                        while (entryValuesIterator.hasNext()) {
                            entryValues[index] = String.valueOf(entryValuesIterator.next());
                            index++;
                        }
                        ((ListPreference) preference).setEntryValues(entryValues);
                    } else if (key.equalsIgnoreCase("user_birthday")) {
                        {
                            // birthday(year)
                            // fill the list dynamically with values
                            // year of birthday: actual year - max. 120
                            int actualYear = LocalDate.now().getYear();
                            int maxYearOfBirth = actualYear - 120;
                            int index = 0;
                            String[] entries = new String[120];
                            while (maxYearOfBirth < actualYear) {
                                entries[index] = String.valueOf(maxYearOfBirth);
                                maxYearOfBirth++;
                                index++;
                            }
                            ((ListPreference) preference).setEntries(entries);
                            ((ListPreference) preference).setEntryValues(entries);
                        }
                    }
                } else {
                    // String value

                }
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    private ArrayList<Preference> getPreferenceList(Preference p, ArrayList<Preference> list) {
        if( p instanceof PreferenceCategory || p instanceof PreferenceScreen) {
            PreferenceGroup pGroup = (PreferenceGroup) p;
            int pCount = pGroup.getPreferenceCount();
            for(int i = 0; i < pCount; i++) {
                getPreferenceList(pGroup.getPreference(i), list); // recursive call
            }
        } else {
            list.add(p);
        }
        return list;
    }
}