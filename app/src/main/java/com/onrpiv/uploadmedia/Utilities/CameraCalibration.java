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

    // TODO are we going to use centimeters?
    /**
     * Calibrate will look to find a circle grid pattern in an image, and return a centimeter/pixel ratio.
     * @param calibrationImagePath: Path to image that has a circle grid pattern.
     */
    public void calibrate(String calibrationImagePath) {
        OpenCVLoader.initDebug();

        if (calibrationImagePath.equals("test")) {
            calibrationImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/piv_calib.png").getAbsolutePath();
        }
        Mat calibrationImg = Imgcodecs.imread(calibrationImagePath);
//        cvtColor(calibrationImg, calibrationImg, COLOR_BGR2GRAY);
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

    private boolean findCirclesGrid(Mat calibrationImage) {
        Size patternSize = new Size(patternCols, patternRows);
        return Calib3d.findCirclesGrid(calibrationImage, patternSize, circleCenters);
    }

    private void calibrateCamera(Mat calibrationImage) {
        // Optimize circle center positions; this might not be needed
        Imgproc.cornerSubPix(calibrationImage, circleCenters, new Size(5, 5), new Size(-1, -1), new TermCriteria(new double[] {TermCriteria.MAX_ITER, 30}));

        imgPoints.add(calibrationImage);

        objPoints.add(Mat.zeros(patternRows*patternCols, 1, CvType.CV_32FC3));

        // Calibrate
        Calib3d.calibrateCamera(objPoints, imgPoints, calibrationImage.size(), cameraMatrix,  distCoeffs, rVecs, tVecs);
        isCalibrated = Core.checkRange(cameraMatrix) && Core.checkRange(distCoeffs);
    }

    private static double getPhysicalToPixelRatio() {
        // TODO are we going to use average center width and center height?
        return 0d;
    }

    private static void saveImage(Mat image1, String userName, String stepName, String imgFileSaveName)
    {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);

        if (!storageDirectory.exists()) storageDirectory.mkdir();
        File pngFile = new File(storageDirectory, stepName+"_"+imgFileSaveName);
        Mat resizeimage = new Mat();
        Size scaleSize = new Size(2560,1440);
        Imgproc.resize(image1, resizeimage, scaleSize, 0,0, INTER_CUBIC);
        Imgcodecs.imwrite(pngFile.getAbsolutePath(), resizeimage);
    }

    private static void saveCalibValues(Mat mat, String userName, String stepName, String imgFileSaveName) {
        double[] v;
        ArrayList<String> toPrint = new ArrayList<>();

        //clear out old file////////////////////////
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);
        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();
        File txtFile = new File(storageDirectory, stepName + "_"+imgFileSaveName+".txt");
        if(txtFile.exists() && txtFile.isFile()){
            txtFile.delete();
        }
        ////////////////////////////////////////////////////////////

        for (int i = 0; i < mat.rows(); i++) {
            v = mat.get(i, 0);

            toPrint.add(String.valueOf(i));
            toPrint.add(String.valueOf(v[0]));
            toPrint.add(String.valueOf(v[1]));

            StringJoiner sj1 = new StringJoiner(",  ");
            sj1.add(toPrint.get(0)).add(toPrint.get(1)).add(toPrint.get(2));
            saveToFile(sj1.toString(), userName, stepName, imgFileSaveName);
            toPrint.clear();
        }
    }

    private static void saveToFile(String data, String userName, String stepName, String imgFileSaveName){
        try {
            File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);
            // Then we create the storage directory if does not exists
            if (!storageDirectory.exists()) storageDirectory.mkdir();
            File txtFile = new File(storageDirectory, stepName + "_"+imgFileSaveName+".txt");

            FileOutputStream fileOutputStream = new FileOutputStream(txtFile,true);
            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());
            fileOutputStream.close();
        }  catch(FileNotFoundException ex) {
            Log.d("", ex.getMessage());
        }  catch(IOException ex) {
            Log.d("", ex.getMessage());
        }
    }
}
