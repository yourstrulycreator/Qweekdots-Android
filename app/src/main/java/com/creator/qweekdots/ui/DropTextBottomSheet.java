package com.creator.qweekdots.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.creator.qweekdots.R;
import com.creator.qweekdots.activity.MainActivity;
import com.creator.qweekdots.activity.ReactionsActivity;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.app.AppController;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.models.SpinSpaces;
import com.creator.qweekdots.utils.AlertUtils;
import com.creator.qweekdots.utils.GifSizeFilter;
import com.creator.qweekdots.utils.Glide4Engine;
import com.creator.qweekdots.utils.ImageUtil;
import com.creator.qweekdots.utils.RoundedBottomSheetDialogFragment;
import com.creator.qweekdots.utils.ServiceHandler;
import com.creator.qweekdots.utils.StorageUtils;
import com.creator.qweekdots.volley.VolleyMultipartRequest;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;
import static maes.tech.intentanim.CustomIntent.customType;

public class DropTextBottomSheet extends RoundedBottomSheetDialogFragment implements AdapterView.OnItemSelectedListener {
    private String TAG = DropTextBottomSheet.class.getSimpleName();
    private View view;
    private BottomSheetBehavior bottomSheetBehavior;

    private String[] textHint = {
            "What's On Your Mind?",
            "Tell The World",
            "What's Happening ?"
    };
    private Random mRandom;
    private FABProgressCircle dropProgress;
    private FloatingActionButton dropBtn;
    private String username;
    private EmojiEditText dropInput;
    private ImageView mediaImage;
    private ImageView gifImage;
    private VideoView videoView;
    private boolean isPhoto = false;
    private boolean isVideo = false;
    private boolean isGif = false;
    private boolean isAudio = false;
    private Bitmap finalBitmap;
    private Uri finalVideo;
    private RequestQueue rQueue;
    private LinearLayout sheetMeta;
    private CardView mediaCard;
    private LinearLayout recordMeta;
    private SearchableSpinner spinSpaces;
    private ArrayList<SpinSpaces> spaceItems;
    ProgressDialog pDialog;

    // Url to get all spaces
    private String URL_SPACES = "https://qweek.fun/genjitsu/all_spaces.php";
    private String URL_SPACE = "https://qweek.fun/genjitsu/this_space.php";

    private ImageButton record_Btn;

    private byte[] inputData;
    private boolean isAudioReady = false;

    private ImageButton recordBtn;
    private boolean isRecording = false;

    private MediaRecorder mediaRecorder;

    private Chronometer timer;
    //private AudioRecordView audioRecordView;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    private File fileToPlay = null;
    //UI Elements
    private ImageButton playBtn;

    ConstraintLayout playerSheet;

    private SeekBar playerSeekbar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;

    private String mFilePath = null;

    private int selectedSpace = 61;

    private static final int PICKER_REQUEST_CODE = 1;

    private String gif = null;
    private String dropTxt;
    private String dropSpace;

    public DropTextBottomSheet(String giffrom, String droptext, String drop_space) {
        gif = giffrom;
        dropTxt = droptext;
        dropSpace = drop_space;
    }

    public DropTextBottomSheet() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        view = inflater.inflate(R.layout.drop_text_bottom_sheet, container, false);

        if (!StorageUtils.checkExternalStorageAvailable()) {
            AlertUtils.showInfoDialog(getActivity(), getString(R.string.noExtStorageAvailable));
        }

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(this).getContext());
        // session manager
        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");

        // init layout
        dropInput = view.findViewById(R.id.drop_txt);
        mRandom = new Random();
        int randomHint = mRandom.nextInt(2);
        dropInput.setHint(textHint[randomHint]);

        dropProgress = view.findViewById(R.id.dropTextProgress);
        dropBtn = view.findViewById(R.id.postTextButton);

        ImageView mediaBtn = view.findViewById(R.id.mediaBtn);
        ImageView recordBtn = view.findViewById(R.id.recordBtn);
        ImageView reactBtn = view.findViewById(R.id.reactBtn);
        mediaImage = view.findViewById(R.id.mediaImage);
        gifImage = view.findViewById(R.id.gifImage);
        videoView = view.findViewById(R.id.mediaVideo);

        spinSpaces = view.findViewById(R.id.spinSpaces);
        spinSpaces.setTitle("Select Space");
        spinSpaces.setPositiveButton("OK");

        spaceItems = new ArrayList<>();
        spinSpaces.getBackground().setColorFilter(getResources().getColor(R.color.contentTextColor), PorterDuff.Mode.SRC_ATOP);
        // spinner item select listener
        spinSpaces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Log.i(TAG, "onItemSelected: ");
                //selectedSpace = parent.getItemAtPosition(position).toString();
                selectedSpace = spaceItems.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        new GetSpaces().execute();

        sheetMeta = view.findViewById(R.id.sheet_meta);

        mediaCard = view.findViewById(R.id.mediaCard);
        recordMeta = view.findViewById(R.id.recordMeta);

        if(gif != null && !gif.equals("")) {
            expand(sheetMeta);
            mediaCard.setVisibility(View.VISIBLE);
            gifImage.setVisibility(View.VISIBLE);
            recordMeta.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            mediaImage.setVisibility(View.GONE);

            Glide
                    .with(this)
                    .asGif()
                    .load(gif)
                    .thumbnail(0.3f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(gifImage);

            dropInput.setText(dropTxt);

            isGif = true;
            isPhoto = false;
            isVideo = false;
            isAudio = false;
        } else {
            // Media Button Press
            mediaBtn.setOnClickListener(v -> {
                String[] PERMISSIONS = {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                if (hasPermissions(requireActivity(), PERMISSIONS)) {
                    ShowPicker();
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PICKER_REQUEST_CODE);
                }

                recordBtn.setClickable(false);
                reactBtn.setClickable(false);
            });


            recordBtn.setOnClickListener(v->{
                expand(sheetMeta);

                mediaBtn.setClickable(false);
                reactBtn.setClickable(false);

                recordMeta.setVisibility(View.VISIBLE);
                mediaCard.setVisibility(View.GONE);

                isAudio = true;

                record_Btn = view.findViewById(R.id.record_btn);
                timer = view.findViewById(R.id.record_timer);

                playerSheet = view.findViewById(R.id.drop_audio_playback);
                playerSheet.setVisibility(View.GONE);

                playBtn = view.findViewById(R.id.player_play_btn);
                playerSeekbar = view.findViewById(R.id.player_seekbar);

                playBtn.setOnClickListener(view1 -> {
                    if(isPlaying){
                        pauseAudio();
                    } else {
                        if(fileToPlay != null){
                            resumeAudio();
                        }
                    }
                });

                record_Btn.setOnClickListener(view1-> {
                    if(isRecording) {
                        //Stop Recording
                        stopRecording();

                        // Change button image and set Recording state to false
                        record_Btn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
                        isRecording = false;
                    } else {
                        //Check permission to record audio
                        if(checkPermissions()) {
                            //Start Recording
                            startRecording();

                            // Change button image and set Recording state to false
                            record_Btn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
                            Toasty.info(requireContext(), "You have 2:30 minutes, GO!", Toasty.LENGTH_SHORT).show();
                            isRecording = true;
                        }
                    }
                });

            });

            reactBtn.setOnClickListener(v->{
                String drop = dropInput.getText().toString().trim();

                mediaBtn.setClickable(false);
                recordBtn.setClickable(false);

                Intent intent = new Intent(requireContext(), ReactionsActivity.class);
                intent.putExtra("drop_txt",drop);
                startActivity(intent);
                customType(requireActivity(), "bottom-to-up");
                dismiss();
            });

        }

        dropBtn.setOnClickListener(v-> {
            String drop = String.valueOf(this.dropInput.getText());

            dropBtn.setEnabled(false);
            dropProgress.show();

            if(drop.isEmpty()) {
                Toasty.info(requireActivity(), "Can't have empty drops", Toasty.LENGTH_SHORT).show();
                dropProgress.hide();
                dropBtn.setEnabled(true);
            } else {
                //Toasty.info(requireActivity(), "Sending...", Toasty.LENGTH_LONG).show();

                if(isPhoto&&finalBitmap!=null) {
                    Toasty.info(requireActivity(), "Dropping...", Toasty.LENGTH_LONG).show();
                    Log.d(TAG, "Drop with image");
                    postPhoto(drop, finalBitmap, username, selectedSpace);
                } else if(isVideo&&finalVideo!=null) {
                    Toasty.info(requireActivity(), "Dropping...", Toasty.LENGTH_LONG).show();
                    Log.d(TAG, "Drop with video");
                    postVideo(drop, finalVideo, username, selectedSpace);
                } else if(isAudio) {
                    if(isAudioReady) {
                        if(inputData!=null) {
                            Toasty.info(requireContext(), "Dropping...", Toasty.LENGTH_SHORT).show();
                            Log.d(TAG, "Drop with audio");
                            postAudio(drop);
                        }
                    } else {
                        Toasty.info(requireContext(), "We're having a little problem with your audio, could you try recording again ?", Toasty.LENGTH_SHORT).show();
                    }
                } else if(isGif) {
                    Toasty.info(requireActivity(), "Dropping...", Toasty.LENGTH_SHORT).show();
                    Log.d(TAG, "Drop with gif");
                    postGif(drop, username, gif, selectedSpace);
                } else {
                    Toasty.info(requireActivity(), "Dropping...", Toasty.LENGTH_LONG).show();
                    Log.d(TAG, "Drop without media");
                    postDrop(drop, username, selectedSpace);
                }
            }
        });

        ImageView deleteMeta = view.findViewById(R.id.delete_meta);
        deleteMeta.setOnClickListener(v-> {
            if(isPhoto) {
                mediaCard.setVisibility(View.GONE);
                mediaImage.setVisibility(View.GONE);
                mediaImage.setImageResource(android.R.color.transparent);
                collapse(sheetMeta);
                finalBitmap = null;
                isPhoto = false;

                mediaBtn.setClickable(true);
                recordBtn.setClickable(true);
                reactBtn.setClickable(true);
            } else if(isVideo) {
                mediaCard.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                collapse(sheetMeta);
                finalVideo = null;
                isVideo = false;

                mediaBtn.setClickable(true);
                recordBtn.setClickable(true);
                reactBtn.setClickable(true);
            } else if(isAudio) {
                recordMeta.setVisibility(View.GONE);
                collapse(sheetMeta);
                timer.setBase(SystemClock.elapsedRealtime());
                timer.stop();
                inputData = null;
                isAudio = false;
                isAudioReady = false;

                mediaBtn.setClickable(true);
                recordBtn.setClickable(true);
                reactBtn.setClickable(true);
            } else if(isGif) {
                mediaCard.setVisibility(View.GONE);
                gifImage.setVisibility(View.GONE);
                gifImage.setImageResource(android.R.color.transparent);
                collapse(sheetMeta);
                gif = null;
                isGif = false;

                mediaBtn.setClickable(true);
                recordBtn.setClickable(true);
                reactBtn.setClickable(true);
            }
        });

        ImageView closeSheet = view.findViewById(R.id.closeSheet);
        closeSheet.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            dismiss();
        });

        return view;
    }

    /**
     * Async task to get all food categories
     * */
    @SuppressLint("StaticFieldLeak")
    private class GetSpaces extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(requireContext());
            pDialog.setMessage("Fetching Spaces..");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ServiceHandler jsonParser = new ServiceHandler();
            if(dropSpace != null && !dropSpace.isEmpty()) {
                String json = jsonParser.makeServiceCall(URL_SPACE+"?space="+dropSpace, ServiceHandler.GET);

                Log.d("Response: ", "> " + json);

                if (json != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(json);
                        if (jsonObj != null) {
                            JSONArray spaces = jsonObj
                                    .getJSONArray("chat_rooms");

                            for (int i = 0; i < spaces.length(); i++) {
                                JSONObject spacesObj = (JSONObject) spaces.get(i);
                                SpinSpaces space = new SpinSpaces(spacesObj.getInt("chat_room_id"),
                                        spacesObj.getString("name"));
                                spaceItems.add(space);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("JSON Data", "Didn't receive any data from server!");
                }
            } else {
                String json = jsonParser.makeServiceCall(URL_SPACES, ServiceHandler.GET);

                Log.d("Response: ", "> " + json);

                if (json != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(json);
                        if (jsonObj != null) {
                            JSONArray spaces = jsonObj
                                    .getJSONArray("chat_rooms");

                            for (int i = 0; i < spaces.length(); i++) {
                                JSONObject spacesObj = (JSONObject) spaces.get(i);
                                SpinSpaces space = new SpinSpaces(spacesObj.getInt("chat_room_id"),
                                        spacesObj.getString("name"));
                                spaceItems.add(space);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("JSON Data", "Didn't receive any data from server!");
                }
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            populateSpinner();
        }

    }

    /**
     * Adding spinner data
     * */
    private void populateSpinner() {
        List<String> lables = new ArrayList<>();

        for (int i = 0; i < spaceItems.size(); i++) {
            lables.add(spaceItems.get(i).getName());
            Log.e("Populate Log", spaceItems.get(i).getName());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_dropdown_item, lables);

        // Drop down layout style - list view with radio button
        //spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinSpaces.setAdapter(spinnerAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        //inflating layout
        view = View.inflate(getContext(), R.layout.drop_text_bottom_sheet, null);

        View extraSpace = view.findViewById(R.id.extraSpace);

        //setting layout with bottom sheet
        bottomSheet.setContentView(view);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        //setting Peek
        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
        //setting min height of bottom sheet
        extraSpace.setMinimumHeight((Resources.getSystem().getDisplayMetrics().heightPixels) / 2);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (BottomSheetBehavior.STATE_EXPANDED == i) {
                    bottomSheetBehavior.setDraggable(false);
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == i) {

                }

                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss();
                }

            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        return bottomSheet;
    }

    private void ShowPicker() {
        Matisse.from(this)
                .choose(MimeType.ofAll())
                .countable(false)
                .maxSelectable(1)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .forResult(PICKER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            // List that will contain the selected files/videos
            List<Uri> mSelected = Matisse.obtainResult(data);

            Log.e(TAG, mSelected.get(0).toString());

            if (mSelected.get(0).toString().contains("image")) {
                //handle image
                try {
                    Uri uri = mSelected.get(0);
                    // You can update this bitmap to your server
                    finalBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);

                    if (finalBitmap != null) {
                        expand(sheetMeta);

                        mediaCard.setVisibility(View.VISIBLE);
                        recordMeta.setVisibility(View.GONE);

                        mediaImage.setVisibility(View.VISIBLE);
                        videoView.setVisibility(View.GONE);
                        gifImage.setVisibility(View.GONE);

                        Glide.with(this)
                                .load(finalBitmap)
                                .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                                .into(mediaImage);

                        isPhoto = true;
                        isVideo = false;
                        isGif = false;
                        isAudio = false;

                        Timber.tag("Image bitmap").i("%s-", finalBitmap.toString());
                    } else {
                        Toasty.error(requireActivity(), "Oops, something went wrong, kindly Try Again",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (mSelected.get(0).toString().contains("video")) {
                //handle video
                try {
                    Uri path = mSelected.get(0);
                    if (path != null) {
                        finalVideo = path;
                        expand(sheetMeta);

                        mediaCard.setVisibility(View.VISIBLE);
                        recordMeta.setVisibility(View.GONE);

                        videoView.setVisibility(View.VISIBLE);
                        mediaImage.setVisibility(View.GONE);
                        gifImage.setVisibility(View.GONE);

                        videoView.setVideoURI(path);
                        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
                        videoView.start();

                        isVideo = true;
                        isPhoto = false;
                        isGif = false;
                        isAudio = false;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Toasty.info(requireActivity(), "Neither Image nor Video was selected", Toasty.LENGTH_SHORT).show();
            }

            Log.d("Matisse", "mSelected: " + mSelected);
        }
    }

    /**
     * Helper method that verifies whether the permissions of a given array are granted or not.
     *
     * @return {Boolean}
     */
    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        playBtn.setImageDrawable(requireActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        isPlaying = false;
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void resumeAudio() {
        mediaPlayer.start();
        playBtn.setImageDrawable(requireActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        isPlaying = true;

        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    private void stopAudio() {
        //Stop The Audio
        playBtn.setImageDrawable(requireActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        isPlaying = false;
        mediaPlayer.stop();
        seekbarHandler.removeCallbacks(updateSeekbar);
        playerSheet.setVisibility(View.GONE);
    }

    private void playAudio(File fileToPlay) {

        mediaPlayer = new MediaPlayer();
        playerSheet.setVisibility(View.VISIBLE);

        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            NoiseSuppressor.create(mediaPlayer.getAudioSessionId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        playBtn.setImageDrawable(requireActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        //Play the audio
        isPlaying = true;
        mediaPlayer.setOnCompletionListener(mp -> stopAudio());

        playerSeekbar.setMax(mediaPlayer.getDuration());

        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                playerSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 500);
            }
        };
    }

    private void stopRecording() {
        //Stop Timer, very obvious
        timer.stop();
        //audioRecordView.recreate();

        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        fileToPlay = new File(mFilePath);
        playAudio(fileToPlay);
        playerSheet.setVisibility(View.VISIBLE);

        try {
            inputData = convert(fileToPlay);
            isAudioReady = true;
        } catch (IOException e) {
            e.printStackTrace();
            Toasty.error(requireContext(), "This recording is corrupted, Record again please.", Toasty.LENGTH_SHORT).show();
        }
    }

    private void startRecording() {
        //Start timer from 0
        setFileNameAndPath();
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
        //startTimerForAmp();

        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(mFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mediaRecorder.setMaxDuration(150000); // 2 minutes, 30 seconds
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioEncodingBitRate(16*44100);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncodingBitRate(192000);

        // Called only if a max duration has been set.
        mediaRecorder.setOnInfoListener((mediaRecorder, what, extra) -> {
            if (what == MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                stopRecording();
            }
        });

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start Recording
        mediaRecorder.start();
    }

    private void setFileNameAndPath() {
        String mFileName = "qweekaudio_" + System.currentTimeMillis();
        mFilePath = StorageUtils.getDirectoryPath(requireContext()) + "/" + mFileName;
    }

    /*
     * Check Audio Permission is granted
     */
    private boolean checkPermissions() {
        //Check permission
        String recordPermission = Manifest.permission.RECORD_AUDIO;
        if (ActivityCompat.checkSelfPermission(requireContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            int PERMISSION_CODE = 21;
            ActivityCompat.requestPermissions(requireActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    /*
     * Process Video Bytes
     */
    private byte[] getVideoBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * Returns a byteArray from a File
     * @param fileToPlay
     * @return
     * @throws IOException
     */
    private byte[] convert(File fileToPlay) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileToPlay);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];

        for (int readNum; (readNum = fis.read(b)) != -1;) {
            bos.write(b, 0, readNum);
        }

        return bos.toByteArray();
    }

    private static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    /**
     * function to post drop text
     * */
    private void postDrop(final String drop, final String username, final int space) {
        // Tag used to cancel the request
        String tag_string_req = "req_post";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DROP_TEXT, response -> {
            Timber.tag(TAG).d("Drop Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Stop animation
                    dropProgress.hide();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    dismiss();

                    // success
                    String sent = jObj.getString("sent");
                    Toasty.success(requireContext(), sent, Toast.LENGTH_SHORT).show();
                } else {
                    // Error in drop. Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Toasty.error(requireContext(),
                            errorMsg, Toast.LENGTH_LONG).show();
                    dropProgress.hide();
                    dropBtn.setEnabled(true);
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                dropProgress.hide();
                dropBtn.setEnabled(true);
            }

        }, error -> {
            Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
            Toasty.error(requireContext(),
                    "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();

            dropProgress.hide();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to drop url
                Map<String, String> params = new HashMap<>();
                params.put("drop", drop);
                params.put("username", username);
                params.put("space", String.valueOf(space));

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

    /**
     * function to post photo
     * */
    private void postPhoto(final String drop, final Bitmap bitmap, final String username, final int space) {
        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, AppConfig.URL_DROP_QWEEKPIC,
                response -> {
                    Timber.tag(TAG).d("Drop Response: %s", new String(response.data));
                    try {
                        JSONObject jObj = new JSONObject(new String(response.data));
                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {
                            // Stop animation
                            dropProgress.hide();
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            dismiss();

                            // success
                            String sent = jObj.getString("sent");
                            Toasty.success(requireActivity(), sent, Toast.LENGTH_SHORT).show();
                        } else {
                            // Error in drop. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toasty.error(requireActivity(),
                                    errorMsg, Toast.LENGTH_SHORT).show();
                            dropProgress.hide();
                            dropBtn.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        // JSON Error
                        e.printStackTrace();
                        Toasty.error(requireActivity(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                        dropProgress.hide();
                        dropBtn.setEnabled(true);
                    }
                },
                error -> {
                    Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                    Toasty.error(requireActivity(),
                            "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();

                    dropProgress.hide();
                    dropBtn.setEnabled(true);
                }) {
            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user", username);
                params.put("drop", drop);
                params.put("space", String.valueOf(space));
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();

                byte[] imageData = ImageUtil.getFileDataFromDrawable(bitmap);
                byte[] imageDataLite = ImageUtil.reduceImageForUpload(imageData);

                params.put("pic", new DataPart("qweeksnap_" + imagename + ".jpg", imageDataLite));
                return params;
            }
        };

        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        volleyMultipartRequest.setRetryPolicy(policy);

        //adding the request to volley
        Volley.newRequestQueue(requireActivity()).add(volleyMultipartRequest);
    }

    /**
     * function to post video
     * */
    private void postVideo(final String drop, final Uri videoFile, final String username, final int space){
        InputStream iStream;
        try {

            iStream = requireActivity().getContentResolver().openInputStream(videoFile);
            assert iStream != null;
            final byte[] inputData = getVideoBytes(iStream);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, AppConfig.URL_DROP_QWEEKVID,
                    response -> {
                        Timber.tag(TAG).d("Drop Response: %s", new String(response.data));
                        rQueue.getCache().clear();
                        try {
                            JSONObject jObj = new JSONObject(new String(response.data));
                            boolean error = jObj.getBoolean("error");

                            // Check for error node in json
                            if (!error) {
                                // Stop animation
                                dropProgress.hide();
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                dismiss();

                                // success
                                String sent = jObj.getString("sent");
                                Toasty.success(requireActivity(), sent, Toast.LENGTH_SHORT).show();
                            } else {
                                // Error in drop. Get the error message
                                String errorMsg = jObj.getString("error_msg");
                                Toasty.error(requireActivity(),
                                        errorMsg, Toast.LENGTH_SHORT).show();
                                dropProgress.hide();
                                dropBtn.setEnabled(true);
                            }
                        } catch (JSONException e) {
                            // JSON Error
                            e.printStackTrace();
                            Toasty.error(requireActivity(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                            dropProgress.hide();
                            dropBtn.setEnabled(true);
                        }
                    },
                    error -> {Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                        Toasty.error(requireActivity(),
                                "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();

                        dropProgress.hide();
                        dropBtn.setEnabled(true);
                    }) {

                /*
                 * If you want to add more parameters with the image
                 * you can do it here
                 * here we have only one parameter with the image
                 * which is tags
                 * */
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("user", username);
                    params.put("drop", drop);
                    params.put("space", String.valueOf(space));
                    return params;
                }

                /*
                 *pass files using below method
                 * */
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    long videoname = System.currentTimeMillis();

                    params.put("vid", new DataPart("qweeksnap_" + videoname + ".mp4" ,inputData));
                    return params;
                }
            };


            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            rQueue = Volley.newRequestQueue(requireActivity());
            rQueue.add(volleyMultipartRequest);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * function to post gif
     * */
    private void postGif(final String drop, final String username, final String url, final int space) {
        // Tag used to cancel the request
        String tag_string_req = "req_post";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DROP_GIF, response -> {
            Timber.tag(TAG).d("Drop Response: %s", response);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Stop animation
                    dropProgress.hide();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    dismiss();

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    getContext().startActivity(intent);

                    // success
                    String sent = "Dropped!";
                    Toasty.success(requireActivity(), sent, Toast.LENGTH_SHORT).show();
                } else {
                    // Error in drop. Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Toasty.error(requireActivity(),
                            errorMsg, Toast.LENGTH_LONG).show();
                    dropProgress.hide();
                    dropBtn.setEnabled(true);
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toasty.error(requireActivity(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                dropProgress.hide();
                dropBtn.setEnabled(true);
            }

        }, error -> {
            Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
            Toasty.error(requireActivity(),
                    "Apollo, we have a problem !", Toast.LENGTH_SHORT).show();
            dropProgress.hide();
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to drop url
                Map<String, String> params = new HashMap<>();
                params.put("drop", drop);
                params.put("username", username);
                params.put("url", url);
                params.put("space", String.valueOf(space));

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

    /**
     * Upload Audio
     */
    private void postAudio(String drop) {
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, AppConfig.URL_DROP_AUDIO,
                response -> {
                    Timber.tag(TAG).d("Drop Response: %s", new String(response.data));
                    rQueue.getCache().clear();
                    try {
                        JSONObject jObj = new JSONObject(new String(response.data));
                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {
                            // Stop animation
                            dropProgress.hide();
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            dismiss();

                            // success
                            String sent = jObj.getString("sent");
                            Toasty.success(requireContext(), sent, Toast.LENGTH_SHORT).show();
                        } else {
                            // Error in drop. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toasty.error(requireContext(),
                                    errorMsg, Toast.LENGTH_SHORT).show();
                            dropProgress.hide();
                            dropBtn.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        // JSON Error
                        e.printStackTrace();
                        Toasty.error(requireContext(), "Json error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dropProgress.hide();
                        dropBtn.setEnabled(true);
                    }
                },
                error -> {Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                    Toasty.error(requireContext(),
                            Objects.requireNonNull(error.getMessage()), Toast.LENGTH_SHORT).show();

                    dropProgress.hide();
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user", username);
                params.put("drop", drop);
                return params;
            }

            /*
             *pass files using below method
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long audioname = System.currentTimeMillis();

                params.put("audio", new DataPart("qweekaudio_" + audioname + ".wav" ,inputData));
                return params;
            }
        };


        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue = Volley.newRequestQueue(requireContext());
        rQueue.add(volleyMultipartRequest);

    }

    @Override
    public void onStop() {
        super.onStop();
        if(isRecording){
            stopRecording();
            //audioRecordView.recreate();
        }
        if(isPlaying) {
            stopAudio();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
