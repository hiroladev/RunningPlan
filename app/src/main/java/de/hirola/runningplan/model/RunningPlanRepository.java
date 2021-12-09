package de.hirola.runningplan.model;

import de.hirola.runningplan.RunningPlanApplication;

import de.hirola.sportslibrary.DataRepository;
import de.hirola.sportslibrary.PersistentObject;
import de.hirola.sportslibrary.SportsLibrary;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;
import de.hirola.sportslibrary.util.Logger;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
    // observe data changing in model to refresh the ui
    private final MutableLiveData<List<RunningPlan>> runningPlans;
    private final MutableLiveData<User> appUser;

    public RunningPlanRepository(Application application) throws RuntimeException {
        // initialize attributes
        SportsLibrary sportsLibrary = ((RunningPlanApplication) application).getSportsLibrary();
        dataRepository = sportsLibrary.getDataRepository();
        try {
            // load all running plans from datastore
            List<? extends PersistentObject> persistentObjects = dataRepository.findAll(RunningPlan.class);
            runningPlans = new MutableLiveData<>();
            // noinspection unchecked
            runningPlans.setValue((List<RunningPlan>) persistentObjects);
            appUser = new MutableLiveData<User>(sportsLibrary.getAppUser());
        } catch (SportsLibraryException exception) {
            // serious problems in data model
            throw new RuntimeException("Error occurred while searching for data: "
                    + exception);
        }
    }

    public LiveData<User> getAppUser() {
        return appUser;
    }

    public LiveData<List<RunningPlan>> getRunningPlans() {
        return runningPlans;
    }

    public void add(PersistentObject persistentObject) throws SportsLibraryException {
        dataRepository.add(persistentObject);
    }

    public void update(PersistentObject persistentObject) throws SportsLibraryException {
        dataRepository.update(persistentObject);
    }
}
