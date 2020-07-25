package com.creator.qweekdots.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;

import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.TabsAdapter;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.ui.DropQweekSnap;
import com.creator.qweekdots.ui.DropText;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

public class DropPostActivity extends AppCompatActivity {
    private final String TAG = DropPostActivity.class.getSimpleName();

    private SQLiteHandler db;
    private SessionManager session;
    private String username;
    private String mention;
    private static TabLayout tabLayout;
    private int[] tabIcons = {
            R.drawable.q_text,
            R.drawable.ic_photo_camera_black_18dp
    };
    private ViewPager viewPager;

    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_post);

        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        Timber.plant(new Timber.DebugTree());

        Intent intent = getIntent();
        mention = Objects.requireNonNull(intent.getExtras()).getString("mention");

        if(new DarkModePrefManager(this).isNightMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
//            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(getResources().getColor(R.color.contentBodyColor));
            }
        }

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(this).getApplicationContext());
        // session manager

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        username = user.get("username");

        viewPager = findViewById(R.id.post_viewpager);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.post_tabs);
        tabLayout.setupWithViewPager(viewPager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setupTabIcons();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setupTabIcons() {
        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(tabIcons[0]);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(tabIcons[1]);
        //Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(tabIcons[2]);
    }

    private void setCustomTabs() {

        for (int i = 0; i < tabIcons.length; i++) {
            @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.customtab,null);
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            view.findViewById(R.id.icon).setBackgroundResource(tabIcons[i]);
            if(tab!=null) tab.setCustomView(view);
        }

        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        Objects.requireNonNull(tab.getCustomView()).setSelected(true);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        Objects.requireNonNull(tab.getCustomView()).setSelected(false);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );
    }

    public void setupViewPager(ViewPager viewPager) {
        TabsAdapter adapter = new TabsAdapter(getSupportFragmentManager());
        adapter.addFragment(new DropText(), "Drop");
        adapter.addFragment(new DropQweekSnap(), "QweekSnap");
        viewPager.setAdapter(adapter);
    }

    public static void showTabLayout() {
        tabLayout.setVisibility(View.VISIBLE);
    }

    public static void hideTabLayout() {
        tabLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(DropPostActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void setDb(SQLiteHandler db) {
        this.db = db;
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
