package com.creator.qweekdots.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.volley.VolleyMultipartRequest;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class QweekSnapActivity extends AppCompatActivity {
    private final static String TAG = QweekSnapActivity.class.getSimpleName();
    View decorView;
    Uri finalVideo;

    private String username;

    private FABProgressCircle dropProgress;
    private FloatingActionButton dropBtn;
    private EmojiEditText dropTxt;
    //
    private RequestQueue rQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qweeksnap);

        Intent intent = getIntent();
        String video = Objects.requireNonNull(intent.getExtras()).getString("video");

        finalVideo = Uri.parse(video);

        // Window View
        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(getApplication()));
        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");

        /*
        VideoView videoView = findViewById( R.id.captured_video);
        videoView.setVideoURI(finalVideo);
        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
        videoView.start();
         */

        dropTxt = findViewById(R.id.drop_txt);

        dropProgress = findViewById(R.id.dropSnapProgress);
        dropBtn = findViewById(R.id.upload_snap);
        dropBtn.setOnClickListener(v -> {
            dropBtn.setEnabled(false);
            dropProgress.show();

            String drop = dropTxt.getText().toString().trim();
            Toasty.info(this, "Dropping...", Toasty.LENGTH_SHORT).show();
            if(finalVideo != null) {
                uploadVideo(drop, finalVideo, username);
            }
        });

    }

    /*
     *
     * Upload Functions
     *
     */
    private void uploadVideo(final String drop, final Uri videoFile, final String username){
        InputStream iStream;
        try {

            iStream = getContentResolver().openInputStream(videoFile);
            assert iStream != null;
            final byte[] inputData = getVideoBytes(iStream);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, AppConfig.URL_DROP_QWEEKVID,
                    response -> {
                        Timber.tag(TAG).d("Drop Response: %s", new String(response.data));
                        rQueue.getCache().clear();
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
                                Toasty.success(getApplicationContext(), sent, Toast.LENGTH_SHORT).show();
                            } else {
                                // Error in drop. Get the error message
                                String errorMsg = jObj.getString("error_msg");
                                Toasty.error(getApplicationContext(),
                                        errorMsg, Toast.LENGTH_SHORT).show();
                                dropProgress.hide();
                                dropBtn.setEnabled(true);
                            }
                        } catch (JSONException e) {
                            // JSON Error
                            e.printStackTrace();
                            Toasty.error(getApplicationContext(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                            dropProgress.hide();
                            dropBtn.setEnabled(true);
                        }
                    },
                    error -> {Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                        Toasty.error(getApplicationContext(),
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
            rQueue = Volley.newRequestQueue(getApplicationContext());
            rQueue.add(volleyMultipartRequest);



        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void onClick(View v) {
        super.onBackPressed(); // or super.finish();
    }
}
