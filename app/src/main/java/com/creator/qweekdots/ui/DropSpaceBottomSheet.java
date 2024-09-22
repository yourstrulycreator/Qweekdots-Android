package com.creator.qweekdots.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.models.Message;
import com.creator.qweekdots.models.User;
import com.creator.qweekdots.utils.GifSizeFilter;
import com.creator.qweekdots.utils.Glide4Engine;
import com.creator.qweekdots.utils.ImageUtil;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.creator.qweekdots.volley.VolleyMultipartRequest;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class DropSpaceBottomSheet extends RoundedBottomSheetDialogFragment {
    private final String TAG = DropSpaceBottomSheet.class.getSimpleName();
    private BottomSheetBehavior bottomSheetBehavior;

    private Context context;
    private String username, chatRoomId, title, selfUserId;

    private EmojiEditText inputMessage;
    private ImageView mediaImage;
    private VideoView videoView;
    private boolean isPhoto = false;
    private boolean isVideo = false;
    private Bitmap rotatedBitmap;
    private Uri finalVideo;
    private CardView postPhotoCard, postVideoCard;

    private RequestQueue rQueue;

    private static final int PICKER_REQUEST_CODE = 1;

    public DropSpaceBottomSheet(Context context, String username, String chatRoomId, String title, String selfUserId) {
        this.context = context;
        this.username = username;
        this.chatRoomId = chatRoomId;
        this.title = title;
        this.selfUserId = selfUserId;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        View view = inflater.inflate(R.layout.drop_space_bottom_sheet, container, false);

        if (context != null) {
            ImageView closeSheet = view.findViewById(R.id.closeSheet);
            closeSheet.setOnClickListener(v -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                dismiss();
            });

            inputMessage = view.findViewById(R.id.space_drop);
            TextView btnSend = view.findViewById(R.id.btn_send);
            btnSend.setClickable(true);
            btnSend.setTextColor(getResources().getColor(R.color.contentTextColor));

            ImageView mediaBtn = view.findViewById(R.id.mediaBtn);
            mediaImage = view.findViewById(R.id.mediaImage);
            videoView = view.findViewById(R.id.mediaVideo);
            postPhotoCard = view.findViewById(R.id.post_photoCard);
            postVideoCard = view.findViewById(R.id.post_videoCard);


            mediaBtn.setOnClickListener(v-> {
                String[] PERMISSIONS = {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                if(hasPermissions(context, PERMISSIONS)) {
                    ShowPicker();
                } else{
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PICKER_REQUEST_CODE);
                }
            });

            btnSend.setOnClickListener(v -> {
                String drop = String.valueOf(this.inputMessage.getText());
                if(drop.isEmpty()) {
                    if(rotatedBitmap!=null) {
                        if(isPhoto) {
                            Toasty.info(context, "Dropping...", Toasty.LENGTH_LONG).show();
                            postPhotoDrop(rotatedBitmap, selfUserId);
                        }
                    }
                    if(finalVideo!=null) {
                        if(isVideo) {
                            Toasty.info(context, "Dropping...", Toasty.LENGTH_LONG).show();
                            postVideoDrop(finalVideo, selfUserId);
                        }
                    }
                    if(rotatedBitmap==null&&finalVideo==null) {
                        Toasty.error(context, "Can't send empty balloons, can we?", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toasty.info(context, "Dropping...", Toasty.LENGTH_LONG).show();
                    if(isPhoto&&rotatedBitmap!=null) {
                        postPhotoDrop(rotatedBitmap, selfUserId);
                    } else if(isVideo&&finalVideo!=null) {
                        postVideoDrop(finalVideo, selfUserId);
                    } else {
                        postDrop(selfUserId);
                    }
                }
            });
        }

        return view;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        //inflating layout
        View view = View.inflate(getContext(), R.layout.drop_space_bottom_sheet, null);

        View extraSpace = view.findViewById(R.id.extraSpace);

        //setting layout with bottom sheet
        bottomSheet.setContentView(view);

        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(0);

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
        if (requestCode == PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // List that will contain the selected files/videos
            List<Uri> mSelected = Matisse.obtainResult(data);

            Timber.tag(TAG).e(mSelected.get(0).toString());

            if (mSelected.get(0).toString().contains("image")) {
                //handle image
                try {
                    Uri uri = mSelected.get(0);
                    // You can update this bitmap to your server
                    rotatedBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

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
                        Toasty.error(context, "Oops, something went wrong, kindly Try Again",
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
                Toasty.info(context, "Neither Image nor Video was selected", Toasty.LENGTH_SHORT).show();
            }

            Timber.tag("Matisse").d("mSelected: %s", mSelected);
        }
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

                    Toasty.success(context, "Sent", Toasty.LENGTH_SHORT).show();
                } else {
                    Toasty.error(context, "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
            if(error.getMessage() == null) {
                Toasty.error(context,
                        "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
            } else {
                Toasty.error(context,
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

                Timber.tag(TAG).e("Params: %s", params.toString());

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

                            Toasty.success(context, "Sent", Toasty.LENGTH_SHORT).show();
                        } else {
                            // Error in drop. Get the error message
                            Toasty.error(context, "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_LONG).show();
                    }

                },
                error -> {
                    Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                    Toasty.error(context,
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
        Volley.newRequestQueue(context).add(volleyMultipartRequest);
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

            iStream = context.getContentResolver().openInputStream(videoFile);
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

                                Toasty.success(context, "Sent", Toasty.LENGTH_SHORT).show();
                            } else {
                                // Error in drop. Get the error message
                                Toasty.error(context, "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toasty.error(context, "Mission Control, come in !", Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                        Toasty.error(context,
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
            rQueue = Volley.newRequestQueue(context);
            rQueue.add(volleyMultipartRequest);



        } catch (IOException e) {
            e.printStackTrace();
        }

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
