package com.creator.qweekdots.adapter.holder;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.SearchReactionsActivity;
import com.creator.qweekdots.adapter.view.IMainView;
import com.tenor.android.core.model.impl.Tag;
import com.tenor.android.core.widget.viewholder.StaggeredGridLayoutItemViewHolder;

public class TagItemVH<CTX extends IMainView> extends StaggeredGridLayoutItemViewHolder<CTX> {

    // Preview GIF image shown in the background
    private final ImageView mImage;
    // Display name of the Tag
    private final TextView mName;

    private Tag mTag;

    private String dropTxt;

    public TagItemVH(@NonNull View itemView, CTX context, String droptext) {
        super(itemView, context);

        mImage = itemView.findViewById(R.id.it_iv_image);
        mName = itemView.findViewById(R.id.it_tv_name);
        dropTxt = droptext;

        itemView.setOnClickListener(view -> onClicked());
    }

    public void render(@Nullable final Tag tag) {
        if (tag == null) {
            return;
        }
        mTag = tag;
        this.setText(tag.getName()).setImage(tag.getImage());
    }

    private TagItemVH setText(@Nullable final CharSequence text) {
        // empty string allowed
        if (text == null) {
            return this;
        }
        mName.setText(text);
        return this;
    }

    private TagItemVH<CTX> setImage(@Nullable final String image) {
        if (TextUtils.isEmpty(image)) {
            return this;
        }

        // load to display
        Glide
                .with(getContext())
                .asGif()
                .load(image)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mImage);

        return this;
    }

    public Tag getTag() {
        return mTag;
    }

    private void onClicked() {
        if (!hasContext()) {
            return;
        }

        // Start a new search
        Intent intent = new Intent(getContext(), SearchReactionsActivity.class);
        intent.putExtra(SearchReactionsActivity.KEY_QUERY, getTag().getSearchTerm());
        intent.putExtra("drop_txt", dropTxt);
        getContext().startActivity(intent);
    }
}
