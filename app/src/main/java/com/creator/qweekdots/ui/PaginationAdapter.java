package com.creator.qweekdots.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.gauravk.audiovisualizer.visualizer.BlobVisualizer;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private String username, avatar;
    private static String today;
    private boolean isPlaying = false;
    private MediaPlayer player;

    long DURATION = 500;
    private boolean on_attach = true;

    private static final String TAG = PaginationAdapter.class.getSimpleName();

    private FeedVH feedVH;

    PaginationAdapter(Context context, Fragment f, String username, String avatar) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) f;
        this.username = username;
        this.avatar = avatar;
        feedItems = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    List<FeedItem> getQweekFeed() {
        return feedItems;
    }

    public void setNewsFeed(List<FeedItem> feedItems) {
        this.feedItems = feedItems;
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
            Log.d(TAG, String.valueOf(position));

            switch (getItemViewType(position)) {

                case FEED:
                    feedVH = (FeedVH) holder;

                    // Set fullname
                    ((FeedVH) holder).fullnameTxt.setText(feedItem.getFullname());
                    // Set username
                    ((FeedVH) holder).usernameTxt.setText("q/" + feedItem.getUsername());

                    // Converting timestamp into x ago format
                    //String timestamp = getTimeStamp(feedItem.getTimeStamp());
                    //feedVH.timeStamp.setText(timestamp);

                    //Build layout depending on type
                    if(feedItem.getHasMedia() == 1) {
                        //Drop has media
                        //Hide the action buttons for text drop
                        ((FeedVH) holder).droptextTextLayout.setVisibility(View.GONE);

                        // QweekSnap Card
                        ((FeedVH) holder).qweeksnapCard.setVisibility(View.VISIBLE);
                        //Make the action buttons visible for media drops

                        //Set up qweeksnap if drop hasMedia
                        switch (feedItem.getType()) {
                            case "qweekpic":
                                //Media type is QweekPic
                                ((FeedVH) holder).qweeksnap.setVisibility(View.VISIBLE);
                                ((FeedVH) holder).video.setVisibility(View.GONE);

                                /*
                                //qweeksnap
                                Target target = new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        int width = bitmap.getWidth();
                                        int height = bitmap.getHeight();
                                        //feedVH.qweeksnap.setImageBitmap(bitmap);
                                        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
                                        feedVH.qweeksnap.setImageBitmap(scaled);
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                        feedVH.qweeksnap.requestLayout();
                                        feedVH.qweeksnap.getLayoutParams().height = 650;
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                };

                                feedVH.qweeksnap.setTag(target);
                                Picasso.get().load(feedItem.getQweekSnap()).into(target);
                                *
                                 */

                                Drawable progress = getProgressBarIndeterminate();

                                RequestOptions requestOptions = new RequestOptions() // because file name is always same
                                        .format(DecodeFormat.PREFER_RGB_565);
                                Glide
                                        .with(context)
                                        .load(feedItem.getQweekSnap())
                                        .override(Target.SIZE_ORIGINAL)
                                        .thumbnail(0.1f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .apply(requestOptions)
                                        .into(feedVH.qweeksnap);
                                break;
                            case "qweekvid":

                                //Media type is QweekVid
                                ((FeedVH) holder).video.setVisibility(View.VISIBLE);
                                ((FeedVH) holder).qweeksnap.setVisibility(View.GONE);

                                // qweekvid
                                ((FeedVH) holder).video.setUp(feedItem.getQweekSnap(),
                                        RSVideoPlayer.SCREEN_LAYOUT_LIST);
                                ((FeedVH) holder).video.setThumbImageView(feedItem.getQweekSnap());
                                break;
                            case "audio":

                                //Media type is Audio
                                ((FeedVH) holder).playAudio.setVisibility(View.VISIBLE);
                                ((FeedVH) holder).qweeksnap.setVisibility(View.GONE);
                                ((FeedVH) holder).video.setVisibility(View.GONE);
                                ((FeedVH) holder).blastAudio.setVisibility(View.VISIBLE);

                                ((FeedVH) holder).playAudio.setOnClickListener(v-> {
                                    ((FeedVH) holder).playAudio.setVisibility(View.GONE);

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
                                            //feedVH.blastAudio.setVisibility(View.GONE);
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
                                            //feedVH.blastAudio.setVisibility(View.GONE);
                                            ((FeedVH) holder).playAudio.setVisibility(View.VISIBLE);
                                            isPlaying = false;
                                        });
                                    }
                                });

                                break;
                        }
                    } else {
                        // The drop has no media
                        //Make action buttons visible for text drop and hide action button for media drops
                        ((FeedVH) holder).droptextTextLayout.setVisibility(View.VISIBLE);

                        ((FeedVH) holder).qweeksnap.setVisibility(View.GONE);
                        ((FeedVH) holder).video.setVisibility(View.GONE);
                    }

                    // Check for empty status message
                    if (!TextUtils.isEmpty(feedItem.getDrop())) {

                        SpannableString hashText = new SpannableString(feedItem.getDrop());
                        Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                        while (matcher.find())
                        {
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
                    Picasso.get()
                            .load(feedItem.getProfilePic())
                            .resize(40, 40)
                            .placeholder(R.drawable.ic_alien)
                            .error(R.drawable.ic_alien)
                            .centerCrop()
                            .into(((FeedVH) holder).profilePic);

                    ((FeedVH) holder).profilePic.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", feedItem.getUsername());
                        context.startActivity(i);
                        customType(context, "up-to-bottom");
                    });
                    ((FeedVH) holder).usernameTxt.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", feedItem.getUsername());
                        context.startActivity(i);
                        customType(context, "up-to-bottom");
                    });
                    ((FeedVH) holder).fullnameTxt.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", feedItem.getUsername());
                        context.startActivity(i);
                        customType(context, "up-to-bottom");
                    });

                    // Build Drop Actions

                    // Upvote Builder
                    if(feedItem.getUpvoted().equals("yes")) {
                        ((FeedVH) holder).upvoteBtn.setImageResource(R.drawable.ic_upvoted);
                        ((FeedVH) holder).upvoteBtn.setColorFilter(context.getResources().getColor(R.color.upvoteColor));
                    } else if(feedItem.getUpvoted().equals("no")) {
                        ((FeedVH) holder).upvoteBtn.setImageResource(R.drawable.ic_upvote);
                        ((FeedVH) holder).upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }

                    // Downvote Builder
                    if(feedItem.getDownvoted().equals("yes")) {
                        ((FeedVH) holder).downvoteBtn.setImageResource(R.drawable.ic_downvoted);
                        ((FeedVH) holder).downvoteBtn.setColorFilter(context.getResources().getColor(R.color.downvoteColor));
                    } else if(feedItem.getDownvoted().equals("no")) {
                        ((FeedVH) holder).downvoteBtn.setImageResource(R.drawable.ic_downvote);
                        ((FeedVH) holder).downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }

                    // Like Builder
                    if(feedItem.getLiked().equals("yes")) {
                        ((FeedVH) holder).likeBtn.setImageResource(R.drawable.ic_liked);
                        ((FeedVH) holder).likeBtn.setColorFilter(context.getResources().getColor(R.color.likeColor));
                    } else if(feedItem.getLiked().equals("no")) {
                        ((FeedVH) holder).likeBtn.setImageResource(R.drawable.ic_like);
                        ((FeedVH) holder).likeBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }

                    // Set up Actions
                    //Like
                    ((FeedVH) holder).likeBtn.setOnClickListener(v -> {
                        ObjectAnimator animY = ObjectAnimator.ofFloat(((FeedVH) holder).likeBtn, "translationY", -100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(1);
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
                    ((FeedVH) holder).upvoteBtn.setOnClickListener(v -> {
                        Log.d(TAG, "Upvote of" + position);
                        ObjectAnimator animY = ObjectAnimator.ofFloat(((FeedVH) holder).upvoteBtn, "translationY", -100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(1);
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
                                animD.setRepeatCount(1);
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
                    ((FeedVH) holder).downvoteBtn.setOnClickListener(v -> {
                        ObjectAnimator animY = ObjectAnimator.ofFloat(((FeedVH) holder).downvoteBtn, "translationY", 100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(1);
                        animY.start();
                        //check whether it is liked or unliked
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
                                animD.setRepeatCount(1);
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

                    ((FeedVH) holder).expandBtn.setOnClickListener(v -> {
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
                Log.d(TAG, "onScrollStateChanged: Called " + newState);
                on_attach = false;
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
        Action Helpers
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

    private void add(FeedItem r) {
        feedItems.add(r);
        notifyItemInserted(feedItems.size() - 1);
    }

    void addAll(List<FeedItem> feedItems) {
        for (FeedItem result : feedItems) {
            add(result);
        }
    }

    private void remove(FeedItem r) {
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


    void addLoadingFooter() {
        isLoadingAdded = true;
        add(new FeedItem());
    }

    void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = feedItems.size() - 1;
        FeedItem feedItem = getItem(position);

        if (feedItem != null) {
            feedItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    private FeedItem getItem(int position) {
        return feedItems.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param errorMsg to display if page load fails
     */
    void showRetry(boolean show, @Nullable String errorMsg) {
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
        private CardView dropLayout;
        private ImageView qweeksnap, upvoteBtn, downvoteBtn, expandBtn, likeBtn;
        private RSVideoPlayerStandard video;
        private LinearLayout droptextLayout, droptextTextLayout, dropActions;
        private CircleImageView profilePic;
        private TextView usernameTxt;
        private EmojiTextView dropText, fullnameTxt;
        private RichLinkView url;
        //private LinearLayout qClickLayout;
        private CardView qweeksnapCard;
        private BlobVisualizer blastAudio;
        private ImageView playAudio;

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
            dropLayout = itemView.findViewById(R.id.drop_layout);
            droptextLayout = itemView.findViewById(R.id.droptext_layout);
            droptextTextLayout = itemView.findViewById(R.id.droptext_text_layout);
            dropActions = itemView.findViewById(R.id.drop_actions);
            qweeksnapCard = itemView.findViewById(R.id.drop_qweekSnapCard);
            blastAudio = itemView.findViewById(R.id.blast);
            playAudio = itemView.findViewById(R.id.playAudio);
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

    private Drawable getProgressBarIndeterminate() {
        final int[] attrs = {android.R.attr.indeterminateDrawable};
        final int attrs_indeterminateDrawable_index = 0;
        TypedArray a = context.obtainStyledAttributes(android.R.style.Widget_ProgressBar_Large, attrs);
        try {
            return a.getDrawable(attrs_indeterminateDrawable_index);
        } finally {
            a.recycle();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static String getTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "";

        today = today.length() < 2 ? "0" + today : today;

        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            assert date != null;
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }

}