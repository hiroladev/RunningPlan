package de.hirola.runningplan.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.*;
import de.hirola.runningplan.R;
import de.hirola.runningplan.model.RunningPlanRepository;
import de.hirola.runningplan.model.RunningPlanViewModel;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.User;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

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
        ArrayList<Preference> preferenceList = getPreferenceList(getPreferenceScreen(), new ArrayList<Preference>());
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
                // validate the given value
                editor.putString(key, maxPulse);


            }
            // TODO: calculate from date
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
                            exception.printStackTrace();
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

    private int calculateMaxPulse(int yearOfBirth) {
        // TODO: Berechnung max. Puls
        // kann aktuell nur für Männer oder Frauen berechnet werden
        /*let gender = self.genderPickerView.selectedRow(inComponent: 0)

        //  Alter mindestens 18
        let calendar = Calendar.current
        var components = calendar.dateComponents([.year], from: Date())
        let actualYear = (components.year ?? Global.actualYear)
        let row = self.birthdayPickerView.selectedRow(inComponent: 0)
        let yearOfBirth = actualYear - row
        let birthay = "11.07." + String(yearOfBirth)
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "d.M.yyyy"
        let birthday = dateFormatter.date(from: birthay)
        components = calendar.dateComponents([.year], from: birthday ?? Date(), to: Date())

        if gender < 2 || components.year ?? 17 < 18 {

            //  Hinweis an Nutzer
            let alert = UIAlertController(title: NSLocalizedString("Keine Berechnung möglich", comment: "Keine Berechnung möglich"),
            message: NSLocalizedString("Kann nur für Männer oder Frauen ab 18 Jahren berechnet werden.",
                    comment: "Formel zur Berechnug des max. Puls funktioniert nur für Männer und Frauen."),
            preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                self.present(alert, animated: true, completion: nil)
                return

        }
        //  max. Puls = 220 (Männer) bzw. 226 (Frauen) - Alter
        let maxPulse: Int = (Global.valuesForCalculateMaxPulse[gender] ?? 18) - components.year!
        if maxPulse == 0 {

            //  ... da ist was schief gegangen
            //  Hinweis an Nutzer
            let alert = UIAlertController(title: NSLocalizedString("Formel zur Berechnug enthielt Fehler",
                    comment: "Formel zur Berechnug enthielt Fehler."),
            message: "",
                    preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                self.present(alert, animated: true, completion: nil)

        }
        self.maxPulseTextField.text = String(maxPulse)
        */
        return 100;
    }
}