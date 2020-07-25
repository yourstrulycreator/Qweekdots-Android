package com.creator.qweekdots.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.creator.qweekdots.R;
import com.creator.qweekdots.models.User;
import com.creator.qweekdots.service.FCMIntentService;
import com.creator.qweekdots.service.NotificationUtils;
import com.creator.qweekdots.ui.PasswordResetBottomSheet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.google.firebase.messaging.FirebaseMessaging;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private EditText inputUser;
    private EditText inputPassword;
    private SessionManager session;
    private SQLiteHandler db;
    private CircularProgressButton loginButton;

    View decorView;

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1234;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //for changing status bar icon colors
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_login);
        changeStatusBarColor();

        inputUser = findViewById(R.id.editTextUser);
        inputPassword = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.cirLoginButton);
        TextView resetPass = findViewById(R.id.resetPass);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        loginButton.setOnClickListener(view -> {
            loginButton.startAnimation();

            String username = inputUser.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            // Check for empty data in the form
            if (!username.isEmpty() && !password.isEmpty()) {
                // login user
                checkLogin(username, password);
            } else {
                // Prompt user to enter credentials
                Toasty.info(getApplicationContext(),
                        "No space left unexplored !", Toast.LENGTH_LONG)
                        .show();
                stopButtonAnimation();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (Objects.equals(intent.getAction(), AppConfig.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    String token = intent.getStringExtra("token");

                    Timber.tag(TAG).d("FCM token: %s", token);

                } else if (Objects.equals(intent.getAction(), AppConfig.SENT_TOKEN_TO_SERVER)) {
                    // gcm registration id is stored in our server's MySQL
                    Timber.tag(TAG).d("Token updated in server");
                } else if (intent.getAction().equals(AppConfig.PUSH_NOTIFICATION)) {
                    // new push notification is received
                }
            }
        };

        resetPass.setOnClickListener(v-> {
            PasswordResetBottomSheet bottomSheet = new PasswordResetBottomSheet();
            bottomSheet.show(getSupportFragmentManager(),bottomSheet.getTag());
        });

        if(checkPlayServices()) {

            registerGCM();

        }

        if(!checkCameraHardware(this.getApplicationContext())) {
            Toasty.info(getApplicationContext(), "You need a camera to use this application", Toast.LENGTH_LONG).show();
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
        // no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private boolean checkAndRequestPermissions() {
        int camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int phonestate = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int recordaudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (phonestate != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (location != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (recordaudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NotNull String[] permissions, @NotNull int[] grantResults) {

        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<>();

            perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

            // Fill with actual results from user
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for both permissions
                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG, "All permissions granted");

                    // here you can do your logic all Permission Success Call
                    Intent intent = new Intent(LoginActivity.this,
                            MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Log.d(TAG, "Some permissions are not granted ask again ");
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        showDialogOK(
                                (dialog, which) -> {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            checkAndRequestPermissions();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // proceed with logic by disabling the related features or quit the app.
                                            dialog.dismiss();
                                            break;
                                    }
                                });
                    } else {
                        explain();
                    }
                }
            }
        }

    }

    private void showDialogOK(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage("Some Permissions are required to Open Camera")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private void explain() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage("You need to give some mandatory permissions to continue. Do you want to go to app settings?");
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", LoginActivity.this.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    // starting the service to register with GCM
    private void registerGCM() {
        Intent intent = new Intent(this, FCMIntentService.class);
        intent.putExtra("key", "register");
        startService(intent);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Timber.tag(TAG).i("This device is not supported. Google Play Services not installed!");
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }


        public void onLoginClick(View View){
        startActivity(new Intent(this,RegisterActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.stay);

    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String username, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, response -> {
            Timber.tag(TAG).d("Login Response: %s", response);

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {
                            // Stop animation
                            stopButtonAnimation();

                            // user successfully logged in
                            // Now store the user in SQLite4

                            String id = jObj.getString("id");
                            String username1 = jObj.getString("username");
                            String fullname = jObj.getString("fullname");
                            String email = jObj.getString("email");
                            String bio = jObj.getString("bio");
                            String avatar = jObj.getString("avatar");
                            String telephone = jObj.getString("telephone");
                            String birthday = jObj.getString("birthday");
                            String created_at = jObj.getString("created_at");

                            // Inserting row in users table
                            db.addUser(username1, fullname, email, bio, avatar, telephone, birthday, created_at);
                            // storing user in shared preferences
                            User user = new User(id,
                                    username1,
                                    fullname,
                                    email,
                                    avatar);
                            AppController.getInstance().getPrefManager().storeUser(user);

                            // Create login session
                            session.setLogin(true);

                            // Launch main activity
                            if(checkAndRequestPermissions()){
                                //all permission granted
                                Intent intent = new Intent(LoginActivity.this,
                                        MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                //require all permission.
                            }
                        } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toasty.error(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                            stopButtonAnimation();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toasty.error(getApplicationContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                        stopButtonAnimation();
                    }

                }, error -> {
            Timber.tag(TAG).e("Login Error: %s", error.getMessage());
            Toasty.error(getApplicationContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();

            stopButtonAnimation();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);

                return params;
            }

        };

        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @SuppressLint("ResourceType")
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.login_bk_color));
        }
    }

    private void stopButtonAnimation(){
        loginButton.revertAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(AppConfig.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(AppConfig.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

}
