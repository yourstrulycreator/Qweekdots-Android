package com.creator.qweekdots.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.creator.qweekdots.R;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

public class DropTextBottomSheet extends RoundedBottomSheetDialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        //inflating layout
        View view = View.inflate(getContext(), R.layout.drop_text_bottom_sheet, null);



        return bottomSheet;
    }
}
