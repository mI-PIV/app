package com.onrpiv.uploadmedia.Experiment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.Camera.CameraFragment;
import com.onrpiv.uploadmedia.Utilities.FrameExtractor;
import com.onrpiv.uploadmedia.Utilities.PathUtil;

import java.util.concurrent.Callable;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class VideoActivity extends AppCompatActivity{
    private Button pickVideo, generateFramesButton, recordVideo;
    public static final int REQUEST_PICK_VIDEO = 3;
    private static final int REQUEST_VIDEO_CAPTURE = 300;
    private VideoView mVideoView;
    private TextView mBufferingTextView;
    private String videoPath;
    private String userName;
    private String fps = "20";

    // Current playback position (in milliseconds).
    private int mCurrentPosition = 0;

    // Tag for the instance state bundle.
    private static final String PLAYBACK_TIME = "play_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the transferred data from source activity.
        Intent userNameIntent = getIntent();
        userName = userNameIntent.getStringExtra("UserName");

        recordVideo = (Button) findViewById(R.id.recordVideo);
        pickVideo = (Button) findViewById(R.id.pickVideo);
        generateFramesButton = (Button) findViewById(R.id.generateFrames);
        generateFramesButton.setEnabled(false);

        recordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Camera supports high speed capture
                if (hasHighSpeedCapability() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    FragmentManager fragManager = getSupportFragmentManager();

                    final String requestKey = "highSpeedKey";
                    fragManager.setFragmentResultListener(requestKey, VideoActivity.this, new FragmentResultListener() {
                        @Override
                        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                            videoCaptured(Uri.parse(result.getString("uri")));
                            fps = result.getString("fps");
                            generateFramesButton.setEnabled(true);
                        }
                    });

                    fragManager.beginTransaction().replace(R.id.video_layout_container, CameraFragment.newInstance(requestKey)).commit();

                // Camera doesn't support high speed capture
                } else {
                    Intent videoCaptureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (videoCaptureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(videoCaptureIntent, REQUEST_VIDEO_CAPTURE);
                    }
                }
            }
        });

        pickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickVideoIntent.setType("video/*");
                startActivityForResult(pickVideoIntent, REQUEST_PICK_VIDEO);
            }
        });

        generateFramesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoPath != null){
                    generateFrames(view);
                }else{
                    Toast.makeText(VideoActivity.this, "Please select a video", Toast.LENGTH_SHORT).show();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mVideoView.pause();
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current playback position (in milliseconds) to the
        // instance state bundle.
        outState.putInt(PLAYBACK_TIME, mVideoView.getCurrentPosition());
    }

    private void initializePlayer(Uri uri) {
        // Show the "Buffering..." message while the video loads.
        mBufferingTextView.setVisibility(VideoView.VISIBLE);
        if (uri != null){
            mVideoView.setVideoURI(uri);
        }
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
                        mVideoView.start();
                    }
                });

        // Listener for onCompletion() event (runs after media has finished
        // playing).
        mVideoView.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Toast.makeText(VideoActivity.this,
                                R.string.toast_message,
                                Toast.LENGTH_SHORT).show();

                        // Return the video position to the start.
                        mVideoView.seekTo(0);
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
            generateFramesButton.setEnabled(true);
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

                // If we retrieved the video from google drive, then delete the temp file we created
                PathUtil.deleteIfTempFile(VideoActivity.this, videoPath);

                return null;
            }
        };

        FrameExtractor.generateFrames(view.getContext(), userName, videoPath, fps, successCallBack);
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    private void videoCaptured(Uri video) {
        Toast.makeText(this, "Video content URI: " + video,
                Toast.LENGTH_LONG).show();

        videoPath = PathUtil.getRealPath(VideoActivity.this, video);
        initializePlayer(video);
        recordVideo.setBackgroundColor(Color.parseColor("#00CC00"));
        pickVideo.setBackgroundColor(Color.parseColor("#00CC00"));
    }

    private void videoSelected(Uri video) {
        Toast.makeText(this, "Video content URI: " + video,
                Toast.LENGTH_LONG).show();

        videoPath = PathUtil.getRealPath(VideoActivity.this, video);
        initializePlayer(video);
        pickVideo.setBackgroundColor(Color.parseColor("#00CC00"));
    }

    private boolean hasHighSpeedCapability() {
        final CameraManager camManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        boolean hasHighFPS = false;
        try {
            int[] capabilities = camManager.getCameraCharacteristics(camManager.getCameraIdList()[0]).get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            assert capabilities != null;
            for (int capability : capabilities) {
                if (capability == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO) {
                    hasHighFPS = true;
                    break;
                }
            }
        } catch (IllegalArgumentException | CameraAccessException e) {
            e.printStackTrace();
        }
        return hasHighFPS;
    }
}

