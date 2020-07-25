package com.creator.qweekdots.ui;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.creator.qweekdots.R;
import com.creator.qweekdots.api.NotificationService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.Cursor;
import com.creator.qweekdots.models.NotificationItem;
import com.creator.qweekdots.models.NotificationsFeed;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.creator.qweekdots.utils.PaginationScrollListener;
import com.github.ybq.android.spinkit.SpinKitView;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class Notifications extends Fragment implements PaginationAdapterCallback {
    private final String TAG = Notifications.class.getSimpleName();

    NotificationAdapter adapter;
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

    private NotificationService notificationService;

    private String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.notifications, container, false);

        if(new DarkModePrefManager(requireActivity()).isNightMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(requireActivity().getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        username = user.get("username");

        RecyclerView rv = rootView.findViewById(R.id.main_recycler);
        progressBar = rootView.findViewById(R.id.spin_kit);
        errorLayout = rootView.findViewById(R.id.error_layout);
        emptyLayout = rootView.findViewById(R.id.empty_layout);
        Button btnRetry = rootView.findViewById(R.id.error_btn_retry);
        txtError = rootView.findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = rootView.findViewById(R.id.main_swiperefresh);

        adapter = new NotificationAdapter(getActivity(), this, username);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapter);

        rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                loadNextPage();
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
        notificationService = QweekdotsApi.getClient(getContext()).create(NotificationService.class);

        if(isAdded()) {
            if (isNetworkConnected()) {
                loadFirstPage();

                Timber.tag(TAG).d("Loading First Notifications Page");
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);

                Timber.tag(TAG).d("No internet connection available");
            }
        }

        btnRetry.setOnClickListener(view -> loadFirstPage());

        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);

        return rootView;
    }

    /**
     * Triggers the actual background refresh via the {@link SwipeRefreshLayout}
     */
    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callNotificationsApi().isExecuted())
            callNotificationsApi().cancel();

        // TODO: Check if data is stale.
        //  Execute network request if cache is expired; otherwise do not update data.
        adapter.getQweekFeed().clear();
        adapter.notifyDataSetChanged();
        loadFirstPage();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ");

        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();

        callNotificationsApi().enqueue(new Callback<NotificationsFeed>() {
            @Override
            public void onResponse(@NotNull Call<NotificationsFeed> call, @NotNull Response<NotificationsFeed> response) {
                hideErrorView();

                Timber.tag(TAG).i("onResponse: " + (response.raw().cacheResponse() != null ? "Cache" : "Network"));


                    // Got data. Send it to adapter
                    List<NotificationItem> notificationItem = fetchNotifications(response);
                    if(notificationItem.isEmpty()) {
                        //showEmptyView();
                        progressBar.setVisibility(View.GONE);
                        emptyLayout.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        adapter.addAll(notificationItem);

                        // Cursor Links
                        List<Cursor> cursor = fetchCursorLinks(response);
                        Cursor cursorLink = cursor.get(0);
                        next_link = cursorLink.getNextLink();
                        prev_link = cursorLink.getPrevLink();
                        max_id = cursorLink.getMaxID();
                        since_id = cursorLink.getSinceID();

                        TOTAL_PAGES = cursorLink.getPagesNum();

                        if (currentPage <= TOTAL_PAGES) {
                            isLastPage = true;
                            progressBar.setVisibility(View.GONE);
                        }
                    }
            }

            @Override
            public void onFailure(Call<NotificationsFeed> call, Throwable t) {
                t.printStackTrace();
                showErrorView();
            }
        });
    }

    /**
     * @param response extracts List<{@link NotificationItem>} from response
     */
    private List<NotificationItem> fetchNotifications(Response<NotificationsFeed> response) {
        NotificationsFeed notificationsFeed = response.body();
        assert notificationsFeed != null;
        return notificationsFeed.getNotificationItem();
    }

    /**
     * @param response extracts List<{@link Cursor>} from response
     */
    private List<Cursor> fetchCursorLinks(Response<NotificationsFeed> response) {
        NotificationsFeed notificationsFeed = response.body();
        assert notificationsFeed != null;
        return notificationsFeed.getCursorLinks();
    }

    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callNextNotificationsApi().enqueue(new Callback<NotificationsFeed>() {
            @Override
            public void onResponse(@NotNull Call<NotificationsFeed> call, @NotNull Response<NotificationsFeed> response) {
//                Log.i(TAG, "onResponse: " + currentPage
//                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                adapter.removeLoadingFooter();
                isLoading = false;

                List<NotificationItem> notificationItems = fetchNotifications(response);
                adapter.addAll(notificationItems);

                // Cursor Links
                List<Cursor> cursor = fetchCursorLinks(response);
                Cursor cursorLink = cursor.get(0);
                next_link = cursorLink.getNextLink();
                prev_link = cursorLink.getPrevLink();
                max_id = cursorLink.getMaxID();
                since_id = cursorLink.getSinceID();

                if (currentPage != TOTAL_PAGES) {
                    isLastPage = true;
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<NotificationsFeed> call, @NotNull Throwable t) {
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
    private Call<NotificationsFeed> callNotificationsApi() {
        return notificationService.getNotificationsFeed(
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
    private Call<NotificationsFeed> callNextNotificationsApi() {
        return notificationService.getNotificationsFeed(
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
    private Call<NotificationsFeed> callPrevNotificationsApi() {
        return notificationService.getNotificationsFeed(
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
        return Objects.requireNonNull(cm).getActiveNetworkInfo() != null;
    }


}
