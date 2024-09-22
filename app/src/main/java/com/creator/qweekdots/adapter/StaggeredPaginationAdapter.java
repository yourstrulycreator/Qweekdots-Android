package com.creator.qweekdots.adapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.MediaPlayer;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.models.FeedItem;
import com.creator.qweekdots.ui.DropBottomSheet;
import com.creator.qweekdots.ui.ReplyCommentBottomSheet;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.gauravk.audiovisualizer.visualizer.BlobVisualizer;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.ponnamkarthik.richlinkpreview.RichLinkView;
import io.github.ponnamkarthik.richlinkpreview.ViewListener;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class StaggeredPaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int FEED= 0;
    private static final int LOADING = 1;

    private final List<FeedItem> feedItems;
    private final Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private final PaginationAdapterCallback mCallback;

    private String errorMsg;
    private final String username;
    private final boolean isPlaying = false;
    private MediaPlayer player;

    private SimpleExoPlayer video_player;
    private PlayerView simpleExoPlayerView;

    private final Random mRandom;
    private final ArrayList<Integer> mBackgroundColors;


    private static final String TAG = StaggeredPaginationAdapter.class.getSimpleName();

    public StaggeredPaginationAdapter(Context context, Fragment f, String username) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) f;
        this.username = username;
        feedItems = new ArrayList<>();

        mRandom = new Random();
        mBackgroundColors = new ArrayList<>();
        mBackgroundColors.add(R.color.DodgerBlue);
        mBackgroundColors.add(R.color.Tomato);
        mBackgroundColors.add(R.color.Coral);
        mBackgroundColors.add(R.color.SteelBlue);
        mBackgroundColors.add(R.color.DarkSlateBlue);
        mBackgroundColors.add(R.color.DodgerBlue);
        mBackgroundColors.add(R.color.DarkSlateGray);
        mBackgroundColors.add(R.color.ArgentinanBlue);
        mBackgroundColors.add(R.color.DeepPurple);
        mBackgroundColors.add(R.color.MediumSlateBlue);
        mBackgroundColors.add(R.color.VioletBlue);
        mBackgroundColors.add(R.color.SeaGreen);
        mBackgroundColors.add(R.color.CornflowerBlue);
        mBackgroundColors.add(R.color.DeepSkyBlue);
        mBackgroundColors.add(R.color.DarkTurquoise);
        mBackgroundColors.add(R.color.BottleGreen);
        mBackgroundColors.add(R.color.DarkCyan);
        mBackgroundColors.add(R.color.GoGreen);
        mBackgroundColors.add(R.color.JungleGreen);
        mBackgroundColors.add(R.color.Raspberry);
        mBackgroundColors.add(R.color.Folly);
        mBackgroundColors.add(R.color.WarriorsBlue);
        mBackgroundColors.add(R.color.SpaceCadet);
        mBackgroundColors.add(R.color.MajorelleBlue);
    }

    public List<FeedItem> getQweekFeed() {
        return feedItems;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EmojiManager.install(new IosEmojiProvider());
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case FEED:
                View viewItem = inflater.inflate(R.layout.staggered_item, parent, false);
                viewHolder = new FeedVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context!=null) {
            holder.setIsRecyclable(false);
            FeedItem feedItem = feedItems.get(position);
            Timber.tag(TAG).d(String.valueOf(position));

            switch (getItemViewType(position)) {

                case FEED:
                    //Build layout depending on type

                                RequestOptions requestOptions = new RequestOptions() // because file name is always same
                                        .format(DecodeFormat.PREFER_RGB_565);
                                Glide
                                        .with(context)
                                        .load(feedItem.getQweekSnap())
                                        //.override(Target.SIZE_ORIGINAL)
                                        .thumbnail(0.3f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .apply(requestOptions)
                                        .into(((FeedVH) holder).qweeksnap);

                                /*
                                List<String> images = Collections.singletonList(feedItem.getQweekSnap());
                                ((FeedVH) holder).qweeksnap.setOnClickListener(v-> new StfalconImageViewer.Builder<>(context, images, (imageView, imageUrl) -> Glide.with(context).load(imageUrl).into(imageView))
                                        .withTransitionFrom(((FeedVH) holder).qweeksnap)
                                        .withBackgroundColor(context.getResources().getColor(R.color.black))
                                        .show());

                                 */
                    ((StaggeredPaginationAdapter.FeedVH) holder).qweeksnap.setOnClickListener(v-> {
                        DropBottomSheet bottomSheet = new DropBottomSheet(context, username, feedItem.getDrop_Id());
                        FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                        bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                    });

                    // Expand to view more drop details
                    ((FeedVH) holder).qweeksnapCard.setOnTouchListener(new View.OnTouchListener() {
                    private final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public void onLongPress(MotionEvent e) {
                            //Log.d("TEST", "onDoubleTap");
                            DropBottomSheet bottomSheet = new DropBottomSheet(context, username, feedItem.getDrop_Id());
                            FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                            bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                            super.onDoubleTap(e);
                        }

                    });

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        //Log.d("TEST", "Raw event: " + event.getAction() + ", (" + event.getRawX() + ", " + event.getRawY() + ")");
                        gestureDetector.onTouchEvent(event);
                        return true;
                    }
                });

                    /*
                    ((FeedVH) holder).replyBtn.setOnClickListener(v-> {
                        ReplyCommentBottomSheet bottomSheet = new ReplyCommentBottomSheet(feedItem.getDrop_Id(), 0, username);
                        bottomSheet.show(((AppCompatActivity)context).getSupportFragmentManager(), bottomSheet.getTag());
                    });

                    ((FeedVH) holder).expandBtn.setOnClickListener(v -> {
                        DropBottomSheet bottomSheet = new DropBottomSheet(context, username, feedItem.getDrop_Id());
                        FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                        bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                    });

                     */

                    /*
                    ((FeedVH) holder).shareBtn.setOnClickListener(v->{
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "OMGGG! Check out this drop on Qweekdots 😀");
                        String shareMessage= "\nHey 👋, Join Qweekdots to view this drop and lots more 👍, Chat 😉, Bring your friends along 👽. It's Free! 🤩 \n\n";
                        shareMessage = shareMessage + "https://bit.ly/QweekDotsApp" +"\n\n";
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        context.startActivity(Intent.createChooser(shareIntent, "Choose One"));
                    });

                     */

                    break;

                case LOADING:
                    LoadingVH loadingVH = (LoadingVH) holder;

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
    }

    private @Nullable
    Drawable getTinted(@ColorInt int color) {
        // need to mutate otherwise all references to this drawable will be tinted
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_alien).mutate();
        return tint(drawable, ColorStateList.valueOf(color));
    }

    public static Drawable tint(Drawable input, ColorStateList tint) {
        if (input == null) {
            return null;
        }
        Drawable wrappedDrawable = DrawableCompat.wrap(input);
        DrawableCompat.setTintList(wrappedDrawable, tint);
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.MULTIPLY);
        return wrappedDrawable;
    }

    @Override
    public void onViewAttachedToWindow(@NotNull RecyclerView.ViewHolder holder) {
        //Log.e(TAG, "onViewAttachedToWindow position " + holder.getLayoutPosition()+" suppose to be seen "+holder.getLayoutPosition()+" ℃" );
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull RecyclerView.ViewHolder holder) {

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                Timber.d("onScrollStateChanged: Called %s", newState);
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onViewRecycled(@NotNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return feedItems == null ? 0 : feedItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return FEED;
        } else {
            return (position == feedItems.size() - 1 && isLoadingAdded) ? LOADING : FEED;
        }
    }

    /*
     * Action Helpers
     * Functions for Drop Actions
     *
    private void doLike(final String type, final String id, final String liker,
                        final String liked) {
        // Tag used to cancel the request
        String tag_string_req = "req_like";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LIKE, response -> {
            Timber.tag(TAG).d("Like Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    //String sent;
                    if(type.equals("like")) {
                        //sent = "You liked that didn't you";
                    } else {
                        //sent = "You could always like it again";
                    }
                    //Toasty.success(context, sent, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_SHORT).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Like Error: %s", error.getMessage());
            Toasty.error(context,
                    "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("type", type);
                params.put("id", id);
                params.put("liker", liker);
                params.put("liked", liked);

                return params;
            }

        };

        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

     */

    private void doUpvote(final String type, final String id, final String upvoter,
                          final String upvoted) {
        // Tag used to cancel the request
        String tag_string_req = "req_upvote";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_UPVOTE, response -> {
            Timber.tag(TAG).d("Upvote Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    //String sent;
                    if(type.equals("upvote")) {
                        //sent = "Nice, You thought more people should see this!";
                    } else {
                        //sent = "Best rethink your actions";
                    }
                    //Toasty.success(context, sent, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_SHORT).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Upvote Error: %s", error.getMessage());
            Toasty.error(context,
                    "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("type", type);
                params.put("id", id);
                params.put("upvoter", upvoter);
                params.put("upvoted", upvoted);

                return params;
            }

        };

        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void doDownvote(final String type, final String id, final String downvoter,
                            final String downvoted) {
        // Tag used to cancel the request
        String tag_string_req = "req_downvote";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DOWNVOTE, response -> {
            Timber.tag(TAG).d("Downvote Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    //String sent;
                    if(type.equals("downvote")) {
                        //sent = "Yikes, less people will see this. You just saved lives";
                    } else {
                        //sent = "Might be worth it huh";
                    }
                    //Toasty.success(context, sent, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_SHORT).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Downvote Error: %s", error.getMessage());
            Toasty.error(context,
                    "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("type", type);
                params.put("id", id);
                params.put("downvoter", downvoter);
                params.put("downvoted", downvoted);

                return params;
            }

        };

        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    public void add(FeedItem r) {
        feedItems.add(r);
        notifyItemInserted(feedItems.size() - 1);
    }

    public void addAll(List<FeedItem> feedItems) {
        for (FeedItem result : feedItems) {
            add(result);
        }
    }

    public void remove(FeedItem r) {
        int position = feedItems.indexOf(r);
        if (position > -1) {
            feedItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));

            notifyItemRangeRemoved(0, getItemCount());
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new FeedItem());
    }

    public void removeLoadingFooter() {

        isLoadingAdded = false;

        int position = feedItems.size() - 1;
        FeedItem feedItem = getItem(position);

        if (feedItem != null) {
            feedItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public FeedItem getItem(int position) {
        return feedItems.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(feedItems.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Feed content ViewHolder
     */
    protected class FeedVH extends RecyclerView.ViewHolder {
        /*
        private ImageView qweeksnap, upvoteBtn, downvoteBtn, replyBtn, expandBtn, shareBtn, playAudio, reactionImage;
        //private RSVideoPlayerStandard video;
        private LinearLayout droptextTextLayout, audioLayout, audioTxtLayout;
        private CircleImageView profilePic;
        private TextView usernameTxt, upvoteNum, downvoteNum, commentsNum, timestamp, spaceTag;
        private EmojiTextView dropText, fullnameTxt, audioDropTxt;
        private RichLinkView url;
        private CardView qweeksnapCard;
        private BlobVisualizer blastAudio;

        /////
        //private SimpleExoPlayer video_player;
        //private long playbackPosition;
        //private int currentWindow;
        //private boolean playWhenReady;
        private PlayerView exoPlayerView;

         */
        private final CardView qweeksnapCard;
        private final ImageView qweeksnap;


        FeedVH(View itemView) {
            super(itemView);

            /*
            fullnameTxt = itemView.findViewById(R.id.fullnameTxt);
            usernameTxt = itemView.findViewById(R.id.usernameTxt);
            timestamp = itemView.findViewById(R.id.timestamp);
            dropText = itemView.findViewById(R.id.txtDrop);
            url = itemView.findViewById(R.id.txtUrl);
            profilePic = itemView.findViewById(R.id.profilePic);


             */
            qweeksnap = itemView.findViewById(R.id.image);
            //video = itemView.findViewById(R.id.videoplayer);
            /*
            exoPlayerView = itemView.findViewById(R.id.video);
            upvoteBtn = itemView.findViewById(R.id.upvote_btn);
            downvoteBtn = itemView.findViewById(R.id.downvote_btn);
            replyBtn = itemView.findViewById(R.id.reply_btn);
            expandBtn = itemView.findViewById(R.id.expand_btn);
            shareBtn = itemView.findViewById(R.id.share_btn);
            droptextTextLayout = itemView.findViewById(R.id.droptext_text_layout);

             */
            qweeksnapCard = itemView.findViewById(R.id.container);
            /*
            blastAudio = itemView.findViewById(R.id.blast);
            playAudio = itemView.findViewById(R.id.playAudio);
            audioLayout = itemView.findViewById(R.id.audioLayout);
            audioTxtLayout = itemView.findViewById(R.id.audioTxtLayout);
            audioDropTxt = itemView.findViewById(R.id.txtDrop2);
            reactionImage = itemView.findViewById(R.id.reactionImage);
            upvoteNum = itemView.findViewById(R.id.upvoteNum);
            downvoteNum = itemView.findViewById(R.id.downvoteNum);
            commentsNum = itemView.findViewById(R.id.commentsNum);
            spaceTag = itemView.findViewById(R.id.space_tag);

             */
        }
    }

    /**
     * Loading content ViewHolder
     */
    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ProgressBar mProgressBar;
        private final TextView mErrorTxt;
        private final LinearLayout mErrorLayout;

        LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            ImageButton mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
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

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }

    public void onDestroy() {
        video_player.release();
    }

    public void onPause() {
        video_player.pause();
    }

}