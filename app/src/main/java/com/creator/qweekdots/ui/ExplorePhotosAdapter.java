package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.creator.qweekdots.R;
import com.creator.qweekdots.models.PhotoItem;
import com.creator.qweekdots.view.StaggeredGridViewItem;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ExplorePhotosAdapter extends StaggeredGridViewItem {
    // View Types
    private static final String TAG = ExplorePhotosAdapter.class.getSimpleName();

    private static final String BASE_URL_MEDIA = "http://10.0.2.2:8080/qweekdots/media/";
    private static final String BASE_URL_USER_PIC = "http://10.0.2.2:8080/qweekdots/profiles/";
    private Context context;

    private PhotoItem photo;
    private View mView;
    private int mHeight;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private String errorMsg;
    private String username;

    ExplorePhotosAdapter(Context context, PhotoItem photo, String username) {
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

            if (photo.getQweekSnap().isEmpty()) {
                image.setImageResource(R.drawable.space_hotel);
            } else{
                /*
                com.squareup.picasso.Target target = new com.squareup.picasso.Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        image.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        image.requestLayout();
                        image.getLayoutParams().height = 600;
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                };

                image.setTag(target);
                Picasso.get().load(photo.getQweekSnap()).into(target);
                *
                 */

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                        .skipMemoryCache(true);
                Glide
                        .with(context)
                        .load(photo.getQweekSnap())
                        .override(Target.SIZE_ORIGINAL)
                        .apply(requestOptions)
                        .into(image);
            }

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
        mHeight = item_containerFrameLayout.getMeasuredHeight();
        return mHeight;
    }
}
