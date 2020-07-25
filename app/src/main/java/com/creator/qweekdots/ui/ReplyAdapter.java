package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.models.CommentItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.squareup.picasso.Picasso;
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

public class ReplyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int FEED= 0;
    private static final int LOADING = 1;

    private List<CommentItem> feedItems;
    private CommentItem fItem;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;
    private String username;

    private static final String TAG = CommentsAdapter.class.getSimpleName();

    ReplyAdapter(Context context, MessageBottomSheet f, String username) {
        this.context = context;
        this.mCallback = f;
        this.username = username;
        feedItems = new ArrayList<>();
    }

    public List<CommentItem> getCommentsFeed() {
        return feedItems;
    }

    public void setCommentsFeed(List<CommentItem> feedItems) {
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
                View viewItem = inflater.inflate(R.layout.reply_item, parent, false);
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

                    // Set username
                    feedVH.usernameTxt.setText("q/" + feedItem.getUsername());

                    // Converting timestamp into x ago format

                    // Check for empty status message
                    if (!TextUtils.isEmpty(feedItem.getDrop())) {

                        SpannableString hashText = new SpannableString(feedItem.getDrop());
                        Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                        while (matcher.find())
                        {
                            hashText.setSpan(new BackgroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
                        }
                        //if none set text and make it visible
                        feedVH.dropText.setText(hashText);
                        feedVH.dropText.setVisibility(View.VISIBLE);
                    } else {
                        // status is empty, remove from view
                        feedVH.dropText.setVisibility(View.GONE);
                    }

                    // load User ProfileModel Picture
                    Picasso.get()
                            .load(feedItem.getAvatar())
                            .resize(40, 40)
                            .placeholder(R.drawable.ic_alien)
                            .error(R.drawable.ic_alien)
                            .centerCrop()
                            .into(feedVH.profilePic);

                    feedVH.profilePic.setOnClickListener(v-> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", feedItem.getUsername());
                        context.startActivity(i);
                        customType(context, "up-to-bottom");
                    });

                    feedVH.usernameTxt.setOnClickListener(v-> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", feedItem.getUsername());
                        context.startActivity(i);
                        customType(context, "up-to-bottom");
                    });

                    // Build Drop Actions
                    if(feedItem.getUpvoted().equals("yes")) {
                        feedVH.upvoteBtn.setImageResource(R.drawable.ic_upvoted);
                        feedVH.upvoteBtn.setColorFilter(context.getResources().getColor(R.color.upvoteColor));
                    } else if(feedItem.getUpvoted().equals("no")) {
                        feedVH.upvoteBtn.setImageResource(R.drawable.ic_upvote);
                        feedVH.upvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }
                    if(feedItem.getDownvoted().equals("yes")) {
                        feedVH.downvoteBtn.setImageResource(R.drawable.ic_downvoted);
                        feedVH.downvoteBtn.setColorFilter(context.getResources().getColor(R.color.downvoteColor));
                    } else if(feedItem.getDownvoted().equals("no")) {
                        feedVH.downvoteBtn.setImageResource(R.drawable.ic_downvote);
                        feedVH.downvoteBtn.setColorFilter(context.getResources().getColor(R.color.Gray));
                    }
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
                        alertDialog.setMessage("Are you sure you want delete this message?");

                        // Setting Icon to Dialog
                        alertDialog.setIcon(R.drawable.ic_delete);

                        // Setting Positive "Yes" Btn
                        alertDialog.setPositiveButton("YES",
                                (dialog, which) -> {
                                    deleteMessage(feedItem.getDrop_Id(), feedItem.getUsername());
                                    Toasty.info(context, "deleting...", Toasty.LENGTH_SHORT).show();
                                });
                        // Setting Negative "NO" Btn
                        alertDialog.setNegativeButton("NO",
                                (dialog, which) -> {
                                    dialog.cancel();
                                });

                        feedVH.deleteBtn.setOnClickListener(v->{
                            alertDialog.show();
                        });
                    } else {
                        feedVH.deleteBtn.setVisibility(View.GONE);
                    }

                    // Set up Actions
                    //Like
                    feedVH.likeBtn.setOnClickListener(v -> {
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
                    feedVH.upvoteBtn.setOnClickListener(v -> {
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
                    feedVH.downvoteBtn.setOnClickListener(v -> {
                        //check whether it is liked or un-downvoted
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
                "https://qweek.fun/genjitsu/chat/reply_like.php", response -> {
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
                "https://qweek.fun/genjitsu/chat/reply_upvote.php", response -> {
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
                "https://qweek.fun/genjitsu/chat/reply_downvote.php", response -> {
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

    private void deleteMessage(final String message_id, String user_id) {
        // Tag used to cancel the request
        String tag_string_req = "req_delete";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/reply_delete.php", response -> {
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

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    private void add(CommentItem r) {
        feedItems.add(r);
        notifyItemInserted(feedItems.size() - 1);
    }

    public void addAll(List<CommentItem> feedItems) {
        for (CommentItem result : feedItems) {
            add(result);
        }
    }

    private void remove(CommentItem r) {
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

    public void updateData(CommentItem feedItems) {
        this.fItem = feedItems;
        this.add(fItem);
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
        private ImageView upvoteBtn, downvoteBtn, shareBtn, likeBtn, deleteBtn, expandBtn;
        private LinearLayout dropActions;
        private CircleImageView profilePic;
        private TextView usernameTxt;
        private EmojiTextView dropText;

        FeedVH(View itemView) {
            super(itemView);

            usernameTxt = itemView.findViewById(R.id.usernameTxt);
            dropText = itemView.findViewById(R.id.txtDrop);
            profilePic = itemView.findViewById(R.id.profilePic);
            upvoteBtn = itemView.findViewById(R.id.upvote_btn);
            downvoteBtn = itemView.findViewById(R.id.downvote_btn);
            expandBtn = itemView.findViewById(R.id.expand_btn);
            shareBtn = itemView.findViewById(R.id.share_btn);
            likeBtn = itemView.findViewById(R.id.like_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
            dropActions = itemView.findViewById(R.id.drop_actions);
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