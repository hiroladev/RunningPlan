package de.hirola.runningplan.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.R;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.util.Logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
    private final boolean sendDebugLog;

    public static AppLogManager getInstance(@NonNull Context context) {

        if (instance == null) {
            instance = new AppLogManager(context);
        }
        return instance;
    }

    public void log(@NonNull String source, @Nullable String logMessage, @Nullable Exception exception) {
        //TODO: logging to file use SportsLibrary
        if (exception != null) {
            exception.printStackTrace();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        // Instant representing a moment on the timeline in UTC with a resolution of nanoseconds
        // to format an Instant a time-zone is required.
        String logDateTime = formatter
                .withZone( ZoneId.of("UTC"))
                .format(Instant.now());
        String messagePrefix = logDateTime + " - " + source + " : ";
        if (logMessage == null) {
            System.out.println(messagePrefix.concat("No log message available."));
        } else {
            System.out.println(messagePrefix.concat(logMessage));
        }
    }

    public void debug(boolean logToRemote, @Nullable Exception exception, @NonNull String logMessage) {
        //TODO: logging to file / remote
        if (logToRemote) {
            System.out.println("Not implemented yet.");
        }
    }

    public boolean sendDebugLog() {
        System.out.println("Not implemented yet.");
        return false;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isSendDebugLog() {
        return sendDebugLog;
    }

    public Logger getLogger() {
        return logger;
    }

    private AppLogManager(Context context) {
        logger = Logger.getInstance(context.getPackageName());
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(context.getString(R.string.preference_file), Context.MODE_PRIVATE);
        debugMode = Global.APP_DEBUG_MODE || sharedPreferences.getBoolean(Global.PreferencesKeys.debugMode, false);
        sendDebugLog = sharedPreferences.getBoolean(Global.PreferencesKeys.sendDebugLog, false);
    }
}
