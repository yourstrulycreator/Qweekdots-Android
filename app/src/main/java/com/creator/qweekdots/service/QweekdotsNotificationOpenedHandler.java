package com.creator.qweekdots.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.creator.qweekdots.activity.ChatActivity;
import com.creator.qweekdots.activity.ProfileActivity;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class QweekdotsNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {

    private Context context;

    public QweekdotsNotificationOpenedHandler(Context context) {
        this.context = context;
    }

    // This fires when a notification is opened by tapping on it.
    @SuppressLint("LogNotTimber")
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;
        String messageKey, followKey;

        if (data != null) {
            messageKey = data.optString("message", null);
            if (messageKey != null) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            followKey = data.optString("follow", null);
            if( followKey != null) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("profile", followKey);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

        if (actionType == OSNotificationAction.ActionType.ActionTaken)
            Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);
    }
}
