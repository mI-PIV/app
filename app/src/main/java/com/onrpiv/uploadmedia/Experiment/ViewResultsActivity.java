package com.onrpiv.uploadmedia.Experiment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.RangeSlider;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ColorMap.ColorMap;
import com.onrpiv.uploadmedia.Utilities.ColorMap.ColorMapPicker;
import com.onrpiv.uploadmedia.Utilities.ResultSettings;

import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import petrov.kristiyan.colorpicker.ColorPicker;

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
    // Settings "Enum"



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
    private ArrayList<ColorMap> colorMaps;
    private ResultSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent displayIntent = getIntent();

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
                madeChange();
                List<Float> vals = rangeSlider.getValues();
                settings.setVortTransVals_min(Math.min(vals.get(0), vals.get(1)));
                settings.setVortTransVals_max(Math.max(vals.get(0), vals.get(1)));
            }
        });

        arrowScale = findViewById(R.id.arrow_scale);
        arrowScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                madeChange();
                settings.setArrowScale(progress);
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
        // TODO display the selected colors, maybe make the buttons the colors and add "color" text
        arrowColor = findViewById(R.id.vect_color);
        arrowColor.setBackgroundColor(settings.getArrowColor());

        vorticityColors = findViewById(R.id.vort_color);
        vorticityColors.setBackground(settings.getVortColorMap().getDrawable());

        solidColor = findViewById(R.id.background_color);
        solidColor.setBackgroundColor(settings.getBackgroundColor());

        applyButton = findViewById(R.id.apply);
        applyButton.setEnabled(false);

        // switches
        displayVectors = findViewById(R.id.vec_display);
        displayVectors.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                madeChange();
                settings.setVecDisplay(isChecked);
            }
        });

        displayVorticity = findViewById(R.id.vort_display);
        displayVorticity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                madeChange();
                settings.setVortDisplay(isChecked);
            }
        });

        // radio buttons
//        singleRadio = findViewById(R.id.singlepass);
//        multiRadio = findViewById(R.id.multipass);
//        replacementRadio = findViewById(R.id.replace);
//        solidRadio = findViewById(R.id.plain);
//        imageRadio = findViewById(R.id.base);

        // radio groups
        vectorRadioGroup = findViewById(R.id.vec_rgroup);
        vectorRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                madeChange();
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
            }
        });

        backgroundRadioGroup = findViewById(R.id.background_rgroup);
        backgroundRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                madeChange();
                String value;
                if (checkedId == R.id.plain) {
                    value = BACKGRND_SOLID;
                } else {
                    value = BACKGRND_IMG;
                }
                settings.setBackground(value);
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
        displayImage("Base", baseImage);
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
        // TODO this is the big one
    }

    public void OnClick_ArrowColor(View view) {
        ColorPicker colorPicker = new ColorPicker(this);
        colorPicker.setColors(ResultSettings.getColors());
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                madeChange();
                settings.setArrowColor(color);
                arrowColor.setBackgroundColor(color);
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
                madeChange();
                for (ColorMap colorMap : colorMaps) {
                    if (colorMap.getDrawable() == color) {
                        settings.setVortColorMap(colorMap);
                        vorticityColors.setBackground(colorMap.getDrawable());
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
                madeChange();
                settings.setBackgroundColor(color);
                solidColor.setBackgroundColor(color);
            }

            @Override
            public void onCancel() {
                //EMPTY
            }
        }).setDefaultColorButton(settings.getBackgroundColor()).show();
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
        settings = new ResultSettings(this, getResources(), getPackageName());
    }

    private ArrayList<Drawable> getColorMapDrawables() {
        ArrayList<Drawable> result = new ArrayList<>();
        for (ColorMap colorMap : colorMaps) {
            result.add(colorMap.getDrawable());
        }
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
