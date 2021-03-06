package com.onrpiv.uploadmedia.Experiment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.onrpiv.uploadmedia.Experiment.Popups.PivFrameSelectionPopup;
import com.onrpiv.uploadmedia.Experiment.Popups.PivOptionsPopup;
import com.onrpiv.uploadmedia.Learn.PIVBasics3;
import com.onrpiv.uploadmedia.Learn.PIVBasicsLayout;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.LightBulb;
import com.onrpiv.uploadmedia.Utilities.PersistedData;
import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;
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
    private int frameSet;
    private int frame1Num;
    private int frame2Num;
    private int fps;
    private static HashMap<String, PivResultData> resultData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);
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

                // create our particle density preview bitmap
                Bitmap bmp = BitmapFactory.decodeFile(frameSelectionPopup.frame1Path.getAbsolutePath());
                Bitmap cropped = Bitmap.createBitmap(
                        bmp,
                        Math.round(bmp.getWidth()/2f), Math.round(bmp.getHeight()/2f),
                        64, 64);
                Bitmap resizedCropped = PivFunctions.resizeBitmap(cropped, 600);

                // create the view holding our bitmap
                PhotoView particleDensityPreview = new PhotoView(ImageActivity.this);
                particleDensityPreview.setImageBitmap(resizedCropped);

                // popup asking user about particle density
                new android.app.AlertDialog.Builder(ImageActivity.this)
                        .setView(particleDensityPreview)
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AlertDialog.Builder(ImageActivity.this)
                                        .setMessage("Please select frames with more particle density.")
                                        .setPositiveButton("Okay", null)
                                        .show();
                            }
                        })
                        .setMessage("Do you see at least 5 particles in the image?")
                        .setTitle("Particle Density")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                frameSet = frameSelectionPopup.frameSet;
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
                        }).create().show();
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
        final PivOptionsPopup parameterPopup = new PivOptionsPopup(
                ImageActivity.this, frameSet, frame1Num, frame2Num);

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

    public void displayResults(View view) {
        // Pass PIV result data to ViewResultsActivity
        ViewResultsActivity.singlePass = resultData.get(PivResultData.SINGLE);
        ViewResultsActivity.multiPass = resultData.get(PivResultData.MULTI);
        if (pivParameters.isReplace()) {
            ViewResultsActivity.replacedPass = resultData.get(PivResultData.REPLACE2);
        }

        Intent displayIntent = new Intent(ImageActivity.this, ViewResultsActivity.class);
        displayIntent.putExtra(PivResultData.USERNAME, userName);
        displayIntent.putExtra(PivResultData.REPLACED_BOOL, pivParameters.isReplace());

        startActivity(displayIntent);
        pickImageMultiple.setBackgroundColor(Color.parseColor("#243EDF"));
        compute.setBackgroundColor(Color.parseColor("#243EDF"));
        review.setBackgroundColor(Color.parseColor("#243EDF"));
        parameters.setBackgroundColor(Color.parseColor("#243EDF"));
    }

    // Process Images
    public void processPiv(View view) {
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
