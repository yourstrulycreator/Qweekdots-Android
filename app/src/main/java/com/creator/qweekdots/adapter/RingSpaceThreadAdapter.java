package com.creator.qweekdots.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.ProfileActivity;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.mediaplayer.RSVideoPlayer;
import com.creator.qweekdots.mediaplayer.RSVideoPlayerStandard;
import com.creator.qweekdots.models.Message;
import com.creator.qweekdots.ui.MessageBottomSheet;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static maes.tech.intentanim.CustomIntent.customType;

public class RingSpaceThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static String TAG = RingSpaceThreadAdapter.class.getSimpleName();

    private String userId;
    private String username;
    private int SELF = 100;

    private Context mContext;
    private ArrayList<Message> messageArrayList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        EmojiTextView message;
        TextView metaMessage, messageTime;
        CircleImageView avatar;
        CardView messageMediaCard, messageMetaActions, messageTxtCard;
        LinearLayout messageActions;
        ImageView messagePhoto;
        RSVideoPlayerStandard messageVideo;
        ConstraintLayout messageLayout;
        LinearLayout messageDelete, cancelFocus;
        ImageView expandBtn;

        public ViewHolder(View view) {
            super(view);
            message = itemView.findViewById(R.id.message);
            metaMessage = itemView.findViewById(R.id.meta_message);
            avatar = itemView.findViewById(R.id.message_avatar);
            messageTime = itemView.findViewById(R.id.message_timestamp);
            messageMediaCard = itemView.findViewById(R.id.message_mediaCard);
            messagePhoto = itemView.findViewById(R.id.message_media);
            messageVideo = itemView.findViewById(R.id.message_videoplayer);
            messageActions = itemView.findViewById(R.id.message_actions);
            messageMetaActions = itemView.findViewById(R.id.message_meta_actions);
            messageLayout = itemView.findViewById(R.id.message_layout);
            messageTxtCard = itemView.findViewById(R.id.messageTxtCard);

            //Message Actions
            expandBtn = itemView.findViewById(R.id.expand_btn);

            // Message Meta Actions
            messageDelete = itemView.findViewById(R.id.delete_message);
            cancelFocus = itemView.findViewById(R.id.cancel_focus);
        }
    }


    public RingSpaceThreadAdapter(Context mContext, ArrayList<Message> messageArrayList, String userId, String username) {
        this.mContext = mContext;
        this.messageArrayList = messageArrayList;
        this.userId = userId;
        this.username = username;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        EmojiManager.install(new IosEmojiProvider());
        View itemView;

        // view type is to identify where to render the chat message
        // left or right
        if (viewType == SELF) {
            // self message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ring_feed_item, parent, false);
        } else {
            // others message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ring_feed_item, parent, false);
        }

        return new ViewHolder(itemView);
    }


    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message.getUser().getId().equals(userId)) {
            return SELF;
        }
        return position;
    }

    @SuppressLint("LogNotTimber")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, int position) {

        Message message = messageArrayList.get(position);

        if(message.getMessage().isEmpty()) {
            ((ViewHolder) holder).messageTxtCard.setVisibility(View.GONE);
        } else {
            String messageTXT = message.getMessage();
            messageTXT = unescapeJava(messageTXT);

            ((ViewHolder) holder).message.setText(messageTXT);
        }


        //String timestamp = getTimeStamp(message.getCreatedAt());
        //((ViewHolder) holder).messageTime.setText(timestamp);

        /*Picasso.get()
                .load(message.getUser().getAvatar())
                .resize(32, 32)
                .placeholder(R.drawable.ic_alien)
                .error(R.drawable.ic_alien)
                .centerCrop()
                .into(((ViewHolder) holder).avatar);*/

        RequestOptions requestOptions2 = new RequestOptions() // because file name is always same
                .format(DecodeFormat.PREFER_RGB_565);
        Drawable placeholder = getTinted(R.drawable.ic_alien, mContext.getResources().getColor(R.color.contentTextColor));
        Glide
                .with(mContext)
                .load(message.getUser().getAvatar())
                .override(32, 32)
                .placeholder(placeholder)
                .error(placeholder)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(requestOptions2)
                .into(((ViewHolder) holder).avatar);

        String meta = "q/"+message.getUser().getUserName();
        ((ViewHolder) holder).metaMessage.setText(meta);

        ((ViewHolder) holder).avatar.setOnClickListener(v-> {
            Intent i = new Intent(mContext, ProfileActivity.class);
            i.putExtra("profile", message.getUser().getUserName());
            mContext.startActivity(i);
            customType(mContext, "fadein-to-fadeout");
        });

        if (!message.getUser().getId().equals(userId)) {

        } else {
            ((ViewHolder) holder).messageLayout.setOnLongClickListener(v -> {
                ((ViewHolder) holder).messageMetaActions.setVisibility(View.VISIBLE);
                //((ViewHolder) holder).messageLayout.hasFocus();
                //((ViewHolder) holder).messageLayout.setFocusableInTouchMode(true);
                return false;
            });
        }

        if(message.getHasMedia() == 1) {
            ((ViewHolder) holder).messageMediaCard.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).messageTxtCard.setElevation(0);

            if(message.getMediaType().equals("photo")) {
                ((ViewHolder) holder).messagePhoto.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).messageVideo.setVisibility(View.GONE);

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                        .format(DecodeFormat.PREFER_RGB_565)
                        .skipMemoryCache(true);
                Glide
                        .with(mContext)
                        .load(message.getQweeksnap())
                        .override(Target.SIZE_ORIGINAL)
                        .apply(requestOptions)
                        .into(((ViewHolder) holder).messagePhoto);
            } else if (message.getMediaType().equals("video")) {
                ((ViewHolder) holder).messageVideo.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).messagePhoto.setVisibility(View.GONE);

                // qweekvid
                ((ViewHolder) holder).messageVideo.setUp(message.getQweeksnap(),
                        RSVideoPlayer.SCREEN_LAYOUT_LIST);
                ((ViewHolder) holder).messageVideo.setThumbImageView(message.getQweeksnap());
            }
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Confirm Delete...");

            // Setting Dialog Message
            alertDialog.setMessage("Are you sure you want delete this message?");

            // Setting Icon to Dialog
            alertDialog.setIcon(R.drawable.ic_delete);

            // Setting Positive "Yes" Btn
            alertDialog.setPositiveButton("YES",
                    (dialog, which) -> {
                        deleteMessage(message.getId(), message.getUser().getId());

                        //((ViewHolder) holder).messageLayout.clearFocus();
                        //((ViewHolder) holder).messageLayout.setFocusableInTouchMode(false);
                        ((ViewHolder) holder).messageMetaActions.setVisibility(View.GONE);
                    });
            // Setting Negative "NO" Btn
            alertDialog.setNegativeButton("NO",
                    (dialog, which) -> dialog.cancel());

        ((ViewHolder) holder).messageDelete.setOnClickListener(v-> alertDialog.show());

        ((ViewHolder) holder).cancelFocus.setOnClickListener(v-> {
            //((ViewHolder) holder).messageLayout.clearFocus();
            //((ViewHolder) holder).messageLayout.setFocusableInTouchMode(false);
            ((ViewHolder) holder).messageMetaActions.setVisibility(View.GONE);
        });

        ((ViewHolder) holder).messageLayout.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // run scale animation and make it bigger
                Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.scale_in_focus);
                ((ViewHolder) holder).messageLayout.startAnimation(anim);
                anim.setFillAfter(true);
            } else {
                // run scale animation and make it smaller
                Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.scale_out_focus);
                ((ViewHolder) holder).messageLayout.startAnimation(anim);
                anim.setFillAfter(true);
            }
        });

        ((ViewHolder) holder).expandBtn.setOnClickListener(v -> {
            MessageBottomSheet bottomSheet = new MessageBottomSheet(mContext, username, message.getId());
            FragmentManager manager = ((AppCompatActivity)mContext).getSupportFragmentManager();
            bottomSheet.show(Objects.requireNonNull(manager),bottomSheet.getTag());
        });
    }

    private @Nullable
    Drawable getTinted(@DrawableRes int res, @ColorInt int color) {
        // need to mutate otherwise all references to this drawable will be tinted
        Drawable drawable = ContextCompat.getDrawable(mContext, res).mutate();
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

    private static String unescapeJava(String escaped) {
        if (!escaped.contains("\\u"))
            return escaped;

        StringBuilder processed = new StringBuilder();

        int position = escaped.indexOf("\\u");
        while (position != -1) {
            if (position != 0)
                processed.append(escaped.substring(0, position));
            String token = escaped.substring(position + 2, position + 6);
            escaped = escaped.substring(position + 6);
            processed.append((char) Integer.parseInt(token, 16));
            position = escaped.indexOf("\\u");
        }
        processed.append(escaped);

        return processed.toString();
    }

    private void deleteMessage(final String message_id, String user_id) {
        // Tag used to cancel the request
        String tag_string_req = "req_delete";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                "https://qweek.fun/genjitsu/chat/message_delete.php", response -> {
            Timber.tag(TAG).d("Delete Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    String sent = jObj.getString("sent");
                    Toasty.success(mContext, sent, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(mContext, "Mission Control, come in !", Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Timber.tag(TAG).e("Delete Error: %s", error.getMessage());
            Toasty.error(mContext,
                    "Apollo, we have a problem !", Toast.LENGTH_LONG).show();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("id", message_id);
                params.put("u", user_id);

                return params;
            }

        };

        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }
}
