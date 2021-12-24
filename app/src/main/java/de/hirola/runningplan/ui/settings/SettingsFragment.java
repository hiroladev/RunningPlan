package de.hirola.runningplan.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.*;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.User;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener, EditTextPreference.OnBindEditTextListener {

    private RunningPlanViewModel viewModel;
    private User appUser;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // set the preference ui elements
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        // get the view model for data handling
        viewModel = new ViewModelProvider(this).get(RunningPlanViewModel.class);
        // load the app user
        appUser = viewModel.getAppUser().getValue();
        // load the preferences
        sharedPreferences = requireContext()
                .getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        // get all preferences from screen
        ArrayList<Preference> preferenceList = getPreferenceList(getPreferenceScreen(), new ArrayList<>());
        // set saved values in ui
        setPreferenceValues(preferenceList);
    }

    @Override
    public boolean onPreferenceChange(@NotNull Preference preference, Object newValue) {
        // save values to local datastore
        String key = preference.getKey();
        // save shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // boolean preferences
        if (preference instanceof SwitchPreferenceCompat) {
            boolean value = ((SwitchPreferenceCompat) preference).isChecked();
            // save trainings
            if (key.equalsIgnoreCase(Global.PreferencesKeys.saveTrainings)) {
                editor.putBoolean(key, (Boolean) newValue);
            }
            // use location data
            if (key.equalsIgnoreCase(Global.PreferencesKeys.useLocationData)) {
                // TODO: request for..
                editor.putBoolean(key, (Boolean) newValue);
            }
            // use fine location data
            if (key.equalsIgnoreCase(Global.PreferencesKeys.useFineLocationData))
            {
                // TODO: request for..
                editor.putBoolean(key, (Boolean) newValue);
            }
            // use notifications
            if (key.equalsIgnoreCase(Global.PreferencesKeys.useNotifications)) {
                // TODO: request for..
                editor.putBoolean(key, (Boolean) newValue);
            }
            // use sync
            if (key.equalsIgnoreCase(Global.PreferencesKeys.useSync))
            {
                // TODO: kinto dialog
                editor.putBoolean(key, (Boolean) newValue);
            }
        }
        // text preferences
        if (preference instanceof EditTextPreference) {
            // email address
            if (key.equalsIgnoreCase(Global.PreferencesKeys.userEmailAddress)) {
                // TODO: check if valid email format
                String emailAddress = (String) newValue;
                editor.putString(key, emailAddress);
                appUser.setEmailAddress(emailAddress);
            }
            // max pulse
            if (key.equalsIgnoreCase(Global.PreferencesKeys.userMaxPulse)) {
                String maxPulse = (String) newValue;
                try {
                    Integer.parseInt(maxPulse);
                } catch (NumberFormatException exception) {
                    if (Global.DEBUG) {
                        exception.printStackTrace();
                    }
                    return false;
                }
                editor.putString(key, maxPulse);
                appUser.setMaxPulse(Integer.parseInt(maxPulse));
            }
        }
        // list preferences
        if (preference instanceof ListPreference) {
            if (newValue != null) {
                int index = ((ListPreference) preference).findIndexOfValue((String) newValue);
                String value = "0";
                if (index > -1) {
                    CharSequence[] entryValues = ((ListPreference) preference).getEntryValues();
                    if (entryValues.length > index) {
                        value = (String) entryValues[index];
                    }
                }
                // gender
                if (key.equalsIgnoreCase(Global.PreferencesKeys.userGender)) {
                    editor.putString(key, value);
                    // try to calculate the max pulse
                    calculateMaxPulse();
                    try {
                        appUser.setGender(Integer.parseInt(value));
                    } catch (NumberFormatException exception) {
                        // preference can not set
                        // TODO: Hinweis an Nutzer?
                        if (Global.DEBUG) {
                            exception.printStackTrace();
                        }
                        return false;
                    }
                }
                // training level
                if (key.equalsIgnoreCase(Global.PreferencesKeys.userTrainingLevel)) {
                    editor.putString(key, value);
                    try {
                        appUser.setTrainingLevel(Integer.parseInt(value));
                    } catch (NumberFormatException exception) {
                        // preference can not set
                        // TODO: Hinweis an Nutzer?
                        if (Global.DEBUG) {
                            exception.printStackTrace();
                        }
                        return false;
                    }
                }
                    // birthday
                if (key.equalsIgnoreCase(Global.PreferencesKeys.userBirthday)) {
                    // build a birthday from selected year
                    editor.putString(key, value);
                    // try to calculate the max pulse
                    calculateMaxPulse();
                    try {
                        int year = Integer.parseInt(value);
                        if (year == 0) {
                            year = LocalDate.now().getYear();
                        }
                        LocalDate birthday = LocalDate.now().withYear(year);
                        appUser.setBirthday(birthday);
                    } catch (NumberFormatException exception) {
                        // preference can not set
                        // TODO: Hinweis an Nutzer?
                        if (Global.DEBUG) {
                            System.out.println(exception.getMessage());
                        }
                        return false;
                    }
                }
            }
        }
        try {
            // save the data
            viewModel.update(appUser);
        } catch (SportsLibraryException exception) {
            // TODO: Hinweis an Nutzer
            if (Global.DEBUG) {
                exception.printStackTrace();
            }
            return false;
        }
        // save the shared preferences
        editor.apply();
        return true;
    }

    @Override
    public void onBindEditText(@NonNull @NotNull EditText editText) {
        // only numbers in some preferences
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    private void setPreferenceValues(@NotNull ArrayList<Preference> preferenceList) {
        for (Preference preference : preferenceList) {
            if (preference != null) {
                // add the listener
                preference.setOnPreferenceChangeListener(this);
                // get the preference key
                String key = preference.getKey();
                // boolean values
                if (preference instanceof SwitchPreferenceCompat) {
                    try {
                        // preference ui key == shared preference key
                        ((SwitchPreferenceCompat) preference)
                                .setChecked(sharedPreferences.getBoolean(key, false));
                    } catch (ClassCastException exception) {
                        ((SwitchPreferenceCompat) preference).setChecked(false);
                        if (Global.DEBUG) {
                            // TODO: Logging
                            exception.printStackTrace();
                        }
                    }
                } else if (preference instanceof ListPreference) {
                    if (key.equalsIgnoreCase(Global.PreferencesKeys.userGender)) {
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
                    } else if (key.equalsIgnoreCase(Global.PreferencesKeys.userTrainingLevel)) {
                        // training level: fill the list dynamically with values
                        // Integer = entryValues, String = entries
                        Map<Integer, String> trainingLevel = Global.TrainingParameter.trainingLevel;
                        Iterator<String> entriesIterator = trainingLevel.values().iterator();
                        int arrayCount = trainingLevel.size();
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
                        Iterator<Integer> entryValuesIterator = trainingLevel.keySet().iterator();
                        String[] entryValues = new String[arrayCount];
                        index = 0;
                        while (entryValuesIterator.hasNext()) {
                            entryValues[index] = String.valueOf(entryValuesIterator.next());
                            index++;
                        }
                        ((ListPreference) preference).setEntryValues(entryValues);
                    } else if (key.equalsIgnoreCase(Global.PreferencesKeys.userBirthday)) {
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
                } else if (preference instanceof EditTextPreference){
                    // String value (EditTextPreference)
                    try {
                        // preference ui key == shared preference key
                        ((EditTextPreference) preference)
                                .setText(sharedPreferences.getString(preference.getKey(), ""));
                        // in some preferences only numbers allowed
                        if (preference.getKey().equalsIgnoreCase(Global.PreferencesKeys.userMaxPulse)) {
                            ((EditTextPreference) preference).setOnBindEditTextListener(this);
                        }
                    } catch (ClassCastException exception) {
                        ((EditTextPreference) preference).setText("");
                        if (Global.DEBUG) {
                            // TODO: Logging
                            exception.printStackTrace();
                        }
                    }
                }
            }
        }
        // set the use location of true
        // dependency by xml -> Exception if preferences not exist
        Preference useLocationDataPref = findPreference(Global.PreferencesKeys.useLocationData);
        Preference useFineLocationDataPref = findPreference(Global.PreferencesKeys.useFineLocationData);
        if (useLocationDataPref != null && useFineLocationDataPref != null) {
            useFineLocationDataPref.setDependency(useLocationDataPref.getKey());
        }
    }

    // thanks to: https://stackoverflow.com/a/15027088/15577485
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

    private void calculateMaxPulse() {
        // Maximalpuls bei Männern = 223 – 0,9 x Lebensalter
        // Maximalpuls bei Frauen = 226 – Lebensalter
        int maxPulse = 0;
        int gender = appUser.getGender();
        // we need the age to calculate
        LocalDate birthday = appUser.getBirthday();
        Period periodBetween = Period.between(birthday, LocalDate.now());
        int age = Math.abs(periodBetween.getYears());

        if (gender < 2 || gender > 3 || age < 18) {
            return;
        }
        if (Global.Defaults.valuesForCalculateMaxPulse.containsKey(gender)) {
            maxPulse = Global.Defaults.valuesForCalculateMaxPulse.get(gender);
            if (gender == 2) {
                // male
                maxPulse = maxPulse - (int) (0.9 * age);
            } else {
                // female
                maxPulse = maxPulse - age;
            }
        }
        try {
            // save the data
            appUser.setMaxPulse(maxPulse);
            viewModel.update(appUser);
        } catch (SportsLibraryException exception) {
            // TODO: Hinweis an Nutzer
            if (Global.DEBUG) {
                exception.printStackTrace();
            }
            return;
        }
        // save to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Global.PreferencesKeys.userMaxPulse, String.valueOf(maxPulse));
        editor.apply();
        // reload the preference in ui
        Preference preference = findPreference(Global.PreferencesKeys.userMaxPulse);
        if (preference instanceof EditTextPreference) {
            ((EditTextPreference) preference).setText(String.valueOf(maxPulse));
        }
    }
}