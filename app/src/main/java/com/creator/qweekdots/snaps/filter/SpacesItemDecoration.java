package com.creator.qweekdots.snaps.filter;


import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,@NonNull View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1) {
            outRect.left = space;
            outRect.right = 0;
        }else{
            outRect.right = space;
            outRect.left = 0;
        }
    }
}