package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.creator.qweekdots.R;
import com.creator.qweekdots.api.ProfileService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.ProfileModel;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class EditUsernameBottomSheet extends RoundedBottomSheetDialogFragment {
    private final String TAG = EditUsernameBottomSheet.class.getSimpleName();

    private String username;

    private BottomSheetBehavior bottomSheetBehavior;
    private ProfileService profileService;

    private View extraSpace;
    private CircularProgressButton saveBtn;
    private EditText optUsernameTxt;

    private List<UserItem> userItem;
    private UserItem user;

    private String optionValue;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        //inflating layout
        View view = View.inflate(getContext(), R.layout.edit_username_bottom_sheet, null);

        if(getContext()!=null) {
            // SqLite database handler
            SQLiteHandler db = new SQLiteHandler(requireActivity());
            // session manager

            // Fetching user details from SQLite
            HashMap<String, String> userData = db.getUserDetails();

            username = userData.get("username");

            extraSpace = view.findViewById(R.id.extraSpace);
            optUsernameTxt = view.findViewById(R.id.optUsernameSheetTxt);
            saveBtn = view.findViewById(R.id.optUsernameSheetSaveButton);
            saveBtn.setClickable(false);

            optUsernameTxt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    saveBtn.setClickable(true);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    saveBtn.setOnClickListener(view1 -> {
                        if(s.length() != 0) {
                            if(s.length() < 3) {
                                Toasty.info(requireContext(), "Username is too short", Toast.LENGTH_SHORT).show();
                            } else {
                                // run save
                            }
                        } else {
                            Toasty.info(requireContext(), "Username is non-existent", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            //setting layout with bottom sheet
            bottomSheet.setContentView(view);

            bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));


            //setting Peek
            bottomSheetBehavior.setPeekHeight(500);


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

            loadOptUsername();
        }


        return bottomSheet;
    }

    private void loadOptUsername() {
        callProfileApi().enqueue(new Callback<ProfileModel>() {
            @SuppressLint({"SetTextI18n", "RestrictedApi"})
            @Override
            public void onResponse(@NotNull Call<ProfileModel> call, @NotNull Response<ProfileModel> response) {

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                userItem = fetchProfile(response);
                user = userItem.get(0);

                optUsernameTxt.setText(user.getUsername());
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

    @Override
    public void onStart() {
        super.onStart();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}
