package com.creator.qweekdots.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.arasthel.spannedgridlayoutmanager.SpanSize;
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager;
import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.ExploreAdapter;
import com.creator.qweekdots.adapter.ExplorePhotosAdapter;
import com.creator.qweekdots.adapter.HotSpacesAdapter;
import com.creator.qweekdots.adapter.StaggeredPaginationAdapter;
import com.creator.qweekdots.adapter.StaggeredPaginationAdapter2;
import com.creator.qweekdots.adapter.SuggestionsAdapter;
import com.creator.qweekdots.adapter.TrendsAdapter;
import com.creator.qweekdots.api.ExplorePhotosService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.api.SuggestionsService;
import com.creator.qweekdots.api.TrendsService;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.ChatRoom;
import com.creator.qweekdots.models.Cursor;
import com.creator.qweekdots.models.ExplorePhotos;
import com.creator.qweekdots.models.FeedItem;
import com.creator.qweekdots.models.NewsFeed;
import com.creator.qweekdots.models.Pager;
import com.creator.qweekdots.models.PhotoItem;
import com.creator.qweekdots.models.Suggestions;
import com.creator.qweekdots.models.TrendsItem;
import com.creator.qweekdots.models.TrendsModel;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.creator.qweekdots.utils.PaginationScrollListener;
import com.creator.qweekdots.utils.StaggeredScrollListener;
import com.creator.qweekdots.view.StaggeredGridView;
import com.creator.qweekdots.view.StaggeredGridViewItem;
import com.github.ybq.android.spinkit.SpinKitView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import es.dmoral.toasty.Toasty;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class Spotlight extends Fragment implements PaginationAdapterCallback {
    View rootView;
    private final String TAG = Spotlight.class.getSimpleName();

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private TrendsAdapter trendsAdapter;
    private TrendsService trendsService;
    private SuggestionsAdapter sugAdapter;
    private HotSpacesAdapter spacesAdapter;
    private ExploreAdapter exploreAdapter;
    private SuggestionsService suggestionsService;
    private ExplorePhotosService explorePhotosService;
    private LinearLayout trendsErrorLayout, trendsEmptyLayout, spacesErrorLayout, spacesEmptyLayout, suggestionsErrorLayout, suggestionsEmptyLayout, exploreErrorLayout, exploreEmptyLayout;
    private TextView trendsTxtError, suggestionsTxtError, exploreTxtError;
    private SpinKitView sugProgress, trendsProgress, spacesProgress, exploreProgress;
    //private StaggeredGridView mStaggeredView;
    private ArrayList<ChatRoom> chatRoomArrayList;
    private static final int PAGE_START = 1;
    private static int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    private String max_id;
    private String username;

    private StaggeredPaginationAdapter2 exploreAdapter2;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

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
        RecyclerView exploreRV = rootView.findViewById(R.id.explore_grid);
        RecyclerView trendsV = rootView.findViewById(R.id.trendsRecyclerView);
        //RecyclerView spacesV = rootView.findViewById(R.id.spacesRecyclerView);
        sugProgress = rootView.findViewById(R.id.suggestionsProgress);
        //spacesProgress = rootView.findViewById(R.id.spacesProgress);
        exploreProgress = rootView.findViewById(R.id.exploreProgress);
        trendsProgress = rootView.findViewById(R.id.trendsProgress);
        trendsErrorLayout = rootView.findViewById(R.id.trends_error_layout);
        trendsEmptyLayout = rootView.findViewById(R.id.trends_empty_layout);
        //spacesErrorLayout = rootView.findViewById(R.id.spaces_error_layout);
        //spacesEmptyLayout = rootView.findViewById(R.id.spaces_empty_layout);
        suggestionsErrorLayout = rootView.findViewById(R.id.suggestions_error_layout);
        suggestionsEmptyLayout = rootView.findViewById(R.id.suggestions_empty_layout);
        exploreErrorLayout = rootView.findViewById(R.id.explore_error_layout);
        exploreEmptyLayout = rootView.findViewById(R.id.explore_empty_layout);
        Button trendsBtnRetry = rootView.findViewById(R.id.trends_error_btn_retry);
        //Button spacesBtnRetry = rootView.findViewById(R.id.spaces_error_btn_retry);
        Button suggestionsBtnRetry = rootView.findViewById(R.id.suggestions_error_btn_retry);
        Button exploreBtnRetry = rootView.findViewById(R.id.explore_error_btn_retry);
        trendsTxtError = rootView.findViewById(R.id.trends_error_txt_cause);
        suggestionsTxtError = rootView.findViewById(R.id.suggestions_error_txt_cause);
        exploreTxtError = rootView.findViewById(R.id.explore_error_txt_cause);

        // getting search functionality to run
        ImageView search = rootView.findViewById(R.id.searchQweekdots);
        search.setOnClickListener(v -> {
            SearchBottomSheet bottomSheet = new SearchBottomSheet(getActivity(), username);
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });
        EditText searchBar = rootView.findViewById(R.id.searchQweekdotsBar);
        searchBar.setOnClickListener(v -> {
            SearchBottomSheet bottomSheet = new SearchBottomSheet(getActivity(), username);
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });


        //Trends
        LinearLayoutManager trendsLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        trendsV.setLayoutManager(trendsLinearLayoutManager);
        trendsAdapter = new TrendsAdapter(getActivity());
        trendsV.setAdapter(trendsAdapter);
        trendsV.setItemAnimator(new DefaultItemAnimator());


        /*
        //Hot Spaces
        chatRoomArrayList = new ArrayList<>();
        LinearLayoutManager myRingsLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        spacesV.setLayoutManager(myRingsLinearLayoutManager);
        spacesAdapter = new HotSpacesAdapter(getActivity(), this, username, chatRoomArrayList);
        spacesV.setAdapter(spacesAdapter);
        spacesV.setItemAnimator(new DefaultItemAnimator());

         */

        // Suggestions
        LinearLayoutManager sugLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        sugV.setLayoutManager(sugLinearLayoutManager);
        sugAdapter = new SuggestionsAdapter(getActivity());
        sugV.setAdapter(sugAdapter);
        sugV.setItemAnimator(new DefaultItemAnimator());

        // Explore / Discover
        SpannedGridLayoutManager spannedGridLayoutManager = new SpannedGridLayoutManager(
                SpannedGridLayoutManager.Orientation.VERTICAL, 3);

        spannedGridLayoutManager.setSpanSizeLookup(new SpannedGridLayoutManager.SpanSizeLookup(position -> {
            if(position == 0) {
                return new SpanSize(2, 2);
            }
            return null;
        }));

        //exploreV.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        //exploreV.setLayoutManager(spannedGridLayoutManager);
        //exploreAdapter = new ExploreAdapter(getActivity(), username);
        //exploreV.setAdapter(exploreAdapter);
        //exploreV.setItemAnimator(new DefaultItemAnimator());
        //mStaggeredView = rootView.findViewById(R.id.explore_grid);

        /*
        RecyclerView rv = rootView.findViewById(R.id.exploreRecyclerView);
        exploreAdapter = new ExploreAdapter(getActivity(), username);
        exploreAdapter.setHasStableIds(true);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);

        // setting recycler view layout to staggered grid

        rv.setLayoutManager(staggeredGridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setItemViewCacheSize(20);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rv.setItemAnimator(null);
        rv.setAdapter(exploreAdapter);

         */

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        exploreRV.setLayoutManager(staggeredGridLayoutManager);
        exploreRV.setItemAnimator(new DefaultItemAnimator());
        exploreRV.setItemViewCacheSize(20);
        exploreRV.setDrawingCacheEnabled(true);
        exploreRV.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        exploreAdapter2 = new StaggeredPaginationAdapter2(getActivity(), getTargetFragment(), username);
        exploreRV.setAdapter(exploreAdapter2);


        //init service and load data
        trendsService = QweekdotsApi.getClient(getContext()).create(TrendsService.class);
        suggestionsService = QweekdotsApi.getClient(getContext()).create(SuggestionsService.class);
        explorePhotosService = QweekdotsApi.getClient(getContext()).create(ExplorePhotosService.class);

        if(isAdded()) {
            if (isNetworkConnected()) {
                loadTrends();
                //loadHotSpaces();
                loadSuggestions();
                loadExplore();
                Timber.tag(TAG).d("Loading First Pages");
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                //trendsProgress.setVisibility(View.GONE);
                sugProgress.setVisibility(View.GONE);
                exploreProgress.setVisibility(View.GONE);
                Timber.tag(TAG).d("No internet connection available");
            }
        }

        // Retry Buttons

        trendsBtnRetry.setOnClickListener(view ->  {
            if(isNetworkConnected()) {
                loadTrends();
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                trendsProgress.setVisibility(View.GONE);
                Timber.tag(TAG).d("No internet connection available");
            }
        });

        /*
        spacesBtnRetry.setOnClickListener(view-> {
            if(isNetworkConnected()) {
                loadHotSpaces();
            } else {
                Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                spacesProgress.setVisibility(View.GONE);
                Timber.tag(TAG).d("No internet connection available");
            }
        });

         */
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

    /*
     * Load Trends
     */

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
                    trendsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NotNull Call<TrendsModel> call, @NotNull Throwable t) {
                t.printStackTrace();
                showTrendsErrorView();
            }
        });
    }


    /**
     * Load Hot Spaces
     */
    /*
    private void loadHotSpaces() {
        spacesAdapter.clear();
        Timber.tag(TAG).d("loadHotSpaces: ");
        // To ensure list is visible when retry button in error view is clicked
        hideSpacesErrorView();
        hideSpacesEmptyView();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                "https://qweek.fun/genjitsu/hot_spaces?u="+username+"", response -> {
            Timber.tag(TAG).d("response: %s", response);
            try {
                JSONObject obj = new JSONObject(response);
                // check for error flag
                    JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                    if(chatRoomsArray.length() < 1) {
                        spacesEmptyLayout.setVisibility(View.VISIBLE);
                        spacesErrorLayout.setVisibility(View.GONE);
                        spacesProgress.setVisibility(View.GONE);
                    } else {
                        spacesEmptyLayout.setVisibility(View.GONE);
                        spacesErrorLayout.setVisibility(View.GONE);
                        spacesProgress.setVisibility(View.GONE);

                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            ChatRoom cr = new ChatRoom();
                            cr.setId(chatRoomsObj.getString("chat_room_id"));
                            cr.setName(chatRoomsObj.getString("name"));
                            cr.setSpace_art(chatRoomsObj.getString("art"));

                            chatRoomArrayList.add(cr);
                        }
                    }

            } catch (JSONException e) {
                Timber.tag(TAG).e("json parsing error: %s", e.getMessage());
                spacesErrorLayout.setVisibility(View.VISIBLE);
                spacesEmptyLayout.setVisibility(View.GONE);
                spacesProgress.setVisibility(View.GONE);
            }

            spacesAdapter.notifyDataSetChanged();
        }, error -> {
            NetworkResponse networkResponse = error.networkResponse;
            Timber.tag(TAG).e("Volley error: " + error.getMessage() + ", code: " + networkResponse);
            spacesErrorLayout.setVisibility(View.VISIBLE);
            spacesEmptyLayout.setVisibility(View.GONE);
        });

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

     */

    /**
     * Load Suggestions
     */
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
            public void onFailure(@NotNull Call<Suggestions> call, @NotNull Throwable t) {
                t.printStackTrace();
                showSuggestionsErrorView();
            }
        });
    }

    /**
     * Load Explore
     */
    /*
    private void loadExplore() {
        Timber.tag(TAG).d("loadExplore: ");
        // To ensure list is visible when retry button in error view is clicked
        hideExploreErrorView();
        hideExploreEmptyView();

        callExploreApi().enqueue(new Callback<ExplorePhotos>() {
            @Override
            public void onResponse(@NotNull Call<ExplorePhotos> call, @NotNull Response<ExplorePhotos> response) {
                hideExploreErrorView();
                hideExploreEmptyView();

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                List<PhotoItem> photoItem = fetchExplore(response);
                if(photoItem.isEmpty()) {
                    //showEmptyView();
                    exploreProgress.setVisibility(View.GONE);
                    exploreEmptyLayout.setVisibility(View.VISIBLE);
                } else {
                    exploreProgress.setVisibility(View.GONE);

                    exploreProgress.setVisibility(View.GONE);
                    exploreAdapter.addAll(photoItem);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ExplorePhotos> call, @NotNull Throwable t) {
                t.printStackTrace();
                showExploreErrorView();
            }
        });
    }

     */

    /**
     * Load First Set of Explore (QweekSnaps)
     */

    /*
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
                    exploreProgress.setVisibility(View.GONE);

                    for (int i = 0; i < photoItem.size(); i++) {
                        PhotoItem pItem = photoItem.get(i);
                        StaggeredGridViewItem item;
                        item = new ExplorePhotosAdapter(requireActivity(), pItem, username);
                        mStaggeredView.addItem(item);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<ExplorePhotos> call, @NotNull Throwable t) {
                t.printStackTrace();
                showExploreErrorView();
            }
        });
    }

     */

    private void loadExplore() {
        Timber.tag(TAG).d("loadFirstPage: ");
        // To ensure list is visible when retry button in error view is clicked
        hideExploreErrorView();
        callNewsFeedApi().enqueue(new Callback<NewsFeed>() {
            @Override
            public void onResponse(@NotNull Call<NewsFeed> call, @NotNull Response<NewsFeed> response) {
                hideExploreErrorView();
                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));
                List<FeedItem> feedItem = fetchNewsFeed(response);
                if(feedItem.isEmpty()) {
                    showExploreEmptyView();
                } else {
                    // Got data. Send it to adapter
                    exploreProgress.setVisibility(View.GONE);
                    exploreAdapter2.addAll(feedItem);
                    exploreAdapter2.notifyDataSetChanged();

                    // Cursor Links
                    //List<Cursor> cursor = fetchCursorLinks(response);
                    //Cursor cursorLink = cursor.get(0);
                    //max_id = cursorLink.getMaxID();
                    //since_id = cursorLink.getSinceID();
                    //TOTAL_PAGES = cursorLink.getPagesNum();

                    /*
                    if(TOTAL_PAGES == 1) {
                        isLastPage = true;
                    } else {
                        if (currentPage < TOTAL_PAGES) {
                            exploreAdapter2.addLoadingFooter();
                        } else if(currentPage == TOTAL_PAGES) {
                            isLastPage = true;
                        }
                    }

                     */
                }
            }

            @Override
            public void onFailure(@NotNull Call<NewsFeed> call, @NotNull Throwable t) {
                t.printStackTrace();
                showExploreErrorView();
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
    /*
    private List<FeedItem> fetchExplore(@NotNull Response<NewsFeed> response) {
        NewsFeed explorePhotos = response.body();
        assert explorePhotos != null;
        return explorePhotos.getPhotoItems();
    }

     */

    /**
     * @param response extracts List<{@link FeedItem>} from response
     */
    private List<FeedItem> fetchNewsFeed(Response<NewsFeed> response) {
        NewsFeed newsFeed = response.body();
        assert newsFeed != null;
        return newsFeed.getFeedItems();
    }

    /**
     * Performs a Retrofit call to the first QweekFeed API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<NewsFeed> callNewsFeedApi() {
        return explorePhotosService.getNewsFeed(
                username
        );
    }

    /**
     * View Handlers
     */

    private void showTrendsErrorView() {
        if (trendsErrorLayout.getVisibility() == View.GONE) {
            trendsErrorLayout.setVisibility(View.VISIBLE);
            trendsTxtError.setText(getResources().getString(R.string.error_msg_unknown));
        }
    }

    private void showSuggestionsErrorView() {
        if (suggestionsErrorLayout.getVisibility() == View.GONE) {
            suggestionsErrorLayout.setVisibility(View.VISIBLE);
            suggestionsTxtError.setText(requireActivity().getResources().getString(R.string.error_msg_unknown));
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
            exploreTxtError.setText(requireActivity().getResources().getString(R.string.error_msg_unknown));
        }
    }

    private void showExploreEmptyView() {
        if (exploreEmptyLayout.getVisibility() == View.GONE) {
            exploreEmptyLayout.setVisibility(View.VISIBLE);
        }
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

    /*
    private void hideSpacesErrorView() {
        if (spacesErrorLayout.getVisibility() == View.VISIBLE) {
            spacesErrorLayout.setVisibility(View.GONE);
        }
    }

    private void hideSpacesEmptyView() {
        if (spacesEmptyLayout.getVisibility() == View.VISIBLE) {
            spacesEmptyLayout.setVisibility(View.GONE);
        }
    }

     */

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
     * @param throwable to identify the type of error
     * @return appropriate error message
     */
    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = requireContext().getResources().getString(R.string.error_msg_unknown);
        if (!isNetworkConnected()) {
            errorMsg = requireContext().getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = requireContext().getResources().getString(R.string.error_msg_timeout);
        }
        return errorMsg;
    }

    @Override
    public void onResume() {
        super.onResume();
        //loadExplore();
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

    @Override
    public void retryPageLoad() {

    }
}
