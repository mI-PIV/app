package com.onrpiv.uploadmedia.Experiment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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
import java.io.Serializable;

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
    private int frame1Num;
    private int frame2Num;
    private int fps;
    private PivResultData resultData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);

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

    public void onClick_MultipleImages(View view) {
        final PivFrameSelectionPopup frameSelectionPopup = new PivFrameSelectionPopup(ImageActivity.this,
                userName);

        // create listener for frame selection save button
        View.OnClickListener saveListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frame1File = frameSelectionPopup.frame1Path;
                frame2File = frameSelectionPopup.frame2Path;
                frame1Num = frameSelectionPopup.frame1Num;
                frame2Num = frameSelectionPopup.frame2Num;

                int framesDirNum = PersistedData.getFrameDirPath(ImageActivity.this, userName,
                        frameSelectionPopup.frameSetPath.getAbsolutePath());
                fps = PersistedData.getFrameDirFPS(ImageActivity.this, userName,
                        framesDirNum);

                review.setEnabled(true);
                parameters.setEnabled(true);
                pickImageMultiple.setBackgroundColor(Color.parseColor("#00CC00"));
                frameSelectionPopup.dismiss();
            }
        };

        frameSelectionPopup.setSaveListener(saveListener);
        frameSelectionPopup.show();
    }

    public void reviewFile(View view) {
        reviewImageFromUrl();
        review.setBackgroundColor(Color.parseColor("#00CC00"));
    }

    private void reviewImageFromUrl() {
        String[] urls = new String[2];
        urls[0] = frame1File.getAbsolutePath();
        urls[1] = frame2File.getAbsolutePath();

        Intent intent = new Intent(this, ViewPagerActivity.class).putExtra("string-array-urls", urls);
        startActivity(intent);
    }

    public void inputPivOptions(View view) {
        final PivOptionsPopup parameterPopup = new PivOptionsPopup(ImageActivity.this);

        // create listener for piv parameter save button
        View.OnClickListener saveListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pivParameters = parameterPopup.parameters;
                compute.setEnabled(true);
                parameters.setBackgroundColor(Color.parseColor("#00CC00"));
                parameterPopup.dismiss();
            }
        };

        parameterPopup.setSaveListener(saveListener);
        parameterPopup.setFPSParameters(fps, frame1Num, frame2Num);
        parameterPopup.show();
    }

    public void displayFile(View view) {
        Intent displayIntent = new Intent(this, ViewResultsActivity.class);
        displayIntent.putExtra(PivResultData.REPLACED, pivParameters.isReplace());
        displayIntent.putExtra(PivResultData.CORRELATION, (Serializable) resultData.getPivCorrelation());
        displayIntent.putExtra(PivResultData.INTERR_CENTERS, (Serializable) resultData.getInterrCenters());
        displayIntent.putExtra(PivResultData.VORTICITY, resultData.getVorticityValues());
        displayIntent.putExtra(PivResultData.MULTI, (Serializable) resultData.getPivCorrelationMulti());
        displayIntent.putExtra(PivResultData.ROWS, resultData.getRows());
        displayIntent.putExtra(PivResultData.COLS, resultData.getCols());
        displayIntent.putExtra(PivResultData.USERNAME, userName);

        if (pivParameters.isReplace()) {
            displayIntent.putExtra(PivResultData.REPLACE2, (Serializable) resultData.getPivReplaceMissing2());
        }

        startActivity(displayIntent);
        pickImageMultiple.setBackgroundColor(Color.parseColor("#243EDF"));
        compute.setBackgroundColor(Color.parseColor("#243EDF"));
        review.setBackgroundColor(Color.parseColor("#243EDF"));
        parameters.setBackgroundColor(Color.parseColor("#243EDF"));
    }

    // Process Images
    public void processFile(View view) {
        PivRunner pivRunner = new PivRunner(ImageActivity.this, userName, pivParameters,
                frame1File, frame2File);
        resultData = pivRunner.Run();
        display.setEnabled(true);
        compute.setBackgroundColor(Color.parseColor("#00CC00"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }
}
