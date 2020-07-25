package com.creator.qweekdots.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static maes.tech.intentanim.CustomIntent.customType;

public class SuggestionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int USERS= 0;
    private static final int LOADING = 1;

    private static final String TAG = "SuggestionsAdapter";

    private List<UserItem> userItems;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;
    private String username;

    SuggestionsAdapter(Context context, Fragment f, String username) {
        this.context = context;
        this.username = username;
        userItems = new ArrayList<>();
    }

    List<UserItem> getUsers() {
        return userItems;
    }

    public void setUsers(List<UserItem> userItems) {
        this.userItems = userItems;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case USERS:
                View viewItem = inflater.inflate(R.layout.suggested_users, parent, false);
                viewHolder = new SuggestionsAdapter.UserVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new SuggestionsAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context != null) {
            UserItem userItem = userItems.get(position);//TimeAgo timestamp

            switch (getItemViewType(position)) {

                case USERS:
                    final SuggestionsAdapter.UserVH userVH = (SuggestionsAdapter.UserVH) holder;

                    // load User ProfileModel Picture
                    Picasso.get()
                            .load(userItem.getAvatar())
                            .resize(100, 100)
                            .placeholder(R.drawable.ic_alien)
                            .error(R.drawable.ic_alien)
                            .into(userVH.profilePic);

                    userVH.profilePic.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", userItem.getUsername());
                        context.startActivity(i);
                        customType(context, "up-to-bottom");
                    });

                    String uName = "q/"+userItem.getUsername();
                    userVH.sugName.setText(uName);
                    Log.d("Suggested Username:", uName);

                    break;

                case LOADING:
                    SuggestionsAdapter.LoadingVH loadingVH = (SuggestionsAdapter.LoadingVH) holder;

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

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    private void add(UserItem r) {
        userItems.add(r);
        notifyItemInserted(userItems.size() - 1);
    }

    void addAll(List<UserItem> userItems) {
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


    void addLoadingFooter() {
        isLoadingAdded = true;
        add(new UserItem());
    }

    void removeLoadingFooter() {
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
     * @param errorMsg to display if page load fails
     */
    void showRetry(boolean show, @Nullable String errorMsg) {
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
        private TextView sugName;

        UserVH(View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            sugName = itemView.findViewById(R.id.sugName);
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
