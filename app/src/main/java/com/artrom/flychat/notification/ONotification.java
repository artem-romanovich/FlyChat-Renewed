package com.artrom.flychat.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class ONotification extends ContextWrapper {

    public static final String channel_id = "com.example.flychat.notification";
    public static final String channel_name = "flychat";

    private NotificationManager notificationManager;

    public ONotification(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                channel_id, channel_name, NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(false);
        channel.enableVibration(true);
        //channel.setLockscreenVisibility(4);
    }

    public NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getNotification(
            String title, String body, PendingIntent pendingIntent, Uri soundUri, String icon) {

        return new Notification.Builder(getApplicationContext(), channel_id)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setSmallIcon(Integer.parseInt(icon));
    }
}
