package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jsibbold.zoomage.ZoomageView;
import com.onrpiv.uploadmedia.R;

import java.io.File;
import java.util.ArrayList;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class ViewResultsActivity extends AppCompatActivity {
    Button firstPass, secondPass, replaceAfterFirstPass, replaceAfterSecondPass;
    ZoomageView baseImage;
    ZoomageView vectorFieldImage;
    ZoomageView vorticityImage;
    private String userName;
    private String imgFileToDisplay;
    private File storageDirectory;
    private int selectedId;
    private double nMaxUpper, nMaxLower, maxDisplacement = 0.0;
    private ArrayList<String> postPathMultiple = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent displayIntent = getIntent();
        selectedId = displayIntent.getIntExtra("selection-Id", 0);
        if (selectedId == 0) {
            setContentView(R.layout.display_result_layout_null_replaced);
//            replaceAfterFirstPass = (Button) findViewById(R.id.firstReplace);
            replaceAfterSecondPass = (Button) findViewById(R.id.secondReplace);
        } else {
            setContentView(R.layout.display_result_layout);
        }
        baseImage = (ZoomageView)findViewById(R.id.baseZoomageView);
        vectorFieldImage = (ZoomageView)findViewById(R.id.vectorsZoomageView);
        vorticityImage = (ZoomageView)findViewById(R.id.vortZoomageView);
        firstPass = (Button) findViewById(R.id.firstPass);
        secondPass = (Button) findViewById(R.id.secondPass);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        postPathMultiple = displayIntent.getStringArrayListExtra("image-paths");
        nMaxLower = displayIntent.getDoubleExtra("n-max-lower",0);
        maxDisplacement = displayIntent.getDoubleExtra("max-displacement",0);
        userName = displayIntent.getStringExtra("username");
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
                +postPathMultiple.get(1).split("/")[6].split("_")[3].split(".png")[0]+".png";
        storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);

        // Display base image (This will be changed when we add controls/buttons to results page)
        String stepB = "Base";
        File basePngFile = new File(storageDirectory, stepB+"_"+imgFileToDisplay);
        if (basePngFile.exists() && baseImage != null) {
            baseImage.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(basePngFile)));
        }

        // Display vorticity colormap (This will be changed when we add controls/buttons to results page)
        String stepV = "Vorticity";
        File vortPngFile = new File(storageDirectory, stepV+"_"+imgFileToDisplay);
        if (vortPngFile.exists() && vorticityImage != null) {
            vorticityImage.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(vortPngFile)));
        }
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
        String step = "Base";
        File pngFile = new File(storageDirectory, step+"_"+imgFileToDisplay);
        baseImage.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(pngFile)));
    }

    public void vorticityImageDisplay(View view) {
        String step = "Vorticity";
        File pngFile = new File(storageDirectory, step+"_"+imgFileToDisplay);
        vorticityImage.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(pngFile)));
    }

    public void singlePassDisplay(View view) {
        String stepMulti = "SinglePass";
        File pngFile = new File(storageDirectory, stepMulti+"_"+imgFileToDisplay);
        vectorFieldImage.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(pngFile)));
    }

    public void singlePassReplaceDisplay(View view) {
        String stepMulti = "Replaced";
        File pngFile = new File(storageDirectory, stepMulti+"_"+imgFileToDisplay);
        vectorFieldImage.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(pngFile)));
    }

    public void multiPassDisplay(View view) {
        String stepMulti = "Multipass";
        File pngFile = new File(storageDirectory, stepMulti+"_"+imgFileToDisplay);
        vectorFieldImage.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(pngFile)));
    }

    public void multiPassReplaceDisplay(View view) {
        String stepMulti = "Replaced2";
        File pngFile = new File(storageDirectory, stepMulti+"_"+imgFileToDisplay);
        vectorFieldImage.setImageBitmap(BitmapFactory.decodeFile(String.valueOf(pngFile)));
    }
}
