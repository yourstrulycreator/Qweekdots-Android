package com.creator.qweekdots.ui;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.creator.qweekdots.R;
import com.creator.qweekdots.grid.util.DynamicHeightTextView;
import com.creator.qweekdots.models.TrendsItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int TRENDS = 0;
    private static final int LOADING = 1;

    private static final String TAG = TrendsAdapter.class.getSimpleName();

    private List<TrendsItem> trendItems;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;
    private String username;
    private final Random mRandom;
    private final ArrayList<Integer> mBackgroundColors;

    TrendsAdapter(Context context, Fragment f, String username) {
        this.context = context;
        this.username = username;
        trendItems = new ArrayList<>();

        mRandom = new Random();
        mBackgroundColors = new ArrayList<>();
        mBackgroundColors.add(R.color.Fuchsia);
        mBackgroundColors.add(R.color.Tomato);
        mBackgroundColors.add(R.color.Coral);
        mBackgroundColors.add(R.color.SteelBlue);
        mBackgroundColors.add(R.color.DarkSlateBlue);
        mBackgroundColors.add(R.color.DodgerBlue);
        mBackgroundColors.add(R.color.DarkSlateGray);
        mBackgroundColors.add(R.color.HotPink);
        mBackgroundColors.add(R.color.DeepPurple);
        mBackgroundColors.add(R.color.DeepPink);
        mBackgroundColors.add(R.color.Violet);
        mBackgroundColors.add(R.color.SeaGreen);
        mBackgroundColors.add(R.color.CornflowerBlue);
        mBackgroundColors.add(R.color.DeepSkyBlue);
        mBackgroundColors.add(R.color.DarkTurquoise);
    }

    List<TrendsItem> getTrends() {
        return trendItems;
    }

    public void setTrends(List<TrendsItem> trendItems) {
        this.trendItems = trendItems;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TRENDS:
                View viewItem = inflater.inflate(R.layout.trend_items, parent, false);
                viewHolder = new TrendsAdapter.TrendsVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new TrendsAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context!=null) {
            TrendsItem trendItem = trendItems.get(position);

            switch (getItemViewType(position)) {

                case TRENDS:
                    final TrendsAdapter.TrendsVH trendsVH = (TrendsAdapter.TrendsVH) holder;

                    int randomColor = mBackgroundColors.get(mRandom.nextInt(15));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        trendsVH.trendsCard.setCardBackgroundColor(context.getColor(randomColor));
                    }

                    trendsVH.trendsCard.setOnClickListener(v -> {

                    });


                    trendsVH.txtLineOne.setText(trendItem.getTitle());

                    break;

                case LOADING:
                    TrendsAdapter.LoadingVH loadingVH = (TrendsAdapter.LoadingVH) holder;

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
        return trendItems == null ? 0 : trendItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TRENDS;
        } else {
            return (position == trendItems.size() - 1 && isLoadingAdded) ? LOADING : TRENDS;
        }
    }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    private void add(TrendsItem r) {
        trendItems.add(r);
        notifyItemInserted(trendItems.size() - 1);
    }

    void addAll(List<TrendsItem> trendItems) {
        for (TrendsItem result : trendItems) {
            add(result);
        }
    }

    private void remove(TrendsItem r) {
        int position = trendItems.indexOf(r);
        if (position > -1) {
            trendItems.remove(position);
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
        add(new TrendsItem());
    }

    void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = trendItems.size() - 1;
        TrendsItem trendItem = getItem(position);

        if (trendItem != null) {
            trendItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    private TrendsItem getItem(int position) {
        return trendItems.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param errorMsg to display if page load fails
     */
    void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(trendItems.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Feed content ViewHolder
     */
    protected class TrendsVH extends RecyclerView.ViewHolder {
        private CardView trendsCard;
        private DynamicHeightTextView txtLineOne;

        TrendsVH(View itemView) {
            super(itemView);

            trendsCard = itemView.findViewById(R.id.panel_content);
            txtLineOne = itemView.findViewById(R.id.trendtitleText);
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
