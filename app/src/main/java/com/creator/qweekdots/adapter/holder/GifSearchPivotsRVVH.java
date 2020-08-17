package com.creator.qweekdots.adapter.holder;

import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.view.IGifSearchView;
import com.creator.qweekdots.sdk.holder.SearchSuggestionVH;
import com.creator.qweekdots.sdk.widget.SearchSuggestionRecyclerView;
import com.tenor.android.core.util.AbstractUIUtils;
import com.tenor.android.core.widget.viewholder.StaggeredGridLayoutItemViewHolder;

import org.jetbrains.annotations.NotNull;

public class GifSearchPivotsRVVH<CTX extends IGifSearchView> extends StaggeredGridLayoutItemViewHolder<CTX> {

    // Horizontal RecyclerView to display related search suggestions
    private final SearchSuggestionRecyclerView mPivotsRV;
    // ProgressBar to display while waiting for GIF background to load
    private final int mPivotMargin;

    public GifSearchPivotsRVVH(View itemView, CTX context) {
        super(itemView, context);

        mPivotsRV = itemView.findViewById(R.id.gspv_rv_pivotlist);

        if (!hasContext()) {
            mPivotMargin = 0;
            return;
        }

        mPivotMargin = AbstractUIUtils.dpToPx(getContext(), 1);
        mPivotsRV.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NotNull Rect outRect, @NotNull View view, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {
                outRect.left = mPivotMargin;
                outRect.right = mPivotMargin;
            }
        });
    }

    public void setOnSearchSuggestionClickListener(@Nullable final SearchSuggestionVH.OnClickListener listener) {
        mPivotsRV.setOnSearchSuggestionClickListener(listener);
    }

    public void setQuery(@Nullable final String query) {
        if (!TextUtils.isEmpty(query)) {
            mPivotsRV.getSearchSuggestions(query);
        }
    }
}
