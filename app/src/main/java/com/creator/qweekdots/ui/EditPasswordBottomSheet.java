package com.creator.qweekdots.ui;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class EditPasswordBottomSheet extends RoundedBottomSheetDialogFragment {
    private final String TAG = EditMobileBottomSheet.class.getSimpleName();

    private String username;

    private BottomSheetBehavior bottomSheetBehavior;

    private CircularProgressButton saveBtn;
    private EditText oldPasswordTxt;
    private EditText newPasswordTxt;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        //inflating layout
        View view = View.inflate(getContext(), R.layout.edit_password_bottom_sheet, null);

        if(getContext()!=null) {
            // SqLite database handler
            SQLiteHandler db = new SQLiteHandler(requireActivity());
            // session manager

            // Fetching user details from SQLite
            HashMap<String, String> userData = db.getUserDetails();

            username = userData.get("username");

            View extraSpace = view.findViewById(R.id.extraSpace);
            oldPasswordTxt = view.findViewById(R.id.oldPasswordSheetTxt);
            newPasswordTxt = view.findViewById(R.id.newPasswordSheetTxt);
            EditText repeatPasswordTxt = view.findViewById(R.id.repeatPasswordSheetTxt);
            saveBtn = view.findViewById(R.id.optPasswordSheetSaveButton);
            saveBtn.setClickable(false);

            oldPasswordTxt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            newPasswordTxt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            repeatPasswordTxt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    saveBtn.setClickable(true);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.length() != 0) {
                        //run save
                        update(oldPasswordTxt.getText().toString(), newPasswordTxt.getText().toString(), s.toString(), username);
                    } else {
                        Toasty.info(requireContext(), "Your security is at stake here", Toast.LENGTH_SHORT).show();
                        stopButtonAnimation();
                    }
                }
            });

            //setting layout with bottom sheet
            bottomSheet.setContentView(view);

            bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));


            //setting Peek
            bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);


            //setting min height of bottom sheet
            extraSpace.setMinimumHeight((Resources.getSystem().getDisplayMetrics().heightPixels) / 2);


            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int i) {
                    if (BottomSheetBehavior.STATE_EXPANDED == i) {

                    }
                    if (BottomSheetBehavior.STATE_COLLAPSED == i) {

                    }

                    if (BottomSheetBehavior.STATE_HIDDEN == i) {
                        dismiss();
                    }

                }

                @Override
                public void onSlide(@NonNull View view, float v) {

                }
            });
        }

        return bottomSheet;
    }

    /**
     * function to update
     * */
    private void update(final String oldPassword, final String newPassword, final String repeatPassword, final String username) {
        // Tag used to cancel the request
        String tag_string_req = "req_update";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_PROFILE_SETTINGS, response -> {
            Timber.tag(TAG).d("Settings Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Stop animation
                    stopButtonAnimation();

                    // success
                    String sent = jObj.getString("sent");
                    Toasty.success(requireContext(), sent, Toast.LENGTH_LONG).show();
                } else {
                    // Error in drop. Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Toasty.error(requireContext(),
                            errorMsg, Toast.LENGTH_LONG).show();
                    stopButtonAnimation();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                stopButtonAnimation();
            }

        }, error -> {
            Timber.tag(TAG).e("Settings Error: %s", error.getMessage());
            Toasty.error(requireContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();

            stopButtonAnimation();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to drop url
                Map<String, String> params = new HashMap<>();
                params.put("savePassword", "save");
                params.put("oldpassword", oldPassword);
                params.put("newpassword", newPassword);
                params.put("repeatpassword", repeatPassword);
                params.put("u", username);

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

    @Override
    public void onStart() {
        super.onStart();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void stopButtonAnimation(){
        saveBtn.revertAnimation();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

}
