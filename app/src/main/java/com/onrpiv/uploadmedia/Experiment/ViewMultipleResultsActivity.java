package com.onrpiv.uploadmedia.Experiment;

import static com.onrpiv.uploadmedia.Utilities.ResultSettings.BACKGRND_IMG;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.BACKGRND_SUB;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.BackgroundSub;
import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
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
    private SeekBar temporalSlider;
    private int currIdx;

    protected void onCreate(Bundle savedInstanceState) {
        changeData(0);  // need to load the data before showing the results
        super.onCreate(savedInstanceState);  // show the results page

        Bundle extras = getIntent().getExtras();
        userName = (String) extras.get(PivResultData.USERNAME);

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

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Saving video...");
        pDialog.show();

        // popup showing user where to find the saved image
        final AlertDialog successDialog = new AlertDialog.Builder(this)
                .setPositiveButton("Okay", null)
                .setMessage("Current experiment results view saved to your video gallery.")
                .create();

        final Context context = this;

        Thread vidThread = new Thread() {
            @Override
            public void run() {
                // create frames
                File expFrames = PathUtil.getNumberedExperimentFramesDirectory(context, userName, experimentNumber);
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
                String tempVidPath = getExternalFilesDir(null).getPath() + "/temp.mp4";
                String input = expFrames + "/%04d.jpg";
                if (FFmpeg.execute("-framerate 2 -i " + input + " " + tempVidPath) == Config.RETURN_CODE_SUCCESS) {
                    // cleanup results frames
                    PathUtil.deleteRecursive(expFrames);
                    Log.d("VID_CREATE", "Created vid at: " + tempVidPath);
                } else {
                    Log.e("VID_CREATE", "Failed to create video.");
                }

                // move temp vid to gallery
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
                contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                Uri videoUri = resolver.insert(videoCollection, contentValues);

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
                    Log.d("MOVE_VID", "Moved temp vid to " + videoUri);

                    moveSuccess = true;
                    // delete temp vid
                    PathUtil.deleteRecursive(new File(tempVidPath));
                } catch (Exception e) {
                    Log.e("MOVE_VID", "Failed to move temp video:\n");
                    e.printStackTrace();
                }

                if (pDialog.isShowing()) { pDialog.dismiss(); }
                if (moveSuccess) { showSuccessDialog(successDialog); }
            }
        };
        vidThread.start();

        saveImageButton.setEnabled(true);
        saveImageButton.setBackgroundColor(Color.parseColor("#243EDF"));
    }

    @Override
    protected void displayBaseImage(String backgroundCode) {
        Bitmap bmp;
        switch (backgroundCode) {
            case BACKGRND_IMG:
                File pngFile = new File(outputDirectory,
                        "Base_"+ currIdx + "_" + imgFileToDisplay);
                bmp = BitmapFactory.decodeFile(pngFile.getAbsolutePath());
                break;
            case BACKGRND_SUB:
                File backsubFile = new File(outputDirectory,
                        BackgroundSub.SUB1_FILENAME + "_" + currIdx + "_" + imgFileToDisplay);
                bmp = BitmapFactory.decodeFile(backsubFile.getAbsolutePath());
                break;
            default:
                bmp = createSolidBaseImage();
                break;
        }
        baseImage.setImageBitmap(bmp);
    }

    private void showSuccessDialog(AlertDialog dialog) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private void onIndexChange(int newIdx) {
        currIdx = newIdx;
        changeData(newIdx);
        settings.vecFieldChanged = true;
        settings.vortMapChanged = true;
        settings.backgroundChanged = true;
        applyDisplay(null);
        frameText.setText(getFrameText(newIdx+1));
    }

    private void changeData(int newIdx) {
        String spName = PivResultData.SINGLE + PivResultData.PROCESSED;
        String mpName = PivResultData.MULTI;


        if (pivParameters.isReplace()) {
            spName += PivResultData.REPLACE;
            replacedPass = data.get(newIdx).get(PivResultData.MULTI + PivResultData.PROCESSED + PivResultData.REPLACE);
        }
        singlePass = data.get(newIdx).get(spName);
        multiPass = data.get(newIdx).get(mpName);

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

    public static Class<?> loadFromFiles(Context context, String userName, int expNum) {
        PivParameters params = (PivParameters) FileIO.read(context, userName, expNum, PivParameters.IO_FILENAME);
        File expDir = PathUtil.getExperimentNumberedDirectory(context, userName, expNum);

        // filenames
        String spFilename = PivResultData.SINGLE + PivResultData.PROCESSED;
        spFilename += params.isReplace()? PivResultData.REPLACE : "";
        String mpFilename = PivResultData.MULTI;
        String repFilename = PivResultData.MULTI + PivResultData.PROCESSED + PivResultData.REPLACE;

        // multiple results
        HashMap<Integer, HashMap<String, PivResultData>> multipleResultData = new HashMap<>();
        // load single pass
        HashMap<Integer, PivResultData> singlepassResults = loadMultipleResults(expDir, spFilename);
        // load multi pass
        HashMap<Integer, PivResultData> multipassResults = loadMultipleResults(expDir, mpFilename);
        // load replaced
        HashMap<Integer, PivResultData> repResults = new HashMap<>();
        if (params.isReplace()) {
            repResults = loadMultipleResults(expDir, repFilename);
        }

        // reformat data
        for (int i = 0; i < singlepassResults.size(); i++) {
            HashMap<String, PivResultData> indexResults = new HashMap<>();
            PivResultData sp = singlepassResults.get(i);
            PivResultData mp = multipassResults.get(i);
            if (null != sp) { indexResults.put(sp.getName(), sp); }
            else {
                Log.e("MULTIPLE_RESULTS", "Single pass is null at i:" + i);
            }
            if (null != mp) { indexResults.put(mp.getName(), mp); }
            else {
                Log.e("MULTIPLE_RESULTS", "Multi pass is null at i: " + i);
            }
            if (params.isReplace()) {
                PivResultData rp = repResults.get(i);
                indexResults.put(rp.getName(), rp);
            }
            multipleResultData.put(i, indexResults);
        }
        // pass to the multiple results activity
        data = multipleResultData;
        pivParameters = params;
        return ViewMultipleResultsActivity.class;
    }

    private static HashMap<Integer, PivResultData> loadMultipleResults(File expDir, String filename) {
        int i = 0;
        File file = PathUtil.getObjectFile(expDir, filename, 0);
        HashMap<Integer, PivResultData> results = new HashMap<>();
        while (file.exists()) {
            results.put(i, (PivResultData) FileIO.read(file));
            file = PathUtil.getObjectFile(expDir, filename, ++i);
        }
        return results;
    }
}
