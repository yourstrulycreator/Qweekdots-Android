/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 * */
package com.creator.qweekdots.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

import timber.log.Timber;

public class SQLiteHandler extends SQLiteOpenHelper {

	private static final String TAG = SQLiteHandler.class.getSimpleName();

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "qweeksocio";

	// Login table name
	private static final String TABLE_USER = "user";

	// Login Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_USER = "username";
	private static final String KEY_NAME = "fullname";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_BIO = "bio";
	private static final String KEY_PROFILE_PIC = "avatar";
	private static final String KEY_TELEPHONE = "telephone";
	private static final String KEY_BIRTHDAY = "birthday";
	private static final String KEY_CREATED_AT = "created_at";

	public SQLiteHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_USER + " TEXT," + KEY_NAME + " TEXT,"
				+ KEY_EMAIL + " TEXT UNIQUE," + KEY_BIO + " TEXT," + KEY_PROFILE_PIC + " TEXT,"
				+ KEY_TELEPHONE + " TEXT," + KEY_BIRTHDAY + " TEXT," + KEY_CREATED_AT + " TEXT" + ")";
		db.execSQL(CREATE_LOGIN_TABLE);

		Timber.tag(TAG).d("Database tables created");
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

		// Create tables again
		onCreate(db);
	}

	/**
	 * Storing user details in database
	 * */
	public void addUser(String username, String fullname, String email, String bio, String avatar, String telephone, String birthday, String created_at) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_USER, username); // Username
		values.put(KEY_NAME, fullname);
		values.put(KEY_EMAIL, email); // Email
		values.put(KEY_BIO, bio); // Bio
		values.put(KEY_PROFILE_PIC, avatar); // Profile Picture
		values.put(KEY_TELEPHONE, telephone); // Telephone
		values.put(KEY_BIRTHDAY, birthday); // Birthday
		values.put(KEY_CREATED_AT, created_at); // Created At

		// Inserting Row
		long id = db.insert(TABLE_USER, null, values);
		db.close(); // Closing database connection

		Timber.tag(TAG).d("New user inserted into sqlite: %s", id);
	}

	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_USER;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put("username", cursor.getString(1));
			user.put("fullname", cursor.getString(2));
			user.put("email", cursor.getString(3));
			user.put("bio", cursor.getString(4));
			user.put("avatar", cursor.getString(5));
			user.put("telephone", cursor.getString(6));
			user.put("birthday", cursor.getString(7));
			user.put("created_at", cursor.getString(8));
		}
		cursor.close();
		db.close();
		// return user
		Timber.tag(TAG).d("Fetching user from Sqlite: %s", user.toString());

		return user;
	}

	/**
	 * Re crate database Delete all tables and create them again
	 * */
	public void deleteUsers() {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_USER, null, null);
		db.close();

		Timber.tag(TAG).d("Deleted all user info from sqlite");
	}

}
