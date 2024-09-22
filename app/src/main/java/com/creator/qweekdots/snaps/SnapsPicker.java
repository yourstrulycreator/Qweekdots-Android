package com.creator.qweekdots.snaps;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.creator.qweekdots.snaps.gallery.SelectActivity;

import java.util.List;


public class SnapsPicker {

    private Activity activity;
    private SnapsPickerListener listener;
    private MultiListener mListener;
    private SingleListener sListener;
    public static int x;
    public static int y;
    public static boolean multiSelect;
    public static int numberOfPictures;

    public static List<String> addresses;

    public SnapsPicker(Activity activity) {
        this.activity = activity;
    }

    /**
     * This method won't work anymore please use one of the bellow for single selection or multi selection
     *
     * @deprecated use {@link #show(int, int, SingleListener)} ()}
     * or {@link #show(int, int, int, MultiListener)} instead.
     */
    @Deprecated
    public void show(int CropXRatio, int CropYRatio, boolean multiSelect, SnapsPickerListener listener) {
        SnapsPicker.x = CropXRatio;
        SnapsPicker.y = CropYRatio;
        SnapsPicker.multiSelect = multiSelect;
        this.listener = listener;
        Intent in = new Intent(activity, SelectActivity.class);
        activity.startActivity(in);
        activity.registerReceiver(br, new IntentFilter("refreshPlease"));
    }

    public void show(int CropXRatio, int CropYRatio, SingleListener listener) {
        SnapsPicker.x = CropXRatio;
        SnapsPicker.y = CropYRatio;
        SnapsPicker.multiSelect = false;
        this.sListener = listener;
        Intent in = new Intent(activity, SelectActivity.class);
        activity.startActivity(in);
        activity.registerReceiver(br, new IntentFilter("refreshPlease"));
    }

    public void show(int CropXRatio, int CropYRatio, int numberOfPictures, MultiListener listener) {
        if (numberOfPictures < 2)
            numberOfPictures = 2;
        else if (numberOfPictures > 1000)
            numberOfPictures = 1000;

        SnapsPicker.x = CropXRatio;
        SnapsPicker.y = CropYRatio;
        SnapsPicker.multiSelect = true;
        SnapsPicker.numberOfPictures = numberOfPictures;

        this.mListener = listener;
        Intent in = new Intent(activity, SelectActivity.class);
        activity.startActivity(in);
        activity.registerReceiver(br, new IntentFilter("refreshPlease"));

    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (listener != null)
                listener.selectedPics(addresses);
            else {
                if (multiSelect)
                    mListener.selectedPics(addresses);
                else
                    sListener.selectedPic(addresses.get(0));
            }
        }
    };

    @Override
    protected void finalize() throws Throwable {
        activity.unregisterReceiver(br);
        super.finalize();
    }
}
