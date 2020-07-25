package com.creator.qweekdots.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.creator.qweekdots.prefs.PreferenceManager;
import com.creator.qweekdots.volley.LruBitmapCache;

@SuppressLint("StaticFieldLeak")
public class AppController extends Application {

	public static final String TAG = AppController.class.getSimpleName();

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	LruBitmapCache mLruBitmapCache;

	private static Context mContext = null;
	private static AppController mInstance;

	private PreferenceManager pref;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		mContext = getApplicationContext();
	}

	public static Context getAppContext() {
		return mContext;
	}

	public static synchronized AppController getInstance() {
		return mInstance;
	}

	public PreferenceManager getPrefManager() {
		if (pref == null) {
			pref = new PreferenceManager(this);
		}

		return pref;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public ImageLoader getImageLoader() {
		getRequestQueue();
		if (mImageLoader == null) {
			getLruBitmapCache();
			mImageLoader = new ImageLoader(this.mRequestQueue, mLruBitmapCache);
		}

		return this.mImageLoader;
	}

	public void getLruBitmapCache() {
		if (mLruBitmapCache == null)
			mLruBitmapCache = new LruBitmapCache();
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}
}