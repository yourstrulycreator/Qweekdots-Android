package com.creator.qweekdots.utils;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.ColorInt;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

public class ToolbarColorizeHelper {
    public static void colouriseToolbar(AppBarLayout appBarLayout, @ColorInt int background, @ColorInt int foreground) {
        if (appBarLayout == null) return;

        appBarLayout.setBackgroundColor(background);

        final Toolbar toolbar = (Toolbar)appBarLayout.getChildAt(0);
        if (toolbar == null) return;

        toolbar.setTitleTextColor(foreground);
        toolbar.setSubtitleTextColor(foreground);

        final PorterDuffColorFilter colorFilter
                = new PorterDuffColorFilter(foreground, PorterDuff.Mode.MULTIPLY);

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            final View view = toolbar.getChildAt(i);

            //todo: cal icon?
            Log.d("ToolbarColorizeHelper", "view: "+i+" "+view.getClass().toString());

            //Back button or drawer open button
            if (view instanceof ImageButton) {
                ((ImageButton)view).getDrawable().setColorFilter(colorFilter);
            }

            if (view instanceof ActionMenuView) {
                for (int j = 0; j < ((ActionMenuView) view).getChildCount(); j++) {

                    final View innerView = ((ActionMenuView)view).getChildAt(j);

                    //Any ActionMenuViews - icons that are not back button, text or overflow menu
                    if (innerView instanceof ActionMenuItemView) {
                        Log.d("ToolbarColorizeHelper", "view (actionmenuitemviwe): "+i);

                        final Drawable[] drawables = ((ActionMenuItemView)innerView).getCompoundDrawables();
                        for (int k = 0; k < drawables.length; k++) {

                            final Drawable drawable = drawables[k];
                            if (drawable != null) {
                                final int drawableIndex = k;
                                //Set the color filter in separate thread
                                //by adding it to the message queue - won't work otherwise
                                innerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((ActionMenuItemView) innerView).getCompoundDrawables()[drawableIndex].setColorFilter(colorFilter);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }

        //Overflow icon
        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            overflowIcon.setColorFilter(colorFilter);
            toolbar.setOverflowIcon(overflowIcon);
        }
    }
}
