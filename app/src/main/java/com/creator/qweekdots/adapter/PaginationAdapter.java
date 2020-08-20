package com.creator.qweekdots.adapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
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
import com.creator.qweekdots.mediaplayer.RSVideoPlayer;
import com.creator.qweekdots.mediaplayer.RSVideoPlayerStandard;
import com.creator.qweekdots.models.FeedItem;
import com.creator.qweekdots.ui.DropBottomSheet;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.gauravk.audiovisualizer.visualizer.BlobVisualizer;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.ponnamkarthik.richlinkpreview.RichLinkView;
import io.github.ponnamkarthik.richlinkpreview.ViewListener;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int FEED= 0;
    private static final int LOADING = 1;

    private List<FeedItem> feedItems;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;
    private String username;
    private boolean isPlaying = false;
    private MediaPlayer player;

    private static final String TAG = PaginationAdapter.class.getSimpleName();

    public PaginationAdapter(Context context, Fragment f, String username) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) f;
        this.username = username;
        feedItems = new ArrayList<>();
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
                View viewItem = inflater.inflate(R.layout.newsfeed_item, parent, false);
                viewHolder = new FeedVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context!=null) {
            FeedItem feedItem = feedItems.get(position);
            Timber.tag(TAG).d(String.valueOf(position));

            switch (getItemViewType(position)) {

                case FEED:
                    FeedVH feedVH = (FeedVH) holder;

                    // Set fullname
                    ((FeedVH) holder).fullnameTxt.setText(feedItem.getFullname());

                    // Set username
                    ((FeedVH) holder).usernameTxt.setText("q/" + feedItem.getUsername());

                    //Build layout depending on type
                    if(feedItem.getHasMedia() == 1) {
                        //Set up qweeksnap if drop hasMedia
                        switch (feedItem.getType()) {
                            case "qweekpic":
                                //Drop has media
                                ((FeedVH) holder).droptextTextLayout.setVisibility(View.GONE);
                                // QweekSnap Card
                                ((FeedVH) holder).qweeksnapCard.setVisibility(View.VISIBLE);

                                //Media type is QweekPic
                                ((FeedVH) holder).qweeksnap.setVisibility(View.VISIBLE);
                                ((FeedVH) holder).video.setVisibility(View.GONE);
                                ((FeedVH) holder).audioLayout.setVisibility(View.GONE);

                                RequestOptions requestOptions = new RequestOptions() // because file name is always same
                                        .format(DecodeFormat.PREFER_RGB_565);
                                Glide
                                        .with(context)
                                        .load(feedItem.getQweekSnap())
                                        .override(Target.SIZE_ORIGINAL)
                                        .thumbnail(0.3f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .apply(requestOptions)
                                        .into(((FeedVH) holder).qweeksnap);
                                break;
                            case "qweekvid":
                                //Drop has media
                                ((FeedVH) holder).droptextTextLayout.setVisibility(View.GONE);
                                // QweekSnap Card
                                ((FeedVH) holder).qweeksnapCard.setVisibility(View.VISIBLE);

                                //Media type is QweekVid
                                ((FeedVH) holder).video.setVisibility(View.VISIBLE);
                                ((FeedVH) holder).qweeksnap.setVisibility(View.GONE);
                                ((FeedVH) holder).audioLayout.setVisibility(View.GONE);

                                // Setup QweekVid with thumbnail
                                ((FeedVH) holder).video.setUp(feedItem.getQweekSnap(),
                                        RSVideoPlayer.SCREEN_LAYOUT_LIST);
                                ((FeedVH) holder).video.setThumbImageView(feedItem.getQweekSnap());
                                break;
                            case "audio":
                                //Drop has media
                                ((FeedVH) holder).droptextTextLayout.setVisibility(View.GONE);
                                // QweekSnap Card
                                ((FeedVH) holder).qweeksnapCard.setVisibility(View.VISIBLE);

                                //Media type is Audio
                                ((FeedVH) holder).audioLayout.setVisibility(View.VISIBLE);
                                ((FeedVH) holder).playAudio.setVisibility(View.VISIBLE);
                                ((FeedVH) holder).audioTxtLayout.setVisibility(View.VISIBLE);

                                ((FeedVH) holder).qweeksnap.setVisibility(View.GONE);
                                ((FeedVH) holder).video.setVisibility(View.GONE);

                                ((FeedVH) holder).blastAudio.setVisibility(View.GONE);

                                // Set The Drop for Audio
                                SpannableString hashText = new SpannableString(feedItem.getDrop());
                                Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                                while (matcher.find()) {
                                    hashText.setSpan(new BackgroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
                                }
                                ((FeedVH) holder).audioDropTxt.setText(hashText);

                                // Setup Audio Visualisation
                                ((FeedVH) holder).playAudio.setOnClickListener(v-> {
                                    ((FeedVH) holder).playAudio.setVisibility(View.GONE);
                                    ((FeedVH) holder).blastAudio.setVisibility(View.VISIBLE);

                                    if(isPlaying) {
                                        player.stop();
                                        isPlaying = false;
                                    } else {
                                        player = MediaPlayer.create(context, Uri.parse(feedItem.getAudio()));
                                        player.setOnPreparedListener(mp -> {
                                            mp.start();
                                            isPlaying = true;
                                            ((FeedVH) holder).blastAudio.show();
                                            ((FeedVH) holder).blastAudio.setAudioSessionId(mp.getAudioSessionId());
                                            NoiseSuppressor.create(mp.getAudioSessionId());
                                        });
                                        player.setOnCompletionListener(mp -> {
                                            ((FeedVH) holder).blastAudio.hide();
                                            ((FeedVH) holder).blastAudio.release();
                                            ((FeedVH) holder).blastAudio.setVisibility(View.GONE);
                                            ((FeedVH) holder).playAudio.setVisibility(View.VISIBLE);
                                            isPlaying = false;
                                        });
                                    }
                                });

                                ((FeedVH) holder).blastAudio.setOnClickListener(v-> {
                                    if(isPlaying) {
                                        player.stop();
                                        isPlaying = false;
                                    } else {
                                        player = MediaPlayer.create(context, Uri.parse(feedItem.getAudio()));
                                        player.setOnPreparedListener(mp -> {
                                            mp.start();
                                            isPlaying = true;
                                            ((FeedVH) holder).blastAudio.show();
                                            ((FeedVH) holder).blastAudio.setAudioSessionId(mp.getAudioSessionId());
                                            NoiseSuppressor.create(mp.getAudioSessionId());
                                        });
                                        player.setOnCompletionListener(mp -> {
                                            ((FeedVH) holder).blastAudio.hide();
                                            ((FeedVH) holder).blastAudio.release();
                                            ((FeedVH) holder).blastAudio.setVisibility(View.GONE);
                                            ((FeedVH) holder).playAudio.setVisibility(View.VISIBLE);
                                            isPlaying = false;
                                        });
                                    }
                                });

                                break;

                            case "reaction":
                                // Drop has media but Different Layout design
                                ((FeedVH) holder).droptextTextLayout.setVisibility(View.VISIBLE);
                                // QweekSnap Card is not in use for Reactions
                                ((FeedVH) holder).qweeksnapCard.setVisibility(View.GONE);

                                // Reaction Card is used instead
                                ((FeedVH) holder).reactionCard.setVisibility(View.VISIBLE);
                                // Set Reaction GIF
                                Glide
                                        .with(context)
                                        .asGif()
                                        .load(feedItem.getQweekSnap())
                                        .override(Target.SIZE_ORIGINAL)
                                        .thumbnail(0.3f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(((FeedVH) holder).reactionImage);

                                break;
                        }
                    } else {
                        // The drop has no media
                        ((FeedVH) holder).droptextTextLayout.setVisibility(View.VISIBLE);
                        ((FeedVH) holder).qweeksnap.setVisibility(View.GONE);
                        ((FeedVH) holder).video.setVisibility(View.GONE);
                    }

                    // Check for empty status message
                    if (!TextUtils.isEmpty(feedItem.getDrop())) {
                        SpannableString hashText = new SpannableString(feedItem.getDrop());
                        Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                        while (matcher.find()) {
                            hashText.setSpan(new BackgroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
                        }
                        //if none set text and make it visible
                        ((FeedVH) holder).dropText.setText(hashText);
                        ((FeedVH) holder).dropText.setVisibility(View.VISIBLE);
                    } else {
                        // status is empty, remove from view
                        ((FeedVH) holder).dropText.setVisibility(View.GONE);
                    }

                    // Checking for null feed url
                    if (feedItem.getHasLink() == 1) {
                        ((FeedVH) holder).url.setLink(feedItem.getLink(), new ViewListener() {
                            @Override
                            public void onSuccess(boolean status) {

                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                        ((FeedVH) holder).url.setVisibility(View.VISIBLE);
                    } else {
                        // url is null, remove from the view
                        ((FeedVH) holder).url.setVisibility(View.GONE);
                    }

                    // load Drop Profile Avatar
                    /*Picasso.get()
                            .load(feedItem.getProfilePic())
                            .resize(40, 40)
                            .placeholder(R.drawable.ic_alien)
                            .error(R.drawable.ic_alien)
                            .centerCrop()
                            .into(((FeedVH) holder).profilePic);*/

                    RequestOptions requestOptions = new RequestOptions() // because file name is always same
                            .format(DecodeFormat.PREFER_RGB_565);
                    Drawable placeholder = getTinted(context.getResources().getColor(R.color.contentTextColor));
                    Glide
                            .with(context)
                            .load(feedItem.getProfilePic())
                            .override(40, 40)
                            .placeholder(placeholder)
                            .error(placeholder)
                            .thumbnail(0.3f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .apply(requestOptions)
                            .into(((FeedVH) holder).profilePic);

                    //Clicks to drops profile
                    ((FeedVH) holder).profilePic.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", feedItem.getUsername());
                        context.startActivity(i);
                        customType(context, "fadein-to-fadeout");
                    });
                    ((FeedVH) holder).usernameTxt.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", feedItem.getUsername());
                        context.startActivity(i);
                        customType(context, "fadein-to-fadeout");
                    });
                    ((FeedVH) holder).fullnameTxt.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", feedItem.getUsername());
                        context.startActivity(i);
                        customType(context, "fadein-to-fadeout");
                    });

                    // Build Drop Actions

                    // Upvote Builder
                    /*
                     * If drop is upvoted, set resource to upvoted along with resource color
                     * else, resource is not upvoted yet
                     */
                    if(feedItem.getUpvoted().equals("yes")) {
                        ((FeedVH) holder).upvoteBtn.setImageResource(R.drawable.ic_upvoted);
                        ((FeedVH) holder).upvoteBtn.setColorFilter(context.getResources().getColor(R.color.upvoteColor));
                    } else if(feedItem.getUpvoted().equals("no")) {
                        ((FeedVH) holder).upvoteBtn.setImageResource(R.drawable.ic_upvote);
                        ((FeedVH) holder).upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }

                    // Downvote Builder
                    /*
                     * If drop is downvoted, set resource to downvoted along with resource color
                     * else, resource is not downvoted yet
                     */
                    if(feedItem.getDownvoted().equals("yes")) {
                        ((FeedVH) holder).downvoteBtn.setImageResource(R.drawable.ic_downvoted);
                        ((FeedVH) holder).downvoteBtn.setColorFilter(context.getResources().getColor(R.color.downvoteColor));
                    } else if(feedItem.getDownvoted().equals("no")) {
                        ((FeedVH) holder).downvoteBtn.setImageResource(R.drawable.ic_downvote);
                        ((FeedVH) holder).downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }

                    // Like Builder
                    /*
                     * If drop is liked, set resource to liked long with resource color
                     * else, resource is not liked yet
                     */
                    if(feedItem.getLiked().equals("yes")) {
                        ((FeedVH) holder).likeBtn.setImageResource(R.drawable.ic_liked);
                        ((FeedVH) holder).likeBtn.setColorFilter(context.getResources().getColor(R.color.likeColor));
                    } else if(feedItem.getLiked().equals("no")) {
                        ((FeedVH) holder).likeBtn.setImageResource(R.drawable.ic_like);
                        ((FeedVH) holder).likeBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }

                    // Set up Actions

                    //Like
                    /*
                     * On click, first check if user liked already
                     * If user liked already, unlike and set resource to pre-liked state
                     * else, like along with resource change
                     */
                    ((FeedVH) holder).likeBtn.setOnClickListener(v -> {
                        ObjectAnimator animY = ObjectAnimator.ofFloat(((FeedVH) holder).likeBtn, "translationY", -100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(0);
                        animY.start();
                        //check whether it is liked or unliked
                        if (feedItem.getLiked().equals("yes")) {
                            //isliked, unlike
                            Toasty.info(context, "taking back like...", Toasty.LENGTH_SHORT).show();

                            feedItem.setLiked("no");
                            ((FeedVH) holder).likeBtn.setImageResource(R.drawable.ic_like);
                            ((FeedVH) holder).likeBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                            doLike("unlike", feedItem.getDrop_Id(), username, feedItem.getUsername());
                        } else {
                            //like
                            Toasty.info(context, "liking...", Toasty.LENGTH_SHORT).show();

                            feedItem.setLiked("yes");
                            ((FeedVH) holder).likeBtn.setImageResource(R.drawable.ic_liked);
                            ((FeedVH) holder).likeBtn.setColorFilter(context.getResources().getColor(R.color.likeColor));
                            doLike("like", feedItem.getDrop_Id(), username, feedItem.getUsername());
                        }
                    });

                    //Upvote
                    /*
                     * On click, first check if user upvoted already
                     * If user upvoted already, undo upvote and set resource to pre-upvote state
                     * else, upvote along with resource change
                     */
                    ((FeedVH) holder).upvoteBtn.setOnClickListener(v -> {
                        ObjectAnimator animY = ObjectAnimator.ofFloat(((FeedVH) holder).upvoteBtn, "translationY", -100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(0);
                        animY.start();

                        //check whether it is upvoted or not
                        if (feedItem.getUpvoted().equals("yes")) {
                            //un-upvote
                            Toasty.info(context, "taking back upvote...", Toasty.LENGTH_SHORT).show();

                            feedItem.setUpvoted("no");
                            ((FeedVH) holder).upvoteBtn.setImageResource(R.drawable.ic_upvote);
                            ((FeedVH) holder).upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                            doUpvote("undo", feedItem.getDrop_Id(), username, feedItem.getUsername());
                        } else {
                            //upvote
                            Toasty.info(context, "upvoting...", Toasty.LENGTH_SHORT).show();

                            feedItem.setUpvoted("yes");
                            //run downvote check and undo if downvoted
                            if(feedItem.getDownvoted().equals("yes")) {
                                ObjectAnimator animD = ObjectAnimator.ofFloat(((FeedVH) holder).downvoteBtn, "translationY", 100f, 0f);
                                animD.setDuration(1000);//1sec
                                animD.setInterpolator(new BounceInterpolator());
                                animD.setRepeatCount(0);
                                animD.start();
                                ((FeedVH) holder).downvoteBtn.setImageResource(R.drawable.ic_downvote);
                                ((FeedVH) holder).downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                                feedItem.setDownvoted("no");
                                doDownvote("undo", feedItem.getDrop_Id(), username, feedItem.getUsername());
                            }

                            ((FeedVH) holder).upvoteBtn.setImageResource(R.drawable.ic_upvoted);
                            ((FeedVH) holder).upvoteBtn.setColorFilter(context.getResources().getColor(R.color.upvoteColor));
                            doUpvote("upvote", feedItem.getDrop_Id(), username, feedItem.getUsername());
                        }
                    });

                    //Downvote
                    /*
                     * On click, first check if user downvoted already
                     * If user downvoted already, undo downvote and set resource to pre-downvote state
                     * else, downvote along with resource
                     */
                    ((FeedVH) holder).downvoteBtn.setOnClickListener(v -> {
                        ObjectAnimator animY = ObjectAnimator.ofFloat(((FeedVH) holder).downvoteBtn, "translationY", 100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(0);
                        animY.start();

                        //check whether it is downvoted or not
                        if (feedItem.getDownvoted().equals("yes")) {
                            //un-do
                            Toasty.info(context, "taking back downvote...", Toasty.LENGTH_SHORT).show();

                            feedItem.setDownvoted("no");
                            ((FeedVH) holder).downvoteBtn.setImageResource(R.drawable.ic_downvote);
                            ((FeedVH) holder).downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                            doDownvote("undo", feedItem.getDrop_Id(), username, feedItem.getUsername());
                        } else {
                            //downvote
                            Toasty.info(context, "downvoting...", Toasty.LENGTH_SHORT).show();

                            feedItem.setDownvoted("yes");
                            //run upvote check and undo if upvoted
                            if(feedItem.getUpvoted().equals("yes")) {
                                ObjectAnimator animD = ObjectAnimator.ofFloat(((FeedVH) holder).upvoteBtn, "translationY", -100f, 0f);
                                animD.setDuration(1000);//1sec
                                animD.setInterpolator(new BounceInterpolator());
                                animD.setRepeatCount(0);
                                animD.start();
                                ((FeedVH) holder).upvoteBtn.setImageResource(R.drawable.ic_upvote);
                                ((FeedVH) holder).upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                                feedItem.setUpvoted("no");
                                doUpvote("undo", feedItem.getDrop_Id(), username, feedItem.getUsername());
                            }

                            ((FeedVH) holder).downvoteBtn.setImageResource(R.drawable.ic_downvoted);
                            ((FeedVH) holder).downvoteBtn.setColorFilter(context.getResources().getColor(R.color.downvoteColor));
                            doDownvote("downvote", feedItem.getDrop_Id(), username, feedItem.getUsername());
                        }
                    });

                    // Expand to view more drop details
                    ((FeedVH) holder).expandBtn.setOnClickListener(v -> {
                        DropBottomSheet bottomSheet = new DropBottomSheet(context, username, feedItem.getDrop_Id());
                        FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                        bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                    });

                    ((FeedVH) holder).dropLayout.setOnClickListener(v -> {
                        DropBottomSheet bottomSheet = new DropBottomSheet(context, username, feedItem.getDrop_Id());
                        FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                        bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                    });

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
     */
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
                    String sent;
                    if(type.equals("like")) {
                        sent = "You liked that didn't you";
                    } else {
                        sent = "You could always like it again";
                    }
                    Toasty.success(context, sent, Toast.LENGTH_SHORT).show();
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
                    String sent;
                    if(type.equals("upvote")) {
                        sent = "Nice, You thought more people should see this!";
                    } else {
                        sent = "Best rethink your actions";
                    }
                    Toasty.success(context, sent, Toast.LENGTH_SHORT).show();
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
                    String sent;
                    if(type.equals("downvote")) {
                        sent = "Yikes, less people will see this. You just saved lives";
                    } else {
                        sent = "Might be worth it huh";
                    }
                    Toasty.success(context, sent, Toast.LENGTH_SHORT).show();
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
        private ImageView qweeksnap, upvoteBtn, downvoteBtn, expandBtn, likeBtn, playAudio, reactionImage;
        private RSVideoPlayerStandard video;
        private LinearLayout droptextTextLayout, audioLayout, audioTxtLayout;
        private CircleImageView profilePic;
        private TextView usernameTxt;
        private EmojiTextView dropText, fullnameTxt, audioDropTxt;
        private RichLinkView url;
        private CardView qweeksnapCard, reactionCard, dropLayout;
        private BlobVisualizer blastAudio;

        FeedVH(View itemView) {
            super(itemView);

            fullnameTxt = itemView.findViewById(R.id.fullnameTxt);
            usernameTxt = itemView.findViewById(R.id.usernameTxt);
            dropText = itemView.findViewById(R.id.txtDrop);
            url = itemView.findViewById(R.id.txtUrl);
            profilePic = itemView.findViewById(R.id.profilePic);
            qweeksnap = itemView.findViewById(R.id.qweekSnap);
            video = itemView.findViewById(R.id.videoplayer);
            upvoteBtn = itemView.findViewById(R.id.upvote_btn);
            downvoteBtn = itemView.findViewById(R.id.downvote_btn);
            expandBtn = itemView.findViewById(R.id.expand_btn);
            likeBtn = itemView.findViewById(R.id.like_btn);
            droptextTextLayout = itemView.findViewById(R.id.droptext_text_layout);
            qweeksnapCard = itemView.findViewById(R.id.drop_qweekSnapCard);
            blastAudio = itemView.findViewById(R.id.blast);
            playAudio = itemView.findViewById(R.id.playAudio);
            audioLayout = itemView.findViewById(R.id.audioLayout);
            audioTxtLayout = itemView.findViewById(R.id.audioTxtLayout);
            audioDropTxt = itemView.findViewById(R.id.txtDrop2);
            reactionCard = itemView.findViewById(R.id.reactionCard);
            reactionImage = itemView.findViewById(R.id.reactionImage);
            dropLayout = itemView.findViewById(R.id.drop_layout);
        }
    }

    /**
     * Loading content ViewHolder
     */
    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

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

    @Override
    public long getItemId(int position) {
        return (long) getItem(position).hashCode();
    }

}