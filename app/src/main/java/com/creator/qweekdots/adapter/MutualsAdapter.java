package com.creator.qweekdots.adapter;

import android.annotation.SuppressLint;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ChatUserActivity;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class MutualsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // View Types
    private static final int FEED= 0;
    private static final int LOADING = 1;

    private List<UserItem> feedItems;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;
    private String username;


    private static final String TAG = MutualsAdapter.class.getSimpleName();

    public MutualsAdapter(Context context, Fragment f, String username) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) f;
        this.username = username;
        feedItems = new ArrayList<>();
    }

    public List<UserItem> getQweekFeed() {
        return feedItems;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EmojiManager.install(new IosEmojiProvider());
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case FEED:
                View viewItem = inflater.inflate(R.layout.mutuals_item, parent, false);
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
            holder.setIsRecyclable(false);
            UserItem feedItem = feedItems.get(position);
            Timber.tag(TAG).d(String.valueOf(position));

            switch (getItemViewType(position)) {

                case FEED:
                    // Set fullname
                    //((FeedVH) holder).fullnameTxt.setText(feedItem.getFullname());

                    // Set username
                    //((FeedVH) holder).usernameTxt.setText("q/" + feedItem.getUsername());
                    ((FeedVH) holder).usernameTxt.setVisibility(View.GONE);

                    RequestOptions requestOptions = new RequestOptions() // because file name is always same
                            .format(DecodeFormat.PREFER_RGB_565);
                    Drawable placeholder = getTinted(context.getResources().getColor(R.color.contentTextColor));
                    Glide
                            .with(context)
                            .load(feedItem.getAvatar())
                            .override(55, 55)
                            .placeholder(placeholder)
                            .error(placeholder)
                            .thumbnail(0.3f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .apply(requestOptions)
                            .into(((FeedVH) holder).profilePic);


                    ((FeedVH) holder).profilePic.setOnClickListener(v -> {
                        Intent i = new Intent(context.getApplicationContext(), ChatUserActivity.class);
                        i.putExtra("to", feedItem.getId());
                        i.putExtra("to_name", feedItem.getFullname());
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.getApplicationContext().startActivity(i);
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
                Timber.d("onScrollStateChanged: Called %s", newState);
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
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    public void add(UserItem r) {
        feedItems.add(r);
        notifyItemInserted(feedItems.size() - 1);
    }

    public void addAll(List<UserItem> feedItems) {
        for (UserItem result : feedItems) {
            add(result);
        }
    }

    public void remove(UserItem r) {
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
        add(new UserItem());
    }

    public void removeLoadingFooter() {

        isLoadingAdded = false;

        int position = feedItems.size() - 1;
        UserItem feedItem = getItem(position);

        if (feedItem != null) {
            feedItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public UserItem getItem(int position) {
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
        private CircleImageView profilePic;
        private TextView usernameTxt;

        FeedVH(View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            usernameTxt = itemView.findViewById(R.id.usernameTxt);
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
