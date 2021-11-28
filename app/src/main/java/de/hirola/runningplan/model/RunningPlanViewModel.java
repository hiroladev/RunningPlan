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
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class RunningPlanViewModel extends AndroidViewModel {

    private RunningPlanRepository repository;

    public RunningPlanViewModel(Application application) {
        super(application);
        // init the repository
        repository = new RunningPlanRepository(application);
    }

    LiveData<ArrayList<RunningPlan>> getRunningPlans() {
        return repository.getRunningPlans();
    }

    public void addRunningPlan(RunningPlan runningPlan) throws KintoException {
        repository.addRunningPlan(runningPlan);
    }

}
