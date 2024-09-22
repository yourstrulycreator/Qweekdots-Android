package com.creator.qweekdots.adapter;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
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
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
        TextView metaMessage, messageTime, messageFilename;
        EmojiTextView message;
        CircleImageView avatar;
        CardView messageMediaCard, messageActions, messageFileCard;
        ImageView messagePhoto;
        RSVideoPlayerStandard messageVideo;
        ConstraintLayout messageLayout;
        LinearLayout messageDelete, cancelFocus, messageTimeLay;

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
            messageFileCard = itemView.findViewById(R.id.message_fileCard);
            messageFilename = itemView.findViewById(R.id.message_filename);

            // Message Actions

            messageDelete = itemView.findViewById(R.id.delete_message);
            cancelFocus = itemView.findViewById(R.id.cancel_focus);
            messageTimeLay = itemView.findViewById(R.id.time_message);
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
            Timber.tag(TAG).e("Message text added:%s", message.getMessage());
        }


        String timestamp = getTimeStamp(message.getCreatedAt());
        ((ViewHolder) holder).messageTime.setText(timestamp);

        if (!message.getUser().getId().equals(userId)) {
            /*Picasso.get()
                    .load(message.getUser().getAvatar())
                    .resize(32, 32)
                    .placeholder(R.drawable.ic_alien)
                    .error(R.drawable.ic_alien)
                    .centerCrop()
                    .into(((ViewHolder) holder).avatar);*/

            RequestOptions requestOptions = new RequestOptions() // because file name is always same
                    .format(DecodeFormat.PREFER_RGB_565);
            Drawable placeholder = getTinted(mContext.getResources().getColor(R.color.contentTextColor));
            Glide
                    .with(mContext)
                    .load(message.getUser().getAvatar())
                    .override(32, 32)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .thumbnail(0.3f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(requestOptions)
                    .into(((ViewHolder) holder).avatar);

            String meta = "q/"+message.getUser().getUserName();
            ((ViewHolder) holder).metaMessage.setText(meta);
        }

        if(message.getHasMedia() == 1) {
            ((ViewHolder) holder).messageMediaCard.setVisibility(View.VISIBLE);

            switch (message.getMediaType()) {
                case "photo":
                    ((ViewHolder) holder).messagePhoto.setVisibility(View.VISIBLE);
                    ((ViewHolder) holder).messageVideo.setVisibility(View.GONE);
                    ((ViewHolder) holder).messageFileCard.setVisibility(View.GONE);

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

                    /*
                    List<String> images = Collections.singletonList(message.getQweeksnap());
                    ((ViewHolder) holder).messagePhoto.setOnClickListener(v-> new StfalconImageViewer.Builder<>(mContext, images, (imageView, imageUrl) -> Glide.with(mContext).load(imageUrl).into(imageView))
                            .withTransitionFrom(((ViewHolder) holder).messagePhoto)
                            .withBackgroundColor(mContext.getResources().getColor(R.color.black))
                            .show());

                     */
                    break;
                case "video":
                    ((ViewHolder) holder).messageVideo.setVisibility(View.VISIBLE);
                    ((ViewHolder) holder).messagePhoto.setVisibility(View.GONE);
                    ((ViewHolder) holder).messageFileCard.setVisibility(View.GONE);

                    // qweekvid
                    ((ViewHolder) holder).messageVideo.setUp(message.getQweeksnap(),
                            RSVideoPlayer.SCREEN_LAYOUT_LIST);
                    ((ViewHolder) holder).messageVideo.setThumbImageView(message.getQweeksnap());
                    break;
                case "file":
                    ((ViewHolder) holder).messageFileCard.setVisibility(View.VISIBLE);
                    ((ViewHolder) holder).messageVideo.setVisibility(View.GONE);
                    ((ViewHolder) holder).messagePhoto.setVisibility(View.GONE);

                    // file
                    ((ViewHolder) holder).messageFilename.setText(message.getFilename());

                    ((ViewHolder) holder).messageFileCard.setOnClickListener(v -> {
                        Toasty.info(mContext, "Downloading... Check your notifications", Toasty.LENGTH_LONG).show();
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(message.getFile()));
                        request.setDescription("Downloading file from Qwekdots servers");
                        request.setTitle("File Download");

                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, message.getFilename());


                        DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                    });
                    break;
            }
        }

        ((ViewHolder) holder).messageLayout.setOnLongClickListener(v -> {
            ((ViewHolder) holder).messageActions.setVisibility(View.VISIBLE);
            //((ViewHolder) holder).messageLayout.hasFocus();
            //((ViewHolder) holder).messageLayout.setFocusableInTouchMode(true);

            if (!message.getUser().getId().equals(userId)) {
                ((ViewHolder) holder).messageDelete.setVisibility(View.GONE);
            } else {
                ((ViewHolder) holder).messageDelete.setVisibility(View.VISIBLE);
            }
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
                (dialog, which) -> dialog.cancel());

        ((ViewHolder) holder).messageDelete.setOnClickListener(v-> alertDialog.show());

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



    @RequiresApi(api = Build.VERSION_CODES.O)
    static long download(String sourceUrl, String targetFileName) throws Exception {
        try (InputStream in = URI.create(sourceUrl).toURL().openStream()) {
            return Files.copy(in, Paths.get(targetFileName));
        }
    }

    private void saveUrl(final String filename, final String urlString)
            throws IOException {

        try (BufferedInputStream in = new BufferedInputStream(new URL(urlString).openStream()); FileOutputStream fout = new FileOutputStream(filename)) {

            final byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        }
    }

    private @Nullable
    Drawable getTinted(@ColorInt int color) {
        // need to mutate otherwise all references to this drawable will be tinted
        Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_alien).mutate();
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

    private static void openFile(Context context, File url) throws IOException {
        // Create URI
        Uri uri = Uri.fromFile(url);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if(url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if(url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if(url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if(url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if(url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if(url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if(url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if(url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if(url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if(url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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

