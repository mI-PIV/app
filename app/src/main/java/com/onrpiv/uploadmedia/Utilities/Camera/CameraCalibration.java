package com.onrpiv.uploadmedia.Utilities.Camera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Environment;

import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


//https://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_calib3d/py_calibration/py_calibration.html
//https://docs.opencv.org/3.4/javadoc/org/opencv/calib3d/Calib3d.html
public final class CameraCalibration {
    private final static int patternRows = 21;
    private final static int patternCols = 27;

    private final static double physicalX = 1d;  // width distance between circles/dots on calibration pattern in centimeters
    private final static double physicalY = 1d;  // height distance between circles/dots on calibration pattern in centimeters

    //https://docs.opencv.org/master/d9/d0c/group__calib3d.html#ga687a1ab946686f0d85ae0363b5af1d7b
    private MatOfPoint3f objPoints;                       // Calibration input of frames in 3D real-world space; Note: We are only using one camera so this will be frames with a zero z-dimension.
    private Mat cameraMatrix = new Mat();                 // Calibration input/output of 3x3 camera intrinsic matrix
    private MatOfDouble distCoeffs = new MatOfDouble();   // Calibration output of distortion coefficients
    private Mat rVecs = new Mat();                        // Calibration output of rotation vectors
    private Mat tVecs = new Mat();                        // Calibration output of translation vectors
    private MatOfPoint2f circleCenters = new MatOfPoint2f();

    private Mat frame1;
    private Mat frame2;


    public CameraCalibration(Context context) {
        OpenCVLoader.initDebug();

        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(cameraMatrix);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(distCoeffs);

        getCameraProperties(context);
        objPoints = new MatOfPoint3f(createPatternImagePoints3D());
    }

    /**
     * Calibrate will look to find a circle grid pattern in an image, then find distortion coefficients, translation and rotation vectors,
     * and finally stores them into member variables for further calculations and undistortions.
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

        double pixelCMRatio1 = calibratePipeline(frame1);
        double pixelCMRatio2 = calibratePipeline(frame2);

        return (pixelCMRatio1 + pixelCMRatio2) / 2;
    }

    /**
     * Undistort a single image using stored member variables found with calibrate() function.
     */
    public Mat undistortImage() {
        cameraMatrix = Calib3d.getOptimalNewCameraMatrix(cameraMatrix, distCoeffs, frame1.size(), 1);
        Mat result = new Mat();
        Imgproc.undistort(frame1, result, cameraMatrix, distCoeffs);
        return result;
    }

    private double calibratePipeline(Mat img) {
        double pixelsPerCentimeter = 1d;
        boolean patternFound = findCirclesGrid(img);
        if (patternFound) {
            calibrateCamera(img);
            if (isCalibrated()) {
                pixelsPerCentimeter = calcPixelsPerCentimeter();
            }
        }
        return pixelsPerCentimeter;
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

    private boolean findCirclesGrid(Mat calibrationImage) {
        boolean found = false;
        for (int rows = (int) (patternRows * 0.75); rows > 1; rows--) {
            for (int cols = (int) (patternCols * 0.75); cols > 1; cols--) {
                found = Calib3d.findCirclesGrid(calibrationImage, new Size(cols, rows), circleCenters);
                if (found) { break; }
            }
            if (found) { break; }
        }
        return found;
    }

    private void calibrateCamera(Mat calibrationImage) {
        // Optimize circle center positions; this might not be needed
//        Imgproc.cornerSubPix(calibrationImage, circleCenters, new Size(5, 5), new Size(-1, -1), new TermCriteria(new double[] {TermCriteria.MAX_ITER, 30}));

        // Calibrate
        Calib3d.solvePnP(objPoints, circleCenters, cameraMatrix, distCoeffs, rVecs, tVecs);
    }

    //https://www.pyimagesearch.com/2016/04/04/measuring-distance-between-objects-in-an-image-with-opencv/
    private double calcPixelsPerCentimeter() {
        double sumDistance = 0d;
        int count = 0;
        for (int i = 1; i < patternCols*patternRows; i++) {
            // Make sure our two points are in the same row
            if (i/patternCols != (i-1)/patternCols) { continue; }

            Point point0 = new Point(circleCenters.get(i-1, 0));
            Point point1 = new Point(circleCenters.get(i, 0));

            sumDistance += euclidean(point0, point1);
            count++;
        }

        // since our circle centers will be 1 cm away from each other (on an 8.5 x 11 inch paper),
        // then the average distance between the circle centers will be our pixels/cm ratio.
        return sumDistance / count;
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

    public boolean isCalibrated() {
        return Core.checkRange(cameraMatrix)
                && Core.checkRange(distCoeffs)
                && Core.checkRange(rVecs)
                && Core.checkRange(tVecs);
    }
    public Mat getCameraMatrix() {
        return cameraMatrix;
    }
    public Mat getDistortionCoeffs() {
        return distCoeffs;
    }
    public Mat getRotationVectors() {
        return rVecs;
    }
    public Mat getTranslationVectors() {
        return tVecs;
    }
}
