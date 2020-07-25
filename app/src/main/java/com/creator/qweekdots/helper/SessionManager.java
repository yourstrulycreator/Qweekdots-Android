package com.creator.qweekdots.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import timber.log.Timber;

public class SessionManager {
	// LogCat tag
	private static String TAG = SessionManager.class.getSimpleName();

	// Shared Preferences
	private SharedPreferences pref;

	private Editor editor;

	// Shared preferences file name
	private static final String PREF_NAME = "QweekdotsLogin";
	
	private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

	@SuppressLint("CommitPrefEdits")
	public SessionManager(Context context) {
		// Shared pref mode
		int PRIVATE_MODE = 0;
		pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	public void setLogin(boolean isLoggedIn) {

		editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);

		// commit changes
		editor.commit();

		Timber.tag(TAG).d("User login session modified!");
	}
	
	public boolean isLoggedIn(){
		return pref.getBoolean(KEY_IS_LOGGED_IN, false);
	}
}
