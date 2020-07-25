package com.creator.qweekdots.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.creator.qweekdots.R;

import java.util.Objects;

public class SplashScreen extends AppCompatActivity {

    private AnimationDrawable animationDrawable;
    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        ImageView container = findViewById(R.id.iv_icons);
        container.setBackgroundResource(R.drawable.splash_animation);

        animationDrawable = (AnimationDrawable) container.getBackground();

    }

    @Override
    protected void onResume() {
        super.onResume();

        animationDrawable.start();

        checkAnimationStatus(50, animationDrawable);
    }


    /**
     * check the animation status recursively, keep the animation until it reach the last frame.
     *
     * @param time              period of animation
     * @param animationDrawable animation list
     */
    private void checkAnimationStatus(final int time, final AnimationDrawable animationDrawable) {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (animationDrawable.getCurrent() != animationDrawable.getFrame(animationDrawable.getNumberOfFrames() - 1))
                checkAnimationStatus(time, animationDrawable);
            else launchApp();
        }, time);
    }

    private void launchApp() {
        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
        startActivity(intent);
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
