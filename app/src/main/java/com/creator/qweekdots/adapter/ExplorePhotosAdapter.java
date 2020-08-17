package com.creator.qweekdots.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.creator.qweekdots.R;
import com.creator.qweekdots.models.PhotoItem;
import com.creator.qweekdots.ui.DropBottomSheet;
import com.creator.qweekdots.view.StaggeredGridViewItem;

import java.util.Objects;

public class ExplorePhotosAdapter extends StaggeredGridViewItem {
    // View Types
    //private static final String TAG = ExplorePhotosAdapter.class.getSimpleName();
    private Context context;
    private PhotoItem photo;
    private View mView;
    private String username;

    public ExplorePhotosAdapter(Context context, PhotoItem photo, String username) {
        this.context = context;
        this.photo = photo;
        this.username = username;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(LayoutInflater inflater, ViewGroup parent) {
        // TODO Auto-generated method stub
        mView = inflater.inflate(R.layout.explore_photos, null);

        if(context!=null) {
            ImageView image = mView.findViewById(R.id.image);

            RequestOptions requestOptions = new RequestOptions() // because file name is always same
                    .format(DecodeFormat.PREFER_RGB_565);
            Glide
                    .with(context)
                    .load(photo.getQweekSnap())
                    .override(Target.SIZE_ORIGINAL)
                    .thumbnail(0.3f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(requestOptions)
                    .into(image);

            mView.setOnClickListener(v -> {
                DropBottomSheet bottomSheet = new DropBottomSheet(context, username, photo.getDrop_Id());
                FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
            });
        }

        return mView;
    }

    @Override
    public int getViewHeight(LayoutInflater inflater, ViewGroup parent) {
        CardView item_containerFrameLayout = mView.findViewById(R.id.container);
        item_containerFrameLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return item_containerFrameLayout.getMeasuredHeight();
    }
}
