package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.google.android.material.slider.RangeSlider;
import com.onrpiv.uploadmedia.R;

import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sarbajit mukherjee on 09/07/2020.
 * Edited by KP on 02/18/2021
 */

public class ViewResultsActivity extends AppCompatActivity {
    // Widgets
    private RangeSlider rangeSlider;
    private ImageView baseImage, vectorFieldImage, vorticityImage;
    private Button arrowColor, vorticityColors, solidColor, applyButton;
    private SeekBar arrowScale;
    private SwitchCompat displayVectors, displayVorticity;
    private RadioButton singleRadio, multiRadio, replacementRadio, solidRadio, imageRadio;

    // paths
    private String imgFileToDisplay;
    private File storageDirectory;

    // dynamic storage
    private final HashMap<String, String> defaultSettings = new HashMap<>();
    private HashMap<String, String> currentSettings = new HashMap<>();
    private HashMap<String, Integer> colormapHash = new HashMap<>();
    private HashMap<String, Bitmap> bmpHash = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent displayIntent = getIntent();

        setContentView(R.layout.display_result_layout_null_replaced);

        loadDefaultSettings();
        loadColormapHash();

        // sliders
        rangeSlider = findViewById(R.id.rangeSeekBar);
        float[] rangeVals = getTransparentValues(defaultSettings);
        rangeSlider.setValues(rangeVals[0], rangeVals[1]);

        arrowScale = findViewById(R.id.arrow_scale);

        // image containers
        baseImage = findViewById(R.id.baseView);
        vectorFieldImage = findViewById(R.id.vectorsView);
        vorticityImage = findViewById(R.id.vortView);

        // buttons
        arrowColor = findViewById(R.id.vect_color);
        vorticityColors = findViewById(R.id.vort_color);
        solidColor = findViewById(R.id.background_color);
        applyButton = findViewById(R.id.apply);

        // switches
        displayVectors = findViewById(R.id.vec_display);
        displayVorticity = findViewById(R.id.vort_display);

        // radio buttons
        singleRadio = findViewById(R.id.singlepass);
        multiRadio = findViewById(R.id.multipass);
        replacementRadio = findViewById(R.id.replace);
        solidRadio = findViewById(R.id.plain);
        imageRadio = findViewById(R.id.base);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // popups
        double nMaxLower = displayIntent.getDoubleExtra("n-max-lower", 0);
        double maxDisplacement = displayIntent.getDoubleExtra("max-displacement", 0);
        popups(nMaxLower, maxDisplacement);

        // Setup images and paths
        ArrayList<String> postPathMultiple = displayIntent.getStringArrayListExtra("image-paths");
        String userName = displayIntent.getStringExtra("username");

        imgFileToDisplay = postPathMultiple.get(0).split("/")[6].split(".png")[0]
                + "-"
                + postPathMultiple.get(1).split("/")[6].split("_")[3].split(".png")[0]+".png";
        storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);

        // Display base image (This will be changed when we add controls/buttons to results page)
        displayImage("Base", baseImage);
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

    private void madeChange() {
        applyButton.setEnabled(true);
    }

    private void resetDefault() {
        applyButton.setEnabled(false);
        currentSettings = deepCopy(defaultSettings);
    }

    private float[] getTransparentValues(HashMap<String, String> map) {
        float[] vals = new float[2];
        if (map.containsKey("transVals")) {
            String rawValue = map.get("transVals");
            String[] stringVals = rawValue.split(",");
            vals[0] = Float.parseFloat(stringVals[0]);
            vals[1] = Float.parseFloat(stringVals[1]);
        }
        return vals;
    }

    private double getDoubleSetting(String key, HashMap<String, String> map) {
        double result = 1d;
        if (map.containsKey(key)) {
            result = Double.parseDouble(map.get(key));
        }
        return result;
    }

    private boolean getBooleanSetting(String key, HashMap<String, String> map) {
        boolean result = false;
        if (map.containsKey(key)) {
            result = Boolean.parseBoolean(map.get(key));
        }
        return result;
    }

    private int getColorSetting(String key, HashMap<String, String> map) {
         // red, blue, green, black, white, gray, cyan, magenta, yellow, lightgray, darkgray,
         // grey, lightgrey, darkgrey, aqua, fuchsia, lime, maroon, navy, olive, purple,
         // silver, and teal.
        int color = Color.BLACK;
        if (map.containsKey(key)) {
            color = Color.parseColor(map.get(key));
        }
        return color;
    }

    private int getColorMapSetting(HashMap<String, String> map) {
        int colormap = Imgproc.COLORMAP_JET;
        if (map.containsKey("vortColors") && colormapHash.containsKey(map.get("vortColors"))) {
            colormap = colormapHash.get(map.get("vortColors"));
        }
        return colormap;
    }

    private void loadDefaultSettings() {
        defaultSettings.clear();
        defaultSettings.put("vecDisplay", "false");
        defaultSettings.put("vecOption", "singlepass");
        defaultSettings.put("arrowColor", "red");
        defaultSettings.put("arrowScale", "1.0");

        defaultSettings.put("vortDisplay", "false");
        defaultSettings.put("vortColors", "jet");
        defaultSettings.put("transVals", "120,135");

        defaultSettings.put("background", "image");
    }

    private void loadColormapHash() {
        colormapHash.put("autumn", Imgproc.COLORMAP_AUTUMN);
        colormapHash.put("bone", Imgproc.COLORMAP_BONE);
        colormapHash.put("cool", Imgproc.COLORMAP_COOL);
        colormapHash.put("hot", Imgproc.COLORMAP_HOT);
        colormapHash.put("hsv", Imgproc.COLORMAP_HSV);
        colormapHash.put("jet", Imgproc.COLORMAP_JET);
        colormapHash.put("ocean", Imgproc.COLORMAP_OCEAN);
        colormapHash.put("parula", Imgproc.COLORMAP_PARULA);
        colormapHash.put("pink", Imgproc.COLORMAP_PINK);
        colormapHash.put("rainbow", Imgproc.COLORMAP_RAINBOW);
        colormapHash.put("spring", Imgproc.COLORMAP_SPRING);
        colormapHash.put("summer", Imgproc.COLORMAP_SUMMER);
        colormapHash.put("winter", Imgproc.COLORMAP_WINTER);
    }

    private static HashMap<String, String> deepCopy(HashMap<String, String> orig) {
        HashMap<String, String> copy = new HashMap<>();
        for (Map.Entry<String, String> entry : orig.entrySet()) {
            copy.put(entry.getKey(), new String(entry.getValue()));
        }
        return copy;
    }

    private void popups(double nMaxLower, double maxDisplacement) {
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
    }
}
