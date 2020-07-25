package com.creator.qweekdots.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Switch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.creator.qweekdots.R;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.prefs.DarkModePrefManager;

import java.util.HashMap;
import java.util.Objects;

import static maes.tech.intentanim.CustomIntent.customType;

public class ActivityThemeOptions extends AppCompatActivity {
    View decorView;
    private SQLiteHandler db;
    private String username;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_options);

        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        if(new DarkModePrefManager(this).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(getResources().getColor(R.color.contentBodyColor));
            }
        }

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(getApplication()));
        // session manager

        // Fetching user details from SQLite
        HashMap<String, String> userData = db.getUserDetails();

        username = userData.get("username");

        //function for enabling dark mode
        Switch darkModeSwitch = findViewById(R.id.darkModeSwitch);
        darkModeSwitch.setChecked(new DarkModePrefManager(getApplicationContext()).isNightMode());
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DarkModePrefManager darkModePrefManager = new DarkModePrefManager(getApplicationContext());
            darkModePrefManager.setDarkMode(!darkModePrefManager.isNightMode());
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            //OptionsBottomSheet fragmentOpt = new OptionsBottomSheet(context);
            //getParentFragmentManager().beginTransaction().replace(R.id.frameOpt, fragmentOpt).commit();
            //Intent i = new Intent(getApplicationContext(), MainActivity.class);
            //startActivity(i);
            this.recreate();
        });
    }

    public void onClick(View v) {
        //Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
        //i.putExtra("profile", username);
        //startActivity(i);

        //super.onBackPressed();
        super.finish();
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
