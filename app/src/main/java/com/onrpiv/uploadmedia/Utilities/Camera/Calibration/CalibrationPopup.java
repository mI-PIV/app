package com.onrpiv.uploadmedia.Utilities.Camera.Calibration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;

import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.PathUtil;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.io.File;


public class CalibrationPopup {
    public static void show(Context context, ActivityResultLauncher<Uri> launcher) {
        new AlertDialog.Builder(context)
            .setMessage("Camera Calibration")
            .setNeutralButton("Take Calibration Picture", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    launcher.launch(PathUtil.getTempFileUri(context));
                }
            })
            .setNegativeButton("Cancel", null).create().show();
    }

    public static ActivityResultCallback<Boolean> getResultCallback(Context context, String userName) {
        return new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                String imagePath = PathUtil.getTempFilePath(context);
                if (!new File(imagePath).exists())
                    return;

                ProgressDialog pDialog = new ProgressDialog(context);
                pDialog.setMessage("Searching for calibration pattern...");
                pDialog.setCancelable(false);
                if (!pDialog.isShowing())
                    pDialog.show();

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        // calculate calibration
                        CameraCalibrationResult ccResult = new CameraCalibrationResult();
                        ccResult.ratio = CameraCalibration.calculatePixelToPhysicalRatio(imagePath);

                        // check to see if calibration pattern was found
                        if (ccResult.ratio == -1d) {
                            AlertDialog.Builder failedDialog = new AlertDialog.Builder(context)
                                    .setMessage("Couldn't find a calibration pattern. Please make sure " +
                                            "the calibration pattern is completely visible in " +
                                            "the photo and try again.")
                                    .setPositiveButton("Okay", null);
                            threadedPopup(context, failedDialog);
                        } else {
                            AlertDialog.Builder successDialog = new AlertDialog.Builder(context)
                                    .setMessage("Calibration pattern found!")
                                    .setPositiveButton("Save Calibration", null);
                            threadedPopup(context, successDialog);
                            setMessage("Calculating camera matrices...", context, pDialog);

                            Mat cameraMatrix = new Mat();
                            Mat distanceCoefficients = new MatOfDouble();
                            Mat.eye(3, 3, CvType.CV_64FC1).copyTo(cameraMatrix);
                            Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(distanceCoefficients);
                            CameraCalibration.saveCameraProperties(context, cameraMatrix, distanceCoefficients);

                            setMessage("Saving camera calibration...", context, pDialog);
                            ccResult.saveCameraMatrix(cameraMatrix);
                            ccResult.saveDistanceCoeffs(distanceCoefficients);

                            // save calibration
                            File calibrationSaveFile = new File(PathUtil.getUserDirectory(context, userName), "calibration.obj");
                            FileIO.write(ccResult, calibrationSaveFile);
                        }

                        // delete temp calibration image
                        PathUtil.deleteIfTempFile(context, imagePath);

                        if (pDialog.isShowing())
                            pDialog.dismiss();
                    }
                };
                thread.start();
            }
        };
    }

    public static void setMessage(String msg, Context context, ProgressDialog pDialog) {
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pDialog.setMessage(msg);
            }
        });
    }

    private static void threadedPopup(Context context, AlertDialog.Builder dialogBuilder) {
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogBuilder.create().show();
            }
        });
    }
}
