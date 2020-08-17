package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class MessageReplyBottomSheet extends RoundedBottomSheetDialogFragment {
    private final String TAG = ReplyCommentBottomSheet.class.getSimpleName();
    private EmojiEditText replyTxt;
    private TextView replyBtn;

    private String drop_id;
    private int parent_id;
    private String username;


    public MessageReplyBottomSheet(String drop_id, int parent_id, String username) {
        this.drop_id = drop_id;
        this.parent_id = parent_id;
        this.username = username;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        //inflating layout
        View view = View.inflate(getContext(), R.layout.reply_comment_bottom_sheet, null);

        View extraSpace = view.findViewById(R.id.extraSpace);

        replyTxt = view.findViewById(R.id.reply_comment_txt);
        replyBtn = view.findViewById(R.id.btn_send);
        replyBtn.setClickable(false);

        replyTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                replyBtn.setClickable(true);
                replyBtn.setTextColor(getResources().getColor(R.color.contentTextColor));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                replyBtn.setOnClickListener(view1 -> {
                    replyBtn.setText("Sending...");
                    if(s.length() != 0) {
                        // reply
                        sendReply(s.toString());
                    } else {
                        Toasty.info(requireContext(), "Don't have anything to say ?", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //setting layout with bottom sheet
        bottomSheet.setContentView(view);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        //setting Peek
        bottomSheetBehavior.setPeekHeight(600);
        //setting min height of bottom sheet
        extraSpace.setMinimumHeight((Resources.getSystem().getDisplayMetrics().heightPixels) / 2);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss();
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {}
        });

        return bottomSheet;
    }

    /**
     * Function to reply to comment
     */
    @SuppressLint("SetTextI18n")
    private void sendReply(String drop) {
        // Tag used to cancel the request
        String tag_string_req = "req_post";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/reply.php", response -> {
            Timber.tag(TAG).d("Drop Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Stop animation
                    replyBtn.setClickable(true);
                    replyBtn.setText("Send");
                    replyBtn.setTextColor(getResources().getColor(R.color.Gray));
                    replyTxt.setText("");
                    replyTxt.clearFocus();

                    // success
                    String sent = jObj.getString("sent");
                    Toasty.success(requireContext(), sent, Toast.LENGTH_LONG).show();
                } else {
                    // Error in drop. Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Toasty.error(requireContext(),
                            errorMsg, Toast.LENGTH_LONG).show();
                    replyBtn.setClickable(true);
                    replyBtn.setText("Send");
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                replyBtn.setClickable(true);
                replyBtn.setText("Send");
            }

        }, error -> {
            Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
            Toasty.error(requireContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
            replyBtn.setClickable(true);
            replyBtn.setText("Send");
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to drop url
                Map<String, String> params = new HashMap<>();
                params.put("drop", drop);
                params.put("username", username);
                params.put("drop_id", drop_id);
                params.put("parent_id", String.valueOf(parent_id));

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
}
