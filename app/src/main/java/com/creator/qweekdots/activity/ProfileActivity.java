package com.creator.qweekdots.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.creator.qweekdots.R;
import com.creator.qweekdots.adapter.TabsAdapter;
import com.creator.qweekdots.api.ProfileService;
import com.creator.qweekdots.api.QweekdotsApi;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.models.ProfileModel;
import com.creator.qweekdots.models.UserItem;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.ui.EditBioBottomSheet;
import com.creator.qweekdots.ui.EditFullnameBottomSheet;
import com.creator.qweekdots.ui.EditProfileBottomSheet;
import com.creator.qweekdots.ui.FollowersBottomSheet;
import com.creator.qweekdots.ui.FollowingBottomSheet;
import com.creator.qweekdots.ui.LikedFeed;
import com.creator.qweekdots.ui.OptionsBottomSheet;
import com.creator.qweekdots.ui.ProfileFeed;
import com.creator.qweekdots.ui.QweeksnapFeed;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Target;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

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

import static maes.tech.intentanim.CustomIntent.customType;

public class ProfileActivity extends AppCompatActivity {
    private final String TAG = ProfileActivity.class.getSimpleName();
    private SQLiteHandler db;

    private SessionManager session;

    private String username;
    private String profile;

    private ProfileService profileService;
    private static CircleImageView profilePic;
    private ImageButton chatBtn;
    private CircularProgressButton followBtn;
    private TextView usernameTxt, dropCount, followingCount,  followersCount;
    private EmojiTextView fullnameTxt, bioTxt;
    private static Target profilePicTarget;
    @SuppressLint("StaticFieldLeak")
    private static TextView staticBioTxt;
    private boolean collapsed;
    private String followed;
    private String profileId, profileName;

    private List<UserItem> userItem;
    private UserItem user;

    View decorView;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "CutPasteId"})
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Timber.plant(new Timber.DebugTree());

        decorView = Objects.requireNonNull(this).getWindow().getDecorView();

        Intent intent = getIntent();
        profile = Objects.requireNonNull(intent.getExtras()).getString("profile");

        // SqLite database handler
        db = new SQLiteHandler(Objects.requireNonNull(getApplication()));
        // session manager
        // Fetching user details from SQLite
        HashMap<String, String> userData = db.getUserDetails();
        username = userData.get("username");

        if(new DarkModePrefManager(this).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(getResources().getColor(R.color.contentBodyColor));
            }
        }

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        ImageButton customOpt = findViewById(R.id.customOptions);
        chatBtn = findViewById(R.id.customChat);
        if(profile.equals(username)) {
            customOpt.setVisibility(View.VISIBLE);
            chatBtn.setVisibility(View.GONE);
        } else {
            customOpt.setVisibility(View.GONE);
            chatBtn.setVisibility(View.VISIBLE);
        }

        //init service and load data
        profileService = QweekdotsApi.getClient(getApplicationContext()).create(ProfileService.class);

        ViewPager viewPager = findViewById(R.id.profile_viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = findViewById(R.id.profile_tabs);
        tabLayout.setupWithViewPager(viewPager);

        profilePic = findViewById(R.id.profileAvatar);
        followBtn = findViewById(R.id.followActionButton);
        fullnameTxt = findViewById(R.id.fullnameTextView);
        usernameTxt = findViewById(R.id.usernameTextView);
        bioTxt = findViewById(R.id.bioText);
        staticBioTxt = findViewById(R.id.bioText);
        dropCount = findViewById(R.id.dropCount);
        followingCount = findViewById(R.id.followingCount);
        followersCount = findViewById(R.id.followerCount);
        LinearLayout followingBox = findViewById(R.id.followingBox);
        LinearLayout followerBox = findViewById(R.id.followerBox);

        loadProfile();

        AppBarLayout appBarLayout1 = findViewById(R.id.profile_appbar);
        appBarLayout1.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if ( verticalOffset < -45) {
                if (!collapsed) {
                    profilePic.animate().scaleX(0f).scaleY(0f).setDuration(350).start();
                    collapsed = true;
                }
            } else {
                if (collapsed) {
                    profilePic.animate().scaleX(1).scaleY(1f).setDuration(350).start();
                    collapsed = false;
                }
            }
        });

        followBtn.setOnClickListener(v -> {
            followBtn.startAnimation();
            if(followed.equals("yes")) {
                doFollow(username, "unfollow", profile);
                user.setFollowed("no");
            } else if(followed.equals("no")) {
                doFollow(username, "follow", profile);
                user.setFollowed("yes");
            }
        });

        chatBtn.setOnClickListener(v -> {
            // when chat is clicked, launch full chat thread activity
            Intent i = new Intent(ProfileActivity.this, ChatUserActivity.class);
            i.putExtra("to", profileId);
            i.putExtra("to_name", profileName);
            startActivity(i);
            customType(ProfileActivity.this, "fadein-to-fadeout");
        });

        followingBox.setOnClickListener(v -> {
            FollowingBottomSheet bottomSheet = new FollowingBottomSheet(getApplicationContext(), profile, username);
            bottomSheet.show(getSupportFragmentManager(),bottomSheet.getTag());
        });

        followerBox.setOnClickListener(v -> {
            FollowersBottomSheet bottomSheet = new FollowersBottomSheet(getApplicationContext(), profile, username);
            bottomSheet.show(getSupportFragmentManager(),bottomSheet.getTag());
        });

        if(profile.equals(username)) {
            profilePic.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                    public void onLongPress(MotionEvent e) {
                        Timber.i("Longpress detected");

                        EditProfileBottomSheet bottomSheet = new EditProfileBottomSheet();
                        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });

            fullnameTxt.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                    public void onLongPress(MotionEvent e) {
                        Timber.i("Longpress detected");

                        EditFullnameBottomSheet bottomSheet = new EditFullnameBottomSheet();
                        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });

            bioTxt.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                    public void onLongPress(MotionEvent e) {
                        Timber.i("Longpress detected");

                        EditBioBottomSheet bottomSheet = new EditBioBottomSheet();
                        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void doFollow(String follower, String type, String followe) {
        // Tag used to cancel the request
        String tag_string_req = "req_follow";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_FOLLOW, response -> {
            Timber.tag(TAG).d("Follow Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Stop animation
                    stopButtonAnimation();

                    if(type.equals("follow")) {
                        followBtn.setText("Following");
                        followBtn.saveInitialState();
                    } else {
                        followBtn.setText("Follow");
                        followBtn.saveInitialState();
                    }

                    String sent = jObj.getString("sent");
                    Toasty.success(getApplicationContext(), sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(getApplicationContext(), "Mission Control, come in !", Toast.LENGTH_LONG).show();
                stopButtonAnimation();
            }

        }, error -> {
            Timber.tag(TAG).e("Follow Error: %s", error.getMessage());
            Toasty.error(getApplicationContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("follower", follower);
                params.put("type", type);
                params.put("followe", followe);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public static void loadProfileImage(String url, Context context) {

        //qweeksnap
        /*profilePicTarget = new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                profilePic.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        profilePic.setTag(profilePicTarget);
        // load User ProfileModel Picture
        Picasso.get().
                load(url)
                .resize(80, 80)
                .error(R.drawable.ic_alien)
                .centerCrop()
                .into(profilePicTarget);*/

        RequestOptions requestOptions = new RequestOptions() // because file name is always same
                .format(DecodeFormat.PREFER_RGB_565);
        Glide
                .with(context)
                .load(url)
                .override(100, 100)
                .placeholder(R.drawable.alien)
                .error(R.drawable.alien)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(requestOptions)
                .into(profilePic);
    }

    public static void loadProfileBio(String bio) {
        staticBioTxt.setText(bio);
    }

    private void loadProfile() {
        callProfileApi().enqueue(new Callback<ProfileModel>() {
            @SuppressLint({"SetTextI18n", "RestrictedApi"})
            @Override
            public void onResponse(@NotNull Call<ProfileModel> call, @NotNull Response<ProfileModel> response) {

                Timber.tag(TAG).i("onResponse: %s", (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                userItem = fetchProfile(response);
                user = userItem.get(0);

                //Profile Id
                profileId = user.getId();
                profileName = user.getFullname();

                        //qweeksnap
                /*profilePicTarget = new com.squareup.picasso.Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        profilePic.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                };

                profilePic.setTag(profilePicTarget);
                // load User ProfileModel Picture
                Picasso.get().
                        load(user.getAvatar())
                        .resize(100, 100)
                        .error(R.drawable.ic_alien)
                        .centerCrop()
                        .into(profilePicTarget);*/

                RequestOptions requestOptions = new RequestOptions() // because file name is always same
                        .format(DecodeFormat.PREFER_RGB_565);
                Drawable placeholder = getTinted(getResources().getColor(R.color.contentTextColor));
                Glide
                        .with(getApplicationContext())
                        .load(user.getAvatar())
                        .override(100, 100)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .thumbnail(0.3f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .apply(requestOptions)
                        .into(profilePic);

                fullnameTxt.setText(user.getFullname());
                usernameTxt.setText("q/"+ user.getUsername());

                bioTxt.setText(user.getBio());
                if(user.getBio().equals("") && (profile.equals(username))) {
                    if(user.getBio().isEmpty()) {
                        bioTxt.setText("Hold down to set your new Bio... and other things");
                    } else {
                        bioTxt.setText(user.getBio());
                    }
                } else {
                    bioTxt.setText(user.getBio());
                }


                dropCount.setText(user.getDrop_count());
                followingCount.setText(user.getFollowing_count());
                followersCount.setText(user.getFollowers_count());


                // Build Buttons
                if(user.getUsername().equals(username)) {
                    followBtn.setVisibility(View.GONE);
                    chatBtn.setVisibility(View.GONE);
                } else {
                    followBtn.setVisibility(View.VISIBLE);
                    chatBtn.setVisibility(View.VISIBLE);
                }

                followed = user.getFollowed();

                if(followed.equals("yes")) {
                    followBtn.setText("Following");
                } else {
                    followBtn.setText("Follow");
                }
            }

            @Override
            public void onFailure(@NotNull Call<ProfileModel> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private @Nullable
    Drawable getTinted(@ColorInt int color) {
        // need to mutate otherwise all references to this drawable will be tinted
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_alien).mutate();
        return tint(drawable, ColorStateList.valueOf(color));
    }

    public static Drawable tint(Drawable input, ColorStateList tint) {
        if (input == null) {
            return null;
        }
        Drawable wrappedDrawable = DrawableCompat.wrap(input);
        DrawableCompat.setTintList(wrappedDrawable, tint);
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.MULTIPLY);
        return wrappedDrawable;
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
                profile
        );
    }

    private void setupViewPager(ViewPager viewPager) {
        TabsAdapter adapter = new TabsAdapter(
                getSupportFragmentManager());
        adapter.addFragment(new ProfileFeed(getApplicationContext(), profile, username), "Drops");
        adapter.addFragment(new QweeksnapFeed(getApplicationContext(), profile, username), "QweekSnaps");
        adapter.addFragment(new LikedFeed(getApplicationContext(), profile, username), "Liked");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
    }

    public void onClick(View v) {
        super.onBackPressed(); // or super.finish();
    }

    public void onClickOptions(View v) {
        OptionsBottomSheet bottomSheet = new OptionsBottomSheet(getApplicationContext());
        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void stopButtonAnimation(){
        followBtn.revertAnimation();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        Glide.get(getApplicationContext()).clearMemory();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Glide.get(getApplicationContext()).clearMemory();
        super.onDestroy();
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
