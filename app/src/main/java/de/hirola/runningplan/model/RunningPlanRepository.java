package de.hirola.runningplan.model;

import androidx.lifecycle.LiveData;
import de.hirola.kintojava.Kinto;
import de.hirola.kintojava.KintoConfiguration;
import de.hirola.kintojava.KintoException;
import de.hirola.kintojava.logger.Logger;
import de.hirola.kintojava.logger.LoggerConfiguration;
import de.hirola.kintojava.model.KintoObject;
import de.hirola.sportslibrary.model.*;

import android.app.Application;

import java.util.ArrayList;

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

    // the kinto data layer
    private Kinto kinto;
    // observe data changing in model to refresh the ui
    private LiveData<ArrayList<RunningPlan>> runningPlans;

    public RunningPlanRepository(Application application) {

        // create the app logger
        LoggerConfiguration loggerConfiguration = new LoggerConfiguration.Builder("runningplan-logs")
                .logggingDestination(LoggerConfiguration.LOGGING_DESTINATION.CONSOLE + LoggerConfiguration.LOGGING_DESTINATION.FILE)
                .build();
        Logger logger = Logger.init(loggerConfiguration);

        // add all types for managing by kinto java
        ArrayList<Class<? extends KintoObject>> typeList = new ArrayList<>();
        typeList.add(User.class);
        typeList.add(LocationData.class);
        typeList.add(Track.class);
        typeList.add(MovementType.class);
        typeList.add(TrainingType.class);
        typeList.add(Training.class);
        typeList.add(RunningUnit.class);
        typeList.add(RunningPlanEntry.class);
        typeList.add(RunningPlan.class);

        // create a kinto java configuration
        try {
            KintoConfiguration configuration = new KintoConfiguration.Builder("StoreTest")
                    .objectTypes(typeList)
                    .build();
            // create the kinto java instance
           kinto = Kinto.getInstance(configuration);
        } catch (KintoException exception) {
            exception.printStackTrace();
        }
    }

    LiveData<ArrayList<RunningPlan>> getRunningPlans() {
        return runningPlans;
    }

    /**
     * Add an running plan to the local datastore.
     * It must call this on a non-UI thread to avoid blocking the UI.
     *
     * @param runningPlan
     * @throws KintoException
     */
    public void addRunningPlan(RunningPlan runningPlan) throws KintoException {
        kinto.add(runningPlan);
    }
}
