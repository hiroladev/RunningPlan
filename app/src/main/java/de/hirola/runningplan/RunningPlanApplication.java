package de.hirola.runningplan;

import android.content.ContentProvider;
import android.os.Build;
import de.hirola.sportslibrary.DataRepository;
import de.hirola.sportslibrary.SportsLibrary;

import android.app.Application;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.util.Logger;

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
public class RunningPlanApplication extends Application {

    private SportsLibrary sportsLibrary;

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     *
     * <p>Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.</p>
     *
     * <p>If you override this method, be sure to call {@code super.onCreate()}.</p>
     *
     * <p class="note">Be aware that direct boot may also affect callback order on
     * Android {@link Build.VERSION_CODES#N} and later devices.
     * Until the user unlocks the device, only direct boot aware components are
     * allowed to run. You should consider that all direct boot unaware
     * components, including such {@link ContentProvider}, are
     * disabled until user unlock happens, especially when component callback
     * order matters.</p>
     */
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // initialize the SportsLibrary, e.g. local datastore and logging,
            // import the templates on first start
            sportsLibrary = SportsLibrary.getInstance("RunningPlan");
        } catch (SportsLibraryException exception) {
            throw new RuntimeException("An exception occurred while initialize the app: "
                    + exception);
        }
    }

    /**
     * This object is used for multiply tasks in the App,
     * e.g. the (persistent) data handling, sync and logging.
     *
     * @return the object for the local datastore
     */
    public SportsLibrary getSportsLibrary() {
        return sportsLibrary;
    }
}
