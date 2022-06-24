package com.onrpiv.uploadmedia.Experiment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ResultSettings;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewMultipleResultsActivity extends ViewResultsActivity {

    private HashMap<Integer, ArrayList<PivResultData>> data;
    private int currentIndex;
    private ResultSettings settings;
    private PivParameters parameters;
    // TODO override the save "image" button to save a video
    // TODO lastly, figure out the whole video processing pipeline

    // ui
    TextView frameText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO need to add the data before we switch to this activity
        data = new HashMap<>();
        onIndexChange(0);

        // add temporal seekbar to the results layout
        RelativeLayout base = findViewById(R.id.base_layout);
        base.addView(buildSliderLayout(), 1);  // index 1 -> underneath images
    }

    public void addData(ArrayList<PivResultData> data, int idx)
    {
        this.data.put(idx, data);
    }

    @Override
    public void OnClick_SaveImage(View view) {
        // TODO create and save video
        // https://stackoverflow.com/questions/40315349/how-to-create-a-video-from-an-array-of-images-in-android
        // TODO iterate through "data" and create a bitmap foreach imagestack
    }

    private void onIndexChange(int newIdx)
    {
        // TODO this is called when seekbar changes
        // TODO need to rework this
        this.settings = super.settings;
        parameters = pivParameters;

        // TODO apply changes to the new pivresultdata?

        currentIndex = newIdx;
        frameText.setText("frame: " + newIdx);
    }

    private LinearLayout buildSliderLayout()
    {
        Context context = ViewMultipleResultsActivity.this;
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                // width, length

        // text
        frameText = new TextView(context);
        frameText.setText("frame: " + currentIndex);

        // temporal seekbar
        SeekBar seekBar = new SeekBar(context);
        seekBar.setMax(data.size());
        seekBar.setProgress(0);

        // add listener to seekBar
        seekBar.setOnSeekBarChangeListener(getSeekBarListener());

        // add children to root
        root.addView(frameText);
        root.addView(seekBar);

        // set layout params
        root.setLayoutParams(params);
        frameText.setLayoutParams(params);
        seekBar.setLayoutParams(params);

        return root;
    }

    private SeekBar.OnSeekBarChangeListener getSeekBarListener()
    {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onIndexChange(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // EMPTY
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // EMPTY
            }
        };
    }
}
