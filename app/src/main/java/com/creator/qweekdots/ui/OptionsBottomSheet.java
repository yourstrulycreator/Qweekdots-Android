package com.creator.qweekdots.ui;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ActivityThemeOptions;
import com.creator.qweekdots.activity.LoginActivity;
import com.creator.qweekdots.activity.MainActivity;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.activity.WebViewActivity;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static maes.tech.intentanim.CustomIntent.customType;

public class OptionsBottomSheet extends RoundedBottomSheetDialogFragment {
    private SQLiteHandler db;
    private SessionManager session;
    private View RootView;

    private final String TAG = OptionsBottomSheet.class.getSimpleName();

    private Context context;
    private String username;

    public OptionsBottomSheet(Context context) {
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RootView = inflater.inflate(R.layout.options_bottom_sheet, container, false);

        if(new DarkModePrefManager(requireActivity()).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // SqLite database handler
        db = new SQLiteHandler(requireActivity().getApplicationContext());
        // session manager
        session = new SessionManager(requireActivity().getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        username = user.get("username");
        String fullname = user.get("fullname");
        String email = user.get("email");
        String telephone = user.get("telephone");
        String birthday = user.get("birthday");

        // Account Info Setup
        TextView usernameText = RootView.findViewById(R.id.optUsernameTxt);
        TextView fullnameText = RootView.findViewById(R.id.optFullnameTxt);
        TextView birthdayText = RootView.findViewById(R.id.optBirthdayTxt);
        TextView telephoneText = RootView.findViewById(R.id.optTelephoneTxt);
        TextView emailText = RootView.findViewById(R.id.optEmailTxt);

        usernameText.setText("q/" + username);
        fullnameText.setText(fullname);
        emailText.setText(email);
        birthdayText.setText(birthday);
        telephoneText.setText(telephone);

        // Info Edit
        CardView usernameCard = RootView.findViewById(R.id.optUsernameCard);
        CardView fullnameCard = RootView.findViewById(R.id.optFullnameCard);
        CardView birthdayCard = RootView.findViewById(R.id.optBirthdayCard);
        CardView telephoneCard = RootView.findViewById(R.id.optTelephoneCard);
        CardView emailCard = RootView.findViewById(R.id.optEmailCard);
        CardView passwordCard = RootView.findViewById(R.id.optPasswordCard);
        CardView themeCard = RootView.findViewById(R.id.optThemeCard);

        // Card Option Sheets
        usernameCard.setOnClickListener(v -> {
            //EditUsernameBottomSheet bottomSheet = new EditUsernameBottomSheet();
            //bottomSheet.show(Objects.requireNonNull(getFragmentManager()),bottomSheet.getTag());
        });
        fullnameCard.setOnClickListener(v -> {
            EditFullnameBottomSheet bottomSheet = new EditFullnameBottomSheet();
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });
        birthdayCard.setOnClickListener(v -> {
            EditBirthdayBottomSheet bottomSheet = new EditBirthdayBottomSheet();
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });
        emailCard.setOnClickListener(v -> {
            EditEmailBottomSheet bottomSheet = new EditEmailBottomSheet();
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });
        telephoneCard.setOnClickListener(v -> {
            EditMobileBottomSheet bottomSheet = new EditMobileBottomSheet();
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });
        passwordCard.setOnClickListener(v -> {
            EditPasswordBottomSheet bottomSheet = new EditPasswordBottomSheet();
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });

        CardView optFeedback = RootView.findViewById(R.id.optFeedbackCard);

        optFeedback.setOnClickListener(v-> {
            FeedbackBottomSheet bottomSheet = new FeedbackBottomSheet(getContext(), username);
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });

        CardView optTermsCard = RootView.findViewById(R.id.optTermsCard);
        CardView optPrivacyCard = RootView.findViewById(R.id.optPrivacyPolicyCard);
        CardView optAboutCard = RootView.findViewById(R.id.optAboutCard);

        optTermsCard.setOnClickListener(v->{
            Intent i = new Intent(getActivity(), WebViewActivity.class);
            i.putExtra("url", "https://qweek.fun/genjitsu/terms");
            startActivity(i);
        });

        optPrivacyCard.setOnClickListener(v-> {
            Intent i = new Intent(getActivity(), WebViewActivity.class);
            i.putExtra("url", "https://qweek.fun/genjitsu/privacy");
            startActivity(i);
        });

        optAboutCard.setOnClickListener(V-> {
            Toasty.info(context, "Qweekdots v. alpha", Toasty.LENGTH_LONG).show();
        });

        themeCard.setOnClickListener(v-> {
            Intent i = new Intent(getActivity(), ActivityThemeOptions.class);
            startActivity(i);
            requireActivity().recreate();
            customType(getActivity(), "fadein-to-fadeout");
            dismissAllowingStateLoss();
        });




        // Logout button click event
        CardView btnLogout = RootView.findViewById(R.id.logoutBtn);
        btnLogout.setOnClickListener(v -> logoutUser());

        return RootView;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        //inflating layout
        RootView = View.inflate(context, R.layout.options_bottom_sheet, null);

        View extraSpace = RootView.findViewById(R.id.extraSpace);

        //setting layout with bottom sheet
        bottomSheet.setContentView(RootView);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) (RootView.getParent()));


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

        return bottomSheet;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
