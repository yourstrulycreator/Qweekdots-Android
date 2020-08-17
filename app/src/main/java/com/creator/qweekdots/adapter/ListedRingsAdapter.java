package com.creator.qweekdots.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.creator.qweekdots.activity.RingSpaceActivity;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.models.SpaceItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class ListedRingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // View Types
    private static final int USERS = 0;
    private static final int LOADING = 1;

    private List<SpaceItem> userItems;
    private Context context;

    private PaginationAdapterCallback mCallback;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private String errorMsg;
    private String username;

    private ListedRingsAdapter.RingVH ringVH;

    private final String TAG = ListedUsersAdapter.class.getSimpleName();

    private final Random mRandom;
    private final ArrayList<Integer> mBackgroundColors;

    public ListedRingsAdapter(Context context, RoundedBottomSheetDialogFragment f, String username) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) f;
        this.username = username;
        userItems = new ArrayList<>();

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

    public ListedRingsAdapter(Context context, String username) {
        this.context = context;
        this.username = username;
        userItems = new ArrayList<>();

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

    public List<SpaceItem> getUsers() {
        return userItems;
    }

    public void setUsers(List<SpaceItem> userItems) {
        this.userItems = userItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case USERS:
                View viewItem = inflater.inflate(R.layout.rings_listed, parent, false);
                viewHolder = new ListedRingsAdapter.RingVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new ListedRingsAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context!=null) {
            SpaceItem spaceItem = userItems.get(position);

            switch (getItemViewType(position)) {

                case USERS:
                    ringVH = (ListedRingsAdapter.RingVH) holder;

                    int randomColor = mBackgroundColors.get(mRandom.nextInt(20));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ((RingVH) holder).spaceImage.setBackgroundColor(context.getColor(randomColor));
                    }

                    ((RingVH) holder).spacenameTxt.setText(spaceItem.getSpacename());

                    // Build Follow
                    String pinned = spaceItem.getPinned();
                    if(pinned.equals("yes")) {
                        ((RingVH) holder).pinActionButton.setText("Pinned");
                    } else {
                        ((RingVH) holder).pinActionButton.setText("Pin");
                    }

                    // Click to view profile
                    ((ListedRingsAdapter.RingVH) holder).rClickLayout.setOnClickListener(v -> {
                        Intent i = new Intent(context, RingSpaceActivity.class);
                        i.putExtra("chat_room_id", spaceItem.getId());
                        i.putExtra("name", spaceItem.getSpacename());
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                        customType(context, "fadein-to-fadeout");
                    });

                    /*
                     * Follow User, first check if user followed already
                     * If yes, set follow status to no along with text change to 'Follow'
                     * else, set follow status to yes along with text change to 'Following'
                     */
                    ((RingVH) holder).pinActionButton.setOnClickListener(v -> {
                        ((RingVH) holder).pinActionButton.startAnimation();
                        if(pinned.equals("yes")) {
                            doPin(username, "unpin", String.valueOf(spaceItem.getId()));
                            spaceItem.setPinned("no");
                        } else if(pinned.equals("no")) {
                            doPin(username, "pin", String.valueOf(spaceItem.getId()));
                            spaceItem.setPinned("yes");
                        }
                    });

                    break;

                case LOADING:
                    ListedRingsAdapter.LoadingVH loadingVH = (ListedRingsAdapter.LoadingVH) holder;

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
    private void doPin(String pinner, String type, String pinned) {
        // Tag used to cancel the request
        String tag_string_req = "req_pin";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/parse/space_pin.php", response -> {
            Timber.tag(TAG).d("Pin Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Stop animation
                    stopButtonAnimation();

                    if(type.equals("pin")) {
                        ringVH.pinActionButton.setText("Pinned");
                        ringVH.pinActionButton.saveInitialState();
                    } else {
                        ringVH.pinActionButton.setText("Pin");
                        ringVH.pinActionButton.saveInitialState();
                    }

                    String sent = "Pinned to MySpaces";
                    Toasty.success(context, sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_LONG).show();
                stopButtonAnimation();
            }

        }, error -> {
            Timber.tag(TAG).e("Pin Error: %s", error.getMessage());
            Toasty.error(context,
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("pinner", pinner);
                params.put("type", type);
                params.put("pinned", pinned);

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

    public void add(SpaceItem r) {
        userItems.add(r);
        notifyItemInserted(userItems.size() - 1);
    }

    public void addAll(List<SpaceItem> spaceItems) {
        for (SpaceItem result : spaceItems) {
            add(result);
        }
    }

    public void remove(SpaceItem r) {
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
        add(new SpaceItem());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = userItems.size() - 1;
        SpaceItem spaceItem = getItem(position);

        if (spaceItem != null) {
            userItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public SpaceItem getItem(int position) {
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

    private void stopButtonAnimation(){
        ringVH.pinActionButton.revertAnimation();
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Feed content ViewHolder
     */
    protected class RingVH extends RecyclerView.ViewHolder {
        private ImageView spaceImage;
        private TextView spacenameTxt;
        private LinearLayout rClickLayout;
        private CircularProgressButton pinActionButton;

        RingVH(View itemView) {
            super(itemView);

            spaceImage = itemView.findViewById(R.id.spaceColor);
            pinActionButton = itemView.findViewById(R.id.pinActionButton);
            spacenameTxt = itemView.findViewById(R.id.spacenameTextView);
            rClickLayout = itemView.findViewById(R.id.rClickLayout);
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
