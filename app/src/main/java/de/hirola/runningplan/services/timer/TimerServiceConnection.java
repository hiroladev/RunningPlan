package de.hirola.runningplan.services.timer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.annotation.NonNull;
import de.hirola.runningplan.services.ServiceCallback;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Connection to the background service.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class TimerServiceConnection implements ServiceConnection {

    private boolean serviceBounded;
    private TimerService timerService;
    private final ServiceCallback callback;

    protected TimerServiceConnection() {
        serviceBounded = false;
        timerService = null;
        callback = null;
    }

    public TimerServiceConnection(@NonNull ServiceCallback callback) {
        this.callback = callback;
        serviceBounded = false;
        timerService = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        TimerService.BackgroundTimerServiceBinder serviceBinder =
                (TimerService.BackgroundTimerServiceBinder) service;
        timerService = serviceBinder.getService();
        if (timerService != null) {
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
        timerService = null;
    }

    public void bindAndStartService(@NonNull Context context) {
        if (timerService != null) {
            // Service is already started and bound.
            return;
        }
        // start the service
        context.startService(new Intent(context, TimerService.class));
        // bind service to the connection
        serviceBounded = context.bindService(new Intent(context, TimerService.class), this, 0);
    }

    public void stopAndUnbindService(@NonNull Context context) {
        try {
            context.unbindService(this);
            context.stopService(new Intent(context, TimerService.class));
        } catch (Exception exception) {
            //TODO: Logging
            exception.printStackTrace();
        } finally {
            serviceBounded = false;
        }
    }

    public void startTimer(long durationInSeconds) {
        if (serviceBounded) {
            timerService.startTimer(durationInSeconds);
        }
    }

    public void resumeTimer() {
        if (serviceBounded) {
            timerService.resumeTimer();
        }
    }

    public void pauseTimer() {
        if (serviceBounded) {
            timerService.pauseTimer();
        }
    }

    public void stopTimer() {
        if (serviceBounded) {
            timerService.stopTimer();
        }
    }

    /**
     * Returns the actual countdown time.
     *
     * @return the actual time for countdown in seconds or -1 if service not bound
     */
    public long getActualDuration() {
        if (serviceBounded) {
            return timerService.getDuration();
        }
        return -1;
    }
}
