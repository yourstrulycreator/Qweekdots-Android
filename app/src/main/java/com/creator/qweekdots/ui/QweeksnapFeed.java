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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.ExplorePhotosAdapter;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.api.QweeksnapFeedService;
import com.creator.qweekdots.models.Pager;
import com.creator.qweekdots.models.PhotoItem;
import com.creator.qweekdots.models.ProfileQweekSnaps;
import com.creator.qweekdots.view.StaggeredGridView;
import com.creator.qweekdots.view.StaggeredGridViewItem;
import com.github.ybq.android.spinkit.SpinKitView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class QweeksnapFeed extends Fragment {

    private String profile;
    private String username;
    private Context context;

    private final String TAG = QweeksnapFeed.class.getSimpleName();
    private SpinKitView progressBar;
    private LinearLayout errorLayout;
    private TextView txtError;
    private LinearLayout emptyLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private StaggeredGridView mStaggeredView;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private static final int PAGE_START = 1;
    private int TOTAL_PAGES;
    private int currentPage = PAGE_START;

    private QweeksnapFeedService feedService;

    public QweeksnapFeed() {
    }

    @SuppressLint("ValidFragment")
    public QweeksnapFeed(Context context, String profile, String username) {
        this.context = context;
        this.profile = profile;
        this.username = username;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.qweeksnap_feed, container, false);
        if(context!=null) {
            progressBar = rootView.findViewById(R.id.profile_qweeksnapProgress);
            errorLayout = rootView.findViewById(R.id.error_layout);
            emptyLayout = rootView.findViewById(R.id.empty_layout);
            Button btnRetry = rootView.findViewById(R.id.error_btn_retry);
            txtError = rootView.findViewById(R.id.error_txt_cause);
            swipeRefreshLayout = rootView.findViewById(R.id.main_swiperefresh);

            // QweekSnap Photos w/ Scroll Handling
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
                            loadMoreQweekSnaps();
                        } else {
                            Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                            Timber.tag(TAG).d("No internet connection available");
                        }
                    }
                }
            };
            mStaggeredView = rootView.findViewById(R.id.profile_staggeredview);
            // Be sure before calling initialize that you haven't initialised from XML
            mStaggeredView.setOnScrollListener(scrollListener);

            feedService = QweekdotsApi.getClient(getContext()).create(QweeksnapFeedService.class);

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
        if(isNetworkConnected()) {
            progressBar.setVisibility(View.VISIBLE);
            if (callQweekSnapsApi().isExecuted())
                callQweekSnapsApi().cancel();
            // TODO: Check if data is stale.
            //  Execute network request if cache is expired; otherwise do not update data.
            mStaggeredView.clear();
            loadFirstPage();
            swipeRefreshLayout.setRefreshing(false);
            Timber.tag(TAG).d("Loading First Feed Page");
        } else {
            Toasty.info(requireContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            Timber.tag(TAG).d("No internet connection available");
        }
    }

    /**
     * Load First Page of QweekSnaps on Profile Feed
     */
    private void loadFirstPage() {
        Timber.tag(TAG).d("loadExplore: ");
        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();
        hideEmptyView();
        currentPage = PAGE_START;
        callQweekSnapsApi().enqueue(new Callback<ProfileQweekSnaps>() {
            @Override
            public void onResponse(@NotNull Call<ProfileQweekSnaps> call, @NotNull Response<ProfileQweekSnaps> response) {
                hideErrorView();
                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));
                // Got data. Send it to adapter
                progressBar.setVisibility(View.GONE);

                List<PhotoItem> photoItem = fetchExplore(response);
                if(photoItem.isEmpty()) {
                    //showEmptyView();
                    progressBar.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
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

                    if (currentPage <= TOTAL_PAGES) {
                        progressBar.setVisibility(View.GONE);
                        isLastPage = true;
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<ProfileQweekSnaps> call, @NotNull Throwable t) {
                t.printStackTrace();
                showErrorView();
            }
        });
    }

    private void loadMoreQweekSnaps() {
        Timber.tag(TAG).d("loadNextPage: %s", currentPage);
        callQweekSnapsApi().enqueue(new Callback<ProfileQweekSnaps>() {
            @Override
            public void onResponse(@NotNull Call<ProfileQweekSnaps> call, @NotNull Response<ProfileQweekSnaps> response) {
                isLoading = false;

                // Got data. Send it to adapter
                List<PhotoItem> photoItem = fetchExplore(response);
                progressBar.setVisibility(View.GONE);
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

                if (currentPage != TOTAL_PAGES) {
                    progressBar.setVisibility(View.GONE);
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(@NotNull Call<ProfileQweekSnaps> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * @param response extracts List<{@link Pager>} from response
     */
    private List<Pager> fetchPageLinks(Response<ProfileQweekSnaps> response) {
        ProfileQweekSnaps photosFeed = response.body();
        assert photosFeed != null;
        return photosFeed.getPageLinks();
    }

    /**
     * @param response extracts List<{@link PhotoItem >} from response
     */
    private List<PhotoItem> fetchExplore(Response<ProfileQweekSnaps> response) {
        ProfileQweekSnaps profilePhotos = response.body();
        assert profilePhotos != null;
        return profilePhotos.getPhotoItems();
    }

    /**
     * Performs a Retrofit call to the Suggestions API.
     */
    private Call<ProfileQweekSnaps> callQweekSnapsApi() {
        return feedService.getNewsFeed(
                username,
                profile,
                currentPage
        );
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

    // Helpers -------------------------------------------------------------------------------------
    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
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

    private boolean isLastPage() {
        return isLastPage;
    }

    private boolean isLoading() {
        return isLoading;
    }
}
