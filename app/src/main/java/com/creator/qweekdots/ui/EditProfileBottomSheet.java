package com.creator.qweekdots.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.api.ProfileService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.ProfileModel;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.utils.ImagePicker;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.creator.qweekdots.volley.VolleyMultipartRequest;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.vanniktech.emoji.EmojiEditText;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

public class EditProfileBottomSheet extends RoundedBottomSheetDialogFragment {
    private final String TAG = EditProfileBottomSheet.class.getSimpleName();

    private String username;
    private SQLiteHandler db;

    private BottomSheetBehavior bottomSheetBehavior;
    private ProfileService profileService;

    private CircularProgressButton saveBtn;
    private CircleImageView profilePic;
    private ImageView profileCover;
    private EmojiEditText optBioTxt;

    private List<UserItem> userItem;
    private UserItem user;

    private Bitmap profilePicBitmap;
    private Bitmap profileCoverBitmap;

    private boolean isAvatar = false;
    private boolean isCover = false;
    private boolean isBioChanged = false;

    private static final int REQUEST_IMAGE = 100;
    private static final int REQUEST_COVER = 200;

    @NotNull
    @Override
    @SuppressLint("ClickableViewAccessibility")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        //inflating layout
        View view = View.inflate(getContext(), R.layout.edit_profile_bottom_sheet, null);

        if(getContext()!=null) {
            // SqLite database handler
            db = new SQLiteHandler(requireActivity());
            // session manager

            // Fetching user details from SQLite
            HashMap<String, String> userData = db.getUserDetails();

            username = userData.get("username");

            View extraSpace = view.findViewById(R.id.extraSpace);
            profilePic = view.findViewById(R.id.optProfilePic);
            profileCover = view.findViewById(R.id.optProfileCover);
            optBioTxt = view.findViewById(R.id.optBioSheetTxt);
            FloatingActionButton picEditBtn = view.findViewById(R.id.optPicEditBtn);
            FloatingActionButton picCoverEditBtn = view.findViewById(R.id.optCoverEditBtn);
            saveBtn = view.findViewById(R.id.optProfileSheetSaveButton);
            saveBtn.setClickable(false);

            ImagePicker.clearCache(requireContext());

            picEditBtn.setOnClickListener(v-> Dexter.withActivity(getActivity())
                    .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                showImagePickerOptions();
                            }

                            if (report.isAnyPermissionPermanentlyDenied()) {
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check());

            picCoverEditBtn.setOnClickListener(v-> Dexter.withActivity(getActivity())
                    .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                showCoverImagePickerOptions();
                            }

                            if (report.isAnyPermissionPermanentlyDenied()) {
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check());

            optBioTxt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    isBioChanged = true;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    isBioChanged = true;
                }
            });

            saveBtn.setOnClickListener(v-> {
                saveBtn.startAnimation();
                if(profilePicBitmap!=null) {
                    updateAvatar(profilePicBitmap, username);
                }
                if(profileCoverBitmap!=null) {
                    updateCover(profileCoverBitmap, username);
                }
                if(isBioChanged) {
                    updateBio(optBioTxt.getText().toString(), username);
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

            //init service and load data
            profileService = QweekdotsApi.getClient(getContext()).create(ProfileService.class);

            loadOptProfile();
        }

        return bottomSheet;
    }

    private void loadAvatar(String url) {
        Timber.tag(TAG).d("Image cache path: %s", url);

        RequestOptions requestOptions = new RequestOptions() // because file name is always same
                .format(DecodeFormat.PREFER_RGB_565);
        Glide
                .with(requireContext())
                .load(url)
                .override(100, 100)
                .placeholder(R.drawable.alien)
                .error(R.drawable.alien)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(requestOptions)
                .into(profilePic);
    }

    private void loadCover(String url) {
        Timber.tag(TAG).d("Cover cache path: %s", url);

        RequestOptions requestOptions = new RequestOptions() // because file name is always same
                .format(DecodeFormat.PREFER_RGB_565);
        Glide
                .with(requireContext())
                .load(url)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(requestOptions)
                .into(profileCover);
    }

    private void loadOptProfile() {
        callProfileApi().enqueue(new Callback<ProfileModel>() {
            @SuppressLint({"SetTextI18n", "RestrictedApi"})
            @Override
            public void onResponse(@NotNull Call<ProfileModel> call, @NotNull Response<ProfileModel> response) {

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                userItem = fetchProfile(response);
                user = userItem.get(0);

                //Load Avatar
                RequestOptions requestOptions = new RequestOptions() // because file name is always same
                        .format(DecodeFormat.PREFER_RGB_565);
                Glide
                        .with(requireContext())
                        .load(user.getAvatar())
                        .override(100, 100)
                        .placeholder(R.drawable.alien)
                        .error(R.drawable.alien)
                        .thumbnail(0.3f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .apply(requestOptions)
                        .into(profilePic);

                //Load Cover
                Glide
                        .with(requireContext())
                        .load(user.getProfileCover())
                        .thumbnail(0.3f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .apply(requestOptions)
                        .into(profileCover);

                // Load Bio
                optBioTxt.setText(user.getBio());
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

    private void showImagePickerOptions() {
        ImagePicker.showImagePickerOptions(getContext(), new ImagePicker.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void showCoverImagePickerOptions() {
        ImagePicker.showImagePickerOptions(getContext(), new ImagePicker.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCoverCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchCoverGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(getActivity(), ImagePicker.class);
        intent.putExtra(ImagePicker.INTENT_IMAGE_PICKER_OPTION, ImagePicker.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePicker.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePicker.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePicker.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePicker.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(getActivity(), ImagePicker.class);
        intent.putExtra(ImagePicker.INTENT_IMAGE_PICKER_OPTION, ImagePicker.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePicker.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchCoverCameraIntent() {
        Intent intent = new Intent(getActivity(), ImagePicker.class);
        intent.putExtra(ImagePicker.INTENT_IMAGE_PICKER_OPTION, ImagePicker.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePicker.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_X, 3); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_Y, 4);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePicker.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePicker.INTENT_BITMAP_MAX_WIDTH, 1200);
        intent.putExtra(ImagePicker.INTENT_BITMAP_MAX_HEIGHT, 1200);

        startActivityForResult(intent, REQUEST_COVER);
    }

    private void launchCoverGalleryIntent() {
        Intent intent = new Intent(getActivity(), ImagePicker.class);
        intent.putExtra(ImagePicker.INTENT_IMAGE_PICKER_OPTION, ImagePicker.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePicker.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_X, 3); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePicker.INTENT_ASPECT_RATIO_Y, 4);
        startActivityForResult(intent, REQUEST_COVER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = Objects.requireNonNull(data).getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    profilePicBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                    isAvatar = true;

                    // loading profile image from local cache
                    loadAvatar(Objects.requireNonNull(uri).toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == REQUEST_COVER) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = Objects.requireNonNull(data).getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    profileCoverBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                    isCover = true;

                    // loading profile image from local cache
                    loadCover(Objects.requireNonNull(uri).toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    /*
     * Upload Functions
     * */

    /*
     * The method is taking Bitmap as an argument
     * then it will return the byte[] array for the given bitmap
     * and we will send this array to the server
     * here we are using PNG Compression with 80% quality
     * you can give quality between 0 to 100
     * 0 means worse quality
     * 100 means best quality
     * */
    private byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /*
     * Update Avatar Function
     */
    private void updateAvatar(final Bitmap bitmap, final String username) {
        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, AppConfig.URL_PROFILE_SETTINGS,
                response -> {
                    Timber.tag(TAG).d("Settings Response: %s", new String(response.data));

                    try {
                        JSONObject jObj = new JSONObject(new String(response.data));
                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {
                            // Stop animation
                            stopButtonAnimation();

                            // success
                            String sent = jObj.getString("sent");
                            String profilePic = jObj.getString("avatar");
                            Toasty.success(requireContext(), sent, Toast.LENGTH_LONG).show();

                            ContentValues values = new ContentValues();
                            values.put("avatar", profilePic);

                            db.getWritableDatabase().update("user", values, "id='1'", null);

                            ProfileActivity.loadProfileImage(profilePic, requireContext());
                        } else {
                            // Error in drop. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toasty.error(requireContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                            stopButtonAnimation();
                        }
                    } catch (JSONException e) {
                        // JSON Error
                        e.printStackTrace();
                        Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                        stopButtonAnimation();
                    }

                },
                error -> {
                    Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                    Toasty.error(requireContext(),
                            "Apollo, we have a problem !", Toast.LENGTH_LONG).show();

                    stopButtonAnimation();
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
                params.put("u", username);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("pPic", new DataPart("avatar_" + imagename + ".jpg", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(requireContext()).add(volleyMultipartRequest);
    }


    /*
     * Update Cover Function
     */
    private void updateCover(final Bitmap bitmap, final String username) {
        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, AppConfig.URL_PROFILE_SETTINGS,
                response -> {
                    Timber.tag(TAG).d("Settings Response: %s", new String(response.data));

                    try {
                        JSONObject jObj = new JSONObject(new String(response.data));
                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {
                            // Stop animation
                            stopButtonAnimation();

                            // success
                            String sent = jObj.getString("sent");
                            String profileCover = jObj.getString("cover");
                            Toasty.success(requireContext(), sent, Toast.LENGTH_LONG).show();

                            //ContentValues values = new ContentValues();
                            //values.put("cover", profileCover);

                            //db.getWritableDatabase().update("user", values, "id='1'", null);

                            ProfileActivity.loadProfileCover(profileCover, requireContext());
                        } else {
                            // Error in drop. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toasty.error(requireContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                            stopButtonAnimation();
                        }
                    } catch (JSONException e) {
                        // JSON Error
                        e.printStackTrace();
                        Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                        stopButtonAnimation();
                    }

                },
                error -> {
                    Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                    Toasty.error(requireContext(),
                            "Apollo, we have a problem !", Toast.LENGTH_LONG).show();

                    stopButtonAnimation();
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
                params.put("u", username);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("pCover", new DataPart("cover_" + imagename + ".jpg", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(requireContext()).add(volleyMultipartRequest);
    }

    /*
     * Update Bio Function
     * */
    private void updateBio(final String updated, final String username) {
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
                    String bio = jObj.getString("bio");
                    Toasty.success(requireContext(), sent, Toast.LENGTH_LONG).show();

                    ContentValues values = new ContentValues();
                    values.put("bio", bio);

                    ProfileActivity.loadProfileBio(bio);

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
                params.put("bio", updated);
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
