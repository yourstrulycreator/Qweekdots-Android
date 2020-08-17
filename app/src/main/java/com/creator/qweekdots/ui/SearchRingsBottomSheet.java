package com.creator.qweekdots.ui;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.ListedRingsAdapter;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.api.SearchRingsService;
import com.creator.qweekdots.models.Cursor;
import com.creator.qweekdots.models.SearchRingsModel;
import com.creator.qweekdots.models.SpaceItem;
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

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Context.SEARCH_SERVICE;

public class SearchRingsBottomSheet extends RoundedBottomSheetDialogFragment implements PaginationAdapterCallback {
    private final String TAG = SearchRingsBottomSheet.class.getSimpleName();

    private String queryString;
    private Context context;
    private String logged;

    private SearchRingsService feedService;
    private ListedRingsAdapter adapter;
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
    private String max_id;
    //private String since_id;

    private BottomSheetBehavior bottomSheetBehavior;
    private View view;

    SearchRingsBottomSheet(Context context, String logged) {
        this.context = context;
        this.logged = logged;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_rings_bottom_sheet, container, false);

        if(context!=null) {
            TextView titleTxtView = view.findViewById(R.id.optTitleSheetTxt);
            titleTxtView.setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                dismiss();
            });

            androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.searchbar);
            searchView.isFocused();
            searchView.onActionViewExpanded();
            SearchManager searchManager = (SearchManager) requireActivity().getSystemService(SEARCH_SERVICE);
            searchView.setSearchableInfo(Objects.requireNonNull(searchManager).getSearchableInfo(requireActivity().getComponentName()));
            Objects.requireNonNull(searchView).setIconifiedByDefault(false);

            EditText searchEditText;
            searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            searchEditText.setTextColor(getResources().getColor(R.color.contentTextColor));
            searchEditText.setHintTextColor(getResources().getColor(R.color.SlateGray));
            searchEditText.setHint("Search Spaces");

            androidx.appcompat.widget.SearchView.OnQueryTextListener queryTextListener = new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String query) {
                    // this is your adapter that will be filtered
                    //beginSearch(query);
                    return true;
                }

                public boolean onQueryTextSubmit(String query) {
                    // Here u can get the value "query" which is entered in the

                    beginSearch(query);
                    return true;

                }
            };

            searchView.setOnQueryTextListener(queryTextListener);

            //Init Search Layout
            RecyclerView rv = view.findViewById(R.id.search_recycler);
            progressBar = view.findViewById(R.id.mSearchProgressBar);
            errorLayout = view.findViewById(R.id.search_error_layout);
            emptyLayout = view.findViewById(R.id.search_empty_layout);
            Button btnRetry = view.findViewById(R.id.search_error_btn_retry);
            txtError = view.findViewById(R.id.search_error_txt_cause);
            swipeRefreshLayout = view.findViewById(R.id.search_swiperefresh);

            adapter = new ListedRingsAdapter(context, logged);

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
            feedService = QweekdotsApi.getClient(getContext()).create(SearchRingsService.class);

            btnRetry.setOnClickListener(v -> loadFirstPage());

            swipeRefreshLayout.setOnRefreshListener(this::doRefresh);
        }

        return view;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        //inflating layout
        view = View.inflate(context, R.layout.search_rings_bottom_sheet, null);

        View extraSpace = view.findViewById(R.id.extraSpace);

        //setting layout with bottom sheet
        bottomSheet.setContentView(view);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(0);
        //setting min height of bottom sheet
        extraSpace.setMinimumHeight((Resources.getSystem().getDisplayMetrics().heightPixels) / 2);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (BottomSheetBehavior.STATE_EXPANDED == i) {
                    bottomSheetBehavior.setDraggable(false);
                }
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss();
                }
            }
            @Override
            public void onSlide(@NonNull View view, float v) {}
        });
        return bottomSheet;
    }

    private void beginSearch(String query) {
        adapter.clear();
        queryString = query;

        loadFirstPage();
    }

    /**
     * Triggers the actual background refresh via the {@link SwipeRefreshLayout}
     */
    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callRingsSearchFeedApi().isExecuted())
            callRingsSearchFeedApi().cancel();

        // TODO: Check if data is stale.
        //  Execute network request if cache is expired; otherwise do not update data.
        adapter.getUsers().clear();
        adapter.notifyDataSetChanged();
        loadFirstPage();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Load First Page
     */
    private void loadFirstPage() {
        Timber.tag(TAG).d("loadFirstPage: ");
        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();
        callRingsSearchFeedApi().enqueue(new Callback<SearchRingsModel>() {
            @Override
            public void onResponse(@NotNull Call<SearchRingsModel> call, @NotNull Response<SearchRingsModel> response) {
                hideErrorView();
                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                List<SpaceItem> feedItem = fetchSearchFeed(response);

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
            public void onFailure(@NotNull Call<SearchRingsModel> call, @NotNull Throwable t) {
                t.printStackTrace();
                showErrorView();
            }
        });
    }

    private void loadNextPage() {
        Timber.tag(TAG).d("loadNextPage: %s", currentPage);
        callNextRingsSearchFeedApi().enqueue(new Callback<SearchRingsModel>() {
            @Override
            public void onResponse(@NotNull Call<SearchRingsModel> call, @NotNull Response<SearchRingsModel> response) {
                adapter.removeLoadingFooter();
                isLoading = false;

                List<SpaceItem> feedItems = fetchSearchFeed(response);
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
            public void onFailure(@NotNull Call<SearchRingsModel> call, @NotNull Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    /**
     * @param response extracts List<{@link SpaceItem>} from response
     */
    private List<SpaceItem> fetchSearchFeed(Response<SearchRingsModel> response) {
        SearchRingsModel searchFeed = response.body();
        assert searchFeed != null;
        return searchFeed.getUserItems();
    }

    /**
     * @param response extracts List<{@link Cursor >} from response
     */
    private List<Cursor> fetchCursorLinks(Response<SearchRingsModel> response) {
        SearchRingsModel searchFeed = response.body();
        assert searchFeed != null;
        return searchFeed.getCursorLinks();
    }

    /**
     * Performs a Retrofit call to the first QweekFeed API.
     * Same API call for Pagination.
     */
    private Call<SearchRingsModel> callRingsSearchFeedApi() {
        return feedService.getSearchedRings(
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
    private Call<SearchRingsModel> callNextRingsSearchFeedApi() {
        return feedService.getSearchedRings(
                queryString,
                logged,
                max_id,
                null
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
