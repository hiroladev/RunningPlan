package de.hirola.runningplan.services.timer;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import androidx.annotation.Nullable;
import de.hirola.runningplan.R;
import de.hirola.runningplan.services.ServiceCallback;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A background service to count the running time.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class TimerService extends Service {

    private final static long TIME_INTERVAL_IN_MILLI = 1000;

    private Handler handler = null;
    private TimerServiceNotificationManager notificationManager;
    private final IBinder serviceBinder = new BackgroundTimerServiceBinder();
    private long duration;
    private boolean isTimerRunning = false; // flag if the timer running
    private boolean isTimerPaused = false; // flag the state of recording track

    private final Runnable secondsInTraining = new Runnable() {
        @Override
        public void run() {
            if (isTimerPaused || duration == 0) {
                // training paused
                return;
            }
            // countdown
            duration = duration - 1;
            // send an update to the activity
            sendDurationUpdate();
            // countdown completed
            if (duration == 0) {
                // send a notification to user
                notificationManager.sendNotification(getString(R.string.training_unit_finished));
            }
            Handler localHandler = TimerService.this.handler;
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
        handler = new Handler(Looper.getMainLooper());
        notificationManager = new TimerServiceNotificationManager(this);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(secondsInTraining);
        notificationManager = null;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Starts a countdown of given duration.
     *
     * @param duration in seconds
     */
    public void startTimer(long duration) {
        if (isTimerRunning) {
            // ignore
            return;
        }
        this.duration = duration;
        isTimerPaused = false;
        isTimerRunning = true;
        handler.postDelayed(secondsInTraining, TIME_INTERVAL_IN_MILLI);
    }

    public void pauseTimer() {
        isTimerPaused = true;
    }

    public void resumeTimer() {
        if (!isTimerRunning && !isTimerPaused) {
            // ignore
            return;
        }
        handler.postDelayed(secondsInTraining, TIME_INTERVAL_IN_MILLI);
    }

    public void stopTimer() {
        isTimerPaused = true;
        isTimerRunning = false;
        duration = 0;
        handler.removeCallbacks(secondsInTraining);
    }

    public long getDuration() {
        return duration;
    }

    private void sendDurationUpdate() {
        Intent intent = new Intent(ServiceCallback.SERVICE_RECEIVER_ACTION);
        intent.putExtra(ServiceCallback.SERVICE_RECEIVER_INTENT_EXRAS_DURATION, duration);
        sendBroadcast(intent);
    }

    public class BackgroundTimerServiceBinder extends Binder {

        public BackgroundTimerServiceBinder() {
            super();
        }

        public TimerService getService() {
            return TimerService.this;
        }

    }

}
