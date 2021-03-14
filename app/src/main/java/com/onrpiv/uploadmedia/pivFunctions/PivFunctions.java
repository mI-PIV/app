package com.onrpiv.uploadmedia.pivFunctions;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.onrpiv.uploadmedia.Utilities.ArrowDrawOptions;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.opencv.core.Core.mean;
import static org.opencv.core.Core.subtract;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgproc.Imgproc.COLORMAP_JET;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2BGRA;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class PivFunctions {
    private int windowSize;
    private int overlap;
    private double dt;
    private String sig2noise_method;
    private final Mat frame1;
    private final Mat frame2;
    private final Mat grayFrame1;
    private final Mat grayFrame2;
    private final int rows;
    private final int cols;

    public PivFunctions(String imagePath1,
                        String imagePath2,
                        int mWindow_size,
                        int mOverlap,
                        double mDt,
                        String mSig2noise_method) {

        frame1 = Imgcodecs.imread(imagePath1);
        frame2 = Imgcodecs.imread(imagePath2);

        rows = frame1.rows();
        cols = frame1.cols();

        grayFrame1 = new Mat(frame1.rows(), frame1.cols(), frame1.type());
        cvtColor(frame1, grayFrame1, COLOR_BGR2GRAY);

        grayFrame2 = new Mat(frame2.rows(), frame2.cols(), frame2.type());
        cvtColor(frame2, grayFrame2, COLOR_BGR2GRAY);

        windowSize = mWindow_size;
        overlap = mOverlap;
        dt = mDt;
        sig2noise_method = mSig2noise_method;
    }

    private static Map<String, Integer> getFieldShape(int imgCols, int imgRows, int areaSize, int overlap) {
        int nRows = ((imgRows - areaSize) / (areaSize - overlap) + 1);
        int nCols = ((imgCols - areaSize) / (areaSize - overlap) + 1);
        Map<String, Integer> map = new HashMap();
        map.put("nRows", nRows);
        map.put("nCols", nCols);
        return map;

    }

    private static Mat openCvPIV(Mat image, Mat temp) {
        int P = temp.rows();
        int Q = temp.cols();

        int M = image.rows();
        int N = image.cols();

        Mat Xt = Mat.zeros(M + 2 * (P - 1), N + 2 * (Q - 1), CvType.CV_8U);
        Rect rect = new Rect((P - 1), (Q - 1), image.width(), image.height());
        Mat submat = Xt.submat(rect);
        image.copyTo(submat);

        Mat outputCorr = new Mat();
        Imgproc.matchTemplate(Xt, temp, outputCorr, Imgproc.TM_CCORR);
        return outputCorr;
    }

    private static Map<String, Double> sig2Noise_update(Mat corr) {
        Core.MinMaxLocResult mmr = Core.minMaxLoc(corr);

        int peak1_i = (int) mmr.maxLoc.x;
        int peak1_j = (int) mmr.maxLoc.y;
        double peak1_value = mmr.maxVal;

        corr.put(peak1_j, peak1_i, 0.0);

        Core.MinMaxLocResult mmr2 = Core.minMaxLoc(corr);
        double peak2_value = mmr2.maxVal;

        double sig2Noise = peak1_value / peak2_value;

        Map<String, Double> map = new HashMap();
        map.put("sig2Noise", sig2Noise);
        return map;
    }


    public static Map<String, double[][]> extendedSearchAreaPiv_update(
            Mat grayFrame1,
            Mat grayFrame2,
            int rows,
            int cols,
            int windowSize,
            int overlap) {
        int i1t, j1l;
        int i2t, j2l;

        int search_area_size = windowSize;

        //get field shape
        Map<String, Integer> fieldShape = getFieldShape(cols, rows, search_area_size, overlap);

        double[][] dr1 = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] dc1 = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];

        double[][] eps_r = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] eps_c = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];

        double[][] mag = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] sig2noise = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];


        Mat corr;
        double win1_avg;
        double win2_avg;

        int nr = fieldShape.get("nRows");
        int nc = fieldShape.get("nCols");

        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {

                Mat window_a_1 = Mat.zeros(windowSize, windowSize, CvType.CV_8U);
                Mat window_b_1 = Mat.zeros(windowSize, windowSize, CvType.CV_8U);

/////////////////////////////////////old code gave errors when overlap was not even  ///////////////////////////////////////////////////////////////////////////////
//                i1t = 0 + overlap*(i+1) - overlap;
//
//                j1l = 0 + overlap*(j+1) - overlap;
//
//                Log.d("WINDOWS: ", (i1t)+":"+(i1t+windowSize)+" ,"+(j1l)+":"+(j1l+windowSize));
//                Rect rectWin_a = new Rect(j1l, i1t, windowSize, windowSize);
//
//                Mat window_a = new Mat(image1, rectWin_a);
//
//                i2t = 0 + overlap*(i+1) - overlap;
//
//                j2l = 0 + overlap*(j+1) - overlap;
//
//                Log.d("WINDOWS2: ", (i2t)+":"+(i2t+windowSize)+" ,"+(j2l)+":"+(j2l+windowSize));
//                Rect rectWin_b = new Rect(j2l, i2t, windowSize, windowSize);
//
//                Mat window_b = new Mat(image2, rectWin_b);
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//             Select first the largest window, work like usual from the top left corner the left edge goes as:
//            # e.g. 0, (search_area_size - overlap), 2*(search_area_size - overlap),....

                i1t = i * (windowSize - overlap);

//              same for top-bottom
                j1l = j * (windowSize - overlap);

                Rect rectWin_a = new Rect(j1l, i1t, windowSize, windowSize);
                Mat window_a = new Mat(grayFrame1, rectWin_a);

//                now shift the left corner of the smaller window inside the larger one
                i1t += (windowSize - windowSize) / 2;
                j1l += (windowSize - windowSize) / 2;

                Rect rectWin_b = new Rect(j1l, i1t, windowSize, windowSize);
                Mat window_b = new Mat(grayFrame2, rectWin_b);

                win1_avg = mean(window_a).val[0];
                win2_avg = mean(window_b).val[0];

                subtract(window_a, new Scalar(win1_avg), window_a_1);
                subtract(window_b, new Scalar(win2_avg), window_b_1);

                corr = openCvPIV(window_a_1, window_b_1);
                Core.MinMaxLocResult mmr = Core.minMaxLoc(corr);

                int c = (int) mmr.maxLoc.x;
                int r = (int) mmr.maxLoc.y;

//                Log.d("WINDOWS: ", "i: "+i+" j:"+j);
                try {
                    eps_r[i][j] = (Math.log(corr.get(r - 1, c)[0]) - Math.log(corr.get(r + 1, c)[0])) / (2 * (Math.log(corr.get(r - 1, c)[0]) - 2 * Math.log(corr.get(r, c)[0]) + Math.log(corr.get(r + 1, c)[0])));
                    eps_c[i][j] = (Math.log(corr.get(r, c - 1)[0]) - Math.log(corr.get(r, c + 1)[0])) / (2 * (Math.log(corr.get(r, c - 1)[0]) - 2 * Math.log(corr.get(r, c)[0]) + Math.log(corr.get(r, c + 1)[0])));

                    dr1[i][j] = (windowSize - 1) - (r + eps_r[i][j]);
                    dc1[i][j] = (windowSize - 1) - (c + eps_c[i][j]);
                } catch (Exception e) {
                    dr1[i][j] = 0.0;
                    dc1[i][j] = 0.0;
                }
                mag[i][j] = Math.sqrt(Math.pow(dr1[i][j], 2) + Math.pow(dc1[i][j], 2));

                Map<String, Double> sig2NoiseRatio = sig2Noise_update(corr);
                sig2noise[i][j] = sig2NoiseRatio.get("sig2Noise");
            }
        }

        Map<String, double[][]> map = new HashMap();
        map.put("u", dc1);
        map.put("v", dr1);
        map.put("magnitude", mag);
        map.put("sig2Noise", sig2noise);
        return map;
    }

    public static Map<String, double[]> getCoordinates(int rows, int cols, int windowSize, int overlap) {
        Map<String, Integer> fieldShape = getFieldShape(cols, rows, windowSize, overlap);

        double[] x = new double[fieldShape.get("nCols")];
        double[] y = new double[fieldShape.get("nRows")];
        double[][] yx;

        for (int i = 0; i < fieldShape.get("nCols"); i++) {
            x[i] = i * (windowSize - overlap) + (windowSize) / 2.0;
        }

        for (int j = 0; j < fieldShape.get("nRows"); j++) {
            y[j] = j * (windowSize - overlap) + (windowSize) / 2.0;
        }

        Map<String, double[]> map = new HashMap();
        map.put("x", x);
        map.put("y", y);
        return map;
    }

    private static void saveToFile(String data, String userName, String stepName, String imgFileSaveName) {
        try {
            File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);
            // Then we create the storage directory if does not exists
            if (!storageDirectory.exists()) storageDirectory.mkdir();
            File txtFile = new File(storageDirectory, stepName + "_" + imgFileSaveName + ".txt");

            FileOutputStream fileOutputStream = new FileOutputStream(txtFile, true);
            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException ex) {
            Log.d("", ex.getMessage());
        } catch (IOException ex) {
            Log.d("", ex.getMessage());
        }
    }

    public static void saveVectors(Map<String, double[][]> pivCorrelation, Map<String, double[]> interrCenters, String userName, String stepName, String imgFileSaveName, double dt) {
        double ux, vy, q, x, y;
        ArrayList<String> toPrint = new ArrayList<>();

        //clear out old file////////////////////////
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);
        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();
        File txtFile = new File(storageDirectory, stepName + "_" + imgFileSaveName + ".txt");
        if (txtFile.exists() && txtFile.isFile()) {
            txtFile.delete();
        }
        ////////////////////////////////////////////////////////////

        for (int i = 0; i < interrCenters.get("y").length; i++) {
            for (int j = 0; j < interrCenters.get("x").length; j++) {
                x = interrCenters.get("x")[j];
                y = interrCenters.get("y")[i];
                ux = pivCorrelation.get("u")[i][j] * dt;
                vy = pivCorrelation.get("v")[i][j] * dt;
                q = pivCorrelation.get("sig2Noise")[i][j];

                toPrint.add(String.valueOf(x));
                toPrint.add(String.valueOf(y));
                toPrint.add(String.valueOf(ux));
                toPrint.add(String.valueOf(vy));
                toPrint.add(String.valueOf(q));

                StringJoiner sj1 = new StringJoiner(",  ");
                sj1.add(toPrint.get(0)).add(toPrint.get(1)).add(toPrint.get(2)).add(toPrint.get(3)).add(toPrint.get(4));
                saveToFile(sj1.toString(), userName, stepName, imgFileSaveName);
                toPrint.clear();
            }
        }
    }

    public static void saveVectorCentimeters(Map<String, double[][]> pivCorrelation, Map<String, double[]> interrCenters, double pixelToCM, String userName, String stepName, String imgFileSaveName, double dt) {
        double ux, vy, q, x, y;
        ArrayList<String> toPrint = new ArrayList<>();

        //clear out old file////////////////////////
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);
        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();
        File txtFile = new File(storageDirectory, stepName + "_" + imgFileSaveName + ".txt");
        if (txtFile.exists() && txtFile.isFile()) {
            txtFile.delete();
        }
        ////////////////////////////////////////////////////////////

        for (int i = 0; i < interrCenters.get("y").length; i++) {
            for (int j = 0; j < interrCenters.get("x").length; j++) {
                x = interrCenters.get("x")[j];
                y = interrCenters.get("y")[i];
                ux = (pivCorrelation.get("u")[i][j] * dt) * pixelToCM;
                vy = (pivCorrelation.get("v")[i][j] * dt) * pixelToCM;
                q = pivCorrelation.get("sig2Noise")[i][j];

                toPrint.add(String.valueOf(x));
                toPrint.add(String.valueOf(y));
                toPrint.add(String.valueOf(ux));
                toPrint.add(String.valueOf(vy));
                toPrint.add(String.valueOf(q));

                StringJoiner sj1 = new StringJoiner(",  ");
                sj1.add(toPrint.get(0)).add(toPrint.get(1)).add(toPrint.get(2)).add(toPrint.get(3)).add(toPrint.get(4));
                saveToFile(sj1.toString(), userName, stepName, imgFileSaveName);
                toPrint.clear();
//                Log.d("TEXT: ", "y: "+y+" x: "+x+" ux: "+ux+" vy: "+vy+" q: "+q);
//                Log.d("JOIN: ", "string join: "+ sj1.toString());
            }
        }
    }

    public static void saveVortMapFile(double[][] vortMap, String userName, String stepName, String imgFileSaveName) {
        double v;
        ArrayList<String> toPrint = new ArrayList<>();

        //clear out old file////////////////////////
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);
        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();
        File txtFile = new File(storageDirectory, stepName + "_" + imgFileSaveName + ".txt");
        if (txtFile.exists() && txtFile.isFile()) {
            txtFile.delete();
        }
        ////////////////////////////////////////////////////////////

        for (int y = 0; y < vortMap.length; y++) {
            for (int x = 0; x < vortMap[0].length; x++) {
                v = vortMap[y][x];

                toPrint.add(String.valueOf(x));
                toPrint.add(String.valueOf(y));
                toPrint.add(String.valueOf(v));

                StringJoiner sj1 = new StringJoiner(",  ");
                sj1.add(toPrint.get(0)).add(toPrint.get(1)).add(toPrint.get(2));
                saveToFile(sj1.toString(), userName, stepName, imgFileSaveName);
                toPrint.clear();
            }
        }
    }

    public void saveBaseImage(String userName, String stepName, String imgFileName) {
        saveImage(frame1, userName, stepName, imgFileName);
    }

    public static void createVectorField(Map<String, double[][]> pivCorrelation, Map<String, double[]> interrCenters, String userName, String stepName, String imgFileSaveName, ArrowDrawOptions arrowOptions, int rows, int cols) {
        Mat transparentBackground = new Mat(rows, cols, CV_8UC4, new Scalar(255, 255, 255, 0));

        int lineType = arrowOptions.lineType;
        int thickness = arrowOptions.thickness;
        double tipLength = arrowOptions.tipLength;
        double scale = arrowOptions.scale;

        double dx, dy;
        Point startPoint = null, endPoint = null;

        for (int i = 0; i < interrCenters.get("y").length; i++) {
            for (int j = 0; j < interrCenters.get("x").length; j++) {
                dx = pivCorrelation.get("u")[i][j];
                dy = -pivCorrelation.get("v")[i][j];

                if (dx < 0 && dy > 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] - (Math.abs(dx) * scale)),
                            (interrCenters.get("y")[i] - (Math.abs(dy) * scale)));
                } else if (dx > 0 && dy > 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] + (Math.abs(dx) * scale)),
                            (interrCenters.get("y")[i] - (Math.abs(dy) * scale)));
                } else if (dx > 0 && dy < 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] + (Math.abs(dx) * scale)),
                            (interrCenters.get("y")[i] + (Math.abs(dy) * scale)));
                } else if (dx < 0 && dy < 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] - (Math.abs(dx) * scale)),
                            (interrCenters.get("y")[i] + (Math.abs(dy) * scale)));
                } else if (dx == 0 && dy < 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point(interrCenters.get("x")[j], (interrCenters.get("y")[i] + (Math.abs(dy) * scale)));
                } else if (dx == 0 && dy > 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point(interrCenters.get("x")[j], (interrCenters.get("y")[i] - (Math.abs(dy) * scale)));
                } else if (dx < 0 && dy == 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] - (Math.abs(dx) * scale)), interrCenters.get("y")[i]);
                } else if (dx > 0 && dy == 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] + (Math.abs(dx) * scale)), interrCenters.get("y")[i]);
                } else if (dx == 0 && dy == 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                }

                Imgproc.arrowedLine(transparentBackground, startPoint, endPoint, new Scalar(255, 255, 255, 255), thickness, lineType, 0, tipLength);
            }
        }

        saveImage(transparentBackground, userName, stepName, imgFileSaveName);
    }

    public static Bitmap createVectorFieldBitmap(Map<String, double[][]> pivCorrelation, Map<String, double[]> interrCenters, ArrowDrawOptions arrowOptions, int rows, int cols) {
        Mat transparentBackground = new Mat(rows, cols, CV_8UC4, new Scalar(255, 255, 255, 0));

        int lineType = arrowOptions.lineType;
        int thickness = arrowOptions.thickness;
        double tipLength = arrowOptions.tipLength;
        double scale = arrowOptions.scale;

        double dx, dy;
        Point startPoint = null, endPoint = null;

        for (int i = 0; i < interrCenters.get("y").length; i++) {
            for (int j = 0; j < interrCenters.get("x").length; j++) {
                dx = pivCorrelation.get("u")[i][j];
                dy = -pivCorrelation.get("v")[i][j];

                if (dx < 0 && dy > 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] - (Math.abs(dx) * scale)),
                            (interrCenters.get("y")[i] - (Math.abs(dy) * scale)));
                } else if (dx > 0 && dy > 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] + (Math.abs(dx) * scale)),
                            (interrCenters.get("y")[i] - (Math.abs(dy) * scale)));
                } else if (dx > 0 && dy < 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] + (Math.abs(dx) * scale)),
                            (interrCenters.get("y")[i] + (Math.abs(dy) * scale)));
                } else if (dx < 0 && dy < 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] - (Math.abs(dx) * scale)),
                            (interrCenters.get("y")[i] + (Math.abs(dy) * scale)));
                } else if (dx == 0 && dy < 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point(interrCenters.get("x")[j], (interrCenters.get("y")[i] + (Math.abs(dy) * scale)));
                } else if (dx == 0 && dy > 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point(interrCenters.get("x")[j], (interrCenters.get("y")[i] - (Math.abs(dy) * scale)));
                } else if (dx < 0 && dy == 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] - (Math.abs(dx) * scale)), interrCenters.get("y")[i]);
                } else if (dx > 0 && dy == 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point((interrCenters.get("x")[j] + (Math.abs(dx) * scale)), interrCenters.get("y")[i]);
                } else if (dx == 0 && dy == 0) {
                    startPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                    endPoint = new Point(interrCenters.get("x")[j], interrCenters.get("y")[i]);
                }

                int red = (int) arrowOptions.color.red() * 255;
                int green = (int) arrowOptions.color.green() * 255;
                int blue = (int) arrowOptions.color.blue() * 255;

                Imgproc.arrowedLine(transparentBackground, startPoint, endPoint, new Scalar(red, green, blue, 255), thickness, lineType, 0, tipLength);
            }
        }

        Mat resized = resizeMat(transparentBackground);
        Bitmap bmp = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888);
        bmp.setHasAlpha(true);
        Utils.matToBitmap(resized, bmp, true);
        return bmp;
    }

    public static void saveImage(Mat image1, String userName, String stepName, String imgFileSaveName) {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Save_Output_" + userName);

        if (!storageDirectory.exists()) storageDirectory.mkdir();
        File pngFile = new File(storageDirectory, stepName + "_" + imgFileSaveName);
        Mat resized = resizeMat(image1);
        Imgcodecs.imwrite(pngFile.getAbsolutePath(), resized);
    }

    private static Mat resizeMat(Mat mat) {
        Mat resized = new Mat();
        Size scaleSize = new Size(2560, 1440);
        Imgproc.resize(mat, resized, scaleSize, 0, 0, INTER_CUBIC);
        return resized;
    }

    public static void saveColorMapImage(double[][] mapValues, String userName, String stepName, String imageFileSaveName) {
        Mat mapValuesMat = new Mat(mapValues.length, mapValues[0].length, CV_8UC1);
        double[] minMax = findMinMax2D(mapValues);
        List<int[]> transparentCoords = findTransparentCoords(mapValuesMat, mapValues, 120, 135, minMax[0], minMax[1]);
        Mat colorMapImage = createColorMap(mapValuesMat, transparentCoords, COLORMAP_JET);
        saveImage(colorMapImage, userName, stepName, imageFileSaveName);
    }

    public static Bitmap createColorMapBitmap(double[][] mapValues, int threshMin, int threshMax, int openCVColorMapCode) {
        Mat mapValuesMat = new Mat(mapValues.length, mapValues[0].length, CV_8UC1);
        double[] minMax = findMinMax2D(mapValues);
        List<int[]> transparentCoords = findTransparentCoords(mapValuesMat, mapValues, threshMin, threshMax, minMax[0], minMax[1]);
        Mat colorMap = createColorMap(mapValuesMat, transparentCoords, openCVColorMapCode);

        Mat resized = resizeMat(colorMap);
        Bitmap result = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888);
        result.setHasAlpha(true);
        Utils.matToBitmap(resized, result, true);
        return result;
    }

    private static List<int[]> findTransparentCoords(Mat valuesMat, double[][] values, int threshMin, int threshMax, double min, double max) {
        // Determine which values are transparent
        List<int[]> transparentCoords = new ArrayList<>();

        // Normalize mapValues to 0-255
        for (int y = 0; y < valuesMat.rows(); y++) {
            for (int x = 0; x < valuesMat.cols(); x++) {
                double val = values[y][x];
                byte byteVal = (byte) (255d * ((val - min) / (max - min)));
                int newVal = byteVal & 0xFF;

                if (newVal > threshMin && newVal < threshMax) {
                    transparentCoords.add(new int[]{y, x});
                }

                valuesMat.put(y, x, newVal);
            }
        }
        return transparentCoords;
    }

    private static Mat createColorMap(Mat valuesMat, List<int[]> transparentCoords, int openCVColorMapCode) {
        // Create colormap
        Mat colorMapImage = new Mat(valuesMat.rows(), valuesMat.cols(), valuesMat.type());
        Imgproc.applyColorMap(valuesMat, colorMapImage, openCVColorMapCode);

        // Convert to four channels (transparent channel)
        cvtColor(colorMapImage, colorMapImage, COLOR_BGR2BGRA);

        // Set our thresholded coordinates to transparent (255, 255, 255, 0)
        for (int t = 0; t < transparentCoords.size(); t++) {
            colorMapImage.put(transparentCoords.get(t)[0], transparentCoords.get(t)[1], new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0});
        }
        return colorMapImage;
    }

    private static double[] findMinMax2D(double[][] arr) {
        // Get min and max values
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int y = 0; y < arr.length; y++) {
            for (int x = 0; x < arr[0].length; x++) {
                double val = arr[y][x];
                if (val < min) {
                    min = val;
                }
                if (val > max) {
                    max = val;
                }
            }
        }
        return new double[]{min, max};
    }

    private static double findMedian(double a1, double a2, double a3, double a4, double a5, double a6, double a7, double a8) {
        double[] a = new double[8];
        a[0] = a1;
        a[1] = a2;
        a[2] = a3;
        a[3] = a4;
        a[4] = a5;
        a[5] = a6;
        a[6] = a7;
        a[7] = a8;

        int n = a.length;
        // First we sort the array
        Arrays.sort(a);

        // check for even case
        if (n % 2 != 0) {
            return a[n / 2];
        }
        return (a[(n - 1) / 2] + a[n / 2]) / 2.0;
    }

    public static Map<String, double[][]> vectorPostProcessing(
            Map<String, double[][]> pivCorrelation,
            int cols,
            int rows,
            int windowSize,
            int overlap,
            double dt,
            double mMax,
            double qMin,
            double E) {
        Map<String, Integer> fieldShape = getFieldShape(cols, rows, windowSize, overlap);

        double[][] dr1_p = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] dc1_p = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] mag_p = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];

        double sm_r = 0.0, sm_c = 0.0, rm_r = 0.0, rm_c = 0.0, sigma_s_r = 0.0, sigma_s_c = 0.0, r_r = 0.0, r_c = 0.0;

        int nr = fieldShape.get("nRows");
        int nc = fieldShape.get("nCols");

        for (int k = 1; k < nr - 1; k++) {
            for (int l = 1; l < nc - 1; l++) {

                sm_r = findMedian(pivCorrelation.get("v")[k - 1][l - 1], pivCorrelation.get("v")[k - 1][l],
                        pivCorrelation.get("v")[k - 1][l + 1], pivCorrelation.get("v")[k][l - 1],
                        pivCorrelation.get("v")[k][l + 1], pivCorrelation.get("v")[k + 1][l - 1],
                        pivCorrelation.get("v")[k + 1][l], pivCorrelation.get("v")[k + 1][l + 1]);

                sm_c = findMedian(pivCorrelation.get("u")[k - 1][l - 1], pivCorrelation.get("u")[k - 1][l],
                        pivCorrelation.get("u")[k - 1][l + 1], pivCorrelation.get("u")[k][l - 1],
                        pivCorrelation.get("u")[k][l + 1], pivCorrelation.get("u")[k + 1][l - 1],
                        pivCorrelation.get("u")[k + 1][l], pivCorrelation.get("u")[k + 1][l + 1]);

                rm_r = findMedian(Math.abs(pivCorrelation.get("v")[k - 1][l - 1] - sm_r), Math.abs(pivCorrelation.get("v")[k - 1][l] - sm_r),
                        Math.abs(pivCorrelation.get("v")[k - 1][l + 1] - sm_r), Math.abs(pivCorrelation.get("v")[k][l - 1] - sm_r),
                        Math.abs(pivCorrelation.get("v")[k][l + 1] - sm_r), Math.abs(pivCorrelation.get("v")[k + 1][l - 1] - sm_r),
                        Math.abs(pivCorrelation.get("v")[k + 1][l] - sm_r), Math.abs(pivCorrelation.get("v")[k + 1][l + 1] - sm_r));

                rm_c = findMedian(Math.abs(pivCorrelation.get("u")[k - 1][l - 1] - sm_c), Math.abs(pivCorrelation.get("u")[k - 1][l] - sm_c),
                        Math.abs(pivCorrelation.get("u")[k - 1][l + 1] - sm_c), Math.abs(pivCorrelation.get("u")[k][l - 1] - sm_c),
                        Math.abs(pivCorrelation.get("u")[k][l + 1] - sm_c), Math.abs(pivCorrelation.get("u")[k + 1][l - 1] - sm_c),
                        Math.abs(pivCorrelation.get("u")[k + 1][l] - sm_c), Math.abs(pivCorrelation.get("u")[k + 1][l + 1] - sm_c));

                //Normalization factor
                sigma_s_r = rm_r + 0.1;
                sigma_s_c = rm_c + 0.1;

                //absolute deviation of pixel displacement with respect to the media pixel displacement of the 8 nearest neighbors
                r_r = Math.abs(pivCorrelation.get("v")[k][l] - sm_r) / sigma_s_r;
                r_c = Math.abs(pivCorrelation.get("u")[k][l] - sm_c) / sigma_s_c;

                if (pivCorrelation.get("magnitude")[k][l] * dt < mMax && pivCorrelation.get("sig2Noise")[k][l] > qMin && r_r < E && r_c < E) {
                    dr1_p[k][l] = pivCorrelation.get("v")[k][l];
                    dc1_p[k][l] = pivCorrelation.get("u")[k][l];
                    mag_p[k][l] = pivCorrelation.get("magnitude")[k][l];
                } else {
                    dr1_p[k][l] = 0.0;
                    dc1_p[k][l] = 0.0;
                }
            }
        }

        Map<String, double[][]> map = new HashMap();
        map.put("u", dc1_p);
        map.put("v", dr1_p);
        map.put("magnitude", mag_p);
        map.put("sig2Noise", pivCorrelation.get("sig2Noise"));
        return map;
    }

    public static Map<String, double[][]> calculateMultipass(
            Map<String, double[][]> pivCorrelation,
            Map<String, double[]> interrCenters,
            Mat grayFrame1,
            Mat grayFrame2,
            int rows,
            int cols,
            int windowSize,
            int overlap) {
        int search_area_size = windowSize;
        //get field shape
        Map<String, Integer> fieldShape = getFieldShape(cols, rows, search_area_size, overlap);

        double[][] dr_new = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] dc_new = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];

        double[][] dr2 = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] dc2 = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];

        double[][] eps_r_new = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] eps_c_new = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];

        double[][] mag = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] sig2noise = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];


        Mat corr;
        int nr = fieldShape.get("nRows");
        int nc = fieldShape.get("nCols");

        for (int ii = 1; ii < nr - 1; ii++) {
            for (int jj = 1; jj < nc - 1; jj++) {
                Mat window_a_1 = Mat.zeros(windowSize, windowSize, CvType.CV_8U);
                Mat window_b_1 = Mat.zeros(windowSize, windowSize, CvType.CV_8U);
                //if pixel displacements from 1st pass are zero keep them as zero in 2nd phase
                if (pivCorrelation.get("v")[ii][jj] == 0 && pivCorrelation.get("u")[ii][jj] == 0) {
                    dr2[ii][jj] = 0.0;
                    dc2[ii][jj] = 0.0;
                    sig2noise[ii][jj] = 0.0;
                } else { //vectors are good
                    //subtract/add half the pixel displacement from interrogation region center
                    //to  find the center of the new interrogation region based on the direction
                    //of the pixel displacement
                    double IA1_x_int = interrCenters.get("x")[jj] - (pivCorrelation.get("u")[ii][jj] / 2);
                    double IA1_y_int = interrCenters.get("y")[ii] - (pivCorrelation.get("v")[ii][jj] / 2);

                    double IA2_x_int = interrCenters.get("x")[jj] + (pivCorrelation.get("u")[ii][jj] / 2);
                    double IA2_y_int = interrCenters.get("y")[ii] + (pivCorrelation.get("v")[ii][jj] / 2);

                    //Interrogation window for Image 1
                    int IA1_x_s = (int) Math.round((IA1_x_int - (windowSize / 2) + 1));
                    int IA1_x_e = Math.round(IA1_x_s + windowSize - 1);

                    int IA1_y_s = (int) Math.round((IA1_y_int - (windowSize / 2) + 1));
                    int IA1_y_e = Math.round(IA1_y_s + windowSize - 1);

                    Rect rectWin_a = new Rect((IA1_x_s - 1), (IA1_y_s - 1), windowSize, windowSize);
                    Mat IA1_new_t = new Mat(grayFrame1, rectWin_a);

                    //Interrogation window for Image 2
                    int IA2_x_s = (int) Math.round((IA2_x_int - (windowSize / 2) + 1));
                    int IA2_x_e = Math.round(IA2_x_s + windowSize - 1);

                    int IA2_y_s = (int) Math.round((IA2_y_int - (windowSize / 2) + 1));
                    int IA2_y_e = Math.round(IA2_y_s + windowSize - 1);

                    Rect rectWin_b = new Rect((IA2_x_s - 1), (IA2_y_s - 1), windowSize, windowSize);
                    Mat IA2_new_t = new Mat(grayFrame2, rectWin_b);

                    double i1_avg_new = mean(IA1_new_t).val[0];
                    double i2_avg_new = mean(IA2_new_t).val[0];

                    subtract(IA1_new_t, new Scalar(i1_avg_new), window_a_1);
                    subtract(IA2_new_t, new Scalar(i2_avg_new), window_b_1);

                    corr = openCvPIV(window_a_1, window_b_1);
                    Core.MinMaxLocResult mmr = Core.minMaxLoc(corr);

                    int c = (int) mmr.maxLoc.x;
                    int r = (int) mmr.maxLoc.y;

                    try {
                        eps_r_new[ii][jj] = (Math.log(corr.get(r - 1, c)[0]) - Math.log(corr.get(r + 1, c)[0])) / (2 * (Math.log(corr.get(r - 1, c)[0]) - 2 * Math.log(corr.get(r, c)[0]) + Math.log(corr.get(r + 1, c)[0])));
                        eps_c_new[ii][jj] = (Math.log(corr.get(r, c - 1)[0]) - Math.log(corr.get(r, c + 1)[0])) / (2 * (Math.log(corr.get(r, c - 1)[0]) - 2 * Math.log(corr.get(r, c)[0]) + Math.log(corr.get(r, c + 1)[0])));

                        dr_new[ii][jj] = (windowSize - 1) - (r + eps_r_new[ii][jj]);
                        dc_new[ii][jj] = (windowSize - 1) - (c + eps_c_new[ii][jj]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Add new pixel displacement to pixel displacements from 1st pass
                    dr2[ii][jj] = pivCorrelation.get("v")[ii][jj] + dr_new[ii][jj];
                    dc2[ii][jj] = pivCorrelation.get("u")[ii][jj] + dc_new[ii][jj];

                    Map<String, Double> sig2NoiseRatio = sig2Noise_update(corr);
                    sig2noise[ii][jj] = sig2NoiseRatio.get("sig2Noise");
                    int x = 2;
                }
                mag[ii][jj] = Math.sqrt(Math.pow(dr2[ii][jj], 2) + Math.pow(dc2[ii][jj], 2));
            }
        }

        Map<String, double[][]> map = new HashMap();
        map.put("u", dc2);
        map.put("v", dr2);
        map.put("magnitude", mag);
        map.put("sig2Noise", sig2noise);
        return map;
    }

    public static Map<String, double[][]> replaceMissingVectors(
            Map<String, double[][]> pivCorrelation,
            int rows,
            int cols,
            int windowSize,
            int overlap) {
        int search_area_size = windowSize;
        //get field shape
        Map<String, Integer> fieldShape = getFieldShape(cols, rows, search_area_size, overlap);

        double[][] dr2 = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] dc2 = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];

        double[][] mag = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];
        double[][] sig2noise = new double[fieldShape.get("nRows")][fieldShape.get("nCols")];

        int nr = fieldShape.get("nRows");
        int nc = fieldShape.get("nCols");

        for (int ii = 2; ii < nr - 2; ii++) {
            for (int jj = 2; jj < nc - 2; jj++) {
                //if pixel displacements from 1st pass are zero keep them as zero in 2nd phase
                if (pivCorrelation.get("v")[ii][jj] == 0 && pivCorrelation.get("u")[ii][jj] == 0) {
                    double bu1 = pivCorrelation.get("u")[ii - 2][jj];
                    double bu2 = pivCorrelation.get("u")[ii - 1][jj];
                    double bu3 = pivCorrelation.get("u")[ii + 1][jj];
                    double bu4 = pivCorrelation.get("u")[ii + 2][jj];

                    double[] bu = {bu1, bu2, bu3, bu4};
                    double sol1 = cubicInterpolator(bu);
                    dc2[ii][jj] = sol1;

                    double bv1 = pivCorrelation.get("v")[ii][jj - 2];
                    double bv2 = pivCorrelation.get("v")[ii][jj - 1];
                    double bv3 = pivCorrelation.get("v")[ii][jj + 1];
                    double bv4 = pivCorrelation.get("v")[ii][jj + 2];

                    double[] bv = {bv1, bv2, bv3, bv4};
                    double sol2 = cubicInterpolator(bv);
                    dr2[ii][jj] = sol2;
                } else {
                    dr2[ii][jj] = pivCorrelation.get("v")[ii][jj];
                    dc2[ii][jj] = pivCorrelation.get("u")[ii][jj];
                }
                mag[ii][jj] = Math.sqrt(Math.pow(dr2[ii][jj], 2) + Math.pow(dc2[ii][jj], 2));
                sig2noise[ii][jj] = pivCorrelation.get("sig2Noise")[ii][jj];
            }
        }

        Map<String, double[][]> map = new HashMap();
        map.put("u", dc2);
        map.put("v", dr2);
        map.put("magnitude", mag);
        map.put("sig2Noise", sig2noise);
        return map;
    }

    public static double cubicInterpolator(double[] values) {
        double output = 0;
        //Specific to the equally spaced 4 point cubic coefficient matrix, may be removed with substitution described in sub-routine
        double[][] L = {{1, 0, 0, 0}, {0.421875, 1, 0, 0}, {0.015625, 0.33333333, 1, 0}, {0, 0, 0, 1}};
        double[][] U = {{64, 16, 4, 1}, {0, 2.25, 1.3125, 0.578125}, {0, 0, 0.5, 0.7916666667}, {0, 0, 0, 1}};

        //find the z values in the LU solution
        double z0 = values[0] / L[0][0];
        double z1 = (values[1] - z0 * L[1][0]) / L[1][1];
        double z2 = (values[2] - z0 * L[2][0] - z1 * L[2][1]) / L[2][2];
        double z3 = (values[3] - z0 * L[3][0] - z1 * L[3][1] - z2 * L[3][2]) / L[3][3];

        //Use the z values to solve for the coeffs vector
        double D = (z3 / U[3][3]);
        double C = ((z2 - U[2][3] * D) / U[2][2]);
        double B = (z1 - U[1][2] * C - U[1][3] * D) / U[1][1];
        double A = (z0 - U[0][1] * B - U[0][2] * C - U[0][3] * D) / U[0][0];

        //output the y value at x = 2
        output = 8 * A + 4 * B + 2 * C + D;
        return output;
    }

    public static double checkMaxDisplacement(Map<String, double[][]> pivCorrelation) {
        double maxValue = pivCorrelation.get("magnitude")[0][0];
        for (int j = 0; j < pivCorrelation.get("magnitude").length; j++) {
            for (int i = 0; i < pivCorrelation.get("magnitude")[j].length; i++) {
                if (pivCorrelation.get("magnitude")[j][i] > maxValue) {
                    maxValue = pivCorrelation.get("magnitude")[j][i];
                }
            }
        }
        return maxValue;
    }

    public static double[][] calculateVorticityMap(Map<String, double[][]> pivCorrelation, int gap) {
        double[][] u = pivCorrelation.get("u");
        double[][] v = pivCorrelation.get("v");
        int nc = u[0].length;
        int nr = u.length;

        double[][] vortMap = new double[nr][nc];

        // Don't divide by zero
        if (gap == 0) {
            return vortMap;
        }

        for (int r = 1; r < nr - 1; r++) {
            for (int c = 1; c < nc - 1; c++) {
                vortMap[r][c] = (((v[r][c + 1] - v[r][c - 1]) - (u[r + 1][c] - u[r - 1][c]))) / gap;
            }
        }

        return vortMap;
    }

    public Mat getFirstFrameGray() {
        return grayFrame1;
    }

    public Mat getSecondFrameGray() {
        return grayFrame2;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
