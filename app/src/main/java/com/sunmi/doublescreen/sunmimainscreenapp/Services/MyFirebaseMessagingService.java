package com.sunmi.doublescreen.sunmimainscreenapp.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Date;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private NotificationManager mNM;
    private String message;

    private String TAG="firebase_message";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG,"onMessageReceived:"+remoteMessage);

        if (remoteMessage == null)
            return;


            message=remoteMessage.getNotification().getBody();
            Intent pushNotification = new Intent("Parameter.FCM_PUSH_NOTIFICATION");
            pushNotification.putExtra("fcmData", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
            Log.d(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());


//        showNotification(remoteMessage.getNotification().getBody());
    }




}
