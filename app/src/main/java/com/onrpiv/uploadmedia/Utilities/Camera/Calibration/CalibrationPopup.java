package com.onrpiv.uploadmedia.Utilities.Camera.Calibration;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;

import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


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

                // calculate calibration
                CameraCalibrationResult ccResult = new CameraCalibrationResult();

                ccResult.ratio = CameraCalibration.calculatePixelToPhysicalRatio(imagePath);

                // check to see if calibration pattern was found
                if (ccResult.ratio == -1d) {
                    new AlertDialog.Builder(context)
                            .setMessage("Couldn't find a calibration pattern. Please make sure " +
                                    "the calibration pattern is completely visible in " +
                                    "the photo and try again.")
                            .setPositiveButton("Okay", null)
                            .create().show();
                } else {
                    Mat cameraMatrix = new Mat();
                    Mat distanceCoefficients = new MatOfDouble();
                    Mat.eye(3, 3, CvType.CV_64FC1).copyTo(cameraMatrix);
                    Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(distanceCoefficients);
                    CameraCalibration.saveCameraProperties(context, cameraMatrix, distanceCoefficients);

                    ccResult.saveCameraMatrix(cameraMatrix);
                    ccResult.saveDistanceCoeffs(distanceCoefficients);

                    // save calibration
                    int newCalibrationNumber = PersistedData.getTotalCalibrations(context, userName) + 1;
                    File calibrationDir = PathUtil.getCameraCalibrationDirectory(context, userName);
                    Date date = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("ddMMMyyyy", Locale.getDefault());

                    FileIO.write(ccResult, new File(calibrationDir,
                            "Calibration." + newCalibrationNumber + "." + df.format(date) + ".obj"));

                    // save the new calibration number
                    PersistedData.setTotalCalibrations(context, userName, newCalibrationNumber);
                }

                // delete temp calibration image
                PathUtil.deleteIfTempFile(context, imagePath);
            }
        };
    }
}
