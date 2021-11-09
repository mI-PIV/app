package com.onrpiv.uploadmedia.Utilities.Camera.Calibration;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


//https://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_calib3d/py_calibration/py_calibration.html
//https://docs.opencv.org/3.4/javadoc/org/opencv/calib3d/Calib3d.html
public final class CameraCalibration {
    private final static int physicalLineLength = 300;  // The triangle's lines are 300 pixels long on a 8x11 paper

    public static double calculatePixelToPhysicalRatio(String calibrationImagePath) {
        OpenCVLoader.initDebug();

        Mat frame1 = Imgcodecs.imread(calibrationImagePath);
        Imgproc.cvtColor(frame1, frame1, Imgproc.COLOR_BGR2GRAY);

        double pixelCMRatio = findTriangle(frame1);

        //cleanup mats
        frame1.release();

        return pixelCMRatio;
    }

    /**
     * Undistort a single image using stored member variables found with calibrate() function.
     */
//    public Mat undistortImage() {
//        // TODO try with a 0 alpha parameter
//        // TODO also try without this
//        cameraMatrix = Calib3d.getOptimalNewCameraMatrix(cameraMatrix, distCoeffs, frame1.size(), 1);
//        Mat result = new Mat();
//        Imgproc.undistort(frame1, result, cameraMatrix, distCoeffs);
//        return result;
//    }

    private static Mat getTopRightQuadrant(Mat img) {
        Size origSize = img.size();
        double midY = origSize.height/2;
        double midX = origSize.width/2;
        Point tl = new Point(midX, 0);
        Point br = new Point(origSize.width, midY);

        Rect topRightQuad = new Rect(tl, br);
        return img.submat(topRightQuad);
    }

    public static void saveCameraProperties(Context context, Mat cameraMatrix, Mat distCoeffs) {
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

    private static double findTriangle(Mat calibrationImage) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat edges = new Mat();
        double result = -1d;

        // blur before edge detection to filter out smaller particles
        Imgproc.blur(calibrationImage, calibrationImage, new Size(3, 3));
        // edge detection
        Imgproc.Canny(calibrationImage, edges, (double)(255/3), 255);
        // contour detection
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        int stop = Math.min(contours.size(), 150);
        for (int i = 0; i < stop; i++) {
            MatOfPoint contour = contours.get(i);
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approx, 0.01*Imgproc.arcLength(contour2f, true), true);
            List<Point> points = approx.toList();
            if (points.size() >= 3) {
                boolean found = false;
                for (int j = 0; j < points.size() - 2; j++) {
                    double d1 = euclidean(points.get(j), points.get(j+1));
                    double d2 = euclidean(points.get(j+1), points.get(j+2));
                    double d3 = euclidean(points.get(j+2), points.get(j));
                    if (compareDistances(d1, d2, d3)) {
                        // Our triangle was found, now we just need to calculate pixels per metric
                        result = calcPixelsPerCentimeter(d1, d2, d3);

                        //cleanup mats
                        contour2f.release();
                        approx.release();
                        found = true;
                        break;
                    }
                }
                //cleanup mats
                contour2f.release();
                approx.release();

                if (found) { break;}
            }
        }
        // cleanup mats
        hierarchy.release();
        edges.release();
        for (MatOfPoint contour: contours) {
            contour.release();
        }
        contours.clear();

        return result;
    }

    private static double calcPixelsPerCentimeter(double d1, double d2, double d3) {
        double averageLength = (d1 + d2 + d3)/3;
        return averageLength/physicalLineLength;
    }

    private static double euclidean(Point a, Point b) {
        return Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
    }

    private static boolean compareDistances(double d1, double d2, double d3) {
        boolean result = false;
        double tolerance = 0.5;

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
}
