package de.hirola.runningplan.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import de.hirola.runningplan.MainActivity;
import de.hirola.runningplan.R;
import de.hirola.runningplan.services.training.TrainingService;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Manager to send notification from timer service.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public class TrainingNotificationManager {

    public static final int SERVICE_NOTIFICATION_ID = 4711;

    private static final int NOTIFICATION_ID = 4712;
    private static final String CHANNEL_ID = TrainingNotificationManager.class.getSimpleName();
    private static final String SERVICE_CHANNEL_ID = TrainingService.class.getSimpleName();

    private NotificationCompat.Builder notificationBuilder;
    private NotificationCompat.Builder serviceNotificationBuilder;
    private final NotificationManager notificationManager;

    public TrainingNotificationManager(@NonNull Context context) {

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // app crashs on API 31
        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingFlags);

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel serviceNotificationChannel = new NotificationChannel(SERVICE_CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            notificationChannel.setAllowBubbles(true);
        }

        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.createNotificationChannel(serviceNotificationChannel);

        makeNotificationBuilder(context, pendingIntent);
        makeServiceNotificationBuilder(context, pendingIntent);

    }

    public Notification getServiceNotification() {
        return serviceNotificationBuilder.build();
    }

    public void sendNotification(String contentText) {
        notificationBuilder.setContentText(contentText);
        notificationManager.notify(NOTIFICATION_ID, getNotification());
    }

    private void makeNotificationBuilder(Context context, PendingIntent pendingIntent) {
        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationBuilder
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(false)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.baseline_directions_run_black_18);
    }

    private void makeServiceNotificationBuilder(Context context, PendingIntent pendingIntent) {
        serviceNotificationBuilder = new NotificationCompat.Builder(context, SERVICE_CHANNEL_ID);
        serviceNotificationBuilder
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getText(R.string.start_training_service))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.baseline_directions_run_black_18)
                .setTicker(context.getText(R.string.training_service_ticker_text));
    }

    private Notification getNotification() {
        return notificationBuilder.build();
    }

}
