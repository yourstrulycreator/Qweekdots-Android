package com.creator.qweekdots.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.app.EndPoints;
import com.creator.qweekdots.models.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

public class FCMIntentService extends IntentService {

    private static final String TAG = FCMIntentService.class.getSimpleName();

    public FCMIntentService() {
        super(TAG);
    }

    public static final String KEY = "key";
    public static final String TOPIC = "topic";
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";


    @Override
    protected void onHandleIntent(Intent intent) {
        // Timber Debug Tree
        Timber.plant(new Timber.DebugTree());

        String key = intent.getStringExtra(KEY);
        assert key != null;
        switch (key) {
            case SUBSCRIBE:
                // subscribe to a topic
                String topic = intent.getStringExtra(TOPIC);
                subscribeToTopic(topic);
                break;
            case UNSUBSCRIBE:
                String topic1 = intent.getStringExtra(TOPIC);
                unsubscribeFromTopic(topic1);
                break;
            default:
                // if key is not specified, register with GCM
                registerGCM();
        }

    }

    /**
     * Registering with GCM and obtaining the gcm registration id
     */
    private void registerGCM() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Timber.w(task.getException(), "getInstanceId failed");
                        sharedPreferences.edit().putBoolean(AppConfig.SENT_TOKEN_TO_SERVER, false).apply();
                        return;
                    }

                    // Get new Instance ID token
                    String token = Objects.requireNonNull(task.getResult()).getToken();

                    // Log and toast
                    Timber.tag(TAG).d("Registration Token: %s", token);

                    // sending the registration id to our server
                    sendRegistrationToServer(token);

                    sharedPreferences.edit().putBoolean(AppConfig.SENT_TOKEN_TO_SERVER, true).apply();
                    //Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    //

                    // Notify UI that registration has completed, so the progress indicator can be hidden.
                    Intent registrationComplete = new Intent(AppConfig.REGISTRATION_COMPLETE);
                    registrationComplete.putExtra("token", token);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
                });
    }

    private void sendRegistrationToServer(final String token) {

        // checking for valid login session
        User user = AppController.getInstance().getPrefManager().getUser();
        if (user == null) {
            // TODO
            // user not found, redirecting him to login screen
            return;
        }

        String endPoint = EndPoints.USER.replace("_ID_", user.getId());

        Timber.tag(TAG).i("endpoint: %s", endPoint);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                endPoint, response -> {
            Timber.tag(TAG).e("response: %s", response);

            try {
                JSONObject obj = new JSONObject(response);

                // check for error
                if (!obj.getBoolean("error")) {
                    // broadcasting token sent to server
                    Timber.tag(TAG).d("FCM ID successfully updated server side");
                } else {
                    //Toast.makeText(getApplicationContext(), "Unable to update FCM to our sever. " + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    Timber.tag(TAG).d("Unable to update FCM Id to server");
                }

            } catch (JSONException e) {
                Timber.tag(TAG).e("json parsing error: %s", e.getMessage());
                //Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            NetworkResponse networkResponse = error.networkResponse;
            Timber.tag(TAG).e("Volley error: " + error.getMessage() + ", code: " + networkResponse);
            //Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fcm_id", token);

                Timber.tag(TAG).d("params: %s", params.toString());
                return params;
            }
        };

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    /**
     * Subscribe to a topic
     */
    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnSuccessListener(aVoid -> {
            //Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
            Timber.tag(TAG).d("Subscribed to topic: %s", topic);
        });
    }

    public void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnSuccessListener(aVoid -> {
            //Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
            Timber.tag(TAG).d("Unsubscribed from topic: %s", topic);
        });
    }
}
