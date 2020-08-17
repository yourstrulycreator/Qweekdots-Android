package com.creator.qweekdots.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.creator.qweekdots.R;

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

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                launchApp();
            }
        }, 3000);

    }

    @Override
    protected void onResume() {
        super.onResume();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                launchApp();
            }
        }, 3000);
    }

    private void launchApp() {
        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
        startActivity(intent);
        customType(SplashScreen.this, "fadein-to-fadeout");
        finish();
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
