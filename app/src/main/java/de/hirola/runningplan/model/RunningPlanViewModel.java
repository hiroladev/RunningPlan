package de.hirola.runningplan.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.RunningPlanApplication;

import de.hirola.runningplan.util.AppLogManager;
import de.hirola.sportslibrary.database.DataRepository;
import de.hirola.sportslibrary.database.DatastoreDelegate;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.SportsLibrary;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;
import android.app.Application;

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
 * @since 0.1
 */
public class RunningPlanViewModel {

    private final static String TAG = RunningPlanViewModel.class.getSimpleName();

    private final AppLogManager appLogger;
    private final SportsLibrary sportsLibrary;
    private final DataRepository dataRepository; // the datastore layer

    public RunningPlanViewModel(@NonNull Application application, @Nullable DatastoreDelegate delegate)  {
        // initialize attributes
        appLogger = AppLogManager.getInstance(application.getApplicationContext());
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

    /**
     * Get the list of running plans, sorted by order number.
     * The list can be empty.
     *
     * @return A list of running plans, sorted by order number.
     */
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
                // we do not add this to the list and make a  debug entry
                String errorMessage = "List of running plans contains an object from type "
                        + object.getClass().getSimpleName();
                if (appLogger.isDebugMode()) {
                    appLogger.debug(TAG, errorMessage, exception);
                }
            }
        }
        // sort the plans
        Collections.sort(runningPlans);
        return runningPlans;
    }

    /**
     * Get the list of trainings, sorted by date of training.
     * The list can be empty.
     *
     * @return A list of trainings, sorted by training date.
     */
    public List<Training> getTrainings() {
        List<Training> trainings = new ArrayList<>();
        List<? extends PersistentObject> persistentObjects = dataRepository.findAll(Training.class);
        if (persistentObjects.isEmpty()) {
            return trainings; // return an empty list
        }
        for (PersistentObject object : persistentObjects) {
            try {
                trainings.add((Training) object);
            } catch (ClassCastException exception) {
                // we do not add this to the list and make a  debug entry
                String errorMessage = "List of trainings contains an object from type "
                        + object.getClass().getSimpleName();
                if (appLogger.isDebugMode()) {
                    appLogger.debug(TAG, errorMessage, exception);
                }
            }
        }
        // sort the training by date
        Collections.sort(trainings);
        return trainings;
    }

    /**
     * Add a new object to datastore.
     *
     * @param persistentObject to be added to the datastore
     * @return True, if the object was added or false if an error occurred.
     */
    public boolean addObject(PersistentObject persistentObject)  {
        try {
            dataRepository.add(persistentObject);
            return true;
        } catch (SportsLibraryException exception) {
            if (appLogger.isDebugMode()) {
                appLogger.debug(TAG, "Adding an object failed.", exception);
            }
        }
        return false;
    }

    /**
     * Update an existing object in datastore.
     *
     * @param persistentObject to be added to the datastore
     * @return True, if the object was updated or false if an error occurred.
     */
    public boolean updateObject(PersistentObject persistentObject) {
        try {
            dataRepository.update(persistentObject);
            return true;
        } catch (SportsLibraryException exception) {
            if (appLogger.isDebugMode()) {
                appLogger.debug(TAG, "Updating an object failed.", exception);
            }
        }
        return false;
    }

    /**
     * Delete an existing object in datastore.
     *
     * @param persistentObject to be added to the datastore
     * @return True, if the object was deleted or false if an error occurred.
     */
    public boolean deleteObject(PersistentObject persistentObject) {
        try {
            dataRepository.delete(persistentObject);
            return true;
        } catch (SportsLibraryException exception) {
            if (appLogger.isDebugMode()) {
                appLogger.debug(TAG, "Deleting an object failed.", exception);
            }
        }
        return false;
    }

    public void removeDelegate(@NonNull DatastoreDelegate delegate) {
        sportsLibrary.removeDelegate(delegate);
    }
}
