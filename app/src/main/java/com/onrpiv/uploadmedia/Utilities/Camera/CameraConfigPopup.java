package com.onrpiv.uploadmedia.Utilities.Camera;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

import com.onrpiv.uploadmedia.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class CameraConfigPopup {

    public boolean cancelled;

    private final AlertDialog popup;
    private final HighSpeedCaptureCallback captureCallback;

    private Size selectedCamera;
    private Range<Integer> selectedFrameRate;

    private final StreamConfigurationMap camConfigMap;
    private final Range<Integer>[] frameRates;
    private final Size[] slowVideoSizes;

    private RadioButton selectedFpsButton = null;
    private RadioButton selectedSizeButton = null;
    private final HashMap<String, Size> stringToSizeMap;

    private LinearLayout resLayout;
    private LinearLayout fpsLayout;
    private CheckBox defaultCheckBox;

    private static final String TAG = "CameraSizes";
    private static final List<String> defaultFPS = new ArrayList<>(Arrays.asList("90", "60", "30", "24", "15"));
    private static final List<String> defaultRes = new ArrayList<>(Arrays.asList(
            "1080 x 1920",
            "960 x 1280",
            "720 x 1280",
            "600 x 800",
            "360 x 640"));


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
            int fps = stringToFps(fpsText).getLower();

            Size[] availableSizes;
            if (fps > 90) {
                availableSizes = camConfigMap.getHighSpeedVideoSizesFor(stringToFps(fpsText));
            } else {
                availableSizes = slowVideoSizes;
            }
            fillResLayout(v, availableSizes);

            // turn on new fps button
            selectedFpsButton = (RadioButton) v;
            selectedFpsButton.setChecked(true);
        }
    };

    private final CompoundButton.OnCheckedChangeListener checkboxListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked && fpsLayout.getChildCount() > 0) {
                boolean fpsSet = false;
                boolean resSet = false;

                // get fps buttons
                List<RadioButton> fpsButtons = new ArrayList<>();
                for (int i = 0; i < fpsLayout.getChildCount(); i++)
                    fpsButtons.add((RadioButton) fpsLayout.getChildAt(i));

                // search fps buttons for our default
                for (String fps : defaultFPS) {
                    for (RadioButton fpsButton : fpsButtons) {
                        if (fpsButton.getText().toString().equals(fps + " fps")) {
                            fpsButton.performClick();
                            fpsSet = true;
                            break;
                        }
                    }
                    if (fpsSet) break;
                }

                // now search for camera res default button
                List<RadioButton> resButtons = new ArrayList<>();
                for (int j = 0; j < resLayout.getChildCount(); j++)
                    resButtons.add((RadioButton) resLayout.getChildAt(j));

                for (String res : defaultRes) {
                    for (RadioButton resButton : resButtons) {
                        if (resButton.getText().toString().equals(res)) {
                            resButton.performClick();
                            resSet = true;
                            break;
                        }
                    }
                    if (resSet) break;
                }

                // Remove the default checkbox if we can't set defaults
                if (!fpsSet || !resSet) {
                    defaultCheckBox.setVisibility(View.GONE);
                }
            }
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


    public CameraConfigPopup(Activity activity, Context context, HighSpeedCaptureCallback captureCallback) {
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
        frameRates = camChar.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        slowVideoSizes = camConfigMap.getOutputSizes(SurfaceTexture.class);

        popup = createPopup(context);
    }

    public void showConfigPopup() {
        popup.show();
        popup.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    public Size getSelectedCameraSize() {
        return selectedCamera;
    }

    public Range<Integer> getSelectedFrameRate() {
        return selectedFrameRate;
    }

    public Size getPreviewSurfaceSize() {
        if (selectedFrameRate.getLower() > 90) {
            return chooseOptimalSize(camConfigMap.getHighSpeedVideoSizesFor(selectedFrameRate), selectedCamera);
        } else {
            return new Size(1920, 1080);
        }
    }

    public String getFrameRateString() {
        return selectedFrameRate.getLower().toString() + " fps";
    }

    public String getCameraString() {
        return sizeToString(selectedCamera);
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
        LayoutInflater inflater = LayoutInflater.from(context);
        View popup = inflater.inflate(R.layout.popup_camera_config, null);

        defaultCheckBox = popup.findViewById(R.id.camera_config_default_cb);
        defaultCheckBox.setOnCheckedChangeListener(checkboxListener);
        fpsLayout = popup.findViewById(R.id.popup_cam_fps);
        resLayout = popup.findViewById(R.id.popup_cam_res);

        // Check if the phone is capable before we manually add 120, 240 fps
        ArrayList<Range<Integer>> allFrameRates = new ArrayList<>(Arrays.asList(frameRates));
        if (hasHighSpeedCapability(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Try to add 120 and 240 to the frame rate options
            try {
                camConfigMap.getHighSpeedVideoSizesFor(stringToFps("120"));
                allFrameRates.add(new Range<>(120, 120));
            } catch (IllegalArgumentException ignored) {}

            try {
                camConfigMap.getHighSpeedVideoSizesFor(stringToFps("240"));
                allFrameRates.add(new Range<>(240, 240));
            } catch (IllegalArgumentException ignored) {}
        }

        // create the fps radio buttons
        for (Range<Integer> fpsRange : allFrameRates) {
            if (fpsRange.getLower().equals(fpsRange.getUpper())) {
                int fps = fpsRange.getLower();
                fpsLayout.addView(createRadioButton(context, fps + " fps", fpsListener));
            }
        }

        // build the popup
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setView(popup)
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

    private void fillResLayout(View view, Size[] availableVideoSizes) {
        Context context = view.getContext();

        // remove any previous resolution buttons
        resLayout.removeAllViews();

        for (Size vidSize : availableVideoSizes) {
            String sizeText = sizeToString(vidSize);
            resLayout.addView(createRadioButton(context, sizeText, sizeListener));
        }
    }

    private RadioButton createRadioButton(Context context, String text, View.OnClickListener listener) {
        RadioButton button = new RadioButton(context);
        button.setText(text);

        // layout
        button.setGravity(Gravity.CENTER | Gravity.START);
        button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        button.setLayoutDirection(View.LAYOUT_DIRECTION_INHERIT);
        button.setPadding(
                button.getPaddingLeft(),
                button.getPaddingTop(),
                50,
                button.getPaddingBottom());

        // logic
        button.setOnClickListener(listener);

        return button;
    }

    private boolean hasHighSpeedCapability(Context context) {
        final CameraManager camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        boolean hasHighFPS = false;
        try {
            int[] capabilities = camManager.getCameraCharacteristics(camManager.getCameraIdList()[0]).get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
            assert capabilities != null;
            for (int capability : capabilities) {
                if (capability == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO) {
                    hasHighFPS = true;
                    break;
                }
            }
        } catch (IllegalArgumentException | CameraAccessException e) {
            e.printStackTrace();
        }
        return hasHighFPS;
    }
}
