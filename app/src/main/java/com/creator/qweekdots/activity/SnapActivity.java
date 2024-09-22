package com.creator.qweekdots.activity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.Volley;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.snaps.SnapsPicker;
import com.creator.qweekdots.utils.ImageUtil;
import com.creator.qweekdots.volley.VolleyMultipartRequest;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class SnapActivity extends AppCompatActivity {
    private final static String TAG = SnapActivity.class.getSimpleName();
    private Bitmap finalBitmap;
    View decorView;
    Uri uri;

    private String username;

    private FABProgressCircle dropProgress;
    private FloatingActionButton dropBtn;
    private EmojiEditText dropTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap);

        // Window View
        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(getApplication()));
        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");

        dropTxt = findViewById(R.id.drop_txt);

        //ImageView capturedImage = findViewById(R.id.captured_snap);

        SnapsPicker a = new SnapsPicker(SnapActivity.this);
        a.show(1, 1, address -> {
            // receive image address in here
            uri = Uri.parse(address);

            try {
                finalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                /*
                Glide.with(this)
                        .load(finalBitmap)
                        .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                        .into(capturedImage);*/
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Log.d(TAG, finalBitmap.toString());
        });

        dropProgress = findViewById(R.id.dropSnapProgress);
        dropBtn = findViewById(R.id.upload_snap);
        dropBtn.setOnClickListener(v -> {
            dropBtn.setEnabled(false);
            dropProgress.show();

            String drop = dropTxt.getText().toString().trim();
            Toasty.info(this, "Dropping...", Toasty.LENGTH_SHORT).show();
            if(finalBitmap != null) {
                uploadBitmap(drop, finalBitmap, username);
            }
        });
    }

    /**
     * Upload Function
     */
    private void uploadBitmap(final String drop, final Bitmap bitmap, final String username) {
        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, AppConfig.URL_DROP_QWEEKPIC,
                response -> {
                    Timber.tag(TAG).d("Drop Response: %s", new String(response.data));
                    try {
                        JSONObject jObj = new JSONObject(new String(response.data));
                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {
                            // Stop animation
                            dropProgress.hide();
                            dropBtn.setEnabled(true);

                            finish();

                            // success
                            String sent = jObj.getString("sent");
                            Toasty.success(this, sent, Toast.LENGTH_SHORT).show();
                        } else {
                            // Error in drop. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toasty.error(this,
                                    errorMsg, Toast.LENGTH_SHORT).show();
                            dropProgress.hide();
                            dropBtn.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        // JSON Error
                        e.printStackTrace();
                        Toasty.error(this, "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                        dropProgress.hide();
                        dropBtn.setEnabled(true);
                    }
                },
                error -> {
                    Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                    Toasty.error(this,
                            "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();

                    dropProgress.hide();
                    dropBtn.setEnabled(true);
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
                params.put("user", username);
                params.put("drop", drop);
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

    public void onClick(View v) {
        super.onBackPressed(); // or super.finish();
    }
}
