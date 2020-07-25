package com.creator.qweekdots.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creator.qweekdots.R;
import com.creator.qweekdots.api.FollowingFeedService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.models.Cursor;
import com.creator.qweekdots.models.FollowModel;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.creator.qweekdots.utils.PaginationScrollListener;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeoutException;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class FollowingBottomSheet extends RoundedBottomSheetDialogFragment implements PaginationAdapterCallback {
    private final String TAG = FollowingBottomSheet.class.getSimpleName();

    private ListedUsersAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    private SpinKitView progressBar;
    private LinearLayout errorLayout;
    private TextView txtError;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout emptyLayout;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private static final int PAGE_START = 1;
    private static int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    private String next_link;
    private String prev_link;
    private String max_id;
    private String since_id;

    private FollowingFeedService feedService;

    private String username, fullname, profile_pic, profile;

    private Context context;
    private BottomSheetBehavior bottomSheetBehavior;

    public FollowingBottomSheet(Context context, String profile, String username) {
        this.context = context;
        this.profile = profile;
        this.username = username;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        //inflating layout
        View view = View.inflate(getContext(), R.layout.ui_follows, null);

        if(context!=null) {
            View extraSpace = view.findViewById(R.id.extraSpace);

            RecyclerView rv = view.findViewById(R.id.follows_recycler);
            progressBar = view.findViewById(R.id.follows_spin_kit);
            errorLayout = view.findViewById(R.id.error_layout);
            emptyLayout = view.findViewById(R.id.empty_layout);
            Button btnRetry = view.findViewById(R.id.error_btn_retry);
            txtError = view.findViewById(R.id.error_txt_cause);
            swipeRefreshLayout = view.findViewById(R.id.follows_swiperefresh);

            adapter = new ListedUsersAdapter(getActivity(), username);

            linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            rv.setLayoutManager(linearLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());

            rv.setAdapter(adapter);

            rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
                @Override
                protected void loadMoreItems() {
                    isLoading = true;
                    currentPage += 1;

                    if(isNetworkConnected()) {
                        loadNextPage();
                    } else {
                        Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                        adapter.removeLoadingFooter();

                        Timber.tag(TAG).d("No internet connection available");
                    }
                }

                @Override
                public int getTotalPageCount() {
                    return TOTAL_PAGES;
                }

                @Override
                public boolean isLastPage() {
                    return isLastPage;
                }

                @Override
                public boolean isLoading() {
                    return isLoading;
                }
            });

            //init service and load data
            feedService = QweekdotsApi.getClient(context).create(FollowingFeedService.class);


            loadFirstPage();

            btnRetry.setOnClickListener(v -> loadFirstPage());

            swipeRefreshLayout.setOnRefreshListener(this::doRefresh);

            //setting layout with bottom sheet
            bottomSheet.setContentView(view);

            bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));


            //setting Peek
            bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);


            //setting min height of bottom sheet
            extraSpace.setMinimumHeight((Resources.getSystem().getDisplayMetrics().heightPixels) / 2);


            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int i) {
                    if (BottomSheetBehavior.STATE_EXPANDED == i) {

                    }
                    if (BottomSheetBehavior.STATE_COLLAPSED == i) {

                    }

                    if (BottomSheetBehavior.STATE_HIDDEN == i) {
                        dismiss();
                    }

                }

                @Override
                public void onSlide(@NonNull View view, float v) {

                }
            });
        }


        return bottomSheet;
    }

    /**
     * Triggers the actual background refresh via the {@link SwipeRefreshLayout}
     */
    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callFollowingFeedApi().isExecuted())
            callFollowingFeedApi().cancel();

        // TODO: Check if data is stale.
        //  Execute network request if cache is expired; otherwise do not update data.
        adapter.getUsers().clear();
        adapter.notifyDataSetChanged();
        loadFirstPage();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadFirstPage() {
        Timber.tag(TAG).d("loadFirstPage: ");

        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();

        callFollowingFeedApi().enqueue(new Callback<FollowModel>() {
            @Override
            public void onResponse(@NotNull Call<FollowModel> call, @NotNull Response<FollowModel> response) {
                hideErrorView();

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                List<UserItem> feedItem = fetchNewsFeed(response);

                if(feedItem.isEmpty()) {
                    showEmptyView();
                } else {

                    progressBar.setVisibility(View.GONE);
                    adapter.addAll(feedItem);

                    // Cursor Links
                    List<Cursor> cursor = fetchCursorLinks(response);
                    Cursor cursorLink = cursor.get(0);
                    next_link = cursorLink.getNextLink();
                    prev_link = cursorLink.getPrevLink();
                    max_id = cursorLink.getMaxID();
                    since_id = cursorLink.getSinceID();

                    TOTAL_PAGES = cursorLink.getPagesNum();

                    if(TOTAL_PAGES == 1) {
                        isLastPage = true;
                    } else {
                        if (currentPage < TOTAL_PAGES) {
                            adapter.addLoadingFooter();
                        } else if(currentPage == TOTAL_PAGES) {
                            isLastPage = true;
                        }
                    }
                }
            }

            @Override
            public void onFailure( Call<FollowModel> call, Throwable t) {
                t.printStackTrace();
                showErrorView();
            }
        });
    }

    /**
     * @param response extracts List<{@link UserItem>} from response
     */
    private List<UserItem> fetchNewsFeed(Response<FollowModel> response) {
        FollowModel followersFeed = response.body();
        assert followersFeed != null;
        return followersFeed.getUserItems();
    }

    /**
     * @param response extracts List<{@link Cursor>} from response
     */
    private List<Cursor> fetchCursorLinks(Response<FollowModel> response) {
        FollowModel followersFeed = response.body();
        assert followersFeed != null;
        return followersFeed.getCursorLinks();
    }

    private void loadNextPage() {
        Timber.tag(TAG).d("loadNextPage: %s", currentPage);

        callNextFollowingFeedApi().enqueue(new Callback<FollowModel>() {
            @Override
            public void onResponse(@NotNull Call<FollowModel> call, @NotNull Response<FollowModel> response) {
//                Log.i(TAG, "onResponse: " + currentPage
//                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                adapter.removeLoadingFooter();
                isLoading = false;

                List<UserItem> feedItems = fetchNewsFeed(response);
                adapter.addAll(feedItems);

                // Cursor Links
                List<Cursor> cursor = fetchCursorLinks(response);
                Cursor cursorLink = cursor.get(0);
                next_link = cursorLink.getNextLink();
                prev_link = cursorLink.getPrevLink();
                max_id = cursorLink.getMaxID();
                since_id = cursorLink.getSinceID();

                if (currentPage != TOTAL_PAGES) {
                    adapter.addLoadingFooter();
                } else {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<FollowModel> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    /**
     * Performs a Retrofit call to the first QweekFeed API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<FollowModel> callFollowingFeedApi() {
        return feedService.getFollowing(
                profile,
                username,
                null,
                null
        );
    }

    /**
     * Performs a Retrofit call to the next QweekFeed API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<FollowModel> callNextFollowingFeedApi() {
        return feedService.getFollowing(
                profile,
                username,
                max_id,
                null
        );
    }

    /**
     * Performs a Retrofit call to the next QweekFeed API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<FollowModel> callPrevFollowingFeedApi() {
        return feedService.getFollowing(
                profile,
                username,
                null,
                since_id
        );
    }

    public void retryPageLoad() {
        loadNextPage();
    }

    /**
     */
    private void showErrorView() {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(getResources().getString(R.string.error_msg_unknown));
        }
    }

    private void showEmptyView() {
        if (emptyLayout.getVisibility() == View.GONE) {
            emptyLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * @param throwable to identify the type of error
     * @return appropriate error message
     */
    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }

        return errorMsg;
    }

    // Helpers -------------------------------------------------------------------------------------


    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyView() {
        if (emptyLayout.getVisibility() == View.VISIBLE) {
            emptyLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Remember to add android.permission.ACCESS_NETWORK_STATE permission.
     *
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onStart() {
        super.onStart();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}
