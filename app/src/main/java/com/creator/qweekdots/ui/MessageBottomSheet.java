package com.creator.qweekdots.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
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
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.adapter.ReplyAdapter;
import com.creator.qweekdots.api.MessageService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.api.ReplyFeedService;
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
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class MessageBottomSheet extends RoundedBottomSheetDialogFragment implements PaginationAdapterCallback {
    private final String TAG = DropBottomSheet.class.getSimpleName();
    private String username;
    private String drop_id;
    private MessageService dropService;

    private ImageView qweeksnap, likeBtn, deleteBtn;
    private RSVideoPlayerStandard video;
    private LinearLayout droptextTextLayout, qClickLayout;
    private CircleImageView profilePic;
    private TextView usernameTxt, likeNum, commentNum;
    private EmojiTextView dropText;
    private CardView qweeksnapCard;

    private ReplyAdapter adapter;
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

    private ReplyFeedService commentFeedService;
    private Context context;
    private BottomSheetBehavior bottomSheetBehavior;

    private View view;

    public MessageBottomSheet(Context context, String username, String drop_id) {
        this.context = context;
        this.username = username;
        this.drop_id = drop_id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        view = inflater.inflate(R.layout.message_bottom_sheet, container, false);

        if(context!=null) {
            usernameTxt = view.findViewById(R.id.usernameTxt);
            dropText = view.findViewById(R.id.txtDrop);
            profilePic = view.findViewById(R.id.profilePic);
            qweeksnap = view.findViewById(R.id.drop_qweekSnap);
            video = view.findViewById(R.id.videoplayer);
            likeBtn = view.findViewById(R.id.like_btn);
            likeNum = view.findViewById(R.id.likeNum);
            commentNum = view.findViewById(R.id.commentNum);
            deleteBtn = view.findViewById(R.id.delete_btn);
            qClickLayout = view.findViewById(R.id.qClickLayout);
            droptextTextLayout = view.findViewById(R.id.droptext_text_layout);
            qweeksnapCard = view.findViewById(R.id.drop_qweekSnapCard);

            //COMMENTS
            RecyclerView rv = view.findViewById(R.id.main_recycler);
            progressBar = view.findViewById(R.id.spin_kit);
            errorLayout = view.findViewById(R.id.error_layout);
            emptyLayout = view.findViewById(R.id.empty_layout);
            Button btnRetry = view.findViewById(R.id.error_btn_retry);
            txtError = view.findViewById(R.id.error_txt_cause);

            adapter = new ReplyAdapter(context, this, username);

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
            dropService = QweekdotsApi.getClient(context).create(MessageService.class);
            commentFeedService = QweekdotsApi.getClient(context).create(ReplyFeedService.class);

            ImageView closeSheet = view.findViewById(R.id.closeSheet);
            closeSheet.setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                dismiss();
            });

            // init Functions
            loadDrop();
            loadFirstCommentsPage();

            // Drop comment
            dropCommentTxt = view.findViewById(R.id.dropCommentTxt);
            dropCommentBtn = view.findViewById(R.id.dropCommentBtn);
            dropCommentBtn.setOnClickListener(v -> {
                dropCommentBtn.setClickable(false);
                String drop = Objects.requireNonNull(dropCommentTxt.getText()).toString();
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
        view = View.inflate(getContext(), R.layout.message_bottom_sheet, null);
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
            public void onSlide(@NonNull View view, float v) {}
        });
        return bottomSheet;
    }

    /**
     * Load Chat Room Message
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

                // Set username
                usernameTxt.setText("q/" + drop.getUsername());

                qClickLayout.setOnClickListener(v-> {
                    Intent i = new Intent(context, ProfileActivity.class);
                    i.putExtra("profile", drop.getUsername());
                    context.startActivity(i);
                    customType(context, "fadein-to-fadeout");
                });

                //Build layout depending on type
                if(drop.getHasMedia() == 1) {
                    qweeksnapCard.setVisibility(View.VISIBLE);

                    //Set up qweeksnap if drop hasMedia
                    switch (drop.getType()) {
                        case "photo":
                            qweeksnap.setVisibility(View.VISIBLE);
                            video.setVisibility(View.GONE);

                            RequestOptions requestOptions = new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                                    .skipMemoryCache(true);
                            Glide
                                    .with(context)
                                    .load(drop.getQweekSnap())
                                    .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                                    .apply(requestOptions)
                                    .into(qweeksnap);

                            /*
                            List<String> images = Collections.singletonList(drop.getQweekSnap());
                            qweeksnap.setOnClickListener(v-> new StfalconImageViewer.Builder<>(context, images, (imageView, imageUrl) -> Glide.with(context).load(imageUrl).into(imageView))
                                    .withTransitionFrom(qweeksnap)
                                    .withBackgroundColor(context.getResources().getColor(R.color.tabColor))
                                    .show());

                             */
                            break;
                        case "video":
                            video.setVisibility(View.VISIBLE);
                            qweeksnap.setVisibility(View.GONE);

                            // qweekvid
                            video.setUp(drop.getQweekSnap(),
                                    RSVideoPlayer.SCREEN_LAYOUT_LIST);
                            video.setThumbImageView(drop.getQweekSnap());

                            break;
                    }
                } else {
                    qweeksnapCard.setVisibility(View.GONE);
                    qweeksnap.setVisibility(View.GONE);
                    video.setVisibility(View.GONE);
                }

                // Check for empty status message
                if (!TextUtils.isEmpty(drop.getDrop())) {
                    String dropTXT = unescapeJava(drop.getDrop());
                    SpannableString hashText = new SpannableString(dropTXT);
                    Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                    while (matcher.find()) {
                        hashText.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.SkyBlue)), matcher.start(), matcher.end(), 0);
                    }
                    //if none set text and make it visible
                    dropText.setText(hashText);
                    dropText.setVisibility(View.VISIBLE);
                } else {
                    droptextTextLayout.setVisibility(View.GONE);
                    dropText.setVisibility(View.GONE);
                }

                // load User Avatar
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
                /// Like Builder
                /*
                 * If drop is liked, set resource to liked long with resource color
                 * else, resource is not liked yet
                 */
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

                // Text Delete Button
                if(drop.getUsername().equals(username)) {
                    deleteBtn.setVisibility(View.VISIBLE);

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Confirm Delete...");
                    // Setting Dialog Message
                    alertDialog.setMessage("Are you sure you want delete this drop?");
                    // Setting Icon to Dialog
                    alertDialog.setIcon(R.drawable.ic_delete);

                    // Setting Positive "Yes" Btn
                    alertDialog.setPositiveButton("YES",
                            (dialog, which) -> {
                                Toasty.info(context, "deleting...", Toasty.LENGTH_SHORT).show();
                                deleteMessage(drop.getDrop_Id(), drop.getUserID());
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
                 */
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
                "https://qweek.fun/genjitsu/chat/message_like.php", response -> {
            Timber.tag(TAG).d("Like Response: %s", response);
            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    String sent = jObj.getString("sent");

                    //Toasty.success(context, sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Timber.tag(TAG).e("Like Error: %s", error.getMessage());
            Toasty.error(context,
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
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

    /**
     * function to delete Chat Room message
     */
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
                    Toasty.success(context, sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Timber.tag(TAG).e("Delete Error: %s", error.getMessage());
            Toasty.error(context,
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

    /**
     * function to post Chat Room message comment
     * */
    private void postComment(final String drop, final String username, final String drop_id) {
        // Tag used to cancel the request
        String tag_string_req = "req_post";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/reply.php", response -> {
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

                    Toasty.success(requireContext(), sent, Toast.LENGTH_LONG).show();
                } else {
                    // Error in drop. Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Toasty.error(requireContext(),
                            errorMsg, Toast.LENGTH_LONG).show();
                    dropCommentBtn.setClickable(true);
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                dropCommentBtn.setClickable(true);
            }

        }, error -> {
            Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
            Toasty.error(requireContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
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
     * Load First Set of Comments for Chat Room Message
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
