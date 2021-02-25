package com.onrpiv.uploadmedia.Utilities;

import android.os.Environment;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.CalibrateCRF;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.cvtColor;

//https://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_calib3d/py_calibration/py_calibration.html
//https://docs.opencv.org/3.4/javadoc/org/opencv/calib3d/Calib3d.html
public final class CameraCalibration {
    public boolean isCalibrated = false;
    public double pixelToPhysicalRatio = 1.0d;

    private int patternRows = 14;
    private int patternCols = 20;

    // TODO print out pattern and measure centimeters
    private int physicalX = 1;  // width distance between circles/dots on calibration plate in centimeters
    private int physicalY = 1;  // height distance between circles/dots on calibration plate in centimeters

    //https://docs.opencv.org/master/d9/d0c/group__calib3d.html#ga687a1ab946686f0d85ae0363b5af1d7b
    private List<Mat> objPoints = new ArrayList<>();   // Calibration input of frames in 3D real-world space; Note: We are only using one camera so this will be frames with a zero z-dimension.
    private List<Mat> imgPoints = new ArrayList<>();   // Calibration input of frames in 2D; Load normal frames into here
    private Mat cameraMatrix = new Mat();              // Calibration input/output of 3x3 camera intrinsic matrix
    private Mat distCoeffs = new Mat();                // Calibration output of distortion coefficients
    private List<Mat> rVecs = new ArrayList<>();       // Calibration output of rotation vectors
    private List<Mat> tVecs = new ArrayList<>();       // Calibration output of translation vectors
    private MatOfPoint2f circleCenters = new MatOfPoint2f();


    public CameraCalibration() {
        OpenCVLoader.initDebug();

        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(cameraMatrix);
        cameraMatrix.put(0, 0, 1.0);

        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(distCoeffs);
    }

    // TODO this won't be needed because we are providing the pattern to the user via printout
    public CameraCalibration(int patternRows, int patternCols, int physicalX, int physicalY) {
        OpenCVLoader.initDebug();

        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(cameraMatrix);
        cameraMatrix.put(0, 0, 1.0);

        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(distCoeffs);

        this.patternRows = patternRows;
        this.patternCols = patternCols;
        this.physicalX = physicalX;
        this.physicalY = physicalY;
    }

    /**
     * Calibrate will look to find a circle grid pattern in an image, and return a centimeter/pixel ratio.
     * @param calibrationImagePath: Path to image that has a circle grid pattern.
     */
    public void calibrate(String calibrationImagePath) {
        OpenCVLoader.initDebug();

        if (calibrationImagePath.equals("test")) {
            calibrationImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/calibration.png").getAbsolutePath();
        }
        Mat calibrationImg = Imgcodecs.imread(calibrationImagePath);
        boolean patternFound = findCirclesGrid(calibrationImg);
        if (patternFound) {
            calibrateCamera(calibrationImg);
        }
    }

    public void calibrate(List<String> calibrationImagePaths) {
        // TODO
//        Mat calibrationImg = Imgcodecs.imread(calibrationImagePath);
//        cvtColor(calibrationImg, calibrationImg, COLOR_BGR2GRAY);
//        boolean patternFound = findCirclesGrid(calibrationImg);
//        if (patternFound) {
//            calibrateCamera(calibrationImg);
//        }
    }

    public Mat undistortImage(Mat image) {
        cameraMatrix = Calib3d.getOptimalNewCameraMatrix(cameraMatrix, distCoeffs, image.size(), 1);

        Mat result = new Mat();
        Imgproc.undistort(image, result, cameraMatrix, distCoeffs);
        return result;
    }

    public List<Mat> undistortImages(List<Mat> images) {
        cameraMatrix = Calib3d.getOptimalNewCameraMatrix(cameraMatrix, distCoeffs, images.get(0).size(), 1);

        for (int i = 0; i < images.size(); i++) {
            Mat result = new Mat();
            Imgproc.undistort(images.get(i), result, cameraMatrix, distCoeffs);
            images.set(i, result);
        }

        return images;
    }

    private boolean findCirclesGrid(Mat calibrationImage) {
        boolean found = false;
        for (int rows = patternRows; rows > 1; rows--) {
            for (int cols = patternCols; cols > 1; cols--) {
                found = Calib3d.findCirclesGrid(calibrationImage, new Size(cols, rows), circleCenters);
                if (found) { break; }
            }
            if (found) { break; }
        }
        return found;
    }

    private void calibrateCamera(Mat calibrationImage) {
        // Optimize circle center positions; this might not be needed
        Imgproc.cornerSubPix(calibrationImage, circleCenters, new Size(5, 5), new Size(-1, -1), new TermCriteria(new double[] {TermCriteria.MAX_ITER, 30}));

        imgPoints.add(calibrationImage);

        // Add zeros to our z dimension because we're only doing a 2D calibration
        objPoints.add(Mat.zeros(patternRows*patternCols, 1, CvType.CV_32FC3));

        // Calibrate
        Calib3d.calibrateCamera(objPoints, imgPoints, calibrationImage.size(), cameraMatrix,  distCoeffs, rVecs, tVecs);
        isCalibrated = Core.checkRange(cameraMatrix) && Core.checkRange(distCoeffs);
    }

    private void calcPhysicalToPhysicalRatio() {
        // TODO are we going to use average center width and center height?

    }

    public double getPixelToPhysicalRatio() {
        return pixelToPhysicalRatio;
    }

    public Mat getCameraMatrix() {
        return cameraMatrix;
    }
    public Mat getDistortionCoeffs() {
        return distCoeffs;
    }
    public List<Mat> getRotationVectors() {
        return rVecs;
    }
    public List<Mat> getTranslationVectors() {
        return tVecs;
    }
}
