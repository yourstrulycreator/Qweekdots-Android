package com.creator.qweekdots.activity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.creator.qweekdots.BuildConfig;
import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.TabsAdapter;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.models.StreamUser;
import com.creator.qweekdots.models.User;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.service.QweekdotsNotificationOpenedHandler;
import com.creator.qweekdots.service.QweekdotsNotificationReceivedHandler;
import com.creator.qweekdots.ui.DropPostBottomSheet;
import com.creator.qweekdots.ui.DropTextBottomSheet;
import com.creator.qweekdots.ui.Feeds;
import com.creator.qweekdots.ui.NotificationsBottomSheet;
import com.creator.qweekdots.ui.QweekChat;
import com.creator.qweekdots.ui.SearchBottomSheet;
import com.creator.qweekdots.ui.Spaces;
import com.creator.qweekdots.ui.Spotlight;
import com.creator.qweekdots.ui.StreamChatMain;
import com.creator.qweekdots.utils.MenuItemBadge;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.getstream.chat.android.client.ChatClient;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private SQLiteHandler db;
    private SessionManager session;

    private static boolean activityStarted;

    int notificationCount, messageCount;

    String username, avatar;

    View decorView;

    private FloatingActionButton dropBtn;

    private AppUpdateManager mAppUpdateManager;
    private static final int RC_APP_UPDATE = 11;
    InstallStateUpdatedListener installStateUpdatedListener;

    String streamID = null;
    ChatClient client;

    //final String tutorialKey = "001";

    @SuppressLint({"ClickableViewAccessibility", "CheckResult"})
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Window View
        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        // Initialize Timber Debug Tree for Activity
        Timber.plant(new Timber.DebugTree());

        //Toolbar
        setupToolbar();

        // Logging set to help debug issues, remove before releasing your app.
        //OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationReceivedHandler(new QweekdotsNotificationReceivedHandler(getApplicationContext()))
                .setNotificationOpenedHandler(new QweekdotsNotificationOpenedHandler(getApplicationContext()))
                .init();

        //client = new ChatClient.Builder("{{ api_key }}", this).build();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String playerId = OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId();
                sendPlayerToServer(playerId);
            }
        }, 30000);

        if (activityStarted
                && getIntent() != null
                && (getIntent().getFlags() & Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) != 0) {
            finish();
            return;
        }

        // init layout
        dropBtn = findViewById(R.id.dropBtn);

        // Make Changes according to theme selected
        if(new DarkModePrefManager(this).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.contentBodyColor));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.getDecorView().getWindowInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        } else {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.contentBodyColor));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.getDecorView().getWindowInsetsController().setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }

        }

        Toasty.Config.getInstance()
                .setTextSize(12)
                .allowQueue(true)
                .apply();

        // session manager
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(this).getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");
        avatar = user.get("avatar");

        //checkStream(username);

        // Set ViewPager pages
        ViewPager viewPager = findViewById(R.id.id_viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = findViewById(R.id.id_tabs);
        tabLayout.setupWithViewPager(viewPager);
        //setCustomTabs();

        // Check for notifications
        checkNotificationStatus();
        //checkMessagesStatus();

        //Update State

        installStateUpdatedListener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADED){
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackbarForCompleteUpdate();
            } else if (state.installStatus() == InstallStatus.INSTALLED){
                if (mAppUpdateManager != null){
                    mAppUpdateManager.unregisterListener(installStateUpdatedListener);
                }

            } else {
                Log.i(TAG, "InstallStateUpdatedListener: state: " + state.installStatus());
            }
        };

        // Drop Factory Button
        dropBtn.setOnClickListener(v -> {
            DropTextBottomSheet bottomSheet = new DropTextBottomSheet(null, null, null);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

        //setAutoOrientationEnabled(this, false);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitleTextColor(getResources().getColor(R.color.QweekColorAccent));
        Drawable drawable = toolbar.getOverflowIcon();
        if(drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), getResources().getColor(R.color.contentTextColor));
            toolbar.setOverflowIcon(drawable);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        TabsAdapter adapter = new TabsAdapter(getSupportFragmentManager());
        //adapter.addFragment(new Spotlight(), "Spotlight");
        adapter.addFragment(new Feeds(), "Drops");
        adapter.addFragment(new QweekChat(), "Chat");
        adapter.addFragment(new Spaces(), "Spaces");
        adapter.addFragment(new Spotlight(), "Explore");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==1) {
                    dropBtn.setVisibility(View.GONE);
                } else {
                    dropBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    // Check status of unseen notifications
    private void checkNotificationStatus() {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/notification_status.php", response -> {
            Timber.tag(TAG).e("response: %s", response);

            try {
                JSONObject obj = new JSONObject(response);

                JSONArray statusArray = obj.getJSONArray("status");
                if(statusArray.length() < 1) {} else {
                    JSONObject statusObj = (JSONObject) statusArray.get(0);
                        int statusNum = statusObj.getInt("number");
                        Timber.d(String.valueOf(statusNum));

                        if(statusNum > 0) {
                            notificationCount = statusNum;
                            invalidateOptionsMenu();
                        } else {
                            invalidateOptionsMenu();
                        }
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
                params.put("u", username);

                Timber.tag(TAG).d("params: %s", params.toString());
                return params;
            }
        };

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    // Check status of unread messages
    /*
    private void checkMessagesStatus() {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/message_status.php", response -> {
            Timber.tag(TAG).e("response: %s", response);

            try {
                JSONObject obj = new JSONObject(response);

                JSONArray statusArray = obj.getJSONArray("status");
                if(statusArray.length() < 1) {} else {
                    JSONObject statusObj = (JSONObject) statusArray.get(0);
                    int statusNum = statusObj.getInt("number");
                    Timber.d(String.valueOf(statusNum));

                    if(statusNum > 0) {
                        messageCount = statusNum;
                        invalidateOptionsMenu();
                    } else {
                        invalidateOptionsMenu();
                    }
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
                params.put("u", username);

                Timber.tag(TAG).d("params: %s", params.toString());
                return params;
            }
        };

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }
     */

    // Get OpenSignal PlaerID and send to server
    private void sendPlayerToServer(final String token) {
        // checking for valid login session
        User user = AppController.getInstance().getPrefManager().getUser();
        if (user == null) {
            // TODO
            // user not found, redirecting him to login screen
            return;
        }

        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/parse/register_player.php", response -> Timber.tag(TAG).e("response: %s", response), error -> {
            NetworkResponse networkResponse = error.networkResponse;
            Timber.tag(TAG).e("Volley error: " + error.getMessage() + ", code: " + networkResponse);
            //Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("player", token);
                params.put("u", username);

                Timber.tag(TAG).d("params: %s", params.toString());
                return params;
            }
        };

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_three);
        View view = MenuItemCompat.getActionView(menuItem);

        CircleImageView avatar2 = view.findViewById(R.id.toolbar_avatar);

        RequestOptions requestOptions = new RequestOptions() // because file name is always same
                .format(DecodeFormat.PREFER_RGB_565);
        Drawable placeholder = getTinted(getResources().getColor(R.color.contentTextColor));
        Glide
                .with(getApplicationContext())
                .load(avatar)
                .override(30, 30)
                .placeholder(placeholder)
                .error(placeholder)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(requestOptions)
                .into(avatar2);

        avatar2.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ProfileActivity.class);
            i.putExtra("profile", username);
            startActivity(i);
            customType(MainActivity.this, "fadein-to-fadeout");
        });

        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.contentTextColor), PorterDuff.Mode.SRC_ATOP);
            }
        }

        // Notification Badges update Icon in Menu Options
        if(notificationCount > 0) {
            MenuItem menuItemNotification = menu.findItem(R.id.menu_two);
            MenuItemBadge.update(this, menuItemNotification, new MenuItemBadge.Builder()
                    .iconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_notification_md))
                    .iconTintColor(getResources().getColor(R.color.contentTextColor))
                    .textBackgroundColor(getResources().getColor(R.color.QweekColorAccent))
                    .textColor(getResources().getColor(R.color.contentBodyColor)));
            MenuItemBadge.getBadgeTextView(menuItemNotification).setBadgeCount(notificationCount);
        } else {
            MenuItem menuItemNotification = menu.findItem(R.id.menu_two);
            MenuItemBadge.update(this, menuItemNotification, new MenuItemBadge.Builder()
                    .iconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_notification_md))
                    .iconTintColor(getResources().getColor(R.color.contentTextColor))
                    .textBackgroundColor(getResources().getColor(R.color.QweekColorAccent))
                    .textColor(getResources().getColor(R.color.contentBodyColor)));
            MenuItemBadge.getBadgeTextView(menuItemNotification).setBadgeCount(0,true);
        }

        /*
        MenuItem menuItemSearch = menu.findItem(R.id.menu_one);
        MenuItemBadge.update(this, menuItemSearch, new MenuItemBadge.Builder()
                .iconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_search_white_24dp))
                .iconTintColor(getResources().getColor(R.color.contentTextColor))
                .textBackgroundColor(getResources().getColor(R.color.QweekColorAccent))
                .textColor(getResources().getColor(R.color.contentBodyColor)));
        MenuItemBadge.getBadgeTextView(menuItemSearch).setBadgeCount(0, true);
         */

        /*
        if(messageCount > 0) {
            MenuItem menuItemMessage = menu.findItem(R.id.menu_two);
            MenuItemBadge.update(this, menuItemMessage, new MenuItemBadge.Builder()
                    .iconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_email_md))
                    .iconTintColor(getResources().getColor(R.color.contentTextColor))
                    .textBackgroundColor(getResources().getColor(R.color.QweekColorAccent))
                    .textColor(getResources().getColor(R.color.contentBodyColor)));
            MenuItemBadge.getBadgeTextView(menuItemMessage).setBadgeCount(messageCount);
        } else {
            MenuItem menuItemMessage = menu.findItem(R.id.menu_two);
            MenuItemBadge.update(this, menuItemMessage, new MenuItemBadge.Builder()
                    .iconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_email_md))
                    .iconTintColor(getResources().getColor(R.color.contentTextColor))
                    .textBackgroundColor(getResources().getColor(R.color.QweekColorAccent))
                    .textColor(getResources().getColor(R.color.contentBodyColor)));
            MenuItemBadge.getBadgeTextView(menuItemMessage).setBadgeCount(0, true);
        }
         */

        return super.onCreateOptionsMenu(menu);
    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.main_layout),
                        "New app is ready!",
                        Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("Install", view -> {
            if (mAppUpdateManager != null){
                mAppUpdateManager.completeUpdate();
            }
        });


        snackbar.setActionTextColor(getResources().getColor(R.color.contentTextColor));
        snackbar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            //case R.id.menu_one:
                //Go to ChatActivity
                //Intent i = new Intent(MainActivity.this, ChatActivity.class);
                //startActivity(i);
                //customType(MainActivity.this, "bottom-to-up");
              //  SearchBottomSheet searchSheet = new SearchBottomSheet(getApplicationContext(), username);
              //  searchSheet.show(getSupportFragmentManager(), searchSheet.getTag());
              //  break;

            case R.id.menu_two:
                // Go to NotificationsBottomSheet
                NotificationsBottomSheet bottomSheet = new NotificationsBottomSheet(getApplicationContext(), username);
                bottomSheet.show(getSupportFragmentManager(),bottomSheet.getTag());
                break;

            case R.id.menu_three:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * function to get stream token
     * */
    private void checkStream(final String username) {
        // Tag used to cancel the request
        String tag_string_req = "req_stream";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_STREAM, response -> {
            Timber.tag(TAG).d("Stream Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {

                    streamID = jObj.getString("stream_token");
                    // storing user in shared preferences
                    StreamUser suser = new StreamUser(streamID);
                    AppController.getInstance().getPrefManager().storeStreamUser(suser);
                } else {
                    // Error in login. Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Toasty.error(getApplicationContext(),
                            errorMsg, Toast.LENGTH_LONG).show();
                    //stopButtonAnimation();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(getApplicationContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                //stopButtonAnimation();
            }

        }, error -> {
            Timber.tag(TAG).e("Stream Error: %s", error.getMessage());
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("username", username);

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

        //return streamID;
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        AppController.getInstance().getPrefManager().clear();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        Handler handler = new Handler();
        int delay = 30000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                checkNotificationStatus();
                //checkMessagesStatus();
                handler.postDelayed(this, delay);
            }
        }, delay);

        NotificationManager manager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
        OneSignal.clearOneSignalNotifications();
    }

    @Override
    public void onStart() {
        mAppUpdateManager = AppUpdateManagerFactory.create(this);

        mAppUpdateManager.registerListener(installStateUpdatedListener);

        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/)){

                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/, MainActivity.this, RC_APP_UPDATE);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED){
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackbarForCompleteUpdate();
            } else {
                Log.e(TAG, "checkForAppUpdateAvailability: something else");
            }
        });

        super.onStart();
    }

    @Override
    public void onStop() {
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }

        super.onStop();
    }

    @Override
    public void onPause() {
        Glide.get(getApplicationContext()).clearMemory();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Glide.get(getApplicationContext()).clearMemory();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Glide.get(getApplicationContext()).clearMemory();
        finish();
    }

    public void setDb(SQLiteHandler db) {
        this.db = db;
    }

    private @Nullable
    Drawable getTinted(@ColorInt int color) {
        // need to mutate otherwise all references to this drawable will be tinted
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_alien).mutate();
        return tint(drawable, ColorStateList.valueOf(color));
    }

    public static Drawable tint(Drawable input, ColorStateList tint) {
        if (input == null) {
            return null;
        }
        Drawable wrappedDrawable = DrawableCompat.wrap(input);
        DrawableCompat.setTintList(wrappedDrawable, tint);
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.MULTIPLY);
        return wrappedDrawable;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "onActivityResult: app download failed");
            }
        }
    }

    public static void setAutoOrientationEnabled(Context context, boolean enabled)
    {
        Settings.System.putInt( context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
    }

    /*
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

     */
}

