package de.hirola.runningplan.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.R;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.util.Logger;

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
    private final Logger logger;
    private final boolean debugMode;
    private final boolean canSendDebugLog;

    public static AppLogManager getInstance(@NonNull Context context) {

        if (instance == null) {
            instance = new AppLogManager(context);
        }
        return instance;
    }

    public void debug(@NonNull String source, @Nullable String logMessage, @Nullable Exception exception) {
        if (logMessage == null && exception == null) {
            return;
        }
        if (logMessage == null) {
            logMessage = "No debug message available.";
        }
        logger.log(Logger.DEBUG, source, logMessage, exception);
    }

    /**
     * Create a log entry. For severity use the flags from Logger.
     *
     * @param severity  of log entry
     * @param source of log entry
     * @param logMessage of log entry
     * @param exception source of log entry
     * @see Logger
     */
    public void log(int severity, @NonNull String source, @Nullable String logMessage, @Nullable Exception exception) {
        if (logMessage == null && exception == null) {
            return;
        }
        if (logMessage == null) {
            logMessage = "No debug message available.";
        }
        switch (severity) {
            case Logger.INFO: logger.log(Logger.INFO, source, logMessage, exception); return;
            case Logger.WARNING: logger.log(Logger.WARNING, source, logMessage, exception); return;
            case Logger.ERROR: logger.log(Logger.ERROR, source, logMessage, exception);
        }
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

    public Logger getLogger() {
        return logger;
    }

    public String getLogContent() {
        return logger.getLogContent();
    }

    private AppLogManager(Context context) {
        logger = Logger.getInstance(context.getPackageName());
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(context.getString(R.string.preference_file), Context.MODE_PRIVATE);
        debugMode = Global.APP_DEBUG_MODE || sharedPreferences.getBoolean(Global.PreferencesKeys.debugMode, false);
        canSendDebugLog = sharedPreferences.getBoolean(Global.PreferencesKeys.sendDebugLog, false);
    }
}
