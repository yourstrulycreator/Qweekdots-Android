package com.creator.qweekdots.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.TabsAdapter;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.ui.DropSpaceBottomSheet;
import com.creator.qweekdots.ui.DropTextBottomSheet;
import com.creator.qweekdots.ui.NotificationsBottomSheet;
import com.creator.qweekdots.ui.SearchBottomSheet;
import com.creator.qweekdots.ui.SpacesInfo;
import com.creator.qweekdots.ui.SpacesThread;
import com.creator.qweekdots.utils.MenuItemBadge;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class SpaceActivity extends AppCompatActivity {
    private String TAG = SpaceActivity.class.getSimpleName();
    private SQLiteHandler db;
    private SessionManager session;

    private FloatingActionButton dropBtn;

    String chatRoomId, title;
    int notificationCount, messageCount;
    String username, avatar, selfUserId;

    View decorView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space);

        Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        title = intent.getStringExtra("name");

        // Window View
        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        // Initialize Timber Debug Tree for Activity
        Timber.plant(new Timber.DebugTree());

        if(new DarkModePrefManager(this).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(getResources().getColor(R.color.contentBodyColor));
            }
        }

        db = new SQLiteHandler(Objects.requireNonNull(getApplication()));
        // session manager

        // Fetching user details from SQLite
        HashMap<String, String> userData = db.getUserDetails();

        username = userData.get("username");
        avatar = userData.get("avatar");
        selfUserId = AppController.getInstance().getPrefManager().getUser().getId();

        ViewPager viewPager = findViewById(R.id.id_viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = findViewById(R.id.id_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Check for notifications
        //checkNotificationStatus();
        //checkMessagesStatus();

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        //setupToolbar();

        dropBtn = findViewById(R.id.dropBtn);
        // Drop Factory Button
        dropBtn.setOnClickListener(v -> {
            DropTextBottomSheet bottomSheet = new DropTextBottomSheet(null, null, chatRoomId);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

        //setAutoOrientationEnabled(this, false);

    }

    @SuppressLint("SetTextI18n")
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        //toolbarTitle.setText("S/"+title);
        toolbar.setTitleTextColor(getResources().getColor(R.color.QweekColorAccent));
        final Drawable upArrow = getResources().getDrawable(R.drawable.tiny_ic_back);
        upArrow.setColorFilter(getResources().getColor(R.color.contentTextColor), PorterDuff.Mode.SRC_ATOP);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        Drawable drawable = toolbar.getOverflowIcon();
        if(drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), getResources().getColor(R.color.contentTextColor));
            toolbar.setOverflowIcon(drawable);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        TabsAdapter adapter = new TabsAdapter(getSupportFragmentManager());

        Bundle myBundle = new Bundle();
        myBundle.putString("username", username);
        myBundle.putString("chat_room_id", chatRoomId);
        myBundle.putString("title", title);
        myBundle.putString("self_user_id", selfUserId);

        SpacesThread spaces_thread_frag = new SpacesThread();
        SpacesInfo spaces_info_frag = new SpacesInfo();

        spaces_thread_frag.setArguments(myBundle);
        spaces_info_frag.setArguments(myBundle);

        adapter.addFragment(spaces_thread_frag, "Space");
        adapter.addFragment(spaces_info_frag, "Explore");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position!=0) {
                    dropBtn.setVisibility(View.GONE);
                } else if(position==0) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
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

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        AppController.getInstance().getPrefManager().clear();

        // Launching the login activity
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
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
            Intent i = new Intent(SpaceActivity.this, ProfileActivity.class);
            i.putExtra("profile", username);
            startActivity(i);
            customType(SpaceActivity.this, "fadein-to-fadeout");
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
        MenuItem menuItemNotification = menu.findItem(R.id.menu_one);
        MenuItemBadge.update(this, menuItemNotification, new MenuItemBadge.Builder()
                .iconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_search_white_24dp))
                .iconTintColor(getResources().getColor(R.color.contentTextColor))
                .textBackgroundColor(getResources().getColor(R.color.QweekColorAccent))
                .textColor(getResources().getColor(R.color.contentBodyColor)));
        MenuItemBadge.getBadgeTextView(menuItemNotification).setBadgeCount(0, true);

         */

        return super.onCreateOptionsMenu(menu);
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

    public void onClick(View v) {
        super.onBackPressed();
    }

    public static void setAutoOrientationEnabled(Context context, boolean enabled)
    {
        Settings.System.putInt( context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
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
