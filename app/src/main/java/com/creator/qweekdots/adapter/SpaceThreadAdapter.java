package com.creator.qweekdots.adapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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
import com.creator.qweekdots.activity.SpaceActivity;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.models.Message;
import com.creator.qweekdots.ui.MessageBottomSheet;
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
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
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

public class SpaceThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static String TAG = SpaceThreadAdapter.class.getSimpleName();

    private String userId;
    private String username;
    private int SELF = 100;

    private Context mContext;
    private ArrayList<Message> messageArrayList;
    private final Random mRandom;
    private final ArrayList<Integer> mBackgroundColors;

    private SimpleExoPlayer video_player;
    private PlayerView simpleExoPlayerView;

    public class ViewHolder extends RecyclerView.ViewHolder {
        EmojiTextView message;
        TextView messageTime, metaSpace;
        CircleImageView avatar;
        CardView messageMediaCard, metaSpaceCard;
        ImageView messagePhoto;
        //RSVideoPlayerStandard messageVideo;
        PlayerView messageVideoPlayer;
        ImageView expandBtn, likeBtn;
        RichLinkView url;

        public ViewHolder(View view) {
            super(view);
            message = itemView.findViewById(R.id.message);
            avatar = itemView.findViewById(R.id.message_avatar);
            messageTime = itemView.findViewById(R.id.message_timestamp);
            metaSpaceCard = itemView.findViewById(R.id.meta_space_card);
            metaSpace = itemView.findViewById(R.id.meta_space);
            messageMediaCard = itemView.findViewById(R.id.message_mediaCard);
            messagePhoto = itemView.findViewById(R.id.message_media);
            //messageVideo = itemView.findViewById(R.id.message_videoplayer);
            messageVideoPlayer = itemView.findViewById(R.id.message_video);
            url = itemView.findViewById(R.id.messageUrl);

            //Message Actions
            expandBtn = itemView.findViewById(R.id.expand_btn);
            likeBtn = itemView.findViewById(R.id.like_btn);
        }
    }


    public SpaceThreadAdapter(Context mContext, ArrayList<Message> messageArrayList, String userId, String username) {
        this.mContext = mContext;
        this.messageArrayList = messageArrayList;
        this.userId = userId;
        this.username = username;

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

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        EmojiManager.install(new IosEmojiProvider());
        View itemView;

        // view type is to identify where to render the chat message
        // left or right
        if (viewType == SELF) {
            // self message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.space_feed_item, parent, false);
        } else {
            // others message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.space_feed_item, parent, false);
        }

        return new ViewHolder(itemView);
    }


    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message.getUser().getId().equals(userId)) {
            return SELF;
        }
        return position;
    }

    @SuppressLint("LogNotTimber")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, int position) {

        Message message = messageArrayList.get(position);

        if(message.getMessage().isEmpty()) {
            ((ViewHolder) holder).message.setVisibility(View.GONE);

        } else {
            String messageTXT = message.getMessage();
            messageTXT = unescapeJava(messageTXT);

            // Check for empty status message
            if (!TextUtils.isEmpty(messageTXT)) {
                SpannableString hashText = new SpannableString(messageTXT);
                Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                while (matcher.find()) {
                    hashText.setSpan(new BackgroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
                }
                //if none set text and make it visible
                ((ViewHolder) holder).message.setText(hashText);
                ((ViewHolder) holder).message.setVisibility(View.VISIBLE);
            } else {
                // status is empty, remove from view
                ((ViewHolder) holder).message.setVisibility(View.GONE);
            }

            // Checking for null feed url
            if (message.getHasLink() == 1) {
                ((ViewHolder) holder).url.setLink(message.getLink(), new ViewListener() {
                    @Override
                    public void onSuccess(boolean status) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
                ((ViewHolder) holder).url.setVisibility(View.VISIBLE);
            } else {
                // url is null, remove from the view
                ((ViewHolder) holder).url.setVisibility(View.GONE);
            }
        }

        String timestamp = message.getCreatedAt();
        ((ViewHolder) holder).messageTime.setText(timestamp);

        RequestOptions requestOptions2 = new RequestOptions() // because file name is always same
                .format(DecodeFormat.PREFER_RGB_565);
        Drawable placeholder = getTinted(mContext.getResources().getColor(R.color.contentTextColor));
        Glide
                .with(mContext)
                .load(message.getUser().getAvatar())
                .override(35, 35)
                .placeholder(placeholder)
                .error(placeholder)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(requestOptions2)
                .into(((ViewHolder) holder).avatar);

        ((ViewHolder) holder).avatar.setOnClickListener(v-> {
            Intent i = new Intent(mContext, ProfileActivity.class);
            i.putExtra("profile", message.getUser().getUserName());
            mContext.startActivity(i);
            customType(mContext, "fadein-to-fadeout");
        });

        // Set Spaces Meta
        if(message.getSpace()==null) {
            ((ViewHolder) holder).metaSpaceCard.setVisibility(View.GONE);
        } else {
            ((ViewHolder) holder).metaSpaceCard.setVisibility(View.VISIBLE);

            int randomColor = mBackgroundColors.get(mRandom.nextInt(15));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((ViewHolder) holder).metaSpaceCard.setCardBackgroundColor(mContext.getColor(randomColor));
            }
            String spacename = "s/"+message.getSpace().getSpace_name();
            ((ViewHolder) holder).metaSpace.setText(spacename);

            ((ViewHolder) holder).metaSpace.setOnClickListener(v-> {
                Intent i = new Intent(mContext, SpaceActivity.class);
                i.putExtra("chat_room_id", message.getSpace().getId());
                i.putExtra("name", message.getSpace().getId());
                mContext.startActivity(i);
                customType(mContext, "fadein-to-fadeout");
            });
        }

        if(message.getHasMedia() == 1) {
            ((ViewHolder) holder).messageMediaCard.setVisibility(View.VISIBLE);

            if(message.getMediaType().equals("photo")) {
                ((ViewHolder) holder).messagePhoto.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).messageVideoPlayer.setVisibility(View.GONE);

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                        .format(DecodeFormat.PREFER_RGB_565)
                        .skipMemoryCache(true);
                Glide
                        .with(mContext)
                        .load(message.getQweeksnap())
                        .override(Target.SIZE_ORIGINAL)
                        .apply(requestOptions)
                        .into(((ViewHolder) holder).messagePhoto);

                /*
                List<String> images = Collections.singletonList(message.getQweeksnap());
                ((ViewHolder) holder).messagePhoto.setOnClickListener(v-> new StfalconImageViewer.Builder<>(mContext, images, (imageView, imageUrl) -> Glide.with(mContext).load(imageUrl).into(imageView))
                        .withTransitionFrom(((ViewHolder) holder).messagePhoto)
                        .withBackgroundColor(mContext.getResources().getColor(R.color.tabColor))
                        .show());

                 */
            } else if (message.getMediaType().equals("video")) {
                ((ViewHolder) holder).messageVideoPlayer.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).messagePhoto.setVisibility(View.GONE);

                // qweekvid
                /*
                ((ViewHolder) holder).messageVideo.setUp(message.getQweeksnap(),
                        RSVideoPlayer.SCREEN_LAYOUT_LIST);
                ((ViewHolder) holder).messageVideo.setThumbImageView(message.getQweeksnap());
                 */

                DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //test

                AdaptiveTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
                TrackSelector trackSelector =
                        new DefaultTrackSelector(videoTrackSelectionFactory);

                video_player = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);
                simpleExoPlayerView = new PlayerView(mContext);
                simpleExoPlayerView = ((ViewHolder) holder).messageVideoPlayer;

                int h = simpleExoPlayerView.getResources().getConfiguration().screenHeightDp;
                int w = simpleExoPlayerView.getResources().getConfiguration().screenWidthDp;
                Timber.v("height : " + h + " weight: " + w);
                ////Set media controller
                simpleExoPlayerView.setUseController(true);//set to true or false to see controllers
                simpleExoPlayerView.requestFocus();
                simpleExoPlayerView.setMinimumHeight(h);
                // Bind the player to the view.
                simpleExoPlayerView.setPlayer(video_player);

                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "Qweekdots"), bandwidthMeter);

                MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(message.getQweeksnap()));
                final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
                // Prepare the player with the source.
                video_player.prepare(videoSource);

                video_player.addListener(new ExoPlayer.EventListener() {
                    @Override
                    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

                    }

                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                        Timber.v("Listener-onTracksChanged... ");
                    }

                    @Override
                    public void onLoadingChanged(boolean isLoading) {

                    }

                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        Timber.v("Listener-onPlayerStateChanged..." + playbackState + "|||isDrawingCacheEnabled():" + simpleExoPlayerView.isDrawingCacheEnabled());
                    }

                    @Override
                    public void onRepeatModeChanged(int repeatMode) {

                    }

                    @Override
                    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

                    }

                    @Override
                    public void onPlayerError(ExoPlaybackException error) {
                        Timber.v("Listener-onPlayerError...");
                        video_player.stop();
                        video_player.prepare(loopingSource);
                        video_player.setPlayWhenReady(false);
                    }

                    @Override
                    public void onPositionDiscontinuity(int reason) { }

                    @Override
                    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) { }

                    @Override
                    public void onSeekProcessed() {

                    }
                });
                video_player.setPlayWhenReady(false); //run file/link when ready to play.
                //video_player.setVideoDebugListener((VideoRendererEventListener) context);
            }
        }

        ((ViewHolder) holder).message.setOnClickListener(v -> {
            MessageBottomSheet bottomSheet = new MessageBottomSheet(mContext, username, message.getId());
            FragmentManager manager = ((AppCompatActivity)mContext).getSupportFragmentManager();
            bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
        });

        ((ViewHolder) holder).expandBtn.setOnClickListener(v -> {
            MessageBottomSheet bottomSheet = new MessageBottomSheet(mContext, username, message.getId());
            FragmentManager manager = ((AppCompatActivity)mContext).getSupportFragmentManager();
            bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
        });

        // Like Builder
        /*
         * If drop is liked, set resource to liked long with resource color
         * else, resource is not liked yet
         */
        if(message.getLiked().equals("yes")) {
            ((ViewHolder) holder).likeBtn.setImageResource(R.drawable.ic_liked);
            ((ViewHolder) holder).likeBtn.setColorFilter(mContext.getResources().getColor(R.color.likeColor));
        } else if(message.getLiked().equals("no")) {
            ((ViewHolder) holder).likeBtn.setImageResource(R.drawable.ic_like);
            ((ViewHolder) holder).likeBtn.setColorFilter(mContext.getResources().getColor(R.color.Gray));
        }

        // Set up Actions
        //Like
        /*
         * On click, first check if user liked already
         * If user liked already, unlike and set resource to pre-liked state
         * else, like along with resource change
         */

        ((ViewHolder) holder).likeBtn.setOnClickListener(v -> {
            ObjectAnimator animY = ObjectAnimator.ofFloat(((ViewHolder) holder).likeBtn, "translationY", -100f, 0f);
            animY.setDuration(1000);//1sec
            animY.setInterpolator(new BounceInterpolator());
            animY.setRepeatCount(0);
            animY.start();
            //check whether it is liked or unliked
            if (message.getLiked().equals("yes")) {
                //isliked, unlike
                //Toasty.info(context, "taking back like...", Toasty.LENGTH_SHORT).show();

                message.setLiked("no");
                ((ViewHolder) holder).likeBtn.setImageResource(R.drawable.ic_like);
                ((ViewHolder) holder).likeBtn.setColorFilter(mContext.getResources().getColor(R.color.Gray));
                doLike("unlike", message.getId(), username, message.getUser().getUserName());
            } else {
                //like
                //Toasty.info(context, "liking...", Toasty.LENGTH_SHORT).show();

                message.setLiked("yes");
                ((ViewHolder) holder).likeBtn.setImageResource(R.drawable.ic_liked);
                ((ViewHolder) holder).likeBtn.setColorFilter(mContext.getResources().getColor(R.color.likeColor));
                doLike("like", message.getId(), username, message.getUser().getUserName());
            }
        });
    }

    private @Nullable
    Drawable getTinted(@ColorInt int color) {
        // need to mutate otherwise all references to this drawable will be tinted
        Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_alien).mutate();
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

    private static String unescapeJava(String escaped) {
        if (!escaped.contains("\\u"))
            return escaped;

        StringBuilder processed = new StringBuilder();

        int position = escaped.indexOf("\\u");
        while (position != -1) {
            if (position != 0)
                processed.append(escaped.substring(0, position));
            String token = escaped.substring(position + 2, position + 6);
            escaped = escaped.substring(position + 6);
            processed.append((char) Integer.parseInt(token, 16));
            position = escaped.indexOf("\\u");
        }
        processed.append(escaped);

        return processed.toString();
    }

    /*
     * Action Helpers
     * Functions for Drop Actions
     *
     */
    private void doLike(final String type, final String id, final String liker,
                        final String liked) {
        // Tag used to cancel the request
        String tag_string_req = "req_like";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/message_like.php", response -> {
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
                Toasty.error(mContext, "Mission Control, come in !", Toast.LENGTH_SHORT).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Like Error: %s", error.getMessage());
            Toasty.error(mContext,
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

    private void deleteMessage(final String message_id, String user_id) {
        // Tag used to cancel the request
        String tag_string_req = "req_delete";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/message_delete.php", response -> {
            Timber.tag(TAG).d("Delete Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    String sent = jObj.getString("sent");
                    Toasty.success(mContext, sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(mContext, "Mission Control, come in !", Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Delete Error: %s", error.getMessage());
            Toasty.error(mContext,
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("id", message_id);
                params.put("u", user_id);

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

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }
}
