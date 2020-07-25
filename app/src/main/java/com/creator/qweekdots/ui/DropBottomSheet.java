package com.creator.qweekdots.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.audiofx.NoiseSuppressor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.api.CommentFeedService;
import com.creator.qweekdots.api.DropService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private SQLiteHandler db;
    private SessionManager session;

    private String username;
    private String drop_id;

    private DropService dropService;

    private RelativeLayout dropLayout;
    private ImageView qweeksnap, upvoteBtn, downvoteBtn, shareBtn, likeBtn, deleteBtn;
    private RSVideoPlayerStandard video;
    private LinearLayout droptextLayout, droptextTextLayout, dropActions, qClickLayout;
    private CircleImageView profilePic;
    private TextView usernameTxt, timeStamp, likeNum, upvoteNum, downvoteNum, commentNum;
    private EmojiTextView dropText, fullnameTxt;
    private RichLinkView url;
    private CardView qweeksnapCard;
    private BlobVisualizer blastAudio;
    private ImageView playAudio;

    private boolean isPlaying = false;
    private MediaPlayer player;

    private RecyclerView rv;
    private CommentsAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private SpinKitView progressBar;
    private LinearLayout errorLayout;
    private Button btnRetry;
    private TextView txtError;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout emptyLayout;
    private Target target;

    private EmojiEditText dropCommentTxt;
    private FloatingActionButton dropCommentBtn;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private static final int PAGE_START = 1;
    private static int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    private String next_link;
    private String prev_link;
    private String max_id;
    private String since_id;

    private CommentFeedService commentFeedService;
    private Context context;
    private BottomSheetBehavior bottomSheetBehavior;

    private View view;

    DropBottomSheet(Context context, String username, String drop_id) {
        this.context = context;
        this.username = username;
        this.drop_id = drop_id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        view = inflater.inflate(R.layout.drop_bottom_sheet, container, false);

        if(context!=null) {
            fullnameTxt = view.findViewById(R.id.fullnameTxt);
            usernameTxt = view.findViewById(R.id.usernameTxt);
            timeStamp = view.findViewById(R.id.timestamp);
            dropText = view.findViewById(R.id.txtDrop);
            url = view.findViewById(R.id.txtUrl);
            profilePic = view.findViewById(R.id.profilePic);
            qweeksnap = view.findViewById(R.id.drop_qweekSnap);
            video = view.findViewById(R.id.videoplayer);
            upvoteBtn = view.findViewById(R.id.upvote_btn);
            downvoteBtn = view.findViewById(R.id.downvote_btn);
            shareBtn = view.findViewById(R.id.share_btn);
            likeBtn = view.findViewById(R.id.like_btn);
            likeNum = view.findViewById(R.id.likeNum);
            upvoteNum = view.findViewById(R.id.upvoteNum);
            downvoteNum = view.findViewById(R.id.downvoteNum);
            commentNum = view.findViewById(R.id.commentNum);
            deleteBtn = view.findViewById(R.id.delete_btn);
            qClickLayout = view.findViewById(R.id.qClickLayout);
            dropLayout = view.findViewById(R.id.drop_relayout);
            droptextLayout = view.findViewById(R.id.droptext_layout);
            droptextTextLayout = view.findViewById(R.id.droptext_text_layout);
            dropActions = view.findViewById(R.id.drop_actions);
            qweeksnapCard = view.findViewById(R.id.drop_qweekSnapCard);
            blastAudio = view.findViewById(R.id.blast);
            playAudio = view.findViewById(R.id.playAudio);

            //COMMENTS
            rv = view.findViewById(R.id.main_recycler);
            progressBar = view.findViewById(R.id.spin_kit);
            errorLayout = view.findViewById(R.id.error_layout);
            emptyLayout = view.findViewById(R.id.empty_layout);
            btnRetry = view.findViewById(R.id.error_btn_retry);
            txtError = view.findViewById(R.id.error_txt_cause);
            swipeRefreshLayout = view.findViewById(R.id.main_swiperefresh);

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

                }
                if (BottomSheetBehavior.STATE_COLLAPSED == i) {

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

                // Converting timestamp into x ago format

                qClickLayout.setOnClickListener(v-> {
                    Intent i = new Intent(context, ProfileActivity.class);
                    i.putExtra("profile", drop.getUsername());
                    context.startActivity(i);
                    customType(context, "up-to-bottom");
                });

                //Build layout depending on type
                if(drop.getHasMedia() == 1) {

                    droptextTextLayout.setVisibility(View.GONE);

                    qweeksnapCard.setVisibility(View.VISIBLE);

                    //Set up qweeksnap if drop hasMedia
                    switch (drop.getType()) {
                        case "qweekpic":
                            qweeksnap.setVisibility(View.VISIBLE);
                            video.setVisibility(View.GONE);

                            //qweeksnap
                            /*
                            target = new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    //int width = bitmap.getWidth();
                                    int height = bitmap.getHeight();
                                    qweeksnap.setImageBitmap(bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    qweeksnap.requestLayout();
                                    qweeksnap.getLayoutParams().height = 600;
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            };

                            qweeksnap.setTag(target);
                            Picasso.get().load(drop.getQweekSnap()).into(target);
                            dropLayout.getLayoutParams();
                            *
                             */

                            RequestOptions requestOptions = new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                                    .skipMemoryCache(true);
                            Glide
                                    .with(context)
                                    .load(drop.getQweekSnap())
                                    .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                                    .apply(requestOptions)
                                    .into(qweeksnap);
                            break;
                        case "qweekvid":
                            video.setVisibility(View.VISIBLE);
                            qweeksnap.setVisibility(View.GONE);

                            // qweekvid
                            video.setUp(drop.getQweekSnap(),
                                    RSVideoPlayer.SCREEN_LAYOUT_LIST);
                            video.setThumbImageView(drop.getQweekSnap());

                            break;
                        case "audio":
                            playAudio.setVisibility(View.VISIBLE);
                            qweeksnap.setVisibility(View.GONE);
                            video.setVisibility(View.GONE);
                            blastAudio.setVisibility(View.VISIBLE);

                            playAudio.setOnClickListener(v-> {
                                playAudio.setVisibility(View.GONE);

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
                    }
                } else {
                    droptextTextLayout.setVisibility(View.VISIBLE);

                    qweeksnapCard.setVisibility(View.GONE);
                    qweeksnap.setVisibility(View.GONE);
                    video.setVisibility(View.GONE);
                }

                // Check for empty status message
                if (!TextUtils.isEmpty(drop.getDrop())) {

                    SpannableString hashText = new SpannableString(drop.getDrop());
                    Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                    while (matcher.find())
                    {
                        hashText.setSpan(new BackgroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
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

                // load User ProfileModel Picture
                Picasso.get()
                        .load(drop.getProfilePic())
                        .resize(40, 40)
                        .placeholder(R.drawable.ic_alien)
                        .error(R.drawable.ic_alien)
                        .centerCrop()
                        .into(profilePic);

                // Build Drop Actions

                // Text Upvote Button
                if(drop.getUpvoted().equals("yes")) {
                    upvoteBtn.setImageResource(R.drawable.ic_upvoted);
                    upvoteBtn.setColorFilter(context.getResources().getColor(R.color.upvoteColor));
                } else if(drop.getUpvoted().equals("no")) {
                    upvoteBtn.setImageResource(R.drawable.ic_upvote);
                    upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                }

                // Text Downvote Button
                if(drop.getDownvoted().equals("yes")) {
                    downvoteBtn.setImageResource(R.drawable.ic_downvoted);
                    downvoteBtn.setColorFilter(context.getResources().getColor(R.color.downvoteColor));
                } else if(drop.getDownvoted().equals("no")) {
                    downvoteBtn.setImageResource(R.drawable.ic_downvote);
                    downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                }

                // Text Like Button
                if(drop.getLiked().equals("yes")) {
                    likeBtn.setImageResource(R.drawable.ic_liked);
                    likeBtn.setColorFilter(context.getResources().getColor(R.color.likeColor));
                } else if(drop.getLiked().equals("no")) {
                    likeBtn.setImageResource(R.drawable.ic_like);
                    likeBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                }

                //Action Numbers
                if(drop.getLikedNum().equals("0")) {
                    likeNum.setText("");
                } else {
                    likeNum.setText(drop.getLikedNum());
                }
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

                // Text Delete Button
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
                            (dialog, which) -> {
                                dialog.cancel();
                            });

                    deleteBtn.setOnClickListener(v->{
                        ObjectAnimator animY = ObjectAnimator.ofFloat(deleteBtn, "translationY", -100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(1);
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
                likeBtn.setOnClickListener(v -> {
                    ObjectAnimator animY = ObjectAnimator.ofFloat(likeBtn, "translationY", -100f, 0f);
                    animY.setDuration(1000);//1sec
                    animY.setInterpolator(new BounceInterpolator());
                    animY.setRepeatCount(1);
                    animY.start();
                    //check whether it is liked or unliked
                    if (drop.getLiked().equals("yes")) {
                        //isliked, unlike
                        Toasty.info(context, "taking back like...", Toasty.LENGTH_SHORT).show();
                        drop.setLiked("no");
                        likeBtn.setImageResource(R.drawable.ic_like);
                        likeBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                        doLike("unlike", drop.getDrop_Id(), username, drop.getUsername());
                    } else {
                        //like
                        Toasty.info(context, "liking...", Toasty.LENGTH_SHORT).show();
                        drop.setLiked("yes");
                        likeBtn.setImageResource(R.drawable.ic_liked);
                        likeBtn.setColorFilter(context.getResources().getColor(R.color.likeColor));
                        doLike("like", drop.getDrop_Id(), username, drop.getUsername());
                    }
                });

                //Upvote
                upvoteBtn.setOnClickListener(v -> {
                    ObjectAnimator animY = ObjectAnimator.ofFloat(upvoteBtn, "translationY", -100f, 0f);
                    animY.setDuration(1000);//1sec
                    animY.setInterpolator(new BounceInterpolator());
                    animY.setRepeatCount(1);
                    animY.start();
                    //check whether it is liked or unliked
                    if (drop.getUpvoted().equals("yes")) {
                        //un-upvote
                        Toasty.info(context, "taking back upvote...", Toasty.LENGTH_SHORT).show();
                        drop.setUpvoted("no");
                        upvoteBtn.setImageResource(R.drawable.ic_upvote);
                        upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                        doUpvote("undo", drop.getDrop_Id(), username, drop.getUsername());
                    } else {
                        //upvote
                        Toasty.info(context, "upvoting...", Toasty.LENGTH_SHORT).show();
                        drop.setUpvoted("yes");
                        //run downvote check and undo if downvoted
                        if(drop.getDownvoted().equals("yes")) {
                            ObjectAnimator animD = ObjectAnimator.ofFloat(downvoteBtn, "translationY", 100f, 0f);
                            animD.setDuration(1000);//1sec
                            animD.setInterpolator(new BounceInterpolator());
                            animD.setRepeatCount(1);
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
                downvoteBtn.setOnClickListener(v -> {
                    ObjectAnimator animY = ObjectAnimator.ofFloat(downvoteBtn, "translationY", 100f, 0f);
                    animY.setDuration(1000);//1sec
                    animY.setInterpolator(new BounceInterpolator());
                    animY.setRepeatCount(1);
                    animY.start();
                    //check whether it is liked or unliked
                    if (drop.getDownvoted().equals("yes")) {
                        //un-do
                        Toasty.info(context, "taking back downvote...", Toasty.LENGTH_SHORT).show();
                        drop.setDownvoted("no");
                        downvoteBtn.setImageResource(R.drawable.ic_downvote);
                        doDownvote("undo", drop.getDrop_Id(), username, drop.getUsername());
                    } else {
                        //downvote
                        Toasty.info(context, "downvoting...", Toasty.LENGTH_SHORT).show();
                        drop.setDownvoted("yes");
                        //run upvote check and undo if upvoted
                        if(drop.getUpvoted().equals("yes")) {
                            ObjectAnimator animD = ObjectAnimator.ofFloat(upvoteBtn, "translationY", -100f, 0f);
                            animD.setDuration(1000);//1sec
                            animD.setInterpolator(new BounceInterpolator());
                            animD.setRepeatCount(1);
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


            }

            @Override
            public void onFailure(Call<DropModel> call, Throwable t) {
                t.printStackTrace();
            }
        });
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
                    String sent = jObj.getString("sent");

                    Toasty.success(context, sent, Toast.LENGTH_LONG).show();
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
                    String sent = jObj.getString("sent");
                    Toasty.success(context, sent, Toast.LENGTH_LONG).show();
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
                    String sent = jObj.getString("sent");
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
     * function to post drop text
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


    /*
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
                    next_link = cursorLink.getNextLink();
                    prev_link = cursorLink.getPrevLink();
                    max_id = cursorLink.getMaxID();
                    since_id = cursorLink.getSinceID();

                    TOTAL_PAGES = cursorLink.getPagesNum();
                    Log.d(TAG, String.valueOf(TOTAL_PAGES));

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
            public void onFailure(Call<CommentsModel> call, Throwable t) {
                t.printStackTrace();
                showErrorView();
            }
        });
    }

    /**
     * Triggers the actual background refresh via the {@link SwipeRefreshLayout}
     */
    private void doRefresh() {
        if(isNetworkConnected()) {
            progressBar.setVisibility(View.VISIBLE);
            if (callCommentsFeedApi().isExecuted())
                callCommentsFeedApi().cancel();

            // TODO: Check if data is stale.
            //  Execute network request if cache is expired; otherwise do not update data.
            adapter.getCommentsFeed().clear();
            loadFirstCommentsPage();

            Timber.tag(TAG).d("Loading First Feed Page");
        } else {
            Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);

            Timber.tag(TAG).d("No internet connection available");
        }
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

    private void loadNextCommentsPage() {
        Timber.tag(TAG).d("loadNextCommentsPage: %s", currentPage);

        callNextCommentsFeedApi().enqueue(new Callback<CommentsModel>() {
            @Override
            public void onResponse(@NotNull Call<CommentsModel> call, @NotNull Response<CommentsModel> response) {
//                Log.i(TAG, "onResponse: " + currentPage
//                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                adapter.removeLoadingFooter();
                isLoading = false;

                List<CommentItem> feedItems = fetchCommentsFeed(response);
                adapter.addAll(feedItems);

                // Cursor Links
                List<Cursor> cursor = fetchCursorLinks(response);
                Cursor cursorLink = cursor.get(0);
                next_link = cursorLink.getNextLink();
                prev_link = cursorLink.getPrevLink();
                max_id = cursorLink.getMaxID();
                since_id = cursorLink.getSinceID();

                if (currentPage != TOTAL_PAGES) {
                    adapter.addLoadingFooter();
                } else {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<CommentsModel> call, Throwable t) {
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
     */
    private Call<CommentsModel> callPrevCommentsFeedApi() {
        return commentFeedService.getComments(
                username,
                drop_id,
                null,
                since_id
        );
    }

    public void retryPageLoad() {
        loadNextCommentsPage();
    }

    /**
     */
    private void showErrorView() {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(getResources().getString(R.string.error_msg_unknown));
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
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
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

    private void hideEmptyView() {
        if (emptyLayout.getVisibility() == View.VISIBLE) {
            emptyLayout.setVisibility(View.GONE);
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
}
