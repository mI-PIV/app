package com.onrpiv.uploadmedia.Utilities.Camera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


//https://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_calib3d/py_calibration/py_calibration.html
//https://docs.opencv.org/3.4/javadoc/org/opencv/calib3d/Calib3d.html
public final class CameraCalibration {
    private final static int patternRows = 21;
    private final static int patternCols = 27;
    private final static int physicalLineLength = 300;  // The triangle's lines are 300 pixels long on a 8x11 paper

    //https://docs.opencv.org/master/d9/d0c/group__calib3d.html#ga687a1ab946686f0d85ae0363b5af1d7b
    private Mat cameraMatrix = new Mat();                 // Calibration input/output of 3x3 camera intrinsic matrix
    private MatOfDouble distCoeffs = new MatOfDouble();   // Calibration output of distortion coefficients
    private Mat frame1;
    private Mat frame2;

    public boolean foundTriangle = false;


    public CameraCalibration(Context context) {
        OpenCVLoader.initDebug();

        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(cameraMatrix);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(distCoeffs);

        getCameraProperties(context);
    }

    /**
     * Tries to find a triangle calibration pattern, calculates and returns the pixels per centimeter.
     * Will return 1.0 if no triangle calibration pattern found.
     * @param calibrationImagePath1: Path to first image that might have a circle grid pattern.
     * @param calibrationImagePath2: Path to second image that might have a circle grid pattern.
     * @return pixel to centimeter ratio for both frames
     */
    public double calibrate(String calibrationImagePath1, String calibrationImagePath2) {
        OpenCVLoader.initDebug();

        frame1 = Imgcodecs.imread(calibrationImagePath1);
        frame2 = Imgcodecs.imread(calibrationImagePath2);
        Imgproc.cvtColor(frame1, frame1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(frame2, frame2, Imgproc.COLOR_BGR2GRAY);

        double pixelCMRatio1 = findTriangle(frame1);
        double pixelCMRatio2 = findTriangle(frame2);

        return (pixelCMRatio1 + pixelCMRatio2) / 2;
    }

    /**
     * Undistort a single image using stored member variables found with calibrate() function.
     */
    public Mat undistortImage() {
        // TODO try with a 0 alpha parameter
        // TODO also try without this
        cameraMatrix = Calib3d.getOptimalNewCameraMatrix(cameraMatrix, distCoeffs, frame1.size(), 1);
        Mat result = new Mat();
        Imgproc.undistort(frame1, result, cameraMatrix, distCoeffs);
        return result;
    }

    private void getCameraProperties(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        float[] intrinsicValues;
        float[] distortionValues;

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            intrinsicValues = characteristics.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION);

            // SDK < 27
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                distortionValues = characteristics.get(CameraCharacteristics.LENS_RADIAL_DISTORTION);
            }
            // SDK > 28
            else {
                distortionValues = characteristics.get(CameraCharacteristics.LENS_DISTORTION);
            }
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
            intrinsicValues = null;
            distortionValues = null;
        }

        if (intrinsicValues == null && distortionValues == null) {
            cameraMatrix.put(0, 0, 1.0);
        }

        if (intrinsicValues != null) {
            float fx = intrinsicValues[0];
            cameraMatrix.put(0, 0, fx);
            float fy = intrinsicValues[1];
            cameraMatrix.put(1, 1, fy);
            float cx = intrinsicValues[2];
            cameraMatrix.put(0, 2, cx);
            float cy = intrinsicValues[3];
            cameraMatrix.put(1, 2, cy);
            float s = intrinsicValues[4];
            cameraMatrix.put(0, 1, s);
            cameraMatrix.put(2, 2, 1f);
        }

        if (distortionValues != null) {
            for (int i = 0; i < 5; i++) {
                distCoeffs.put(i, 0, distortionValues[i]);
            }
        }
    }

    private double findTriangle(Mat calibrationImage) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat edges = new Mat();

        // blur before edge detection to filter out smaller particles
        Imgproc.blur(calibrationImage, calibrationImage, new Size(3, 3));
        // edge detection
        Imgproc.Canny(calibrationImage, edges, (double)(255/3), 255);
        // contour detection
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        for (MatOfPoint contour: contours) {
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approx, 0.01*Imgproc.arcLength(contour2f, true), true);
            List<Point> points = approx.toList();
            if(points.size() == 3) {
                double d1 = euclidean(points.get(0), points.get(1));
                double d2 = euclidean(points.get(1), points.get(2));
                double d3 = euclidean(points.get(2), points.get(0));
                if (compareDistances(d1, d2, d3)) {
                    // Our triangle was found, now we just need to calculate pixels per metric
                    foundTriangle = true;
                    return calcPixelsPerCentimeter(d1, d2, d3);
                }
            }
        }
        // triangle wasn't found
        return 1d;
    }

    private double calcPixelsPerCentimeter(double d1, double d2, double d3) {
        double averageLength = (d1 + d2 + d3)/3;
        return averageLength/physicalLineLength;
    }

    private Mat createPatternImagePoints3D() {
        Mat result = new Mat(patternRows*patternCols, 1, CvType.CV_32FC3);

        for (int row = 0; row < patternRows; row++) {
            for (int col = 0; col < patternCols; col++) {

                // 2D to 1D indexing
                int index = row * patternCols + col;

                // When our calibration pattern is printed on an 8.5 x 11 paper,
                // then the center of the circles will be exactly 1 cm apart from each other,
                // both in terms of width and height
                result.put(index, 0, new float[] {(float)(row), (float)(col), 0f});
                // This means our output from solvePnP will be in terms of centimeters.
            }
        }
        return result;
    }

    private static double euclidean(Point a, Point b) {
        return Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
    }

    private static boolean compareDistances(double d1, double d2, double d3) {
        boolean result = false;
        double tolerance = 0.05;

        // Because we don't know how close/far our triangle is, we have to compare based on
        // ratios and not on a static number.
        double d1Ratio = Math.abs(d1 - d2) / ((d1 + d2)/2);
        double d2Ratio = Math.abs(d2 - d3) / ((d2 + d3)/2);
        double d3Ratio = Math.abs(d3 - d1) / ((d3 + d1)/2);

        if (d1Ratio < tolerance && d2Ratio < tolerance && d3Ratio < tolerance) {
            result = true;
        }
        return result;
    }

    public Mat getCameraMatrix() {
        return cameraMatrix;
    }
    public Mat getDistortionCoeffs() {
        return distCoeffs;
    }
}
