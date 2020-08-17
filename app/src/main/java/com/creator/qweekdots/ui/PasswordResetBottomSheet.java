package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.creator.qweekdots.R;
import com.creator.qweekdots.api.passRequestInterface;
import com.creator.qweekdots.app.passwordConstants;
import com.creator.qweekdots.models.passServerRequest;
import com.creator.qweekdots.models.passServerResponse;
import com.creator.qweekdots.models.passUser;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class PasswordResetBottomSheet extends RoundedBottomSheetDialogFragment {
    //private final String TAG = EditMobileBottomSheet.class.getSimpleName();

    private BottomSheetBehavior bottomSheetBehavior;

    private AppCompatButton btn_reset;
    private EditText et_email,et_code,et_password;
    private TextView tv_timer;
    private ProgressBar progress;
    private boolean isResetInitiated = false;
    private String email;
    private CountDownTimer countDownTimer;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.password_reset_bottom_sheet, container, false);

        btn_reset = view.findViewById(R.id.btn_reset);
        tv_timer = view.findViewById(R.id.timer);
        et_code = view.findViewById(R.id.et_code);
        et_email = view.findViewById(R.id.et_email);
        et_password = view.findViewById(R.id.et_password);
        et_password.setVisibility(View.GONE);
        et_code.setVisibility(View.GONE);
        tv_timer.setVisibility(View.GONE);
        btn_reset.setOnClickListener(v-> passReset());
        progress = view.findViewById(R.id.progress);

        return view;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        //inflating layout
        view = View.inflate(getContext(), R.layout.password_reset_bottom_sheet, null);

        View extraSpace = view.findViewById(R.id.extraSpace);

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
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss();
                }
            }
            @Override
            public void onSlide(@NonNull View view, float v) {}
        });
        return bottomSheet;
    }

    private void passReset() {
        if(!isResetInitiated) {
            email = et_email.getText().toString();
            if (!email.isEmpty()) {
                progress.setVisibility(View.VISIBLE);
                initiateResetPasswordProcess(email);
            } else {
                Snackbar.make(requireView(), "Fields are empty !", Snackbar.LENGTH_LONG).show();
            }
        } else {
            String code = et_code.getText().toString();
            String password = et_password.getText().toString();
            if(!code.isEmpty() && !password.isEmpty()) {
                finishResetPasswordProcess(email,code,password);
            } else {
                Snackbar.make(requireView(), "Fields are empty !", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void initiateResetPasswordProcess(String email){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(passwordConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        passRequestInterface requestInterface = retrofit.create(passRequestInterface.class);

        passUser user = new passUser();
        user.setEmail(email);
        passServerRequest request = new passServerRequest();
        request.setOperation(passwordConstants.RESET_PASSWORD_INITIATE);
        request.setUser(user);
        Call<passServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<passServerResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call<passServerResponse> call, @NotNull retrofit2.Response<passServerResponse> response) {
                passServerResponse resp = response.body();
                assert resp != null;
                Snackbar.make(requireView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();

                if(resp.getResult().equals(passwordConstants.SUCCESS)) {
                    Snackbar.make(requireView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                    et_email.setVisibility(View.GONE);
                    et_code.setVisibility(View.VISIBLE);
                    et_password.setVisibility(View.VISIBLE);
                    tv_timer.setVisibility(View.VISIBLE);
                    btn_reset.setText("Change Password");
                    isResetInitiated = true;
                    startCountdownTimer();
                } else {
                    Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                }
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(@NotNull Call<passServerResponse> call, @NotNull Throwable t) {
                progress.setVisibility(View.INVISIBLE);
                Timber.tag(passwordConstants.TAG).d("failed");
                Snackbar.make(requireView(), Objects.requireNonNull(t.getLocalizedMessage()), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void finishResetPasswordProcess(String email,String code, String password){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(passwordConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        passRequestInterface requestInterface = retrofit.create(passRequestInterface.class);

        passUser user = new passUser();
        user.setEmail(email);
        user.setCode(code);
        user.setPassword(password);
        passServerRequest request = new passServerRequest();
        request.setOperation(passwordConstants.RESET_PASSWORD_FINISH);
        request.setUser(user);
        Call<passServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<passServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<passServerResponse> call, @NotNull retrofit2.Response<passServerResponse> response) {
                passServerResponse resp = response.body();
                assert resp != null;
                Snackbar.make(requireView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();

                if(resp.getResult().equals(passwordConstants.SUCCESS)){
                    Snackbar.make(requireView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                    countDownTimer.cancel();
                    isResetInitiated = false;
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                } else {
                    Snackbar.make(requireView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                }
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(@NotNull Call<passServerResponse> call, @NotNull Throwable t) {
                progress.setVisibility(View.INVISIBLE);
                Timber.tag(passwordConstants.TAG).d("failed");
                Snackbar.make(requireView(), Objects.requireNonNull(t.getLocalizedMessage()), Snackbar.LENGTH_LONG).show();

            }
        });
    }

    private void startCountdownTimer(){
        countDownTimer = new CountDownTimer(300000, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                tv_timer.setText("Time remaining : " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                Snackbar.make(requireView(), "Time Out ! Request again to reset password.", Snackbar.LENGTH_LONG).show();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }.start();
    }


    @Override
    public void onStart() {
        super.onStart();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

}
