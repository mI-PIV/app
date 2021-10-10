package com.onrpiv.uploadmedia.Utilities.Camera.Calibration;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CalibrationPopup {
    private final Context context;
    private final ActivityResultLauncher<String> cameraLauncher;
    private boolean tryAgain = false;

    public CalibrationPopup(Context context, String userName, ActivityResultRegistry resultRegistry) {
        this.context = context;
        cameraLauncher = resultRegistry.register("calibrationImage",
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        String imagePath = PathUtil.getRealPath(context, result);

                        // calculate calibration
                        CameraCalibrationResult ccResult = new CameraCalibrationResult();

                        ccResult.ratio = CameraCalibration.calculatePixelToPhysicalRatio(imagePath);

                        // check to see if calibration pattern was found
                        if (ccResult.ratio == -1d) {
                            new AlertDialog.Builder(context)
                                    .setMessage("Couldn't find a calibration pattern. Try again with a new picture?")
                                    .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            tryAgain = true;
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            tryAgain = false;
                                        }
                                    })
                                    .create().show();
                            return;
                        }

                        ccResult.cameraMatrix = new Mat();
                        ccResult.distanceCoefficients = new MatOfDouble();
                        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(ccResult.cameraMatrix);
                        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(ccResult.distanceCoefficients);
                        CameraCalibration.saveCameraProperties(context, ccResult.cameraMatrix, ccResult.distanceCoefficients);

                        // save calibration
                        int newCalibrationNumber = PersistedData.getTotalCalibrations(context, userName) + 1;
                        File calibrationDir = PathUtil.getCameraCalibrationDirectory(context, userName);
                        Date date = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("ddMMMyyyy", Locale.getDefault());

                        try {
                            FileOutputStream fout = new FileOutputStream(new File(calibrationDir, "Calibration" + newCalibrationNumber + "." + df.format(date) + ".obj"));
                            ObjectOutputStream oos = new ObjectOutputStream(fout);
                            oos.writeObject(ccResult);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // save the new calibration number
                        PersistedData.setTotalCalibrations(context, userName, newCalibrationNumber);
                    }
                });
    }

    public void show() {
        new AlertDialog.Builder(context)
            .setMessage("Camera Calibration")
            .setNeutralButton("Take Calibration Picture", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    do {
                        cameraLauncher.launch("image/*");
                    } while(tryAgain);
                }
            })
            .setNegativeButton("Cancel", null).create().show();
    }
}
