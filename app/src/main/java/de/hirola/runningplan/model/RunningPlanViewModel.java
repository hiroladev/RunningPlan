package de.hirola.runningplan.model;

import androidx.annotation.NonNull;
import de.hirola.sportslibrary.PersistentObject;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.RunningPlan;
import de.hirola.sportslibrary.model.User;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

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
    // observe data changing in model to refresh the ui
    private final LiveData<List<RunningPlan>> runningPlans;
    public LiveData<User> appUser;

    public RunningPlanViewModel(Application application) {
        super(application);
        // init the repository
        repository = new RunningPlanRepository(application);
        runningPlans = repository.getRunningPlans();
        appUser = repository.getAppUser();
    }

    public LiveData<List<RunningPlan>> getRunningPlans() {
        return runningPlans;
    }

    public @NonNull LiveData<User> getAppUser() {
        return appUser;
    }

    public void add(PersistentObject persistentObject) throws SportsLibraryException {
        repository.add(persistentObject);
    }

    public void update(PersistentObject persistentObject) throws SportsLibraryException {
        repository.update(persistentObject);
    }
}
