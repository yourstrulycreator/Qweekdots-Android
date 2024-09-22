package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.creator.qweekdots.BuildConfig;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ActivityThemeOptions;
import com.creator.qweekdots.activity.LoginActivity;
import com.creator.qweekdots.activity.WebViewActivity;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.helper.SessionManager;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static maes.tech.intentanim.CustomIntent.customType;

public class OptionsBottomSheet extends RoundedBottomSheetDialogFragment {
    private SQLiteHandler db;
    private SessionManager session;
    private View RootView;
    //private final String TAG = OptionsBottomSheet.class.getSimpleName();
    private Context context;
    private ReviewManager reviewManager;

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

        String username = user.get("username");
        String fullname = user.get("fullname");
        String bio = user.get("bio");
        String email = user.get("email");
        String telephone = user.get("telephone");
        String birthday = user.get("birthday");

        // Account Info Setup
        TextView usernameText = RootView.findViewById(R.id.optUsernameTxt);
        TextView fullnameText = RootView.findViewById(R.id.optFullnameTxt);
        TextView bioText = RootView.findViewById(R.id.optBioTxt);
        TextView birthdayText = RootView.findViewById(R.id.optBirthdayTxt);
        TextView telephoneText = RootView.findViewById(R.id.optTelephoneTxt);
        TextView emailText = RootView.findViewById(R.id.optEmailTxt);

        usernameText.setText("q/" + username);
        fullnameText.setText(fullname);
        bioText.setText(bio);
        emailText.setText(email);
        birthdayText.setText(birthday);
        telephoneText.setText(telephone);

        // Init Options Button Card
        CardView fullnameCard = RootView.findViewById(R.id.optFullnameCard);
        CardView bioCard = RootView.findViewById(R.id.optBioCard);
        CardView birthdayCard = RootView.findViewById(R.id.optBirthdayCard);
        CardView telephoneCard = RootView.findViewById(R.id.optTelephoneCard);
        CardView emailCard = RootView.findViewById(R.id.optEmailCard);
        CardView passwordCard = RootView.findViewById(R.id.optPasswordCard);
        CardView themeCard = RootView.findViewById(R.id.optThemeCard);
        CardView optTermsCard = RootView.findViewById(R.id.optTermsCard);
        CardView optPrivacyCard = RootView.findViewById(R.id.optPrivacyPolicyCard);
        CardView optAboutCard = RootView.findViewById(R.id.optAboutCard);
        CardView optOpenSrcCard = RootView.findViewById(R.id.optOpenSrcCard);
        CardView optShareCard = RootView.findViewById(R.id.optShareCard);

        // Card Option Sheets
        fullnameCard.setOnClickListener(v -> {
            EditFullnameBottomSheet bottomSheet = new EditFullnameBottomSheet();
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });
        bioCard.setOnClickListener(v-> {
            EditBioBottomSheet bottomSheet = new EditBioBottomSheet();
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
        CardView optRate = RootView.findViewById(R.id.optRateCard);

        optFeedback.setOnClickListener(v-> {
            FeedbackBottomSheet bottomSheet = new FeedbackBottomSheet(getContext());
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });

        reviewManager = ReviewManagerFactory.create(context);
        optRate.setOnClickListener(v-> {
           showRateApp();
        });

        // Terms button click event
        optTermsCard.setOnClickListener(v->{
            Intent i = new Intent(getActivity(), WebViewActivity.class);
            i.putExtra("url", "https://qweek.fun/qweekdots/terms");
            startActivity(i);
        });

        // Privacy button click event
        optPrivacyCard.setOnClickListener(v-> {
            Intent i = new Intent(getActivity(), WebViewActivity.class);
            i.putExtra("url", "https://qweek.fun/qweekdots/privacy");
            startActivity(i);
        });

        // Open Source click event
        optOpenSrcCard.setOnClickListener(v-> {
            startActivity(new Intent(requireContext(), OssLicensesMenuActivity.class));
        });

        // About button click event
        optAboutCard.setOnClickListener(v->{
            AboutBottomSheet bottomSheet = new AboutBottomSheet();
            bottomSheet.show(requireFragmentManager(),bottomSheet.getTag());
        });

        optShareCard.setOnClickListener(v->{
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out Qweekdots 😀");
                String shareMessage= "\nHey 👋, Join Qweekdots today 👍, Chat 😉, Bring your friends along 👽. It's Free! 🤩 \n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=com.creator.qweekdots" + BuildConfig.APPLICATION_ID +"\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                context.startActivity(Intent.createChooser(shareIntent, "Choose One"));
            } catch(Exception e) {
                //e.toString();
            }
        });

        // Theme button click event
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
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss();
                }
            }
            @Override
            public void onSlide(@NonNull View view, float v) {}
        });
        return bottomSheet;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Shows rate app bottom sheet using In-App review API
     * The bottom sheet might or might not shown depending on the Quotas and limitations
     * https://developer.android.com/guide/playcore/in-app-review#quotas
     * We show fallback dialog if there is any error
     */
    public void showRateApp() {
        Task<ReviewInfo> request = reviewManager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();

                Task<Void> flow = reviewManager.launchReviewFlow(requireActivity(), reviewInfo);
                flow.addOnCompleteListener(task1 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            } else {
                // There was some problem, continue regardless of the result.
                // show native rate app dialog on error
                showRateAppFallbackDialog();
            }
        });
    }

    /**
     * Showing native dialog with three buttons to review the app
     * Redirect user to playstore to review the app
     */
    private void showRateAppFallbackDialog() {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.rate_app_title)
                .setMessage(R.string.rate_app_message)
                .setPositiveButton(R.string.rate_btn_pos, (dialog, which) -> redirectToPlayStore())
                .setNegativeButton(R.string.rate_btn_neg,
                        (dialog, which) -> {
                            // take action when pressed not now
                        })
                .setNeutralButton(R.string.rate_btn_nut,
                        (dialog, which) -> {
                            // take action when pressed remind me later
                        })
                .setOnDismissListener(dialog -> {
                })
                .show();
    }

    // redirecting user to PlayStore
    private void redirectToPlayStore() {
        final String appPackageName = context.getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException exception) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
