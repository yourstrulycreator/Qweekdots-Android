package com.creator.qweekdots.utils;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class FabBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
    private static final String TAG = "ScrollingFABBehavior";
    Handler mHandler;

    public FabBehavior(Context context, AttributeSet attrs) {
        super();
    }

    public FabBehavior() {
        super();
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull final FloatingActionButton child, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
        if (mHandler == null)
            mHandler = new Handler();


        mHandler.postDelayed(() -> {
            child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            Log.d("FabAnim", "startHandler()");
        }, 1000);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (dyConsumed > 0) {
            Log.d("Scrolling", "Up");
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            int fab_bottomMargin = layoutParams.bottomMargin;
            child.animate().translationY(child.getHeight() + fab_bottomMargin).setInterpolator(new LinearInterpolator()).start();
        } else if (dyConsumed < 0) {
            Log.d("Scrolling", "down");
            child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        if (mHandler != null) {
            mHandler.removeMessages(0);
            Log.d("Scrolling", "stopHandler()");
        }
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }


}