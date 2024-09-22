package com.creator.qweekdots.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.models.NotificationItem;
import com.creator.qweekdots.ui.DropBottomSheet;
import com.creator.qweekdots.utils.PaginationAdapterCallback;
import com.gauravk.audiovisualizer.visualizer.BlobVisualizer;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.card.MaterialCardView;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.ponnamkarthik.richlinkpreview.RichLinkView;
import io.github.ponnamkarthik.richlinkpreview.ViewListener;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // View Types
    private static final int NOTIFICATIONS= 0;
    private static final int LOADING = 1;

    private List<NotificationItem> notificationItems;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;
    private String username;

    private boolean isPlaying = false;
    private MediaPlayer player;

    public NotificationAdapter(Context context, Fragment f, String username) {
        this.context = context;
        this.username = username;
        this.mCallback = (PaginationAdapterCallback) f;
        notificationItems = new ArrayList<>();
    }

    public List<NotificationItem> getQweekFeed() {
        return notificationItems;
    }

    public void setNewsFeed(List<NotificationItem> notificationItems) {
        this.notificationItems = notificationItems;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EmojiManager.install(new IosEmojiProvider());
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case NOTIFICATIONS:
                View viewItem = inflater.inflate(R.layout.notification_item, parent, false);
                viewHolder = new NotificationAdapter.NotificationVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new NotificationAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(context!=null) {
            NotificationItem notificationItem = notificationItems.get(position);//TimeAgo timestamp

            switch (getItemViewType(position)) {

                case NOTIFICATIONS:
                    final NotificationAdapter.NotificationVH notificationVH = (NotificationAdapter.NotificationVH) holder;

                    notificationVH.status.setVisibility(View.VISIBLE);
                    notificationVH.status.setText(notificationItem.getStatus());

                    // load User ProfileModel Picture
                    /*Picasso.get().load(notificationItem.getAvatar())
                            .resize(60, 60)
                            .placeholder(R.drawable.ic_alien)
                            .error(R.drawable.ic_alien)
                            .into(notificationVH.profilePic);*/

                    RequestOptions requestOptions = new RequestOptions() // because file name is always same
                            .format(DecodeFormat.PREFER_RGB_565);
                    Drawable placeholder = getTinted(context.getResources().getColor(R.color.contentTextColor));
                    Glide
                            .with(context)
                            .load(notificationItem.getAvatar())
                            .override(40, 40)
                            .placeholder(placeholder)
                            .error(placeholder)
                            .thumbnail(0.3f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .apply(requestOptions)
                            .into(notificationVH.profilePic);

                    notificationVH.profilePic.setOnClickListener(v -> {
                        Intent i = new Intent(context, ProfileActivity.class);
                        i.putExtra("profile", notificationItem.getUsername());
                        context.startActivity(i);
                        customType(context, "fadein-to-fadeout");
                    });

                    if(notificationItem.getNotificationType().equals("like")) {
                        notificationVH.noteLayout.setClickable(true);
                        notificationVH.noteLayout.setOnClickListener(v->{
                            DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                            FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                            bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                        });

                        notificationVH.noteContentCard.setVisibility(View.VISIBLE);

                        String dropType = notificationItem.getDropType();
                        if(dropType != null) {
                            switch (notificationItem.getDropType()) {
                                case "drop":
                                    notificationVH.txtDrop.setVisibility(View.VISIBLE);
                                    notificationVH.txtDrop2.setVisibility(View.GONE);
                                    notificationVH.qweeksnap.setVisibility(View.GONE);
                                    notificationVH.reactionImage.setVisibility(View.GONE);
                                    notificationVH.audioLayout.setVisibility(View.GONE);
                                    notificationVH.exoPlayerView.setVisibility(View.GONE);


                                    if (!TextUtils.isEmpty(notificationItem.getDrop())) {
                                        SpannableString hashText = new SpannableString(notificationItem.getDrop());
                                        Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                                        while (matcher.find()) {
                                            hashText.setSpan(
                                                    new ForegroundColorSpan(context.getResources().getColor(R.color.QweekThemeColor)),
                                                    matcher.start(),
                                                    matcher.end(),
                                                    0);
                                        }
                                        //if none set text and make it visible
                                        notificationVH.txtDrop.setText(hashText);
                                        notificationVH.txtDrop.setVisibility(View.VISIBLE);
                                    } else {
                                        // status is empty, remove from view
                                        notificationVH.txtDrop.setVisibility(View.GONE);
                                    }

                                    // Checking for null feed url
                                    if (notificationItem.getHasLink() == 1) {
                                        notificationVH.url.setLink(notificationItem.getLink(), new ViewListener() {
                                            @Override
                                            public void onSuccess(boolean status) {

                                            }

                                            @Override
                                            public void onError(Exception e) {

                                            }
                                        });
                                        notificationVH.url.setVisibility(View.VISIBLE);
                                    } else {
                                        // url is null, remove from the view
                                        notificationVH.url.setVisibility(View.GONE);
                                    }
                                    break;
                                case "qweekpic":
                                    notificationVH.txtDrop.setVisibility(View.GONE);
                                    notificationVH.txtDrop2.setVisibility(View.GONE);
                                    notificationVH.qweeksnap.setVisibility(View.VISIBLE);
                                    notificationVH.reactionImage.setVisibility(View.GONE);
                                    notificationVH.audioLayout.setVisibility(View.GONE);
                                    notificationVH.exoPlayerView.setVisibility(View.GONE);

                                    RequestOptions requestOptions2 = new RequestOptions() // because file name is always same
                                            .format(DecodeFormat.PREFER_RGB_565);
                                    Glide
                                            .with(context)
                                            .load(notificationItem.getQweekSnap())
                                            //.override(Target.SIZE_ORIGINAL)
                                            .thumbnail(0.3f)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .apply(requestOptions2)
                                            .into(notificationVH.qweeksnap);

                                    /*
                                    List<String> images = Collections.singletonList(notificationItem.getQweekSnap());
                                    notificationVH.qweeksnap.setOnClickListener(v -> new StfalconImageViewer.Builder<>(context, images, (imageView, imageUrl) -> Glide.with(context).load(imageUrl).into(imageView))
                                            .withTransitionFrom(notificationVH.qweeksnap)
                                            .withBackgroundColor(context.getResources().getColor(R.color.black))
                                            .show());

                                     */
                                    notificationVH.qweeksnap.setOnClickListener(v-> {
                                        DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                                        FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                                        bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                                    });

                                    break;
                                case "qweekvid":
                                    notificationVH.txtDrop.setVisibility(View.GONE);
                                    notificationVH.txtDrop2.setVisibility(View.GONE);
                                    notificationVH.qweeksnap.setVisibility(View.VISIBLE);
                                    notificationVH.reactionImage.setVisibility(View.GONE);
                                    notificationVH.audioLayout.setVisibility(View.GONE);

                                    RequestOptions requestOptions3 = new RequestOptions() // because file name is always same
                                            .format(DecodeFormat.PREFER_RGB_565);
                                    Glide
                                            .with(context)
                                            .load(notificationItem.getQweekSnap())
                                            //.override(Target.SIZE_ORIGINAL)
                                            .thumbnail(0.3f)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .apply(requestOptions3)
                                            .into(notificationVH.qweeksnap);

                                    /*
                                    List<String> images3 = Collections.singletonList(notificationItem.getQweekSnap());
                                    notificationVH.qweeksnap.setOnClickListener(v -> new StfalconImageViewer.Builder<>(context, images3, (imageView, imageUrl) -> Glide.with(context).load(imageUrl).into(imageView))
                                            .withTransitionFrom(notificationVH.qweeksnap)
                                            .withBackgroundColor(context.getResources().getColor(R.color.black))
                                            .show());

                                     */
                                    notificationVH.qweeksnap.setOnClickListener(v-> {
                                        DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                                        FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                                        bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                                    });

                                    break;
                                case "reaction":
                                    notificationVH.txtDrop.setVisibility(View.GONE);
                                    notificationVH.txtDrop2.setVisibility(View.GONE);
                                    notificationVH.qweeksnap.setVisibility(View.GONE);
                                    notificationVH.reactionImage.setVisibility(View.VISIBLE);
                                    notificationVH.audioLayout.setVisibility(View.GONE);
                                    notificationVH.exoPlayerView.setVisibility(View.GONE);

                                    // Set Reaction GIF
                                    Glide
                                            .with(context)
                                            .asGif()
                                            .load(notificationItem.getQweekSnap())
                                            .override(Target.SIZE_ORIGINAL)
                                            .thumbnail(0.3f)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(notificationVH.reactionImage);

                                    /*
                                    List<String> images4 = Collections.singletonList(notificationItem.getQweekSnap());
                                    notificationVH.reactionImage.setOnClickListener(v -> new StfalconImageViewer.Builder<>(context, images4, (imageView, imageUrl) -> Glide.with(context).asGif().load(imageUrl).into(imageView))
                                            .withTransitionFrom(notificationVH.reactionImage)
                                            .withBackgroundColor(context.getResources().getColor(R.color.black))
                                            .show());

                                     */

                                    notificationVH.reactionImage.setOnClickListener(v-> {
                                        DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                                        FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                                        bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                                    });

                                    break;
                                case "audio":
                                    notificationVH.txtDrop.setVisibility(View.GONE);
                                    notificationVH.txtDrop2.setVisibility(View.GONE);
                                    notificationVH.qweeksnap.setVisibility(View.GONE);
                                    notificationVH.reactionImage.setVisibility(View.GONE);
                                    notificationVH.audioLayout.setVisibility(View.VISIBLE);
                                    notificationVH.exoPlayerView.setVisibility(View.GONE);

                                    //Media type is Audio
                                    notificationVH.playAudio.setVisibility(View.VISIBLE);
                                    notificationVH.audioTxtLayout.setVisibility(View.VISIBLE);
                                    notificationVH.blastAudio.setVisibility(View.GONE);

                                    // Set The Drop for Audio
                                    SpannableString hashText = new SpannableString(notificationItem.getDrop());
                                    Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                                    while (matcher.find()) {
                                        hashText.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.QweekThemeColor)), matcher.start(), matcher.end(), 0);
                                    }
                                    notificationVH.txtDrop2.setText(hashText);

                                    // Setup Audio Visualisation
                                    notificationVH.playAudio.setOnClickListener(v -> {
                                        notificationVH.playAudio.setVisibility(View.GONE);
                                        notificationVH.blastAudio.setVisibility(View.VISIBLE);

                                        if (isPlaying) {
                                            player.stop();
                                            isPlaying = false;
                                        } else {
                                            player = MediaPlayer.create(context, Uri.parse(notificationItem.getAudio()));
                                            player.setOnPreparedListener(mp -> {
                                                mp.start();
                                                isPlaying = true;
                                                notificationVH.blastAudio.show();
                                                notificationVH.blastAudio.setAudioSessionId(mp.getAudioSessionId());
                                                NoiseSuppressor.create(mp.getAudioSessionId());
                                            });
                                            player.setOnCompletionListener(mp -> {
                                                notificationVH.blastAudio.hide();
                                                notificationVH.blastAudio.release();
                                                notificationVH.blastAudio.setVisibility(View.GONE);
                                                notificationVH.playAudio.setVisibility(View.VISIBLE);
                                                isPlaying = false;
                                            });
                                        }
                                    });

                                    notificationVH.blastAudio.setOnClickListener(v -> {
                                        if (isPlaying) {
                                            player.stop();
                                            isPlaying = false;
                                        } else {
                                            player = MediaPlayer.create(context, Uri.parse(notificationItem.getAudio()));
                                            player.setOnPreparedListener(mp -> {
                                                mp.start();
                                                isPlaying = true;
                                                notificationVH.blastAudio.show();
                                                notificationVH.blastAudio.setAudioSessionId(mp.getAudioSessionId());
                                                NoiseSuppressor.create(mp.getAudioSessionId());
                                            });
                                            player.setOnCompletionListener(mp -> {
                                                notificationVH.blastAudio.hide();
                                                notificationVH.blastAudio.release();
                                                notificationVH.blastAudio.setVisibility(View.GONE);
                                                notificationVH.playAudio.setVisibility(View.VISIBLE);
                                                isPlaying = false;
                                            });
                                        }
                                    });

                                    break;
                            }
                        }

                    } else if(notificationItem.getNotificationType().equals("comment"))  {
                        notificationVH.noteLayout.setClickable(true);
                        notificationVH.noteLayout.setOnClickListener(v->{
                            DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                            FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                            bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                        });

                        notificationVH.commentParent.setVisibility(View.VISIBLE);
                        notificationVH.txtDrop3.setVisibility(View.VISIBLE);
                        if (!TextUtils.isEmpty(notificationItem.getString())) {
                            SpannableString hashText = new SpannableString(notificationItem.getString());
                            Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                            while (matcher.find()) {
                                hashText.setSpan(
                                        new ForegroundColorSpan(context.getResources().getColor(R.color.QweekThemeColor)),
                                        matcher.start(),
                                        matcher.end(),
                                        0);
                            }
                            //if none set text and make it visible
                            notificationVH.txtDrop3.setText(hashText);
                            notificationVH.txtDrop3.setVisibility(View.VISIBLE);
                        } else {
                            // status is empty, remove from view
                            notificationVH.txtDrop3.setVisibility(View.GONE);
                            notificationVH.commentParent.setVisibility(View.GONE);
                        }

                        notificationVH.noteContentCard.setVisibility(View.VISIBLE);

                        String dropType = notificationItem.getDropType();
                        if(dropType != null) {
                        switch (notificationItem.getDropType()) {
                            case "drop":
                                notificationVH.txtDrop.setVisibility(View.VISIBLE);
                                notificationVH.txtDrop2.setVisibility(View.GONE);
                                notificationVH.qweeksnap.setVisibility(View.GONE);
                                notificationVH.reactionImage.setVisibility(View.GONE);
                                notificationVH.audioLayout.setVisibility(View.GONE);
                                notificationVH.exoPlayerView.setVisibility(View.GONE);


                                if (!TextUtils.isEmpty(notificationItem.getDrop())) {
                                    SpannableString hashText = new SpannableString(notificationItem.getDrop());
                                    Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                                    while (matcher.find()) {
                                        hashText.setSpan(
                                                new ForegroundColorSpan(context.getResources().getColor(R.color.QweekThemeColor)),
                                                matcher.start(),
                                                matcher.end(),
                                                0);
                                    }
                                    //if none set text and make it visible
                                    notificationVH.txtDrop.setText(hashText);
                                    notificationVH.txtDrop.setVisibility(View.VISIBLE);
                                } else {
                                    // status is empty, remove from view
                                    notificationVH.txtDrop.setVisibility(View.GONE);
                                }

                                // Checking for null feed url
                                if (notificationItem.getHasLink() == 1) {
                                    notificationVH.url.setLink(notificationItem.getLink(), new ViewListener() {
                                        @Override
                                        public void onSuccess(boolean status) {

                                        }

                                        @Override
                                        public void onError(Exception e) {

                                        }
                                    });
                                    notificationVH.url.setVisibility(View.VISIBLE);
                                } else {
                                    // url is null, remove from the view
                                    notificationVH.url.setVisibility(View.GONE);
                                }
                                break;
                            case "qweekpic":
                                notificationVH.txtDrop.setVisibility(View.GONE);
                                notificationVH.txtDrop2.setVisibility(View.GONE);
                                notificationVH.qweeksnap.setVisibility(View.VISIBLE);
                                notificationVH.reactionImage.setVisibility(View.GONE);
                                notificationVH.audioLayout.setVisibility(View.GONE);
                                notificationVH.exoPlayerView.setVisibility(View.GONE);

                                RequestOptions requestOptions2 = new RequestOptions() // because file name is always same
                                        .format(DecodeFormat.PREFER_RGB_565);
                                Glide
                                        .with(context)
                                        .load(notificationItem.getQweekSnap())
                                        //.override(Target.SIZE_ORIGINAL)
                                        .thumbnail(0.3f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .apply(requestOptions2)
                                        .into(notificationVH.qweeksnap);

                                /*
                                List<String> images = Collections.singletonList(notificationItem.getQweekSnap());
                                notificationVH.qweeksnap.setOnClickListener(v -> new StfalconImageViewer.Builder<>(context, images, (imageView, imageUrl) -> Glide.with(context).load(imageUrl).into(imageView))
                                        .withTransitionFrom(notificationVH.qweeksnap)
                                        .withBackgroundColor(context.getResources().getColor(R.color.black))
                                        .show());

                                 */

                                notificationVH.qweeksnap.setOnClickListener(v-> {
                                    DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                                    FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                                    bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                                });

                                break;
                            case "qweekvid":
                                notificationVH.txtDrop.setVisibility(View.GONE);
                                notificationVH.txtDrop2.setVisibility(View.GONE);
                                notificationVH.qweeksnap.setVisibility(View.VISIBLE);
                                notificationVH.reactionImage.setVisibility(View.GONE);
                                notificationVH.audioLayout.setVisibility(View.GONE);

                                RequestOptions requestOptions3 = new RequestOptions() // because file name is always same
                                        .format(DecodeFormat.PREFER_RGB_565);
                                Glide
                                        .with(context)
                                        .load(notificationItem.getQweekSnap())
                                        //.override(Target.SIZE_ORIGINAL)
                                        .thumbnail(0.3f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .apply(requestOptions3)
                                        .into(notificationVH.qweeksnap);

                                /*
                                List<String> images3 = Collections.singletonList(notificationItem.getQweekSnap());
                                notificationVH.qweeksnap.setOnClickListener(v -> new StfalconImageViewer.Builder<>(context, images3, (imageView, imageUrl) -> Glide.with(context).load(imageUrl).into(imageView))
                                        .withTransitionFrom(notificationVH.qweeksnap)
                                        .withBackgroundColor(context.getResources().getColor(R.color.black))
                                        .show());

                                 */

                                notificationVH.qweeksnap.setOnClickListener(v-> {
                                    DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                                    FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                                    bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                                });

                                break;
                            case "reaction":
                                notificationVH.txtDrop.setVisibility(View.GONE);
                                notificationVH.txtDrop2.setVisibility(View.GONE);
                                notificationVH.qweeksnap.setVisibility(View.GONE);
                                notificationVH.reactionImage.setVisibility(View.VISIBLE);
                                notificationVH.audioLayout.setVisibility(View.GONE);
                                notificationVH.exoPlayerView.setVisibility(View.GONE);

                                // Set Reaction GIF
                                Glide
                                        .with(context)
                                        .asGif()
                                        .load(notificationItem.getQweekSnap())
                                        .override(Target.SIZE_ORIGINAL)
                                        .thumbnail(0.3f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(notificationVH.reactionImage);

                                /*
                                List<String> images4 = Collections.singletonList(notificationItem.getQweekSnap());
                                notificationVH.reactionImage.setOnClickListener(v -> new StfalconImageViewer.Builder<>(context, images4, (imageView, imageUrl) -> Glide.with(context).asGif().load(imageUrl).into(imageView))
                                        .withTransitionFrom(notificationVH.reactionImage)
                                        .withBackgroundColor(context.getResources().getColor(R.color.black))
                                        .show());

                                 */

                                notificationVH.reactionImage.setOnClickListener(v-> {
                                    DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                                    FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                                    bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                                });

                                break;
                            case "audio":
                                notificationVH.txtDrop.setVisibility(View.GONE);
                                notificationVH.txtDrop2.setVisibility(View.GONE);
                                notificationVH.qweeksnap.setVisibility(View.GONE);
                                notificationVH.reactionImage.setVisibility(View.GONE);
                                notificationVH.audioLayout.setVisibility(View.VISIBLE);
                                notificationVH.exoPlayerView.setVisibility(View.GONE);

                                //Media type is Audio
                                notificationVH.playAudio.setVisibility(View.VISIBLE);
                                notificationVH.audioTxtLayout.setVisibility(View.VISIBLE);
                                notificationVH.blastAudio.setVisibility(View.GONE);

                                // Set The Drop for Audio
                                SpannableString hashText = new SpannableString(notificationItem.getDrop());
                                Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                                while (matcher.find()) {
                                    hashText.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.QweekThemeColor)), matcher.start(), matcher.end(), 0);
                                }
                                notificationVH.txtDrop2.setText(hashText);

                                // Setup Audio Visualisation
                                notificationVH.playAudio.setOnClickListener(v -> {
                                    notificationVH.playAudio.setVisibility(View.GONE);
                                    notificationVH.blastAudio.setVisibility(View.VISIBLE);

                                    if (isPlaying) {
                                        player.stop();
                                        isPlaying = false;
                                    } else {
                                        player = MediaPlayer.create(context, Uri.parse(notificationItem.getAudio()));
                                        player.setOnPreparedListener(mp -> {
                                            mp.start();
                                            isPlaying = true;
                                            notificationVH.blastAudio.show();
                                            notificationVH.blastAudio.setAudioSessionId(mp.getAudioSessionId());
                                            NoiseSuppressor.create(mp.getAudioSessionId());
                                        });
                                        player.setOnCompletionListener(mp -> {
                                            notificationVH.blastAudio.hide();
                                            notificationVH.blastAudio.release();
                                            notificationVH.blastAudio.setVisibility(View.GONE);
                                            notificationVH.playAudio.setVisibility(View.VISIBLE);
                                            isPlaying = false;
                                        });
                                    }
                                });

                                notificationVH.blastAudio.setOnClickListener(v -> {
                                    if (isPlaying) {
                                        player.stop();
                                        isPlaying = false;
                                    } else {
                                        player = MediaPlayer.create(context, Uri.parse(notificationItem.getAudio()));
                                        player.setOnPreparedListener(mp -> {
                                            mp.start();
                                            isPlaying = true;
                                            notificationVH.blastAudio.show();
                                            notificationVH.blastAudio.setAudioSessionId(mp.getAudioSessionId());
                                            NoiseSuppressor.create(mp.getAudioSessionId());
                                        });
                                        player.setOnCompletionListener(mp -> {
                                            notificationVH.blastAudio.hide();
                                            notificationVH.blastAudio.release();
                                            notificationVH.blastAudio.setVisibility(View.GONE);
                                            notificationVH.playAudio.setVisibility(View.VISIBLE);
                                            isPlaying = false;
                                        });
                                    }
                                });

                                break;
                        }
                        }


                    } else if(notificationItem.getNotificationType().equals("upvote"))  {
                        notificationVH.noteLayout.setClickable(true);
                        notificationVH.noteLayout.setOnClickListener(v->{
                            DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                            FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                            bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                        });

                        notificationVH.noteContentCard.setVisibility(View.VISIBLE);

                        String dropType = notificationItem.getDropType();
                        if(dropType != null) {
                        switch (notificationItem.getDropType()) {
                            case "drop":
                                notificationVH.txtDrop.setVisibility(View.VISIBLE);
                                notificationVH.txtDrop2.setVisibility(View.GONE);
                                notificationVH.qweeksnap.setVisibility(View.GONE);
                                notificationVH.reactionImage.setVisibility(View.GONE);
                                notificationVH.audioLayout.setVisibility(View.GONE);
                                notificationVH.exoPlayerView.setVisibility(View.GONE);


                                if (!TextUtils.isEmpty(notificationItem.getDrop())) {
                                    SpannableString hashText = new SpannableString(notificationItem.getDrop());
                                    Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                                    while (matcher.find()) {
                                        hashText.setSpan(
                                                new ForegroundColorSpan(context.getResources().getColor(R.color.QweekThemeColor)),
                                                matcher.start(),
                                                matcher.end(),
                                                0);
                                    }
                                    //if none set text and make it visible
                                    notificationVH.txtDrop.setText(hashText);
                                } else {
                                    // status is empty, remove from view
                                    notificationVH.txtDrop.setVisibility(View.GONE);
                                }

                                // Checking for null feed url
                                if (notificationItem.getHasLink() == 1) {
                                    notificationVH.url.setLink(notificationItem.getLink(), new ViewListener() {
                                        @Override
                                        public void onSuccess(boolean status) {

                                        }

                                        @Override
                                        public void onError(Exception e) {

                                        }
                                    });
                                    notificationVH.url.setVisibility(View.VISIBLE);
                                } else {
                                    // url is null, remove from the view
                                    notificationVH.url.setVisibility(View.GONE);
                                }
                                break;
                            case "qweekpic":
                                notificationVH.txtDrop.setVisibility(View.GONE);
                                notificationVH.txtDrop2.setVisibility(View.GONE);
                                notificationVH.qweeksnap.setVisibility(View.VISIBLE);
                                notificationVH.reactionImage.setVisibility(View.GONE);
                                notificationVH.audioLayout.setVisibility(View.GONE);
                                notificationVH.exoPlayerView.setVisibility(View.GONE);

                                RequestOptions requestOptions2 = new RequestOptions() // because file name is always same
                                        .format(DecodeFormat.PREFER_RGB_565);
                                Glide
                                        .with(context)
                                        .load(notificationItem.getQweekSnap())
                                        //.override(Target.SIZE_ORIGINAL)
                                        .thumbnail(0.3f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .apply(requestOptions2)
                                        .into(notificationVH.qweeksnap);

                                /*
                                List<String> images = Collections.singletonList(notificationItem.getQweekSnap());
                                notificationVH.qweeksnap.setOnClickListener(v -> new StfalconImageViewer.Builder<>(context, images, (imageView, imageUrl) -> Glide.with(context).load(imageUrl).into(imageView))
                                        .withTransitionFrom(notificationVH.qweeksnap)
                                        .withBackgroundColor(context.getResources().getColor(R.color.black))
                                        .show());

                                 */

                                notificationVH.qweeksnap.setOnClickListener(v-> {
                                    DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                                    FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                                    bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                                });

                                break;
                            case "qweekvid":
                                notificationVH.txtDrop.setVisibility(View.GONE);
                                notificationVH.txtDrop2.setVisibility(View.GONE);
                                notificationVH.qweeksnap.setVisibility(View.VISIBLE);
                                notificationVH.reactionImage.setVisibility(View.GONE);
                                notificationVH.audioLayout.setVisibility(View.GONE);

                                RequestOptions requestOptions3 = new RequestOptions() // because file name is always same
                                        .format(DecodeFormat.PREFER_RGB_565);
                                Glide
                                        .with(context)
                                        .load(notificationItem.getQweekSnap())
                                        //.override(Target.SIZE_ORIGINAL)
                                        .thumbnail(0.3f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .apply(requestOptions3)
                                        .into(notificationVH.qweeksnap);

                                /*
                                List<String> images3 = Collections.singletonList(notificationItem.getQweekSnap());
                                notificationVH.qweeksnap.setOnClickListener(v -> new StfalconImageViewer.Builder<>(context, images3, (imageView, imageUrl) -> Glide.with(context).load(imageUrl).into(imageView))
                                        .withTransitionFrom(notificationVH.qweeksnap)
                                        .withBackgroundColor(context.getResources().getColor(R.color.black))
                                        .show());

                                 */

                                notificationVH.qweeksnap.setOnClickListener(v-> {
                                    DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                                    FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                                    bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                                });

                                break;
                            case "reaction":
                                notificationVH.txtDrop.setVisibility(View.GONE);
                                notificationVH.txtDrop2.setVisibility(View.GONE);
                                notificationVH.qweeksnap.setVisibility(View.GONE);
                                notificationVH.reactionImage.setVisibility(View.VISIBLE);
                                notificationVH.audioLayout.setVisibility(View.GONE);
                                notificationVH.exoPlayerView.setVisibility(View.GONE);

                                // Set Reaction GIF
                                Glide
                                        .with(context)
                                        .asGif()
                                        .load(notificationItem.getQweekSnap())
                                        .override(Target.SIZE_ORIGINAL)
                                        .thumbnail(0.3f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(notificationVH.reactionImage);

                                /*
                                List<String> images4 = Collections.singletonList(notificationItem.getQweekSnap());
                                notificationVH.reactionImage.setOnClickListener(v -> new StfalconImageViewer.Builder<>(context, images4, (imageView, imageUrl) -> Glide.with(context).asGif().load(imageUrl).into(imageView))
                                        .withTransitionFrom(notificationVH.reactionImage)
                                        .withBackgroundColor(context.getResources().getColor(R.color.black))
                                        .show());

                                 */

                                notificationVH.reactionImage.setOnClickListener(v-> {
                                    DropBottomSheet bottomSheet = new DropBottomSheet(context, username, notificationItem.getDropID());
                                    FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                                    bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
                                });

                                break;
                            case "audio":
                                notificationVH.txtDrop.setVisibility(View.GONE);
                                notificationVH.txtDrop2.setVisibility(View.GONE);
                                notificationVH.qweeksnap.setVisibility(View.GONE);
                                notificationVH.reactionImage.setVisibility(View.GONE);
                                notificationVH.audioLayout.setVisibility(View.VISIBLE);
                                notificationVH.exoPlayerView.setVisibility(View.GONE);

                                //Media type is Audio
                                notificationVH.playAudio.setVisibility(View.VISIBLE);
                                notificationVH.audioTxtLayout.setVisibility(View.VISIBLE);
                                notificationVH.blastAudio.setVisibility(View.GONE);

                                // Set The Drop for Audio
                                SpannableString hashText = new SpannableString(notificationItem.getDrop());
                                Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
                                while (matcher.find()) {
                                    hashText.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.QweekThemeColor)), matcher.start(), matcher.end(), 0);
                                }
                                notificationVH.txtDrop2.setText(hashText);

                                // Setup Audio Visualisation
                                notificationVH.playAudio.setOnClickListener(v -> {
                                    notificationVH.playAudio.setVisibility(View.GONE);
                                    notificationVH.blastAudio.setVisibility(View.VISIBLE);

                                    if (isPlaying) {
                                        player.stop();
                                        isPlaying = false;
                                    } else {
                                        player = MediaPlayer.create(context, Uri.parse(notificationItem.getAudio()));
                                        player.setOnPreparedListener(mp -> {
                                            mp.start();
                                            isPlaying = true;
                                            notificationVH.blastAudio.show();
                                            notificationVH.blastAudio.setAudioSessionId(mp.getAudioSessionId());
                                            NoiseSuppressor.create(mp.getAudioSessionId());
                                        });
                                        player.setOnCompletionListener(mp -> {
                                            notificationVH.blastAudio.hide();
                                            notificationVH.blastAudio.release();
                                            notificationVH.blastAudio.setVisibility(View.GONE);
                                            notificationVH.playAudio.setVisibility(View.VISIBLE);
                                            isPlaying = false;
                                        });
                                    }
                                });

                                notificationVH.blastAudio.setOnClickListener(v -> {
                                    if (isPlaying) {
                                        player.stop();
                                        isPlaying = false;
                                    } else {
                                        player = MediaPlayer.create(context, Uri.parse(notificationItem.getAudio()));
                                        player.setOnPreparedListener(mp -> {
                                            mp.start();
                                            isPlaying = true;
                                            notificationVH.blastAudio.show();
                                            notificationVH.blastAudio.setAudioSessionId(mp.getAudioSessionId());
                                            NoiseSuppressor.create(mp.getAudioSessionId());
                                        });
                                        player.setOnCompletionListener(mp -> {
                                            notificationVH.blastAudio.hide();
                                            notificationVH.blastAudio.release();
                                            notificationVH.blastAudio.setVisibility(View.GONE);
                                            notificationVH.playAudio.setVisibility(View.VISIBLE);
                                            isPlaying = false;
                                        });
                                    }
                                });

                                break;
                        }
                        }
                    } else {
                        notificationVH.noteLayout.setClickable(false);
                        notificationVH.noteContentCard.setVisibility(View.GONE);
                    }

                    break;

                case LOADING:
                    NotificationAdapter.LoadingVH loadingVH = (NotificationAdapter.LoadingVH) holder;

                    if (retryPageLoad) {
                        loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                        loadingVH.mProgressBar.setVisibility(View.GONE);

                        loadingVH.mErrorTxt.setText(
                                errorMsg != null ?
                                        errorMsg :
                                        context.getString(R.string.error_msg_unknown));

                    } else {
                        loadingVH.mErrorLayout.setVisibility(View.GONE);
                        loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

    private @Nullable
    Drawable getTinted(@ColorInt int color) {
        // need to mutate otherwise all references to this drawable will be tinted
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_alien).mutate();
        return tint(drawable, ColorStateList.valueOf(color));
    }

    public static Drawable tint(Drawable input, ColorStateList tint) {
        if (input == null) {
            return null;
        }
        Drawable wrappedDrawable = DrawableCompat.wrap(input);
        DrawableCompat.setTintList(wrappedDrawable, tint);
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.MULTIPLY);
        return wrappedDrawable;
    }

    @Override
    public int getItemCount() {
        return notificationItems == null ? 0 : notificationItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return NOTIFICATIONS;
        } else {
            return (position == notificationItems.size() - 1 && isLoadingAdded) ? LOADING : NOTIFICATIONS;
        }
    }

    /*
        Helpers - bind Views
   _________________________________________________________________________________________________
    */


    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    private void add(NotificationItem r) {
        notificationItems.add(r);
        notifyItemInserted(notificationItems.size() - 1);
    }

    public void addAll(List<NotificationItem> notificationItems) {
        for (NotificationItem result : notificationItems) {
            add(result);
        }
    }

    private void remove(NotificationItem r) {
        int position = notificationItems.indexOf(r);
        if (position > -1) {
            notificationItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new NotificationItem());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = notificationItems.size() - 1;
        NotificationItem notificationItem = getItem(position);

        if (notificationItem != null) {
            notificationItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    private NotificationItem getItem(int position) {
        return notificationItems.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(notificationItems.size() - 1);
        if (errorMsg != null) this.errorMsg = errorMsg;
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */
    /**
     * Feed content ViewHolder
     */
    protected class NotificationVH extends RecyclerView.ViewHolder {
        private TextView status;
        private CircleImageView profilePic;
        private CardView noteLayout, noteContentCard;
        private EmojiTextView txtDrop, txtDrop2, txtDrop3;
        private ImageView qweeksnap, reactionImage, playAudio;
        private BlobVisualizer blastAudio;
        private RichLinkView url;
        private LinearLayout audioLayout, audioTxtLayout, commentParent;
        private PlayerView exoPlayerView;

        NotificationVH(View itemView) {
            super(itemView);

            status = itemView.findViewById(R.id.status);
            profilePic = itemView.findViewById(R.id.profilePic);
            noteLayout = itemView.findViewById(R.id.noteLayout);
            exoPlayerView = itemView.findViewById(R.id.video);
            noteContentCard = itemView.findViewById(R.id.noteContentCard);
            txtDrop = itemView.findViewById(R.id.txtDrop);
            txtDrop2 = itemView.findViewById(R.id.txtDrop2);
            qweeksnap = itemView.findViewById(R.id.qweekSnap);
            reactionImage = itemView.findViewById(R.id.reactionImage);
            blastAudio = itemView.findViewById(R.id.blast);
            url = itemView.findViewById(R.id.txtUrl);
            audioLayout = itemView.findViewById(R.id.audioLayout);
            audioTxtLayout = itemView.findViewById(R.id.audioTxtLayout);
            playAudio = itemView.findViewById(R.id.playAudio);
            commentParent = itemView.findViewById(R.id.commentParent);
            txtDrop3 = itemView.findViewById(R.id.txtDrop3);
        }
    }

    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;


        LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            ImageButton mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }
}
