package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.Camera.Calibration.CameraCalibrationResult;
import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.FrameExtractor;
import com.onrpiv.uploadmedia.Utilities.PathUtil;

import java.io.File;
import java.util.concurrent.Callable;


public class CalibrationActivity extends VideoActivity {
    private SeekBar frameSeekBar;
    private TextView selectedFrameText;
    private LinearLayout seekbarLayout;
    private Button calibButton;
    private float vidTime;
    private String calImgPath;

    // calibration input registration
    private Intent calibInputIntent;
    private ActivityResultLauncher<Intent> calibInputLauncher;
    private CameraCalibrationResult ccResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        calibrationMessage();

        rangeSlider.setVisibility(View.GONE);
        viewBackgroundCheckbox.setVisibility(View.GONE);
        generateFramesButton.setVisibility(View.GONE);

        // Seek bar instructions
        TextView seekbarLabel = new TextView(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.weight = Gravity.CENTER_HORIZONTAL;
        seekbarLabel.setLayoutParams(labelParams);
        seekbarLabel.setText("Please select a frame with a clear view of the calibration pattern.");

        // Seek bar selected frame
        selectedFrameText = new TextView(this);
        LinearLayout.LayoutParams frameTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        frameTextParams.gravity = Gravity.CENTER_HORIZONTAL;
        selectedFrameText.setLayoutParams(frameTextParams);
        selectedFrameText.setBackground(ContextCompat.getDrawable(this,
                android.R.drawable.editbox_background));
        selectedFrameText.setText("0");

        frameSeekBar = new SeekBar(this);
        // frameSeekBar layout
        LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        seekBarParams.gravity = Gravity.CENTER;
        frameSeekBar.setLayoutParams(seekBarParams);

        // seek bar container
        seekbarLayout = new LinearLayout(this);
        seekbarLayout.setOrientation(LinearLayout.VERTICAL);

        // calibration button
        calibButton = new Button(this);
        calibButton.setText("Input Measurement");
        calibButton.setBackground(ContextCompat.getDrawable(this, R.drawable.buttons));
        calibButton.setTextColor(Color.parseColor("#FFFFFF"));
        calibButton.setVisibility(View.GONE);

        calibButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get tempfile path string
                String tempPath = PathUtil.getUserDirectory(CalibrationActivity.this, userName).getAbsolutePath() + "/calFrame.jpg";
                // callback after frame extraction with calibration
                Callable<Void> afterExtraction = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        // calibrate
                        calibrationCallback(CalibrationActivity.this, tempPath);
                        calibButton.setBackgroundColor(Color.parseColor("#00CC00"));
                        return null;
                    }
                };
                // single frame extraction (FrameExtractor.java) with callback
                FrameExtractor.extractSingleFrame(CalibrationActivity.this, videoPath, tempPath, vidTime, afterExtraction);
            }
        });

        // seek bar container
        LinearLayout.LayoutParams seekbarLayoutParams = new LinearLayout.LayoutParams(
                calcDP(300), ViewGroup.LayoutParams.WRAP_CONTENT);
        seekbarLayoutParams.gravity = Gravity.CENTER;
        seekbarLayoutParams.topMargin = calcDP(10);
        seekbarLayout.setLayoutParams(seekbarLayoutParams);

        seekbarLayout.addView(seekbarLabel);
        seekbarLayout.addView(selectedFrameText);
        seekbarLayout.addView(frameSeekBar);
        seekbarLayout.setVisibility(View.INVISIBLE);

        ViewGroup buttonContainer = (ViewGroup) pickVideo.getParent();
        buttonContainer.addView(seekbarLayout);
        buttonContainer.addView(calibButton);

        // register calibration input
        ccResult = new CameraCalibrationResult();
        calibInputIntent = new Intent(CalibrationActivity.this, CalibrationInputActivity.class);
        calibInputLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent resultsIntent = result.getData();
                if (null == resultsIntent) {
                    return;
                }
                ccResult.ratio = resultsIntent.getDoubleExtra("pixelsPerCm",-1d);

                // delete calibration frame
                PathUtil.deleteRecursive(new File(calImgPath));

                // save calibration
                File calibrationSaveFile = new File(PathUtil.getUserDirectory(CalibrationActivity.this, userName), "calibration.obj");
                FileIO.write(ccResult, calibrationSaveFile);

                new AlertDialog.Builder(CalibrationActivity.this)
                        .setMessage("Calibration saved!")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(true).create().show();
            }
        });
    }

    @Override
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

        frameSeekBar.setProgress(0);
        frameSeekBar.setMax(Math.round(videoDuration/1000f));
        frameSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mVideoView.seekTo(progress * 1000);
                selectedFrameText.setText(String.valueOf(progress));
                vidTime = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // EMPTY
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // EMPTY
            }
        });
        seekbarLayout.setVisibility(View.VISIBLE);
        calibButton.setVisibility(View.VISIBLE);
    }

    private void calibrationCallback(Context context, String imagePath){
        File calibImage = new File(imagePath);
        if (!calibImage.exists())
            return;
        calImgPath = imagePath;
        calibInputIntent.putExtra("framePath", imagePath);
        calibInputLauncher.launch(calibInputIntent);
    }

    private int calcDP(int desiredDP) {
        return Math.round(desiredDP * this.getResources().getDisplayMetrics().density);
    }

    private void calibrationMessage() {
        // Create a AlertDialog Builder.
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(CalibrationActivity.this);

        // Set title, icon, can not cancel properties (the box still remains on the screen if clicked outside).
        alertDialogBuilder.setTitle("Calibration Variables");
        alertDialogBuilder.setIcon(R.drawable.ic_launcher_background);
        alertDialogBuilder.setMessage("\nBe sure to use the same camera settings and magnification as the videos you intend to conduct PIV on.");
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create AlertDialog and show.
        alertDialogBuilder.create().show();
    }
}
