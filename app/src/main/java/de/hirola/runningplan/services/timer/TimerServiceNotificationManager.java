package de.hirola.runningplan.services.timer;

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

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Manager to send notification from timer service.
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class TimerServiceNotificationManager {

    static final int NOTIFICATION_ID = 4711;
    private static final String CHANNEL_ID = TimerServiceNotificationManager.class.getSimpleName();
    private final NotificationCompat.Builder notificationBuilder;
    private final NotificationManager notificationManager;

    public TimerServiceNotificationManager(@NonNull Context context) {

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            notificationChannel.setAllowBubbles(true);
        }

        notificationManager.createNotificationChannel(notificationChannel);

        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationBuilder
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(context.getString(R.string.app_name))
                // tap to activity
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                //TODO: notification icon
                .setSmallIcon(R.drawable.baseline_home_black_20);
    }

    public void sendNotification(String contentText) {
        notificationBuilder.setContentText(contentText);
        notificationManager.notify(NOTIFICATION_ID, getNotification());
    }

    public Notification getNotification() {
        return notificationBuilder.build();
    }

}
