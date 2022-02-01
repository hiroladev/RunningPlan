package de.hirola.runningplan.services.tracking;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hirola.runningplan.services.ServiceCallback;
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
public class TrackingServiceConnection implements ServiceConnection {

    private boolean serviceBounded;
    private TrackingService trackingService;
    private final ServiceCallback callback;
    private Context context;

    protected TrackingServiceConnection() {
        serviceBounded = false;
        trackingService = null;
        callback = null;
        context = null;
    }

    public TrackingServiceConnection(@NonNull ServiceCallback callback) {
        this.callback = callback;
        serviceBounded = false;
        trackingService = null;
        context = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        TrackingService.LocationTrackingServiceBinder serviceBinder =
                (TrackingService.LocationTrackingServiceBinder) service;
        trackingService = serviceBinder.getService();
        if (trackingService != null) {
            trackingService.loadTrackManager(context);
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
        trackingService = null;
    }

    public void bindAndStartService(@NonNull Context context) {
        if (trackingService != null) {
            // Service is already started and bound.
            return;
        }
        // start the service
        context.startService(new Intent(context, TrackingService.class));
        // bind service to the connection
        serviceBounded = context.bindService(new Intent(context, TrackingService.class), this, 0);
        this.context = context;
    }

    public void stopAndUnbindService(@NonNull Context context) {
        try {
            context.unbindService(this);
            context.stopService(new Intent(context, TrackingService.class));
        } catch (Exception exception) {
            //TODO: Logging
            exception.printStackTrace();
        } finally {
            serviceBounded = false;
        }
    }

    @Nullable
    public Track.Id startTrackRecording(){
        if (serviceBounded) {
            return trackingService.startTrackRecording();
        }
        return null;
    }

    public void resumeTrackRecording(Track.Id trackId){
        if (serviceBounded) {
            trackingService.resumeTrackRecording(trackId);
        }
    }

    public void pauseTrackRecording(){
        if (serviceBounded) {
            trackingService.pauseTrackRecording();
        }
    }

    public void stopTrackRecording() {
        if (serviceBounded) {
            trackingService.stopTrackRecording();
        }
    }

    public void stopAndRemoveTrackRecording() {
        if (serviceBounded) {
            trackingService.stopAndRemoveTrackRecording();
        }
    }

    @Nullable
    public Track getRecordedTrack(Track.Id trackId) {
        if (serviceBounded) {
            return trackingService.getRecordedTrack(trackId);
        }
        return null;
    }
}
