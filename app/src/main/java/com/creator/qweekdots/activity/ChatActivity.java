package com.creator.qweekdots.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.ChatActivityAdapter;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.app.EndPoints;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.models.ChatRoom;
import com.creator.qweekdots.models.Message;
import com.creator.qweekdots.ui.AddChatBottomSheet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ArrayList<ChatRoom> chatRoomArrayList;
    private ChatActivityAdapter mAdapter;
    private SQLiteHandler db;
    private SessionManager session;
    private LinearLayout emptyLayout, errorLayout;

    String username, avatar, selfUserId;

    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.plant(new Timber.DebugTree());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn() && AppController.getInstance().getPrefManager().getUser() == null) {
            launchLoginActivity();
        }

        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        errorLayout = findViewById(R.id.error_layout);
        Button btnRetry = findViewById(R.id.error_btn_retry);
        emptyLayout = findViewById(R.id.empty_layout);
        FloatingActionButton addChat = findViewById(R.id.add_chat);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        // SqLite database handler
        db = new SQLiteHandler(Objects.requireNonNull(this).getApplicationContext());
        // session manager

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        username = user.get("username");
        avatar = user.get("avatar");

        // self user id is to identify the message owner
        selfUserId = AppController.getInstance().getPrefManager().getUser().getId();

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
        mAdapter = new ChatActivityAdapter(this, chatRoomArrayList, selfUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new ChatActivityAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new ChatActivityAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // when chat is clicked, launch full chat thread activity
                ChatRoom chatRoom = chatRoomArrayList.get(position);
                Intent intent = new Intent(ChatActivity.this, ChatUserActivity.class);
                intent.putExtra("chat_room_id", chatRoom.getId());
                intent.putExtra("to", chatRoom.getPrivate_to());
                intent.putExtra("to_name", chatRoom.getName());
                startActivity(intent);
                customType(ChatActivity.this, "fadein-to-fadeout");

                resetMessageStatus(chatRoom.getId(), username);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        addChat.setOnClickListener(v->{
            AddChatBottomSheet bottomSheet = new AddChatBottomSheet(getApplicationContext(), username);
            bottomSheet.show(getSupportFragmentManager(),bottomSheet.getTag());
        });

        if (checkPlayServices()) {
            fetchUserChatRooms();
        }

        btnRetry.setOnClickListener(view -> fetchUserChatRooms());
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

    private void launchLoginActivity() {
        session.setLogin(false);

        db.deleteUsers();

        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(AppConfig.PUSH_NOTIFICATION));

        NotificationManager manager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
        OneSignal.clearOneSignalNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        Glide.get(getApplicationContext()).clearMemory();
        super.onPause();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Timber.tag(TAG).i("This device is not supported. Google Play Services not installed!");
                Toasty.error(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    public void onClick(View v) {
        super.onBackPressed(); // or super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        Glide.get(getApplicationContext()).clearMemory();
        super.onDestroy();
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
