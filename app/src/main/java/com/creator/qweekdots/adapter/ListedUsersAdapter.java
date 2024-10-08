package com.creator.qweekdots.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class ListedUsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // View Types
    private static final int USERS = 0;
    private static final int LOADING = 1;

    private List<UserItem> userItems;
    private Context context;

    private PaginationAdapterCallback mCallback;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private String errorMsg;
    private String username;

    private final String TAG = ListedUsersAdapter.class.getSimpleName();

    public ListedUsersAdapter(Context context, RoundedBottomSheetDialogFragment f, String username) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) f;
        this.username = username;
        userItems = new ArrayList<>();
    }

    public ListedUsersAdapter(Context context, String username) {
        this.context = context;
        this.username = username;
        userItems = new ArrayList<>();
    }

    public List<UserItem> getUsers() {
        return userItems;
    }

    public void setUsers(List<UserItem> userItems) {
        this.userItems = userItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case USERS:
                View viewItem = inflater.inflate(R.layout.users_listed, parent, false);
                viewHolder = new ListedUsersAdapter.UserVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new ListedUsersAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context!=null) {
            UserItem userItem = userItems.get(position);

            switch (getItemViewType(position)) {

                case USERS:
                    final ListedUsersAdapter.UserVH userVH = (ListedUsersAdapter.UserVH) holder;

                    ((ListedUsersAdapter.UserVH) holder).fullnameTxt.setText(userItem.getFullname());
                    ((ListedUsersAdapter.UserVH) holder).usernameTxt.setText("q/" + userItem.getUsername());

                    // Build Follow
                    if(userItem.getUsername().equals(username)) {
                        ((ListedUsersAdapter.UserVH) holder).followActionButton.setVisibility(View.GONE);
                    }
                    if(userItem.getFollowed().equals("yes")) {
                        ((ListedUsersAdapter.UserVH) holder).followActionButton.setText("Following");
                    } else {
                        ((ListedUsersAdapter.UserVH) holder).followActionButton.setText("Follow");
                    }

                    // load User ProfileModel Picture
                    Picasso.get()
                            .load(userItem.getAvatar())
                            .resize(50, 50)
                            .placeholder(R.drawable.ic_alien)
                            .error(R.drawable.ic_alien)
                            .into(userVH.profilePic);

                    // Click to view profile
                    ((ListedUsersAdapter.UserVH) holder).qClickLayout.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", userItem.getUsername());
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                        //customType(context, "fadein-to-fadeout");
                    });

                    /*
                     * Follow User, first chceck if user followed already
                     * If yes, set follow status to no along with text change to 'Follow'
                     * else, set follow status to yes along with text change to 'Following'
                     */
                    ((ListedUsersAdapter.UserVH) holder).followActionButton.setOnClickListener(v -> {
                        ((ListedUsersAdapter.UserVH) holder).followActionButton.startAnimation();
                        if(userItem.getFollowed().equals("yes")) {
                            doFollow(username, "unfollow", userItem.getUsername());
                            userItem.setFollowed("no");
                            ((ListedUsersAdapter.UserVH) holder).followActionButton.revertAnimation();
                            ((ListedUsersAdapter.UserVH) holder).followActionButton.setText("Follow");
                            ((ListedUsersAdapter.UserVH) holder).followActionButton.saveInitialState();
                        } else if(userItem.getFollowed().equals("no")) {
                            doFollow(username, "follow", userItem.getUsername());
                            userItem.setFollowed("yes");
                            ((ListedUsersAdapter.UserVH) holder).followActionButton.revertAnimation();
                            ((ListedUsersAdapter.UserVH) holder).followActionButton.setText("Following");
                            ((ListedUsersAdapter.UserVH) holder).followActionButton.saveInitialState();
                        }
                    });

                    break;

                case LOADING:
                    ListedUsersAdapter.LoadingVH loadingVH = (ListedUsersAdapter.LoadingVH) holder;

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
        return userItems == null ? 0 : userItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return USERS;
        } else {
            return (position == userItems.size() - 1 && isLoadingAdded) ? LOADING : USERS;
        }
    }


    @SuppressLint("SetTextI18n")
    private void doFollow(String follower, String type, String followe) {
        // Tag used to cancel the request
        String tag_string_req = "req_follow";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_FOLLOW, response -> {
            Timber.tag(TAG).d("Follow Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {

                    String sent = jObj.getString("sent");
                    //Toasty.success(context, sent, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_SHORT).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Follow Error: %s", error.getMessage());
            Toasty.error(context,
                    "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("follower", follower);
                params.put("type", type);
                params.put("followe", followe);

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

    public void add(UserItem r) {
        userItems.add(r);
        notifyItemInserted(userItems.size() - 1);
    }

    public void addAll(List<UserItem> userItems) {
        for (UserItem result : userItems) {
            add(result);
        }
    }

    public void remove(UserItem r) {
        int position = userItems.indexOf(r);
        if (position > -1) {
            userItems.remove(position);
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
        add(new UserItem());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = userItems.size() - 1;
        UserItem userItem = getItem(position);

        if (userItem != null) {
            userItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public UserItem getItem(int position) {
        return userItems.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(userItems.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Feed content ViewHolder
     */
    protected class UserVH extends RecyclerView.ViewHolder {
        private CircleImageView profilePic;
        private TextView usernameTxt, fullnameTxt;
        private LinearLayout qClickLayout;
        private CircularProgressButton followActionButton;

        UserVH(View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            followActionButton = itemView.findViewById(R.id.followActionButton);
            usernameTxt = itemView.findViewById(R.id.usernameTextView);
            fullnameTxt = itemView.findViewById(R.id.fullnameTextView);
            qClickLayout = itemView.findViewById(R.id.qClickLayout);
        }
    }


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
