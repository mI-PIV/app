package com.onrpiv.uploadmedia.Experiment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.google.android.material.slider.RangeSlider;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.BackgroundSub;
import com.onrpiv.uploadmedia.Utilities.Camera.CameraConfigPopup;
import com.onrpiv.uploadmedia.Utilities.Camera.CameraFragment;
import com.onrpiv.uploadmedia.Utilities.Camera.HighSpeedCaptureCallback;
import com.onrpiv.uploadmedia.Utilities.FpsExtractor;
import com.onrpiv.uploadmedia.Utilities.FrameExtractor;
import com.onrpiv.uploadmedia.Utilities.PathUtil;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class VideoActivity extends AppCompatActivity {
    protected Button pickVideo, generateFramesButton, recordVideo;
    protected RangeSlider rangeSlider;
    private static final int REQUEST_PICK_VIDEO = 3;
    private static final int REQUEST_VIDEO_CAPTURE = 300;
    protected VideoView mVideoView;
    private TextView mBufferingTextView;
    protected CheckBox viewBackgroundCheckbox;
    protected TextView frameSetNameInput;

    protected String videoPath;
    protected Uri videoUri;
    protected String userName;
    protected String frameSetName;
    private String fps = "20";
    private float vidStart = 0f;
    private float vidEnd = 1f;

    private Context context;

    // Current playback position (in milliseconds).
    private int mCurrentPosition = 0;
    private boolean video_selected = false;

    // Tag for the instance state bundle.
    private static final String PLAYBACK_TIME = "play_time";

    // Play video thread
    private Thread playVideoThread;
    private boolean threadRunning;  // only use with the playVideoThread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the transferred data from source activity.
        Intent userNameIntent = getIntent();
        userName = userNameIntent.getStringExtra("UserName");

        // widgets
        recordVideo = (Button) findViewById(R.id.recordVideo);
        pickVideo = (Button) findViewById(R.id.pickVideo);
        generateFramesButton = (Button) findViewById(R.id.generateFrames);
        generateFramesButton.setEnabled(false);
        rangeSlider = findViewById(R.id.vid_rangeSlider);
        ((ViewGroup) rangeSlider.getParent()).setVisibility(View.GONE);
        viewBackgroundCheckbox = findViewById(R.id.backsub_video_checkbox);
        frameSetNameInput = findViewById(R.id.frameSetNameText);

        context = this;
        Activity activity = this;

        // record button click
        recordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraConfigPopup cameraConfigPopup = new CameraConfigPopup(activity, context, new HighSpeedCaptureCallback() {
                    // start the high speed capture
                    @Override
                    public void highSpeedCapture(CameraConfigPopup cameraSizes) {
                        FragmentManager fragManager = getSupportFragmentManager();
                        final String requestKey = "highSpeedKey";
                        fragManager.setFragmentResultListener(requestKey, VideoActivity.this,
                                new FragmentResultListener() {
                                    @Override
                                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                                        videoCaptured(Uri.parse(result.getString("uri")));
                                        fps = result.getString("fps");
                                    }
                                });
                        fragManager.beginTransaction().replace(R.id.video_layout_container,
                                CameraFragment.newInstance(requestKey, cameraSizes)).commit();
                    }
                });
                cameraConfigPopup.showConfigPopup();
            }
        });

        // pick video click
        pickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickVideoIntent.setType("video/*");
                startActivityForResult(pickVideoIntent, REQUEST_PICK_VIDEO);
            }
        });

        // generate frames click
        generateFramesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoPath != null){
                    generateFrames(view);
                    frameSetNameInput.setText("");
                }else{
                    Toast.makeText(VideoActivity.this, "Please select a video", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Frame set name input
        frameSetNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                generateFramesButton.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                frameSetNameInput.setBackgroundColor(Color.WHITE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Check for existing name
                List<String> frameSetsList = PathUtil.getFrameSetNames(context, userName);
                for (String existingName : frameSetsList) {
                    if (existingName.equals(editable.toString())) {
                        frameSetNameInput.setBackgroundColor(Color.RED);
                        Toast.makeText(context, "Frame set name already exists!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (!editable.toString().isEmpty()) {
                    generateFramesButton.setEnabled(true);
                    frameSetName = editable.toString();
                }
            }
        });

        mVideoView = (VideoView) findViewById(R.id.videoview);
        mBufferingTextView = (TextView) findViewById(R.id.buffering_textview);

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(PLAYBACK_TIME);
        }

        // Set up the media controller widget and attach it to the video view.
        MediaController controller = new MediaController(this);
        controller.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(controller);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // cleanup this activity
            cleanup();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ensuring the screen is locked to vertical position
    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mVideoView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Media playback takes a lot of resources, so everything should be
        // stopped and released at this time.
        releasePlayer();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void initializePlayer(Uri uri) {
        // Show the "Buffering..." message while the video loads.
        mBufferingTextView.setVisibility(VideoView.VISIBLE);

        if (uri == null)
            return;

        mVideoView.setVideoURI(uri);
        // Listener for onPrepared() event (runs after the media is prepared).
        mVideoView.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {

                        // Hide buffering message.
                        mBufferingTextView.setVisibility(VideoView.INVISIBLE);

                        // Restore saved position, if available.
                        if (mCurrentPosition > 0) {
                            mVideoView.seekTo(mCurrentPosition);
                        } else {
                            // Skipping to 1 shows the first frame of the video.
                            mVideoView.seekTo(1);
                        }

                        // Start playing!
                        playVideo(1, 1000);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_VIDEO_CAPTURE) {
                if (data != null) {
                    videoCaptured(data.getData());
                }
            }
            if (requestCode == REQUEST_PICK_VIDEO) {
                if (data != null) {
                    videoSelected(data.getData());
                }
            }
            video_selected = true;
            setupRangeSlider();
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    private void generateFrames(final View view) {
        // This is called when frame generation completes successfully
        final Callable<Void> successCallBack = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Change generate Frames button to green
                generateFramesButton.setBackgroundColor(Color.parseColor("#00CC00"));

                // Create popup that will show background if desired and direct user to processing or stay in videoActivity
                AlertDialog.Builder frameFinishedPopup;
                if (viewBackgroundCheckbox.isChecked()) {
                    frameFinishedPopup = BackgroundSub.showLatestBackground(VideoActivity.this, userName, frameSetName);
                } else {
                    frameFinishedPopup = new AlertDialog.Builder(VideoActivity.this);
                }

                frameFinishedPopup.setMessage("Head to processing newly extracted frames? Or continue extracting frames?");
                frameFinishedPopup.setPositiveButton("Process new frames", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // cleanup this activity
                        cleanup();
                        Intent imageActivityIntent = new Intent(VideoActivity.this, ImageActivity.class);
                        imageActivityIntent.putExtra("UserName", userName);
                        startActivity(imageActivityIntent);
                    }
                }).setNegativeButton("Continue extracting", null).create().show();
                return null;
            }
        };

        FrameExtractor.generateFrames(view.getContext(), userName, videoPath, frameSetName,
                fps, vidStart, vidEnd, successCallBack, viewBackgroundCheckbox.isChecked());
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    private void videoCaptured(Uri video) {
        Toast.makeText(this, "Video content URI: " + video,
                Toast.LENGTH_LONG).show();

        videoPath = PathUtil.getRealPath(VideoActivity.this, video);
        videoUri = video;
        initializePlayer(video);
        setupRangeSlider();
        recordVideo.setBackgroundColor(Color.parseColor("#00CC00"));
        pickVideo.setBackgroundColor(Color.parseColor("#00CC00"));
    }

    private void videoSelected(Uri video) {
        Toast.makeText(this, "Video content URI: " + video,
                Toast.LENGTH_LONG).show();

        videoPath = PathUtil.getRealPath(VideoActivity.this, video);
        videoUri = video;
        initializePlayer(video);
        fps = FpsExtractor.extractFps(videoPath);
        pickVideo.setBackgroundColor(Color.parseColor("#00CC00"));
    }

    private void playVideo(int start, int stop) {
        // stop the thread if it is currently running
        stopPlaybackThread();

        // get the video ready
        threadRunning = true;
        mVideoView.seekTo(start);
        mVideoView.start();

        // start the video playback thread
        playVideoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (threadRunning) {
                    if (mVideoView.isPlaying()) {
                        if (mVideoView.getCurrentPosition() >= stop) {
                            mVideoView.pause();
                            threadRunning = false;
                        }
                    }
                }
            }
        });
        playVideoThread.start();
    }

    protected void setupRangeSlider() {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        float videoDuration = 0;
        try {
            metaRetriever.setDataSource(videoPath);
            videoDuration = Float.parseFloat(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (videoDuration == 0f) {
            return;
        }

        rangeSlider.setValueFrom(0);
        rangeSlider.setValueTo(videoDuration/1000f);
        rangeSlider.setValues(0f, 0.1f);
        rangeSlider.setMinSeparation(0.1f);
        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                List<Float> vals = slider.getValues();
                vidStart = Math.min(vals.get(0), vals.get(1));
                vidEnd = Math.max(vals.get(0), vals.get(1));

                // find if it was the right or left slider that was changed (min, max)
                mCurrentPosition =(int) (value == vidStart? vidStart*1000 : vidEnd*1000);

                playVideo((int) vidStart*1000, (int) vidEnd*1000);
            }
        });
        ((ViewGroup) rangeSlider.getParent()).setVisibility(View.VISIBLE);
    }

    private void stopPlaybackThread() {
        if (null != playVideoThread && playVideoThread.isAlive()) {
            threadRunning = false;
            try {
                playVideoThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanup() {
        stopPlaybackThread();
        mVideoView.stopPlayback();
        mVideoView.clearAnimation();
        mVideoView.suspend();
        mVideoView.setVideoURI(null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("username", userName);
        outState.putBoolean("selected", video_selected);

        if (video_selected) {
            outState.putString("fps", fps);
            outState.putString("vidpath", videoPath);
            outState.putBoolean("background_check", viewBackgroundCheckbox.isChecked());
            outState.putString("viduri", videoUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        userName = savedInstanceState.getString("username");
        video_selected = savedInstanceState.getBoolean("selected");

        if (video_selected) {
            fps = savedInstanceState.getString("fps");
            videoPath = savedInstanceState.getString("vidpath");
            viewBackgroundCheckbox.setChecked(savedInstanceState.getBoolean("background_check"));
            videoUri = Uri.parse(savedInstanceState.getString("viduri"));

            initializePlayer(videoUri);
            setupRangeSlider();
            generateFramesButton.setEnabled(true);
            recordVideo.setBackgroundColor(Color.parseColor("#00CC00"));
            pickVideo.setBackgroundColor(Color.parseColor("#00CC00"));
        }
    }
}

