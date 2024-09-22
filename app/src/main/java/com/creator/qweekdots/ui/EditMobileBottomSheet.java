package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.creator.qweekdots.R;
import com.creator.qweekdots.api.ProfileService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.ProfileModel;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import es.dmoral.toasty.Toasty;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class EditMobileBottomSheet extends RoundedBottomSheetDialogFragment {
    private final String TAG = EditMobileBottomSheet.class.getSimpleName();
    private String username;
    private SQLiteHandler db;
    private BottomSheetBehavior bottomSheetBehavior;
    private ProfileService profileService;
    private CircularProgressButton saveBtn;
    private EditText optMobileTxt;
    private TextView optLocationTxt;
    private List<UserItem> userItem;
    private UserItem user;
    private PhoneNumberUtil phoneNumberUtil;
    private Phonenumber.PhoneNumber phoneNumber;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        //inflating layout
        View view = View.inflate(getContext(), R.layout.edit_mobile_bottom_sheet, null);

        if(getContext()!=null) {
            // SqLite database handler
            db = new SQLiteHandler(requireActivity());
            // Fetching user details from SQLite
            HashMap<String, String> userData = db.getUserDetails();
            username = userData.get("username");

            phoneNumberUtil = PhoneNumberUtil.createInstance(getContext());

            // Init Layout
            View extraSpace = view.findViewById(R.id.extraSpace);
            optMobileTxt = view.findViewById(R.id.optMobileSheetTxt);
            optLocationTxt = view.findViewById(R.id.optLocationSheetTxt);
            saveBtn = view.findViewById(R.id.optMobileSheetSaveButton);
            saveBtn.setClickable(false);

            optMobileTxt.setText("+");
            Selection.setSelection(optMobileTxt.getText(), optMobileTxt.getText().length());
            optMobileTxt.addTextChangedListener(new TextWatcher() {
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
                        if(!s.toString().startsWith("+")) {
                            optMobileTxt.setText("+");
                            Selection.setSelection(optMobileTxt.getText(), optMobileTxt.getText().length());
                        }
                        String number = s.toString();

                        String countryIsoCode = getCountryIsoCode(number);
                        optLocationTxt.setText(countryIsoCode);
                    }

                    saveBtn.setOnClickListener(view1 -> {
                        saveBtn.startAnimation();
                        if(s.length() != 0) {
                            //run save
                            update(s.toString(), String.valueOf(optLocationTxt.getText()), username);
                        } else {
                            Toasty.info(requireContext(), "Can't save an empty space", Toast.LENGTH_SHORT).show();
                            stopButtonAnimation();
                        }
                    });
                }
            });

            //setting layout with bottom sheet
            bottomSheet.setContentView(view);
            bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
            //setting Peek
            bottomSheetBehavior.setPeekHeight(600);
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

            //init service and load data
            profileService = QweekdotsApi.getClient(getContext()).create(ProfileService.class);
            //load from server
            loadOptMobile();
        }

        return bottomSheet;
    }

    private void loadOptMobile() {
        callProfileApi().enqueue(new Callback<ProfileModel>() {
            @SuppressLint({"SetTextI18n", "RestrictedApi"})
            @Override
            public void onResponse(@NotNull Call<ProfileModel> call, @NotNull Response<ProfileModel> response) {

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                userItem = fetchProfile(response);
                user = userItem.get(0);

                optMobileTxt.setText(user.getTelephone());
            }

            @Override
            public void onFailure(@NotNull Call<ProfileModel> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * @param response extracts List<{@link ProfileModel >} from response
     *
     */
    private List<UserItem> fetchProfile(Response<ProfileModel> response) {
        ProfileModel profileModel = response.body();
        assert profileModel != null;
        return profileModel.getUserItems();
    }

    /**
     * Performs a Retrofit call to the first QweekFeed API.
     *
     */
    private Call<ProfileModel> callProfileApi() {
        return  profileService.getProfileData(
                username,
                username
        );
    }

    private String getCountryIsoCode(String number) {
        try {
            phoneNumber = phoneNumberUtil.parse(number, null);
        } catch (Exception e) {
            Timber.tag(TAG).i("error during parsing number");
        }
        if(phoneNumber == null) {
            return "N/A";
        } else {
            return phoneNumberUtil.getRegionCodeForCountryCode(phoneNumber.getCountryCode());
        }
    }

    /**
     * function to update
     * */
    private void update(final String updated, final String location, final String username) {
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
                    String telephone = jObj.getString("telephone");
                    Toasty.success(requireContext(), sent, Toast.LENGTH_LONG).show();

                    ContentValues values = new ContentValues();
                    values.put("telephone", telephone);

                    db.getWritableDatabase().update("user", values, "id='1'", null);
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
                params.put("telephone", updated);
                params.put("location", location);
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
