package com.onrpiv.uploadmedia.Utilities.Camera.Calibration;

import android.content.Context;

import androidx.collection.ArrayMap;

import com.onrpiv.uploadmedia.Utilities.PathUtil;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;

public class CameraCalibrationResult implements Serializable {
    public double ratio;
    public Mat cameraMatrix;
    public MatOfDouble distanceCoefficients;

    public void finalize() {
        if (null != cameraMatrix)
            cameraMatrix.release();
        if (null != distanceCoefficients)
            distanceCoefficients.release();
    }

    public static ArrayMap<String, String> getSavedCalibrationNamesMapping(Context context,
                                                                           String userName) {
        ArrayMap<String, String> filenamesToPrettyPrint = new ArrayMap<>();
        File calibrationDir = PathUtil.getCameraCalibrationDirectory(context, userName);
        for (File calibrationFile : Objects.requireNonNull(calibrationDir.listFiles())) {
            String filename = calibrationFile.getName();
            String[] filenameArray = filename.split("\\.");

            filenamesToPrettyPrint.put("#" + filenameArray[1] + ": " + filenameArray[2], filename);
        }
        return filenamesToPrettyPrint;
    }

    public static CameraCalibrationResult loadCalibrationByName(Context context, String userName,
                                                                String calibrationFilename) {
        File calibrationDir = PathUtil.getCameraCalibrationDirectory(context, userName);
        File calibrationObjFile = new File(calibrationDir, calibrationFilename);
        CameraCalibrationResult result = new CameraCalibrationResult();

        try {
            FileInputStream fin = new FileInputStream(calibrationObjFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            result = (CameraCalibrationResult) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
