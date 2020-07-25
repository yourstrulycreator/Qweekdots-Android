package com.creator.qweekdots.ui;

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
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.chat.ChatUserActivity;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class AddChatUsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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

    public AddChatUsersAdapter(Context context, String username) {
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
                viewHolder = new AddChatUsersAdapter.UserVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new AddChatUsersAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context!=null) {
            UserItem userItem = userItems.get(position);//TimeAgo timestamp

            switch (getItemViewType(position)) {

                case USERS:
                    final AddChatUsersAdapter.UserVH userVH = (AddChatUsersAdapter.UserVH) holder;

                    userVH.fullnameTxt.setText(userItem.getFullname());
                    userVH.usernameTxt.setText("q/" + userItem.getUsername());

                    //Hide Follow,  not needed in this adapter / User
                    userVH.followActionButton.setVisibility(View.GONE);


                    // load User ProfileModel Picture
                    Picasso.get()
                            .load(userItem.getAvatar())
                            .resize(50, 50)
                            .placeholder(R.drawable.ic_alien)
                            .error(R.drawable.ic_alien)
                            .into(userVH.profilePic);

                    userVH.qClickLayout.setOnClickListener(v -> {
                        Intent i = new Intent(context, ChatUserActivity.class);
                        i.putExtra("to", userItem.getId());
                        i.putExtra("to_name", userItem.getFullname());
                        context.startActivity(i);
                    });

                    break;

                case LOADING:
                    AddChatUsersAdapter.LoadingVH loadingVH = (AddChatUsersAdapter.LoadingVH) holder;

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
                    Toasty.success(context, sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Follow Error: %s", error.getMessage());
            Toasty.error(context,
                    Objects.requireNonNull(error.getMessage()), Toast.LENGTH_LONG).show();
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

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    private void add(UserItem r) {
        userItems.add(r);
        notifyItemInserted(userItems.size() - 1);
    }

    public void addAll(List<UserItem> userItems) {
        for (UserItem result : userItems) {
            add(result);
        }
    }

    private void remove(UserItem r) {
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

    private UserItem getItem(int position) {
        return userItems.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
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
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
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
