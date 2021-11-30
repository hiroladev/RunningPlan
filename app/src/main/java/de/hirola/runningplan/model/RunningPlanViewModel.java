package de.hirola.runningplan.model;

import de.hirola.kintojava.KintoException;
import de.hirola.sportslibrary.model.RunningPlan;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;

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
    private RunningPlanRepository repository;
    // observe data changing in model to refresh the ui
    private LiveData<ArrayList<RunningPlan>> runningPlans;

    public RunningPlanViewModel(Application application) {
        super(application);
        // init the repository
        repository = new RunningPlanRepository(application);
        runningPlans = repository.getRunningPlans();
    }

    public LiveData<ArrayList<RunningPlan>> getRunningPlans() {
        return runningPlans;
    }

    public void addRunningPlan(RunningPlan runningPlan) throws KintoException {
        repository.addRunningPlan(runningPlan);
    }

}
