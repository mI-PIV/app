package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.VideoCreator;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class ViewMultipleResultsActivity extends ViewResultsActivity {

    public static HashMap<Integer, HashMap<String, PivResultData>> data;
    private TextView frameText;
    private String userName;
    private String framesetName;
    private SeekBar temporalSlider;

    protected void onCreate(Bundle savedInstanceState) {
        changeData(0);  // need to load the data before showing the results
        super.onCreate(savedInstanceState);  // show the results page

        Bundle extras = getIntent().getExtras();
        userName = (String) extras.get(PivResultData.USERNAME);
        framesetName = (String) extras.get(PivResultData.FRAMESET);

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

        // create frames
        File expFrames = PathUtil.getNumberedExperimentFramesDirectory(this, userName, experimentNumber);
        int currentIdx = temporalSlider.getProgress();
        for (int i = 0; i < data.size(); i++) {
            // change data
            onIndexChange(i);
            // get results view
            View imageStack = findViewById(R.id.img_frame);
            imageStack.setDrawingCacheEnabled(true);
            Bitmap bmp = imageStack.getDrawingCache();
            // save results frame
            File framePath = new File(expFrames, String.format("%04d", i) + ".jpg");
            try (FileOutputStream output = new FileOutputStream(framePath)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        onIndexChange(currentIdx);

        // video creation
        // TODO run on different thread w/ pDialog
        String tempVidPath = getExternalFilesDir(null).getPath() + "/temp.avi";
        if (VideoCreator.createAndSaveVideo(expFrames, tempVidPath)) {
            // cleanup results frames
            PathUtil.deleteRecursive(expFrames);
            Log.d("VID_CREATE", "Created vid at: " + tempVidPath);
        } else {
            Log.e("VID_CREATE", "Failed to create video.");
        }

        // move temp vid to gallery
        Context context = this;
        String vidFilename = "mI_PIV_" + experimentNumber + "_" + imageCounter++;
        ContentResolver resolver = getContentResolver();
        Uri videoCollection;
        if (Build.VERSION.SDK_INT >= 29) {
            videoCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, vidFilename);
        contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/avi");
        Uri videoUri = resolver.insert(videoCollection, contentValues);

        // TODO test this
        // move temp vid to gallery
        boolean moveSuccess = false;
        try {
            InputStream istream = new FileInputStream(tempVidPath);
            OutputStream ostream = resolver.openOutputStream(videoUri);
            byte[] buf = new byte[1024];
            int len;
            while ((len = istream.read(buf)) > 0) {
                ostream.write(buf, 0, len);
            }
            ostream.close();
            istream.close();
            moveSuccess = true;
            Log.d("MOVE_VID", "Moved temp vid to " + videoUri);
        } catch (Exception e) {
            Log.e("MOVE_VID", "Failed to move temp video:\n");
            e.printStackTrace();
        }

        if (moveSuccess) {
            // popup showing user where to find the saved image
            new AlertDialog.Builder(this)
                    .setPositiveButton("Okay", null)
                    .setMessage("Current experiment results view saved to your video gallery.")
                    .create().show();

            // delete temp vid
            PathUtil.deleteRecursive(new File(tempVidPath));
        } else {
            Toast.makeText(this, "Failed to move temp vid to gallery", Toast.LENGTH_LONG).show();
        }

        saveImageButton.setEnabled(true);
        saveImageButton.setBackgroundColor(Color.parseColor("#243EDF"));
    }

    private void onIndexChange(int newIdx) {
        changeData(newIdx);
        settings.vecFieldChanged = true;
        settings.vortMapChanged = true;
        super.applyDisplay(null);
        frameText.setText(getFrameText(newIdx+1));
    }

    private void changeData(int newIdx) {
        singlePass = data.get(newIdx).get(PivResultData.SINGLE);
        multiPass = data.get(newIdx).get(PivResultData.MULTI);
        if (pivParameters.isReplace()) {
            replacedPass = data.get(newIdx).get(PivResultData.MULTI + PivResultData.PROCESSED + PivResultData.REPLACE);
        }
         correlationMaps = loadCorrelationMaps(pivParameters.isReplace());
    }

    private RelativeLayout buildSliderLayout(int belowThisId, int aboveThisId) {
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
        temporalSlider = new SeekBar(context);
        temporalSlider.setMax(data.size()-1);
        temporalSlider.setProgress(0);

        // add listener to seekBar
        temporalSlider.setOnSeekBarChangeListener(getSeekBarListener());

        // add children to root
        container.addView(frameText);
        container.addView(temporalSlider);
        root.addView(container);

        return root;
    }

    private SeekBar.OnSeekBarChangeListener getSeekBarListener() {
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
