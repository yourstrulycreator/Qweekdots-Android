package com.creator.qweekdots.adapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.models.CommentItem;
import com.creator.qweekdots.ui.DropBottomSheet;
import com.creator.qweekdots.ui.ReplyCommentBottomSheet;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // View Types
    private static final int FEED= 0;
    private static final int LOADING = 1;

    private List<CommentItem> feedItems;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;
    private String username;

    private static final String TAG = CommentsAdapter.class.getSimpleName();

    public CommentsAdapter(Context context, DropBottomSheet f, String username) {
        this.context = context;
        this.mCallback = f;
        this.username = username;
        feedItems = new ArrayList<>();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EmojiManager.install(new IosEmojiProvider());
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case FEED:
                View viewItem = inflater.inflate(R.layout.comments_item, parent, false);
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
            CommentItem feedItem = feedItems.get(position);

            switch (getItemViewType(position)) {

                case FEED:
                    final FeedVH feedVH = (FeedVH) holder;

                    if(feedItem.getCommentDepth() > 0) {
                        Timber.tag("Child comment depth:").i(String.valueOf(feedItem.getCommentDepth()+1));

                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) feedVH.commentParent.getLayoutParams();
                        params.leftMargin = feedItem.getCommentDepth()*10;
                    }

                    // Set fullname
                    // Set username
                    //feedVH.usernameTxt.setText("q/" + feedItem.getUsername());

                    // Check for empty status message
                    if (!TextUtils.isEmpty(feedItem.getDrop())) {
                        SpannableString hashText = new SpannableString(feedItem.getDrop());
                        Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                        while (matcher.find()) {
                            hashText.setSpan(new BackgroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
                        }
                        //if none set text and make it visible
                        feedVH.dropText.setText(hashText);
                        feedVH.dropText.setVisibility(View.VISIBLE);
                    } else {
                        // status is empty, remove from view
                        feedVH.dropText.setVisibility(View.GONE);
                    }

                    // load User Avatar
                    /*Picasso.get()
                            .load(feedItem.getAvatar())
                            .resize(40, 40)
                            .placeholder(R.drawable.ic_alien)
                            .error(R.drawable.ic_alien)
                            .centerCrop()
                            .into(feedVH.profilePic);*/

                    RequestOptions requestOptions = new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                            .skipMemoryCache(true);
                    Drawable placeholder = getTinted(context.getResources().getColor(R.color.contentTextColor));
                    Glide
                            .with(context)
                            .load(feedItem.getAvatar())
                            .override(40, 40)
                            .placeholder(placeholder)
                            .error(placeholder)
                            .thumbnail(0.3f)
                            .apply(requestOptions)
                            .into(feedVH.profilePic);

                    feedVH.profilePic.setOnClickListener(v-> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", feedItem.getUsername());
                        context.startActivity(i);
                        customType(context, "fadein-to-fadeout");
                    });

                    /*feedVH.usernameTxt.setOnClickListener(v-> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", feedItem.getUsername());
                        context.startActivity(i);
                        customType(context, "up-to-bottom");
                    });*/

                    // Build Drop Actions

                    // Upvote Builder
                    /*
                     * If drop is upvoted, set resource to upvoted along with resource color
                     * else, resource is not upvoted yet
                     */
                    if(feedItem.getUpvoted().equals("yes")) {
                        feedVH.upvoteBtn.setImageResource(R.drawable.ic_upvoted);
                        feedVH.upvoteBtn.setColorFilter(context.getResources().getColor(R.color.upvoteColor));
                    } else if(feedItem.getUpvoted().equals("no")) {
                        feedVH.upvoteBtn.setImageResource(R.drawable.ic_upvote);
                        feedVH.upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }

                    // Downvote Builder
                    /*
                     * If drop is downvoted, set resource to downvoted along with resource color
                     * else, resource is not downvoted yet
                     */
                    if(feedItem.getDownvoted().equals("yes")) {
                        feedVH.downvoteBtn.setImageResource(R.drawable.ic_downvoted);
                        feedVH.downvoteBtn.setColorFilter(context.getResources().getColor(R.color.downvoteColor));
                    } else if(feedItem.getDownvoted().equals("no")) {
                        feedVH.downvoteBtn.setImageResource(R.drawable.ic_downvote);
                        feedVH.downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }

                    // Like Builder
                    /*
                     * If drop is liked, set resource to liked long with resource color
                     * else, resource is not liked yet
                     */
                    if(feedItem.getLiked().equals("yes")) {
                        feedVH.likeBtn.setImageResource(R.drawable.ic_liked);
                        feedVH.likeBtn.setColorFilter(context.getResources().getColor(R.color.likeColor));
                    } else if(feedItem.getLiked().equals("no")) {
                        feedVH.likeBtn.setImageResource(R.drawable.ic_like);
                        feedVH.likeBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }

                    if(feedItem.getUsername().equals(username)) {
                        feedVH.deleteBtn.setVisibility(View.VISIBLE);
                    } else {
                        feedVH.deleteBtn.setVisibility(View.GONE);
                    }

                    // Text Delete Button
                    if(feedItem.getUsername().equals(username)) {
                        feedVH.deleteBtn.setVisibility(View.VISIBLE);

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle("Confirm Delete...");

                        // Setting Dialog Message
                        alertDialog.setMessage("Are you sure you want delete this comment?");

                        // Setting Icon to Dialog
                        alertDialog.setIcon(R.drawable.ic_delete);

                        // Setting Positive "Yes" Btn
                        alertDialog.setPositiveButton("YES",
                                (dialog, which) -> {
                                    Toasty.info(context, "deleting...", Toasty.LENGTH_SHORT).show();
                                    deleteDrop(feedItem.getDrop_Id(), feedItem.getUsername(), position);
                                });
                        // Setting Negative "NO" Btn
                        alertDialog.setNegativeButton("NO",
                                (dialog, which) -> dialog.cancel());

                        feedVH.deleteBtn.setOnClickListener(v-> alertDialog.show());
                    } else {
                        feedVH.deleteBtn.setVisibility(View.GONE);
                    }

                    // Set up Actions

                    //Like
                    /*
                     * On click, first check if user liked already
                     * If user liked already, unlike and set resource to pre-liked state
                     * else, like along with resource change
                     */
                    feedVH.likeBtn.setOnClickListener(v -> {
                        ObjectAnimator animY = ObjectAnimator.ofFloat(((FeedVH) holder).likeBtn, "translationY", -100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(0);
                        animY.start();
                        //check whether it is liked or unliked
                        if (feedItem.getLiked().equals("yes")) {
                            //unlike
                            Toasty.info(context, "taking back like...", Toasty.LENGTH_SHORT).show();
                            feedItem.setLiked("no");
                            feedVH.likeBtn.setImageResource(R.drawable.ic_like);
                            feedVH.likeBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                            doLike("unlike", feedItem.getDrop_Id(), username, feedItem.getUsername());
                        } else {
                            //like
                            Toasty.info(context, "liking...", Toasty.LENGTH_SHORT).show();
                            feedItem.setLiked("yes");
                            feedVH.likeBtn.setImageResource(R.drawable.ic_liked);
                            feedVH.likeBtn.setColorFilter(context.getResources().getColor(R.color.likeColor));
                            doLike("like", feedItem.getDrop_Id(), username, feedItem.getUsername());
                        }
                    });

                    //Upvote
                    /*
                     * On click, first check if user upvoted already
                     * If user upvoted already, undo upvote and set resource to pre-upvote state
                     * else, upvote along with resource change
                     */
                    feedVH.upvoteBtn.setOnClickListener(v -> {
                        ObjectAnimator animY = ObjectAnimator.ofFloat(((FeedVH) holder).upvoteBtn, "translationY", -100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(0);
                        animY.start();

                        //check whether it is liked or un-upvoted
                        if (feedItem.getUpvoted().equals("yes")) {
                            //un-upvote
                            Toasty.info(context, "taking back upvote...", Toasty.LENGTH_SHORT).show();

                            feedItem.setUpvoted("no");
                            feedVH.upvoteBtn.setImageResource(R.drawable.ic_upvote);
                            feedVH.upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
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

                                feedVH.downvoteBtn.setImageResource(R.drawable.ic_downvote);
                                feedVH.downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                                feedItem.setDownvoted("no");
                                doDownvote("undo", feedItem.getDrop_Id(), username, feedItem.getUsername());
                            }

                            feedVH.upvoteBtn.setImageResource(R.drawable.ic_upvoted);
                            feedVH.upvoteBtn.setColorFilter(context.getResources().getColor(R.color.upvoteColor));
                            doUpvote("upvote", feedItem.getDrop_Id(), username, feedItem.getUsername());
                        }
                    });

                    //Downvote
                    /*
                     * On click, first check if user downvoted already
                     * If user downvoted already, undo downvote and set resource to pre-downvote state
                     * else, downvote along with resource
                     */
                    feedVH.downvoteBtn.setOnClickListener(v -> {
                        ObjectAnimator animY = ObjectAnimator.ofFloat(((FeedVH) holder).downvoteBtn, "translationY", 100f, 0f);
                        animY.setDuration(1000);//1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(0);
                        animY.start();

                        //check whether it is downvoted or not
                        if (feedItem.getDownvoted().equals("yes")) {
                            //un-downvote
                            Toasty.info(context, "taking back downvote...", Toasty.LENGTH_SHORT).show();

                            feedItem.setDownvoted("no");
                            feedVH.downvoteBtn.setImageResource(R.drawable.ic_downvote);
                            feedVH.downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
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

                                feedVH.upvoteBtn.setImageResource(R.drawable.ic_upvote);
                                feedVH.upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                                feedItem.setUpvoted("no");
                                doUpvote("undo", feedItem.getDrop_Id(), username, feedItem.getUsername());
                            }

                            feedVH.downvoteBtn.setImageResource(R.drawable.ic_downvoted);
                            feedVH.downvoteBtn.setColorFilter(context.getResources().getColor(R.color.downvoteColor));
                            doDownvote("downvote", feedItem.getDrop_Id(), username, feedItem.getUsername());
                        }
                    });

                    //Reply
                    feedVH.replyBtn.setOnClickListener(v->{
                        ReplyCommentBottomSheet bottomSheet = new ReplyCommentBottomSheet(feedItem.getCommented_drop_id(), feedItem.getId(), username);
                        bottomSheet.show(((AppCompatActivity)context).getSupportFragmentManager(), bottomSheet.getTag());
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
                    String sent = jObj.getString("sent");
                    Toasty.success(context, sent, Toast.LENGTH_LONG).show();
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
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Upvote Error: %s", error.getMessage());
            Toasty.error(context,
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
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
                    Toasty.success(context, sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Downvote Error: %s", error.getMessage());
            Toasty.error(context,
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
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

    private void deleteDrop(final String drop_id, String username, int position) {
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

                    feedItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, feedItems.size());
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
                params.put("id", drop_id);
                params.put("u", username);
                params.put("type", "comment");

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
    public void add(CommentItem r) {
        feedItems.add(r);
        notifyItemInserted(feedItems.size() - 1);
    }

    public void addAll(List<CommentItem> feedItems) {
        for (CommentItem result : feedItems) {
            add(result);
        }
    }

    public void remove(CommentItem r) {
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
        add(new CommentItem());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = feedItems.size() - 1;
        CommentItem feedItem = getItem(position);

        if (feedItem != null) {
            feedItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    private CommentItem getItem(int position) {
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
        private ImageView upvoteBtn, downvoteBtn, likeBtn, replyBtn, deleteBtn;
        private CircleImageView profilePic;
        //private TextView usernameTxt;
        private EmojiTextView dropText;
        private LinearLayout commentParent;

        FeedVH(View itemView) {
            super(itemView);

            //usernameTxt = itemView.findViewById(R.id.usernameTxt);
            dropText = itemView.findViewById(R.id.txtDrop);
            profilePic = itemView.findViewById(R.id.profilePic);
            upvoteBtn = itemView.findViewById(R.id.upvote_btn);
            downvoteBtn = itemView.findViewById(R.id.downvote_btn);
            likeBtn = itemView.findViewById(R.id.like_btn);
            replyBtn = itemView.findViewById(R.id.reply_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
            commentParent = itemView.findViewById(R.id.commentParent);
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

}