package com.creator.qweekdots.adapter.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.view.IGifSearchView;
import com.tenor.android.core.constant.StringConstant;
import com.tenor.android.core.widget.viewholder.StaggeredGridLayoutItemViewHolder;

public class GifNoResultsVH<CTX extends IGifSearchView> extends StaggeredGridLayoutItemViewHolder<CTX> {
    // Text view to tell the user no results were found
    private final TextView mNoResults;

    public GifNoResultsVH(View itemView, CTX context) {
        super(itemView, context);
        mNoResults = (TextView) itemView.findViewById(R.id.no_results);
    }

    public void setNoResultsMessage(@NonNull String message) {
        mNoResults.setText(StringConstant.getOrEmpty(message));
    }
}
