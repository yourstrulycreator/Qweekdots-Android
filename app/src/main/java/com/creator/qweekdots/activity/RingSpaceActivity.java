package com.creator.qweekdots.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.api.SpaceService;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.app.EndPoints;
import com.creator.qweekdots.adapter.RingSpaceThreadAdapter;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.Message;
import com.creator.qweekdots.models.SpaceItem;
import com.creator.qweekdots.models.SpaceProfileModel;
import com.creator.qweekdots.models.User;
import com.creator.qweekdots.service.NotificationUtils;
import com.creator.qweekdots.utils.GifSizeFilter;
import com.creator.qweekdots.utils.Glide4Engine;
import com.creator.qweekdots.utils.ImageUtil;
import com.creator.qweekdots.volley.VolleyMultipartRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Target;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import org.jetbrains.annotations.NotNull;
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

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;


@SuppressLint("LogNotTimber")
public class RingSpaceActivity extends AppCompatActivity {

    private String TAG = RingSpaceActivity.class.getSimpleName();

    private String chatRoomId;
    private RecyclerView recyclerView;
    private RingSpaceThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private EmojiEditText inputMessage;
    private LinearLayout emptyLayout;
    private String selfUserId;
    private String pinned;
    private ImageView spaceImage;
    private ImageView mediaImage;
    private VideoView videoView;
    private CircularProgressButton pinBtn;
    private static Target spacePicTarget;
    private SpaceService spaceService;
    private boolean isPhoto = false;
    private boolean isVideo = false;
    private Bitmap rotatedBitmap;
    private Uri finalVideo;
    private CardView postPhotoCard, postVideoCard;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String username;

    private List<SpaceItem> spaceItem;
    private SpaceItem space;

    private RequestQueue rQueue;

    private static final int PICKER_REQUEST_CODE = 1;

    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiManager.install(new IosEmojiProvider());
        setContentView(R.layout.ring_space_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Drawable upArrow = getResources().getDrawable(R.drawable.tiny_ic_back);
        upArrow.setColorFilter(getResources().getColor(R.color.contentTextColor), PorterDuff.Mode.SRC_ATOP);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        toolbar.setTitleTextColor(getResources().getColor(R.color.QweekColorAccent));

        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        Timber.plant(new Timber.DebugTree());

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(getApplication()));
        // session manager

        // Fetching user details from SQLite
        HashMap<String, String> userData = db.getUserDetails();

        username = userData.get("username");
        String avatar = userData.get("avatar");

        CircleImageView userAvatar = findViewById(R.id.userProfilePic);

        /*Picasso.get().load(avatar).resize(28, 28).placeholder(R.drawable.ic_alien).error(R.drawable.ic_alien).into(userAvatar);*/

        RequestOptions requestOptions = new RequestOptions() // because file name is always same
                .format(DecodeFormat.PREFER_RGB_565);
        Drawable placeholder = getTinted(getResources().getColor(R.color.contentTextColor));
        Glide
                .with(getApplicationContext())
                .load(avatar)
                .override(28, 28)
                .placeholder(placeholder)
                .error(placeholder)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(requestOptions)
                .into(userAvatar);

        userAvatar.setOnClickListener(v -> {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("profile", username);
            startActivity(i);
            customType(this, "fadein-to-fadeout");
        });

        inputMessage = findViewById(R.id.chat_message);
        FloatingActionButton btnSend = findViewById(R.id.btn_send);

        ImageView mediaBtn = findViewById(R.id.mediaBtn);
        mediaImage = findViewById(R.id.mediaImage);
        videoView = findViewById(R.id.mediaVideo);
        postPhotoCard = findViewById(R.id.post_photoCard);
        postVideoCard = findViewById(R.id.post_videoCard);

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

        Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        String title = intent.getStringExtra("name");

        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(title);

        spaceImage = findViewById(R.id.spaceImage);

        emptyLayout = findViewById(R.id.empty_layout);

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.main_swiperefresh);

        messageArrayList = new ArrayList<>();

        // self user id is to identify the message owner
        selfUserId = AppController.getInstance().getPrefManager().getUser().getId();

        //init service and load data
        spaceService = QweekdotsApi.getClient(getApplicationContext()).create(SpaceService.class);

        mAdapter = new RingSpaceThreadAdapter(this, messageArrayList, selfUserId, username);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
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
                        Toasty.info(getApplicationContext(), "Dropping...", Toasty.LENGTH_LONG).show();
                        postPhotoDrop(rotatedBitmap, selfUserId);
                    }
                }
                if(finalVideo!=null) {
                    if(isVideo) {
                        Toasty.info(getApplicationContext(), "Dropping...", Toasty.LENGTH_LONG).show();
                        postVideoDrop(finalVideo, selfUserId);
                    }
                }
                if(rotatedBitmap==null&&finalVideo==null) {
                    Toasty.error(getApplicationContext(), "Can't send empty balloons, can we?", Toast.LENGTH_LONG).show();
                }
            } else {
                Toasty.info(getApplicationContext(), "Dropping...", Toasty.LENGTH_LONG).show();
                if(isPhoto&&rotatedBitmap!=null) {
                    postPhotoDrop(rotatedBitmap, selfUserId);
                } else if(isVideo&&finalVideo!=null) {
                    postVideoDrop(finalVideo, selfUserId);
                } else {
                    postDrop(selfUserId);
                }
            }
        });

        loadSpace();
        fetchChatThread();
        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);
    }

    /**
     * Triggers the actual background refresh via the {@link SwipeRefreshLayout}
     */
    private void doRefresh() {
        if(isNetworkConnected()) {
            fetchChatThread();
            messageArrayList.clear();
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 1) {
                // scrolling to bottom of the recycler view
                Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
            }
            swipeRefreshLayout.setRefreshing(false);

            Timber.tag(TAG).d("Loading First Feed Page");
        } else {
            Toasty.info(this, "No Jet Fuel, connect to the internet", Toast.LENGTH_LONG).show();

            Timber.tag(TAG).d("No internet connection available");
        }
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
        String chatRoomId = intent.getStringExtra("chat_room_id");

        if (message != null && chatRoomId != null) {
            messageArrayList.add(message);
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 1) {
                Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
            }
        }
    }

    private void loadSpace() {
        callSpaceProfileApi().enqueue(new Callback<SpaceProfileModel>() {
            @SuppressLint({"SetTextI18n", "RestrictedApi"})
            @Override
            public void onResponse(@NotNull Call<SpaceProfileModel> call, @NotNull Response<SpaceProfileModel> response) {

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                spaceItem = fetchSpaceProfile(response);
                space = spaceItem.get(0);

                //toolbar.setTitle(space.getSpacename());

                /*spacePicTarget = new com.squareup.picasso.Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        spaceImage.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                };

                Picasso.get().
                        load(space.getSpaceArt())
                        .resize(spaceImage.getWidth(), 125)
                        .centerInside()
                        .into(spacePicTarget);*/

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

                    String sent = "Pinned to MySpaces";
                    Toasty.success(getApplicationContext(), sent, Toast.LENGTH_LONG).show();
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

    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void postDrop(final String id) {
        final String message = this.inputMessage.getText().toString();

        // Tag used to cancel the request
        String tag_string_req = "req_post";

        this.inputMessage.setText("");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/ring_droptext.php", response -> {
            Timber.tag(TAG).d("Drop Response: %s", response);

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

                    Toasty.success(getApplicationContext(), "Sent", Toasty.LENGTH_SHORT).show();
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
            if(error.getMessage() == null) {
                Toasty.error(getApplicationContext(),
                        "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
            } else {
                Toasty.error(getApplicationContext(),
                        Objects.requireNonNull(error.getMessage()), Toast.LENGTH_LONG).show();
            }
            this.inputMessage.setText(message);
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to drop url
                Map<String, String> params = new HashMap<>();
                params.put("user_id", id);
                params.put("chat_message", message);
                params.put("chat_room_id", chatRoomId);

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
    private void postPhotoDrop(final Bitmap bitmap, final String id) {
        final String message = this.inputMessage.getText().toString();

        this.inputMessage.setText("");

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/ring_dropphoto.php",
                response -> {
                    Timber.tag(TAG).d("Drop Response: %s", new String(response.data));

                    try {
                        JSONObject obj = new JSONObject(new String(response.data));

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
             * */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", id);
                params.put("chat_message", message);
                params.put("chat_room_id", chatRoomId);
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
    private void postVideoDrop(Uri videoFile, final String id) {
        final String message = this.inputMessage.getText().toString();

        this.inputMessage.setText("");

        InputStream iStream;
        try {

            iStream = getContentResolver().openInputStream(videoFile);
            assert iStream != null;
            final byte[] inputData = getVideoBytes(iStream);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                    "https://qweek.fun/genjitsu/chat/ring_dropvideo.php",
                    response -> {
                        Timber.tag(TAG).d("Drop Response: %s", new String(response.data));
                        rQueue.getCache().clear();
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));

                            // Check for error node in json
                            // check for error
                            if (!obj.getBoolean("error")) {
                                videoView.setVisibility(View.GONE);
                                videoView.stopPlayback();
                                videoView.setVideoPath(null);

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
                 * */
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("user_id", id);
                    params.put("chat_message", message);
                    params.put("chat_room_id", chatRoomId);
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
        Log.d(TAG, "endPoint: " + endPoint);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                endPoint, response -> {
                    Log.d(TAG, "response: " + response);

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
                                    String liked = commentObj.getString("liked");
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
                                    message.setLiked(liked);
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
                            Toasty.error(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
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

    public void onClick(View v) {
        super.onBackPressed();
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

    private void stopButtonAnimation(){
        pinBtn.revertAnimation();
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

    /**
     * Remember to add android.permission.ACCESS_NETWORK_STATE permission.
     *
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return cm.getActiveNetworkInfo() != null;
    }

}
