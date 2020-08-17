package com.creator.qweekdots.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.creator.qweekdots.R;
import com.creator.qweekdots.mediaplayer.RSVideoPlayer;
import com.creator.qweekdots.mediaplayer.RSVideoPlayerStandard;
import com.creator.qweekdots.models.VideoItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HotVideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // View Types
    private static final int FEED= 0;
    private static final int LOADING = 1;
    private static final String TAG = "HotVideosAdapter";
    private List<VideoItem> videoItems;
    private List<Integer> mDrawable = new ArrayList<>();
    private Context context;
    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;
    private String errorMsg;

    public HotVideosAdapter(Context context, List<Integer> drawableList, Fragment f) {
        this.context = context;
        videoItems = new ArrayList<>();
        mDrawable.addAll(drawableList);
    }

    List<VideoItem> getHotVideos() {
        return videoItems;
    }

    public void setHotVideos(List<VideoItem> videoItems) {
        this.videoItems = videoItems;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case FEED:
                View viewItem = inflater.inflate(R.layout.carousel_videos, parent, false);
                viewHolder = new FeedVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {

            case FEED:
                final HotVideosAdapter.FeedVH feedVH = (HotVideosAdapter.FeedVH) holder;
                //Log.i(TAG, "onBindViewHolder [" + feedVH.video.hashCode() + "] position=" + position);
                feedVH.bind(videoItems.get(position), mDrawable.get(position % 8));

                break;

            case LOADING:
                HotVideosAdapter.LoadingVH loadingVH = (HotVideosAdapter.LoadingVH) holder;

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return videoItems == null ? 0 : videoItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return FEED;
        } else {
            return (position == videoItems.size() - 1 && isLoadingAdded) ? LOADING : FEED;
        }
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     */
    private void showRetry() {
        retryPageLoad = false;
        notifyItemChanged(videoItems.size() - 1);

        if (null != null) this.errorMsg = null;
    }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */
    private void add(VideoItem r) {
        videoItems.add(r);
        notifyItemInserted(videoItems.size() - 1);
    }

    public void addAll(List<VideoItem> videoItems) {
        for (VideoItem result : videoItems) {
            add(result);
        }
    }

    private void remove(VideoItem r) {
        int position = videoItems.indexOf(r);
        if (position > -1) {
            videoItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    private VideoItem getItem(int position) {
        return videoItems.get(position);
    }
    /*
        View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Feed content ViewHolder
     */
    protected class FeedVH extends RecyclerView.ViewHolder {
        @BindView(R.id.ivFrame) ImageView mFrameView;
        @BindView(R.id.videoplayer) RSVideoPlayerStandard video;
        @BindView(R.id.tvVideoTitle) TextView title;
        @BindView(R.id.ivVideo) CardView videoCard;

        FeedVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(VideoItem videoItem, @DrawableRes Integer drawable) {

            video.setUp(videoItem.getQweekSnap(),
                    RSVideoPlayer.SCREEN_LAYOUT_LIST, videoItem.getDrop());
            Picasso.get()
                    .load(videoItem.getQweekSnapThumb())
                    .into(video.thumbImageView);

            title.setText(videoItem.getDrop());
        }
    }


    /**
     * Load content ViewHolder
     */
    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry();
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }

}