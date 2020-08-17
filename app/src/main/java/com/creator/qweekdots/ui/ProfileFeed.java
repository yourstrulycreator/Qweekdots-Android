package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.PaginationAdapter;
import com.creator.qweekdots.api.ProfileFeedService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.models.Cursor;
import com.creator.qweekdots.models.FeedItem;
import com.creator.qweekdots.models.NewsFeed;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.creator.qweekdots.utils.PaginationScrollListener;
import com.github.ybq.android.spinkit.SpinKitView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeoutException;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ProfileFeed extends Fragment implements PaginationAdapterCallback {
    private String profile;
    String username;
    private Context context;

    private PaginationAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    private final String TAG = ProfileFeed.class.getSimpleName();

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
    private String max_id;
    //private String since_id;

    private ProfileFeedService feedService;

    public ProfileFeed() {
    }

    @SuppressLint("ValidFragment")
    public ProfileFeed(Context context, String profile, String username) {
        this.context = context;
        this.profile = profile;
        this.username = username;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_feed, container, false);
        if(context!=null) {
            RecyclerView rv = rootView.findViewById(R.id.profile_recycler);
            progressBar = rootView.findViewById(R.id.spin_kit);
            errorLayout = rootView.findViewById(R.id.error_layout);
            emptyLayout = rootView.findViewById(R.id.empty_layout);
            Button btnRetry = rootView.findViewById(R.id.error_btn_retry);
            txtError = rootView.findViewById(R.id.error_txt_cause);
            swipeRefreshLayout = rootView.findViewById(R.id.main_swiperefresh);

            linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            rv.setLayoutManager(linearLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.setLayoutManager(linearLayoutManager);
            rv.setHasFixedSize(true);

            adapter = new PaginationAdapter(getActivity(), getTargetFragment(), username);

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

            feedService = QweekdotsApi.getClient(getContext()).create(ProfileFeedService.class);

            if(isAdded()) {
                loadFirstPage();
            }

            btnRetry.setOnClickListener(view -> loadFirstPage());
            swipeRefreshLayout.setOnRefreshListener(this::doRefresh);
        }
        return rootView;
    }

    /**
     * Triggers the actual background refresh via the {@link SwipeRefreshLayout}
     */
    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callNewsFeedApi().isExecuted())
            callNewsFeedApi().cancel();

        // TODO: Check if data is stale.
        //  Execute network request if cache is expired; otherwise do not update data.
        adapter.getQweekFeed().clear();
        adapter.notifyDataSetChanged();
        loadFirstPage();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Load First Page of Profile Feed
     */
    private void loadFirstPage() {
        Timber.tag(TAG).d("loadFirstPage: ");
        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();
        hideEmptyView();
        callNewsFeedApi().enqueue(new Callback<NewsFeed>() {
            @Override
            public void onResponse(@NotNull Call<NewsFeed> call, @NotNull Response<NewsFeed> response) {
                hideErrorView();
                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));
                // Got data. Send it to adapter
                List<FeedItem> feedItem = fetchNewsFeed(response);
                if(feedItem.isEmpty()) {
                    showEmptyView();
                } else {
                    progressBar.setVisibility(View.GONE);
                    adapter.addAll(feedItem);

                    // Cursor Links
                    List<Cursor> cursor = fetchCursorLinks(response);
                    Cursor cursorLink = cursor.get(0);
                    max_id = cursorLink.getMaxID();
                    //since_id = cursorLink.getSinceID();

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
            public void onFailure(@NotNull Call<NewsFeed> call, @NotNull Throwable t) {
                t.printStackTrace();
                showErrorView();
            }
        });
    }

    /**
     * @param response extracts List<{@link FeedItem>} from response
     */
    private List<FeedItem> fetchNewsFeed(Response<NewsFeed> response) {
        NewsFeed newsFeed = response.body();
        assert newsFeed != null;
        return newsFeed.getFeedItems();
    }

    /**
     * @param response extracts List<{@link Cursor>} from response
     */
    private List<Cursor> fetchCursorLinks(Response<NewsFeed> response) {
        NewsFeed newsFeed = response.body();
        assert newsFeed != null;
        return newsFeed.getCursorLinks();
    }

    private void loadNextPage() {
        Timber.tag(TAG).d("loadNextPage: %s", currentPage);
        callNextNewsFeedApi().enqueue(new Callback<NewsFeed>() {
            @Override
            public void onResponse(@NotNull Call<NewsFeed> call, @NotNull Response<NewsFeed> response) {
                adapter.removeLoadingFooter();
                isLoading = false;

                List<FeedItem> feedItems = fetchNewsFeed(response);
                adapter.addAll(feedItems);

                // Cursor Links
                List<Cursor> cursor = fetchCursorLinks(response);
                Cursor cursorLink = cursor.get(0);
                max_id = cursorLink.getMaxID();
                //since_id = cursorLink.getSinceID();

                if (currentPage != TOTAL_PAGES) {
                    adapter.addLoadingFooter();
                } else {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(@NotNull Call<NewsFeed> call, @NotNull Throwable t) {
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
    private Call<NewsFeed> callNewsFeedApi() {
        return feedService.getNewsFeed(
                username,
                profile,
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
    private Call<NewsFeed> callNextNewsFeedApi() {
        return feedService.getNewsFeed(
                username,
                profile,
                max_id,
                null
        );
    }

    /**
     * Performs a Retrofit call to the next QweekFeed API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     *
    private Call<NewsFeed> callPrevNewsFeedApi() {
        return feedService.getNewsFeed(
                username,
                profile,
                null,
                since_id
        );
    }
     */

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

}
