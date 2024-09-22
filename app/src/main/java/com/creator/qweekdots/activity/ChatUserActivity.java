package com.creator.qweekdots.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.app.EndPoints;
import com.creator.qweekdots.adapter.ChatUserThreadAdapter;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.Message;
import com.creator.qweekdots.models.User;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.service.NotificationUtils;
import com.creator.qweekdots.utils.GifSizeFilter;
import com.creator.qweekdots.utils.Glide4Engine;
import com.creator.qweekdots.utils.ImageUtil;
import com.creator.qweekdots.volley.VolleyMultipartRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;
import com.onesignal.OneSignal;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

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
    private boolean isFile = false;
    private Bitmap rotatedBitmap;
    private Uri finalVideo;
    private Uri finalFile;
    private CardView postPhotoCard, postVideoCard, postFileCard;
    private ImageView mediaImage;
    private VideoView videoView;
    private RequestQueue rQueue;
    private RelativeLayout attachmentLayout;
    private TextView attached_filenameTxt;
    private String docfile, extension;

    private String username, avatar;

    private static final int PICKER_REQUEST_CODE = 1;
    private int FILE_CODE = 66;

    View decorView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        // Make Changes according to theme selected
        if(new DarkModePrefManager(this).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.contentBodyColor));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.getDecorView().getWindowInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        } else {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.contentBodyColor));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.getDecorView().getWindowInsetsController().setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }

        }

        Timber.plant(new Timber.DebugTree());

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(getApplication()));
        // session manager

        // Fetching user details from SQLite
        HashMap<String, String> userData = db.getUserDetails();
        username = userData.get("username");
        avatar = userData.get("avatar");

        inputMessage = findViewById(R.id.chat_message);

        FloatingActionButton btnSend = findViewById(R.id.btn_send);

        Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        toUserId = intent.getStringExtra("to");
        String toUserName = intent.getStringExtra("to_name");

        EmojiTextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(toUserName);
        toolbarTitle.setOnClickListener(v->{
            Intent i = new Intent(ChatUserActivity.this, ProfileActivity.class);
            i.putExtra("profile", toUserName);
            startActivity(i);
            customType(ChatUserActivity.this, "fadein-to-fadeout");
        });

        emptyLayout = findViewById(R.id.empty_layout);

        attachmentLayout = findViewById(R.id.attachment_meta);
        ImageView mediaBtn = findViewById(R.id.mediaBtn);
        ImageView attachBtn = findViewById(R.id.attachBtn);
        mediaImage = findViewById(R.id.mediaImage);
        videoView = findViewById(R.id.mediaVideo);
        attached_filenameTxt = findViewById(R.id.attached_filename);
        postPhotoCard = findViewById(R.id.post_photoCard);
        postVideoCard = findViewById(R.id.post_videoCard);
        postFileCard = findViewById(R.id.post_fileCard);

        mediaBtn.setOnClickListener(v-> {
            String[] PERMISSIONS = {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            if(hasPermissions(this, PERMISSIONS)) {
                ShowPicker();
                attachBtn.setClickable(false);
            } else{
                ActivityCompat.requestPermissions(this, PERMISSIONS, PICKER_REQUEST_CODE);
            }
        });

        attachBtn.setOnClickListener(V-> {
            Intent i = new Intent(this, FilePickerActivity.class);
            // This works if you defined the intent filter
            // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

            // Set these depending on your use case. These are the defaults.
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

            // Configure initial directory by specifying a String.
            // You could specify a String like "/storage/emulated/0/", but that can
            // dangerous. Always use Android's API calls to get paths to the SD-card or
            // internal memory.
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

            startActivityForResult(i, FILE_CODE);
            mediaBtn.setClickable(false);
        });

        ImageView deleteMeta = findViewById(R.id.delete_meta);
        deleteMeta.setOnClickListener(v-> {
            if(isPhoto) {
                postPhotoCard.setVisibility(View.GONE);
                mediaImage.setVisibility(View.GONE);
                mediaImage.setImageResource(android.R.color.transparent);
                collapse(attachmentLayout);
                rotatedBitmap = null;
                isPhoto = false;

                mediaBtn.setClickable(true);
                attachBtn.setClickable(true);
            }
            if(isVideo) {
                postVideoCard.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                collapse(attachmentLayout);
                finalVideo = null;
                isVideo = false;

                mediaBtn.setClickable(true);
                attachBtn.setClickable(true);
            }
            if(isFile) {
                postFileCard.setVisibility(View.GONE);
                collapse(attachmentLayout);
                finalFile = null;
                isFile = false;

                mediaBtn.setClickable(true);
                attachBtn.setClickable(true);
            }
        });

        // Make the call for chat room
        fetchChatRoomID_ifExists();

        recyclerView = findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();

        // self user id is to identify the message owner
        selfUserId = AppController.getInstance().getPrefManager().getUser().getId();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(true);
        //recyclerView.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
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
                if(finalFile!=null) {
                    if(isFile) {
                        Toasty.info(getApplicationContext(), "Sending...", Toasty.LENGTH_LONG).show();
                        Log.d(TAG, "Send message with file");
                        postFileMessage(finalFile, docfile, extension, selfUserId);
                    }
                }
                if(rotatedBitmap==null&&finalVideo==null&&finalFile==null) {
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
                } else if(isFile&&finalFile!=null) {
                    Toasty.info(getApplicationContext(), "Sending...", Toasty.LENGTH_LONG).show();
                    Log.d(TAG, "Send message with file");
                    postFileMessage(finalFile, docfile, extension, selfUserId);
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

        NotificationManager manager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
        OneSignal.clearOneSignalNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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
     * Handling new push message, will add the message to
     * recycler view and scroll it to bottom
     * */
    private void handlePushNotification(Intent intent) {
        int type = intent.getIntExtra("type", -1);

        // if the push is of chat room message
        // simply update the UI unread messages count
        if (type == AppConfig.PUSH_TYPE_CHATROOM) {
            Message message = (Message) intent.getSerializableExtra("message");
            String chatRoomId = intent.getStringExtra("chat_room_id");

            if (message != null && chatRoomId != null) {
                //updateRow(chatRoomId, message);
            }
        } else if (type == AppConfig.PUSH_TYPE_USER) {
            // push belongs to user alone
            // just showing the message in a toast
            Message message = (Message) intent.getSerializableExtra("message");
            assert message != null;
            //Toast.makeText(getApplicationContext(), "New push: " + message.getMessage(), Toast.LENGTH_LONG).show();
            if (message != null && chatRoomId != null) {
                messageArrayList.add(message);
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() > 1) {
                    Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                }
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
                        expand(attachmentLayout);
                        postPhotoCard.setVisibility(View.VISIBLE);
                        mediaImage.setVisibility(View.VISIBLE);
                        postVideoCard.setVisibility(View.GONE);
                        videoView.setVisibility(View.GONE);
                        postFileCard.setVisibility(View.GONE);

                        Glide.with(this)
                                .load(rotatedBitmap)
                                .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                                .into(mediaImage);

                        isPhoto = true;
                        isVideo = false;
                        isFile = false;

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
                        expand(attachmentLayout);
                        finalVideo = path;
                        postVideoCard.setVisibility(View.VISIBLE);
                        videoView.setVisibility(View.VISIBLE);
                        postPhotoCard.setVisibility(View.GONE);
                        mediaImage.setVisibility(View.GONE);
                        postFileCard.setVisibility(View.GONE);

                        videoView.setVideoURI(path);
                        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
                        videoView.start();

                        isVideo = true;
                        isPhoto = false;
                        isFile = false;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Toasty.info(this, "Neither Image nor Video was selected", Toasty.LENGTH_SHORT).show();
            }

            Log.d("Matisse", "mSelected: " + mSelected);
        }

        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            for (Uri uri: files) {
                File file = Utils.getFileForUri(uri);
                // Do something with the result...
                expand(attachmentLayout);
                postFileCard.setVisibility(View.VISIBLE);
                postVideoCard.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                postPhotoCard.setVisibility(View.GONE);
                mediaImage.setVisibility(View.GONE);

                if(file != null){
                    uri = data.getData();

                    String filename;
                    Cursor cursor = getContentResolver().query(uri,null,null,null,null);

                    if(cursor == null) filename=uri.getPath();
                    else{
                        cursor.moveToFirst();
                        int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                        filename = cursor.getString(idx);
                        cursor.close();
                    }

                    String name = filename.substring(0,filename.lastIndexOf("."));
                    String exte = filename.substring(filename.lastIndexOf(".")+1);

                    String file_name = name+"."+exte;
                    attached_filenameTxt.setText(file_name);

                    finalFile = Uri.fromFile(file);
                    docfile = name;
                    extension = exte;

                    isFile = true;
                    isPhoto = true;
                    isVideo = true;
                }
            }
        }
    }

    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void postMessage(final String id) {
        final String message = Objects.requireNonNull(this.inputMessage.getText()).toString().trim();

        emptyLayout.setVisibility(View.GONE);

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
                    String file1 = commentObj.getString("file");
                    String file_name1 = commentObj.getString("file_name");
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
                    message1.setFile(file1);
                    message1.setFilename(file_name1);
                    message1.setMediaType(mediaType);
                    message1.setHasMedia(hasMedia);
                    message1.setHasLink(hasLink);
                    message1.setLink(link);
                    message1.setCreatedAt(createdAt);
                    message1.setUser(user);

                    messageArrayList.add(message1);

                    mAdapter.notifyDataSetChanged();

                    emptyLayout.setVisibility(View.GONE);
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
     * Posting a new message in chat room with Image
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void postPhotoMessage(final Bitmap bitmap, final String id) {
        final String message = this.inputMessage.getText().toString();

        emptyLayout.setVisibility(View.GONE);

        this.inputMessage.setText("");

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/message_photo.php",
                response -> {
                    Timber.tag(TAG).d("Drop Response: %s", new String(response.data));

                    try {
                        JSONObject obj = new JSONObject(new String(response.data));

                        // Check for error node in json
                        if (!obj.getBoolean("error")) {
                            collapse(attachmentLayout);
                            mediaImage.setVisibility(View.GONE);
                            mediaImage.setImageResource(android.R.color.transparent);
                            isPhoto = false;

                            // success
                            JSONObject commentObj = obj.getJSONObject("message");

                            String commentId = commentObj.getString("message_id");
                            String commentText = commentObj.getString("message");
                            String qweeksnap = commentObj.getString("qweeksnap");
                            String file1 = commentObj.getString("file");
                            String file_name1 = commentObj.getString("file_name");
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
                            message1.setFile(file1);
                            message1.setFilename(file_name1);
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
     * Posting a new message in chat room with Video
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void postVideoMessage(Uri videoFile, final String id) {
        final String message = this.inputMessage.getText().toString();

        emptyLayout.setVisibility(View.GONE);

        this.inputMessage.setText("");

        InputStream iStream;
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
                            JSONObject obj = new JSONObject(new String(response.data));

                            // Check for error node in json
                            // check for error
                            if (!obj.getBoolean("error")) {
                                collapse(attachmentLayout);
                                videoView.setVisibility(View.GONE);
                                videoView.stopPlayback();
                                finalVideo = null;
                                isVideo = false;

                                JSONObject commentObj = obj.getJSONObject("message");

                                String commentId = commentObj.getString("message_id");
                                String commentText = commentObj.getString("message");
                                String qweeksnap = commentObj.getString("qweeksnap");
                                String file1 = commentObj.getString("file");
                                String file_name1 = commentObj.getString("file_name");
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
                                message1.setFile(file1);
                                message1.setFilename(file_name1);
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
     * Posting a new message in chat room with Video
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void postFileMessage(Uri file, String doc, String ext, final String id) {
        final String message = this.inputMessage.getText().toString();

        emptyLayout.setVisibility(View.GONE);

        this.inputMessage.setText("");

        InputStream iStream;
        try {

            iStream = getContentResolver().openInputStream(file);
            assert iStream != null;
            final byte[] inputData = getVideoBytes(iStream);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                    "https://qweek.fun/genjitsu/chat/message_file.php",
                    response -> {
                        Timber.tag(TAG).d("Drop Response: %s", new String(response.data));
                        rQueue.getCache().clear();
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));

                            // Check for error node in json
                            // check for error
                            if (!obj.getBoolean("error")) {
                                collapse(attachmentLayout);
                                postFileCard.setVisibility(View.GONE);
                                finalFile = null;
                                isFile = false;

                                JSONObject commentObj = obj.getJSONObject("message");

                                String commentId = commentObj.getString("message_id");
                                String commentText = commentObj.getString("message");
                                String qweeksnap = commentObj.getString("qweeksnap");
                                String file1 = commentObj.getString("file");
                                String file_name1 = commentObj.getString("file_name");
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
                                message1.setFile(file1);
                                message1.setFilename(file_name1);
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
                    params.put("file_name", doc+"."+ext);
                    return params;
                }

                /*
                 *pass files using below method
                 * */
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    long docname = System.currentTimeMillis();

                    params.put("file", new DataPart( docname+"."+ext ,inputData));
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
                            String file1 = commentObj.getString("file");
                            String file_name1 = commentObj.getString("file_name");
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
                            message.setFile(file1);
                            message.setFilename(file_name1);
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

                    resetMessageStatus(chatRoomId, username);
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

    /**
     * Run a check on if an ID exists for the Chat Room
     * A new ID is to be prepared for every new chat between two users
     */
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

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RelativeLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
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
        super.onBackPressed(); // or super.finish();
    }

}
