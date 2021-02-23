package com.onrpiv.uploadmedia.Utilities;

import android.os.Environment;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringJoiner;

import static org.opencv.imgproc.Imgproc.INTER_CUBIC;

//https://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_calib3d/py_calibration/py_calibration.html
//https://docs.opencv.org/3.4/javadoc/org/opencv/calib3d/Calib3d.html
public final class CameraCalibration {
    private static int patternRows = 14;
    private static int patternCols = 20;

    private CameraCalibration() {
        // EMPTY
    }

    /**
     * Calibration testing constructor. Loads a default circle grid.
     * ONLY USE THIS FOR TESTING. WILL DELETE WHEN TESTING IS COMPLETE.
     * @return
     */
    public static double Calibrate() {
        OpenCVLoader.initDebug();
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/calibration.png");
        Mat calImg = Imgcodecs.imread(path.getAbsolutePath());
        return findCirclesGrid(calImg, 1d, 1d);
    }

    // TODO are we going to use centimeters?
    /**
     * Calibrate will look to find a circle grid pattern in an image, and return a centimeter/pixel ratio.
     * @param calibrationImagePath: Path to image that has a circle grid pattern.
     * @param circleRows: Number of circle rows in the circle grid pattern.
     * @param circleCols: Number of circle columns in the circle grid pattern.
     * @param xDiff_CM: The width between two circle centers in Centimeters.
     * @param yDiff_CM: The height between two circle centers in Centimeters.
     * @return double centimeter/pixel ratio.
     */
    public static double Calibrate(String calibrationImagePath, int circleRows, int circleCols, double xDiff_CM, double yDiff_CM) {
        OpenCVLoader.initDebug();
        patternCols = circleCols;
        patternRows = circleRows;
        Mat calibrationImg = Imgcodecs.imread(calibrationImagePath);
        return findCirclesGrid(calibrationImg, xDiff_CM, yDiff_CM);
    }

    private static double findCirclesGrid(Mat calibrationImage, double xDiff, double yDiff) {
        Size patternSize = new Size(patternCols, patternRows);
        MatOfPoint2f circleCenters = new MatOfPoint2f();
        double resultRatio = 1d;

        // TODO use tags for camera aperture, focal length, etc...

        boolean found = Calib3d.findCirclesGrid(calibrationImage, patternSize, circleCenters);
        // Format of the returned structure (circle centers) is index: x, y

        if (found) {
            resultRatio = getPhysicalToPixelRatio(xDiff, yDiff);
        }
        return resultRatio;
    }

    private static double getPhysicalToPixelRatio(double xDiff, double yDiff) {
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
