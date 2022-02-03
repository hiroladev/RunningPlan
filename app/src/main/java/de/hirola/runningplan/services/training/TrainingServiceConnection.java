package de.hirola.runningplan.services.training;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.sportslibrary.model.Track;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Connection to the background service.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class TrainingServiceConnection implements ServiceConnection {

    private boolean serviceBounded;
    private TrainingService trainingService;
    private final TrainingServiceCallback callback;

    protected TrainingServiceConnection() {
        serviceBounded = false;
        trainingService = null;
        callback = null;
    }

    public TrainingServiceConnection(@NonNull TrainingServiceCallback callback) {
        this.callback = callback;
        serviceBounded = false;
        trainingService = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        TrainingService.BackgroundTimerServiceBinder serviceBinder =
                (TrainingService.BackgroundTimerServiceBinder) service;
        trainingService = serviceBinder.getService();
        if (trainingService != null) {
            serviceBounded = true;
        }
        if (callback != null) {

            callback.onServiceConnected(this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (callback != null) {
            callback.onServiceDisconnected(this);
        }
        serviceBounded = false;
        trainingService = null;
    }

    public void bindAndStartService(@NonNull Context context) {
        if (trainingService != null) {
            // Service is already started and bound.
            return;
        }
        // start the service
        context.startService(new Intent(context, TrainingService.class));
        // bind service to the connection
        serviceBounded = context.bindService(new Intent(context, TrainingService.class), this, 0);
    }

    public void stopAndUnbindService(@NonNull Context context) {
        try {
            context.unbindService(this);
            context.stopService(new Intent(context, TrainingService.class));
        } catch (Exception exception) {
            //TODO: Logging
            exception.printStackTrace();
        } finally {
            serviceBounded = false;
        }
    }

    public Track.Id startTraining(long durationInSeconds, boolean withLocationTracking) {
        if (serviceBounded) {
            return trainingService.startTraining(durationInSeconds, withLocationTracking);
        }
        return null;
    }

    /**
     * Continues a workout. If a duration greater than 0 is specified, the training duration is updated.
     *
     * @param durationInSeconds new duration in seconds for the continuation of the training 
     *                          or 0 if the previous training time should be continued
     * @param trackId of track for the training
     */
    public void resumeTraining(long durationInSeconds, @Nullable Track.Id trackId) {
        if (serviceBounded) {
            trainingService.resumeTraining(durationInSeconds, trackId);
        }
    }

    public void pauseTraining() {
        if (serviceBounded) {
            trainingService.pauseTraining();
        }
    }

    public void cancelTraining() {
        if (serviceBounded) {
            trainingService.stopTraining();
        }
    }

    public void endTraining() {
        if (serviceBounded) {
            trainingService.stopTraining();
        }
    }

    public boolean isTrainingActive() {
        if (serviceBounded) {
            return trainingService.isTrainingActive();
        }
        return false;
    }

    /**
     * Returns the actual countdown time.
     *
     * @return the actual time for countdown in seconds or -1 if service not bound
     */
    public long getActualDuration() {
        if (serviceBounded) {
            return trainingService.getTrainingDuration();
        }
        return -1;
    }

    @Nullable
    public Track getRecordedTrack(Track.Id trackId) {
        return trainingService.getRecordedTrack(trackId);
    }
}
