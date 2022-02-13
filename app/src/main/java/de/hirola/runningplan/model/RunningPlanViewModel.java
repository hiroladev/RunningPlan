package de.hirola.runningplan.model;

import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.model.User;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Model for the app data (running plans).
 * It shares the data between the different fragments.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanViewModel extends AndroidViewModel {

    // the repository for the app data
    private final RunningPlanRepository repository;
    // the running plans live data
    private final MutableListLiveData<RunningPlan> runningPlans;

    public RunningPlanViewModel(Application application) {
        super(application);
        // init the repository
        repository = new RunningPlanRepository(application);
        // load the running plans from data store
        runningPlans = new MutableListLiveData<>();
        runningPlans.setValue(repository.getRunningPlans());
    }

    public User getAppUser() {
        return repository.getAppUser();
    }

    public MutableListLiveData<RunningPlan> getMutableRunningPlans() {
        return runningPlans;
    }

    public List<RunningPlan> getRunningPlanes() {
        return repository.getRunningPlans();
    }

    public void save(PersistentObject persistentObject) throws SportsLibraryException {
        // add object to datastore
        repository.save(persistentObject);
        if (persistentObject instanceof RunningPlan) {
            runningPlans.addItem((RunningPlan) persistentObject);
        }
    }

    public void delete(PersistentObject persistentObject) throws SportsLibraryException {
        // remove object from datastore
        repository.delete(persistentObject);
        if (persistentObject instanceof RunningPlan) {
            runningPlans.removeItem((RunningPlan) persistentObject);
        }
    }

}
