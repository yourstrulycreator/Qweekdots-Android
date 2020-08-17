package com.creator.qweekdots.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
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
import com.creator.qweekdots.activity.RingSpaceActivity;
import com.creator.qweekdots.grid.util.DynamicHeightTextView;
import com.creator.qweekdots.models.ChatRoom;
import com.creator.qweekdots.utils.PaginationAdapterCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

import static maes.tech.intentanim.CustomIntent.customType;

public class HotSpacesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // View Types
    private static final int RINGS = 0;
    private static final int LOADING = 1;

    private static final String TAG = MyRingsAdapter.class.getSimpleName();

    private ArrayList<ChatRoom> ringItems;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;
    private String username;
    private final Random mRandom;
    private final ArrayList<Integer> mBackgroundColors;

    public HotSpacesAdapter(Context context, Fragment f, String username, ArrayList<ChatRoom> ringItems) {
        this.context = context;
        this.username = username;
        this.ringItems = ringItems;

        mRandom = new Random();
        mBackgroundColors = new ArrayList<>();
        mBackgroundColors.add(R.color.DodgerBlue);
        mBackgroundColors.add(R.color.Tomato);
        mBackgroundColors.add(R.color.Coral);
        mBackgroundColors.add(R.color.SteelBlue);
        mBackgroundColors.add(R.color.DarkSlateBlue);
        mBackgroundColors.add(R.color.DodgerBlue);
        mBackgroundColors.add(R.color.DarkSlateGray);
        mBackgroundColors.add(R.color.ArgentinanBlue);
        mBackgroundColors.add(R.color.DeepPurple);
        mBackgroundColors.add(R.color.MediumSlateBlue);
        mBackgroundColors.add(R.color.VioletBlue);
        mBackgroundColors.add(R.color.SeaGreen);
        mBackgroundColors.add(R.color.CornflowerBlue);
        mBackgroundColors.add(R.color.DeepSkyBlue);
        mBackgroundColors.add(R.color.DarkTurquoise);
        mBackgroundColors.add(R.color.BottleGreen);
        mBackgroundColors.add(R.color.DarkCyan);
        mBackgroundColors.add(R.color.GoGreen);
        mBackgroundColors.add(R.color.JungleGreen);
        mBackgroundColors.add(R.color.Raspberry);
        mBackgroundColors.add(R.color.Folly);
        mBackgroundColors.add(R.color.WarriorsBlue);
        mBackgroundColors.add(R.color.SpaceCadet);
        mBackgroundColors.add(R.color.MajorelleBlue);
    }

    public ArrayList<ChatRoom> getMyRings() {
        return ringItems;
    }

    public void setMyRings(ArrayList<ChatRoom> ringItems) {
        this.ringItems = ringItems;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case RINGS:
                View viewItem = inflater.inflate(R.layout.hot_spaces_item, parent, false);
                viewHolder = new HotSpacesAdapter.RingsVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new HotSpacesAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context!=null) {
            ChatRoom ringItem = ringItems.get(position);

            switch (getItemViewType(position)) {
                case RINGS:
                    final HotSpacesAdapter.RingsVH ringsVH = (HotSpacesAdapter.RingsVH) holder;

                    int randomColor = mBackgroundColors.get(mRandom.nextInt(20));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ringsVH.ringsCard.setCardBackgroundColor(context.getColor(randomColor));
                    }

                    ringsVH.ringsCard.setOnClickListener(v -> {
                        Intent intent = new Intent(context, RingSpaceActivity.class);
                        intent.putExtra("chat_room_id", ringItem.getId());
                        intent.putExtra("name", ringItem.getName());
                        context.startActivity(intent);
                    });

                    ringsVH.txtLineOne.setText(ringItem.getName());

                    // View Click Listener
                    ringsVH.ringsCard.setOnClickListener(v -> {
                        ObjectAnimator animY = ObjectAnimator.ofFloat(ringsVH.ringsCard, "translationY", -100f, 0f);
                        animY.setDuration(1000); //1sec
                        animY.setInterpolator(new BounceInterpolator());
                        animY.setRepeatCount(1);
                        animY.start();
                        Intent i = new Intent(context, RingSpaceActivity.class);
                        i.putExtra("chat_room_id", ringItem.getId());
                        i.putExtra("name", ringItem.getName());
                        context.startActivity(i);
                        customType(context, "fadein-to-fadeout");
                    });

                    break;

                case LOADING:
                    HotSpacesAdapter.LoadingVH loadingVH = ( HotSpacesAdapter.LoadingVH) holder;

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
        return ringItems == null ? 0 : ringItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return RINGS;
        } else {
            return (position == ringItems.size() - 1 && isLoadingAdded) ? LOADING : RINGS;
        }
    }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */
    public void add(ChatRoom r) {
        ringItems.add(r);
        notifyItemInserted(ringItems.size() - 1);
    }

    public void addAll(ArrayList<ChatRoom> ringItems) {
        for (ChatRoom result : ringItems) {
            add(result);
        }
    }

    public void remove(ChatRoom r) {
        int position = ringItems.indexOf(r);
        if (position > -1) {
            ringItems.remove(position);
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
        add(new ChatRoom());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = ringItems.size() - 1;
        ChatRoom ringItem = getItem(position);

        if (ringItem != null) {
            ringItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    private ChatRoom getItem(int position) {
        return ringItems.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(ringItems.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Feed content ViewHolder
     */
    protected class RingsVH extends RecyclerView.ViewHolder {
        private CardView ringsCard;
        private DynamicHeightTextView txtLineOne;

        RingsVH(View itemView) {
            super(itemView);

            ringsCard = itemView.findViewById(R.id.ring_content);
            txtLineOne = itemView.findViewById(R.id.ringnameText);
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
