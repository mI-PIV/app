package com.onrpiv.uploadmedia.Experiment;

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

import androidx.core.content.ContextCompat;

import com.onrpiv.uploadmedia.R;


public class CalibrationActivity extends VideoActivity {
    private SeekBar frameSeekBar;
    private TextView selectedFrameText;
    private LinearLayout seekbarLayout;
    private Button calibButton;

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
                // TODO
                // TODO we need to extract the selected frame from the video
                // TODO hopefully I can find something that is quick and easy
                // TODO ffmpeg might be overkill for this
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
        frameSeekBar.setMax(Math.round(videoDuration/100f));
        frameSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mVideoView.seekTo(progress * 100);
                selectedFrameText.setText(String.valueOf(progress));
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

    private int calcDP(int desiredDP) {
        return Math.round(desiredDP * this.getResources().getDisplayMetrics().density);
    }
}
