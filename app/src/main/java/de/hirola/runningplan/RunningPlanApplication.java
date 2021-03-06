package de.hirola.runningplan;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.LocaleList;
import de.hirola.sportsapplications.Global;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.SportsLibraryApplication;
import de.hirola.sportsapplications.SportsLibraryException;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * This application object is available while the app exists.
 * The object is created automatically by the android runtime, when the app starts.
 * In all activities you can access to this object.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class RunningPlanApplication extends Application
        implements SportsLibraryApplication, SharedPreferences.OnSharedPreferenceChangeListener {

    private SportsLibrary sportsLibrary;
    private boolean debugMode;
    private boolean canSendDebugLogs;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // create or get the app directory
            File appDirectory = SportsLibrary.initializeAppDirectory(getPackageName());
            // determine the debug state
            Context context = getApplicationContext();
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(context.getString(R.string.preference_file), Context.MODE_PRIVATE);
            debugMode = sharedPreferences.getBoolean(Global.UserPreferencesKeys.DEBUG_MODE, false);
            // while app is using, the user change settings
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
            canSendDebugLogs = sharedPreferences.getBoolean(Global.UserPreferencesKeys.SEND_DEBUG_LOG, false);
            // initialize the SportsLibrary, e.g. local datastore and logging,
            // import the templates on first start
            // get the actual locale
            Locale locale = LocaleList.getDefault().get(0);
            sportsLibrary = SportsLibrary.getInstance(debugMode, locale, appDirectory,this);
        } catch (SportsLibraryException | InstantiationException exception) {
            throw new RuntimeException("An exception occurred while initialize the app: "
                    + exception);
        }
    }

    @Override
    public InputStream getMovementTypeTemplates() {
        return getResources().openRawResource(R.raw.movement_types);
    }

    @Override
    public InputStream getTrainingTypeTemplates() {
        return getResources().openRawResource(R.raw.training_types);
    }

    @Override
    public InputStream[] getRunningPlanTemplates() {
        boolean isDeveloperVersion = getString(R.string.isDeveloperVersion).equalsIgnoreCase("TRUE");
        InputStream[] inputStreams;
        if (isDeveloperVersion) {
            inputStreams = new InputStream[4];
        } else {
            inputStreams = new InputStream[3];
        }
        inputStreams[0] = getResources().openRawResource(R.raw.start);
        inputStreams[1] = getResources().openRawResource(R.raw.start60);
        inputStreams[2] = getResources().openRawResource(R.raw.start90);
        if (isDeveloperVersion) {
            inputStreams[3] = getResources().openRawResource(R.raw.start_test);
        }
        return inputStreams;
    }

    public SportsLibrary getSportsLibrary() {
        return sportsLibrary;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Global.UserPreferencesKeys.DEBUG_MODE)) {
            debugMode = sharedPreferences.getBoolean(key, false);
        }
        if (key.equals(Global.UserPreferencesKeys.SEND_DEBUG_LOG)) {
            canSendDebugLogs = sharedPreferences.getBoolean(key, false);
        }
    }

}
