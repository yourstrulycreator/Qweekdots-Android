package com.creator.qweekdots.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.models.UserItem;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class SuggestionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // View Types
    private static final int USERS= 0;
    private static final int LOADING = 1;
    //private static final String TAG = "SuggestionsAdapter";

    private List<UserItem> userItems;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private String errorMsg;

    public SuggestionsAdapter(Context context) {
        this.context = context;
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
            UserItem userItem = userItems.get(position);

            switch (getItemViewType(position)) {
                case USERS:
                    final SuggestionsAdapter.UserVH userVH = (SuggestionsAdapter.UserVH) holder;
                    // load User Avatar
                    /*Picasso.get()
                            .load(userItem.getAvatar())
                            .resize(100, 100)
                            .placeholder(R.drawable.ic_alien)
                            .error(R.drawable.ic_alien)
                            .into(userVH.profilePic);*/

                    RequestOptions requestOptions = new RequestOptions() // because file name is always same
                            .format(DecodeFormat.PREFER_RGB_565);
                    Drawable placeholder = getTinted(R.drawable.ic_alien, context.getResources().getColor(R.color.contentTextColor));
                    Glide
                            .with(context)
                            .load(userItem.getAvatar())
                            .override(100, 100)
                            .placeholder(placeholder)
                            .error(placeholder)
                            .thumbnail(0.3f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .apply(requestOptions)
                            .into(userVH.profilePic);

                    userVH.profilePic.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", userItem.getUsername());
                        context.startActivity(i);
                        customType(context, "fadein-to-fadeout");
                    });

                    String uName = "q/"+userItem.getUsername();
                    userVH.sugName.setText(uName);
                    Timber.tag("Suggested Username:").d(uName);

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

    private @Nullable Drawable getTinted(@DrawableRes int res, @ColorInt int color) {
        // need to mutate otherwise all references to this drawable will be tinted
        Drawable drawable = ContextCompat.getDrawable(context, res).mutate();
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

                    break;
            }
        }
    }
}
