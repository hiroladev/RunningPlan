package de.hirola.runningplan;

import android.app.Application;
import de.hirola.sportslibrary.SportsLibrary;
import de.hirola.sportslibrary.SportsLibraryApplication;
import de.hirola.sportslibrary.SportsLibraryException;

import java.io.InputStream;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * This application object is available while the app exists.
 * The object is created automatically by the android runtime, when the app starts.
 * In all activities you can access to this object.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanApplication extends Application implements SportsLibraryApplication {

    private SportsLibrary sportsLibrary;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // initialize the SportsLibrary, e.g. local datastore and logging,
            // import the templates on first start
            sportsLibrary = new SportsLibrary(getPackageName(), this);
        } catch (SportsLibraryException exception) {
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
        InputStream[] inputStreams = new InputStream[3];
        inputStreams[0] = getResources().openRawResource(R.raw.start);
        inputStreams[1] = getResources().openRawResource(R.raw.start60);
        inputStreams[2] = getResources().openRawResource(R.raw.start_test);
        return inputStreams;
    }

    public SportsLibrary getSportsLibrary() {
        return sportsLibrary;
    }

}
