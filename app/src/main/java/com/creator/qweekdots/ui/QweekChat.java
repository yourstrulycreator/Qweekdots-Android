package com.creator.qweekdots.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ChatUserActivity;
import com.creator.qweekdots.adapter.ChatActivityAdapter;
import com.creator.qweekdots.adapter.MutualsAdapter;
import com.creator.qweekdots.api.MutualFeedService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.app.EndPoints;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.models.ChatRoom;
import com.creator.qweekdots.models.Cursor;
import com.creator.qweekdots.models.Message;
import com.creator.qweekdots.models.ProfileModel;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.creator.qweekdots.utils.PaginationScrollListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class QweekChat extends Fragment implements PaginationAdapterCallback {
    View rootView;
    private final String TAG = QweekChat.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ArrayList<ChatRoom> chatRoomArrayList;
    private ChatActivityAdapter mAdapter;
    private SQLiteHandler db;
    private SessionManager session;
    private LinearLayout emptyLayout, errorLayout;

    private String username, avatar, selfUserId;

    private MutualFeedService feedService;
    private RecyclerView mutualRv;
    private LinearLayoutManager linearLayoutManager;
    private MutualsAdapter adapter;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private static final int PAGE_START = 1;
    private static int TOTAL_PAGES;
    private int currentPage = PAGE_START;
    private String max_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.qweekchat, container, false);

        if(new DarkModePrefManager(requireActivity()).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        errorLayout = rootView.findViewById(R.id.error_layout);
        Button btnRetry = rootView.findViewById(R.id.error_btn_retry);
        emptyLayout = rootView.findViewById(R.id.empty_layout);
        ImageView addChat = rootView.findViewById(R.id.add_chat);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        // mutuals
        mutualRv = rootView.findViewById(R.id.mutual_recyclerview);

        adapter = new MutualsAdapter(getActivity(), this, username);
        adapter.setHasStableIds(true);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mutualRv.setLayoutManager(linearLayoutManager);
        mutualRv.setItemAnimator(null);
        mutualRv.setItemViewCacheSize(20);
        mutualRv.setDrawingCacheEnabled(true);
        mutualRv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mutualRv.setAdapter(adapter);

        // SqLite database handler
        db = new SQLiteHandler(Objects.requireNonNull(this).requireContext());
        // session manager

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        username = user.get("username");
        avatar = user.get("avatar");

        // self user id is to identify the message owner
        selfUserId = AppController.getInstance().getPrefManager().getUser().getId();

        //init service and load data
        feedService = QweekdotsApi.getClient(getContext()).create(MutualFeedService.class);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(AppConfig.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    Timber.tag(TAG).d("Push notification is received!");
                    handlePushNotification(intent);
                }
            }
        };

        chatRoomArrayList = new ArrayList<>();
        mAdapter = new ChatActivityAdapter(requireContext(), chatRoomArrayList, selfUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new ChatActivityAdapter.RecyclerTouchListener(requireContext(), recyclerView, new ChatActivityAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // when chat is clicked, launch full chat thread activity
                ChatRoom chatRoom = chatRoomArrayList.get(position);
                Intent intent = new Intent(requireActivity(), ChatUserActivity.class);
                intent.putExtra("chat_room_id", chatRoom.getId());
                intent.putExtra("to", chatRoom.getPrivate_to());
                intent.putExtra("to_name", chatRoom.getName());
                startActivity(intent);
                customType(requireActivity(), "right-to-left");

                resetMessageStatus(chatRoom.getId(), username);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        addChat.setOnClickListener(v->{
            AddChatBottomSheet bottomSheet = new AddChatBottomSheet(requireContext(), username);
            bottomSheet.show(requireActivity().getSupportFragmentManager(),bottomSheet.getTag());
        });

        if (checkPlayServices()) {
            fetchUserChatRooms();
        }

        loadFirstPage();

        btnRetry.setOnClickListener(view -> fetchUserChatRooms());

        return rootView;
    }

    private void loadFirstPage() {
        Timber.tag(TAG).d("loadFirstPage: ");

        // To ensure list is visible when retry button in error view is clicked
        //hideErrorView();

        callNewsFeedApi().enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(@NotNull Call<ProfileModel> call, @NotNull Response<ProfileModel> response) {
                //hideErrorView();
                //progressBar.setVisibility(View.GONE);

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                List<UserItem> feedItem = fetchNewsFeed(response);

                if(feedItem.isEmpty()) {
                    //showEmptyView();
                } else {
                    //progressBar.setVisibility(View.GONE);
                    adapter.addAll(feedItem);
                    adapter.notifyDataSetChanged();

                    // Cursor Links
                    List<Cursor> cursor = fetchCursorLinks(response);
                    Cursor cursorLink = cursor.get(0);
                    max_id = cursorLink.getMaxID();
                    //since_id = cursorLink.getSinceID();

                    TOTAL_PAGES = cursorLink.getPagesNum();

                    /*
                    if(TOTAL_PAGES == 1) {
                        isLastPage = true;
                    } else {
                        if (currentPage < TOTAL_PAGES) {
                            adapter.addLoadingFooter();
                        } else if(currentPage == TOTAL_PAGES) {
                            isLastPage = true;
                        }
                    }

                     */
                }
            }

            @Override
            public void onFailure(@NotNull Call<ProfileModel> call, @NotNull Throwable t) {
                t.printStackTrace();
                //showErrorView();
            }
        });
    }

    /**
     * @param response extracts List<{@link UserItem>} from response
     */
    private List<UserItem> fetchNewsFeed(Response<ProfileModel> response) {
        ProfileModel newsFeed = response.body();
        assert newsFeed != null;
        return newsFeed.getUserItems();
    }

    /**
     * @param response extracts List<{@link Cursor>} from response
     */
    private List<Cursor> fetchCursorLinks(Response<ProfileModel> response) {
        ProfileModel newsFeed = response.body();
        assert newsFeed != null;
        return newsFeed.getCursorLinks();
    }

    private void loadNextPage() {
        Timber.tag(TAG).d("loadNextPage: %s", currentPage);

        callNextNewsFeedApi().enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(@NotNull Call<ProfileModel> call, @NotNull Response<ProfileModel> response) {
                adapter.removeLoadingFooter();
                isLoading = false;

                List<UserItem> feedItems = fetchNewsFeed(response);
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
            public void onFailure(@NotNull Call<ProfileModel> call, @NotNull Throwable t) {
                t.printStackTrace();
                //adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    /**
     * Performs a Retrofit call to the first QweekFeed API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<ProfileModel> callNewsFeedApi() {
        return feedService.getProfileFeed(
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
    private Call<ProfileModel> callNextNewsFeedApi() {
        return feedService.getProfileFeed(
                username,
                max_id,
                null
        );
    }

    public void retryPageLoad() {
        loadFirstPage();
    }

    /**
     * Handles new push notification
     */
    private void handlePushNotification(Intent intent) {
        int type = intent.getIntExtra("type", -1);

        // if the push is of chat room message
        // simply update the UI unread messages count
        if (type == AppConfig.PUSH_TYPE_CHATROOM) {
            Message message = (Message) intent.getSerializableExtra("message");
            String chatRoomId = intent.getStringExtra("chat_room_id");

            if (message != null && chatRoomId != null) {
                updateRow(chatRoomId, message);
            }
        } else if (type == AppConfig.PUSH_TYPE_USER) {
            // push belongs to user alone
            // just showing the message in a toast
            Message message = (Message) intent.getSerializableExtra("message");
            assert message != null;
            //Toast.makeText(getApplicationContext(), "New push: " + message.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Updates the chat list unread count and the last message
     */
    private void updateRow(String chatRoomId, Message message) {
        for (ChatRoom cr : chatRoomArrayList) {
            if (cr.getId().equals(chatRoomId)) {
                int index = chatRoomArrayList.indexOf(cr);
                cr.setLastMessage(message.getMessage());
                cr.setUnreadCount(cr.getUnreadCount() + 1);
                chatRoomArrayList.remove(index);
                chatRoomArrayList.add(index, cr);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void resetMessageStatus(String chat_room_id, String user_id) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/parse/message_reset.php", response -> Timber.tag(TAG).e("response: %s", response), error -> {
            NetworkResponse networkResponse = error.networkResponse;
            Timber.tag(TAG).e("Volley error: " + error.getMessage() + ", code: " + networkResponse);
            //Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", chat_room_id);
                params.put("u", user_id);

                Timber.tag(TAG).d("params: %s", params.toString());
                return params;
            }
        };

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }


    /**
     * fetching the chat rooms by making http call
     */
    private void fetchUserChatRooms() {
        String endPoint = EndPoints.USER_CHAT_ROOMS.replace("_ID_", selfUserId);
        Timber.tag(TAG).e("endPoint: %s", endPoint);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                endPoint, response -> {
            Timber.tag(TAG).d("response: %s", response);

            try {
                JSONObject obj = new JSONObject(response);

                // check for error flag
                if (!obj.getBoolean("error")) {
                    JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                    if(chatRoomsArray.length() < 1) {
                        emptyLayout.setVisibility(View.VISIBLE);
                    } else {
                        emptyLayout.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.GONE);

                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            ChatRoom cr = new ChatRoom();
                            cr.setId(chatRoomsObj.getString("chat_room_id"));
                            cr.setName(chatRoomsObj.getString("name"));
                            cr.setType(chatRoomsObj.getString("type"));
                            cr.setPrivate_to(chatRoomsObj.getString("private_to"));
                            cr.setPrivate_from(chatRoomsObj.getString("private_from"));
                            cr.setLastMessage(chatRoomsObj.getString("last_message"));
                            cr.setLastMessageTo(chatRoomsObj.getString("last_message_to"));
                            cr.setLastMessageFrom(chatRoomsObj.getString("last_message_from"));
                            cr.setUnreadCount(chatRoomsObj.getInt("unreadCount"));
                            cr.setTimestamp(chatRoomsObj.getString("created_at"));
                            cr.setPrivate_avatar(chatRoomsObj.getString("avatar"));

                            chatRoomArrayList.add(cr);
                        }
                    }

                } else {
                    // error in fetching chat rooms
                    //Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    errorLayout.setVisibility(View.VISIBLE);
                }

            } catch (JSONException e) {
                Timber.tag(TAG).e("json parsing error: %s", e.getMessage());
                //Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                errorLayout.setVisibility(View.VISIBLE);
            }

            mAdapter.notifyDataSetChanged();

        }, error -> {
            NetworkResponse networkResponse = error.networkResponse;
            Timber.tag(TAG).e("Volley error: " + error.getMessage() + ", code: " + networkResponse);
            //Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            errorLayout.setVisibility(View.VISIBLE);
        });

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(requireContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(requireActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Timber.tag(TAG).i("This device is not supported. Google Play Services not installed!");
                Toasty.error(requireContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
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
    public void onResume() {
        super.onResume();
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(AppConfig.PUSH_NOTIFICATION));

        //NotificationManager manager = (NotificationManager) requireContext()
        //        .getSystemService(Context.NOTIFICATION_SERVICE);
        //manager.cancelAll();
        OneSignal.clearOneSignalNotifications();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
