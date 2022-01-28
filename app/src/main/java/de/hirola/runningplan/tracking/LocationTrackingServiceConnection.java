package de.hirola.runningplan.tracking;

import androidx.annotation.NonNull;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Communication with the location tracking service.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class LocationTrackingServiceConnection {

    private final LocationTrackingServiceConnection.CallBack callback;
    // communicate with this service
    private LocationTrackingService locationTrackingService;

    public LocationTrackingServiceConnection() {
        callback = null;
    }

    public LocationTrackingServiceConnection(@NonNull LocationTrackingServiceConnection.CallBack callback) {
        this.callback = callback;
    }

    public void connectAndStartService() {

    }

    public interface CallBack {
        void onConnectAndStarted(LocationTrackingService locationTrackingService);
    }
}
