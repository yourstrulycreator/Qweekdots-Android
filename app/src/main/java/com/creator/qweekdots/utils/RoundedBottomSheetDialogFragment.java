package com.creator.qweekdots.utils;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.creator.qweekdots.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RoundedBottomSheetDialogFragment extends BottomSheetDialogFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }
}
