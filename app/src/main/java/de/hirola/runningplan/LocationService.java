package de.hirola.runningplan;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.sportslibrary.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A foreground service to get location updates for recording trainings.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class LocationService extends Service  implements LocationListener {

    // intent to pass data
    private Intent intent;
    //  location manager
    private LocationManager locationManager;
    // running track
    private List<Location> trackLocations;
    // distance
    private float runningDistance;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (trackLocations == null) {
            trackLocations = new ArrayList<>();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // stop location updates
        locationManager.removeUpdates(this);
        // put distance to intent
        intent.putExtra("runningDistance", runningDistance);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        this.intent = intent;
        // start location updates
        try {
            // get location manager
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            // locationServicesAllowed is true, if gps available
            // minimal time between update = 1 sec
            // minimal distance = 1 m
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        } catch (SecurityException exception) {
            if (Global.DEBUG) {
                //TODO: Logging
                exception.printStackTrace();
                return START_NOT_STICKY;
            }
        }
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
        // add location to list
        trackLocations.add(location);
        // calculate the distance
        int size = trackLocations.size();
        if (size > 2) {
            runningDistance = runningDistance + trackLocations.get(size - 2).distanceTo(trackLocations.get(size - 1));
        }
        System.out.println(Math.round(runningDistance));
    }

    @Override
    // want to change in future releases
    // https://stackoverflow.com/questions/64638260/android-locationlistener-abstractmethoderror-on-onstatuschanged-and-onproviderd
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public List<Location> getTrackLocations() {
        return trackLocations;
    }

}
