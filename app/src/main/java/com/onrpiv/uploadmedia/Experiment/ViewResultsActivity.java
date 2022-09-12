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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.FrameView;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;
import com.onrpiv.uploadmedia.Utilities.PositionCallback;
import com.onrpiv.uploadmedia.Utilities.ResultSettings;
import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import petrov.kristiyan.colorpicker.ColorPicker;

/**
 * Created by sarbajit mukherjee on 09/07/2020.
 * Edited by KP on 02/18/2021
 */

public class ViewResultsActivity extends AppCompatActivity implements PositionCallback {
    // Widgets
    private RangeSlider rangeSlider;
    protected ImageView baseImage;
    private ImageView vectorFieldImage, vorticityImage;
    private Button arrowColor, vorticityColors, solidColor, selectColor;

    // paths
    protected String imgFileToDisplay;
    protected File outputDirectory;

    // maps and settings
    protected HashMap<String, PivResultData> correlationMaps;
    private HashMap<Integer, LinearLayout> sectionMaps;
    private ArrayList<ColorMap> colorMaps;
    protected ResultSettings settings;
    protected int experimentNumber;
    protected int imageCounter = 0;

    // info section
    private ImageView selectionImage;
    private TextView infoText;
    private TableLayout infoTextTable;
    private TextView resultsVelocity;
    private TextView resultsX;
    private TextView resultsY;
    private TextView resultsVort;
    private TextView resultsU;
    private TextView resultsV;
    private TextView resultsXYConversion;
    private TextView resultsUVConversion;
    private float currentX;
    private float currentY;

    // From Image Activity
    public static PivResultData singlePass;
    public static PivResultData multiPass;
    public static PivResultData replacedPass;
    public static PivParameters pivParameters;
    public static boolean calibrated;
    public static boolean backgroundSubtracted;
    private int rows;
    private int cols;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent displayIntent = getIntent();
        setContentView(R.layout.display_result_layout);

        // Setup images and paths
        String userName = displayIntent.getStringExtra(PivResultData.USERNAME);
        int currentExpDir = PersistedData.getTotalExperiments(this, userName);
        imgFileToDisplay = PathUtil.getExperimentImageFileSuffix(currentExpDir);
        outputDirectory = PathUtil.getExperimentNumberedDirectory(this, userName, currentExpDir);

        // resumed
        if (null != savedInstanceState) {
            onRestoreInstanceState(savedInstanceState);
        } else {  //brand new
            settings = new ResultSettings(this);
            settings.setCalibrated(calibrated);
            settings.setDropDownVisible(R.id.vecDropDown, true);
            saveLocationPopup();
        }

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
        experimentNumber = (int) extras.get(PivResultData.EXP_NUM);

        // load params section
        TextView paramsText = findViewById(R.id.paramsText);
        paramsText.setText(pivParameters.prettyPrintData_comprehensive());

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
                applyDisplay();
            }
        });

        SeekBar arrowScale = findViewById(R.id.arrow_scale);
        arrowScale.setProgress((int)settings.getArrowScale());
        arrowScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settings.setArrowScale(progress);
                applyDisplay();
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

        // text views for information results
        infoText = findViewById(R.id.infoText);
        infoTextTable = findViewById(R.id.infoTextTable);
        resultsVelocity = findViewById(R.id.resultsVelocity);
        resultsX = findViewById(R.id.resultsX);
        resultsY = findViewById(R.id.resultsY);
        resultsVort = findViewById(R.id.resultsVort);
        resultsU = findViewById(R.id.resultsU);
        resultsV = findViewById(R.id.resultsV);
        resultsXYConversion = findViewById(R.id.resultsXYConversion);
        resultsUVConversion = findViewById(R.id.resultsUVConversion);
        TableRow resultsXYConversionRow = findViewById(R.id.resultsXYConversionRow);
        TableRow resultsUVConversionRow = findViewById(R.id.resultsUVConversionRow);
        if (calibrated) {
            resultsUVConversionRow.setVisibility(View.VISIBLE);
            resultsXYConversionRow.setVisibility(View.VISIBLE);
        }

        // buttons
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
        backgroundSpinner.setSelection(bgOptionsList.indexOf(settings.getBackgroundPretty()));
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
                applyDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // EMPTY
            }
        });

        // drop-downs
        loadDropDownMaps();

        // switches
        SwitchCompat displayVectors = findViewById(R.id.vec_display);
        displayVectors.setChecked(settings.getVecDisplay());
        displayVectors.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setVecDisplay(isChecked);
                applyDisplay();
            }
        });

        SwitchCompat displayVorticity = findViewById(R.id.vort_display);
        displayVorticity.setChecked(settings.getVortDisplay());
        displayVorticity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setVortDisplay(isChecked);
                applyDisplay();
            }
        });

        // radio groups
        RadioGroup vectorRadioGroup = findViewById(R.id.postp_rgroup);
        int checkedId = settings.getVecOption().equals(VEC_MULTI)? R.id.multipass
                : settings.getVecOption().equals(VEC_REPLACED)? R.id.replace
                : R.id.singlepass;
        vectorRadioGroup.check(checkedId);
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
                applyDisplay();
            }
        });

        if (!replaced) {
            RadioButton replacedRadioButton = findViewById(R.id.replace);
            replacedRadioButton.setVisibility(View.GONE);
            settings.setVecOption(VEC_MULTI);
            vectorRadioGroup.check(R.id.multipass);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Defaults
        displayBaseImage(BACKGRND_SOLID);
        displayVectorImage(correlationMaps.get(settings.getVecOption()));

        // popups
//        double nMaxLower = displayIntent.getDoubleExtra("n-max-lower", 0);
//        double maxDisplacement = displayIntent.getDoubleExtra("max-displacement", 0);
//        popups(nMaxLower, maxDisplacement);

        // reload the display
        settings.vecFieldChanged = true;
        settings.vortMapChanged = true;
        settings.selectionChanged = true;
        settings.backgroundChanged = true;
        applyDisplay();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
        // maps and settings
        settings = new ResultSettings(this).loadInstanceBundle(savedInstanceState);
        imageCounter = savedInstanceState.getInt("imageCounter");

        // info
        currentX = savedInstanceState.getFloat("currentX");
        currentY = savedInstanceState.getFloat("currentY");

        // from image activity
        rows = savedInstanceState.getInt("rows");
        cols = savedInstanceState.getInt("cols");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void applyDisplay() {
        // Vector Field
        if (settings.vecFieldChanged && settings.getVecDisplay()) {
            displayVectorImage(correlationMaps.get(settings.getVecOption()));
        } else if (!settings.getVecDisplay()) {
            vectorFieldImage.setVisibility(View.INVISIBLE);
        }

        // Vorticity
        if (settings.vortMapChanged && settings.getVortDisplay()) {
            displayVortImage();
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
    }

    public void OnClick_ArrowColor(View view) {
        ColorPicker colorPicker = new ColorPicker(this);
        colorPicker.setColors(ResultSettings.getColors());
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                settings.setArrowColor(color);
                arrowColor.setBackgroundColor(color);
                applyDisplay();
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
                        applyDisplay();
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
                applyDisplay();
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
                applyDisplay();
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

        String imageFilename = "mI_PIV_" + experimentNumber + "_" + imageCounter++ + ".png";
        ContentResolver resolver = getContentResolver();
        Uri imageCollection;

        if (Build.VERSION.SDK_INT >= 29) {
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageFilename);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        Uri imageUri = resolver.insert(imageCollection, contentValues);

        try {
            OutputStream ostream = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            bmp.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            ostream.close();
            imageStack.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            imageStack.setDrawingCacheEnabled(false);
        }

        // popup showing user where to find the saved image
        new AlertDialog.Builder(this)
                .setPositiveButton("Okay", null)
                .setMessage("Current experiment results view saved to your photo gallery.")
                .create().show();

        saveImageButton.setEnabled(true);
        saveImageButton.setBackgroundColor(Color.parseColor("#243EDF"));
    }

    private void displayVectorImage(PivResultData pivResultData) {
        Bitmap bmp = createVectorFieldBitmap(pivResultData);
        vectorFieldImage.setImageBitmap(bmp);
        vectorFieldImage.setVisibility(View.VISIBLE);
    }

    private void displayVortImage() {
        Bitmap bmp = createVorticityBitmap(multiPass);
        vorticityImage.setImageBitmap(bmp);
        vorticityImage.setVisibility(View.VISIBLE);
    }

    protected void displayBaseImage(String backgroundCode) {
        Bitmap bmp;
        switch (backgroundCode) {
            case BACKGRND_IMG:
                File pngFile = new File(outputDirectory, "Base_0000_" + imgFileToDisplay);
                bmp = BitmapFactory.decodeFile(pngFile.getAbsolutePath());
                break;
            case BACKGRND_SUB:
                File backsubFile = new File(outputDirectory,
                        BackgroundSub.SUB1_FILENAME + "_0000_" + imgFileToDisplay);
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

    protected Bitmap createSolidBaseImage() {
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

    protected HashMap<String, PivResultData> loadCorrelationMaps(boolean replaced) {
        HashMap<String, PivResultData> result = new HashMap<>();
        result.put(VEC_SINGLE, singlePass);
        result.put(VEC_MULTI, multiPass);
        if (replaced)
            result.put(VEC_REPLACED, replacedPass);
        return result;
    }

    private void loadDropDownMaps() {
        sectionMaps = new HashMap<>();
        ImageButton vectDropDown = findViewById(R.id.vecDropDown);
        ImageButton postpDropDown = findViewById(R.id.postpDropDown);
        ImageButton vortDropDown = findViewById(R.id.vortDropDown);
        ImageButton backgroundDropDown = findViewById(R.id.backgroundDropDown);
        ImageButton infoDropDown = findViewById(R.id.infoDropDown);
        ImageButton paramsDropDown = findViewById(R.id.paramsDropDown);

        View.OnClickListener dropDownListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // make section layout visible/gone
                LinearLayout sectionLayout = sectionMaps.getOrDefault(v.getId(), null);

                if (null == sectionLayout)
                    return;

                // switch visibility
                boolean visible = sectionLayout.getVisibility() == View.VISIBLE;
                sectionLayout.setVisibility(visible? View.GONE : View.VISIBLE);
                settings.setDropDownVisible(v.getId(), !visible);

                // change arrow image to down/up
                ImageButton arrow = (ImageButton) v;
                arrow.setImageResource(visible? R.drawable.drop_down : R.drawable.drop_up);
            }
        };

        vectDropDown.setOnClickListener(dropDownListener);
        postpDropDown.setOnClickListener(dropDownListener);
        vortDropDown.setOnClickListener(dropDownListener);
        backgroundDropDown.setOnClickListener(dropDownListener);
        infoDropDown.setOnClickListener(dropDownListener);
        paramsDropDown.setOnClickListener(dropDownListener);

        sectionMaps.put(vectDropDown.getId(), (LinearLayout)findViewById(R.id.vecFieldLayout));
        sectionMaps.put(postpDropDown.getId(), (LinearLayout)findViewById(R.id.postpLayout));
        sectionMaps.put(vortDropDown.getId(), (LinearLayout)findViewById(R.id.vortLayout));
        sectionMaps.put(backgroundDropDown.getId(), (LinearLayout)findViewById(R.id.backgroundLayout));
        sectionMaps.put(infoDropDown.getId(), (LinearLayout)findViewById(R.id.infoSection));
        sectionMaps.put(paramsDropDown.getId(), (LinearLayout) findViewById(R.id.paramsSection));

        // if the dropdowns are already 'visible' (aka dropped down) then we drop them down on resume
        List<ImageButton> dropdowns = new ArrayList<>(Arrays.asList(
                vectDropDown, postpDropDown, vortDropDown, backgroundDropDown, infoDropDown, paramsDropDown
        ));
        for (ImageButton dd : dropdowns) {
            if (settings.getDropDownVisible(dd.getId())) {
                dd.callOnClick();
            }
        }
    }

    private void saveLocationPopup() {
        new AlertDialog.Builder(ViewResultsActivity.this)
                .setTitle("Data Saved Location")
                .setMessage("This experiment's data can be found in the text files located on your phone at:\n\n" + outputDirectory)
                .setPositiveButton("Okay", null)
                .show();
    }

    private void popups(double nMaxLower, double maxDisplacement) {
        AlertDialog.Builder alertDialogParametersBuilder = new AlertDialog.Builder(ViewResultsActivity.this)
                .setTitle("Alert!")
                .setCancelable(false)
                .setNegativeButton("I Understand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }});

        if (maxDisplacement < nMaxLower) {
            alertDialogParametersBuilder.setMessage(R.string.move_forward);
        } else {
            alertDialogParametersBuilder.setMessage(R.string.final_display);
        }
        alertDialogParametersBuilder.create().show();
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
        if (calibrated) {
            resultsVelocity.setText(pivCorr.getCalibratedMag()[pivCoords.y][pivCoords.x] + " cm/s");
            resultsX.setText(String.valueOf((int)imgX));
            resultsY.setText(String.valueOf((int)dispY));
            // Only multipass has vorticity values (design decision to only show one vorticity field)
            resultsVort.setText(String.valueOf((float) multiPass.getCalibratedVorticity()[pivCoords.y][pivCoords.x]));
            resultsU.setText((float) pivCorr.getCalibratedU()[pivCoords.y][pivCoords.x] + " cm/s");
            // because we're inverting the y axis, we also invert the v values
            resultsV.setText(-((float) pivCorr.getCalibratedV()[pivCoords.y][pivCoords.x]) + " cm/s");
            resultsXYConversion.setText(String.valueOf((float) pivCorr.getPixelToPhysicalRatio()));
            resultsUVConversion.setText(String.valueOf((float) pivCorr.getUvConversion()));
        } else {
            resultsVelocity.setText(pivCorr.getMag()[pivCoords.y][pivCoords.x] + " px/s");
            resultsX.setText(String.valueOf((int)imgX));
            resultsY.setText(String.valueOf((int)dispY));
            // Only multipass has vorticity values (design decision to only show one vorticity field)
            resultsVort.setText(String.valueOf((float) multiPass.getVorticityValues()[pivCoords.y][pivCoords.x]));
            resultsU.setText((float)pivCorr.getU()[pivCoords.y][pivCoords.x] + " px");
            // because we're inverting the y axis, we also invert the v values
            resultsV.setText(-((float)pivCorr.getV()[pivCoords.y][pivCoords.x]) + " px");
        }
        // switch from info text ("please select...") to information table
        infoText.setVisibility(View.INVISIBLE);
        infoTextTable.setVisibility(View.VISIBLE);
    }

    private Point viewCoordsToPivCoords(ImageView view, int pivHeight, int pivWidth, float x, float y) {
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        // this will always give us the correct bmp dimensions
        Bitmap bmp = BitmapFactory.decodeFile(new File(outputDirectory, "Base_0000_" + imgFileToDisplay).getAbsolutePath());
        int bitmapHeight = bmp.getHeight();
        int bitmapWidth = bmp.getWidth();

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

    public static Class<?> loadFromFiles(Context context, String userName, int expNum) {
        PivParameters params = (PivParameters) FileIO.read(context, userName, expNum, PivParameters.IO_FILENAME);
        pivParameters = params;

        // filenames
        String spFilename = PivResultData.SINGLE + PivResultData.PROCESSED;
        spFilename += params.isReplace()? PivResultData.REPLACE : "";
        spFilename += "_0000";
        String mpFilename = PivResultData.MULTI + "_0000";
        String repFilename = PivResultData.MULTI + PivResultData.PROCESSED + PivResultData.REPLACE + "_0000";

        singlePass = (PivResultData) FileIO.read(context, userName, expNum, spFilename);
        multiPass = (PivResultData) FileIO.read(context, userName, expNum, mpFilename);
        if (params.isReplace()) {
            replacedPass = (PivResultData) FileIO.read(context, userName, expNum, repFilename);
        }
        return ViewResultsActivity.class;
    }
}
