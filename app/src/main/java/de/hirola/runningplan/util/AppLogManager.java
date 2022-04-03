package de.hirola.runningplan.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import de.hirola.runningplan.R;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.util.LogManager;

import java.util.List;

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
    private final LogManager logManager;
    private final boolean canSendDebugLog;
    private final boolean isDeveloperVersion;

    public static AppLogManager getInstance(@NonNull Context context) {

        if (instance == null) {
            instance = new AppLogManager(context);
        }
        return instance;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public boolean isLoggingEnabled() {
        return logManager.isLoggingEnabled();
    }

    public boolean sendDebugLog() {
        System.out.println("Not implemented yet.");
        return false;
    }

    public boolean isDebugMode() {
        return logManager.isDebugMode();
    }

    public boolean canSendDebugLog() {
        return canSendDebugLog;
    }

    public List<LogManager.LogContent> getLogContent() {
        return logManager.getLogContent();
    }

    public boolean isDeveloperVersion() {
        return isDeveloperVersion;
    }

    private AppLogManager(Context context) {
        // initialize the attributes
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(context.getString(R.string.preference_file), Context.MODE_PRIVATE);
        canSendDebugLog = sharedPreferences.getBoolean(Global.PreferencesKeys.sendDebugLog, false);
        isDeveloperVersion = context.getString(R.string.isDeveloperVersion).equals("TRUE");
        logManager = LogManager.getInstance(context.getPackageName(),
                sharedPreferences.getBoolean(Global.PreferencesKeys.debugMode, false));
    }
}
