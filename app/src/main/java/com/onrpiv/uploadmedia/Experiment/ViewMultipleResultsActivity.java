package com.onrpiv.uploadmedia.Experiment;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import java.util.HashMap;

public class ViewMultipleResultsActivity extends ViewResultsActivity {

    public static HashMap<Integer, HashMap<String, PivResultData>> data;
    private int currentIndex;
    TextView frameText;

    protected void onCreate(Bundle savedInstanceState) {
        changeData(0);  // need to load the data before showing the results
        super.onCreate(savedInstanceState);  // show the results page

        // add temporal seekbar to the results layout
        RelativeLayout base = findViewById(R.id.base_layout);
        // TODO this isn't working, try adding the slider to frame viewer view
        base.addView(buildSliderLayout(R.id.img_frame, R.id.apply_layout));

        onIndexChange(0);
    }

    @Override
    public void OnClick_SaveImage(View view) {
        // TODO create and save video
        // https://stackoverflow.com/questions/40315349/how-to-create-a-video-from-an-array-of-images-in-android
        // TODO iterate through "data" and create a bitmap foreach imagestack
    }

    private void onIndexChange(int newIdx)
    {
        changeData(newIdx);
        settings.vecFieldChanged = true;
        settings.vortMapChanged = true;
        super.applyDisplay(null);

        currentIndex = newIdx;
        frameText.setText("frame: " + (newIdx+1));
    }

    private void changeData(int newIdx)
    {
        singlePass = data.get(newIdx).get(PivResultData.SINGLE);
        multiPass = data.get(newIdx).get(PivResultData.MULTI);
        if (pivParameters.isReplace()) {
            replacedPass = data.get(newIdx).get(PivResultData.MULTI + PivResultData.PROCESSED + PivResultData.REPLACE);
        }
         correlationMaps = loadCorrelationMaps(pivParameters.isReplace());
    }

    private RelativeLayout buildSliderLayout(int belowThisId, int aboveThisId)
    {
        Context context = ViewMultipleResultsActivity.this;
        RelativeLayout root = new RelativeLayout(context);
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        RelativeLayout.LayoutParams paramsR = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // add the below rule
        paramsR.addRule(RelativeLayout.BELOW, belowThisId);
        root.setLayoutParams(paramsR);

        // change the 'aboveThis' below rule
        View aboveThis = findViewById(aboveThisId);
        ViewGroup.LayoutParams aboveThisParams = aboveThis.getLayoutParams();
        if ((RelativeLayout.LayoutParams) aboveThisParams != null) {
            root.setId(View.generateViewId());
            ((RelativeLayout.LayoutParams) aboveThisParams).addRule(RelativeLayout.BELOW, root.getId());
        }

        // text
        frameText = new TextView(context);
        frameText.setGravity(Gravity.CENTER);
        frameText.setText("frame: " + currentIndex);

        // temporal seekbar
        SeekBar seekBar = new SeekBar(context);
        seekBar.setMax(data.size()-1);
        seekBar.setProgress(0);

        // add listener to seekBar
        seekBar.setOnSeekBarChangeListener(getSeekBarListener());

        // add children to root
        container.addView(frameText);
        container.addView(seekBar);
        root.addView(container);

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
