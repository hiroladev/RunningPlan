package de.hirola.runningplan.tracking;

import android.content.Context;
import android.content.Intent;
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

    public void connectAndStartService(@NonNull Context context) {
        if (locationTrackingService != null) {
            // service already connected
            return;
        }
        // start the service
        context.startService(new Intent(context, LocationTrackingService.class));

    }

    public interface CallBack {
        void onConnectAndStarted(LocationTrackingService locationTrackingService);
    }
}
