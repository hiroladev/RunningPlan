package de.hirola.runningplan.services.training;

import android.annotation.SuppressLint;
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
import de.hirola.runningplan.RunningPlanApplication;
import de.hirola.runningplan.util.TrainingNotificationManager;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.model.Track;
import org.tinylog.Logger;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A background service to count the running time.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class TrainingService extends Service implements LocationListener {

    public final static long LOCATION_UPDATE_INTERVAL_IN_MILLI = 2000; // location updates every 2 seconds

    private final static long TIME_INTERVAL_IN_MILLI = 1000; // timer interval (tick) in milliseconds
    private final static long LOCATION_UPDATE_MIN_DISTANCE = 2; // min distance for location updates in m

    private Handler handler;
    private SportsLibrary sportsLibrary;
    private PowerManager.WakeLock wakeLock; // partial wake lock for recording and time
    private TrainingNotificationManager notificationManager;
    private final IBinder serviceBinder = new BackgroundTimerServiceBinder();
    private TrackManager trackManager;
    private Track.Id trackId = null; // actual recording track
    private LocationManager locationManager; // location manager
    private int numberOfStabilizationSamples = 2; // ignore the first location updates
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

    @Override
    public void onCreate() {
        super.onCreate();
        initializeGPSLocationManager();
        sportsLibrary = ((RunningPlanApplication) getApplication()).getSportsLibrary();
        trackManager = new TrackManager(this);
        notificationManager = new TrainingNotificationManager(this);
        handler = new Handler(Looper.getMainLooper());
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"RunningPlan:wakelock");
        if (sportsLibrary.isDebugMode()) {
            Logger.debug("The training service was created.");
        }
    }

    @Override
    public void onDestroy() {
        if (sportsLibrary.isDebugMode()) {
            Logger.debug("The training service will be destroyed");
        }
        trackManager.end();
        handler.removeCallbacks(secondsInTraining);
        handler = null;
        notificationManager = null;
        locationManager = null;
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onDestroy();
    }

    @SuppressLint("WakelockTimeout")
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
        return serviceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // When you use a foreground service, you must display a notification
        // so that users are actively aware that the service is running.
        // This notification cannot be dismissed unless the service is either
        // stopped or removed from the foreground.
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
            gpsAvailable = true;
            if (sportsLibrary.isDebugMode()) {
                Logger.debug(provider + " enabled");
            }
        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
        if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            gpsAvailable = false;
            if (sportsLibrary.isDebugMode()) {
                Logger.debug(provider + " disabled");
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // ignore the first location updates
        if (numberOfStabilizationSamples == 0) {
            // insert location to local tracking database
            trackManager.insertLocationForTrack(trackId, location);
        } else {
            numberOfStabilizationSamples--;
        }
    }

    @SuppressWarnings("deprecation")
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
        if (sportsLibrary.isDebugMode()) {
            Logger.debug("The training with track id " + trackId + " was started.");
        }
        // track of current training
        return trackId;
    }

    public void pauseTraining() {
        handler.removeCallbacks(secondsInTraining);
        isTrainingPaused = true;
        // stop location tracking
        stopLocationUpdates();
        if (sportsLibrary.isDebugMode()) {
            Logger.debug("The training with track id " + trackId + " was paused.");
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
        if (sportsLibrary.isDebugMode()) {
            Logger.debug("The training with track id " + trackId + " was resumed.");
        }
    }

    public void stopTraining() {
        handler.removeCallbacks(secondsInTraining);
        // stop location tracking
        stopLocationUpdates();
        trainingDuration = 0;
        isTrainingPaused = false;
        isTrainingActive = false;
        if (sportsLibrary.isDebugMode()) {
            Logger.debug("The training with track id " + trackId + " was stopped.");
        }
        // completed the recorded track
        if (withLocationTracking) {
            if (!trackManager.completeTrack(trackId)) {
                if (sportsLibrary.isDebugMode()) {
                    Logger.debug("The track with id " + trackId + " couldn't completed.");
                }
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
        if (sportsLibrary.isDebugMode()) {
            Logger.debug("The Training with track id " + trackId + " was canceled.");
        }
        if (!trackManager.removeTrack(trackId)) {
            if (sportsLibrary.isDebugMode()) {
                if (withLocationTracking) {
                    Logger.debug("The track with id " + trackId + " couldn't removed.");
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
        intent.putExtra(TrainingServiceCallback.SERVICE_RECEIVER_INTENT_ACTUAL_DURATION, trainingDuration);
        sendBroadcast(intent);
    }

    private void initializeGPSLocationManager() {
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
            try {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_INTERVAL_IN_MILLI,
                        LOCATION_UPDATE_MIN_DISTANCE,
                        this);
            } catch (SecurityException exception) {
                gpsAvailable = false;
                if (sportsLibrary.isDebugMode()) {
                    Logger.debug("Could not determine the status of GPS.", exception);
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
