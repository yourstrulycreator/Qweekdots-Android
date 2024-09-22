package com.creator.qweekdots.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.creator.qweekdots.app.AppConfig;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class QweekdotsNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
    private Context context;

    public QweekdotsNotificationReceivedHandler(Context context) {
        this.context = context;
    }

    @SuppressLint("LogNotTimber")
    @Override
    public void notificationReceived(OSNotification notification) {
        JSONObject data = notification.payload.additionalData;
        String messageKey;

        if (data != null) {
            messageKey = data.optString("message", null);
            if (messageKey != null) {
                Log.i("OneSignalExample", "customkey set with value: " + messageKey);

                Intent pushNotification = new Intent(AppConfig.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", messageKey);
                LocalBroadcastManager.getInstance(context).sendBroadcast(pushNotification);
            }
        }
    }
}