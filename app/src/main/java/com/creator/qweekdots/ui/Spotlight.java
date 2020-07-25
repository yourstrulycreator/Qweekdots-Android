package com.creator.qweekdots.ui;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.creator.qweekdots.R;
import com.creator.qweekdots.api.ExplorePhotosService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.api.SuggestionsService;
import com.creator.qweekdots.api.TrendsService;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.ExplorePhotos;
import com.creator.qweekdots.models.Pager;
import com.creator.qweekdots.models.PhotoItem;
import com.creator.qweekdots.models.Suggestions;
import com.creator.qweekdots.models.TrendsItem;
import com.creator.qweekdots.models.TrendsModel;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.view.StaggeredGridView;
import com.creator.qweekdots.view.StaggeredGridViewItem;
import com.github.ybq.android.spinkit.SpinKitView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class Spotlight extends Fragment {
    View rootView;
    private final String TAG = Spotlight.class.getSimpleName();

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private TrendsAdapter trendsAdapter;
    private TrendsService trendsService;
    private SuggestionsAdapter sugAdapter;
    private SuggestionsService suggestionsService;
    private ExplorePhotosService explorePhotosService;
    private LinearLayout trendsErrorLayout, trendsEmptyLayout, suggestionsErrorLayout, suggestionsEmptyLayout, exploreErrorLayout, exploreEmptyLayout;
    private TextView trendsTxtError, suggestionsTxtError, exploreTxtError;
    private SpinKitView sugProgress, trendsProgress, exploreProgress;
    private StaggeredGridView mStaggeredView;
    private static final int PAGE_START = 1;
    private int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    private String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.spotlight, container, false);

        if(new DarkModePrefManager(requireActivity()).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(requireActivity().getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");

        // initialize spotlight layout elements
        RecyclerView sugV = rootView.findViewById(R.id.suggestionsRecyclerView);
        RecyclerView trendsV = rootView.findViewById(R.id.trendsRecyclerView);
        sugProgress = rootView.findViewById(R.id.suggestionsProgress);
        exploreProgress = rootView.findViewById(R.id.exploreProgress);
        trendsProgress = rootView.findViewById(R.id.trendsProgress);
        trendsErrorLayout = rootView.findViewById(R.id.trends_error_layout);
        trendsEmptyLayout = rootView.findViewById(R.id.trends_empty_layout);
        suggestionsErrorLayout = rootView.findViewById(R.id.suggestions_error_layout);
        suggestionsEmptyLayout = rootView.findViewById(R.id.suggestions_empty_layout);
        exploreErrorLayout = rootView.findViewById(R.id.explore_error_layout);
        exploreEmptyLayout = rootView.findViewById(R.id.explore_empty_layout);
        Button trendsBtnRetry = rootView.findViewById(R.id.trends_error_btn_retry);
        Button suggestionsBtnRetry = rootView.findViewById(R.id.suggestions_error_btn_retry);
        Button exploreBtnRetry = rootView.findViewById(R.id.explore_error_btn_retry);
        trendsTxtError = rootView.findViewById(R.id.trends_error_txt_cause);
        suggestionsTxtError = rootView.findViewById(R.id.suggestions_error_txt_cause);
        exploreTxtError = rootView.findViewById(R.id.explore_error_txt_cause);

        // getting search functionality to run
        EditText search = rootView.findViewById(R.id.search_bar);
        search.setFocusable(false);
        search.setClickable(true);
        search.setLongClickable(false);
        search.setOnClickListener(v -> {
            SearchBottomSheet bottomSheet = new SearchBottomSheet(getActivity(), username);
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });

        //Trends
        LinearLayoutManager trendsLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        trendsV.setLayoutManager(trendsLinearLayoutManager);
        trendsAdapter = new TrendsAdapter(getActivity(), this, username);
        trendsV.setAdapter(trendsAdapter);
        trendsV.setItemAnimator(new DefaultItemAnimator());

        // Suggestions
        LinearLayoutManager sugLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        sugV.setLayoutManager(sugLinearLayoutManager);
        sugAdapter = new SuggestionsAdapter(getActivity(), this, username);
        sugV.setAdapter(sugAdapter);
        sugV.setItemAnimator(new DefaultItemAnimator());

        // Explore Photos w/ Scroll Handling
        StaggeredGridView.OnScrollListener scrollListener = new StaggeredGridView.OnScrollListener() {
            public void onTop() {
            }

            public void onScroll() {
            }

            public void onBottom() {
                if (!isLoading() && !isLastPage()) {
                    isLoading = true;
                    currentPage += 1;

                    if(isNetworkConnected()) {
                        loadMoreExplore();
                    } else {
                        Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                        exploreProgress.setVisibility(View.GONE);

                        Timber.tag(TAG).d("No internet connection available");
                    }
                }
            }
        };
        mStaggeredView = rootView.findViewById(R.id.staggeredview);
        // Be sure before calling initialize that you haven't initialised from XML
        mStaggeredView.setOnScrollListener(scrollListener);

        //init service and load data
        trendsService = QweekdotsApi.getClient(getContext()).create(TrendsService.class);
        suggestionsService = QweekdotsApi.getClient(getContext()).create(SuggestionsService.class);
        explorePhotosService = QweekdotsApi.getClient(getContext()).create(ExplorePhotosService.class);

        if(isAdded()) {
            if (isNetworkConnected()) {
                loadTrends();
                loadSuggestions();
                loadExplore();

                Timber.tag(TAG).d("Loading First Pages");
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                trendsProgress.setVisibility(View.GONE);
                sugProgress.setVisibility(View.GONE);
                exploreProgress.setVisibility(View.GONE);

                Timber.tag(TAG).d("No internet connection available");
            }
        }

        trendsBtnRetry.setOnClickListener(view ->  {
            if(isNetworkConnected()) {
                loadTrends();
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                trendsProgress.setVisibility(View.GONE);
                Timber.tag(TAG).d("No internet connection available");
            }
        });
        suggestionsBtnRetry.setOnClickListener(view -> {
            if(isNetworkConnected()) {
                loadSuggestions();
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                sugProgress.setVisibility(View.GONE);
                Timber.tag(TAG).d("No internet connection available");
            }
        });
        exploreBtnRetry.setOnClickListener(view-> {
            if(isNetworkConnected()) {
                loadExplore();
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                exploreProgress.setVisibility(View.GONE);
                Timber.tag(TAG).d("No internet connection available");
            }
        });

        return rootView;
    }

    //Function to load Trends
    private void loadTrends() {
        Timber.tag(TAG).d("loadTrends: ");

        // To ensure list is visible when retry button in error view is clicked
        hideTrendsErrorView();
        hideTrendsEmptyView();

        callTrendsApi().enqueue(new Callback<TrendsModel>() {
            @Override
            public void onResponse(@NotNull Call<TrendsModel> call, @NotNull Response<TrendsModel> response) {
                hideTrendsErrorView();
                hideTrendsEmptyView();

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                List<TrendsItem> trendsItem = fetchTrends(response);
                if(trendsItem.isEmpty()) {
                    //showEmptyView();
                    trendsProgress.setVisibility(View.GONE);
                    trendsEmptyLayout.setVisibility(View.VISIBLE);
                } else {
                    trendsProgress.setVisibility(View.GONE);
                    trendsAdapter.addAll(trendsItem);
                }
            }

            @Override
            public void onFailure(Call<TrendsModel> call, Throwable t) {
                t.printStackTrace();
                showTrendsErrorView();
            }
        });
    }

    //Function to load Suggestions
    private void loadSuggestions() {
        Timber.tag(TAG).d("loadSuggestions: ");

        // To ensure list is visible when retry button in error view is clicked
        hideSuggestionsErrorView();
        hideSuggestionsEmptyView();

        callSuggestionsApi().enqueue(new Callback<Suggestions>() {
            @Override
            public void onResponse(@NotNull Call<Suggestions> call, @NotNull Response<Suggestions> response) {
                hideSuggestionsErrorView();
                hideSuggestionsEmptyView();

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                    List<UserItem> userItem = fetchSuggestions(response);
                    if(userItem.isEmpty()) {
                        showSuggestionsEmptyView();
                        sugProgress.setVisibility(View.GONE);
                        suggestionsEmptyLayout.setVisibility(View.VISIBLE);
                    } else {
                        sugProgress.setVisibility(View.GONE);
                        sugAdapter.addAll(userItem);
                    }
            }

            @Override
            public void onFailure(Call<Suggestions> call, Throwable t) {
                t.printStackTrace();
                showSuggestionsErrorView();
            }
        });
    }

    // Function to load Explore feed
    private void loadExplore() {
        Timber.tag(TAG).d("loadExplore: ");

        // To ensure list is visible when retry button in error view is clicked
        hideExploreErrorView();
        hideExploreEmptyView();
        currentPage = PAGE_START;

        callExploreApi().enqueue(new Callback<ExplorePhotos>() {
            @Override
            public void onResponse(@NotNull Call<ExplorePhotos> call, @NotNull Response<ExplorePhotos> response) {
                hideExploreErrorView();

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                hideExploreErrorView();
                hideExploreEmptyView();
                // Got data. Send it to adapter
                exploreProgress.setVisibility(View.GONE);


                    List<PhotoItem> photoItem = fetchExplore(response);
                    if(photoItem.isEmpty()) {
                        //showEmptyView();
                        exploreProgress.setVisibility(View.GONE);
                        exploreEmptyLayout.setVisibility(View.VISIBLE);
                    } else {
                        for (int index = 0; index < photoItem.size(); index++) {

                            PhotoItem flkrImage = photoItem.get(index);
                            StaggeredGridViewItem item;

                            item = new ExplorePhotosAdapter(getContext(), flkrImage, username);
                            mStaggeredView.addItem(item);
                        }

                        // Cursor Links
                        List<Pager> cursor = fetchPageLinks(response);
                        Pager cursorLink = cursor.get(0);
                        TOTAL_PAGES = cursorLink.getTotalPages();

                        if(TOTAL_PAGES == 1) {
                            isLastPage = true;
                        } else {
                            if (currentPage < TOTAL_PAGES) {
                                exploreProgress.setVisibility(View.VISIBLE);
                            } else if(currentPage == TOTAL_PAGES) {
                                isLastPage = true;
                            }
                        }
                    }
            }

            @Override
            public void onFailure(Call<ExplorePhotos> call, Throwable t) {
                t.printStackTrace();
                showExploreErrorView();
            }
        });
    }

    private void loadMoreExplore() {
        Timber.tag(TAG).d("loadNextPage: %s", currentPage);

        callExploreApi().enqueue(new Callback<ExplorePhotos>() {
            @Override
            public void onResponse(@NotNull Call<ExplorePhotos> call, @NotNull Response<ExplorePhotos> response) {
//                Log.i(TAG, "onResponse: " + currentPage
//                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                hideExploreEmptyView();
                hideExploreErrorView();
                isLoading = false;

                // Got data. Send it to adapter
                List<PhotoItem> photoItem = fetchExplore(response);
                exploreProgress.setVisibility(View.GONE);
                for(int index = 0 ; index < photoItem.size(); index++) {

                    PhotoItem flkrImage = photoItem.get(index);
                    StaggeredGridViewItem item;

                    item = new ExplorePhotosAdapter(getContext(), flkrImage, username);
                    mStaggeredView.addItem(item);
                }

                // Cursor Links
                List<Pager> cursor = fetchPageLinks(response);
                Pager cursorLink = cursor.get(0);
                TOTAL_PAGES = cursorLink.getTotalPages();

                if (currentPage == TOTAL_PAGES) {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<ExplorePhotos> call, Throwable t) {
                t.printStackTrace();
                Toasty.info(requireContext(), "Apollo, we have a problem !", Toasty.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @param response extracts List<{@link TrendsItem >} from response
     */
    private List<TrendsItem> fetchTrends(Response<TrendsModel> response) {
        TrendsModel trendsModel = response.body();
        assert trendsModel != null;
        return trendsModel.getTrendItems();
    }

    /**
     * @param response extracts List<{@link UserItem >} from response
     */
    private List<UserItem> fetchSuggestions(Response<Suggestions> response) {
        Suggestions suggestions = response.body();
        assert suggestions != null;
        return suggestions.getUserItems();
    }

    /**
     * @param response extracts List<{@link Pager>} from response
     */
    private List<Pager> fetchPageLinks(Response<ExplorePhotos> response) {
        ExplorePhotos photosFeed = response.body();
        assert photosFeed != null;
        return photosFeed.getPageLinks();
    }

    /**
     * Performs a Retrofit call to the Trends API.
     */
    private Call<TrendsModel> callTrendsApi() {
        return trendsService.getTrends(
                null
        );
    }

    /**
     * Performs a Retrofit call to the Suggestions API.
     */
    private Call<Suggestions> callSuggestionsApi() {
        return suggestionsService.getSuggestions(
                username,
                username
        );
    }

    /**
     * @param response extracts List<{@link PhotoItem >} from response
     */
    private List<PhotoItem> fetchExplore(Response<ExplorePhotos> response) {
        ExplorePhotos explorePhotos = response.body();
        assert explorePhotos != null;
        return explorePhotos.getPhotoItems();
    }

    /**
     * Performs a Retrofit call to the Explore API.
     */
    private Call<ExplorePhotos> callExploreApi() {
        return explorePhotosService.getExplorePhotos(
                username,
                currentPage
        );
    }

    /**
     */
    private void showTrendsErrorView() {

        if (trendsErrorLayout.getVisibility() == View.GONE) {
            trendsErrorLayout.setVisibility(View.VISIBLE);

            trendsTxtError.setText(getResources().getString(R.string.error_msg_unknown));
        }
    }

    private void showTrendsEmptyView() {
        if (trendsEmptyLayout.getVisibility() == View.GONE) {
            trendsEmptyLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showSuggestionsErrorView() {

        if (suggestionsErrorLayout.getVisibility() == View.GONE) {
            suggestionsErrorLayout.setVisibility(View.VISIBLE);

            suggestionsTxtError.setText(getResources().getString(R.string.error_msg_unknown));
        }
    }

    private void showSuggestionsEmptyView() {
        if (suggestionsEmptyLayout.getVisibility() == View.GONE) {
            suggestionsEmptyLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showExploreErrorView() {

        if (exploreErrorLayout.getVisibility() == View.GONE) {
            exploreErrorLayout.setVisibility(View.VISIBLE);

            exploreTxtError.setText(getResources().getString(R.string.error_msg_unknown));
        }
    }

    private void showExploreEmptyView() {
        if (exploreEmptyLayout.getVisibility() == View.GONE) {
            exploreEmptyLayout.setVisibility(View.VISIBLE);
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


    private void hideTrendsErrorView() {
        if (trendsErrorLayout.getVisibility() == View.VISIBLE) {
            trendsErrorLayout.setVisibility(View.GONE);
        }
    }

    private void hideTrendsEmptyView() {
        if (trendsEmptyLayout.getVisibility() == View.VISIBLE) {
            trendsEmptyLayout.setVisibility(View.GONE);
        }
    }

    private void hideSuggestionsErrorView() {
        if (suggestionsErrorLayout.getVisibility() == View.VISIBLE) {
            suggestionsErrorLayout.setVisibility(View.GONE);
        }
    }

    private void hideSuggestionsEmptyView() {
        if (suggestionsEmptyLayout.getVisibility() == View.VISIBLE) {
            suggestionsEmptyLayout.setVisibility(View.GONE);
        }
    }

    private void hideExploreErrorView() {
        if (exploreErrorLayout.getVisibility() == View.VISIBLE) {
            exploreErrorLayout.setVisibility(View.GONE);
        }
    }

    private void hideExploreEmptyView() {
        if (exploreEmptyLayout.getVisibility() == View.VISIBLE) {
            exploreEmptyLayout.setVisibility(View.GONE);
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

    private boolean isLastPage() {
        return isLastPage;
    }

    private boolean isLoading() {
        return isLoading;
    }
}
