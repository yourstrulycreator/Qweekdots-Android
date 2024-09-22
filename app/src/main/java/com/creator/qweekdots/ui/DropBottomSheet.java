package com.creator.qweekdots.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.MediaPlayer;
import android.media.audiofx.NoiseSuppressor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.adapter.CommentsAdapter;
import com.creator.qweekdots.api.CommentFeedService;
import com.creator.qweekdots.api.DropService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.mediaplayer.RSVideoPlayer;
import com.creator.qweekdots.mediaplayer.RSVideoPlayerStandard;
import com.creator.qweekdots.models.CommentItem;
import com.creator.qweekdots.models.CommentsModel;
import com.creator.qweekdots.models.Cursor;
import com.creator.qweekdots.models.DropModel;
import com.creator.qweekdots.models.FeedItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.creator.qweekdots.utils.PaginationScrollListener;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.gauravk.audiovisualizer.visualizer.BlobVisualizer;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.ponnamkarthik.richlinkpreview.RichLinkView;
import io.github.ponnamkarthik.richlinkpreview.ViewListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class DropBottomSheet extends RoundedBottomSheetDialogFragment implements PaginationAdapterCallback {
    private final String TAG = DropBottomSheet.class.getSimpleName();

    private String username;
    private String drop_id;

    private DropService dropService;

    private ImageView qweeksnap, upvoteBtn, downvoteBtn, shareBtn, reportBtn, deleteBtn;
    private RSVideoPlayerStandard video;
    private LinearLayout droptextTextLayout, qClickLayout, audioLayout, audioTxtLayout, mediaMeta;
    private CircleImageView profilePic;
    private TextView usernameTxt, upvoteNum, downvoteNum, commentNum, timestamp, spaceTag;
    private EmojiTextView dropText, fullnameTxt, audioDropTxt, dropMedia;
    private RichLinkView url, mediaUrl;
    private CardView qweeksnapCard, reactionCard;
    private BlobVisualizer blastAudio;
    private ImageView playAudio, reactionImage;

    private boolean isPlaying = false;
    private MediaPlayer player;

    private CommentsAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private SpinKitView progressBar;
    private LinearLayout errorLayout;
    private TextView txtError;
    private LinearLayout emptyLayout;

    private EmojiEditText dropCommentTxt;
    private FloatingActionButton dropCommentBtn;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private static final int PAGE_START = 1;
    private static int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    private String max_id;
    //private String since_id;

    private CommentFeedService commentFeedService;
    private Context context;
    private BottomSheetBehavior bottomSheetBehavior;

    private final Random mRandom;
    private final ArrayList<Integer> mBackgroundColors;

    private View view;

    public DropBottomSheet(Context context, String username, String drop_id) {
        this.context = context;
        this.username = username;
        this.drop_id = drop_id;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        view = inflater.inflate(R.layout.drop_bottom_sheet, container, false);

        if(context!=null) {
            // Init Layout
            fullnameTxt = view.findViewById(R.id.fullnameTxt);
            usernameTxt = view.findViewById(R.id.usernameTxt);
            timestamp = view.findViewById(R.id.timestamp);
            dropText = view.findViewById(R.id.txtDrop);
            url = view.findViewById(R.id.txtUrl);
            profilePic = view.findViewById(R.id.profilePic);
            qweeksnap = view.findViewById(R.id.drop_qweekSnap);
            video = view.findViewById(R.id.videoplayer);
            upvoteBtn = view.findViewById(R.id.upvote_btn);
            downvoteBtn = view.findViewById(R.id.downvote_btn);
            upvoteNum = view.findViewById(R.id.upvoteNum);
            downvoteNum = view.findViewById(R.id.downvoteNum);
            commentNum = view.findViewById(R.id.commentNum);
            shareBtn = view.findViewById(R.id.share_btn);
            reportBtn = view.findViewById(R.id.report_btn);
            deleteBtn = view.findViewById(R.id.delete_btn);
            qClickLayout = view.findViewById(R.id.qClickLayout);
            droptextTextLayout = view.findViewById(R.id.droptext_text_layout);
            qweeksnapCard = view.findViewById(R.id.drop_qweekSnapCard);
            blastAudio = view.findViewById(R.id.blast);
            playAudio = view.findViewById(R.id.playAudio);
            audioLayout = view.findViewById(R.id.audioLayout);
            audioTxtLayout = view.findViewById(R.id.audioTxtLayout);
            audioDropTxt = view.findViewById(R.id.txtDrop2);
            reactionCard = view.findViewById(R.id.reactionCard);
            reactionImage = view.findViewById(R.id.reactionImage);
            mediaMeta = view.findViewById(R.id.mediaMeta);
            dropMedia = view.findViewById(R.id.mediaDrop);
            mediaUrl = view.findViewById(R.id.mediaUrl);
            spaceTag = view.findViewById(R.id.space_tag);

            // Setup Comments
            RecyclerView rv = view.findViewById(R.id.main_recycler);
            progressBar = view.findViewById(R.id.spin_kit);
            errorLayout = view.findViewById(R.id.error_layout);
            emptyLayout = view.findViewById(R.id.empty_layout);
            Button btnRetry = view.findViewById(R.id.error_btn_retry);
            txtError = view.findViewById(R.id.error_txt_cause);

            adapter = new CommentsAdapter(context, this, username);

            linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            rv.setLayoutManager(linearLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());

            rv.setAdapter(adapter);

            rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    if(!isLastPage) {
                        isLoading = true;
                        currentPage += 1;

                        if(isNetworkConnected()) {
                            loadNextCommentsPage();
                        } else {
                            Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                            adapter.removeLoadingFooter();

                            Timber.tag(TAG).d("No internet connection available");
                        }
                    }
                }

                @Override
                public int getTotalPageCount() {
                    return TOTAL_PAGES;
                }

                @Override
                public boolean isLastPage() {
                    return isLastPage;
                }

                @Override
                public boolean isLoading() {
                    return isLoading;
                }
            });

            //init service and load data
            dropService = QweekdotsApi.getClient(context).create(DropService.class);
            commentFeedService = QweekdotsApi.getClient(context).create(CommentFeedService.class);

            ImageView closeSheet = view.findViewById(R.id.closeSheet);
            closeSheet.setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                dismiss();
            });

            // init Functions
            loadDrop();
            loadFirstCommentsPage();

            dropCommentTxt = view.findViewById(R.id.dropCommentTxt);
            dropCommentBtn = view.findViewById(R.id.dropCommentBtn);
            dropCommentBtn.setOnClickListener(v -> {
                dropCommentBtn.setClickable(false);

                String drop = dropCommentTxt.getText().toString();
                if(!drop.isEmpty()) {
                    postComment(drop, username, drop_id);
                    Toasty.info(requireContext(), "...making a drop", Toasty.LENGTH_SHORT).show();
                } else {
                    Toasty.error(requireContext(), "Won't you say something nice ?", Toast.LENGTH_SHORT).show();
                    dropCommentBtn.setClickable(true);
                }
            });

            btnRetry.setOnClickListener(v -> loadFirstCommentsPage());
        }

        return view;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        //inflating layout
        view = View.inflate(getContext(), R.layout.drop_bottom_sheet, null);

        View extraSpace = view.findViewById(R.id.extraSpace);
        //setting layout with bottom sheet
        bottomSheet.setContentView(view);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        //setting Peek
        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
        //setting min height of bottom sheet
        extraSpace.setMinimumHeight((Resources.getSystem().getDisplayMetrics().heightPixels) / 2);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (BottomSheetBehavior.STATE_EXPANDED == i) {
                    bottomSheetBehavior.setDraggable(false);
                }
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss();
                }
            }
            @Override
            public void onSlide(@NonNull View view, float v) {
            }
        });

        return bottomSheet;
    }

    /**
     * Load Drop Content
     */
    private void loadDrop() {
        callDropApi().enqueue(new Callback<DropModel>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call<DropModel> call, @NotNull Response<DropModel> response) {
                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                List<FeedItem> feedItem = fetchDrop(response);
                FeedItem drop = feedItem.get(0);

                // Set fullname
                fullnameTxt.setText(drop.getFullname());
                // Set username
                usernameTxt.setText("q/" + drop.getUsername());

                //Set time ago
                timestamp.setText(drop.getTimeStamp());

                int randomColor = mBackgroundColors.get(mRandom.nextInt(15));

                Drawable background = spaceTag.getBackground();
                if (background instanceof ShapeDrawable) {
                    ((ShapeDrawable)background).getPaint().setColor(ContextCompat.getColor(context, randomColor));
                } else if (background instanceof GradientDrawable) {
                    ((GradientDrawable)background).setColor(ContextCompat.getColor(context, randomColor));
                } else if (background instanceof ColorDrawable) {
                    ((ColorDrawable)background).setColor(ContextCompat.getColor(context, randomColor));
                }
                String spacename = "s/"+drop.getSpace();
                spaceTag.setText(spacename);

                // Click to view profile
                qClickLayout.setOnClickListener(v-> {
                    Intent i = new Intent(context, ProfileActivity.class);
                    i.putExtra("profile", drop.getUsername());
                    context.startActivity(i);
                    customType(context, "fadein-to-fadeout");
                });

                //Build layout depending on type
                if(drop.getHasMedia() == 1) {
                    //Set up qweeksnap if drop hasMedia
                    switch (drop.getType()) {
                        case "qweekpic":
                            droptextTextLayout.setVisibility(View.GONE);
                            qweeksnapCard.setVisibility(View.VISIBLE);

                            qweeksnap.setVisibility(View.VISIBLE);
                            video.setVisibility(View.GONE);

                            RequestOptions requestOptions = new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                                    .skipMemoryCache(true);
                            Glide
                                    .with(context)
                                    .load(drop.getQweekSnap())
                                    .override(Target.SIZE_ORIGINAL)
                                    .thumbnail(0.3f)
                                    .apply(requestOptions)
                                    .into(qweeksnap);

                            /*
                            List<String> images = Collections.singletonList(drop.getQweekSnap());
                            qweeksnap.setOnClickListener(v-> new StfalconImageViewer.Builder<>(context, images, (imageView, imageUrl) -> Glide.with(context).load(imageUrl).into(imageView))
                                    .withTransitionFrom(qweeksnap)
                                    .withBackgroundColor(context.getResources().getColor(R.color.black))
                                    .show());

                             */

                            // Check for empty status message
                            if (!TextUtils.isEmpty(drop.getDrop())) {
                                SpannableString hashText = new SpannableString(drop.getDrop());
                                Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                                while (matcher.find()) {
                                    hashText.setSpan(new BackgroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
                                }
                                //if none set text and make it visible
                                mediaMeta.setVisibility(View.VISIBLE);
                                dropMedia.setText(hashText);
                                dropMedia.setVisibility(View.VISIBLE);
                            } else {
                                // status is empty, remove from view
                                mediaMeta.setVisibility(View.GONE);
                                dropMedia.setVisibility(View.GONE);
                            }

                            // Checking for null feed url
                            if (drop.getHasLink() == 1) {
                                mediaUrl.setLink(drop.getLink(), new ViewListener() {
                                    @Override
                                    public void onSuccess(boolean status) {}
                                    @Override
                                    public void onError(Exception e) {}
                                });
                                mediaUrl.setVisibility(View.VISIBLE);
                            } else {
                                // url is null, remove from the view
                                mediaUrl.setVisibility(View.GONE);
                            }
                            break;
                        case "qweekvid":
                            droptextTextLayout.setVisibility(View.GONE);
                            qweeksnapCard.setVisibility(View.VISIBLE);

                            video.setVisibility(View.VISIBLE);
                            qweeksnap.setVisibility(View.GONE);

                            // qweekvid
                            video.setUp(drop.getQweekSnap(),
                                    RSVideoPlayer.SCREEN_LAYOUT_LIST);
                            video.setThumbImageView(drop.getQweekSnap());

                            // Check for empty status message
                            if (!TextUtils.isEmpty(drop.getDrop())) {
                                SpannableString hashText = new SpannableString(drop.getDrop());
                                Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                                while (matcher.find()) {
                                    hashText.setSpan(new BackgroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
                                }
                                //if none set text and make it visible
                                mediaMeta.setVisibility(View.VISIBLE);
                                dropMedia.setText(hashText);
                                dropMedia.setVisibility(View.VISIBLE);
                            } else {
                                // status is empty, remove from view
                                mediaMeta.setVisibility(View.GONE);
                                dropMedia.setVisibility(View.GONE);
                            }

                            // Checking for null feed url
                            if (drop.getHasLink() == 1) {
                                mediaUrl.setLink(drop.getLink(), new ViewListener() {
                                    @Override
                                    public void onSuccess(boolean status) {}
                                    @Override
                                    public void onError(Exception e) {}
                                });
                                mediaUrl.setVisibility(View.VISIBLE);
                            } else {
                                // url is null, remove from the view
                                mediaUrl.setVisibility(View.GONE);
                            }
                            break;
                        case "audio":
                            droptextTextLayout.setVisibility(View.GONE);
                            qweeksnapCard.setVisibility(View.VISIBLE);

                            playAudio.setVisibility(View.VISIBLE);
                            qweeksnap.setVisibility(View.GONE);
                            video.setVisibility(View.GONE);
                            blastAudio.setVisibility(View.VISIBLE);

                            audioLayout.setVisibility(View.VISIBLE);
                            playAudio.setVisibility(View.VISIBLE);
                            audioTxtLayout.setVisibility(View.VISIBLE);

                            qweeksnap.setVisibility(View.GONE);
                            video.setVisibility(View.GONE);

                            blastAudio.setVisibility(View.GONE);

                            // Set The Drop for Audio
                            SpannableString hashText = new SpannableString(drop.getDrop());
                            Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                            while (matcher.find()) {
                                hashText.setSpan(new BackgroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
                            }
                            audioDropTxt.setText(hashText);

                            playAudio.setOnClickListener(v-> {
                                playAudio.setVisibility(View.GONE);
                                blastAudio.setVisibility(View.VISIBLE);

                                if(isPlaying) {
                                    player.stop();
                                    isPlaying = false;
                                } else {
                                    player = MediaPlayer.create(context, Uri.parse(drop.getAudio()));
                                    player.setOnPreparedListener(mp -> {
                                        mp.start();
                                        isPlaying = true;
                                        blastAudio.show();
                                        blastAudio.setAudioSessionId(mp.getAudioSessionId());
                                        NoiseSuppressor.create(mp.getAudioSessionId());
                                    });
                                    player.setOnCompletionListener(mp -> {
                                        blastAudio.hide();
                                        blastAudio.release();
                                        blastAudio.setVisibility(View.GONE);
                                        playAudio.setVisibility(View.VISIBLE);
                                        isPlaying = false;
                                    });
                                }
                            });

                            blastAudio.setOnClickListener(v-> {
                                if(isPlaying) {
                                    player.stop();
                                    isPlaying = false;
                                } else {
                                    player = MediaPlayer.create(context, Uri.parse(drop.getAudio()));
                                    player.setOnPreparedListener(mp -> {
                                        mp.start();
                                        isPlaying = true;
                                        blastAudio.show();
                                        blastAudio.setAudioSessionId(mp.getAudioSessionId());
                                        NoiseSuppressor.create(mp.getAudioSessionId());
                                    });
                                    player.setOnCompletionListener(mp -> {
                                        blastAudio.hide();
                                        blastAudio.release();
                                        blastAudio.setVisibility(View.GONE);
                                        playAudio.setVisibility(View.VISIBLE);
                                        isPlaying = false;
                                    });
                                }
                            });

                            break;
                        case "reaction":
                            // Drop has media but Different Layout design
                            droptextTextLayout.setVisibility(View.VISIBLE);
                            // QweekSnap Card is not in use for Reactions
                            qweeksnapCard.setVisibility(View.GONE);

                            // Reaction Card is used instead
                            reactionCard.setVisibility(View.VISIBLE);
                            // Set Reaction GIF
                            Glide
                                    .with(context)
                                    .asGif()
                                    .load(drop.getQweekSnap())
                                    .override(Target.SIZE_ORIGINAL)
                                    .thumbnail(0.3f)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(reactionImage);


                            /*
                            List<String> images2 = Collections.singletonList(drop.getQweekSnap());
                            reactionImage.setOnClickListener(v-> new StfalconImageViewer.Builder<>(context, images2, (imageView, imageUrl) -> Glide.with(context).asGif().load(imageUrl).into(imageView))
                                    .withTransitionFrom(reactionImage)
                                    .withBackgroundColor(context.getResources().getColor(R.color.black))
                                    .show());

                             */

                            break;
                    }
                } else {
                    droptextTextLayout.setVisibility(View.VISIBLE);
                    qweeksnapCard.setVisibility(View.GONE);
                    qweeksnap.setVisibility(View.GONE);
                    video.setVisibility(View.GONE);
                }

                if(drop.getType().equals("drop")) {
                    // Check for empty status message
                    if (!TextUtils.isEmpty(drop.getDrop())) {
                        SpannableString hashText = new SpannableString(drop.getDrop());
                        Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                        while (matcher.find()) {
                            hashText.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.QweekThemeColor)), matcher.start(), matcher.end(), 0);
                        }
                        //if none set text and make it visible
                        dropText.setText(hashText);
                        dropText.setVisibility(View.VISIBLE);
                    } else {
                        // status is empty, remove from view
                        dropText.setVisibility(View.GONE);
                    }

                    // Checking for null feed url
                    if (drop.getHasLink() == 1) {
                        url.setLink(drop.getLink(), new ViewListener() {

                            @Override
                            public void onSuccess(boolean status) {

                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                        url.setVisibility(View.VISIBLE);
                    } else {
                        // url is null, remove from the view
                        url.setVisibility(View.GONE);
                    }
                }

                // load Drop Profile Avatar
                /*Picasso.get()
                        .load(drop.getProfilePic())
                        .resize(40, 40)
                        .placeholder(R.drawable.ic_alien)
                        .error(R.drawable.ic_alien)
                        .centerCrop()
                        .into(profilePic);*/

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                        .skipMemoryCache(true);
                Drawable placeholder = getTinted(context.getResources().getColor(R.color.contentTextColor));
                Glide
                        .with(context)
                        .load(drop.getProfilePic())
                        .override(40, 40)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .thumbnail(0.3f)
                        .apply(requestOptions)
                        .into(profilePic);

                // Build Drop Actions

                // Upvote Builder
                /*
                 * If drop is upvoted, set resource to upvoted along with resource color
                 * else, resource is not upvoted yet
                 */
                if(drop.getUpvoted().equals("yes")) {
                    upvoteBtn.setImageResource(R.drawable.ic_upvoted);
                    upvoteBtn.setColorFilter(context.getResources().getColor(R.color.upvoteColor));
                } else if(drop.getUpvoted().equals("no")) {
                    upvoteBtn.setImageResource(R.drawable.ic_upvote);
                    upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                }

                // Downvote Builder
                /*
                 * If drop is downvoted, set resource to downvoted along with resource color
                 * else, resource is not downvoted yet
                 */
                if(drop.getDownvoted().equals("yes")) {
                    downvoteBtn.setImageResource(R.drawable.ic_downvoted);
                    downvoteBtn.setColorFilter(context.getResources().getColor(R.color.downvoteColor));
                } else if(drop.getDownvoted().equals("no")) {
                    downvoteBtn.setImageResource(R.drawable.ic_downvote);
                    downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                }

                // Like Builder
                /*
                 * If drop is liked, set resource to liked long with resource color
                 * else, resource is not liked yet
                 *
                if(drop.getLiked().equals("yes")) {
                    likeBtn.setImageResource(R.drawable.ic_liked);
                    likeBtn.setColorFilter(context.getResources().getColor(R.color.likeColor));
                } else if(drop.getLiked().equals("no")) {
                    likeBtn.setImageResource(R.drawable.ic_like);
                    likeBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                }

                 */

                //Action Numbers
                /*if(feedItem.getLikedNum().equals("0")) {
                        ((FeedVH) holder).likeNum.setText("");
                    } else {
                        ((FeedVH) holder).likeNum.setText(feedItem.getLikedNum());
                    }*/
                if(drop.getUpvoteNum().equals("0")) {
                    upvoteNum.setText("");
                } else {
                    upvoteNum.setText(drop.getUpvoteNum());
                }
                if(drop.getDownvoteNum().equals("0")) {
                    downvoteNum.setText("");
                } else {
                    downvoteNum.setText(drop.getDownvoteNum());
                }

                //Report Number
                if(drop.getUsername().equals(username)) {
                    reportBtn.setVisibility(View.GONE);
                } else {
                    reportBtn.setVisibility(View.VISIBLE);

                    reportBtn.setOnClickListener(v->{
                        reportDrop(drop.getDrop_Id());
                    });
                }

                // Delete Button
                if(drop.getUsername().equals(username)) {
                    deleteBtn.setVisibility(View.VISIBLE);

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Confirm Delete...");

                    // Setting Dialog Message
                    alertDialog.setMessage("Are you sure you want delete this message?");

                    // Setting Icon to Dialog
                    alertDialog.setIcon(R.drawable.ic_delete);

                    // Setting Positive "Yes" Btn
                    alertDialog.setPositiveButton("YES",
                            (dialog, which) -> {
                                Toasty.info(context, "deleting...", Toasty.LENGTH_SHORT).show();
                                deleteDrop(drop.getDrop_Id(), drop.getUsername());
                            });
                    // Setting Negative "NO" Btn
                    alertDialog.setNegativeButton("NO",
                            (dialog, which) -> dialog.cancel());

                    deleteBtn.setOnClickListener(v->{
                        ObjectAnimator animY = ObjectAnimator.ofFloat(deleteBtn, "translationY", -100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(0);
                        animY.start();
                        alertDialog.show();
                    });
                } else {
                    deleteBtn.setVisibility(View.GONE);
                }

                //Comments Num
                if(drop.getCommentNum().equals("0")) {}
                else if(drop.getCommentNum().equals("1")) {
                    commentNum.setText(drop.getCommentNum() + " comment");
                } else {
                    commentNum.setText(drop.getCommentNum() + " comments");
                }

                // Set up Actions

                //Like
                /*
                 * On click, first check if user liked already
                 * If user liked already, unlike and set resource to pre-liked state
                 * else, like along with resource change
                 *
                likeBtn.setOnClickListener(v -> {
                    ObjectAnimator animY = ObjectAnimator.ofFloat(likeBtn, "translationY", -100f, 0f);
                    animY.setDuration(1000);//1sec
                    animY.setInterpolator(new BounceInterpolator());
                    animY.setRepeatCount(0);
                    animY.start();
                    //check whether it is liked or unliked
                    if (drop.getLiked().equals("yes")) {
                        //isliked, unlike
                        //Toasty.info(context, "taking back like...", Toasty.LENGTH_SHORT).show();

                        drop.setLiked("no");
                        likeBtn.setImageResource(R.drawable.ic_like);
                        likeBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                        doLike("unlike", drop.getDrop_Id(), username, drop.getUsername());
                    } else {
                        //like
                        //Toasty.info(context, "liking...", Toasty.LENGTH_SHORT).show();

                        drop.setLiked("yes");
                        likeBtn.setImageResource(R.drawable.ic_liked);
                        likeBtn.setColorFilter(context.getResources().getColor(R.color.likeColor));
                        doLike("like", drop.getDrop_Id(), username, drop.getUsername());
                    }
                });

                 */

                //Upvote
                /*
                 * On click, first check if user upvoted already
                 * If user upvoted already, undo upvote and set resource to pre-upvote state
                 * else, upvote along with resource change
                 */
                upvoteBtn.setOnClickListener(v -> {
                    ObjectAnimator animY = ObjectAnimator.ofFloat(upvoteBtn, "translationY", -100f, 0f);
                    animY.setDuration(1000);//1sec
                    animY.setInterpolator(new BounceInterpolator());
                    animY.setRepeatCount(0);
                    animY.start();
                    //check whether it is liked or unliked
                    if (drop.getUpvoted().equals("yes")) {
                        //un-upvote
                        //Toasty.info(context, "taking back upvote...", Toasty.LENGTH_SHORT).show();

                        drop.setUpvoted("no");
                        upvoteBtn.setImageResource(R.drawable.ic_upvote);
                        upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                        doUpvote("undo", drop.getDrop_Id(), username, drop.getUsername());
                    } else {
                        //upvote
                        //Toasty.info(context, "upvoting...", Toasty.LENGTH_SHORT).show();

                        drop.setUpvoted("yes");
                        //run downvote check and undo if downvoted
                        if(drop.getDownvoted().equals("yes")) {
                            ObjectAnimator animD = ObjectAnimator.ofFloat(downvoteBtn, "translationY", 100f, 0f);
                            animD.setDuration(1000);//1sec
                            animD.setInterpolator(new BounceInterpolator());
                            animD.setRepeatCount(0);
                            animD.start();
                            downvoteBtn.setImageResource(R.drawable.ic_downvote);
                            downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                            drop.setDownvoted("no");
                            doDownvote("undo", drop.getDrop_Id(), username, drop.getUsername());
                        }
                        upvoteBtn.setImageResource(R.drawable.ic_upvoted);
                        upvoteBtn.setColorFilter(context.getResources().getColor(R.color.upvoteColor));
                        doUpvote("upvote", drop.getDrop_Id(), username, drop.getUsername());
                    }
                });

                //Downvote
                /*
                 * On click, first check if user downvoted already
                 * If user downvoted already, undo downvote and set resource to pre-downvote state
                 * else, downvote along with resource
                 */
                downvoteBtn.setOnClickListener(v -> {
                    ObjectAnimator animY = ObjectAnimator.ofFloat(downvoteBtn, "translationY", 100f, 0f);
                    animY.setDuration(1000);//1sec
                    animY.setInterpolator(new BounceInterpolator());
                    animY.setRepeatCount(0);
                    animY.start();
                    //check whether it is liked or unliked
                    if (drop.getDownvoted().equals("yes")) {
                        //un-do
                        //Toasty.info(context, "taking back downvote...", Toasty.LENGTH_SHORT).show();

                        drop.setDownvoted("no");
                        downvoteBtn.setImageResource(R.drawable.ic_downvote);
                        doDownvote("undo", drop.getDrop_Id(), username, drop.getUsername());
                    } else {
                        //downvote
                        //Toasty.info(context, "downvoting...", Toasty.LENGTH_SHORT).show();

                        drop.setDownvoted("yes");
                        //run upvote check and undo if upvoted
                        if(drop.getUpvoted().equals("yes")) {
                            ObjectAnimator animD = ObjectAnimator.ofFloat(upvoteBtn, "translationY", -100f, 0f);
                            animD.setDuration(1000);//1sec
                            animD.setInterpolator(new BounceInterpolator());
                            animD.setRepeatCount(0);
                            animD.start();
                            upvoteBtn.setImageResource(R.drawable.ic_upvote);
                            upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                            drop.setUpvoted("no");
                            doUpvote("undo", drop.getDrop_Id(), username, drop.getUsername());
                        }
                        downvoteBtn.setImageResource(R.drawable.ic_downvoted);
                        downvoteBtn.setColorFilter(context.getResources().getColor(R.color.downvoteColor));
                        doDownvote("downvote", drop.getDrop_Id(), username, drop.getUsername());
                    }
                });

                shareBtn.setOnClickListener(v->{
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this drop on Qweekdots 😀");
                    String shareMessage= "\nHey 👋, Join Qweekdots to view this drop and lots more 👍, Chat 😉, Bring your friends along 👽. It's Free! 🤩 \n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=com.creator.qweekdots";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    context.startActivity(Intent.createChooser(shareIntent, "Choose One"));
                });
            }

            @Override
            public void onFailure(@NotNull Call<DropModel> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
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

    /**
     * @param response extracts List<{@link DropModel >} from response
     */
    private List<FeedItem> fetchDrop(Response<DropModel> response) {
        DropModel dropModel = response.body();
        assert dropModel != null;
        return dropModel.getDropItems();
    }

    /**
     * Performs a Retrofit call to the first QweekFeed API.
     */
    private Call<DropModel> callDropApi() {
        return  dropService.getDropData(
                drop_id,
                username
        );
    }

    /*
     * Like Drop
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
                    //String sent = jObj.getString("sent");
                    //Toasty.success(context, sent, Toast.LENGTH_LONG).show();
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

    /*
     * Report Drop
     */
    private void reportDrop(final String drop_id) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","theqweekcompany@gmail.com", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, "REPORT this Drop with the id - "+ drop_id);
        intent.putExtra(Intent.EXTRA_TEXT, "I'd like to report this drop for the following reasons: \n\n\nState your reasons and we'll get back to you with a definite action pending an investigation.");
        startActivity(Intent.createChooser(intent, "Choose an Email client :"));
    }

    /*
     * Upvote Drop
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
                    //String sent = jObj.getString("sent");
                    //Toasty.success(context, sent, Toast.LENGTH_LONG).show();
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

    /*
     * Downvote Drop
     */
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
                    //String sent = jObj.getString("sent");
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
     * Delete Drop
     */
    private void deleteDrop(final String drop_id, String username) {
        // Tag used to cancel the request
        String tag_string_req = "req_delete";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DELETE, response -> {
            Timber.tag(TAG).d("Delete Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    String sent = jObj.getString("sent");
                    Toasty.success(context, sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_SHORT).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Delete Error: %s", error.getMessage());
            Toasty.error(context,
                    "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("id", drop_id);
                params.put("u", username);
                params.put("type", "drop");

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

    /**
     * Drop Comment
     * */
    private void postComment(final String drop, final String username, final String drop_id) {
        // Tag used to cancel the request
        String tag_string_req = "req_post";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_COMMENT_TEXT, response -> {
            Timber.tag(TAG).d("Drop Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Stop animation
                    dropCommentBtn.setClickable(true);
                    dropCommentTxt.setText("");
                    dropCommentTxt.clearFocus();

                    // success
                    String sent = jObj.getString("sent");

                    adapter.notifyDataSetChanged();

                    Toasty.success(requireContext(), sent, Toast.LENGTH_SHORT).show();
                } else {
                    // Error in drop. Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Toasty.error(requireContext(),
                            errorMsg, Toast.LENGTH_SHORT).show();
                    dropCommentBtn.setClickable(true);
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                dropCommentBtn.setClickable(true);
            }

        }, error -> {
            Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
            Toasty.error(requireContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();
            dropCommentBtn.setClickable(true);
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to drop url
                Map<String, String> params = new HashMap<>();
                params.put("drop", drop);
                params.put("username", username);
                params.put("drop_id", drop_id);
                params.put("parent_id", "");

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

    /**
     * Comments Load Function
     */
    private void loadFirstCommentsPage() {
        Timber.tag(TAG).d("loadFirstCommentsPage: ");

        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();

        callCommentsFeedApi().enqueue(new Callback<CommentsModel>() {
            @Override
            public void onResponse(@NotNull Call<CommentsModel> call, @NotNull Response<CommentsModel> response) {
                hideErrorView();

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                List<CommentItem> feedItem = fetchCommentsFeed(response);

                if(feedItem.isEmpty()) {
                    showEmptyView();
                } else {

                    progressBar.setVisibility(View.GONE);
                    adapter.addAll(feedItem);

                    // Cursor Links
                    List<Cursor> cursor = fetchCursorLinks(response);
                    Cursor cursorLink = cursor.get(0);
                    max_id = cursorLink.getMaxID();
                    //since_id = cursorLink.getSinceID();

                    TOTAL_PAGES = cursorLink.getPagesNum();

                    if(TOTAL_PAGES == 1) {
                        isLastPage = true;
                    } else {
                        if (currentPage < TOTAL_PAGES) {
                            adapter.addLoadingFooter();
                        } else if(currentPage == TOTAL_PAGES) {
                            isLastPage = true;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<CommentsModel> call, @NotNull Throwable t) {
                t.printStackTrace();
                showErrorView();
            }
        });
    }

    /**
     * @param response extracts List<{@link FeedItem>} from response
     */
    private List<CommentItem> fetchCommentsFeed(Response<CommentsModel> response) {
        CommentsModel newsFeed = response.body();
        assert newsFeed != null;
        return newsFeed.getFeedItems();
    }

    /**
     * @param response extracts List<{@link Cursor>} from response
     */
    private List<Cursor> fetchCursorLinks(Response<CommentsModel> response) {
        CommentsModel newsFeed = response.body();
        assert newsFeed != null;
        return newsFeed.getCursorLinks();
    }

    /*
     * Load Next Set of Comments
     */
    private void loadNextCommentsPage() {
        Timber.tag(TAG).d("loadNextCommentsPage: %s", currentPage);

        callNextCommentsFeedApi().enqueue(new Callback<CommentsModel>() {
            @Override
            public void onResponse(@NotNull Call<CommentsModel> call, @NotNull Response<CommentsModel> response) {
                adapter.removeLoadingFooter();
                isLoading = false;

                List<CommentItem> feedItems = fetchCommentsFeed(response);
                adapter.addAll(feedItems);

                // Cursor Links
                List<Cursor> cursor = fetchCursorLinks(response);
                Cursor cursorLink = cursor.get(0);
                max_id = cursorLink.getMaxID();
                //since_id = cursorLink.getSinceID();

                if (currentPage != TOTAL_PAGES) {
                    adapter.addLoadingFooter();
                } else {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(@NotNull Call<CommentsModel> call, @NotNull Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    /**
     * Performs a Retrofit call to the first QweekFeed API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<CommentsModel> callCommentsFeedApi() {
        return commentFeedService.getComments(
                username,
                drop_id,
                null,
                null
        );
    }

    /**
     * Performs a Retrofit call to the next QweekFeed API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<CommentsModel> callNextCommentsFeedApi() {
        return commentFeedService.getComments(
                username,
                drop_id,
                max_id,
                null
        );
    }

    /**
     * Performs a Retrofit call to the next QweekFeed API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.

    private Call<CommentsModel> callPrevCommentsFeedApi() {
        return commentFeedService.getComments(
                username,
                drop_id,
                null,
                since_id
        );
    }
     */

    public void retryPageLoad() {
        loadNextCommentsPage();
    }

    /*
     */
    private void showErrorView() {
        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(context.getResources().getString(R.string.error_msg_unknown));
        }
    }

    private void showEmptyView() {
        if (emptyLayout.getVisibility() == View.GONE) {
            emptyLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * @param throwable to identify the type of error
     * @return appropriate error message
     */
    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = context.getResources().getString(R.string.error_msg_unknown);
        if (!isNetworkConnected()) {
            errorMsg = context.getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = context.getResources().getString(R.string.error_msg_timeout);
        }
        return errorMsg;
    }

    // Helpers -------------------------------------------------------------------------------------
    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Remember to add android.permission.ACCESS_NETWORK_STATE permission.
     *
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onStart() {
        super.onStart();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
