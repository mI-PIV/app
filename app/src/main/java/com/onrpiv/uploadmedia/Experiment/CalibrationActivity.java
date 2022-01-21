package com.onrpiv.uploadmedia.Experiment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.Camera.Calibration.CameraCalibration;
import com.onrpiv.uploadmedia.Utilities.Camera.Calibration.CameraCalibrationResult;
import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.FrameExtractor;
import com.onrpiv.uploadmedia.Utilities.PathUtil;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.io.File;
import java.util.concurrent.Callable;


public class CalibrationActivity extends VideoActivity {
    private SeekBar frameSeekBar;
    private TextView selectedFrameText;
    private LinearLayout seekbarLayout;
    private Button calibButton;
    private float vidTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        calibButton.setText("Calibrate");
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
                vidTime = progress * 1000f;
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

    private void calibrationCallback(Context context, String imagePath) {
        if (!new File(imagePath).exists())
            return;

        ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Searching for calibration pattern...");
        pDialog.setCancelable(false);
        pDialog.show();

        Thread thread = new Thread() {
            @Override
            public void run() {
                // calculate calibration
                CameraCalibrationResult ccResult = new CameraCalibrationResult();
                ccResult.ratio = CameraCalibration.calculatePixelToPhysicalRatio(imagePath);

                // check to see if calibration pattern was found
                if (ccResult.ratio == -1d) {
                    AlertDialog.Builder failedDialog = new AlertDialog.Builder(context)
                            .setMessage("Couldn't find a calibration pattern. Please make sure " +
                                    "the calibration pattern is completely visible in " +
                                    "the photo and try again.")
                            .setPositiveButton("Okay", null);
                    threadedPopup(context, failedDialog);
                } else {
                    AlertDialog.Builder successDialog = new AlertDialog.Builder(context)
                            .setMessage("Calibration pattern found!")
                            .setPositiveButton("Save Calibration", null);
                    threadedPopup(context, successDialog);
                    setMessage("Calculating camera matrices...", context, pDialog);

                    Mat cameraMatrix = new Mat();
                    Mat distanceCoefficients = new MatOfDouble();
                    Mat.eye(3, 3, CvType.CV_64FC1).copyTo(cameraMatrix);
                    Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(distanceCoefficients);
                    CameraCalibration.saveCameraProperties(context, cameraMatrix, distanceCoefficients);

                    setMessage("Saving camera calibration...", context, pDialog);
                    ccResult.saveCameraMatrix(cameraMatrix);
                    ccResult.saveDistanceCoeffs(distanceCoefficients);

                    // save calibration
                    File calibrationSaveFile = new File(PathUtil.getUserDirectory(context, userName), "calibration.obj");
                    FileIO.write(ccResult, calibrationSaveFile);
                }

                // delete temp calibration image
                PathUtil.deleteRecursive(new File(imagePath));

                if (pDialog.isShowing())
                    pDialog.dismiss();
            }
        };
        thread.start();
    }

    public static void setMessage(String msg, Context context, ProgressDialog pDialog) {
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pDialog.setMessage(msg);
            }
        });
    }

    private static void threadedPopup(Context context, AlertDialog.Builder dialogBuilder) {
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogBuilder.create().show();
            }
        });
    }

    private int calcDP(int desiredDP) {
        return Math.round(desiredDP * this.getResources().getDisplayMetrics().density);
    }
}
