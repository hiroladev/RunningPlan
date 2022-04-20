package de.hirola.runningplan.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.sportsapplications.database.DatastoreDelegate;
import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.SportsLibraryException;
import de.hirola.sportsapplications.model.*;
import android.app.Application;

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

    private final SportsLibrary sportsLibrary;

    public RunningPlanViewModel(@NonNull Application application, @Nullable DatastoreDelegate delegate)  {
        // initialize attributes
        sportsLibrary = ((RunningPlanApplication) application).getSportsLibrary();
        if (delegate != null) {
            sportsLibrary.addDelegate(delegate);
        }
    }

    @NonNull
    public User getAppUser() {
        return sportsLibrary.getAppUser();
    }

    @Nullable
    public RunningPlan getRunningPlanByUUID(@NonNull UUID uuid) {
        return (RunningPlan) sportsLibrary.findByUUID(RunningPlan.class, uuid);
    }

    /**
     * Get the list of running plans, sorted by order number.
     * The list can be empty.
     *
     * @return A list of running plans, sorted by order number.
     */
    public List<RunningPlan> getRunningPlans() {
        return sportsLibrary.getRunningPlans();
    }

    /**
     * Get the list of trainings, sorted by date of training.
     * The list can be empty.
     *
     * @return A list of trainings, sorted by training date.
     */
    public List<Training> getTrainings() {
        return sportsLibrary.getTrainings();
    }

    /**
     * Add a new object to datastore.
     *
     * @param persistentObject to be added to the datastore
     * @return True, if the object was added or false if an error occurred.
     */
    public boolean addObject(PersistentObject persistentObject)  {
        try {
            sportsLibrary.add(persistentObject);
            return true;
        } catch (SportsLibraryException exception) {
            if (sportsLibrary.isDebugMode()) {
                sportsLibrary.debug("Adding an object failed.", exception);
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
            sportsLibrary.update(persistentObject);
            return true;
        } catch (SportsLibraryException exception) {
            if (sportsLibrary.isDebugMode()) {
                sportsLibrary.debug("Updating an object failed.", exception);
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
            sportsLibrary.delete(persistentObject);
            return true;
        } catch (SportsLibraryException exception) {
            if (sportsLibrary.isDebugMode()) {
                sportsLibrary.debug("Deleting an object failed.", exception);
            }
        }
        return false;
    }

    public void removeDelegate(@NonNull DatastoreDelegate delegate) {
        sportsLibrary.removeDelegate(delegate);
    }
}
