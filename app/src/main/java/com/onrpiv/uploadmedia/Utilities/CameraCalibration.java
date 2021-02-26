package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.lang.Math;


//https://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_calib3d/py_calibration/py_calibration.html
//https://docs.opencv.org/3.4/javadoc/org/opencv/calib3d/Calib3d.html
public final class CameraCalibration {
    public boolean isCalibrated = false;
    public double pixelToPhysicalRatio = 1.0d;

    private final static int patternRows = 21;
    private final static int patternCols = 27;

    private final static double physicalX = 1d;  // width distance between circles/dots on calibration plate in centimeters
    private final static double physicalY = 1d;  // height distance between circles/dots on calibration plate in centimeters

    //https://docs.opencv.org/master/d9/d0c/group__calib3d.html#ga687a1ab946686f0d85ae0363b5af1d7b
    private MatOfPoint3f objPoints;                       // Calibration input of frames in 3D real-world space; Note: We are only using one camera so this will be frames with a zero z-dimension.
    private Mat cameraMatrix = new Mat();                 // Calibration input/output of 3x3 camera intrinsic matrix
    private MatOfDouble distCoeffs = new MatOfDouble();   // Calibration output of distortion coefficients
    private Mat rVecs = new Mat();                        // Calibration output of rotation vectors
    private Mat tVecs = new Mat();                        // Calibration output of translation vectors
    private MatOfPoint2f circleCenters = new MatOfPoint2f();


    public CameraCalibration(Context context) {
        OpenCVLoader.initDebug();

        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(cameraMatrix);
        setCameraMatrix(context);
        objPoints = new MatOfPoint3f(createPatternImagePoints3D());
    }

    /**
     * Calibrate will look to find a circle grid pattern in an image, then find distortion coefficients, translation and rotation vectors,
     * and finally stores them into member variables for further calculations and undistortions.
     * @param calibrationImagePath: Path to image that might have a circle grid pattern.
     */
    public void calibrate(String calibrationImagePath) {
        OpenCVLoader.initDebug();

        // TODO debug delete
        if (calibrationImagePath.equals("test")) {
            calibrationImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/calibrationPattern.png").getAbsolutePath();
        }

        Mat calibrationImg = Imgcodecs.imread(calibrationImagePath);
        Imgproc.cvtColor(calibrationImg, calibrationImg, Imgproc.COLOR_BGR2GRAY);

        // TODO debug delete
        Core.rotate(calibrationImg, calibrationImg, Core.ROTATE_90_COUNTERCLOCKWISE);
        Imgproc.resize(calibrationImg, calibrationImg, new Size(2560, 1440));

        boolean patternFound = findCirclesGrid(calibrationImg);
        if (patternFound) {
            calibrateCamera(calibrationImg);
        }
    }

    private void setCameraMatrix(Context context) {
        CameraCharacteristics.Key<float[]> intrinsicCameraKey = CameraCharacteristics.LENS_INTRINSIC_CALIBRATION;
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        float[] intrinsicValues;

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            intrinsicValues = characteristics.get(intrinsicCameraKey);
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
            intrinsicValues = null;
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
        else
        {
            cameraMatrix.put(0, 0, 1.0);
        }
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

        // Calibrate
        Calib3d.solvePnP(objPoints, circleCenters, cameraMatrix, distCoeffs, rVecs, tVecs);

        isCalibrated = Core.checkRange(cameraMatrix) && Core.checkRange(distCoeffs);

        Log.i("kp", "Camera matrix: " + cameraMatrix.dump());
        Log.i("kp", "PP tvec: " + tVecs.dump());
        Log.i("kp", "PP rvec: " + rVecs.dump());
        Log.i("kp", "PP Distortion coefficients: " + distCoeffs.dump());

        Mat R = new Mat();
        Calib3d.Rodrigues(tVecs, R);
        Log.i("kp", "PP R: " + R.dump());


        // TODO might not want to calculate physical measurements here
//        double test = calcPhysicalToPixelRatio(0, 0);
    }

    //https://www.pyimagesearch.com/2016/04/04/measuring-distance-between-objects-in-an-image-with-opencv/
//    private double calcPhysicalToPixelRatio(int y, int x) {
//
//        // TODO this needs work and testing once we get an image with a pattern printout
//
//        List<double[]> look = new ArrayList<>();
//        for (int i = 0; i < tVecs.size(); i++) {
//            Mat image = tVecs.get(i);
//            for (int rows = 0; rows < image.rows(); rows++) {
//                for (int cols = 0; cols < image.cols(); cols++) {
//                    look.add(image.get(rows, cols));
//                }
//            }
//        }
////        double[] vec = image.get(y,x);
////        double ty = vec[2];
////        double tx = vec[1];
//
////        return Math.sqrt(tx*tx + ty*ty) * Math.sqrt(physicalX * physicalX + physicalY * physicalY);
//        return 0d;
//    }

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

    public double getPixelToPhysicalRatio() {
        return pixelToPhysicalRatio;
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
