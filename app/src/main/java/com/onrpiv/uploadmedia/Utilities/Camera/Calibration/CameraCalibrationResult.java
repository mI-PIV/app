package com.onrpiv.uploadmedia.Utilities.Camera.Calibration;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.io.Serializable;

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
}
