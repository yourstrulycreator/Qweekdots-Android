package com.creator.qweekdots.adapter.holder;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.OrientationHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.DropReactionsActivity;
import com.creator.qweekdots.adapter.view.IGifSearchView;
import com.creator.qweekdots.sdk.concurrency.WeakRefOnPreDrawListener;
import com.creator.qweekdots.widget.IFetchGifDimension;
import com.tenor.android.core.constant.MediaCollectionFormats;
import com.tenor.android.core.model.impl.Result;
import com.tenor.android.core.network.ApiClient;
import com.tenor.android.core.util.AbstractListUtils;
import com.tenor.android.core.widget.viewholder.StaggeredGridLayoutItemViewHolder;

import static maes.tech.intentanim.CustomIntent.customType;

public class GifSearchItemVH<CTX extends IGifSearchView> extends StaggeredGridLayoutItemViewHolder<CTX> {
    // ImageView to contain and display the GIF
    private final ImageView mImageView;
    // Icon to signify the GIF is an mp4 with sound
    private final View mAudio;
    // Model with the necessary GIF fields, including urls
    private Result mResult;

    // Waits for holder's view to be pre-drawn, and returns the height and width values based on the GIFs aspectRatio
    private IFetchGifDimension mFetchGifDimensionListener;

    public GifSearchItemVH(View itemView, CTX ctx) {
        super(itemView, ctx);

        mImageView = itemView.findViewById(R.id.gdi_iv_image);
        mAudio = itemView.findViewById(R.id.gdi_v_audio);

        itemView.setOnClickListener(view -> onClicked());

    }

    public void renderGif(@Nullable final Result result) {
        if (result == null || !hasContext()) {
            return;
        }
        mResult = result;

        // Only show mAudio for Results with sound
        if (result.isHasAudio()) {
            mAudio.setVisibility(View.VISIBLE);
        } else {
            mAudio.setVisibility(View.GONE);
        }

        if (AbstractListUtils.isEmpty(result.getMedias())) {
            return;
        }

        final String url = result.getMedias().get(0).get(MediaCollectionFormats.GIF_TINY).getUrl();
        Glide
                .with(getContext())
                .asGif()
                .load(url)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mImageView);
    }

    public void setFetchGifHeightListener(IFetchGifDimension fetchGifDimensionListener) {
        mFetchGifDimensionListener = fetchGifDimensionListener;
    }

    public boolean setupViewHolder(@Nullable final Result result, int orientation) {
        if (result == null || !hasContext()) {
            return false;
        }
        postChangeGifViewDimension(itemView, result, orientation);
        return true;
    }

    private void postChangeGifViewDimension(@NonNull View view, final @NonNull Result result, final int orientation) {
        final float aspectRatio = result.getMedias().get(0).get(MediaCollectionFormats.GIF_TINY).getAspectRatio();

        /*
         * Re-adjust itemView size on items without exterior ad badge.
         * The MATCH_PARENT on the image view is two levels too low,
         * and thus got dominated by it parent views
         */
        view.getViewTreeObserver().addOnPreDrawListener(new WeakRefOnPreDrawListener<View>(view) {
            @Override
            public boolean onPreDraw(@NonNull View view) {
                view.getViewTreeObserver().removeOnPreDrawListener(this);

                ViewGroup.LayoutParams params = view.getLayoutParams();

                // Calculate the height/width of the GIF based on aspectRatio and orientation
                if (orientation == OrientationHelper.VERTICAL) {
                    params.height = Math.round((view.getMeasuredWidth() / aspectRatio));
                }

                if (orientation == OrientationHelper.HORIZONTAL) {
                    params.width = Math.round((view.getMeasuredHeight() * aspectRatio));
                }
                if (mFetchGifDimensionListener != null) {
                    mFetchGifDimensionListener.onReceiveViewHolderDimension(result.getId(), params.width,
                            params.height, orientation);
                }
                view.setLayoutParams(params);
                return true;
            }
        });
    }

    private void onClicked() {
        if (!hasContext()) {
            return;
        }

        if (mResult == null) {
            return;
        }

        // register share to receive more relevant results in the future
        ApiClient.registerShare(getContext(), mResult.getId());

        // todo - Add in functionality for when a GIF is clicked by the user
        Intent intent = new Intent(getContext(), DropReactionsActivity.class);
        intent.putExtra("url", mResult.getMedias().get(0).get(MediaCollectionFormats.GIF_MEDIUM).getUrl());
        getContext().startActivity(intent);
        customType(getContext(), "right-to-left");
    }
}
