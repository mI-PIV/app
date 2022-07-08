package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;
import com.onrpiv.uploadmedia.Utilities.VideoCreator;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import java.io.File;
import java.util.HashMap;

public class ViewMultipleResultsActivity extends ViewResultsActivity {

    public static HashMap<Integer, HashMap<String, PivResultData>> data;
    private TextView frameText;
    private String userName;
    private String framesetName;

    protected void onCreate(Bundle savedInstanceState) {
        changeData(0);  // need to load the data before showing the results
        super.onCreate(savedInstanceState);  // show the results page

        userName = savedInstanceState.getString(PivResultData.USERNAME);
        framesetName = savedInstanceState.getString(PivResultData.FRAMESET);

        // add temporal seekbar to the results layout
        RelativeLayout base = findViewById(R.id.base_layout);
        base.addView(buildSliderLayout(R.id.img_frame, R.id.apply_layout));

        onIndexChange(0);
    }

    @Override
    public void OnClick_SaveImage(View view) {
        ImageButton saveImageButton = findViewById(R.id.imageSaveButton);
        saveImageButton.setEnabled(false);
        saveImageButton.setBackgroundColor(Color.parseColor("#576674"));

        // create and save video
        Context context = this;
        File frameDir = new File(PersistedData.getFrameDirPath(context, userName, framesetName));
        int fps = PersistedData.getFrameDirFPS(context, userName, framesetName);
        String imageFilename = "mI_PIV_" + experimentNumber + "_" + imageCounter++ + ".png";

        ContentResolver resolver = getContentResolver();
        Uri imageCollection;

        if (Build.VERSION.SDK_INT >= 29) {
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
        else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageFilename);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
        Uri videoUri = resolver.insert(imageCollection, contentValues);
        String videoPath = PathUtil.getRealPath(context, videoUri);

        VideoCreator.createAndSaveVideo(context, frameDir, videoPath, fps);

        // popup showing user where to find the saved image
        new AlertDialog.Builder(this)
                .setPositiveButton("Okay", null)
                .setMessage("Current experiment results view saved to your photo gallery.")
                .create().show();

        saveImageButton.setEnabled(true);
        saveImageButton.setBackgroundColor(Color.parseColor("#243EDF"));
    }

    private void onIndexChange(int newIdx)
    {
        changeData(newIdx);
        settings.vecFieldChanged = true;
        settings.vortMapChanged = true;
        super.applyDisplay(null);

        frameText.setText(getFrameText(newIdx+1));
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
        frameText.setText(getFrameText(1));

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

    private static String getFrameText(int idx)
    {
        return "Frames: " + idx + " & " + (idx + 1);
    }
}
