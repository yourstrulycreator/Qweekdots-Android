package com.creator.qweekdots.adapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.SpaceActivity;
import com.creator.qweekdots.grid.util.DynamicHeightTextView;
import com.creator.qweekdots.models.ChatRoom;
import com.creator.qweekdots.view.StaggeredGridViewItem;


import java.util.ArrayList;
import java.util.Random;

import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;


public class SpacesAdapter extends StaggeredGridViewItem {
    private static final String TAG = SpacesAdapter.class.getSimpleName();
    private Context context;

    private ChatRoom ring;
    private View mView;
    private final int position;

    private final Random mRandom;
    private final ArrayList<Integer> mBackgroundColors;
    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<>();

    public SpacesAdapter(Context context, ChatRoom ring, final int position) {
        this.context = context;
        this.ring = ring;
        this.position = position;

        mRandom = new Random();
        mBackgroundColors = new ArrayList<>();
        mBackgroundColors.add(R.color.DodgerBlue);
        mBackgroundColors.add(R.color.Tomato);
        mBackgroundColors.add(R.color.Coral);
        mBackgroundColors.add(R.color.SteelBlue);
        mBackgroundColors.add(R.color.DarkSlateBlue);
        mBackgroundColors.add(R.color.DodgerBlue);
        mBackgroundColors.add(R.color.DarkSlateGray);
        mBackgroundColors.add(R.color.ArgentinanBlue);
        mBackgroundColors.add(R.color.DeepPurple);
        mBackgroundColors.add(R.color.MediumSlateBlue);
        mBackgroundColors.add(R.color.VioletBlue);
        mBackgroundColors.add(R.color.SeaGreen);
        mBackgroundColors.add(R.color.CornflowerBlue);
        mBackgroundColors.add(R.color.DeepSkyBlue);
        mBackgroundColors.add(R.color.DarkTurquoise);
        mBackgroundColors.add(R.color.BottleGreen);
        mBackgroundColors.add(R.color.DarkCyan);
        mBackgroundColors.add(R.color.GoGreen);
        mBackgroundColors.add(R.color.JungleGreen);
        mBackgroundColors.add(R.color.Raspberry);
        mBackgroundColors.add(R.color.Folly);
        mBackgroundColors.add(R.color.WarriorsBlue);
        mBackgroundColors.add(R.color.SpaceCadet);
        mBackgroundColors.add(R.color.MajorelleBlue);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(LayoutInflater inflater, ViewGroup parent) {
        // TODO Auto-generated method stub
        mView = inflater.inflate(R.layout.rings_item, null);
        if(context != null) {
            CardView ringCard = mView.findViewById(R.id.ring_content);
            DynamicHeightTextView txtLineOne = mView.findViewById(R.id.ringnameText);
            ImageView art = mView.findViewById(R.id.art);

            double positionHeight = getPositionRatio(position);
            //int backgroundIndex = position >= mBackgroundColors.size() ?
            //        position % mBackgroundColors.size() : position;

            int randomColor = mBackgroundColors.get(mRandom.nextInt(20));
            //int indexColor = mBackgroundColors.get(backgroundIndex);
            //mView.setBackgroundResource(mBackgroundColors.get(backgroundIndex));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ringCard.setCardBackgroundColor(context.getColor(randomColor));
            }

            Timber.tag(TAG).d("getView position:" + position + " h:" + positionHeight);

            // Set the Dynamic Text and Height
            txtLineOne.setHeightRatio(positionHeight);
            txtLineOne.setText(ring.getName());

            // Set Art GIF
            Glide
                    .with(context)
                    .asGif()
                    .load(ring.getSpace_art())
                    .override(Target.SIZE_ORIGINAL)
                    .thumbnail(0.3f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(art);

            // View Click Listener
            mView.setOnClickListener(v -> {
                ObjectAnimator animY = ObjectAnimator.ofFloat(mView, "translationY", -100f, 0f);
                animY.setDuration(1000);//1sec
                animY.setInterpolator(new BounceInterpolator());
                animY.setRepeatCount(1);
                animY.start();
                Intent i = new Intent(context, SpaceActivity.class);
                i.putExtra("chat_room_id", ring.getId());
                i.putExtra("name", ring.getName());
                context.startActivity(i);
                customType(context, "bottom-to-up");
            });
        }

        return mView;
    }

    @Override
    public int getViewHeight(LayoutInflater inflater, ViewGroup parent) {
        CardView item_containerFrameLayout = mView.findViewById(R.id.ring_content);
        item_containerFrameLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        return item_containerFrameLayout.getMeasuredHeight();
    }

    private double getPositionRatio(final int position) {
        double ratio = sPositionHeightRatios.get(position, 0.0);
        // if not yet done generate and stash the columns height
        // in our real world scenario this will be determined by
        // some match based on the known height and width of the image
        // and maybe a helpful way to get the column height!
        if (ratio == 0) {
            ratio = getRandomHeightRatio();
            sPositionHeightRatios.append(position, ratio);
            Timber.tag(TAG).d("getPositionRatio:" + position + " ratio:" + ratio);
        }
        return ratio;
    }

    private double getRandomHeightRatio() {
        return (mRandom.nextDouble() / 2.0) + 0.5; //
    }

}
