package com.creator.qweekdots.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static com.android.volley.VolleyLog.TAG;

public class DropText extends Fragment {
    private String[] textHint = {
            "What's On Your Mind?",
            "Tell The World",
            "What's Happening ?"
    };
    private Random mRandom;
    private FABProgressCircle dropProgress;
    private FloatingActionButton dropBtn;
    private String username;
    private EmojiEditText dropTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRandom = new Random();
        ArrayList<Integer> mBackgroundColors = new ArrayList<>();
        mBackgroundColors.add(R.color.MistyRose);
        mBackgroundColors.add(R.color.Fuchsia);
        mBackgroundColors.add(R.color.Tomato);
        mBackgroundColors.add(R.color.Coral);
        mBackgroundColors.add(R.color.SteelBlue);
        mBackgroundColors.add(R.color.DarkSlateBlue);
        mBackgroundColors.add(R.color.DodgerBlue);
        mBackgroundColors.add(R.color.DarkSlateGray);
        mBackgroundColors.add(R.color.HotPink);
        mBackgroundColors.add(R.color.Goldenrod);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        View rootView = inflater.inflate(R.layout.drop_text, container, false);

        if(new DarkModePrefManager(requireActivity()).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(this).getContext());
        // session manager
        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");

        dropTxt = rootView.findViewById(R.id.drop_txt);
        int randomHint = mRandom.nextInt(3);
        dropTxt.setHint(textHint[randomHint]);

        dropProgress = rootView.findViewById(R.id.dropTextProgress);

        dropBtn = rootView.findViewById(R.id.postTextButton);
        dropBtn.setOnClickListener(v -> {
            dropBtn.setEnabled(false);
            dropProgress.show();

            String drop = dropTxt.getText().toString().trim();
            if(!drop.isEmpty()) {
                Toasty.info(requireContext(), "Dropping...", Toasty.LENGTH_SHORT).show();
                postDrop(drop, username);
            } else {
                Toasty.error(requireContext(), "Come on, you gotta tell us !", Toast.LENGTH_SHORT).show();
                dropProgress.hide();
                dropBtn.setEnabled(true);
            }
        });

        ImageView goBack = rootView.findViewById(R.id.goBack);
        goBack.setOnClickListener(v-> goBack());

        return rootView;
    }

    /**
     * function to post drop text
     * */
    private void postDrop(final String drop, final String username) {
        // Tag used to cancel the request
        String tag_string_req = "req_post";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DROP_TEXT, response -> {
            Timber.tag(TAG).d("Drop Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Stop animation
                    dropProgress.hide();
                    dropBtn.setEnabled(true);
                    dropTxt.setText("");
                    dropTxt.clearFocus();

                    // success
                    String sent = jObj.getString("sent");
                    Toasty.success(requireContext(), sent, Toast.LENGTH_SHORT).show();
                } else {
                    // Error in drop. Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Toasty.error(requireContext(),
                            errorMsg, Toast.LENGTH_LONG).show();
                    dropProgress.hide();
                    dropBtn.setEnabled(true);
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                dropProgress.hide();
                dropBtn.setEnabled(true);
            }

        }, error -> {
            Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
            Toasty.error(requireContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();

            dropProgress.hide();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to drop url
                Map<String, String> params = new HashMap<>();
                params.put("drop", drop);
                params.put("username", username);

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

    public void onClick() {
        assert getFragmentManager() != null;
        getFragmentManager().popBackStack();
    }

    private void goBack() {
        assert getFragmentManager() != null;
        getFragmentManager().popBackStack();
    }

}
