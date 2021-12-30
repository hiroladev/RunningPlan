package de.hirola.runningplan.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import de.hirola.sportslibrary.PersistentObject;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.model.User;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public RunningPlanViewModel(Application application) {
        super(application);
        // init the repository
        repository = new RunningPlanRepository(application);
    }

    public User getAppUser() {
        return repository.getAppUser();
    }

    public List<RunningPlan> getRunningPlans() {
        return repository.getRunningPlans();
    }

    public void add(PersistentObject persistentObject) throws SportsLibraryException {
        // add object to datastore
        repository.add(persistentObject);

    }

    public void update(PersistentObject persistentObject) throws SportsLibraryException {
        repository.update(persistentObject);
    }
}
