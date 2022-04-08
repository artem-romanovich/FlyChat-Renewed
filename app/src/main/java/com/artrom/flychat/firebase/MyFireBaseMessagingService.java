package com.artrom.flychat.firebase;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.artrom.flychat.R;
import com.artrom.flychat.WrapActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String actionLiked = "liked";
    private static final int NOTIFICATION_ID = 1593;
    private final String GROUP_KEY = "GROUP_KEY_RANDOM_NAME";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        ArrayList<String> notificationString = new ArrayList<>();

        if (remoteMessage.getData().size() > 0) {

            Map<String, String> dataFromCloud = remoteMessage.getData();

            String action = dataFromCloud.get("action");
            String userName = dataFromCloud.get("userName");

            switch (action) {
                case actionLiked:
                    notificationString.add(action);
                    Intent intent = new Intent(this, WrapActivity.class);
                    String message = userName + " liked your photo.";
                    sendNotification(message, intent);
                    break;

                default:
                    break;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void sendNotification(String messageBody, Intent intent) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        Intent onCancelNotificationReceiver = new Intent(this, CancelNotificationReceiver.class);
        PendingIntent onCancelNotificationReceiverPendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0,
                onCancelNotificationReceiver, 0);

        String notificationHeader = this.getResources().getString(R.string.app_name);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        StatusBarNotification[] notifications = manager.getActiveNotifications();

        for (int i = 0; i < notifications.length; i++) {

            if (notifications[i].getPackageName().equals(getApplicationContext().getPackageName())) {

                Log.d("Notification", notifications[i].toString());

                Intent startNotificationActivity = new Intent(this, WrapActivity.class);

                startNotificationActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, startNotificationActivity,
                        PendingIntent.FLAG_ONE_SHOT);

                Notification notification = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(notificationHeader)
                        .setContentText("Tap to open")
                        .setAutoCancel(true)
                        .setStyle(getStyleForNotification(messageBody))
                        .setGroupSummary(true)
                        .setGroup(GROUP_KEY)
                        .setContentIntent(pendingIntent)
                        .setDeleteIntent(onCancelNotificationReceiverPendingIntent)
                        .build();

                SharedPreferences sharedPreferences = getSharedPreferences("NotificationData", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(String.valueOf(new Random(NOTIFICATION_ID)), messageBody);
                editor.apply();

                notificationManager.notify(NOTIFICATION_ID, notification);
                return;
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Notification notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(notificationHeader)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(onCancelNotificationReceiverPendingIntent)
                .build();

        SharedPreferences sharedPreferences = getSharedPreferences("NotificationData", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(String.valueOf(new Random(NOTIFICATION_ID)), messageBody);
        editor.apply();

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder);
    }

    private NotificationCompat.InboxStyle getStyleForNotification(String messageBody) {

        NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
        SharedPreferences sharedPref = getSharedPreferences("NotificationData", 0);
        Map<String, String> notificationMessages = (Map<String, String>) sharedPref.getAll();
        Map<String, String> myNewHashMap = new HashMap<>();

        for (Map.Entry<String, String> entry : notificationMessages.entrySet()) {
            myNewHashMap.put(entry.getKey(), entry.getValue());
        }

        inbox.addLine(messageBody);

        for (Map.Entry<String, String> message : myNewHashMap.entrySet()) {
            inbox.addLine(message.getValue());
        }

        inbox.setBigContentTitle(this.getResources().getString(R.string.app_name))
                .setSummaryText("Tap to open");
        return inbox;
    }
}
