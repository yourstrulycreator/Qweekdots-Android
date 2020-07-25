package com.creator.qweekdots.chat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.app.EndPoints;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.Message;
import com.creator.qweekdots.models.User;
import com.creator.qweekdots.service.NotificationUtils;
import com.creator.qweekdots.utils.GifSizeFilter;
import com.creator.qweekdots.utils.Glide4Engine;
import com.creator.qweekdots.utils.ImageUtil;
import com.creator.qweekdots.volley.VolleyMultipartRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

@SuppressLint("LogNotTimber")
public class ChatUserActivity extends AppCompatActivity {

    private String TAG = ChatUserActivity.class.getSimpleName();

    private String chatRoomId, checkedRoomId;
    private RecyclerView recyclerView;
    private ChatUserThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private EmojiEditText inputMessage;
    private LinearLayout emptyLayout;
    private String selfUserId;
    private String toUserId;
    private boolean isPhoto = false;
    private boolean isVideo = false;
    private Bitmap rotatedBitmap;
    private Uri finalVideo;
    private CardView postPhotoCard, postVideoCard;
    private ImageView mediaImage;
    private VideoView videoView;
    private RequestQueue rQueue;

    private String username;

    private static final int PICKER_REQUEST_CODE = 1;

    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.QweekColorAccent));

        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        Timber.plant(new Timber.DebugTree());

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(getApplication()));
        // session manager

        // Fetching user details from SQLite
        HashMap<String, String> userData = db.getUserDetails();

        username = userData.get("username");

        inputMessage = findViewById(R.id.chat_message);
        FloatingActionButton btnSend = findViewById(R.id.btn_send);

        Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        toUserId = intent.getStringExtra("to");
        String toUserName = intent.getStringExtra("to_name");

        EmojiTextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(toUserName);

        emptyLayout = findViewById(R.id.empty_layout);

        ImageView mediaBtn = findViewById(R.id.mediaBtn);
        mediaImage = findViewById(R.id.mediaImage);
        videoView = findViewById(R.id.mediaVideo);
        postPhotoCard = findViewById(R.id.post_photoCard);
        postVideoCard = findViewById(R.id.post_videoCard);

        mediaBtn.setOnClickListener(v-> {
            String[] PERMISSIONS = {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            if(hasPermissions(this, PERMISSIONS)) {
                ShowPicker();
            } else{
                ActivityCompat.requestPermissions(this, PERMISSIONS, PICKER_REQUEST_CODE);
            }
        });

        // Make the call for chat room
        fetchChatRoomID_ifExists();

        recyclerView = findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();

        // self user id is to identify the message owner
        selfUserId = AppController.getInstance().getPrefManager().getUser().getId();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ChatUserThreadAdapter(this, messageArrayList, selfUserId);
        recyclerView.setAdapter(mAdapter);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.equals(intent.getAction(), AppConfig.PUSH_NOTIFICATION)) {
                    // new push message is received
                    handlePushNotification(intent);
                }
            }
        };

        btnSend.setOnClickListener(v -> {
            String drop = String.valueOf(this.inputMessage.getText());
            if(drop.isEmpty()) {
                if(rotatedBitmap!=null) {
                    if(isPhoto) {
                        Toasty.info(getApplicationContext(), "Sending...", Toasty.LENGTH_LONG).show();
                        Log.d(TAG, "Send message with image");
                        postPhotoMessage(rotatedBitmap, selfUserId);
                    }
                }
                if(finalVideo!=null) {
                    if(isVideo) {
                        Toasty.info(getApplicationContext(), "Sending...", Toasty.LENGTH_LONG).show();
                        Log.d(TAG, "Send message with video");
                        postVideoMessage(finalVideo, selfUserId);
                    }
                }
                if(rotatedBitmap==null&&finalVideo==null) {
                    Toasty.error(getApplicationContext(), "Can't send empty balloons, can we?", Toast.LENGTH_LONG).show();
                }
            } else {
                Toasty.info(getApplicationContext(), "Sending...", Toasty.LENGTH_LONG).show();
                if(isPhoto&&rotatedBitmap!=null) {
                    Toasty.info(getApplicationContext(), "Sending...", Toasty.LENGTH_LONG).show();
                    Log.d(TAG, "Send message with image");
                    postPhotoMessage(rotatedBitmap, selfUserId);
                } else if(isVideo&&finalVideo!=null) {
                    Toasty.info(getApplicationContext(), "Sending...", Toasty.LENGTH_LONG).show();
                    Log.d(TAG, "Send message with video");
                    postVideoMessage(finalVideo, selfUserId);
                } else {
                    Toasty.info(getApplicationContext(), "Sending...", Toasty.LENGTH_LONG).show();
                    Log.d(TAG, "Send message without media");
                    postMessage(selfUserId);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // registering the receiver for new notification
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(AppConfig.PUSH_NOTIFICATION));

        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Handling new push message, will add the message to
     * recycler view and scroll it to bottom
     * */
    private void handlePushNotification(Intent intent) {
        Message message = (Message) intent.getSerializableExtra("message");

        if (message != null && chatRoomId != null) {
            messageArrayList.add(message);
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 1) {
                Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
            }
        }
    }

    private void ShowPicker() {
        Matisse.from(this)
                .choose(MimeType.ofAll())
                .countable(false)
                .maxSelectable(1)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .forResult(PICKER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            // List that will contain the selected files/videos
            List<Uri> mSelected = Matisse.obtainResult(data);

            Log.e(TAG, mSelected.get(0).toString());

            if (mSelected.get(0).toString().contains("image")) {
                //handle image
                try {
                    Uri uri = mSelected.get(0);
                    // You can update this bitmap to your server
                    rotatedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                    if (rotatedBitmap != null) {
                        postPhotoCard.setVisibility(View.VISIBLE);
                        mediaImage.setVisibility(View.VISIBLE);
                        postVideoCard.setVisibility(View.GONE);
                        videoView.setVisibility(View.GONE);

                        Glide.with(this)
                                .load(rotatedBitmap)
                                .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                                .into(mediaImage);

                        isPhoto = true;
                        isVideo = false;

                        Timber.tag("Image bitmap").i("%s-", rotatedBitmap.toString());
                    } else {
                        Toasty.error(this, "Oops, something went wrong, kindly Try Again",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (mSelected.get(0).toString().contains("video")) {
                //handle video
                try {
                    Uri path = mSelected.get(0);
                    if (path != null) {
                        finalVideo = path;
                        postVideoCard.setVisibility(View.VISIBLE);
                        videoView.setVisibility(View.VISIBLE);
                        postPhotoCard.setVisibility(View.GONE);
                        mediaImage.setVisibility(View.GONE);

                        videoView.setVideoURI(path);
                        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
                        videoView.start();

                        isVideo = true;
                        isPhoto = false;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Toasty.info(this, "Neither Image nor Video was selected", Toasty.LENGTH_SHORT).show();
            }

            Log.d("Matisse", "mSelected: " + mSelected);
        }
    }

    /**
     * function to post drop text
     * */
    private void postMessage(final String id) {
        final String message = Objects.requireNonNull(this.inputMessage.getText()).toString().trim();

        // Tag used to cancel the request
        String tag_string_req = "req_post";

        this.inputMessage.setText("");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/message.php", response -> {
            Timber.tag(TAG).d("Message Response: %s", response);

            try {
                JSONObject obj = new JSONObject(response);

                // Check for error node in json
                // check for error
                if (!obj.getBoolean("error")) {
                    JSONObject commentObj = obj.getJSONObject("message");

                    String commentId = commentObj.getString("message_id");
                    String commentText = commentObj.getString("message");
                    String qweeksnap = commentObj.getString("qweeksnap");
                    String mediaType = commentObj.getString("mediaType");
                    Integer hasMedia = commentObj.getInt("hasMedia");
                    Integer hasLink = commentObj.getInt("hasLink");
                    String link = commentObj.getString("link");
                    String createdAt = commentObj.getString("created_at");

                    JSONObject userObj = obj.getJSONObject("user");
                    String userId = userObj.getString("id");
                    String userName = userObj.getString("username");
                    String fullName = userObj.getString("fullname");
                    String email = userObj.getString("email");
                    String avatar = userObj.getString("avatar");
                    User user = new User(userId, userName, fullName, email, avatar);
                    Timber.tag(TAG).d(String.valueOf(user));

                    Message message1 = new Message();
                    message1.setId(commentId);
                    message1.setMessage(commentText);
                    message1.setQweeksnap(qweeksnap);
                    message1.setMediaType(mediaType);
                    message1.setHasMedia(hasMedia);
                    message1.setHasLink(hasLink);
                    message1.setLink(link);
                    message1.setCreatedAt(createdAt);
                    message1.setUser(user);

                    messageArrayList.add(message1);

                    mAdapter.notifyDataSetChanged();
                    if (mAdapter.getItemCount() > 1) {
                        // scrolling to bottom of the recycler view
                        Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                    }

                } else {
                    Toasty.error(getApplicationContext(), "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(getApplicationContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
            Toasty.error(getApplicationContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
            this.inputMessage.setText(message);
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to drop url
                Map<String, String> params = new HashMap<>();
                params.put("user_id", id);
                params.put("chat_message", message);
                params.put("chat_room_id", checkedRoomId);
                params.put("to_user_id", toUserId);

                Log.e(TAG, "Params: " + params.toString());

                return params;
            }

        };

        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void postPhotoMessage(final Bitmap bitmap, final String id) {
        final String message = this.inputMessage.getText().toString();

        this.inputMessage.setText("");

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/message_photo.php",
                response -> {
                    Timber.tag(TAG).d("Drop Response: %s", new String(response.data));

                    try {
                        JSONObject obj = new JSONObject(String.valueOf(response));

                        // Check for error node in json
                        if (!obj.getBoolean("error")) {
                            mediaImage.setVisibility(View.GONE);
                            mediaImage.setImageDrawable(null);
                            // success
                            JSONObject commentObj = obj.getJSONObject("message");

                            String commentId = commentObj.getString("message_id");
                            String commentText = commentObj.getString("message");
                            String qweeksnap = commentObj.getString("qweeksnap");
                            String mediaType = commentObj.getString("mediaType");
                            Integer hasMedia = commentObj.getInt("hasMedia");
                            Integer hasLink = commentObj.getInt("hasLink");
                            String link = commentObj.getString("link");
                            String createdAt = commentObj.getString("created_at");

                            JSONObject userObj = obj.getJSONObject("user");
                            String userId = userObj.getString("id");
                            String userName = userObj.getString("username");
                            String fullName = userObj.getString("fullname");
                            String email = userObj.getString("email");
                            String avatar = userObj.getString("avatar");
                            User user = new User(userId, userName, fullName, email, avatar);
                            Timber.tag(TAG).d(String.valueOf(user));

                            Message message1 = new Message();
                            message1.setId(commentId);
                            message1.setMessage(commentText);
                            message1.setQweeksnap(qweeksnap);
                            message1.setMediaType(mediaType);
                            message1.setHasMedia(hasMedia);
                            message1.setHasLink(hasLink);
                            message1.setLink(link);
                            message1.setCreatedAt(createdAt);
                            message1.setUser(user);

                            messageArrayList.add(message1);

                            mAdapter.notifyDataSetChanged();
                            if (mAdapter.getItemCount() > 1) {
                                // scrolling to bottom of the recycler view
                                Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                            }

                            Toasty.success(getApplicationContext(), "Sent", Toasty.LENGTH_SHORT).show();
                        } else {
                            // Error in drop. Get the error message
                            Toasty.error(getApplicationContext(), "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toasty.error(getApplicationContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                    }

                },
                error -> {
                    Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                    Toasty.error(this,
                            "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
                    this.inputMessage.setText(message);
                }) {
            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", id);
                params.put("chat_message", message);
                params.put("chat_room_id", checkedRoomId);
                params.put("to_user_id", toUserId);
                params.put("username", username);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();

                byte[] imageData = ImageUtil.getFileDataFromDrawable(bitmap);
                byte[] imageDataLite = ImageUtil.reduceImageForUpload(imageData);

                params.put("pic", new DataPart("qweeksnap_" + imagename + ".jpg", imageDataLite));
                return params;
            }
        };

        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        volleyMultipartRequest.setRetryPolicy(policy);

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void postVideoMessage(Uri videoFile, final String id) {
        final String message = this.inputMessage.getText().toString();

        this.inputMessage.setText("");

        InputStream iStream = null;
        try {

            iStream = getContentResolver().openInputStream(videoFile);
            assert iStream != null;
            final byte[] inputData = getVideoBytes(iStream);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                    "https://qweek.fun/genjitsu/chat/message_video.php",
                    response -> {
                        Timber.tag(TAG).d("Drop Response: %s", new String(response.data));
                        rQueue.getCache().clear();
                        try {
                            JSONObject obj = new JSONObject(String.valueOf(response));

                            // Check for error node in json
                            // check for error
                            if (!obj.getBoolean("error")) {
                                videoView.setVisibility(View.GONE);
                                videoView.stopPlayback();

                                JSONObject commentObj = obj.getJSONObject("message");

                                String commentId = commentObj.getString("message_id");
                                String commentText = commentObj.getString("message");
                                String qweeksnap = commentObj.getString("qweeksnap");
                                String mediaType = commentObj.getString("mediaType");
                                Integer hasMedia = commentObj.getInt("hasMedia");
                                Integer hasLink = commentObj.getInt("hasLink");
                                String link = commentObj.getString("link");
                                String createdAt = commentObj.getString("created_at");

                                JSONObject userObj = obj.getJSONObject("user");
                                String userId = userObj.getString("id");
                                String userName = userObj.getString("username");
                                String fullName = userObj.getString("fullname");
                                String email = userObj.getString("email");
                                String avatar = userObj.getString("avatar");
                                User user = new User(userId, userName, fullName, email, avatar);
                                Timber.tag(TAG).d(String.valueOf(user));

                                Message message1 = new Message();
                                message1.setId(commentId);
                                message1.setMessage(commentText);
                                message1.setQweeksnap(qweeksnap);
                                message1.setMediaType(mediaType);
                                message1.setHasMedia(hasMedia);
                                message1.setHasLink(hasLink);
                                message1.setLink(link);
                                message1.setCreatedAt(createdAt);
                                message1.setUser(user);

                                messageArrayList.add(message1);

                                mAdapter.notifyDataSetChanged();
                                if (mAdapter.getItemCount() > 1) {
                                    // scrolling to bottom of the recycler view
                                    Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                                }

                                Toasty.success(getApplicationContext(), "Sent", Toasty.LENGTH_SHORT).show();
                            } else {
                                // Error in drop. Get the error message
                                Toasty.error(getApplicationContext(), "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toasty.error(getApplicationContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                        Toasty.error(this,
                                "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
                        this.inputMessage.setText(message);
                    }) {

                /*
                 * If you want to add more parameters with the image
                 * you can do it here
                 * here we have only one parameter with the image
                 * which is tags
                 * */
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("user_id", id);
                    params.put("chat_message", message);
                    params.put("chat_room_id", checkedRoomId);
                    params.put("to_user_id", toUserId);
                    params.put("username", username);
                    return params;
                }

                /*
                 *pass files using below method
                 * */
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    long videoname = System.currentTimeMillis();

                    params.put("vid", new DataPart("qweeksnap_" + videoname + ".mp4" ,inputData));
                    return params;
                }
            };


            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            rQueue = Volley.newRequestQueue(this);
            rQueue.add(volleyMultipartRequest);



        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Fetching all the messages of a single chat room
     * */
    private void fetchChatThread() {

        String endPoint = EndPoints.CHAT_THREAD.replace("_ID_", chatRoomId);
        Log.e(TAG, "endPoint: " + endPoint);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                endPoint, response -> {
            Log.e(TAG, "response: " + response);

            try {
                JSONObject obj = new JSONObject(response);

                // check for error
                if (!obj.getBoolean("error")) {
                    JSONArray commentsObj = obj.getJSONArray("messages");

                    if(commentsObj.length() < 1) {
                        emptyLayout.setVisibility(View.VISIBLE);
                    } else {
                        emptyLayout.setVisibility(View.GONE);
                        for (int i = 0; i < commentsObj.length(); i++) {
                            JSONObject commentObj = (JSONObject) commentsObj.get(i);

                            String commentId = commentObj.getString("message_id");
                            String commentText = commentObj.getString("message");
                            String qweeksnap = commentObj.getString("qweeksnap");
                            String mediaType = commentObj.getString("mediaType");
                            Integer hasMedia = commentObj.getInt("hasMedia");
                            Integer hasLink = commentObj.getInt("hasLink");
                            String link = commentObj.getString("link");
                            String createdAt = commentObj.getString("created_at");

                            JSONObject userObj = commentObj.getJSONObject("user");
                            String userId = userObj.getString("user_id");
                            String userName = userObj.getString("username");
                            String fullName = userObj.getString("fullname");
                            String email = userObj.getString("email");
                            String avatar = userObj.getString("avatar");
                            User user = new User(userId, userName, fullName, email, avatar);

                            Message message = new Message();
                            message.setId(commentId);
                            message.setMessage(commentText);
                            message.setQweeksnap(qweeksnap);
                            message.setMediaType(mediaType);
                            message.setHasMedia(hasMedia);
                            message.setHasLink(hasLink);
                            message.setLink(link);
                            message.setCreatedAt(createdAt);
                            message.setUser(user);

                            messageArrayList.add(message);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                    if (mAdapter.getItemCount() > 1) {
                        Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
                Toasty.error(getApplicationContext(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            NetworkResponse networkResponse = error.networkResponse;
            Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            Toasty.error(getApplicationContext(), "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();
        });

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    private void fetchChatRoomID_ifExists() {

        String endPoint = EndPoints.CHAT_ROOM_EXISTS;
        Log.e(TAG, "endPoint: " + endPoint);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                endPoint, response -> {
            Log.e(TAG, "response: " + response);

            try {
                JSONObject obj = new JSONObject(response);

                // check for error
                if (!obj.getBoolean("error")) {

                        String chatRoomExists = obj.getString("chat_room_num");
                        if(chatRoomExists.equals("1")) {
                            checkedRoomId = obj.getString("chat_room_id");
                            chatRoomId = obj.getString("chat_room_id");
                            fetchChatThread();
                            Log.e(TAG, checkedRoomId);
                        } else if(chatRoomExists.equals("0")) {
                            checkedRoomId = "create";
                            emptyLayout.setVisibility(View.VISIBLE);
                            Log.e(TAG, checkedRoomId);
                        }

                } else {
                    //Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Check error");
                }

            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
                //Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            NetworkResponse networkResponse = error.networkResponse;
            Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            //Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", selfUserId);
                params.put("to", toUserId);

                Log.e(TAG, "Params: " + params.toString());

                return params;
            }
        };

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }

    /**
     * Helper method that verifies whether the permissions of a given array are granted or not.
     *
     * @return {Boolean}
     */
    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private byte[] getVideoBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public void onClick(View v) {
        super.onBackPressed(); // or super.finish();
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
