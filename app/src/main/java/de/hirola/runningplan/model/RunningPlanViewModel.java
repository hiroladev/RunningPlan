package de.hirola.runningplan.model;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.R;
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.runningplan.ui.runningplans.RunningPlanRecyclerView;
import de.hirola.sportsapplications.Global;
import de.hirola.sportsapplications.database.DatastoreDelegate;
import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.SportsLibraryException;
import de.hirola.sportsapplications.model.*;
import android.app.Application;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * The repository for the running plan data.
 * It manages the sync and local data store.
 *
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public class RunningPlanViewModel {

    private final Application application;
    private final SportsLibrary sportsLibrary;

    public RunningPlanViewModel(@NonNull Application application, @Nullable DatastoreDelegate delegate)  {
        // initialize attributes
        this.application = application;
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
     * If the user setting HIDE_TEMPLATES is <B>true</B>, the list only contains plans
     * that were not defined as a template (isTemplate).
     * The list can be empty.
     *
     * @return A list of (filtered) running plans, sorted by order number.
     * @see RunningPlan
     */
    public List<RunningPlan> getRunningPlans() {
        List<RunningPlan> runningPlans = sportsLibrary.getRunningPlans();
        // should I hide templates?
        SharedPreferences sharedPreferences =
                application.getSharedPreferences(application.getString(R.string.preference_file), Context.MODE_PRIVATE);
        boolean hideTemplates = sharedPreferences.getBoolean(Global.UserPreferencesKeys.HIDE_TEMPLATES,false);
        // filter the list
        if (hideTemplates) {
            Stream<RunningPlan> filteredStream = runningPlans.stream().filter(runningPlan -> !runningPlan.isTemplate());
            return filteredStream.collect(Collectors.toList());
        }
        return runningPlans;
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
