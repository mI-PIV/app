package com.onrpiv.uploadmedia.Experiment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.FrameView;
import com.onrpiv.uploadmedia.Utilities.PositionCallback;
import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CalibrationInputActivity extends FragmentActivity implements PositionCallback {
    // Frames
    private Bitmap calibrationFrame;
    private Bitmap drawFrame;
    private ImageView drawView;

    // Widgets
    private Button clearLastButton;
    private Button clearAllButton;
    private Button calibrateButton;
    private TextView helpText;

    // Calibration
    private List<org.opencv.core.Point> selectedPoints;
    private double productFactor = 1d;
    private double measureInput;

    // Switches
    boolean spinnerSelected = false;
    boolean pointsSelected = false;
    boolean measureInputted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.calibration_input);

        OpenCVLoader.initDebug();

        Bundle extras = intent.getExtras();
        selectedPoints = new ArrayList<>();

        // frames
        String framePath = extras.getString("framePath");
        calibrationFrame = BitmapFactory.decodeFile(framePath);
        drawFrame = PivFunctions.createTransparentBitmap(calibrationFrame.getHeight(), calibrationFrame.getWidth());

        // views
        FrameView calibFrameView = findViewById(R.id.calib_input_frameview);
        calibFrameView.setPositionCallback(this);
        ImageView frameView = findViewById(R.id.calib_input_frame);
        frameView.setImageBitmap(calibrationFrame);

        helpText = findViewById(R.id.calib_input_helpText);

        drawView = findViewById(R.id.calib_input_drawFrame);
        drawView.setImageBitmap(drawFrame);

        // Buttons
        clearLastButton = findViewById(R.id.calib_input_clearLast_button);
        clearLastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeLastPoint();
            }
        });
        clearAllButton = findViewById(R.id.calib_input_clearAll);
        clearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAllPoints();
            }
        });
        calibrateButton = findViewById(R.id.calib_input_calibrate);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double cmInput = productFactor * measureInput;
                double pixelsPerCm = getLinesPixelLength() / cmInput;
                Intent finalIntent = getIntent();
                finalIntent.putExtra("pixelsPerCm", pixelsPerCm);
                setResult(RESULT_OK, finalIntent);
                finish();
            }
        });

        // Spinner
        Spinner measurementSpinner = findViewById(R.id.calib_input_measure_spinner);
        List<String> spinnerOptions = Arrays.asList("cm", "mm", "in");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spinnerOptions);
        measurementSpinner.setAdapter(spinnerAdapter);
        // Spinner listener
        measurementSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinnerSelected = true;
                if (i == 1) {  // mm
                    // 0.1 cm in a mm
                    productFactor = 0.1d;
                } else if (i == 2) {  // inches
                    // 2.54 cm in an inch
                    productFactor = 2.54d;
                } else {  //cm
                    productFactor = 1d;
                }
                calibrateButton.setEnabled(checkInputs());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // select 'cm' default
                adapterView.setSelection(0);
            }
        });

        // user input
        EditText inputDecimalMeasurement = findViewById(R.id.calib_input_measure_input);
        inputDecimalMeasurement.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // EMPTY
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // EMPTY
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                if (input.startsWith("."))
                    input = "0" + input;
                measureInput = Double.parseDouble(input);
                measureInputted = true;
                calibrateButton.setEnabled(checkInputs());
            }
        });
    }

    @Override
    public void getSelectPosition(float x, float y) {
        Point convertedPoint = viewCoordsToImageCoords(drawView, calibrationFrame.getHeight(), calibrationFrame.getWidth(), x, y);
        addPoint(convertedPoint);
    }

    private double getLinesPixelLength() {
        double result = 0d;
        Point prevP = null;
        for (Point p : selectedPoints) {
            if (null == prevP) {
                prevP = p;
            } else {
                result += calcDistance(prevP, p);
            }
        }
        return result;
    }

    private double calcDistance(Point a, Point b) {
        return Math.sqrt(Math.pow(b.x - a.x, 2.0) + Math.pow(b.y - a.y, 2.0));
    }

    private boolean checkInputs() {
        return measureInputted && spinnerSelected && pointsSelected;
    }

    private void addPoint(Point p) {
        clearAllButton.setEnabled(true);
        clearLastButton.setEnabled(true);
        helpText.setVisibility(View.INVISIBLE);

        selectedPoints.add(p);

        if (selectedPoints.size() >= 2) {
            pointsSelected = true;
            calibrateButton.setEnabled(checkInputs());
        }
        // draw
        drawPoints();
    }

    private void removeLastPoint() {
        if (selectedPoints.size() == 1) {
            removeAllPoints();
        } else {
            selectedPoints.remove(selectedPoints.size()-1);
            drawPoints();
        }
    }

    private void removeAllPoints() {
        clearAllButton.setEnabled(false);
        clearLastButton.setEnabled(false);
        calibrateButton.setEnabled(false);
        helpText.setVisibility(View.VISIBLE);

        pointsSelected = false;
        selectedPoints.clear();

        drawPoints();
    }

    private void drawPoints() {
        drawFrame = null;
        drawFrame = PivFunctions.createLinedBitmap(selectedPoints, calibrationFrame.getHeight(), calibrationFrame.getWidth());
        drawView.setImageBitmap(drawFrame);
    }

    private Point viewCoordsToImageCoords(ImageView view, int imageHeight, int imageWidth, float x, float y) {
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
        m.postScale(imageWidth/resizedBmpWidth, imageHeight/resizedBmpHeight);
        m.mapPoints(coords);

        int pivX = (int)(coords[0]);
        int pivY = (int)(coords[1]);

        pivX = Math.max(0, Math.min(pivX, imageWidth - 1));
        pivY = Math.max(0, Math.min(pivY, imageHeight - 1));

        return new Point(pivX, pivY);
    }
}
