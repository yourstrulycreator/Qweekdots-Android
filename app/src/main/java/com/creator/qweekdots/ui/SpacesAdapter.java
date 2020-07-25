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
import com.creator.qweekdots.models.SpaceItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpacesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int SPACES= 0;
    private static final int LOADING = 1;

    private static final String TAG = SpacesAdapter.class.getSimpleName();

    private static final String BASE_URL_MEDIA = "http://10.0.2.2:8080/qweekdots/media/";
    private static final String BASE_URL_USER_PIC = "http://10.0.2.2:8080/qweekdots/profiles/";

    private List<SpaceItem> spaceItems;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;
    private String username;
    private final Random mRandom;
    private final ArrayList<Integer> mBackgroundColors;

    SpacesAdapter(Context context, Fragment f, String username) {
        this.context = context;
        this.username = username;
        spaceItems = new ArrayList<>();

        mRandom = new Random();
        mBackgroundColors = new ArrayList<Integer>();
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

    List<SpaceItem> getSpaces() {
        return spaceItems;
    }

    public void setSpaces(List<SpaceItem> spaceItems) {
        this.spaceItems = spaceItems;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case SPACES:
                View viewItem = inflater.inflate(R.layout.spaces_item, parent, false);
                viewHolder = new SpacesAdapter.SpaceVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new SpacesAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SpaceItem spaceItem = spaceItems.get(position);//TimeAgo timestamp

        switch (getItemViewType(position)) {

            case SPACES:
                final SpacesAdapter.SpaceVH spaceVH = (SpacesAdapter.SpaceVH) holder;

                int randomColor = mBackgroundColors.get(mRandom.nextInt(15));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    spaceVH.spaceCard.setCardBackgroundColor(context.getColor(randomColor));
                }

                spaceVH.spaceCard.setOnClickListener(v -> {

                });


                spaceVH.txtLineOne.setText(spaceItem.getSpacename());

                break;

            case LOADING:
                SpacesAdapter.LoadingVH loadingVH = (SpacesAdapter.LoadingVH) holder;

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

    @Override
    public int getItemCount() {
        return spaceItems == null ? 0 : spaceItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return SPACES;
        } else {
            return (position == spaceItems.size() - 1 && isLoadingAdded) ? LOADING : SPACES;
        }
    }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    private void add(SpaceItem r) {
        spaceItems.add(r);
        notifyItemInserted(spaceItems.size() - 1);
    }

    void addAll(List<SpaceItem> spaceItems) {
        for (SpaceItem result : spaceItems) {
            add(result);
        }
    }

    private void remove(SpaceItem r) {
        int position = spaceItems.indexOf(r);
        if (position > -1) {
            spaceItems.remove(position);
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
        add(new SpaceItem());
    }

    void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = spaceItems.size() - 1;
        SpaceItem spaceItem = getItem(position);

        if (spaceItem != null) {
            spaceItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    private SpaceItem getItem(int position) {
        return spaceItems.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(spaceItems.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Feed content ViewHolder
     */
    protected class SpaceVH extends RecyclerView.ViewHolder {
        private CardView spaceCard;
        private DynamicHeightTextView txtLineOne;

        SpaceVH(View itemView) {
            super(itemView);

            spaceCard = itemView.findViewById(R.id.panel_content);
            txtLineOne = itemView.findViewById(R.id.spacenameText);
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
