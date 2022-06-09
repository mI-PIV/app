package com.onrpiv.uploadmedia.Experiment;

import static com.onrpiv.uploadmedia.Utilities.ResultSettings.BACKGRND_IMG;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.BACKGRND_SOLID;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.BACKGRND_SUB;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.BCKGRND_FRAME_PRETTY;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.BCKGRND_SUB_PRETTY;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.VEC_MULTI;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.VEC_REPLACED;
import static com.onrpiv.uploadmedia.Utilities.ResultSettings.VEC_SINGLE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.slider.RangeSlider;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ArrowDrawOptions;
import com.onrpiv.uploadmedia.Utilities.BackgroundSub;
import com.onrpiv.uploadmedia.Utilities.ColorMap.ColorMap;
import com.onrpiv.uploadmedia.Utilities.ColorMap.ColorMapPicker;
import com.onrpiv.uploadmedia.Utilities.FrameView;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;
import com.onrpiv.uploadmedia.Utilities.PositionCallback;
import com.onrpiv.uploadmedia.Utilities.ResultSettings;
import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import petrov.kristiyan.colorpicker.ColorPicker;

/**
 * Created by sarbajit mukherjee on 09/07/2020.
 * Edited by KP on 02/18/2021
 */

public class ViewResultsActivity extends AppCompatActivity implements PositionCallback {
    // Widgets
    private RangeSlider rangeSlider;
    private ImageView baseImage, vectorFieldImage, vorticityImage;
    private Button arrowColor, vorticityColors, solidColor, applyButton, selectColor;

    // paths
    private String imgFileToDisplay;
    private File outputDirectory;

    // maps and settings
    private HashMap<String, PivResultData> correlationMaps;
    private HashMap<View, LinearLayout> sectionMaps;
    private ArrayList<ColorMap> colorMaps;
    private ResultSettings settings;
    private int imageCounter = 0;

    // info section
    private ImageView selectionImage;
    private TextView infoText;
    private float currentX;
    private float currentY;
    private static float conversionFactor;

    // From Image Activity
    public static PivResultData singlePass;
    public static PivResultData multiPass;
    public static PivResultData replacedPass;
    public static boolean calibrated;
    public static boolean backgroundSubtracted;
    private int rows;
    private int cols;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent displayIntent = getIntent();
        setContentView(R.layout.display_result_layout);

        OpenCVLoader.initDebug();

        // Bring in variables from ImageActivity
        Bundle extras = displayIntent.getExtras();

        rows = singlePass.getRows();
        cols = singlePass.getCols();

        // determine if replaced vectors are an option for the user
        boolean replaced = (boolean) extras.get(PivResultData.REPLACED_BOOL);

        // load our maps and settings
        correlationMaps = loadCorrelationMaps(replaced);
        colorMaps = ColorMap.loadColorMaps(this);
        settings = new ResultSettings(this);

        // sliders
        rangeSlider = findViewById(R.id.rangeSeekBar);
        float[] rangeVals = settings.getVortTransVals();
        rangeSlider.setValues(rangeVals[0], rangeVals[1]);
        rangeSlider.setMinSeparation(1f);
        rangeSlider.setStepSize(1f);
        // https://developer.android.com/reference/com/google/android/material/slider/RangeSlider
        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                List<Float> vals = rangeSlider.getValues();
                settings.setVortTransVals_min(Math.min(vals.get(0), vals.get(1)));
                settings.setVortTransVals_max(Math.max(vals.get(0), vals.get(1)));
                applyButton.setEnabled(true);
            }
        });

        SeekBar arrowScale = findViewById(R.id.arrow_scale);
        settings.setArrowScale(arrowScale.getProgress());
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
        FrameView imagesDisplay = findViewById(R.id.img_frame);
        imagesDisplay.setPositionCallback(this);  // callback sets the current selected x and y
        baseImage = findViewById(R.id.baseView);
        vectorFieldImage = findViewById(R.id.vectorsView);
        vorticityImage = findViewById(R.id.vortView);
        selectionImage = findViewById(R.id.selectionView);

        // text views
        infoText = findViewById(R.id.infoText);

        // buttons
        applyButton = findViewById(R.id.apply);
        applyButton.setEnabled(false);

        arrowColor = findViewById(R.id.vect_color);
        arrowColor.setBackgroundColor(settings.getArrowColor());

        vorticityColors = findViewById(R.id.vort_color);
        vorticityColors.setBackground(settings.getVortColorMap().getDrawable());

        solidColor = findViewById(R.id.background_color);
        solidColor.setBackgroundColor(settings.getBackgroundColor());

        selectColor = findViewById(R.id.select_color);
        selectColor.setBackgroundColor(settings.getSelectColor());

        LinearLayout backgroundColorPicker = findViewById(R.id.bg_color_picker);

        // spinners
        Spinner backgroundSpinner = findViewById(R.id.bg_spinner);
        List<String> bgOptionsList = new ArrayList<String>();
        bgOptionsList.add(ResultSettings.BCKGRND_SOLID_PRETTY);
        bgOptionsList.add(ResultSettings.BCKGRND_FRAME_PRETTY);
        if (backgroundSubtracted) {
            bgOptionsList.add(ResultSettings.BCKGRND_SUB_PRETTY);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, bgOptionsList);
        backgroundSpinner.setAdapter(adapter);
        backgroundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                switch (selected) {
                    case BCKGRND_FRAME_PRETTY:
                        settings.setBackground(BACKGRND_IMG);
                        if (backgroundColorPicker.getVisibility() == View.VISIBLE)
                            backgroundColorPicker.setVisibility(View.GONE);
                        break;
                    case BCKGRND_SUB_PRETTY:
                        settings.setBackground(BACKGRND_SUB);
                        if (backgroundColorPicker.getVisibility() == View.VISIBLE)
                            backgroundColorPicker.setVisibility(View.GONE);
                        break;
                    default:
                        settings.setBackground(BACKGRND_SOLID);
                        backgroundColorPicker.setVisibility(View.VISIBLE);
                }
                applyButton.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // EMPTY
            }
        });

        // drop-downs
        sectionMaps = loadDropDownMaps();

        // switches
        SwitchCompat displayVectors = findViewById(R.id.vec_display);
        displayVectors.setChecked(settings.getVecDisplay());
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

        SwitchCompat calibrationSwitch = findViewById(R.id.results_calib_switch);
        calibrationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setCalibrated(isChecked);
                applyButton.setEnabled(true);
                settings.selectionChanged = true;
            }
        });
        calibrationSwitch.setVisibility(calibrated? View.VISIBLE : View.GONE);

        // radio groups
        RadioGroup vectorRadioGroup = findViewById(R.id.postp_rgroup);
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

        if (!replaced) {
            RadioButton replacedRadioButton = findViewById(R.id.replace);
            replacedRadioButton.setVisibility(View.GONE);
            settings.setVecOption(VEC_MULTI);
            vectorRadioGroup.check(R.id.multipass);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // popups
//        double nMaxLower = displayIntent.getDoubleExtra("n-max-lower", 0);
//        double maxDisplacement = displayIntent.getDoubleExtra("max-displacement", 0);
//        popups(nMaxLower, maxDisplacement);

        // Setup images and paths
        String userName = displayIntent.getStringExtra(PivResultData.USERNAME);
        int currentExpDir = PersistedData.getTotalExperiments(this, userName);
        imgFileToDisplay = PathUtil.getExperimentImageFileSuffix(currentExpDir);
        outputDirectory = PathUtil.getExperimentNumberedDirectory(this, userName, currentExpDir);

        // Defaults
        displayBaseImage(BACKGRND_SOLID);
        displayVectorImage(correlationMaps.get(settings.getVecOption()));
        applyDisplay(applyButton);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // paths
        outState.putString("imgFileToDisplay", imgFileToDisplay);
        outState.putString("outputDirectory", outputDirectory.getAbsolutePath());

        // maps and settings
        outState = settings.saveInstanceBundle(outState);
        outState.putInt("imageCounter", imageCounter);

        // info
        outState.putFloat("currentX", currentX);
        outState.putFloat("currentY", currentY);

        // from image activity
        outState.putInt("rows", rows);
        outState.putInt("cols", cols);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // paths
        imgFileToDisplay = savedInstanceState.getString("imgFileToDisplay");
        outputDirectory = new File(savedInstanceState.getString("outputDirectory"));

        // maps and settings
        settings = new ResultSettings(this).loadInstanceBundle(savedInstanceState);
        imageCounter = savedInstanceState.getInt("imageCounter");

        // info
        currentX = savedInstanceState.getFloat("currentX");
        currentY = savedInstanceState.getFloat("currentY");

        // from image activity
        rows = savedInstanceState.getInt("rows");
        cols = savedInstanceState.getInt("cols");

        // reload the display
        settings.vecFieldChanged = true;
        settings.vortMapChanged = true;
        settings.selectionChanged = true;
        settings.backgroundChanged = true;
        applyDisplay(applyButton);
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

        // Selection/Info
        if (settings.selectionChanged) {
            getSelectPosition(currentX, currentY);
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

    public void OnClick_SelectionColor(View view) {
        ColorPicker colorPicker = new ColorPicker(this);
        colorPicker.setColors(ResultSettings.getColors());
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                settings.setSelectColor(color);
                selectColor.setBackgroundColor(color);
                applyButton.setEnabled(true);
            }

            @Override
            public void onCancel() {
                // EMPTY
            }
        }).setDefaultColorButton(settings.getSelectColor()).show();
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

        try {
            new AlertDialog.Builder(this)
                    .setPositiveButton("Okay", null)
                    .setMessage("Current image saved as: \n" + pngFile.getCanonicalPath())
                    .create().show();
        } catch (IOException | SecurityException e) {
            new AlertDialog.Builder(this)
                    .setPositiveButton("Okay", null)
                    .setMessage("Current image saved as: \n" + pngFile.getAbsolutePath())
                    .create().show();
        }

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
        Bitmap bmp;
        switch (backgroundCode) {
            case BACKGRND_IMG:
                File pngFile = new File(outputDirectory, "Base_" + imgFileToDisplay);
                bmp = BitmapFactory.decodeFile(pngFile.getAbsolutePath());
                break;
            case BACKGRND_SUB:
                File backsubFile = new File(outputDirectory,
                        BackgroundSub.SUB1_FILENAME + "_" + imgFileToDisplay);
                bmp = BitmapFactory.decodeFile(backsubFile.getAbsolutePath());
                break;
            default:
                bmp = createSolidBaseImage();
                break;
        }
        baseImage.setImageBitmap(bmp);
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
                new ArrowDrawOptions(settings.getArrowColor(), settings.getArrowScale()),
                rows, cols);
    }

    private Bitmap createSolidBaseImage() {
        // TODO fix the hard code
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

    private HashMap<String, PivResultData> loadCorrelationMaps(boolean replaced) {
        HashMap<String, PivResultData> result = new HashMap<>();
        result.put(VEC_SINGLE, singlePass);
        result.put(VEC_MULTI, multiPass);
        if (replaced)
            result.put(VEC_REPLACED, replacedPass);
        return result;
    }

    private HashMap<View, LinearLayout> loadDropDownMaps() {
        HashMap<View, LinearLayout> dropDownMap = new HashMap<>();
        ImageButton vectDropDown = findViewById(R.id.vecDropDown);
        ImageButton postpDropDown = findViewById(R.id.postpDropDown);
        ImageButton vortDropDown = findViewById(R.id.vortDropDown);
        ImageButton backgroundDropDown = findViewById(R.id.backgroundDropDown);
        ImageButton infoDropDown = findViewById(R.id.infoDropDown);

        View.OnClickListener dropDownListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // make section layout visible/gone
                LinearLayout sectionLayout = sectionMaps.get(v);

                if (null == sectionLayout)
                    return;

                boolean visible = sectionLayout.getVisibility() == View.VISIBLE;
                sectionLayout.setVisibility(visible? View.GONE : View.VISIBLE);

                // change arrow image to down/up
                ImageButton arrow = (ImageButton) v;
                arrow.setImageResource(visible?
                        R.drawable.drop_down : R.drawable.drop_up);
            }
        };

        vectDropDown.setOnClickListener(dropDownListener);
        postpDropDown.setOnClickListener(dropDownListener);
        vortDropDown.setOnClickListener(dropDownListener);
        backgroundDropDown.setOnClickListener(dropDownListener);
        infoDropDown.setOnClickListener(dropDownListener);

        dropDownMap.put((View) findViewById(R.id.vecDropDown), (LinearLayout)findViewById(R.id.vecFieldLayout));
        dropDownMap.put((View) findViewById(R.id.postpDropDown), (LinearLayout)findViewById(R.id.postpLayout));
        dropDownMap.put((View) findViewById(R.id.vortDropDown), (LinearLayout)findViewById(R.id.vortLayout));
        dropDownMap.put((View) findViewById(R.id.backgroundDropDown), (LinearLayout)findViewById(R.id.backgroundLayout));
        dropDownMap.put((View) findViewById(R.id.infoDropDown), (LinearLayout)findViewById(R.id.infoSection));
        return dropDownMap;
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

    @Override
    public void getSelectPosition(float x, float y) {
        currentX = x;
        currentY = y;

        PivResultData pivCorr;
        switch (settings.getVecOption()) {
            case VEC_SINGLE:
                pivCorr = singlePass;
                break;
            case VEC_MULTI:
                pivCorr = multiPass;
                break;
            default:
                pivCorr = replacedPass;
        }

        // view to piv translation
        Point pivCoords = viewCoordsToPivCoords(baseImage,
                pivCorr.getInterrY().length, pivCorr.getInterrX().length, x, y);

        // Draw the selection on the vector field image
        double imgX = pivCorr.getInterrX()[pivCoords.x];
        double imgY = pivCorr.getInterrY()[pivCoords.y];
        Bitmap selectionDrawnBmp = PivFunctions.createSelectionImage(imgX, imgY, rows, cols,
                pivCorr.getStepX(), pivCorr.getStepY(), settings.getSelectColor());

        selectionImage.setImageBitmap(selectionDrawnBmp);

        // create a flipped y for display (y == 0 at the bottom of the image instead of the top)
        double dispY = imgY - pivCorr.getInterrY().length;

        // update the info text
        String updatedText;
        if (settings.getCalibrated()) {
            // compile the data
            float u = (float) pivCorr.getCalibratedU()[pivCoords.y][pivCoords.x];
            float v = (float) pivCorr.getCalibratedV()[pivCoords.y][pivCoords.x];
            float vort = (float) pivCorr.getCalibratedVorticity()[pivCoords.y][pivCoords.x];
            // because we're inverting the y axis, we also invert the v values
            updatedText = settings.formatInfoString_physical((float) imgX, (float) dispY, u, -v, vort,
                    (float) pivCorr.getPixelToPhysicalRatio(), (float) pivCorr.getUvConversion());
        } else {
            // compile the data
            float u = (float)pivCorr.getU()[pivCoords.y][pivCoords.x];
            float v = (float)pivCorr.getV()[pivCoords.y][pivCoords.x];
            // Only multipass has vorticity values (design decision to only show one vorticity field)
            float vort = (float)multiPass.getVorticityValues()[pivCoords.y][pivCoords.x];
            // because we're inverting the y axis, we also invert the v values
            updatedText = settings.formatInfoString_pixel((float)imgX, (float)dispY, u, -v, vort);
        }
        infoText.setText(updatedText);
    }

    private Point viewCoordsToPivCoords(ImageView view, int pivHeight, int pivWidth, float x, float y) {
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        int bitmapHeight = view.getDrawable().getIntrinsicHeight();
        int bitmapWidth = view.getDrawable().getIntrinsicWidth();

        // find the resized bitmap dimensions
        float resizeFactor = Math.min(viewHeight / (float)bitmapHeight, viewWidth / (float)bitmapWidth);
        float resizedBmpHeight = bitmapHeight * resizeFactor;
        float resizedBmpWidth = bitmapWidth * resizeFactor;

        // find the resize padding
        float xDiff = (viewWidth - resizedBmpWidth)/2;
        float yDiff = (viewHeight - resizedBmpHeight)/2;

        // transform the view's coordinates to match the piv coordinates
        final float[] coords = new float[] {x - xDiff, y - yDiff};
        Matrix m = new Matrix();
        view.getMatrix().invert(m);
        m.postScale(pivWidth/resizedBmpWidth, pivHeight/resizedBmpHeight);
        m.mapPoints(coords);

        int pivX = (int)(coords[0]);
        int pivY = (int)(coords[1]);

        pivX = Math.max(0, Math.min(pivX, pivWidth - 1));
        pivY = Math.max(0, Math.min(pivY, pivHeight - 1));

        return new Point(pivX, pivY);
    }
}
