package com.onrpiv.uploadmedia.Utilities.Camera;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class CameraSizes {

    public boolean cancelled;

    private final AlertDialog popup;
    private final HighSpeedCaptureCallback captureCallback;

    private Size selectedCamera;
    private Range<Integer> selectedFrameRate;

    private final StreamConfigurationMap camConfigMap;
    private static final int[] frameRates = new int[]{120, 240};

    private RadioButton selectedFpsButton = null;
    private RadioButton selectedSizeButton = null;
    private final HashMap<String, Size> stringToSizeMap;

    private static final String TAG = "CameraSizes";


    private final View.OnClickListener fpsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // turn off old fps button
            if (null != selectedFpsButton)
                selectedFpsButton.setChecked(false);

            // reset selected size and save button
            selectedSizeButton = null;
            popup.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            // get and display sizes for new fps
            String fpsText = ((RadioButton) v).getText().toString();
            Size[] availableSizes = camConfigMap.getHighSpeedVideoSizesFor(stringToFps(fpsText));
            fillSizeRow(v, availableSizes);

            // turn on new fps button
            selectedFpsButton = (RadioButton) v;
            selectedFpsButton.setChecked(true);
        }
    };

    private final View.OnClickListener sizeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != selectedSizeButton)
                selectedSizeButton.setChecked(false);
            selectedSizeButton = (RadioButton) v;
            selectedSizeButton.setChecked(true);

            popup.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    };


    public CameraSizes(Activity activity, Context context, HighSpeedCaptureCallback captureCallback) {
        this.captureCallback = captureCallback;

        CameraManager camManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics camChar = null;

        stringToSizeMap = new HashMap<>();

        try {
            camChar = camManager.getCameraCharacteristics(camManager.getCameraIdList()[0]);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        assert camChar != null;
        camConfigMap = camChar.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        popup = createPopup(context);
    }

    public void showConfigPopup() {
        popup.show();
        popup.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public Size getSelectedCameraSize() {
        return selectedCamera;
    }

    public Size getPreviewSurfaceSize() {
        return chooseOptimalSize(
                camConfigMap.getHighSpeedVideoSizesFor(selectedFrameRate),
                selectedCamera);
    }

    public String getFrameRateString() {
        return selectedFrameRate.getLower().toString() + " fps";
    }

    public String getCameraString() {
        return sizeToString(selectedCamera);
    }

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    private static Size chooseOptimalSize(Size[] choices, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        double ratio = (double) h / w;
        for (Size option : choices) {
            double optionRatio = (double) option.getHeight() / option.getWidth();
            if (ratio == optionRatio) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CameraFragment.CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return Collections.min(Arrays.asList(choices), new CameraFragment.CompareSizesByHeight());
        }
    }

    private String sizeToString(Size size) {
        String sizeString = size.getHeight() + " x " + size.getWidth();
        stringToSizeMap.put(sizeString, size);
        return sizeString;
    }

    private Range<Integer> stringToFps(String fpsText) {
        String fpsIntString = fpsText.split(" ")[0];
        int fps = Integer.parseInt(fpsIntString);
        return new Range<Integer>(fps, fps);
    }

    private void saveSelections() {
        String fpsText = selectedFpsButton.getText().toString();
        String sizeText = selectedSizeButton.getText().toString();

        selectedFrameRate = stringToFps(fpsText);
        selectedCamera = stringToSizeMap.get(sizeText);
        captureCallback.highSpeedCapture(this);
    }

    private AlertDialog createPopup(Context context) {
        TableLayout tableLayout = new TableLayout(context);
        tableLayout.setGravity(Gravity.CENTER);

        // horizontal line
        tableLayout.addView(createHorizDivider(context));

        // textview
        tableLayout.addView(createTextView(context, "Frame Rate"));

        // create the fps row
        TableRow fpsRow = new TableRow(context);
        fpsRow.setGravity(Gravity.CENTER);

        for (int fps : frameRates) {
            fpsRow.addView(createRadioButton(context, fps + " fps", fpsListener));
        }
        tableLayout.addView(fpsRow);


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setView(tableLayout)
                .setMessage("Choose a frame rate and a frame size.")
                .setCancelable(false)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveSelections();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelled = true;
                    }
                });

        return alertDialog.create();
    }

    private void fillSizeRow(View view, Size[] availableVideoSizes) {
        TableLayout root = (TableLayout) view.getParent().getParent();
        Context context = view.getContext();

        TableRow sizesRow = new TableRow(context);
        sizesRow.setGravity(Gravity.CENTER);

        for (Size vidSize : availableVideoSizes) {
            String sizeText = sizeToString(vidSize);
            sizesRow.addView(createRadioButton(context, sizeText, sizeListener));
        }

        // if we're already displaying fps sizes, then we need to replace it with the new sizes.
        if (root.getChildCount() > 5) {
            root.removeViewAt(5);
        } else {
            root.addView(createHorizDivider(context));
            root.addView(createTextView(context, "Resolution"));
        }

        root.addView(sizesRow);
    }

    private RadioButton createRadioButton(Context context, String text, View.OnClickListener listener) {
        RadioButton button = new RadioButton(context);
        button.setText(text);

        // layout
        button.setGravity(Gravity.CENTER | Gravity.START);
        button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        button.setPadding(
                button.getPaddingLeft(),
                button.getPaddingTop(),
                50,
                button.getPaddingBottom());

        // logic
        button.setOnClickListener(listener);

        return button;
    }

    private TextView createTextView(Context context, String text) {
        TextView tv = new TextView(context);
        tv.setGravity(Gravity.CENTER);
        tv.setText("Frame Rate");
        return tv;
    }

    private View createHorizDivider(Context context) {
        View v = new View(context);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5
        ));
        v.setBackgroundColor(Color.parseColor("#B3B3B3"));
        return v;
    }
}
