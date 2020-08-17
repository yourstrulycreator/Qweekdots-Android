package com.creator.qweekdots.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private CircularProgressButton btnRegister;
    private TextInputEditText inputUserName, inputEmail, inputPassword, inputPasswordRepeat;
    private EmojiEditText inputFullName;
    private SessionManager session;
    private SQLiteHandler db;

    View decorView;

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiManager.install(new IosEmojiProvider());
        setContentView(R.layout.activity_register);
        changeStatusBarColor();

        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        Timber.plant(new Timber.DebugTree());

        inputUserName = findViewById(R.id.editTextUser);
        inputFullName = findViewById(R.id.editTextName);
        inputEmail = findViewById(R.id.editTextEmail);
        inputPassword = findViewById(R.id.editTextPassword);
        inputPasswordRepeat = findViewById(R.id.editTextPasswordRepeat);

        btnRegister = findViewById(R.id.cirRegisterButton);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(view -> {
            btnRegister.startAnimation();

            String username = Objects.requireNonNull(inputUserName.getText()).toString().trim();
            String fullname = Objects.requireNonNull(inputFullName.getText()).toString().trim();
            String email = Objects.requireNonNull(inputEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(inputPassword.getText()).toString().trim();
            String password_repeat = Objects.requireNonNull(inputPasswordRepeat.getText()).toString().trim();

            if (!username.isEmpty() && !fullname.isEmpty() && !email.isEmpty() && !password.isEmpty() && !password_repeat.isEmpty()) {
                if(validatePass(password, password_repeat)) {
                    registerUser(username, fullname, email, password);
                } else {
                    Toasty.info(getApplicationContext(),
                            "Passwords don\'t match", Toast.LENGTH_LONG)
                            .show();
                    stopButtonAnimation();
                }
            } else {
                Toasty.info(getApplicationContext(),
                        "Fill everything!", Toast.LENGTH_LONG)
                        .show();
                stopButtonAnimation();
            }
        });

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
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), REQUEST_ID_MULTIPLE_PERMISSIONS);
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
                if(perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                ) {
                    Timber.tag(TAG).d("All permissions granted");

                    // here you can do your logic all Permission Success Call
                    Intent intent = new Intent(RegisterActivity.this,
                            MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Timber.tag(TAG).d("Some permissions are not granted ask again ");
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
                .setMessage("Some Permissions are required for Open Camera")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private void explain() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RegisterActivity.this);
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
        Uri uri = Uri.fromParts("package", RegisterActivity.this.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String username, final String fullname, final String email,
                              final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, response -> {
            Timber.tag(TAG).d("Register Response: %s", response);

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            // User successfully stored in MySQL
                            stopButtonAnimation();
                            // Now store the user in sqlite
                            String id = jObj.getString("id");
                            String username1 = jObj.getString("username");
                            String fullname1 = jObj.getString("fullname");
                            String email1 = jObj.getString("email");
                            String bio = jObj.getString("bio");
                            String avatar = jObj.getString("avatar");
                            String telephone = jObj.getString("telephone");
                            String birthday = jObj.getString("birthday");
                            String created_at = jObj.getString("created_at");

                            // Inserting row in users table
                            db.addUser(username1, fullname1, email1, bio, avatar, telephone, birthday, created_at);
                            // storing user in shared preferences
                            User user = new User(id,
                                    username1,
                                    fullname1,
                                    email1,
                                    avatar);
                            AppController.getInstance().getPrefManager().storeUser(user);

                            // Create login session
                            session.setLogin(true);

                            Toasty.success(getApplicationContext(), "Blast Off!!", Toast.LENGTH_LONG).show();

                            // Launch main activity
                            if(checkAndRequestPermissions()){
                                //all permission granted
                                Intent intent = new Intent(RegisterActivity.this,
                                        MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                //require all permission.
                            }

                        } else {

                            // Error occurred in registration. Get the error
                            // message
                            String errorMsg = jObj.getString("error_msg");
                            Toasty.error(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                            stopButtonAnimation();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        stopButtonAnimation();
                    }

                }, error -> {
            Timber.tag(TAG).e("Registration Error: %s", error.getMessage());
            Toasty.error(getApplicationContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
            stopButtonAnimation();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("fullname", fullname);
                params.put("email", email);
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

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    public void onLoginClick(View view){
        startActivity(new Intent(this,LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);

    }

    private boolean validatePass(String password, String repeatPassword) {
        return password.length() > 0 && repeatPassword.equals(password);
    }


    private void stopButtonAnimation() {
        btnRegister.revertAnimation();
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
