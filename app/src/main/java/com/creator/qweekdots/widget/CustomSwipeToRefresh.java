package com.creator.qweekdots.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.reflect.Field;

public class CustomSwipeToRefresh extends SwipeRefreshLayout implements ViewTreeObserver
        .OnGlobalLayoutListener {

    private static float MAX_SWIPE_DISTANCE_FACTOR = 0.8f;
    private static int DEFAULT_REFRESH_TRIGGER_DISTANCE = 350;

    private int refreshTriggerDistance = DEFAULT_REFRESH_TRIGGER_DISTANCE;

    ViewTreeObserver vto;

    public CustomSwipeToRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);

        vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        // Calculate the trigger distance.
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        float mDistanceToTriggerSync = Math.min(
                ((View) getParent()).getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                refreshTriggerDistance * metrics.density);

        try {
            // Set the internal trigger distance using reflection.
            Field field = SwipeRefreshLayout.class.getDeclaredField("mTotalDragDistance");
            field.setAccessible(true);
            field.setFloat(this, mDistanceToTriggerSync);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        // Only needs to be done once so remove listener.
        ViewTreeObserver obs = getViewTreeObserver();
        obs.removeOnGlobalLayoutListener(this);
    }

    private int getRefreshTriggerDistance() {
        return refreshTriggerDistance;
    }

    private void setRefreshTriggerDistance(int refreshTriggerDistance) {
        this.refreshTriggerDistance = refreshTriggerDistance;
    }
}
