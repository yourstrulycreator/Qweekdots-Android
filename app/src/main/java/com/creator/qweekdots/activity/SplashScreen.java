package com.creator.qweekdots.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.creator.qweekdots.BuildConfig;
import com.creator.qweekdots.R;
import com.creator.qweekdots.prefs.DarkModePrefManager;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static maes.tech.intentanim.CustomIntent.customType;

public class SplashScreen extends AppCompatActivity {
    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        int splashStatusColor = Color.parseColor("#3a47d5");
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(splashStatusColor);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                launchApp();
            }
        }, 1500);

    }

    @Override
    protected void onResume() {
        super.onResume();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                launchApp();
            }
        }, 1500);
    }

    private void launchApp() {
        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
        startActivity(intent);
        customType(SplashScreen.this, "fadein-to-fadeout");
        finish();
    }
}
