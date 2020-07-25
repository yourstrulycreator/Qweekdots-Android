package com.creator.qweekdots.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.mediaplayer.RSVideoPlayer;
import com.creator.qweekdots.mediaplayer.RSVideoPlayerStandard;
import com.creator.qweekdots.models.Message;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class ChatUserThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static String TAG = ChatUserThreadAdapter.class.getSimpleName();

    private String userId;
    private int SELF = 100;
    private static String today;

    private Context mContext;
    private ArrayList<Message> messageArrayList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView metaMessage, messageTime;
        EmojiTextView message;
        CircleImageView avatar;
        CardView messageMediaCard, messageActions;
        ImageView messagePhoto;
        RSVideoPlayerStandard messageVideo;
        ConstraintLayout messageLayout;
        LinearLayout messageDelete, cancelFocus;

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
            messageLayout = itemView.findViewById(R.id.message_layout);

            // Message Actions
            messageDelete = itemView.findViewById(R.id.delete_message);
            cancelFocus = itemView.findViewById(R.id.cancel_focus);
        }
    }


    public ChatUserThreadAdapter(Context mContext, ArrayList<Message> messageArrayList, String userId) {
        this.mContext = mContext;
        this.messageArrayList = messageArrayList;
        this.userId = userId;

        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
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
                    .inflate(R.layout.chat_item_self, parent, false);
        } else {
            // others message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_other, parent, false);
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

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, int position) {

        Message message = messageArrayList.get(position);

        if(message.getMessage().isEmpty()) {
            ((ViewHolder) holder).message.setVisibility(View.GONE);
        } else {
            ((ViewHolder) holder).message.setText(message.getMessage());
            Log.e(TAG, "Message text added:" + message.getMessage());
        }


        String timestamp = getTimeStamp(message.getCreatedAt());
        ((ViewHolder) holder).messageTime.setText(timestamp);

        if (!message.getUser().getId().equals(userId)) {
            Picasso.get()
                    .load(message.getUser().getAvatar())
                    .resize(32, 32)
                    .placeholder(R.drawable.ic_alien)
                    .error(R.drawable.ic_alien)
                    .centerCrop()
                    .into(((ViewHolder) holder).avatar);

            String meta = "q/"+message.getUser().getUserName();
            ((ViewHolder) holder).metaMessage.setText(meta);
        }

        if(message.getHasMedia() == 1) {
            ((ViewHolder) holder).messageMediaCard.setVisibility(View.VISIBLE);

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

        ((ViewHolder) holder).messageLayout.setOnLongClickListener(v -> {
            ((ViewHolder) holder).messageActions.setVisibility(View.VISIBLE);
            //((ViewHolder) holder).messageLayout.hasFocus();
            //((ViewHolder) holder).messageLayout.setFocusableInTouchMode(true);
            return false;
        });

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
                    ((ViewHolder) holder).messageActions.setVisibility(View.GONE);
                });
        // Setting Negative "NO" Btn
        alertDialog.setNegativeButton("NO",
                (dialog, which) -> {
                    dialog.cancel();
                });

        ((ViewHolder) holder).messageDelete.setOnClickListener(v-> {
            alertDialog.show();
        });

        ((ViewHolder) holder).cancelFocus.setOnClickListener(v-> {
            //((ViewHolder) holder).messageLayout.clearFocus();
            //((ViewHolder) holder).messageLayout.setFocusableInTouchMode(false);
            ((ViewHolder) holder).messageActions.setVisibility(View.GONE);
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

    @SuppressLint("SimpleDateFormat")
    private static String getTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "";

        today = today.length() < 2 ? "0" + today : today;

        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            assert date != null;
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }
}

