package de.hirola.runningplan.services.training;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.R;
import de.hirola.runningplan.util.AppLogManager;
import de.hirola.runningplan.util.TrainingNotificationManager;
import de.hirola.sportslibrary.model.Track;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A background service to count the running time.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class TrainingService extends Service implements LocationListener {

    private final static String TAG = TrainingService.class.getSimpleName();

    // timer interval (tick) in milliseconds
    // used for location updates too
    private final static long TIME_INTERVAL_IN_MILLI = 1000;
    // min distance for location updates in m
    private final static long LOCATION_UPDATE_MIN_DISTANCE = 1;

    private Handler handler;
    private AppLogManager logManager;
    private TrainingNotificationManager notificationManager;
    private final IBinder serviceBinder = new BackgroundTimerServiceBinder();
    private TrackManager trackManager;
    private Track.Id trackId = null; // actual recording track
    private LocationManager locationManager; // location manager
    private boolean withLocationTracking = false;
    private boolean gpsAvailable = false; // (gps) tracking available?
    private long trainingDuration = 0L;
    private boolean isTrainingActive = false; // flag if the timer running
    private boolean isTrainingPaused = false; // flag the state of recording track

    private final Runnable secondsInTraining = new Runnable() {
        @Override
        public void run() {
            if (!isTrainingActive || isTrainingPaused) {
                // training paused
                return;
            }
            // countdown
            trainingDuration = trainingDuration - 1;
            // send a time update to the activity
            sendDurationUpdate();
            // update location
            updateLocation();
            // training unit completed
            if (trainingDuration == 0) {
                // send a notification to user
                notificationManager.sendNotification(getString(R.string.training_unit_finished));
                // stop location tracking
                stopLocationUpdates();
                // pause the training, maybe there is another one unit
                isTrainingPaused = true;
            }
            Handler localHandler = TrainingService.this.handler;
            localHandler.postDelayed(this, TIME_INTERVAL_IN_MILLI);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeGPSLocationManager();
        logManager = AppLogManager.getInstance(this);
        trackManager = new TrackManager(this);
        notificationManager = new TrainingNotificationManager(this);
        handler = new Handler(Looper.getMainLooper());
        if (logManager.isDebugMode()) {
            logManager.log(TAG, "The training service was created.", null);
        }
    }

    @Override
    public void onDestroy() {
        if (logManager.isDebugMode()) {
            logManager.log(TAG, "The training service will be destroyed", null);
        }
        handler.removeCallbacks(secondsInTraining);
        handler = null;
        notificationManager = null;
        locationManager = null;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // created a foreground service with user notification
        // start service and send notification, if the app hidden
        // the drawable will be display in top
        startForeground(TrainingNotificationManager.SERVICE_NOTIFICATION_ID, notificationManager.getServiceNotification());
        return START_STICKY;
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
        if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            System.out.println("gps enabled");
        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
        if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            System.out.println("gps disabled");
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // insert location to local tracking database
        // TODO: callback, if location not added
        if (!trackManager.insertLocationForTrack(trackId, location)) {
            System.out.println("Location not added!");
        }
    }

    @Override
    // want to change in future releases
    // https://stackoverflow.com/questions/64638260/android-locationlistener-abstractmethoderror-on-onstatuschanged-and-onproviderd
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Nullable
    public Track.Id startTraining(long duration, boolean withLocationTracking) {
        if (isTrainingActive) {
            // ignore
            return trackId;
        }
        this.withLocationTracking = withLocationTracking;
        if (withLocationTracking) {
            trackId = trackManager.addNewTrack();
        }
        this.trainingDuration = duration;
        isTrainingPaused = false;
        isTrainingActive = true;
        handler.postDelayed(secondsInTraining, TIME_INTERVAL_IN_MILLI);
        if (logManager.isDebugMode()) {
            logManager.log(TAG, "The training with track id " + trackId + " was started.", null);
        }
        // track of current training
        return trackId;
    }

    public void pauseTraining() {
        handler.removeCallbacks(secondsInTraining);
        isTrainingPaused = true;
        // stop location tracking
        stopLocationUpdates();
        if (logManager.isDebugMode()) {
            logManager.log(TAG, "The training with track id " + trackId + " was paused.", null);
        }
    }

    public void resumeTraining(long durationInSeconds, Track.Id trackId) {
        if (isTrainingActive && !isTrainingPaused) {
            // ignore
            return;
        }
        if (this.trackId == null || trackId == null) {
            withLocationTracking = false;
        } else {
            withLocationTracking = this.trackId.equals(trackId);
        }
        if (durationInSeconds > TrainingServiceCallback.INVALID_TRAINING_DURATION) {
            // set the new duration (unit)
            trainingDuration = durationInSeconds;
        }
        isTrainingPaused = false;
        handler.postDelayed(secondsInTraining, TIME_INTERVAL_IN_MILLI);
        if (logManager.isDebugMode()) {
            logManager.log(TAG, "The training with track id " + trackId + " was resumed.", null);
        }
    }

    public void stopTraining() {
        handler.removeCallbacks(secondsInTraining);
        // stop location tracking
        stopLocationUpdates();
        trainingDuration = 0;
        isTrainingPaused = false;
        isTrainingActive = false;
        if (logManager.isDebugMode()) {
            logManager.log(TAG, "The training with track id " + trackId + " was stopped.", null);
        }
        // completed the recorded track
        // in further versions we qualify the location updates
        if (!trackManager.completeTrack(trackId)) {
            if (logManager.isDebugMode()) {
                logManager.log(TAG, "The track with id " + trackId + " couldn't completed.", null);
            }
        }
    }

    public void cancelTraining() {
        handler.removeCallbacks(secondsInTraining);
        // stop location tracking
        stopLocationUpdates();
        trainingDuration = 0;
        isTrainingPaused = false;
        isTrainingActive = false;
        if (logManager.isDebugMode()) {
            logManager.log(TAG, "The Training with track id " + trackId + " was canceled.", null);
        }
        if (!trackManager.removeTrack(trackId)) {
            if (logManager.isDebugMode()) {
                if (withLocationTracking) {
                    logManager.log(TAG, "The track with id " + trackId + " couldn't removed.", null);
                }
            }
        }
    }

    public boolean isTrainingActive() {
        return isTrainingActive;
    }

    @Nullable
    public Track getRecordedTrack(Track.Id trackId) {
        return trackManager.getTrack(trackId);
    }

    public long getTrainingDuration() {
        return trainingDuration;
    }

    public class BackgroundTimerServiceBinder extends Binder {

        public BackgroundTimerServiceBinder() {
            super();
        }

        public TrainingService getService() {
            return TrainingService.this;
        }

    }

    private void sendDurationUpdate() {
        Intent intent = new Intent(TrainingServiceCallback.SERVICE_RECEIVER_ACTION);
        intent.putExtra(TrainingServiceCallback.SERVICE_RECEIVER_INTENT_EXRAS_DURATION, trainingDuration);
        sendBroadcast(intent);
    }

    private void initializeGPSLocationManager() {
        // start location updates
        // get location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void updateLocation() {
        if (withLocationTracking) {
            if (locationManager != null && gpsAvailable) {
                startLocationUpdates();
            }
        }
    }

    private void startLocationUpdates() {
        if (withLocationTracking) {
            // locationServicesAllowed is true, if gps available
            // minimal time between update = 1 sec
            // minimal distance = 1 m
            try {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        TIME_INTERVAL_IN_MILLI,
                        LOCATION_UPDATE_MIN_DISTANCE,
                        this);
            } catch (SecurityException exception) {
                gpsAvailable = false;
                if (logManager.isDebugMode()) {
                    logManager.log(TAG, null, exception);
                }
            }
        }
    }

    private void stopLocationUpdates() {
        if (withLocationTracking) {
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
        }
    }

}
