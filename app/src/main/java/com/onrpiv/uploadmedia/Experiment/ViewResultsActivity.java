package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

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

enum ResultSettingsKeys {
    TRUE("true"),
    FALSE("false"),
    VEC_DISPLAY("vecDisplay"),
    VEC_OPTION("vecOption"),
    VEC_SINGLE("singlepass"),
    VEC_MULTI("multipass"),
    VEC_REPLACED("replaced"),
    ARROW_COLOR("arrowColor"),
    ARROW_SCALE("arrowScale"),
    VORT_DISPLAY("vortDisplay"),
    VORT_COLORS("vorColors"),
    VORT_TRANS_VALS("transVals"),
    BACKGROUND("background"),
    BACKGRND_SOLID("solid"),
    BACKGRND_IMG("image");

    public final String label;
    ResultSettingsKeys(String label) {
        this.label = label;
    }
}


public class ViewResultsActivity extends AppCompatActivity {
    // Widgets
    private RangeSlider rangeSlider;
    private ImageView baseImage, vectorFieldImage, vorticityImage;
    private Button arrowColor, vorticityColors, solidColor, applyButton;
    private SeekBar arrowScale;
    private SwitchCompat displayVectors, displayVorticity;
    private RadioGroup vectorRadioGroup, backgroundRadioGroup;
    private RadioButton singleRadio, multiRadio, replacementRadio, solidRadio, imageRadio;

    // paths
    private String imgFileToDisplay;
    private File storageDirectory;

    // dynamic storage
    private HashMap<String, Integer> colormapHash = loadColormapHash();
    private final HashMap<String, String> defaultSettings = loadDefaultSettings();
    private HashMap<String, String> currentSettings = new HashMap<>();
    private HashMap<String, Bitmap> bmpHash = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent displayIntent = getIntent();

        setContentView(R.layout.display_result_layout);

        loadDefaultSettings();

        // sliders
        rangeSlider = findViewById(R.id.rangeSeekBar);
        float[] rangeVals = getTransparentValues(defaultSettings);
        rangeSlider.setValues(rangeVals[0], rangeVals[1]);
        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                madeChange();
                currentSettings.put(ResultSettingsKeys.VORT_TRANS_VALS.label, String.valueOf((int) value));
            }
        });

        arrowScale = findViewById(R.id.arrow_scale);
        arrowScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                madeChange();
                currentSettings.put(ResultSettingsKeys.ARROW_SCALE.label, String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // image containers
        // TODO check listeners in Utilites.FrameView for interactive images
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
        displayVectors.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                madeChange();
                currentSettings.put(ResultSettingsKeys.VEC_DISPLAY.label, Boolean.toString(isChecked));
            }
        });

        displayVorticity = findViewById(R.id.vort_display);
        displayVorticity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                madeChange();
                currentSettings.put(ResultSettingsKeys.VORT_DISPLAY.label, Boolean.toString(isChecked));
            }
        });

        // radio buttons
        singleRadio = findViewById(R.id.singlepass);
        multiRadio = findViewById(R.id.multipass);
        replacementRadio = findViewById(R.id.replace);
        solidRadio = findViewById(R.id.plain);
        imageRadio = findViewById(R.id.base);

        // radio groups
        vectorRadioGroup = findViewById(R.id.vec_rgroup);
        vectorRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                madeChange();
                String value;
                switch (checkedId) {
                    case R.id.multipass:
                        value = ResultSettingsKeys.VEC_MULTI.label;
                        break;
                    case R.id.replace:
                        value = ResultSettingsKeys.VEC_REPLACED.label;
                        break;
                    default:
                        value = ResultSettingsKeys.VEC_SINGLE.label;
                }
                currentSettings.put(ResultSettingsKeys.VEC_OPTION.label, value);
            }
        });

        backgroundRadioGroup = findViewById(R.id.background_rgroup);
        backgroundRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                madeChange();
                String value;
                if (checkedId == R.id.plain) {
                    value = ResultSettingsKeys.BACKGRND_SOLID.label;
                } else {
                    value = ResultSettingsKeys.BACKGRND_IMG.label;
                }
                currentSettings.put(ResultSettingsKeys.BACKGROUND.label, value);
            }
        });

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

    public void applyDisplay(View view) {
        // TODO apply button clicked
    }

    public void OnClick_ArrowColor(View view) {
        // TODO
    }

    public void OnClick_VortColors(View view) {
        // TODO
    }

    public void OnClick_BackgroundColor(View view) {
        // TODO
    }

    public void baseImageDisplay() {
        displayImage("Base", baseImage);
    }

    public void vorticityImageDisplay() {
        displayImage("Vorticity", vorticityImage);
    }

    public void singlePassDisplay() {
        displayImage("SinglePass", vectorFieldImage);
    }

    public void singlePassReplaceDisplay(View view) {
        displayImage("Replaced", vectorFieldImage);
    }

    public void multiPassDisplay() {
        displayImage("Multipass", vectorFieldImage);
    }

    public void multiPassReplaceDisplay() {
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

    private HashMap<String, String> loadDefaultSettings() {
        HashMap<String, String> settings = new HashMap<>();
        settings.put(ResultSettingsKeys.VEC_DISPLAY.label, ResultSettingsKeys.FALSE.label);
        settings.put(ResultSettingsKeys.VEC_OPTION.label, ResultSettingsKeys.VEC_SINGLE.label);
        settings.put(ResultSettingsKeys.ARROW_COLOR.label, "red");
        settings.put(ResultSettingsKeys.ARROW_SCALE.label, "1.0");

        settings.put(ResultSettingsKeys.VORT_DISPLAY.label, ResultSettingsKeys.FALSE.label);
        settings.put(ResultSettingsKeys.VORT_COLORS.label, "jet");
        settings.put(ResultSettingsKeys.VORT_TRANS_VALS.label, "120,135");

        settings.put(ResultSettingsKeys.BACKGROUND.label, ResultSettingsKeys.BACKGRND_IMG.label);
        return settings;
    }

    private HashMap<String, Integer> loadColormapHash() {
        HashMap<String, Integer> colormap = new HashMap<>();
        colormap.put("autumn", Imgproc.COLORMAP_AUTUMN);
        colormap.put("bone", Imgproc.COLORMAP_BONE);
        colormap.put("cool", Imgproc.COLORMAP_COOL);
        colormap.put("hot", Imgproc.COLORMAP_HOT);
        colormap.put("hsv", Imgproc.COLORMAP_HSV);
        colormap.put("jet", Imgproc.COLORMAP_JET);
        colormap.put("ocean", Imgproc.COLORMAP_OCEAN);
        colormap.put("parula", Imgproc.COLORMAP_PARULA);
        colormap.put("pink", Imgproc.COLORMAP_PINK);
        colormap.put("rainbow", Imgproc.COLORMAP_RAINBOW);
        colormap.put("spring", Imgproc.COLORMAP_SPRING);
        colormap.put("summer", Imgproc.COLORMAP_SUMMER);
        colormap.put("winter", Imgproc.COLORMAP_WINTER);
        return colormap;
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
