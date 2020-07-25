package com.creator.qweekdots.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.prefs.DarkModePrefManager;
import com.creator.qweekdots.utils.AlertUtils;
import com.creator.qweekdots.utils.StorageUtils;
import com.creator.qweekdots.volley.VolleyMultipartRequest;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.visualizer.amplitude.AudioRecordView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;

public class DropAudio extends Fragment {
    private static final String TAG = DropAudio.class.getSimpleName();
    private String username;

    private Uri fileUri;
    private String mFileName = null;
    private String mFilePath = null;

    private RequestQueue rQueue;

    private FABProgressCircle dropProgress;
    private FloatingActionButton dropBtn;

    private byte[] inputData;
    private boolean isAudioReady = false;

    private TextView dropTitle;
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

    private SeekBar playerSeekbar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;

    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;

    private TimerTask mIncrementTimerTask = null;

    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!StorageUtils.checkExternalStorageAvailable()) {
            AlertUtils.showInfoDialog(getActivity(), getString(R.string.noExtStorageAvailable));
        }

    }

    @Override
    public  View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        View rootView = inflater.inflate(R.layout.drop_audio, container, false);

        if(new DarkModePrefManager(requireActivity()).isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(this).getContext());
        // session manager

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        username = user.get("username");

        dropTitle = rootView.findViewById(R.id.drop_title);
        recordBtn = rootView.findViewById(R.id.record_btn);
        timer = rootView.findViewById(R.id.record_timer);
        //audioRecordView = rootView.findViewById(R.id.audioRecordView);

        ConstraintLayout playerSheet = rootView.findViewById(R.id.drop_audio_playback);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        playBtn = rootView.findViewById(R.id.player_play_btn);
        playerSeekbar = rootView.findViewById(R.id.player_seekbar);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    stopAudio();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //We cant do anything here for this app
            }
        });

        playBtn.setOnClickListener(v -> {
            if(isPlaying){
                pauseAudio();
            } else {
                if(fileToPlay != null){
                    resumeAudio();
                }
            }
        });

        recordBtn.setOnClickListener(v-> {
            if(isRecording) {
                //Stop Recording
                stopRecording();

                // Change button image and set Recording state to false
                recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
                isRecording = false;
            } else {
                //Check permission to record audio
                if(checkPermissions()) {
                    //Start Recording
                    startRecording();

                    // Change button image and set Recording state to false
                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
                    Toasty.info(requireContext(), "You have 2:30 minutes, GO!", Toasty.LENGTH_SHORT).show();
                    isRecording = true;
                }
            }
        });

        dropProgress = rootView.findViewById(R.id.dropAudioProgress);
        dropBtn = rootView.findViewById(R.id.postAudioButton);

        dropBtn.setOnClickListener(v-> {
            if(isAudioReady) {
                if(inputData!=null) {
                    dropBtn.setEnabled(false);
                    Toasty.info(requireContext(), "Dropping...", Toasty.LENGTH_SHORT).show();
                    uploadAudio();
                }
            } else {
                Toasty.info(requireContext(), "We're having a little problem with your audio, could you try recording again ?", Toasty.LENGTH_SHORT).show();
            }
        });

        return rootView;
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
    }

    private void playAudio(File fileToPlay) {

        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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

        fileUri = Uri.fromFile(fileToPlay);
        Log.d("Audio", fileUri.toString());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

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
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        mediaRecorder.setMaxDuration(150000); // 2 minute, 30 seconds
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

    private void startTimerForAmp() {
        Timer mTimer = new Timer();

        // Increment seconds.
        mElapsedMillis = 0;
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedMillis += 100;
                int currentMaxAmplitude = mediaRecorder.getMaxAmplitude();
                //audioRecordView.update(currentMaxAmplitude);

            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 0, 100);
    }

    private void setFileNameAndPath() {
        mFileName = "qweekaudio_" + System.currentTimeMillis();
        mFilePath = StorageUtils.getDirectoryPath(requireContext()) + "/" + mFileName;
        Log.d(TAG, "mFilePath =  " + mFilePath);
    }

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

    private void uploadAudio(){
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
                            dropBtn.setEnabled(true);

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
                return params;
            }

            /*
             *pass files using below method
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long audioname = System.currentTimeMillis();

                params.put("audio", new DataPart("qweekaudio_" + audioname + ".m4a" ,inputData));
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

    public void onClick(View v) {
        assert getFragmentManager() != null;
        getFragmentManager().popBackStack(); // or super.finish();
    }
}
