package com.creator.qweekdots.utils;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.Nullable;

import com.creator.qweekdots.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public abstract class RoundedBottomSheetDialogFragment extends BottomSheetDialogFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    public abstract void onItemSelected(AdapterView<?> parent, View view, int position,
                                        long id);

    public abstract void onNothingSelected(AdapterView<?> arg0);
}
