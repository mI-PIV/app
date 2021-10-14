package com.onrpiv.uploadmedia.Utilities.Camera.Calibration;

import android.content.Context;

import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.PathUtil;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraCalibrationResult implements Serializable {
    private static final long serialVersionUID = 5257095847490394137L;

    public double ratio;
    private double[][] cameraMatrix;
    private double[][] distanceCoefficients;

    public static String getSavedCalibrationPrettyPrint(Context context, String userName) {
        File calibrationFile = new File(PathUtil.getUserDirectory(context, userName), "calibration.obj");

        String prettyName = null;
        if (calibrationFile.exists()) {
            Date lastModified = new Date(calibrationFile.lastModified());
            prettyName = new SimpleDateFormat("ddMMMyyyy").format(lastModified);
        }

        return prettyName;
    }

    public static CameraCalibrationResult loadCalibration(Context context, String userName) {
        File calibrationFile = new File(PathUtil.getUserDirectory(context, userName), "calibration.obj");
        return (CameraCalibrationResult) FileIO.read(calibrationFile);
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
