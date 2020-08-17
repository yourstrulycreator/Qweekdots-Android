package com.creator.qweekdots.sdk.util;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public abstract class AbstractColorUtils {

    /**
     * Retrieve color from res id
     *
     * @param context    the context
     * @param colorResId res id of the color value
     * @return A single color value in the form 0xAARRGGBB, or, the colorResId if context is null
     */
    @ColorInt
    public static int getColor(@NonNull Context context, @ColorRes int colorResId) {
        return ContextCompat.getColor(context, colorResId);
    }
}
