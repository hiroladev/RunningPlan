package de.hirola.runningplan.model;

import de.hirola.runningplan.RunningPlanApplication;

import de.hirola.sportslibrary.database.DataRepository;
import de.hirola.sportslibrary.database.PersistentObject;
import de.hirola.sportslibrary.SportsLibrary;
import de.hirola.sportslibrary.SportsLibraryException;
import de.hirola.sportslibrary.model.*;
import android.app.Application;
import de.hirola.sportslibrary.util.Logger;

import java.util.ArrayList;
import java.util.Iterator;
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

    private final static String TAG = RunningPlanRepository.class.getSimpleName();

    private final Logger logger = Logger.getInstance(null);
    private final DataRepository dataRepository; // the datastore layer
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
        List<RunningPlan> runningPlans = new ArrayList<>();
        List<? extends PersistentObject> persistentObjects = dataRepository.findAll(RunningPlan.class);
        if (persistentObjects.isEmpty()) {
            return runningPlans; // return an empty list
        }
        for (PersistentObject object : persistentObjects) {
            try {
                runningPlans.add((RunningPlan) object);
            } catch (ClassCastException exception) {
                // we do not add thi sto list and make a  log entry
                String errorMessage = "List of running plans contains an object from type "
                        + object.getClass().getSimpleName();
                logger.log(Logger.DEBUG, TAG, errorMessage, exception);
            }
        }

        return runningPlans;
    }

    public void save(PersistentObject persistentObject) throws SportsLibraryException {
        dataRepository.save(persistentObject);
    }

    public void delete(PersistentObject persistentObject) throws SportsLibraryException {
        dataRepository.delete(persistentObject);
    }
}
