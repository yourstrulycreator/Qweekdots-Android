package com.creator.qweekdots.widget;


import androidx.annotation.NonNull;

public interface IFetchGifDimension {

    void onReceiveViewHolderDimension(@NonNull String id, int width, int height, int orientation);
}
