package com.creator.qweekdots.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.creator.qweekdots.models.User;

public class PreferenceManager {
    private String TAG = PreferenceManager.class.getSimpleName();

    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private SharedPreferences.Editor editor;

    // Sharedpref file name
    private static final String PREF_NAME = "qweekdots_chat";

    // All Shared Preferences Keys
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_NAME = "username";
    private static final String KEY_USER_FULL = "fullname";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_AVATAR = "avatar";
    private static final String KEY_NOTIFICATIONS = "notifications";

    // Constructor
    @SuppressLint("CommitPrefEdits")
    public PreferenceManager(Context context) {
        // Context
        // Shared pref mode
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    @SuppressLint("LogNotTimber")
    public void storeUser(User user) {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getUserName());
        editor.putString(KEY_USER_FULL, user.getFullName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_AVATAR, user.getAvatar());
        editor.commit();

        Log.e(TAG, "User is stored in shared preferences. " + user.getUserName() + ", " + user.getEmail());
    }

    public User getUser() {
        if (pref.getString(KEY_USER_ID, null) != null) {
            String id, username, fullname, email, avatar;
            id = pref.getString(KEY_USER_ID, null);
            username = pref.getString(KEY_USER_NAME, null);
            fullname = pref.getString(KEY_USER_FULL, null);
            email = pref.getString(KEY_USER_EMAIL, null);
            avatar = pref.getString(KEY_USER_AVATAR, null);

            return new User(id, username, fullname, email, avatar);
        }
        return null;
    }

    public void addNotification(String notification) {

        // get old notifications
        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
}
