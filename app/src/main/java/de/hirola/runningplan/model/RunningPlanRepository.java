package de.hirola.runningplan.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import de.hirola.runningplan.RunningPlanApplication;

import de.hirola.sportslibrary.DataRepository;
import de.hirola.sportslibrary.PersistentObject;
import de.hirola.sportslibrary.SportsLibrary;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;
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
 * @since 1.1.1
 */
public class RunningPlanRepository {

    // the datastore layer
    private final DataRepository dataRepository;
    private final User appUser;

    public RunningPlanRepository(Application application) throws RuntimeException {
        // initialize attributes
        SportsLibrary sportsLibrary = ((RunningPlanApplication) application).getSportsLibrary();
        dataRepository = sportsLibrary.getDataRepository();
        appUser = sportsLibrary.getAppUser();
    }

    public User getAppUser() {
        return appUser;
    }

    public List<RunningPlan> getRunningPlans() {
        try {
            // load all running plans from datastore
            // noinspection unchecked
            return (List<RunningPlan>) dataRepository.findAll(RunningPlan.class);
        } catch (SportsLibraryException exception) {
            // serious problems in data model
            throw new RuntimeException("Error occurred while searching for data: "
                    + exception);
        }
    }

    public void add(PersistentObject persistentObject) throws SportsLibraryException {
        dataRepository.add(persistentObject);
    }

    public void update(PersistentObject persistentObject) throws SportsLibraryException {
        dataRepository.update(persistentObject);
    }
}
