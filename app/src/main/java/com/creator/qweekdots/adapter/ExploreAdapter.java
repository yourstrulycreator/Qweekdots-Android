package com.creator.qweekdots.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.creator.qweekdots.R;
import com.creator.qweekdots.models.PhotoItem;
import com.creator.qweekdots.ui.DropBottomSheet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExploreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // View Types
    private static final int EXPLORE = 0;
    private static final int LOADING = 1;
    //private static final String TAG = ExploreAdapter.class.getSimpleName();

    private List<PhotoItem> exploreItems;
    private Context context;
    private String username;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private String errorMsg;

    public ExploreAdapter(Context context, String username) {
        this.context = context;
        this.username = username;
        exploreItems = new ArrayList<>();

    }

    public List<PhotoItem> getExplore() {
        return exploreItems;
    }

    public void setExplore(List<PhotoItem> exploreItems) {
        this.exploreItems = exploreItems;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case EXPLORE:
                View viewItem = inflater.inflate(R.layout.explore_photos, parent, false);
                viewHolder = new ExploreVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context!=null) {
            PhotoItem exploreItem = exploreItems.get(position);

            switch (getItemViewType(position)) {
                case EXPLORE:
                    final ExploreVH exploreVH = (ExploreVH) holder;
                    RequestOptions requestOptions = new RequestOptions() // because file name is always same
                            .format(DecodeFormat.PREFER_RGB_565);
                    Glide
                            .with(context)
                            .load(exploreItem.getQweekSnap())
                            .override(Target.SIZE_ORIGINAL)
                            .thumbnail(0.3f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .apply(requestOptions)
                            .into(((ExploreVH) holder).snap);

                    ((ExploreVH) holder).exploreCard.setOnClickListener(v -> {
                        DropBottomSheet bottomSheet = new DropBottomSheet(context, username, exploreItem.getDrop_Id());
                        FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                        bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
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
        return exploreItems == null ? 0 : exploreItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return EXPLORE;
        } else {
            return (position == exploreItems.size() - 1 && isLoadingAdded) ? LOADING : EXPLORE;
        }
    }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */
    private void add(PhotoItem r) {
        exploreItems.add(r);
        notifyItemInserted(exploreItems.size() - 1);
    }

    public void addAll(List<PhotoItem> exploreItems) {
        for (PhotoItem result : exploreItems) {
            add(result);
        }
    }

    private void remove(PhotoItem r) {
        int position = exploreItems.indexOf(r);
        if (position > -1) {
            exploreItems.remove(position);
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
        add(new PhotoItem());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = exploreItems.size() - 1;
        PhotoItem exploreItem = getItem(position);

        if (exploreItem != null) {
            exploreItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    private PhotoItem getItem(int position) {
        return exploreItems.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(exploreItems.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    /*
   View Holders
   _________________________________________________________________________________________________
    */
    /**
     * Feed content ViewHolder
     */
    protected class ExploreVH extends RecyclerView.ViewHolder {
        private CardView exploreCard;
        private ImageView snap;

        ExploreVH(View itemView) {
            super(itemView);

            exploreCard = itemView.findViewById(R.id.container);
            snap = itemView.findViewById(R.id.image);
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
