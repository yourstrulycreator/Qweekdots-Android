package com.creator.qweekdots.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static com.android.volley.VolleyLog.TAG;

public class DropReactionsActivity extends AppCompatActivity {
    private EmojiEditText dropTxt;
    private FABProgressCircle dropProgress;
    private FloatingActionButton dropBtn;

    private String username;
    private String url;

    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drop_reactions);

        // Window View
        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        Intent i = getIntent();
        url = i.getExtras().getString("url");

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(this);
        // session manager
        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");

        dropTxt = findViewById(R.id.drop_txt);
        ImageView mImageView = findViewById(R.id.gifImage);

        ImageView goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(v-> goBack());

        Glide
                .with(this)
                .asGif()
                .load(url)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mImageView);

        dropProgress = findViewById(R.id.dropReactionsProgress);

        dropBtn = findViewById(R.id.postReactionsButton);
        dropBtn.setOnClickListener(v -> {
            dropBtn.setEnabled(false);
            dropProgress.show();

            String drop = dropTxt.getText().toString().trim();
            if(!drop.isEmpty()) {
                Toasty.info(this, "Dropping...", Toasty.LENGTH_SHORT).show();
                postDrop(drop, username);
            } else {
                Toasty.error(this, "Come on, you gotta say something !", Toast.LENGTH_SHORT).show();
                dropProgress.hide();
                dropBtn.setEnabled(true);
            }
        });
    }

    /**
     * function to post drop text
     * */
    private void postDrop(final String drop, final String username) {
        // Tag used to cancel the request
        String tag_string_req = "req_post";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DROP_GIF, response -> {
            Timber.tag(TAG).d("Drop Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Stop animation
                    dropProgress.hide();

                    goBack();

                    // success
                    String sent = "Dropped!";
                    Toasty.success(this, sent, Toast.LENGTH_SHORT).show();
                } else {
                    // Error in drop. Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Toasty.error(this,
                            errorMsg, Toast.LENGTH_LONG).show();
                    dropProgress.hide();
                    dropBtn.setEnabled(true);
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(this, "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                dropProgress.hide();
                dropBtn.setEnabled(true);
            }

        }, error -> {
            Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
            Toasty.error(this,
                    "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();
            dropProgress.hide();
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to drop url
                Map<String, String> params = new HashMap<>();
                params.put("drop", drop);
                params.put("username", username);
                params.put("url", url);

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

    private void goBack() {
        super.finish();
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
