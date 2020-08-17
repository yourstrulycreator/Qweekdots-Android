package com.creator.qweekdots.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.creator.qweekdots.R;
import com.creator.qweekdots.app.AppConfig;
import com.creator.qweekdots.helper.SQLiteHandler;
import com.creator.qweekdots.qweekcamera.BitmapStickerIcon;
import com.creator.qweekdots.qweekcamera.DeleteIconEvent;
import com.creator.qweekdots.qweekcamera.DrawableSticker;
import com.creator.qweekdots.qweekcamera.Sticker;
import com.creator.qweekdots.qweekcamera.StickerView;
import com.creator.qweekdots.qweekcamera.TextSticker;
import com.creator.qweekdots.qweekcamera.ZoomIconEvent;
import com.creator.qweekdots.utils.CircleProgressBar;
import com.creator.qweekdots.utils.GifSizeFilter;
import com.creator.qweekdots.utils.Glide4Engine;
import com.creator.qweekdots.utils.ImageUtil;
import com.creator.qweekdots.volley.VolleyMultipartRequest;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class DropQweekSnap extends Fragment {
    private static final String TAG = DropQweekSnap.class.getSimpleName();
    //
    private SurfaceView preview = null;
    private SurfaceHolder previewHolder = null;
    private Camera camera = null;
    private Camera.Parameters params;
    private boolean inPreview = false;
    private boolean cameraConfigured = false;
    private boolean isRecording;
    private boolean isFlashOn;
    private MediaRecorder mediaRecorder;
    private static int currentCameraId = 0;
    private Bitmap rotatedBitmap;
    private RelativeLayout captureMedia;
    private FrameLayout editMedia;
    private CircleProgressBar customButton;
    private ImageView flashButton;
    private ImageView capturedImage;
    private VideoView videoView;
    private ImageView goBack;
    private int VideoSeconds = 1;
    //
    private static final int PICKER_REQUEST_CODE = 1;
    //
    private StickerView stickerView;
    private EditText editText;
    private LinearLayout editTextLayout;
    private int textColor;
    private InputMethodManager keyboard;
    private LinearLayout selectSticker;
    private StickerAdapter stickerAdapter;
    private GridView stickersGrid;
    private ImageView addSticker;
    private String defaultVideo;
    private Integer[]  sticker_images = {
            R.drawable.sticker_00, R.drawable.sticker_0,
            R.drawable.sticker_1, R.drawable.sticker_2, R.drawable.sticker_3, R.drawable.sticker_4,
            R.drawable.sticker_5, R.drawable.sticker_6, R.drawable.sticker_7, R.drawable.sticker_8,
            R.drawable.sticker_9, R.drawable.sticker_10, R.drawable.sticker_11, R.drawable.sticker_12,
            R.drawable.sticker_13, R.drawable.sticker_14, R.drawable.sticker_15, R.drawable.sticker_16,
            R.drawable.sticker_17, R.drawable.sticker_18, R.drawable.sticker_19, R.drawable.sticker_20,
            R.drawable.sticker_21, R.drawable.sticker_22, R.drawable.sticker_23, R.drawable.sticker_24,
            R.drawable.sticker_25, R.drawable.sticker_26, R.drawable.sticker_27, R.drawable.sticker_28,
            R.drawable.sticker_29, R.drawable.sticker_30, R.drawable.sticker_31, R.drawable.sticker_32,
            R.drawable.sticker_33, R.drawable.sticker_34, R.drawable.sticker_35, R.drawable.sticker_36,
            R.drawable.sticker_37, R.drawable.sticker_38, R.drawable.sticker_39, R.drawable.sticker_40,
            R.drawable.sticker_41, R.drawable.sticker_42, R.drawable.sticker_43, R.drawable.sticker_44,
            R.drawable.sticker_45, R.drawable.sticker_46, R.drawable.sticker_47, R.drawable.sticker_48,
            R.drawable.sticker_49, R.drawable.sticker_50, R.drawable.sticker_51, R.drawable.sticker_52,
            R.drawable.sticker_53, R.drawable.sticker_54, R.drawable.sticker_55, R.drawable.sticker_56,
            R.drawable.sticker_57, R.drawable.sticker_58, R.drawable.sticker_59, R.drawable.sticker_60,
            R.drawable.sticker_61, R.drawable.sticker_62, R.drawable.sticker_63, R.drawable.sticker_64,
            R.drawable.sticker_65, R.drawable.sticker_66, R.drawable.sticker_67, R.drawable.sticker_68,
            R.drawable.sticker_69, R.drawable.sticker_70, R.drawable.sticker_71, R.drawable.sticker_72,
            R.drawable.sticker_73, R.drawable.sticker_74, R.drawable.sticker_75, R.drawable.sticker_76,
            R.drawable.sticker_77, R.drawable.sticker_77, R.drawable.sticker_78, R.drawable.sticker_79,
            R.drawable.sticker_80, R.drawable.sticker_81, R.drawable.sticker_82, R.drawable.sticker_83,
            R.drawable.sticker_84, R.drawable.sticker_85, R.drawable.sticker_86, R.drawable.sticker_87,
            R.drawable.sticker_88, R.drawable.sticker_89, R.drawable.sticker_90, R.drawable.sticker_91,
            R.drawable.sticker_92, R.drawable.sticker_93, R.drawable.sticker_94, R.drawable.sticker_95,
            R.drawable.sticker_96, R.drawable.sticker_96, R.drawable.sticker_97, R.drawable.sticker_98,
            R.drawable.sticker_99, R.drawable.sticker_100, R.drawable.sticker_101, R.drawable.sticker_102,
            R.drawable.sticker_103, R.drawable.sticker_104, R.drawable.sticker_105, R.drawable.sticker_106,
            R.drawable.sticker_107, R.drawable.sticker_108, R.drawable.sticker_109, R.drawable.sticker_110,
            R.drawable.sticker_111, R.drawable.sticker_112, R.drawable.sticker_113, R.drawable.sticker_114,
            R.drawable.sticker_115, R.drawable.sticker_116, R.drawable.sticker_117, R.drawable.sticker_118,
            R.drawable.sticker_119, R.drawable.sticker_120, R.drawable.sticker_121, R.drawable.sticker_122,
            R.drawable.sticker_123, R.drawable.sticker_124, R.drawable.sticker_125, R.drawable.sticker_126,
            R.drawable.sticker_127, R.drawable.sticker_128, R.drawable.sticker_129, R.drawable.sticker_130,
            R.drawable.sticker_131, R.drawable.sticker_132, R.drawable.sticker_133, R.drawable.sticker_134,
            R.drawable.sticker_135, R.drawable.sticker_136, R.drawable.sticker_137, R.drawable.sticker_138,
            R.drawable.sticker_139, R.drawable.sticker_140, R.drawable.sticker_141, R.drawable.sticker_142,
            R.drawable.sticker_143, R.drawable.sticker_144, R.drawable.sticker_145, R.drawable.sticker_146,
            R.drawable.sticker_147, R.drawable.sticker_148, R.drawable.sticker_149, R.drawable.sticker_150,
            R.drawable.sticker_151, R.drawable.sticker_152, R.drawable.sticker_153, R.drawable.sticker_154,
            R.drawable.sticker_155, R.drawable.sticker_156, R.drawable.sticker_157, R.drawable.sticker_158,
            R.drawable.sticker_159, R.drawable.sticker_160, R.drawable.sticker_161, R.drawable.sticker_162,
            R.drawable.sticker_163, R.drawable.sticker_164, R.drawable.sticker_165, R.drawable.sticker_166,
            R.drawable.sticker_167, R.drawable.sticker_168, R.drawable.sticker_169, R.drawable.sticker_170,
            R.drawable.sticker_171, R.drawable.sticker_172, R.drawable.sticker_173, R.drawable.sticker_174,
            R.drawable.sticker_175, R.drawable.sticker_176, R.drawable.sticker_177, R.drawable.sticker_178,
            R.drawable.sticker_179, R.drawable.sticker_180, R.drawable.sticker_181, R.drawable.sticker_182,
            R.drawable.sticker_183, R.drawable.sticker_184,
    };
    //
    private static final int MEDIA_TYPE_IMAGE = 4;
    private static final int MEDIA_TYPE_VIDEO = 5;
    //
    private FABProgressCircle dropProgress;
    private FloatingActionButton dropBtn;
    //
    private Uri mMediaUri, finalVideo;
    private View rootView;
    private String username;
    //
    private RequestQueue rQueue;


    @Override

    public void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);
        //this.onBackPressed();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.drop_qweeksnap, container, false);

        // SqLite database handler
        SQLiteHandler db = new SQLiteHandler(Objects.requireNonNull(this).getContext());
        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        username = user.get("username");

        // Set Boolean values at start
        isRecording = false;
        isFlashOn = false;

        /*
         * Init Layouts
         */
        captureMedia = rootView.findViewById(R.id.camera_view);

        // Preview for Capture
        preview = rootView.findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        preview.setOnClickListener(v -> FocusCamera());

        // Upload Media From Gallery
        ImageView uploadMediaBtn = rootView.findViewById(R.id.media_upload_media);
        uploadMediaBtn.setOnClickListener(v-> {
            String[] PERMISSIONS = {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            if(hasPermissions(getActivity(), PERMISSIONS)) {
                ShowPicker();
            } else {
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PICKER_REQUEST_CODE);
            }
        });

        // Switch Camera From Front Facing to Back and vice versa
        ImageView switchCameraBtn = rootView.findViewById(R.id.img_switch_camera);
        switchCameraBtn.setOnClickListener(v -> switchCamera());

        // Flash Toggle Button
        flashButton = rootView.findViewById(R.id.img_flash_control);
        flashButton.setOnClickListener(v1 -> FlashControl());

        // Save Media to Storage/Gallery
        ImageView saveMediaBtn = rootView.findViewById(R.id.save_media);
        saveMediaBtn.setOnClickListener(v -> {
            try {
                saveMedia();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Cancel Capture Button
        ImageView editCaptureSwitchBtn = rootView.findViewById(R.id.cancel_capture);
        editCaptureSwitchBtn.setOnClickListener(v -> EditCaptureSwitch());

        // Custom Button/Progress Button for Image or Video capture
        customButton = rootView.findViewById(R.id.custom_progressBar);
        customButton.setOnTouchListener(new View.OnTouchListener() {
            private Timer timer = new Timer();
            private long LONG_PRESS_TIMEOUT = 1000;
            private boolean wasLong = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Timber.tag(getClass().getName()).d("touch event: %s", event.toString());
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // touch & hold started
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            wasLong = true;
                            // touch & hold was long
                            Timber.tag("Click").i("touch & hold was long");
                            VideoCountDown.start();
                            try {
                                startRecording();
                            } catch (IOException e) {
                                String message = e.getMessage();
                                Timber.tag(null).i("Problem %s", message);
                                mediaRecorder.release();
                                e.printStackTrace();
                            }
                        }
                    }, LONG_PRESS_TIMEOUT);
                    return true;
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // touch & hold stopped
                    timer.cancel();
                    if(!wasLong){
                        // touch & hold was short
                        Timber.tag("Click").i("touch & hold was short");
                        if (isFlashOn && currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            params = camera.getParameters();
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            camera.setParameters(params);

                            camera.autoFocus((success, camera) -> takePicture());

                        } else {
                            takePicture();
                        }
                    } else {
                        stopRecording();
                        VideoCountDown.cancel();
                        VideoSeconds = 1;
                        customButton.setProgressWithAnimation(0);
                        wasLong = false;
                    }
                    timer = new Timer();
                    return true;
                }

                return false;
            }
        });

        // Captured Media Layouts
        capturedImage = rootView.findViewById(R.id.captured_image);
        videoView = rootView.findViewById(R.id.captured_video);

        // Drop Button
        // Drop QweekSnap
        dropProgress = rootView.findViewById(R.id.dropSnapProgress);
        dropBtn = rootView.findViewById(R.id.upload_media);
        dropBtn.setOnClickListener(v -> {
            dropBtn.setEnabled(false);
            dropProgress.show();
            Toasty.info(requireContext(), "Dropping...", Toasty.LENGTH_SHORT).show();
            if(!videoView.isShown()) {
                if(rotatedBitmap != null) {
                    uploadBitmap(rotatedBitmap, username);
                    Timber.tag(TAG).d("Uploading Photo");
                }
            } else {
                if(finalVideo != null) {
                    uploadVideo(finalVideo);
                    Timber.tag(TAG).d("Uploading Video");
                }
            }
        });

        //Edit Media Button
        editMedia = rootView.findViewById(R.id.edit_media);
        editMedia.setOnClickListener(view -> {
            //StickerView.invalidate();
        });

        // Stickers
        //////////////////////////////
        stickerAdapter = new StickerAdapter(getActivity());

        keyboard = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        editText = rootView.findViewById(R.id.editText);
        editTextLayout = rootView.findViewById(R.id.editTextLayout);
        ImageView addText = rootView.findViewById(R.id.add_text);
        addSticker = rootView.findViewById(R.id.add_stickers);
        ImageView hideStickers = rootView.findViewById(R.id.hide_sticker_options);

        editTextLayout.setOnClickListener(view -> showHideEditText());
        addText.setOnClickListener(view -> showHideEditText());
        addSticker.setOnClickListener(view -> stickerOptions());
        hideStickers.setOnClickListener(view -> stickerOptions());

        editTextLayout = rootView.findViewById(R.id.editTextLayout);
        selectSticker  = rootView.findViewById(R.id.select_sticker);
        stickersGrid = rootView.findViewById(R.id.sticker_grid);

        textColor = Color.WHITE;

        // Text Sticker Color Selector
        LinearGradient testGradient = new LinearGradient(0.f, 0.f, 700.f, 0.0f,
                new int[] {0xFF000000, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF,
                        0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF}, null, Shader.TileMode.CLAMP);
        ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
        shapeDrawable.getPaint().setShader(testGradient);
        SeekBar seekBar = rootView.findViewById(R.id.seekbar_font);
        final View colorSelected = rootView.findViewById(R.id.colorSelected);
        seekBar.setProgressDrawable(shapeDrawable);
        seekBar.setMax(256*7-1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int r = 0;
                    int g = 0;
                    int b = 0;

                    if(progress < 256) {
                        b = progress;
                    } else if (progress < 256*2) {
                        g = progress%256;
                        b = 256 - progress%256;
                    } else if (progress < 256 * 4) {
                        r = progress%256;
                        g = 256 - progress%256;
                        b = 256 - progress%256;
                    } else if (progress < 256 * 5) {
                        r = 255;
                        g = 0;
                        b = progress%256;
                    } else if (progress < 256 * 6) {
                        r = 255;
                        g = progress%256;
                        b = 256 - progress%256;
                    } else if (progress < 256 * 7) {
                        r = 255;
                        g = 255;
                        b = progress%256;
                    }
                    colorSelected.setBackgroundColor(Color.argb(255, r, g, b));
                    textColor = Color.argb(255, r, g, b);
                    editText.setTextColor(textColor);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        stickersGrid.setOnItemClickListener((parent, view, position, id) -> {
            //String sticker_bitmap = sticker_links[position];
            ImageView iv = view.findViewById(R.id.sticker_grid_item);
            Drawable drawable = iv.getDrawable();
            if (drawable != null) {
                stickerView.addSticker(new DrawableSticker(drawable));
                stickerOptions();
            }
        });


        /*
         * Video Pre-Processing
         */

        // setting dir and VideoFile value for Video capture
        File dir = new File(String.valueOf(requireContext().getExternalFilesDir(null)));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        defaultVideo =  dir + "/defaultVideo.mp4.nomedia";
        File createDefault = new File(defaultVideo);
        if (!createDefault.isFile()) {
            try {
                FileWriter writeDefault = new FileWriter(createDefault);
                writeDefault.append("yy");
                writeDefault.close();
                writeDefault.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        goBack = rootView.findViewById(R.id.goBack);
        goBack.setOnClickListener(v-> goBack());

        return rootView;
    }

    // Show Picker for Media Upload from Gallery using Matisse
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

    // Countdown timer for Video Capture
    private CountDownTimer VideoCountDown = new CountDownTimer(10000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            VideoSeconds++;
            int VideoSecondsPercentage = VideoSeconds * 10;
            customButton.setProgressWithAnimation(VideoSecondsPercentage);
        }

        @Override
        public void onFinish() {
            stopRecording();
            customButton.setProgress(0);
            VideoSeconds = 0;
        }
    };

    // Focus Camera for Capture
    private void FocusCamera(){
        if (camera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {} else {
            camera.autoFocus((success, camera) -> { });
        }
    }

    private void takePicture() {
        params = camera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();

        List<Integer> list = new ArrayList<>();
        for (Camera.Size size : params.getSupportedPictureSizes()) {
            Timber.tag("ASDF").i("Supported Picture: " + size.width + "x" + size.height);
            list.add(size.height);
        }

        Camera.Size cs = sizes.get(closest(1080, list));
        Timber.tag("Width x Height").i(cs.width + "x" + cs.height);
        params.setPictureSize(cs.width, cs.height); //1920, 1080
        //params.setRotation(90);
        camera.setParameters(params);
        camera.takePicture(null, null, (data, camera) -> {
            Bitmap bitmap;
            Matrix matrix = new Matrix();

            if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                matrix.postRotate(90);
            } else {
                Matrix matrixMirrory = new Matrix();
                float[] mirrory = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                matrixMirrory.setValues(mirrory);
                matrix.postConcat(matrixMirrory);
                matrix.postRotate(90);
            }
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotatedBitmap != null) {
                mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                if (mMediaUri == null) {
                    // display an error
                    Toasty.error(requireActivity(), R.string.error_external_storage,
                            Toast.LENGTH_LONG).show();
                } else {
                    setStickerView(0);
                    capturedImage.setVisibility(View.VISIBLE);
                    capturedImage.setImageBitmap(rotatedBitmap);
                    editMedia.setVisibility(View.VISIBLE);
                    captureMedia.setVisibility(View.GONE);
                    goBack.setVisibility(View.GONE);

                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(params);
                }
                Timber.tag("Image bitmap").i("%s-", rotatedBitmap.toString());
            } else {
                Toasty.error(requireActivity(), "Oops, something went wrong, kindly Try Again",
                        Toast.LENGTH_LONG).show();
            }

        });
    }

    private void startRecording() throws IOException {
        if (camera == null) {
            camera = Camera.open(currentCameraId);
            Timber.tag("Camera").i("Camera open");
        }
        params = camera.getParameters();

        if (isFlashOn && currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
        }

        mediaRecorder = new MediaRecorder();
        camera.lock();
        camera.unlock();
        // Please maintain sequence of following code.
        // If you change sequence it will not work.
        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setPreviewDisplay(previewHolder.getSurface());

        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mediaRecorder.setOrientationHint(270);
        } else {
            mediaRecorder.setOrientationHint(setCameraDisplayOrientation(requireActivity(), currentCameraId));
        }
        mediaRecorder.setMaxDuration(150000); // 2 minutes, 30 seconds
        mediaRecorder.setVideoEncodingBitRate(3000000);
        mediaRecorder.setVideoFrameRate(30);

        List<Integer> list = new ArrayList<>();

        List<Camera.Size> VidSizes = params.getSupportedVideoSizes();
        if (VidSizes == null) {
            Timber.tag("Size length").i("is null");
            mediaRecorder.setVideoSize(1920,1080);
        } else {
            Timber.tag("Size length").i("is NOT null");
            for (Camera.Size sizesx : params.getSupportedVideoSizes()) {
                Timber.tag("ASDF").i("Supported Video: " + sizesx.width + "x" + sizesx.height);
                list.add(sizesx.height);
            }
            Camera.Size cs = VidSizes.get(closest(1080, list));
            Timber.tag("Width x Height").i(cs.width + "x" + cs.height);
            mediaRecorder.setVideoSize(cs.width,cs.height);
        }

        mediaRecorder.setOutputFile(defaultVideo);
        mediaRecorder.prepare();
        isRecording = true;
        mediaRecorder.start();
    }

    private void stopRecording() {
        if (isRecording) {
            try {
                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);

                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                playVideo();
            } catch (RuntimeException stopException) {
                Timber.tag("Stop Recoding").i("Too short video");
                takePicture();
            }
            camera.lock();
        } else {
            Timber.tag("Stop Recoding").i("isRecording is true");
        }
    }

    private void playVideo() {
        videoView.setVisibility(View.VISIBLE);
        editMedia.setVisibility(View.VISIBLE);
        captureMedia.setVisibility(View.GONE);
        mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        if (mMediaUri == null) {
            // display an error
            Toasty.error(requireActivity(), R.string.error_external_storage,
                    Toast.LENGTH_LONG).show();
        } else {
            Uri video = Uri.parse(defaultVideo);
            finalVideo = Uri.parse(defaultVideo);
            videoView.setVideoURI(video);
            videoView.setOnPreparedListener(mp -> mp.setLooping(true));
            videoView.start();
            preview.setVisibility(View.INVISIBLE);
            setStickerView(1);
            addSticker.setVisibility(View.GONE);
            goBack.setVisibility(View.GONE);
        }
    }

    /**
     * Function to save Media Captured to Gallery/Storage
     * @throws IOException
     */
    private void saveMedia() throws IOException {
        if (!videoView.isShown()) {
            Toasty.info(requireActivity(), "Saving...",Toast.LENGTH_SHORT).show();

            String snapLocation = Environment.DIRECTORY_PICTURES + File.separator + "Qweekdots";

            @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("ddMMyyHHmm").format(new Date());
            String snapName = "qweeksnap-" + timeStamp + ".jpg";

            final ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, snapName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, snapLocation);
            }

            final ContentResolver resolver = requireActivity().getContentResolver();

            OutputStream stream = null;
            Uri uri = null;

            try {
                final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                uri = resolver.insert(contentUri, contentValues);

                if(uri == null) {
                    throw new IOException("Failed to create new QweekSnap");
                }

                stream = resolver.openOutputStream(uri);
                if(stream == null) {
                    throw new IOException("Failed to get output stream.");
                }
                if(!stickerView.createBitmap().compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                    throw new IOException("Failed to save bitmap");
                }
                // Success
                Toasty.success(requireActivity(), "Saved!",Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                if(uri != null) {
                    resolver.delete(uri, null, null);
                }
                Toasty.error(requireActivity(), "Error saving!",Toast.LENGTH_LONG).show();
                throw e;
            } finally {
                if(stream != null) {
                    stream.close();
                }
            }
        } else {
            if (defaultVideo != null) {
                Toasty.info(requireActivity(), "Saving...",Toast.LENGTH_SHORT).show();

                String snapLocation = Environment.DIRECTORY_PICTURES + File.separator + "Qweekdots";

                @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("ddMMyyHHmm").format(new Date());
                String snapTitle = "qweeksnap-" + timeStamp;
                String snapName = "qweeksnap-" + timeStamp + ".mp4";

                final ContentValues contentValues = new ContentValues();

                long dateTaken = System.currentTimeMillis();

                contentValues.put(MediaStore.Video.VideoColumns.TITLE, snapTitle);
                contentValues.put(MediaStore.Video.VideoColumns.DISPLAY_NAME, snapName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Video.VideoColumns.DATE_TAKEN, dateTaken);
                }
                contentValues.put(MediaStore.Video.VideoColumns.MIME_TYPE, "video/mp4");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Video.VideoColumns.RELATIVE_PATH, snapLocation);
                }

                final ContentResolver resolver = requireActivity().getContentResolver();

                File from = new File(defaultVideo);
                InputStream in = new FileInputStream(from);
                OutputStream stream = null;
                Uri uri = null;

                try {
                    final Uri contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    uri = resolver.insert(contentUri, contentValues);

                    if(uri == null) {
                        throw new IOException("Failed to create new QweekSnap");
                    }

                    stream = resolver.openOutputStream(uri);
                    if(stream == null) {
                        throw new IOException("Failed to get output stream.");
                    }
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        stream.write(buf, 0, len);
                    }
                    in.close();
                    stream.close();
                    // Success
                    Toasty.success(requireActivity(), "Saved!",Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    if(uri != null) {
                        resolver.delete(uri, null, null);
                    }
                    Toasty.error(requireActivity(), "Error saving!",Toast.LENGTH_LONG).show();

                    throw e;
                } finally {
                    if(stream != null) {
                        stream.close();
                    }
                }
            } else {
                Toasty.error(requireActivity(), "Error saving!",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void FlashControl() {
        Timber.tag("Flash").i("Flash button clicked!");
        boolean hasFlash = requireActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!hasFlash) {
            AlertDialog alert = new AlertDialog.Builder(getActivity())
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", (dialog, which) -> requireActivity().finish());
            alert.show();
        } else {

            if (!isFlashOn) {
                isFlashOn = true;
                flashButton.setImageResource(R.drawable.ic_flash_on_shadow_48dp);
                Timber.tag("Flash").i("Flash On");

            } else {
                isFlashOn = false;
                flashButton.setImageResource(R.drawable.ic_flash_off_shadow_48dp);
                Timber.tag("Flash").i("Flash Off");
            }
        }
    }

    private void switchCamera() {
        if (!isRecording) {
            if (Camera.getNumberOfCameras() != 1) {
                camera.release();
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                camera = Camera.open(currentCameraId);
                try {
                    camera.setPreviewDisplay(previewHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startPreview();
            }
        } else {
            Timber.tag("Switch Camera").i("isRecording true");
        }
    }

    private void EditCaptureSwitch() {
        preview.setVisibility(View.VISIBLE);
        captureMedia.setVisibility(View.VISIBLE);
        startPreview();
        capturedImage.setVisibility(View.GONE);
        editMedia.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();
        camera=Camera.open(currentCameraId);
        try {
            camera.setPreviewDisplay(previewHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startPreview();
        //FocusCamera();
    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }

        camera.release();
        camera = null;
        inPreview = false;

        super.onPause();
    }

    private int closest(int of, List<Integer> in) {
        int min = Integer.MAX_VALUE;
        int closest = of;
        int position=0;
        int i = 0;

        for (int v: in) {
            final int diff = Math.abs(v - of);
            i++;

            if(diff < min) {
                min = diff;
                closest = v;
                position = i;
            }
        }
        int rePos = position - 1;
        Timber.tag("Value").i(closest + "-" + rePos);
        return rePos;
    }

    private void initPreview() {
        if (camera!=null && previewHolder.getSurface()!=null) {
            try {
                camera.stopPreview();
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Timber.tag("Preview:surfaceCallback").e(t, "Exception in setPreviewDisplay()");
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }

            if (!cameraConfigured) {

                Camera.Parameters parameters = camera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();

                List<Integer> list = new ArrayList<>();
                for (int i=0; i < sizes.size(); i++) {
                    Timber.tag("ASDF").i("Supported Preview: " + sizes.get(i).width + "x" + sizes.get(i).height);
                    list.add(sizes.get(i).width);
                }
                Camera.Size cs = sizes.get(closest(1920, list));

                Timber.tag("Width x Height").i(cs.width + "x" + cs.height);

                parameters.setPreviewSize(cs.width, cs.height);
                camera.setParameters(parameters);
                cameraConfigured=true;
            }
        }
    }

    /*
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }
     */

    private void startPreview() {
        if (cameraConfigured && camera!=null) {
            camera.setDisplayOrientation(setCameraDisplayOrientation(requireActivity(), currentCameraId));
            camera.startPreview();
            inPreview=true;
            goBack.setVisibility(View.VISIBLE);
        }
    }

    private int setCameraDisplayOrientation(Activity activity, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            initPreview();
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

    /**
     * Sticker Functions
     * @param stickerV
     */

    // Set Sticker View after Media Capture
    private void setStickerView(int stickerV) {
        Timber.tag("setStickerView").i("Called");
        if (stickerV == 0) {
            stickerView = rootView.findViewById(R.id.sticker_view);
            StickerView SvVideo = rootView.findViewById(R.id.sticker_view1);
            SvVideo.setBackgroundColor(0);
        } else if (stickerV == 1) {
            stickerView = rootView.findViewById(R.id.sticker_view1);
            StickerView SvVideo = rootView.findViewById(R.id.sticker_view1);
            SvVideo.setBackgroundColor(0);
        }

        //currently you can config your own icons and icon event. the event you can custom
        BitmapStickerIcon deleteIcon = new BitmapStickerIcon(ContextCompat.getDrawable(requireContext(),
                R.drawable.sticker_ic_close_white_18dp),
                BitmapStickerIcon.LEFT_TOP);
        deleteIcon.setIconEvent(new DeleteIconEvent());

        BitmapStickerIcon zoomIcon = new BitmapStickerIcon(ContextCompat.getDrawable(requireContext(),
                R.drawable.sticker_ic_scale_white_18dp),
                BitmapStickerIcon.RIGHT_BOTOM);
        zoomIcon.setIconEvent(new ZoomIconEvent());

        stickerView.setIcons(Arrays.asList(deleteIcon, zoomIcon));

        stickerView.setBackgroundColor(Color.WHITE);
        stickerView.setLocked(false);
        stickerView.setConstrained(true);

        stickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override
            public void onStickerAdded(@NonNull Sticker sticker) {
                Timber.tag(TAG).d("onStickerAdded");
            }

            @Override
            public void onStickerClicked(@NonNull Sticker sticker) {
                if (sticker instanceof TextSticker) {
                    String stext = ((TextSticker) sticker).getText();
                    editText.setText(stext);
                    stickerView.removeCurrentSticker();
                    editTextLayout.setVisibility(View.VISIBLE);
                    showKeyboard(true);
                    editText.requestFocus();
                }
                Timber.tag(TAG).d("onStickerClicked");
            }

            @Override
            public void onStickerDeleted(@NonNull Sticker sticker) {
                Timber.tag(TAG).d("onStickerDeleted");
            }

            @Override
            public void onStickerDragFinished(@NonNull Sticker sticker) {
                Timber.tag(TAG).d("onStickerDragFinished");
            }

            @Override
            public void onStickerZoomFinished(@NonNull Sticker sticker) {
                Timber.tag(TAG).d("onStickerZoomFinished");
            }

            @Override
            public void onStickerFlipped(@NonNull Sticker sticker) {
                Timber.tag(TAG).d("onStickerFlipped");
            }

            @Override
            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
                Timber.tag(TAG).d("onDoubleTapped: double tap will be with two click");
            }
        });
    }

    // Toggle Visibility of Sticker EditText
    private void showHideEditText() {
        int getEditTextVisibility = editTextLayout.getVisibility();
        if (getEditTextVisibility == View.VISIBLE) {
            String sText = editText.getText().toString();
            addText(sText, textColor);
            showKeyboard(false);
            editTextLayout.setVisibility(View.GONE);
        } else {
            editText.setText("");
            editText.setTextColor(Color.WHITE);
            editTextLayout.setVisibility(View.VISIBLE);
            showKeyboard(true);
            editText.requestFocus();

        }
    }

    // Show Keyboard
    private void showKeyboard(boolean show) {
        if (show) {
            keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 1);
        } else {
            keyboard.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
        Timber.tag("Keyboard function").i("triggered");
    }

    private void addText(String stickerText, int color) {
        if (!stickerText.equals("")) {
            final TextSticker sticker = new TextSticker(requireActivity());
            sticker.setText(stickerText);
            sticker.setTextColor(color);
            sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER);
            sticker.resizeText();

            stickerView.addSticker(sticker);
        }
    }

    // Toggle Sticker Grid
    private void stickerOptions() {
        if (selectSticker.getVisibility() == View.VISIBLE) {
            selectSticker.setVisibility(View.GONE);
        } else {
            stickersGrid.setAdapter(stickerAdapter);
            selectSticker.setVisibility(View.VISIBLE);
        }
    }

    public class StickerAdapter extends BaseAdapter {

        private Activity activity;
        private LayoutInflater inflater;

        private StickerAdapter(Activity activity) {
            this.activity = activity;
        }

        @Override
        public int getCount() {
            return sticker_images.length;
        }

        @Override
        public Object getItem(int location) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (inflater == null)
                inflater = (LayoutInflater) activity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null)
                convertView = Objects.requireNonNull(inflater).inflate(R.layout.item_sticker_gridview, null);

            ImageView gridPhoto = convertView.findViewById(R.id.sticker_grid_item);

            // set grid Photos
            gridPhoto.setImageResource(sticker_images[position]);
            return convertView;
        }
    }

    private void removeAllStickers() {
        stickerView.removeAllStickers();
    }

    ///////////

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            // List that will contain the selected files/videos
            List<Uri> mSelected = Matisse.obtainResult(data);

            Timber.tag(TAG).e(mSelected.get(0).toString());

            if (mSelected.get(0).toString().contains("image")) {
                //handle image
                try {
                    Uri uri = mSelected.get(0);
                    // Bitmap to the server
                    rotatedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);

                    if (rotatedBitmap != null) {
                        setStickerView(0);
                        capturedImage.setVisibility(View.VISIBLE);
                        Glide.with(requireContext())
                                .load(rotatedBitmap)
                                .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                                .into(capturedImage);
                        editMedia.setVisibility(View.VISIBLE);
                        captureMedia.setVisibility(View.GONE);

                        Timber.tag("Image bitmap").i("%s-", rotatedBitmap.toString());
                    } else {
                        Toasty.error(requireActivity(), "Oops, something went wrong, kindly Try Again",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else  if (mSelected.get(0).toString().contains("video")) {
                //handle video
                try {
                    Uri path = mSelected.get(0);
                    if(path != null) {
                        finalVideo = path;
                        videoView.setVisibility(View.VISIBLE);
                        editMedia.setVisibility(View.VISIBLE);
                        captureMedia.setVisibility(View.GONE);

                        videoView.setVideoURI(path);
                        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
                        videoView.start();

                        preview.setVisibility(View.INVISIBLE);
                        setStickerView(1);
                    }
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Toasty.info(requireContext(), "Neither Image nor Video was selected", Toasty.LENGTH_SHORT).show();
            }

            Timber.tag("Matisse").d("mSelected: %s", mSelected);
        }
    }

    private Uri getOutputMediaFileUri(int mediaType) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (isExternalStorageAvailable()) {
            // get the URI
            File mediaStorageDir = new File(String.valueOf(requireContext().getExternalFilesDir(null)));
            // 1. Create our subdirectory
            if (! mediaStorageDir.exists()) {
                if (! mediaStorageDir.mkdirs()) {
                    Timber.tag(TAG).e("Failed to create directory.");
                    return null;
                }
            }
            // 2. Create a file name
            // 3. Create the file
            File mediaFile;
            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

            String path = mediaStorageDir.getPath() + File.separator;
            if (mediaType == MEDIA_TYPE_IMAGE) {
                mediaFile = new File(path + "qweeksnap_" + timestamp + ".jpg");
            }
            else if (mediaType == MEDIA_TYPE_VIDEO) {
                mediaFile = new File(path + "qweeksnap_" + timestamp + ".mp4");
            }
            else {
                return null;
            }

            Timber.tag(TAG).d("File: %s", Uri.fromFile(mediaFile));

            // 4. Return the file's URI
            return Uri.fromFile(mediaFile);
        } else {
            Timber.tag(TAG).d("No storage detected");
            return null;
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();

        return state.equals(Environment.MEDIA_MOUNTED);
    }

    /*
    *
    * Upload Functions
    *
     */
    private void uploadBitmap(final Bitmap bitmap, final String username) {
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
                    dropBtn.setEnabled(true);

                    getFragmentManager().popBackStack();

                    // success
                    String sent = jObj.getString("sent");
                    Toasty.success(requireActivity(), sent, Toast.LENGTH_SHORT).show();
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
                Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                dropProgress.hide();
                dropBtn.setEnabled(true);
            }

            },
                error -> {
                Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                Toasty.error(requireContext(),
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
        Volley.newRequestQueue(requireContext()).add(volleyMultipartRequest);
    }

    private void uploadVideo(Uri videoFile){
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
                                dropBtn.setEnabled(true);

                                getFragmentManager().popBackStack();

                                // success
                                String sent = jObj.getString("sent");
                                Toasty.success(requireActivity(), sent, Toast.LENGTH_SHORT).show();
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
                            Toasty.error(requireContext(), "Mission Control, come in !", Toast.LENGTH_SHORT).show();
                            dropProgress.hide();
                            dropBtn.setEnabled(true);
                        }
                    },
                    error -> {Timber.tag(TAG).e("Drop Error: %s", error.getMessage());
                        Toasty.error(requireContext(),
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
            rQueue = Volley.newRequestQueue(requireContext());
            rQueue.add(volleyMultipartRequest);



        } catch (IOException e) {
            e.printStackTrace();
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

    public void onClick() {
        assert getFragmentManager() != null;
        if (selectSticker.getVisibility() == View.VISIBLE) {
            stickerOptions();
        } else if(editTextLayout.getVisibility() == View.VISIBLE) {
            showHideEditText();
        } else if (editMedia.getVisibility() == View.VISIBLE) {
            EditCaptureSwitch();
            removeAllStickers();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    private void goBack() {
        assert getFragmentManager() != null;
        if (selectSticker.getVisibility() == View.VISIBLE) {
            stickerOptions();
        } else if(editTextLayout.getVisibility() == View.VISIBLE) {
            showHideEditText();
        } else if (editMedia.getVisibility() == View.VISIBLE) {
            EditCaptureSwitch();
            removeAllStickers();
        } else {
            getFragmentManager().popBackStack();
        }
    }
}
