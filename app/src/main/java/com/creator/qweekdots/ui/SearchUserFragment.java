package com.creator.qweekdots.ui;

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

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creator.qweekdots.R;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.api.SearchUserService;
import com.creator.qweekdots.models.Cursor;
import com.creator.qweekdots.models.SearchUserModel;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.prefs.DarkModePrefManager;
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

public class SearchUserFragment extends Fragment implements PaginationAdapterCallback {
    View rootView;
    private final String TAG = SearchUserFragment.class.getSimpleName();

    private SearchUserService feedService;
    private String queryString;

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

    private Context context;
    private String logged;

    SearchUserFragment(Context context, String logged) {
        this.context = context;
        this.logged = logged;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.search, container, false);

        if(new DarkModePrefManager(requireActivity()).isNightMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        RecyclerView rv = rootView.findViewById(R.id.search_recycler);
        progressBar = rootView.findViewById(R.id.mSearchProgressBar);
        errorLayout = rootView.findViewById(R.id.search_error_layout);
        emptyLayout = rootView.findViewById(R.id.search_empty_layout);
        Button btnRetry = rootView.findViewById(R.id.search_error_btn_retry);
        txtError = rootView.findViewById(R.id.search_error_txt_cause);
        swipeRefreshLayout = rootView.findViewById(R.id.search_swiperefresh);

        adapter = new ListedUsersAdapter(context, logged);

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

        /*Create handle for the RetrofitInstance interface*/
        feedService = QweekdotsApi.getClient(getContext()).create(SearchUserService.class);

        btnRetry.setOnClickListener(v -> loadFirstPage());

        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);

        return rootView;
    }

    /**
     * Triggers the actual background refresh via the {@link SwipeRefreshLayout}
     */
    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callUserSearchFeedApi().isExecuted())
            callUserSearchFeedApi().cancel();

        // TODO: Check if data is stale.
        //  Execute network request if cache is expired; otherwise do not update data.
        adapter.getUsers().clear();
        adapter.notifyDataSetChanged();
        loadFirstPage();
        swipeRefreshLayout.setRefreshing(false);
    }

    void beginSearch(String query) {
        adapter.clear();
        queryString = query;

        loadFirstPage();
    }

    private void loadFirstPage() {
        Timber.tag(TAG).d("loadFirstPage: ");

        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();

        callUserSearchFeedApi().enqueue(new Callback<SearchUserModel>() {
            @Override
            public void onResponse(@NotNull Call<SearchUserModel> call, @NotNull Response<SearchUserModel> response) {
                hideErrorView();

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                List<UserItem> feedItem = fetchSearchFeed(response);

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
            public void onFailure(Call<SearchUserModel> call, Throwable t) {
                t.printStackTrace();
                showErrorView();
            }
        });
    }

    private void loadNextPage() {
        Timber.tag(TAG).d("loadNextPage: %s", currentPage);

        callNextUserSearchFeedApi().enqueue(new Callback<SearchUserModel>() {
            @Override
            public void onResponse(@NotNull Call<SearchUserModel> call, @NotNull Response<SearchUserModel> response) {
//                Log.i(TAG, "onResponse: " + currentPage
//                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                adapter.removeLoadingFooter();
                isLoading = false;

                List<UserItem> feedItems = fetchSearchFeed(response);
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
            public void onFailure(Call<SearchUserModel> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    /**
     * @param response extracts List<{@link UserItem>} from response
     */
    private List<UserItem> fetchSearchFeed(Response<SearchUserModel> response) {
        SearchUserModel searchFeed = response.body();
        assert searchFeed != null;
        return searchFeed.getUserItems();
    }

    /**
     * @param response extracts List<{@link Cursor >} from response
     */
    private List<Cursor> fetchCursorLinks(Response<SearchUserModel> response) {
        SearchUserModel searchFeed = response.body();
        assert searchFeed != null;
        return searchFeed.getCursorLinks();
    }

    /**
     * Performs a Retrofit call to the first QweekFeed API.
     * Same API call for Pagination.
     */
    private Call<SearchUserModel> callUserSearchFeedApi() {
        return feedService.getSearchedUsers(
                queryString,
                logged,
                null,
                null
        );
    }

    /**
     * Performs a Retrofit call to the next QweekFeed API.
     * Same API call for Pagination.
     */
    private Call<SearchUserModel> callNextUserSearchFeedApi() {
        return feedService.getSearchedUsers(
                queryString,
                logged,
                max_id,
                null
        );
    }

    /**
     * Performs a Retrofit call to the next QweekFeed API.
     * Same API call for Pagination.
     */
    private Call<SearchUserModel> callPrevUserSearchFeedApi() {
        return feedService.getSearchedUsers(
                queryString,
                logged,
                null,
                since_id
        );
    }

    @Override
    public void retryPageLoad() {
        loadFirstPage();
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
