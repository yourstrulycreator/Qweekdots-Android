package com.creator.qweekdots.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * Modified StaggeredGridLayoutManager to disable pre-fetching
 */
public class TenorStaggeredGridLayoutManager extends StaggeredGridLayoutManager {
    public TenorStaggeredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setItemPrefetchEnabled(false);
    }

    public TenorStaggeredGridLayoutManager(int spanCount, int orientation) {
        super(spanCount, orientation);
        setItemPrefetchEnabled(false);
    }
}
