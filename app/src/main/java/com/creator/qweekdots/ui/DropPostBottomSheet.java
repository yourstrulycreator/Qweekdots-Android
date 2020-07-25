package com.creator.qweekdots.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import com.creator.qweekdots.R;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

public class DropPostBottomSheet extends RoundedBottomSheetDialogFragment {
    private final String TAG = DropPostBottomSheet.class.getSimpleName();

    private Context context;
    private BottomSheetBehavior bottomSheetBehavior;

    public DropPostBottomSheet(Context context) {
        this.context = context;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        //inflating layout
        View view = View.inflate(getContext(), R.layout.drop_post_bottom_sheet, null);

        View extraSpace = view.findViewById(R.id.extraSpace);

        ImageView postText = view.findViewById(R.id.postText);
        ImageView postSnap = view.findViewById(R.id.postSnap);
        ImageView postAudio = view.findViewById(R.id.postAudio);

        postText.setOnClickListener(v-> {
            assert getFragmentManager() != null;
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.postFrame, new DropText(), "Drop Text");
            ft.commit();
            ft.addToBackStack(null);
            bottomSheet.dismiss();
        });

        postSnap.setOnClickListener(v-> {
            assert getFragmentManager() != null;
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.postFrame, new DropQweekSnap(), "Drop QweekSnap");
            ft.commit();
            ft.addToBackStack(null);
            bottomSheet.dismiss();
        });


        postAudio.setOnClickListener(v-> {
            assert getFragmentManager() != null;
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.postFrame, new DropAudio(), "Drop Audio");
            ft.commit();
            ft.addToBackStack(null);
            bottomSheet.dismiss();
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

        return bottomSheet;
    }

    @Override
    public void onStart() {
        super.onStart();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}
