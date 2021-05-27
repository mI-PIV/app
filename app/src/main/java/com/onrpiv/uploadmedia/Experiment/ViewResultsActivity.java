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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.slider.RangeSlider;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ArrowDrawOptions;
import com.onrpiv.uploadmedia.Utilities.ColorMap.ColorMap;
import com.onrpiv.uploadmedia.Utilities.ColorMap.ColorMapPicker;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;
import com.onrpiv.uploadmedia.Utilities.ResultSettings;
import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    // Widgets
    private RangeSlider rangeSlider;
    private ImageView baseImage, vectorFieldImage, vorticityImage;
    private Button arrowColor, vorticityColors, solidColor, applyButton;

    // paths
    private String imgFileToDisplay;
    private File outputDirectory;

    // maps and settings
    private HashMap<String, PivResultData> correlationMaps;

    private ArrayList<ColorMap> colorMaps;
    private ResultSettings settings;
    private int imageCounter = 0;

    // From Image Activity
    private PivResultData singlePass;
    private PivResultData multiPass;
    private PivResultData replacedPass;
    private int rows;
    private int cols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent displayIntent = getIntent();
        setContentView(R.layout.display_result_layout);

        // Bring in variables from ImageActivity
        Bundle extras = displayIntent.getExtras();

        singlePass = (PivResultData) extras.getSerializable(PivResultData.SINGLE);
        multiPass = (PivResultData) extras.getSerializable(PivResultData.MULTI);
        rows = singlePass.getRows();
        cols = singlePass.getCols();
        boolean replaced = (boolean) extras.get(PivResultData.REPLACED_BOOL);
        if (replaced) {
            replacedPass = (PivResultData) extras.getSerializable(PivResultData.REPLACE2);
        } else {
            RadioButton replacedRadioButton = findViewById(R.id.replace);
            replacedRadioButton.setVisibility(View.GONE);
        }

        // load our maps and settings
        correlationMaps = loadCorrelationMaps();
        colorMaps = ColorMap.loadColorMaps(this, getResources(), getPackageName());
        settings = new ResultSettings(this, getResources(), getPackageName());

        // sliders
        rangeSlider = findViewById(R.id.rangeSeekBar);
        float[] rangeVals = settings.getVortTransVals();
        rangeSlider.setValues(rangeVals[0], rangeVals[1]);
        rangeSlider.setMinSeparation(1f);
        rangeSlider.setStepSize(1f);
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

        SeekBar arrowScale = findViewById(R.id.arrow_scale);
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
        SwitchCompat displayVectors = findViewById(R.id.vec_display);
        displayVectors.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setVecDisplay(isChecked);
                applyButton.setEnabled(true);
            }
        });

        SwitchCompat displayVorticity = findViewById(R.id.vort_display);
        displayVorticity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setVortDisplay(isChecked);
                applyButton.setEnabled(true);
            }
        });

        // radio groups
        RadioGroup vectorRadioGroup = findViewById(R.id.vec_rgroup);
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

        RadioGroup backgroundRadioGroup = findViewById(R.id.background_rgroup);
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
//        double nMaxLower = displayIntent.getDoubleExtra("n-max-lower", 0);
//        double maxDisplacement = displayIntent.getDoubleExtra("max-displacement", 0);
//        popups(nMaxLower, maxDisplacement);

        // Setup images and paths
        String userName = displayIntent.getStringExtra(PivResultData.USERNAME);
        int currentExpDir = PersistedData.getTotalExperiments(this, userName);
        imgFileToDisplay = PathUtil.getExperimentImageFileSuffix(currentExpDir);
        outputDirectory = PathUtil.getExperimentNumberedDirectory(userName, currentExpDir);

        // Display base image
        displayBaseImage(BACKGRND_IMG);
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
        // Vector Field
        if (settings.vecFieldChanged && settings.getVecDisplay()) {
            displayVectorImage(correlationMaps.get(settings.getVecOption()));
        } else if (!settings.getVecDisplay()) {
            vectorFieldImage.setVisibility(View.INVISIBLE);
        }

        // Vorticity
        if (settings.vortMapChanged && settings.getVortDisplay()) {
            displayVortImage(correlationMaps.get(settings.getVecOption()));
        } else if (!settings.getVortDisplay()) {
            vorticityImage.setVisibility(View.INVISIBLE);
            vorticityImage.invalidate();
        }

        // Background
        if (settings.backgroundChanged) {
            displayBaseImage(settings.getBackground());
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

    public void OnClick_SaveImage(View view) {
        ImageButton saveImageButton = findViewById(R.id.imageSaveButton);
        saveImageButton.setEnabled(false);
        saveImageButton.setBackgroundColor(Color.parseColor("#576674"));

        View imageStack = findViewById(R.id.img_frame);
        imageStack.setDrawingCacheEnabled(true);
        Bitmap bmp = imageStack.getDrawingCache();
        File pngFile = new File(outputDirectory, "Saved_Image_" + imageCounter++ + ".png");

        try {
            if (!pngFile.exists()) {
                pngFile.createNewFile();
            }
            FileOutputStream ostream = new FileOutputStream(pngFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            ostream.close();
            imageStack.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            imageStack.setDrawingCacheEnabled(false);
        }

        Toast.makeText(this, "Current image saved as " + pngFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        saveImageButton.setEnabled(true);
        saveImageButton.setBackgroundColor(Color.parseColor("#243EDF"));
    }

    private void displayVectorImage(PivResultData pivResultData) {
        Bitmap bmp = createVectorFieldBitmap(pivResultData);
        vectorFieldImage.setImageBitmap(bmp);
        vectorFieldImage.setVisibility(View.VISIBLE);
    }

    private void displayVortImage(PivResultData pivResultData) {
        Bitmap bmp = createVorticityBitmap(pivResultData);
        vorticityImage.setImageBitmap(bmp);
        vorticityImage.setVisibility(View.VISIBLE);
    }

    private void displayBaseImage(String backgroundCode) {
        if (backgroundCode.equals(BACKGRND_IMG)) {
            File pngFile = new File(outputDirectory, "Base_" + imgFileToDisplay);
            Bitmap bmp = BitmapFactory.decodeFile(pngFile.getAbsolutePath());
            baseImage.setImageBitmap(bmp);
        } else {
            Bitmap bmp = createSolidBaseImage();
            baseImage.setImageBitmap(bmp);
        }
    }

    private Bitmap createVorticityBitmap(PivResultData pivResultData) {
        return PivFunctions.createColorMapBitmap(
                pivResultData.getVorticityValues(),
                settings.getVortTransVals_min(),
                settings.getVortTransVals_max(),
                settings.getVortColorMap().getOpenCVCode()
        );
    }

    private Bitmap createVectorFieldBitmap(PivResultData pivResultData) {
        return PivFunctions.createVectorFieldBitmap(
                pivResultData,
                new ArrowDrawOptions(Color.valueOf(settings.getArrowColor()), settings.getArrowScale()),
                rows,
                cols);
    }

    private Bitmap createSolidBaseImage() {
        Rect rect = new Rect(0, 0, 2560, 1440);
        Bitmap bmp = Bitmap.createBitmap(rect.right, rect.bottom, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(settings.getBackgroundColor());
        canvas.drawRect(rect, paint);
        return bmp;
    }

    private ArrayList<Drawable> getColorMapDrawables() {
        ArrayList<Drawable> result = new ArrayList<>();
        for (ColorMap colorMap : colorMaps) {
            result.add(colorMap.getDrawable());
        }
        return result;
    }

    private HashMap<String, PivResultData> loadCorrelationMaps() {
        HashMap<String, PivResultData> result = new HashMap<>();
        result.put(VEC_SINGLE, singlePass);
        result.put(VEC_REPLACED, replacedPass);
        result.put(VEC_MULTI, multiPass);
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
