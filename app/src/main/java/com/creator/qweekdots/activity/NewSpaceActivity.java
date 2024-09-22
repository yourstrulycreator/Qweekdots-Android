package com.creator.qweekdots.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.NewMySpacesAdapter;
import com.creator.qweekdots.adapter.SpacePaginationAdapter;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.api.SpaceFeedService;
import com.creator.qweekdots.api.SpaceService;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.models.ChatRoom;
import com.creator.qweekdots.models.Cursor;
import com.creator.qweekdots.models.FeedItem;
import com.creator.qweekdots.models.NewsFeed;
import com.creator.qweekdots.models.SpaceItem;
import com.creator.qweekdots.models.SpaceProfileModel;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.ui.DropTextBottomSheet;
import com.creator.qweekdots.ui.NotificationsBottomSheet;
import com.creator.qweekdots.ui.SearchBottomSheet;
import com.creator.qweekdots.utils.MenuItemBadge;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.creator.qweekdots.utils.PaginationScrollListener;
import com.creator.qweekdots.widget.CustomSwipeToRefresh;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class NewSpaceActivity extends AppCompatActivity implements PaginationAdapterCallback {
    private String TAG = SpaceActivity.class.getSimpleName();
    private SQLiteHandler db;
    private SessionManager session;

    private FloatingActionButton dropBtn;

    String chatRoomId, title;
    int notificationCount, messageCount;
    String username, avatar, selfUserId;

    View decorView;

    ///

    private String spaceId;

    private SpacePaginationAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    private SpinKitView progressBar;
    private LinearLayout errorLayout;
    private TextView txtError;
    private CustomSwipeToRefresh swipeRefreshLayout;
    private LinearLayout emptyLayout;

    private FloatingActionButton backToTop;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private static final int PAGE_START = 1;
    private static int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    private String max_id;

    private SpaceFeedService feedService;

    ///

    private ImageView spaceImage;
    private CircularProgressButton pinBtn;

    private String pinned;

    private SpaceService spaceService;
    private List<SpaceItem> spaceItem;
    private SpaceItem space;

    private NewMySpacesAdapter spacesAdapter;
    private SpinKitView spacesProgress;
    private LinearLayout spacesErrorLayout, spacesEmptyLayout;
    private ArrayList<ChatRoom> chatRoomArrayList;

    private TextView spaceTitle;
    private TextView membersNum;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_space);

        Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        spaceId = chatRoomId;
        title = intent.getStringExtra("name");

        // Window View
        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        // Initialize Timber Debug Tree for Activity
        Timber.plant(new Timber.DebugTree());

        if (new DarkModePrefManager(this).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(getResources().getColor(R.color.contentBodyColor));
            }
        }

        db = new SQLiteHandler(Objects.requireNonNull(getApplication()));
        // session manager

        // Fetching user details from SQLite
        HashMap<String, String> userData = db.getUserDetails();

        username = userData.get("username");
        avatar = userData.get("avatar");
        selfUserId = AppController.getInstance().getPrefManager().getUser().getId();

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        dropBtn = findViewById(R.id.dropBtn);
        // Drop Factory Button
        dropBtn.setOnClickListener(v -> {
            DropTextBottomSheet bottomSheet = new DropTextBottomSheet(null, null, chatRoomId);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });

        ///

        RecyclerView rv = findViewById(R.id.space_recycler);
        progressBar = findViewById(R.id.spin_kit);
        errorLayout = findViewById(R.id.error_layout);
        emptyLayout = findViewById(R.id.empty_layout);
        Button btnRetry = findViewById(R.id.error_btn_retry);
        txtError = findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = findViewById(R.id.space_swiperefresh);
        //
        spaceTitle = findViewById(R.id.spaceTitle);
        membersNum = findViewById(R.id.spaceMembersNum);

        backToTop = findViewById(R.id.back_to_top);

        adapter = new SpacePaginationAdapter(getApplicationContext(), this, username);
        adapter.setHasStableIds(true);

        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setItemViewCacheSize(20);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        rv.setAdapter(adapter);

        rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                if(isNetworkConnected()) {
                    loadNextPage();
                } else {
                    Toasty.info(getApplicationContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
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

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                int visibility = (linearLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) ? View.VISIBLE : View.GONE;
                backToTop.setVisibility(visibility);
            }
        });
        backToTop.setOnClickListener(v-> rv.smoothScrollToPosition(0));

        //init service and load data
        feedService = QweekdotsApi.getClient(getApplicationContext()).create(SpaceFeedService.class);


        if(isNetworkConnected()) {
            loadFirstPage();

            Timber.tag(TAG).d("Loading First Feed Page");
        } else {
            Toasty.info(getApplicationContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);

            Timber.tag(TAG).d("No internet connection available");
        }

        btnRetry.setOnClickListener(view -> loadFirstPage());

        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);

        /////

        // Follow Implementation for Spaces
        pinBtn = findViewById(R.id.pinActionButton);

        pinBtn.setOnClickListener(v -> {
            pinBtn.startAnimation();
            if(pinned.equals("yes")) {
                doPin(username, "unpin", chatRoomId);
                space.setPinned("no");
            } else if(pinned.equals("no")) {
                doPin(username, "pin", chatRoomId);
                space.setPinned("yes");
            }
        });

        spaceImage = findViewById(R.id.spaceImage);
        //init service and load data
        spaceService = QweekdotsApi.getClient(this).create(SpaceService.class);

        RecyclerView spacesV = findViewById(R.id.spacesRecyclerView);
        spacesProgress = findViewById(R.id.spacesProgress);
        spacesErrorLayout = findViewById(R.id.spaces_error_layout);
        spacesEmptyLayout = findViewById(R.id.spaces_empty_layout);
        Button spacesBtnRetry = findViewById(R.id.spaces_error_btn_retry);

        //Similar
        chatRoomArrayList = new ArrayList<>();
        //LinearLayoutManager myRingsLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        spacesV.setLayoutManager(new GridLayoutManager(this, 2));
        //spacesAdapter = new NewMySpacesAdapter(this, this, username, chatRoomArrayList);
        spacesV.setAdapter(spacesAdapter);
        spacesV.setItemAnimator(new DefaultItemAnimator());

        if (isNetworkConnected()) {
            loadSpace();
            loadSimilar();
            Timber.tag(TAG).d("Loading First Pages");
        } else {
            Toasty.info(getApplicationContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
            Timber.tag(TAG).d("No internet connection available");
        }

        // Retry Buttons
        spacesBtnRetry.setOnClickListener(view-> {
            if(isNetworkConnected()) {
                loadSimilar();
            } else {
                Toasty.info(getApplicationContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
                spacesProgress.setVisibility(View.GONE);
                Timber.tag(TAG).d("No internet connection available");
            }
        });


    }

    ///

    /**
     * Triggers the actual background refresh via the {@link CustomSwipeToRefresh}
     */
    private void doRefresh() {
        if(isNetworkConnected()) {
            progressBar.setVisibility(View.VISIBLE);
            if (callNewsFeedApi().isExecuted())
                callNewsFeedApi().cancel();

            // TODO: Check if data is stale.
            //  Execute network request if cache is expired; otherwise do not update data.
            adapter.getQweekFeed().clear();
            adapter.notifyDataSetChanged();
            loadFirstPage();
            swipeRefreshLayout.setRefreshing(false);

            Timber.tag(TAG).d("Loading First Feed Page");
        } else {
            Toasty.info(getApplicationContext(), "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);

            Timber.tag(TAG).d("No internet connection available");
        }
    }

    private void loadFirstPage() {
        Timber.tag(TAG).d("loadFirstPage: ");

        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();

        callNewsFeedApi().enqueue(new Callback<NewsFeed>() {
            @Override
            public void onResponse(@NotNull Call<NewsFeed> call, @NotNull Response<NewsFeed> response) {
                hideErrorView();
                progressBar.setVisibility(View.GONE);

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
                spaceId,
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
                spaceId,
                max_id,
                null
        );
    }

    /**
     * Performs a Retrofit call to the next QweekFeed API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.

     private Call<NewsFeed> callPrevNewsFeedApi() {
     return feedService.getNewsFeed(
     username,
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
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return cm.getActiveNetworkInfo() != null;
    }

    ///

    private void loadSpace() {
        callSpaceProfileApi().enqueue(new Callback<SpaceProfileModel>() {
            @SuppressLint({"SetTextI18n", "RestrictedApi"})
            @Override
            public void onResponse(@NotNull Call<SpaceProfileModel> call, @NotNull Response<SpaceProfileModel> response) {

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                spaceItem = fetchSpaceProfile(response);
                space = spaceItem.get(0);

                spaceTitle.setText(space.getSpacename());

                membersNum.setText(space.getFollowed_count());

                RequestOptions requestOptions = new RequestOptions() // because file name is always same
                        .format(DecodeFormat.PREFER_RGB_565);
                Glide
                        .with(getApplicationContext())
                        .load(space.getSpaceArt())
                        .thumbnail(0.7f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .apply(requestOptions)
                        .into(spaceImage);

                pinned = space.getPinned();

                if(pinned.equals("yes")) {
                    pinBtn.setText("Pinned");
                } else {
                    pinBtn.setText("Pin");
                }
            }

            @Override
            public void onFailure(@NotNull Call<SpaceProfileModel> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * @param response extracts List<{@link com.creator.qweekdots.models.SpaceProfileModel >} from response
     *
     */
    private List<SpaceItem> fetchSpaceProfile(Response<SpaceProfileModel> response) {
        SpaceProfileModel profileModel = response.body();
        assert profileModel != null;
        return profileModel.getSpaceItems();
    }

    /**
     * Performs a Retrofit call to the first QweekFeed API.
     *
     */
    private Call<SpaceProfileModel> callSpaceProfileApi() {
        return  spaceService.getSpaceData(
                username,
                chatRoomId
        );
    }

    /**
     * Load Hot Spaces
     */
    private void loadSimilar() {
        spacesAdapter.clear();
        Timber.tag(TAG).d("loadHotSpaces: ");
        // To ensure list is visible when retry button in error view is clicked
        hideSpacesErrorView();
        hideSpacesEmptyView();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                "https://qweek.fun/genjitsu/similar_spaces?u="+username+"&id="+chatRoomId+"", response -> {
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
                        pinBtn.setText("Pinned");
                        pinBtn.saveInitialState();
                    } else {
                        pinBtn.setText("Pin");
                        pinBtn.saveInitialState();
                    }

                    //String sent = "Pinned to MySpaces";
                    //Toasty.success(getApplicationContext(), sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(getApplicationContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                stopButtonAnimation();
            }

        }, error -> {
            Timber.tag(TAG).e("Pin Error: %s", error.getMessage());
            Toasty.error(getApplicationContext(),
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

    private void stopButtonAnimation(){
        pinBtn.revertAnimation();
    }

    // Helpers -------------------------------------------------------------------------------------

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

    ///

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        Glide.get(getApplicationContext()).clearMemory();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Glide.get(getApplicationContext()).clearMemory();
        super.onDestroy();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        AppController.getInstance().getPrefManager().clear();

        // Launching the login activity
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_three);
        View view = MenuItemCompat.getActionView(menuItem);

        CircleImageView avatar2 = view.findViewById(R.id.toolbar_avatar);

        RequestOptions requestOptions = new RequestOptions() // because file name is always same
                .format(DecodeFormat.PREFER_RGB_565);
        Drawable placeholder = getTinted(getResources().getColor(R.color.contentTextColor));
        Glide
                .with(getApplicationContext())
                .load(avatar)
                .override(30, 30)
                .placeholder(placeholder)
                .error(placeholder)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(requestOptions)
                .into(avatar2);

        avatar2.setOnClickListener(v -> {
            Intent i = new Intent(NewSpaceActivity.this, ProfileActivity.class);
            i.putExtra("profile", username);
            startActivity(i);
            customType(NewSpaceActivity.this, "fadein-to-fadeout");
        });

        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.contentTextColor), PorterDuff.Mode.SRC_ATOP);
            }
        }

        // Notification Badges update Icon in Menu Options
        if(notificationCount > 0) {
            MenuItem menuItemNotification = menu.findItem(R.id.menu_two);
            MenuItemBadge.update(this, menuItemNotification, new MenuItemBadge.Builder()
                    .iconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_notification_md))
                    .iconTintColor(getResources().getColor(R.color.contentTextColor))
                    .textBackgroundColor(getResources().getColor(R.color.QweekColorAccent))
                    .textColor(getResources().getColor(R.color.contentBodyColor)));
            MenuItemBadge.getBadgeTextView(menuItemNotification).setBadgeCount(notificationCount);
        } else {
            MenuItem menuItemNotification = menu.findItem(R.id.menu_two);
            MenuItemBadge.update(this, menuItemNotification, new MenuItemBadge.Builder()
                    .iconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_notification_md))
                    .iconTintColor(getResources().getColor(R.color.contentTextColor))
                    .textBackgroundColor(getResources().getColor(R.color.QweekColorAccent))
                    .textColor(getResources().getColor(R.color.contentBodyColor)));
            MenuItemBadge.getBadgeTextView(menuItemNotification).setBadgeCount(0,true);
        }

        /*
        MenuItem menuItemNotification = menu.findItem(R.id.menu_one);
        MenuItemBadge.update(this, menuItemNotification, new MenuItemBadge.Builder()
                .iconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_search_white_24dp))
                .iconTintColor(getResources().getColor(R.color.contentTextColor))
                .textBackgroundColor(getResources().getColor(R.color.QweekColorAccent))
                .textColor(getResources().getColor(R.color.contentBodyColor)));
        MenuItemBadge.getBadgeTextView(menuItemNotification).setBadgeCount(0, true);

         */

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            //case R.id.menu_one:
                //Go to ChatActivity
                //Intent i = new Intent(MainActivity.this, ChatActivity.class);
                //startActivity(i);
                //customType(MainActivity.this, "bottom-to-up");
              //  SearchBottomSheet searchSheet = new SearchBottomSheet(getApplicationContext(), username);
              //  searchSheet.show(getSupportFragmentManager(), searchSheet.getTag());
              //  break;

            case R.id.menu_two:
                // Go to NotificationsBottomSheet
                NotificationsBottomSheet bottomSheet = new NotificationsBottomSheet(getApplicationContext(), username);
                bottomSheet.show(getSupportFragmentManager(),bottomSheet.getTag());
                break;

            case R.id.menu_three:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private @Nullable
    Drawable getTinted(@ColorInt int color) {
        // need to mutate otherwise all references to this drawable will be tinted
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_alien).mutate();
        return tint(drawable, ColorStateList.valueOf(color));
    }

    public static Drawable tint(Drawable input, ColorStateList tint) {
        if (input == null) {
            return null;
        }
        Drawable wrappedDrawable = DrawableCompat.wrap(input);
        DrawableCompat.setTintList(wrappedDrawable, tint);
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.MULTIPLY);
        return wrappedDrawable;
    }

    public void onClick(View v) {
        super.onBackPressed();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
