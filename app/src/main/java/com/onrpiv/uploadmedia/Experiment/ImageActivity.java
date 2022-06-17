package com.onrpiv.uploadmedia.Experiment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.onrpiv.uploadmedia.Experiment.Popups.DensityPreviewPopup;
import com.onrpiv.uploadmedia.Experiment.Popups.PivFrameSelectionPopup;
import com.onrpiv.uploadmedia.Experiment.Popups.PivOptionsPopup;
import com.onrpiv.uploadmedia.Learn.PIVBasics3;
import com.onrpiv.uploadmedia.Learn.PIVBasicsLayout;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.LightBulb;
import com.onrpiv.uploadmedia.Utilities.PersistedData;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;
import com.onrpiv.uploadmedia.pivFunctions.PivRunner;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.HashMap;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class ImageActivity extends AppCompatActivity {
    Button parameters, compute, display, pickImageMultiple, review;
    private Uri fileUri;
    private String userName;
    private PivParameters pivParameters;
    private File frame1File;
    private File frame2File;
    private String frameSetName;
    private int frame1Num;
    private int frame2Num;
    private int fps;
    private static HashMap<String, PivResultData> resultData;

    private int step = 0;
    private static final String greenString = "#00CC00";
    private static final String blueString = "#165A7D";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // init buttons
        pickImageMultiple = (Button) findViewById(R.id.pickImageMultiple);
        parameters = (Button) findViewById(R.id.parameters);
        compute = (Button) findViewById(R.id.compute);
        display = (Button) findViewById(R.id.display);
        review = (Button) findViewById(R.id.Review);

        // Get the transferred data from source activity.
        Intent userNameIntent = getIntent();
        userName = userNameIntent.getStringExtra("UserName");

        OpenCVLoader.initDebug();

        Context context = getApplicationContext();

        new LightBulb(context, pickImageMultiple).setLightBulbOnClick("Image Pair",
                "You need to select two images to compute movement of the particles from the first to the second image.",
                getWindow());

        new LightBulb(context, review).setLightBulbOnClick("Image Correlation",
                "Review the images selected in \"select an image pair\" and consider whether the images will result in a useful PIV output.",
                new PIVBasics3(), "Learn More", getWindow());

        new LightBulb(context, compute).setLightBulbOnClick("Compute PIV",
                "Compute PIV computes the velocity field between the first and second image from \"Select An Image Pair\" according to the parameters in \"Input PIV Parameters\". For more information see: ",
                new PIVBasicsLayout(), "Learn More", getWindow());
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

    public void onClick_MultipleImages(View view) {
        final PivFrameSelectionPopup frameSelectionPopup = new PivFrameSelectionPopup(ImageActivity.this,
                userName);

        // create listener for frame selection save button
        View.OnClickListener saveListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Yes" button
                DialogInterface.OnClickListener densityPreviewListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        frameSetName = frameSelectionPopup.frameSetName;
                        frame1File = frameSelectionPopup.frame1Path;
                        frame2File = frameSelectionPopup.frame2Path;
                        frame1Num = frameSelectionPopup.frame1Num;
                        frame2Num = frameSelectionPopup.frame2Num;

                        fps = PersistedData.getFrameDirFPS(ImageActivity.this, userName,
                                frameSetName);

                        review.setEnabled(true);
                        parameters.setEnabled(true);
                        pickImageMultiple.setBackgroundColor(Color.parseColor("#00CC00"));
                        frameSelectionPopup.dismiss();
                    }
                };
                // create and display our density preview popup
                DensityPreviewPopup densityPreviewPopup = new DensityPreviewPopup(
                        ImageActivity.this, frameSelectionPopup.frame1Path.getAbsolutePath(),
                        frameSelectionPopup.frame2Path.getAbsolutePath(), densityPreviewListener);
                densityPreviewPopup.show();
            }
        };

        frameSelectionPopup.setSaveListener(saveListener);
        frameSelectionPopup.show();
    }

    public void reviewFile(View view) {
        reviewImageFromUrl();
        review.setBackgroundColor(Color.parseColor(greenString));
        step = 2;
    }

    private void reviewImageFromUrl() {
        String[] urls = new String[2];
        urls[0] = frame1File.getAbsolutePath();
        urls[1] = frame2File.getAbsolutePath();

        Intent intent = new Intent(this, ViewPagerActivity.class).putExtra("string-array-urls", urls);
        startActivity(intent);
    }

    public void inputPivOptions(View view) {
        final PivOptionsPopup parameterPopup = new PivOptionsPopup(ImageActivity.this,
                userName, frameSetName, frame1Num, frame2Num, getActivityResultRegistry());

        // create listener for piv parameter save button
        View.OnClickListener saveListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pivParameters = parameterPopup.parameters;
                compute.setEnabled(true);
                parameters.setBackgroundColor(Color.parseColor(greenString));
                step = 3;
                parameterPopup.dismiss();
            }
        };

        parameterPopup.setSaveListener(saveListener);
        parameterPopup.setFPSParameters(fps, frame1Num, frame2Num);
        parameterPopup.show();
    }

    public void displayResults(View view) {
        // Pass PIV result data to ViewResultsActivity
        PivResultData singlePassResult = resultData.get(PivResultData.SINGLE);
        assert singlePassResult != null;

        ViewResultsActivity.pivParameters = pivParameters;

        ViewResultsActivity.singlePass = singlePassResult;
        ViewResultsActivity.multiPass = resultData.get(PivResultData.MULTI);
        if (pivParameters.isReplace()) {
            ViewResultsActivity.replacedPass = resultData.get(PivResultData.MULTI+PivResultData.PROCESSED+PivResultData.REPLACE);
        }

        // calibration
        ViewResultsActivity.calibrated = singlePassResult.isCalibrated() ||
                ViewResultsActivity.multiPass.isCalibrated() ||
                ViewResultsActivity.replacedPass.isCalibrated();

        ViewResultsActivity.backgroundSubtracted = singlePassResult.isBackgroundSubtracted();

        Intent displayIntent = new Intent(ImageActivity.this, ViewResultsActivity.class);
        displayIntent.putExtra(PivResultData.USERNAME, userName);
        displayIntent.putExtra(PivResultData.REPLACED_BOOL, pivParameters.isReplace());

        startActivity(displayIntent);
        pickImageMultiple.setBackgroundColor(Color.parseColor(blueString));
        compute.setBackgroundColor(Color.parseColor(blueString));
        review.setBackgroundColor(Color.parseColor(blueString));
        parameters.setBackgroundColor(Color.parseColor(blueString));
    }

    // Process Images
    public void processPiv(View view) {
        PivRunner pivRunner = new PivRunner(ImageActivity.this, userName, pivParameters,
                frame1File, frame2File);
        resultData = pivRunner.Run();
        display.setEnabled(true);
        step = 4;
        compute.setBackgroundColor(Color.parseColor(greenString));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("step", step);
        outState.putParcelable("file_uri", fileUri);
        outState.putString("username", userName);

        if (step >= 1) {
            outState.putString("frame1file_str", frame1File.getAbsolutePath());
            outState.putString("frame2file_str", frame2File.getAbsolutePath());
            outState.putString("frameset", frameSetName);
            outState.putInt("frame1num", frame1Num);
            outState.putInt("frame2num", frame2Num);
            outState.putInt("fps", fps);
        }

        if (step >= 3) {
            outState.putSerializable("pivparams", pivParameters);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        step = savedInstanceState.getInt("step");
        fileUri = savedInstanceState.getParcelable("file_uri");
        userName = savedInstanceState.getString("username");

        if (step >= 1) {
            frame1File = new File(savedInstanceState.getString("frame1file_str"));
            frame2File = new File(savedInstanceState.getString("frame2file_str"));
            frameSetName = savedInstanceState.getString("frameset");
            frame1Num = savedInstanceState.getInt("frame1num");
            frame2Num = savedInstanceState.getInt("frame2num");
            fps = savedInstanceState.getInt("fps");

            // change buttons to reflect step
            setButton(pickImageMultiple, greenString, true);
            setButton(review, greenString,true);
        }

        if (step >= 3) {
            pivParameters = (PivParameters) savedInstanceState.getSerializable("pivparams");

            // change buttons to reflect step
            setButton(parameters, greenString,true);
            setButton(compute, blueString, true);
        }
    }

    private static void setButton(Button btn, String color, boolean enabled) {
        btn.setEnabled(enabled);
        btn.setBackgroundColor(Color.parseColor(color));
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
    }
}
