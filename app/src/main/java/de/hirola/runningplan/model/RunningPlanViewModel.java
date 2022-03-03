package de.hirola.runningplan.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.RunningPlanApplication;

import de.hirola.sportslibrary.database.DataRepository;
import de.hirola.sportslibrary.database.DatastoreDelegate;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.SportsLibrary;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;
import android.app.Application;
import de.hirola.sportslibrary.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * The repository for the running plan data.
 * It manages the sync and local data store.
 *
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanViewModel {

    private final static String TAG = RunningPlanViewModel.class.getSimpleName();

    private final Logger logger;
    private final SportsLibrary sportsLibrary;
    private final DataRepository dataRepository; // the datastore layer

    public RunningPlanViewModel(@NonNull Application application, @Nullable DatastoreDelegate delegate)  {
        // initialize attributes
        logger = Logger.getInstance(application.getPackageName());
        sportsLibrary = ((RunningPlanApplication) application).getSportsLibrary();
        if (delegate != null) {
            sportsLibrary.addDelegate(delegate);
        }
        dataRepository = sportsLibrary.getDataRepository();
    }

    @NonNull
    public User getAppUser() {
        return sportsLibrary.getAppUser();
    }

    @Nullable
    public RunningPlan getRunningPlanByUUID(@NonNull UUID uuid) {
        return (RunningPlan) dataRepository.findByUUID(RunningPlan.class, uuid);
    }

    public List<RunningPlan> getRunningPlans() {
        List<RunningPlan> runningPlans = new ArrayList<>();
        List<? extends PersistentObject> persistentObjects = dataRepository.findAll(RunningPlan.class);
        if (persistentObjects.isEmpty()) {
            return runningPlans; // return an empty list
        }
        for (PersistentObject object : persistentObjects) {
            try {
                runningPlans.add((RunningPlan) object);
            } catch (ClassCastException exception) {
                // we do not add this to the list and make a  log entry
                String errorMessage = "List of running plans contains an object from type "
                        + object.getClass().getSimpleName();
                logger.log(Logger.DEBUG, TAG, errorMessage, exception);
            }
        }
        // sort the plans
        Collections.sort(runningPlans);
        return runningPlans;
    }

    public void addObject(PersistentObject persistentObject) throws SportsLibraryException {
        dataRepository.add(persistentObject);
    }

    public void updateObject(PersistentObject persistentObject) throws SportsLibraryException {
        dataRepository.update(persistentObject);
    }

    public void deleteObject(PersistentObject persistentObject) throws SportsLibraryException {
        dataRepository.delete(persistentObject);
    }

    public void removeDelegate(@NonNull DatastoreDelegate delegate) {
        sportsLibrary.removeDelegate(delegate);
    }
}
