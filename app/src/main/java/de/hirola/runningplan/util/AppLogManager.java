package de.hirola.runningplan.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.R;
import de.hirola.sportslibrary.Global;
import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;

import java.io.File;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Manager for app logging and debugging.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class AppLogManager {

    private static AppLogManager instance = null;
    private final boolean isLoggingEnabled;
    private final boolean debugMode;
    private final boolean canSendDebugLog;

    public static AppLogManager getInstance(@NonNull Context context) {

        if (instance == null) {
            instance = new AppLogManager(context);
        }
        return instance;
    }

    public boolean isLoggingEnabled() {
        return isLoggingEnabled;
    }

    public boolean sendDebugLog() {
        System.out.println("Not implemented yet.");
        return false;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean canSendDebugLog() {
        return canSendDebugLog;
    }

    public String getLogContent() {
        return "";
    }

    private AppLogManager(Context context) {
        String logDirString = "/data/data"
                + File.separatorChar
                + context.getPackageName();
        // set the property for the rolling file logger
        System.setProperty("tinylog.directory", logDirString);
        // initialize the attributes
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(context.getString(R.string.preference_file), Context.MODE_PRIVATE);
        debugMode = Global.APP_DEBUG_MODE && sharedPreferences.getBoolean(Global.PreferencesKeys.debugMode, false);
        canSendDebugLog = sharedPreferences.getBoolean(Global.PreferencesKeys.sendDebugLog, false);
        isLoggingEnabled = true;
    }
}
