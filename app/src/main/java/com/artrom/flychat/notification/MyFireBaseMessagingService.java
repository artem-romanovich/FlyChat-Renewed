/*
package com.artrom.flychat.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.artrom.flychat.MySystem.MyLog;
import static com.artrom.flychat.MySystem.file_nick;
import static com.artrom.flychat.MySystem.readFile;

public class MyFireBaseMessagingService extends FirebaseMessagingService {
    String title, message;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("Sented");

        if (readFile(MyFireBaseMessagingService.this, file_nick).equals(sented)){
            sendNotification(remoteMessage);
        }

        title = remoteMessage.getData().get("Title");
        message = remoteMessage.getData().get("Message");

        MyLog(title + " " + message);

        */
/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendONotification(remoteMessage);
        } else {
        }*//*

    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String User = remoteMessage.getData().get("User");
        String Icon = remoteMessage.getData().get("Icon");
        String Title = remoteMessage.getData().get("Title");
        String Body = remoteMessage.getData().get("Body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(User.replaceAll("[\\D]]", ""));
        Intent intent = new Intent(this, TestActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", User);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentTitle(Title)
                .setContentText(Body)
                .setSound(defaultSound)
                .setAutoCancel(true)
                .setSmallIcon(Integer.parseInt(Icon));
        NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0){
            i = j;
        }

        noti.notify(i, builder.build());
    }
}
*/
