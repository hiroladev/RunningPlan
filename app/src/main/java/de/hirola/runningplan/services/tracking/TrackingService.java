package de.hirola.runningplan.services.tracking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.model.Track;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A background service to get location updates for recording trainings.
 * The tracks are saved in a local datastore.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class TrackingService extends Service implements LocationListener {

    private final static long LOCATION_UPDATE_MIN_TIME_INTERVAL = 1000;
    private final static float LOCATION_UPDATE_MIN_DISTANCE = 1;

    private Handler handler = null;
    private final IBinder serviceBinder = new LocationTrackingServiceBinder();
    private TrackManager trackManager;
    private Track.Id trackId = null; // actual recording track
    private LocationManager locationManager; // location manager
    private boolean isRecording; // flag the state of recording track
    private boolean recordingPaused;
    private boolean gpsAvailable; // (gps) tracking available?

    private final Runnable locationData = new Runnable() {
        @Override
        public void run() {
            if (!isRecording && recordingPaused) {
                // no recording
                return;
            }
            updateLocation();

            Handler localHandler = TrackingService.this.handler;
            localHandler.postDelayed(this, LOCATION_UPDATE_MIN_TIME_INTERVAL);
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
        handler = new Handler(Looper.getMainLooper());
        isRecording = false;
        recordingPaused = true;
        initializeGPSLocationManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // stop location updates
        handler.removeCallbacks(locationData);
        locationManager.removeUpdates(this);
        locationManager = null;
        // remove the recorded track
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        if (trackManager.insertLocationForTrack(trackId, location)) {
            //Todo: Broadcast
            System.out.println("Location not added!");
        }
    }

    @Override
    // want to change in future releases
    // https://stackoverflow.com/questions/64638260/android-locationlistener-abstractmethoderror-on-onstatuschanged-and-onproviderd
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void loadTrackManager(Context context) {
        trackManager = new TrackManager(context);
    }

    public Track.Id startTrackRecording() {
        if (isRecording) {
            if (Global.DEBUG) {
                //TODO: Logging
            }
            return trackId;
        }
        // create a new track and start recording
        // we needed name, description, start time
        Track track = new Track("Running unit");
        trackId = trackManager.addTrack(track);
        // get new location data
        handler.postDelayed(locationData, LOCATION_UPDATE_MIN_TIME_INTERVAL);
        isRecording = true;
        recordingPaused = false;
        return trackId;
    }

    public void pauseTrackRecording() {
        recordingPaused = true;
        stopLocationUpdates();
    }

    public void resumeTrackRecording(Track.Id id) {

    }

    public void stopTrackRecording() {
        isRecording = false;
        recordingPaused = false;
        stopLocationUpdates();
        // completed the recorded track
        // in further versions we qualify the location updates
        trackManager.completeTrack(trackId);
    }

    public void stopAndRemoveTrackRecording() {
        isRecording = false;
        recordingPaused = false;
        stopLocationUpdates();
        trackManager.removeTrack(trackId);
    }

    @Nullable
    public Track getRecordedTrack(Track.Id trackId) {
        return trackManager.getTrack(trackId);
    }

    public class LocationTrackingServiceBinder extends Binder {

        public LocationTrackingServiceBinder() {
            super();
        }

        public TrackingService getService() {
            return TrackingService.this;
        }

    }

    private void initializeGPSLocationManager() {
        // start location updates
        // get location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void updateLocation() {
        if (locationManager != null && gpsAvailable) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        // locationServicesAllowed is true, if gps available
        // minimal time between update = 1 sec
        // minimal distance = 1 m
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME_INTERVAL,
                    LOCATION_UPDATE_MIN_DISTANCE,
                    this);
        } catch (SecurityException exception) {
            gpsAvailable = false;
            if (Global.DEBUG) {
                //TODO: logging
                exception.printStackTrace();
            }
        }
    }

    private void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
