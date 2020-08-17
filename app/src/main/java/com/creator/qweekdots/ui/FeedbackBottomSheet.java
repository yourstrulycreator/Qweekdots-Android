package com.creator.qweekdots.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.creator.qweekdots.R;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class FeedbackBottomSheet extends RoundedBottomSheetDialogFragment {
    private final String TAG = SearchBottomSheet.class.getSimpleName();
    private View view;
    private EditText optFeedbackTxt;
    private Context context;

    FeedbackBottomSheet(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.feedback_bottom_sheet, container, false);
        if(context!=null) {
            optFeedbackTxt = view.findViewById(R.id.optFeedbackSheetTxt);
            CircularProgressButton sendBtn = view.findViewById(R.id.optSendFeedbackButton);
            sendBtn.setOnClickListener(v-> {
                if(optFeedbackTxt.getText() != null) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"theqweekcompany@gmail.com"});
                    i.putExtra(Intent.EXTRA_SUBJECT, "Feedback/I Have A Suggestion/Bug Report");
                    i.putExtra(Intent.EXTRA_TEXT   , optFeedbackTxt.getText());
                    try {
                        startActivity(Intent.createChooser(i, "Send mail..."));
                        Timber.tag(TAG).d("Sending feedback, report");
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toasty.info(requireActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toasty.info(context, "Not going to say anything ?", Toasty.LENGTH_SHORT).show();
                }
            });
        }
        return view;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        //inflating layout
        view = View.inflate(context, R.layout.feedback_bottom_sheet, null);
        View extraSpace = view.findViewById(R.id.extraSpace);

        //setting layout with bottom sheet
        bottomSheet.setContentView(view);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
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
}
