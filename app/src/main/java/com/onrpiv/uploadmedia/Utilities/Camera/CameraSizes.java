package com.onrpiv.uploadmedia.Utilities.Camera;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CameraSizes {

    public boolean cancelled;

    private Size selectedCamera;
    private Range<Integer> selectedFrameRate;

    private final StreamConfigurationMap camConfigMap;
    private static final int[] frameRates = new int[]{30, 60, 90, 120, 240};


    private RadioButton selectedFpsButton = null;
    private RadioButton selectedSizeButton = null;
    private final HashMap<String, Size> stringToSizeMap;

    private final View.OnClickListener fpsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedFpsButton.toggle();
            String fpsText = ((RadioButton) v).getText().toString();
            Size[] availableSizes = camConfigMap.getHighSpeedVideoSizesFor(stringToFps(fpsText));
            fillSizeRow(v, availableSizes);
            selectedFpsButton = (RadioButton) v;
            selectedFpsButton.toggle();
        }
    };

    private final View.OnClickListener sizeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedSizeButton.toggle();
            selectedSizeButton = (RadioButton) v;
            selectedSizeButton.toggle();
        }
    };

    private static final String TAG = "CameraSizes";


    public CameraSizes(Activity activity) {
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
    }

    public CameraSizes selectionPopup(Context context) {


        return this;
    }


    public Size getSelectedCameraSize() {
        return selectedCamera;
    }

    public String getFrameRateString() {
        return selectedFrameRate.getLower().toString() + " ms";
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
        int fps = Integer.parseInt(fpsText);
        return new Range<Integer>(fps, fps);
    }

    private void saveSelections() {
        String fpsText = selectedFpsButton.getText().toString();
        String sizeText = selectedSizeButton.getText().toString();

        selectedFrameRate = stringToFps(fpsText);
        selectedCamera = stringToSizeMap.get(sizeText);
    }

    private AlertDialog createPopup(Context context) {
        TableLayout tableLayout = new TableLayout(context);

        // create the fps row
        TableRow fpsRow = new TableRow(context);
        for (int fps : frameRates) {
            fpsRow.addView(createRadioButton(context, String.valueOf(fps), fpsListener));
        }
        tableLayout.addView(fpsRow);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setView(tableLayout)
                .setMessage("Choose a frame rate and a frame size.")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
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
        TableLayout root = (TableLayout) view.getRootView();
        Context context = view.getContext();

        TableRow sizesRow = new TableRow(context);
        for (Size vidSize : availableVideoSizes) {
            String sizeText = sizeToString(vidSize);
            sizesRow.addView(createRadioButton(context, sizeText, sizeListener));
        }

        root.addView(sizesRow);
    }

    private RadioButton createRadioButton(Context context, String text, View.OnClickListener listener) {
        RadioButton button = new RadioButton(context);
        button.setText(text);
        button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // layout
        button.setGravity(Gravity.CENTER);
//        button.setPadding(
//                button.getPaddingStart(),
//                button.getPaddingTop(),
//                20,
//                20);

        // logic
        button.setOnClickListener(listener);

        return button;
    }
}
