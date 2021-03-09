package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.slider.RangeSlider;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ArrowDrawOptions;
import com.onrpiv.uploadmedia.Utilities.ColorMap.ColorMap;
import com.onrpiv.uploadmedia.Utilities.ColorMap.ColorMapPicker;
import com.onrpiv.uploadmedia.Utilities.ResultSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import petrov.kristiyan.colorpicker.ColorPicker;

import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;

import org.opencv.core.Mat;

import static com.onrpiv.uploadmedia.Utilities.ResultSettings.BACKGRND_IMG;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.BACKGRND_SOLID;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.VEC_MULTI;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.VEC_REPLACED;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.VEC_SINGLE;

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
    private RadioGroup vectorRadioGroup, backgroundRadioGroup;
//    private RadioButton singleRadio, multiRadio, replacementRadio, solidRadio, imageRadio;

    // paths
    private String imgFileToDisplay;
    private File storageDirectory;

    private HashMap<String, Bitmap> bmpHash = new HashMap<>();
    private HashMap<String, HashMap<String, double[][]>> correlationMaps;
    private ArrayList<ColorMap> colorMaps;
    private ResultSettings settings;

    // From Image Activity
    private HashMap<String, double[][]> pivCorrelation;
    private HashMap<String, double[]> interrCenters;
    private double[][] vorticityValues;
    private HashMap<String, double[][]> pivCorrelationMulti;
    private HashMap<String, double[][]> pivReplaceMissing2;
    private int rows;
    private int cols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent displayIntent = getIntent();

        Bundle extras = displayIntent.getExtras();
        assert extras != null;
        pivCorrelation = (HashMap<String, double[][]>) extras.getSerializable("pivCorrelation");
        interrCenters = (HashMap<String, double[]>) extras.getSerializable("interrCenters");
        vorticityValues = (double[][]) extras.get("vorticityValues");
        rows = (int) extras.get("rows");
        cols = (int) extras.get("cols");
        pivReplaceMissing2 = (HashMap<String, double[][]>) extras.getSerializable("pivReplaceMissing2");
        pivCorrelationMulti = (HashMap<String, double[][]>) extras.getSerializable("pivCorrelationMulti");

        correlationMaps = loadCorrelationMaps();

        setContentView(R.layout.display_result_layout);

        colorMaps = ColorMap.loadColorMaps(this, getResources(), getPackageName());
        settings = new ResultSettings(this, getResources(), getPackageName());

        // sliders
        rangeSlider = findViewById(R.id.rangeSeekBar);
        float[] rangeVals = settings.getVortTransVals();
        rangeSlider.setValues(rangeVals[0], rangeVals[1]);
        rangeSlider.setMinSeparation(1f);
        rangeSlider.setStepSize(1f);
        // TODO display numbers above thumbs
        // https://developer.android.com/reference/com/google/android/material/slider/RangeSlider
        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                List<Float> vals = rangeSlider.getValues();
                settings.setVortTransVals_min(Math.min(vals.get(0), vals.get(1)));
                settings.setVortTransVals_max(Math.max(vals.get(0), vals.get(1)));
                applyButton.setEnabled(true);
            }
        });

        arrowScale = findViewById(R.id.arrow_scale);
        arrowScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settings.setArrowScale(progress);
                applyButton.setEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //EMPTY
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //EMPTY
            }
        });

        // image containers
        // TODO check listeners in Utilites.FrameView for interactive images
        baseImage = findViewById(R.id.baseView);
        vectorFieldImage = findViewById(R.id.vectorsView);
        vorticityImage = findViewById(R.id.vortView);

        // buttons
        applyButton = findViewById(R.id.apply);
        applyButton.setEnabled(false);

        arrowColor = findViewById(R.id.vect_color);
        arrowColor.setBackgroundColor(settings.getArrowColor());

        vorticityColors = findViewById(R.id.vort_color);
        vorticityColors.setBackground(settings.getVortColorMap().getDrawable());

        solidColor = findViewById(R.id.background_color);
        solidColor.setBackgroundColor(settings.getBackgroundColor());

        // switches
        displayVectors = findViewById(R.id.vec_display);
        displayVectors.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setVecDisplay(isChecked);
                applyButton.setEnabled(true);
            }
        });

        displayVorticity = findViewById(R.id.vort_display);
        displayVorticity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setVortDisplay(isChecked);
                applyButton.setEnabled(true);
            }
        });

        // radio buttons
        // TODO radio groups listener doesn't work, need to use buttons
//        singleRadio = findViewById(R.id.singlepass);
//        multiRadio = findViewById(R.id.multipass);
//        replacementRadio = findViewById(R.id.replace);
//        solidRadio = findViewById(R.id.plain);
//        imageRadio = findViewById(R.id.base);

        // radio groups
        // TODO this doesn't work, use buttons instead
        vectorRadioGroup = findViewById(R.id.vec_rgroup);
        vectorRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String value;
                switch (checkedId) {
                    case R.id.multipass:
                        value = VEC_MULTI;
                        break;
                    case R.id.replace:
                        value = VEC_REPLACED;
                        break;
                    default:
                        value = VEC_SINGLE;
                }
                settings.setVecOption(value);
                applyButton.setEnabled(true);
            }
        });

        // TODO this doesn't work use buttons instead
        backgroundRadioGroup = findViewById(R.id.background_rgroup);
        backgroundRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String value;
                if (checkedId == R.id.plain) {
                    value = BACKGRND_SOLID;
                } else {
                    value = BACKGRND_IMG;
                }
                settings.setBackground(value);
                applyButton.setEnabled(true);
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

        // Display base image
        displayBaseImage("background" + BACKGRND_IMG, BACKGRND_IMG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void applyDisplay(View view) {
        // TODO apply button clicked
        // TODO "loading" popup uninterruptable for each intensive change

        // Vector Field
        vectorFieldImage.setVisibility(settings.getVecDisplay()? View.VISIBLE : View.INVISIBLE);
        if (settings.vecFieldChanged && settings.getVecDisplay()) {
            // TODO loading popup with relevant info
            HashMap<String, double[][]> correlation = correlationMaps.get(settings.getVecOption());
            String key = "vec"
                    + settings.getVecOption()
                    + settings.getArrowColor()
                    + settings.getArrowScale();
            displayVectorImage(key, correlation);
        }

        // Vorticity
        vorticityImage.setVisibility(settings.getVortDisplay()? View.VISIBLE : View.INVISIBLE);
        if (settings.vortMapChanged && settings.getVortDisplay()) {
            // TODO loading popup with relevant info
            String key = "vort"
                    + settings.getVortColorMap().getOpenCVCode()
                    + settings.getVortTransVals_min()
                    + settings.getVortTransVals_max();
            displayVortImage(key);
        }

        // Background
        if (settings.backgroundChanged) {
            String key;
            if (settings.getBackground().equals(BACKGRND_IMG)) {
                key = "background" + BACKGRND_IMG;
            } else {
                key = "background"
                        + BACKGRND_SOLID
                        + settings.getBackgroundColor();
            }
            displayBaseImage(key, settings.getBackground());
        }

        // reset detected changes
        settings.resetBools();
        applyButton.setEnabled(false);
    }

    public void OnClick_ArrowColor(View view) {
        ColorPicker colorPicker = new ColorPicker(this);
        colorPicker.setColors(ResultSettings.getColors());
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                settings.setArrowColor(color);
                arrowColor.setBackgroundColor(color);
                applyButton.setEnabled(true);
            }

            @Override
            public void onCancel() {
                //EMPTY
            }
        }).setDefaultColorButton(settings.getArrowColor()).show();
    }

    public void OnClick_VortColors(View view) {
        ColorMapPicker colorMapPicker = new ColorMapPicker(this);
        ArrayList<Drawable> drawables = getColorMapDrawables();
        colorMapPicker.setColors(drawables);
        colorMapPicker.setOnFastChooseColorListener(new ColorMapPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, Drawable color) {
                for (ColorMap colorMap : colorMaps) {
                    if (colorMap.getDrawable() == color) {
                        settings.setVortColorMap(colorMap);
                        vorticityColors.setBackground(colorMap.getDrawable());
                        applyButton.setEnabled(true);
                        return;
                    }
                }
            }

            @Override
            public void onCancel() {
                //EMPTY
            }
        }).setDefaultColorButton(settings.getVortColorMap().getDrawable()).show();
    }

    public void OnClick_BackgroundColor(View view) {
        ColorPicker colorPicker = new ColorPicker(this);
        colorPicker.setColors(ResultSettings.getColors());
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                settings.setBackgroundColor(color);
                solidColor.setBackgroundColor(color);
                applyButton.setEnabled(true);
            }

            @Override
            public void onCancel() {
                //EMPTY
            }
        }).setDefaultColorButton(settings.getBackgroundColor()).show();
    }

    private void displayVectorImage(String key, HashMap<String, double[][]> correlation) {
        if (bmpHash.containsKey(key)) {
            vectorFieldImage.setImageBitmap(bmpHash.get(key));
        } else {
            Bitmap bmp = calculateVectorField(correlation);
            bmpHash.put(key, bmp);
            vectorFieldImage.setImageBitmap(bmp);
        }
    }

    private void displayVortImage(String key) {
        if (bmpHash.containsKey(key)) {
            vorticityImage.setImageBitmap(bmpHash.get(key));
        } else {
            Bitmap bmp = calculateVorticity();
            bmpHash.put(key, bmp);
            vorticityImage.setImageBitmap(bmp);
        }
    }

    private void displayBaseImage(String key, String backgroundCode) {
        if (bmpHash.containsKey(key)) {
            baseImage.setImageBitmap(bmpHash.get(key));
        } else {
            if (backgroundCode.equals(BACKGRND_IMG)) {
                File pngFile = new File(storageDirectory, "Base" + "_" + imgFileToDisplay);
                Bitmap bmp = BitmapFactory.decodeFile(pngFile.getAbsolutePath());
                baseImage.setImageBitmap(bmp);
            } else {
                Bitmap bmp = calculateSolidBaseImage();
                bmpHash.put(key, bmp);
                baseImage.setImageBitmap(bmp);
            }
        }
    }

    private Bitmap calculateVorticity() {
        return PivFunctions.createColorMapBitmap(
                vorticityValues,
                settings.getVortTransVals_min(),
                settings.getVortTransVals_max(),
                settings.getVortColorMap().getOpenCVCode()
        );
    }

    private Bitmap calculateVectorField(HashMap<String, double[][]> correlation) {
        return PivFunctions.createVectorFieldBitmap(
                correlation,
                interrCenters,
                new ArrowDrawOptions(Color.valueOf(settings.getArrowColor()), settings.getArrowScale()),
                rows,
                cols);
    }

    private Bitmap calculateSolidBaseImage() {
        Rect rect = new Rect(0, 0, 2560, 1440);
        Bitmap bmp = Bitmap.createBitmap(rect.right, rect.bottom, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(settings.getBackgroundColor());
        canvas.drawRect(rect, paint);
        return bmp;
    }

    private void resetDefault() {
        applyButton.setEnabled(false);
        settings = new ResultSettings(this, getResources(), getPackageName());
    }

    private ArrayList<Drawable> getColorMapDrawables() {
        ArrayList<Drawable> result = new ArrayList<>();
        for (ColorMap colorMap : colorMaps) {
            result.add(colorMap.getDrawable());
        }
        return result;
    }

    private HashMap<String, HashMap<String, double[][]>> loadCorrelationMaps() {
        HashMap<String, HashMap<String, double[][]>> result = new HashMap<>();
        result.put(VEC_SINGLE, pivCorrelation);
        result.put(VEC_REPLACED, pivReplaceMissing2);
        result.put(VEC_MULTI, pivCorrelationMulti);
        return result;
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
