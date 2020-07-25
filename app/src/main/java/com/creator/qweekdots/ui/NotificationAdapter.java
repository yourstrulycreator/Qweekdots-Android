package com.creator.qweekdots.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.models.NotificationItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static maes.tech.intentanim.CustomIntent.customType;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int NOTIFICATIONS= 0;
    private static final int LOADING = 1;

    private List<NotificationItem> notificationItems;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;
    private String username;

    NotificationAdapter(Context context, Fragment f, String username) {
        this.context = context;
        this.username = username;
        this.mCallback = (PaginationAdapterCallback) f;
        notificationItems = new ArrayList<>();
    }

    List<NotificationItem> getQweekFeed() {
        return notificationItems;
    }

    public void setNewsFeed(List<NotificationItem> notificationItems) {
        this.notificationItems = notificationItems;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case NOTIFICATIONS:
                View viewItem = inflater.inflate(R.layout.notification_item, parent, false);
                viewHolder = new NotificationAdapter.NotificationVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new NotificationAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context!=null) {
            NotificationItem notificationItem = notificationItems.get(position);//TimeAgo timestamp

            switch (getItemViewType(position)) {

                case NOTIFICATIONS:
                    final NotificationAdapter.NotificationVH notificationVH = (NotificationAdapter.NotificationVH) holder;

                    notificationVH.status.setVisibility(View.VISIBLE);
                    notificationVH.status.setText(notificationItem.getStatus());

                    // load User ProfileModel Picture
                    Picasso.get().load(notificationItem.getAvatar())
                            .resize(60, 60)
                            .placeholder(R.drawable.ic_alien)
                            .error(R.drawable.ic_alien)
                            .into(notificationVH.profilePic);

                    notificationVH.profilePic.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", notificationItem.getUsername());
                        context.startActivity(i);
                        customType(context, "up-to-bottom");
                    });

                    if(notificationItem.getNotificationType().equals("like")&&notificationItem.getNotificationType().equals("comment")) {
                        notificationVH.noteLayout.setClickable(true);
                        notificationVH.noteLayout.setOnClickListener(v->{
                            DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                            FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                            bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                        });
                    } else {
                        notificationVH.noteLayout.setClickable(false);
                    }

                    break;

                case LOADING:
                    NotificationAdapter.LoadingVH loadingVH = (NotificationAdapter.LoadingVH) holder;

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
        return notificationItems == null ? 0 : notificationItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return NOTIFICATIONS;
        } else {
            return (position == notificationItems.size() - 1 && isLoadingAdded) ? LOADING : NOTIFICATIONS;
        }
    }

    /*
        Helpers - bind Views
   _________________________________________________________________________________________________
    */


    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    private void add(NotificationItem r) {
        notificationItems.add(r);
        notifyItemInserted(notificationItems.size() - 1);
    }

    void addAll(List<NotificationItem> notificationItems) {
        for (NotificationItem result : notificationItems) {
            add(result);
        }
    }

    private void remove(NotificationItem r) {
        int position = notificationItems.indexOf(r);
        if (position > -1) {
            notificationItems.remove(position);
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
        add(new NotificationItem());
    }

    void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = notificationItems.size() - 1;
        NotificationItem notificationItem = getItem(position);

        if (notificationItem != null) {
            notificationItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    private NotificationItem getItem(int position) {
        return notificationItems.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param errorMsg to display if page load fails
     */
    void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(notificationItems.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Feed content ViewHolder
     */
    protected class NotificationVH extends RecyclerView.ViewHolder {
        private TextView status;
        private CircleImageView profilePic;
        private LinearLayout noteLayout;

        NotificationVH(View itemView) {
            super(itemView);

            status = itemView.findViewById(R.id.status);
            profilePic = itemView.findViewById(R.id.profilePic);
            noteLayout = itemView.findViewById(R.id.noteLayout);
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
