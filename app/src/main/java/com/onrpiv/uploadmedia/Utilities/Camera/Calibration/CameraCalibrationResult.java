package com.onrpiv.uploadmedia.Utilities.Camera.Calibration;

import android.content.Context;

import androidx.collection.ArrayMap;

import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.PathUtil;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class CameraCalibrationResult implements Serializable {
    public double ratio;
    private double[][] cameraMatrix;
    private double[][] distanceCoefficients;

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
        return (CameraCalibrationResult) FileIO.read(calibrationObjFile);
    }

    public void saveCameraMatrix(Mat cameraMatrix) {
        // 3x3 matrix
        this.cameraMatrix = new double[3][3];
        for (int y = 0; y < cameraMatrix.rows(); y++) {
            for (int x = 0; x < cameraMatrix.cols(); x++) {
                this.cameraMatrix[y][x] = cameraMatrix.get(y, x)[0];
            }
        }
    }

    public Mat getCameraMatrix() {
        Mat cameraMatrix = new Mat();
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(cameraMatrix);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                cameraMatrix.put(y, x, this.cameraMatrix[y][x]);
            }
        }
        return cameraMatrix;
    }

    public void saveDistanceCoeffs(Mat distCoeffs) {
        // 5x1 matrix
        this.distanceCoefficients = new double[5][1];
        for (int y = 0; y < distCoeffs.rows(); y++) {
            distanceCoefficients[y][0] = distCoeffs.get(y, 0)[0];
        }
    }

    public Mat getDistCoeffsMatrix() {
        Mat distCoeffs = new MatOfDouble();
        Mat.ones(5, 1, CvType.CV_64FC1).copyTo(distCoeffs);
        for (int y = 0; y < 5; y++) {
            distCoeffs.put(y, 0, this.distanceCoefficients[y][0]);
        }
        return distCoeffs;
    }
}
