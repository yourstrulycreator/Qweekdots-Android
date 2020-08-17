package com.creator.qweekdots.ui;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.AddChatUsersAdapter;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.api.SearchUserService;
import com.creator.qweekdots.models.Cursor;
import com.creator.qweekdots.models.SearchUserModel;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.creator.qweekdots.utils.PaginationScrollListener;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Context.SEARCH_SERVICE;

public class AddChatBottomSheet extends RoundedBottomSheetDialogFragment implements PaginationAdapterCallback {
    private final String TAG = AddChatBottomSheet.class.getSimpleName();
    private BottomSheetBehavior bottomSheetBehavior;
    private View view;
    private Context context;
    private String logged;

    private String queryString;
    private SearchUserService feedService;

    private AddChatUsersAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    private SpinKitView progressBar;
    private LinearLayout errorLayout;
    private TextView txtError;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout emptyLayout;

    private androidx.appcompat.widget.SearchView searchView;
    private androidx.appcompat.widget.SearchView.OnQueryTextListener queryTextListener;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private static final int PAGE_START = 1;
    private static int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    private String max_id;
    //private String since_id;


    public AddChatBottomSheet(Context context, String logged) {
        this.context = context;
        this.logged = logged;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.add_chat_bottom_sheet, container, false);

        if(context!=null) {
            // Initialise service
            feedService = QweekdotsApi.getClient(getContext()).create(SearchUserService.class);

            // Initialize Search
            initSearchBar();

            // Init RecyclerView
            initRecyclerView();

            // Init rest of layout
            progressBar = view.findViewById(R.id.mSearchProgressBar);
            errorLayout = view.findViewById(R.id.error_layout);
            emptyLayout = view.findViewById(R.id.empty_layout);
            Button btnRetry = view.findViewById(R.id.error_btn_retry);
            txtError = view.findViewById(R.id.error_txt_cause);
            swipeRefreshLayout = view.findViewById(R.id.search_swiperefresh);

            // Set query listener
            setQueryListener();
            // Run Search
            searchView.setOnQueryTextListener(queryTextListener);

            // Refresh/ Retry
            btnRetry.setOnClickListener(v -> loadFirstPage());
            swipeRefreshLayout.setOnRefreshListener(this::doRefresh);

            // Click TextView to Dismiss Bottom Sheet (A workaround)
            TextView titleTxtView = view.findViewById(R.id.optTitleSheetTxt);
            titleTxtView.setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                dismiss();
            });
        }

        return view;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        //inflating layout
        view = View.inflate(context, R.layout.add_chat_bottom_sheet, null);

        //setting layout with bottom sheet
        bottomSheet.setContentView(view);

        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(0);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {

                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss();
                }

            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        return bottomSheet;
    }

    /*
     * Initialise SearchBar
     */
    private void initSearchBar() {
        searchView = view.findViewById(R.id.searchbar);
        searchView.isFocused();
        searchView.onActionViewExpanded();
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(Objects.requireNonNull(searchManager).getSearchableInfo(requireActivity().getComponentName()));
        Objects.requireNonNull(searchView).setIconifiedByDefault(false);

        EditText searchEditText;
        searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.contentTextColor));
        searchEditText.setHintTextColor(getResources().getColor(R.color.SlateGray));
        searchEditText.setHint("Find Someone");
    }

    /*
     * Run Search Listener
     */
    private void setQueryListener() {
        queryTextListener = new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                //beginSearch(query);
                return true;

            }

            public boolean onQueryTextSubmit(String query) {
                // Here u can get the value "query" which is entered in the
                beginSearch(query);

                return true;

            }
        };
    }

    /*
     * Initialise RecyclerView
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initRecyclerView() {
        RecyclerView rv = view.findViewById(R.id.search_recycler);

        adapter = new AddChatUsersAdapter(context, this);

        linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);

        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapter);

        rv.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> bottomSheetBehavior.setDraggable(false));

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

    private void beginSearch(String query) {
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
            public void onFailure(@NotNull Call<SearchUserModel> call, @NotNull Throwable t) {
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
                max_id = cursorLink.getMaxID();
                //since_id = cursorLink.getSinceID();

                if (currentPage != TOTAL_PAGES) {
                    adapter.addLoadingFooter();
                } else {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(@NotNull Call<SearchUserModel> call, @NotNull Throwable t) {
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

    private Call<SearchUserModel> callPrevUserSearchFeedApi() {
        return feedService.getSearchedUsers(
                queryString,
                logged,
                null,
                since_id
        );
    }
     */

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
