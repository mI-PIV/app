package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.google.android.material.slider.RangeSlider;
import com.onrpiv.uploadmedia.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sarbajit mukherjee on 09/07/2020.
 * Edited by KP on 02/18/2021
 */

public class ViewResultsActivity extends AppCompatActivity {
//    private Button firstPass, secondPass, replaceAfterFirstPass, replaceAfterSecondPass;
    private RangeSlider rangeSlider;
    private ImageView baseImage;
    private ImageView vectorFieldImage;
    private ImageView vorticityImage;
    private String imgFileToDisplay;
    private File storageDirectory;
    private double nMaxUpper;

    private HashMap<String, Bitmap> bmpHash = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent displayIntent = getIntent();
        int selectedId = displayIntent.getIntExtra("selection-Id", 0);

        // TODO rework this; this is just awful
        if (selectedId == 0) {
            setContentView(R.layout.display_result_layout_null_replaced);
//            replaceAfterFirstPass = (Button) findViewById(R.id.firstReplace);
//            replaceAfterSecondPass = (Button) findViewById(R.id.secondReplace);
        } else {
            setContentView(R.layout.display_result_layout);
        }

        rangeSlider = findViewById(R.id.rangeSeekBar);
        rangeSlider.setValues(120f, 135f);
        baseImage = findViewById(R.id.baseView);
        vectorFieldImage = findViewById(R.id.vectorsView);
        vorticityImage = findViewById(R.id.vortView);

//        firstPass = (Button) findViewById(R.id.firstPass);
//        secondPass = (Button) findViewById(R.id.secondPass);

        // Sync stacked images
//        OnScaleChangedListener scaleChangedListener = new OnScaleChangedListener() {
//            @Override
//            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
//                if (scaleFactor > 3.0f) {scaleFactor = 3.0f;}
//                if (scaleFactor < 1.0f) {scaleFactor = 1.0f;}
//
//                baseImage.setScale(scaleFactor, focusX, focusY, false);
//                vectorFieldImage.setScale(scaleFactor, focusX, focusY, false);
//                vorticityImage.setScale(scaleFactor, focusX, focusY, false);
//            }
//        };
//        baseImage.setOnScaleChangeListener(scaleChangedListener);
//        vectorFieldImage.setOnScaleChangeListener(scaleChangedListener);
//        vorticityImage.setOnScaleChangeListener(scaleChangedListener);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayList<String> postPathMultiple = displayIntent.getStringArrayListExtra("image-paths");
        double nMaxLower = displayIntent.getDoubleExtra("n-max-lower", 0);
        double maxDisplacement = displayIntent.getDoubleExtra("max-displacement", 0);
        String userName = displayIntent.getStringExtra("username");

        if (maxDisplacement < nMaxLower) {
            AlertDialog.Builder alertDialogParametersBuilder = new AlertDialog.Builder(ViewResultsActivity.this);
            alertDialogParametersBuilder.setTitle("Alert !");
            alertDialogParametersBuilder.setMessage(R.string.move_forward);
            alertDialogParametersBuilder.setCancelable(false);

            alertDialogParametersBuilder
                    .setNegativeButton(
                            "I Understand",
                            new DialogInterface
                                    .OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which)
                                {
                                    dialog.cancel();
                                }
                            });

            final AlertDialog alertDialogParameters = alertDialogParametersBuilder.create();
            alertDialogParameters.show();

        } else {
            AlertDialog.Builder alertDialogParametersBuilder = new AlertDialog.Builder(ViewResultsActivity.this);
            alertDialogParametersBuilder.setTitle("Alert !");
            alertDialogParametersBuilder.setMessage(R.string.final_display);
            alertDialogParametersBuilder.setCancelable(false);

            alertDialogParametersBuilder
                    .setNegativeButton(
                            "I Understand",
                            new DialogInterface
                                    .OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which)
                                {
                                    dialog.cancel();
                                }
                            });

            final AlertDialog alertDialogParameters = alertDialogParametersBuilder.create();
            alertDialogParameters.show();
        }

        // Setup images and paths
        imgFileToDisplay = postPathMultiple.get(0).split("/")[6].split(".png")[0]
                + "-"
                + postPathMultiple.get(1).split("/")[6].split("_")[3].split(".png")[0]+".png";
        storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);

        // Display base image (This will be changed when we add controls/buttons to results page)
        displayImage("Base", baseImage);

        // Display vorticity colormap (This will be changed when we add controls/buttons to results page)
        displayImage("Vorticity", vorticityImage);
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

    public void baseImageDisplay(View view) {
        displayImage("Base", baseImage);
    }

    public void vorticityImageDisplay(View view) {
        displayImage("Vorticity", vorticityImage);
    }

    public void singlePassDisplay(View view) {
        displayImage("SinglePass", vectorFieldImage);
    }

    public void singlePassReplaceDisplay(View view) {
        displayImage("Replaced", vectorFieldImage);
    }

    public void multiPassDisplay(View view) {
        displayImage("Multipass", vectorFieldImage);
    }

    public void multiPassReplaceDisplay(View view) {
        displayImage("Replaced2", vectorFieldImage);
    }

    private void displayImage(String step, ImageView imageContainer) {
        if (bmpHash.containsKey(step)) {
            imageContainer.setImageBitmap(bmpHash.get(step));
        } else {
            File pngFile = new File(storageDirectory, step + "_" + imgFileToDisplay);
            Bitmap bmp = BitmapFactory.decodeFile(String.valueOf(pngFile));
            bmpHash.put(step, bmp);
            imageContainer.setImageBitmap(bmp);
        }
    }
}
