package com.creator.qweekdots.utils;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Utils {

    public static void savePreferences(Activity activity, String key1,
                                       String value1, String key2, String value2) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.putString(key1, value1);
        editor.putString(key2, value2);
        editor.commit();
    }

    public static String readPreferences(Activity activity, String key,
                                         String defaultValue) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(activity.getApplicationContext());
        return sp.getString(key, defaultValue);
    }
}
