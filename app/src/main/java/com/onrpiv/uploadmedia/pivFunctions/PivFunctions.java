package com.onrpiv.uploadmedia.pivFunctions;

import static org.opencv.core.Core.mean;
import static org.opencv.core.Core.subtract;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2BGRA;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGRA;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.cvtColor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.onrpiv.uploadmedia.Utilities.ArrowDrawOptions;
import com.onrpiv.uploadmedia.Utilities.BackgroundSub;
import com.onrpiv.uploadmedia.Utilities.ProgressUpdateInterface;

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
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * author: sarbajit mukherjee
 * Created by sarbajit mukherjee on 09/07/2020.
 */

public class PivFunctions {
    private final Mat frame1;
    private final Mat frame2;
    private Mat grayFrame1;
    private Mat grayFrame2;
    private final int rows;
    private final int cols;
    private final int windowSize;
    private final int overlap;
    private final double _e;
    private final double qMin;
    private final double dt;
    private final int fieldRows;
    private final int fieldCols;

    private final File outputDirectory;
    private final String imageFileSaveName;
    private final String textFileSaveName;

    private boolean pivGuiUpdates = false;

    public PivFunctions(String imagePath1,
                        String imagePath2,
                        String mSig2noise_method,
                        PivParameters parameters,
                        File outputDirectory,
                        String imageFileSaveName,
                        String textFileSaveName) {

        frame1 = Imgcodecs.imread(imagePath1);
        frame2 = Imgcodecs.imread(imagePath2);

        rows = frame1.rows();
        cols = frame1.cols();

        grayFrame1 = new Mat(frame1.rows(), frame1.cols(), frame1.type());
        cvtColor(frame1, grayFrame1, COLOR_BGR2GRAY);

        grayFrame2 = new Mat(frame2.rows(), frame2.cols(), frame2.type());
        cvtColor(frame2, grayFrame2, COLOR_BGR2GRAY);

        this.outputDirectory = outputDirectory;
        this.imageFileSaveName = imageFileSaveName;
        this.textFileSaveName = textFileSaveName;

        windowSize = parameters.getWindowSize();
        overlap = parameters.getOverlap();
        _e = parameters.getE();
        qMin = parameters.getqMin();
        dt = parameters.getDt();

        int[] fieldShape = getFieldShape(cols, rows, windowSize, overlap);
        fieldCols = fieldShape[0];
        fieldRows = fieldShape[1];
    }

    public PivFunctions(Mat grayscaleFrame1,
                        Mat grayscaleFrame2,
                        String mSig2noise_method,
                        PivParameters parameters,
                        File outputDirectory,
                        String imageFileSaveName,
                        String textFileSaveName) {

        frame1 = grayscaleFrame1;
        frame2 = grayscaleFrame2;
        grayFrame1 = grayscaleFrame1;
        grayFrame2 = grayscaleFrame2;

        rows = grayFrame1.rows();
        cols = grayFrame1.cols();

        this.outputDirectory = outputDirectory;
        this.imageFileSaveName = imageFileSaveName;
        this.textFileSaveName = textFileSaveName;

        windowSize = parameters.getWindowSize();
        overlap = parameters.getOverlap();
        _e = parameters.getE();
        qMin = parameters.getqMin();
        dt = parameters.getDt();

        int[] fieldShape = getFieldShape(cols, rows, windowSize, overlap);
        fieldCols = fieldShape[0];
        fieldRows = fieldShape[1];
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        grayFrame1.release();
        grayFrame2.release();
        frame1.release();
        frame2.release();
    }

    private int[] getFieldShape(int imgCols, int imgRows, int areaSize, int overlap) {
        int nRows = ((imgRows - areaSize) / (areaSize - overlap) + 1);
        int nCols = ((imgCols - areaSize) / (areaSize - overlap) + 1);
        return new int[]{nCols, nRows};
    }

    public void framesSubtraction(int backgroundSubType, File frameDir, int frame1Idx, int frame2Idx) {
        Mat[] subtractedFrames;

        if (backgroundSubType == PivParameters.BACKGROUNDSUB_ALLFRAME) {
            subtractedFrames = BackgroundSub.allFrameSubtraction(frameDir, frame1Idx, frame2Idx);
        } else if (backgroundSubType == PivParameters.BACKGROUNDSUB_TWOFRAME) {
            subtractedFrames = BackgroundSub.doubleFrameSubtraction(grayFrame1, grayFrame2);
        } else {
            // if we reach this then something bad happened, keep original gray frames
            return;
        }

        grayFrame1.release();
        grayFrame2.release();
        grayFrame1 = null;
        grayFrame2 = null;

        grayFrame1 = subtractedFrames[0];
        grayFrame2 = subtractedFrames[1];

        // save frames for results page
        saveImage(grayFrame1, BackgroundSub.SUB1_FILENAME);
        saveImage(grayFrame2, BackgroundSub.SUB2_FILENAME);
    }

    private static Mat openCvPIV(Mat image, Mat template) {
        int P = template.rows();
        int Q = template.cols();

        int M = image.rows();
        int N = image.cols();

        Mat Xt = Mat.zeros(M + 2 * (P - 1), N + 2 * (Q - 1), CvType.CV_8U);
        Rect rect = new Rect((P - 1), (Q - 1), image.width(), image.height());
        Mat submat = Xt.submat(rect);
        image.copyTo(submat);

        Imgproc.matchTemplate(Xt, template, image, Imgproc.TM_CCORR);

        // cleanup mats
        Xt.release();
        submat.release();

        return image;
    }

    private static Mat fftPIV(Mat winA, Mat winB) {
//        https://stackoverflow.com/questions/51347829/c-cross-correlation-of-2-shifted-images-with-opencv

        // prepare Mats for fft
        int height = Core.getOptimalDFTSize(Math.max(winA.rows(), winB.rows()));
        int width = Core.getOptimalDFTSize(Math.max(winA.cols(), winB.cols()));
        Mat fft1 = new Mat();
        Mat fft2 = new Mat();
        // add padding to windows
        Core.copyMakeBorder(winA, fft1, 0, height - winA.rows(), 0, width - winA.cols(), Core.BORDER_CONSTANT, Scalar.all(0d));
        Core.copyMakeBorder(winB, fft2, 0, height - winB.rows(), 0, width - winB.cols(), Core.BORDER_CONSTANT, Scalar.all(0d));

        fft1.convertTo(fft1, CvType.CV_32F);
        fft2.convertTo(fft2, CvType.CV_32F);

        // fft
        Core.dft(fft1, fft1, 0, winA.rows());
        Core.dft(fft2, fft2, 0, winB.rows());
        // FFT(winA) * FFT(winB)
        Core.mulSpectrums(fft1, fft2, fft1, 0, true);
        // ifft
        Core.idft(fft1, fft1, Core.DFT_SCALE | Core.DFT_REAL_OUTPUT);
        // fft shift
        fftShift2D(fft1);
        fft2.release();
        return fft1;
    }

    private static void fftShift2D(Mat ifft) {
        ifft = ifft.submat(new Rect(0, 0, ifft.cols() & -2, ifft.rows() & -2));
        int cx = ifft.cols() / 2;
        int cy = ifft.rows() / 2;

        Mat q0 = new Mat(ifft, new Rect(0, 0, cx, cy));
        Mat q1 = new Mat(ifft, new Rect(cx, 0, cx, cy));
        Mat q2 = new Mat(ifft, new Rect(0, cy, cx, cy));
        Mat q3 = new Mat(ifft, new Rect(cx, cy, cx, cy));

        Mat tmp = new Mat();
        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);

        q1.copyTo(tmp);
        q2.copyTo(q1);
        tmp.copyTo(q2);
    }

    private static double sig2Noise_update(Mat corr, Core.MinMaxLocResult mmr) {
        Mat correlation = new Mat();
        corr.copyTo(correlation);

        int cols = corr.cols();
        int rows = corr.rows();

        // find first peak location and value
        int peak1_x = (int) mmr.maxLoc.x;
        int peak1_y = (int) mmr.maxLoc.y;
        double peak1_value = mmr.maxVal;

        // remove primary peak
        for (int x = peak1_x - 1; x <= peak1_x + 1; x++)
            for (int y = peak1_y - 1; y <= peak1_y + 1; y++) {
                x = Math.max(0, Math.min(cols, x));
                y = Math.max(0, Math.min(rows, y));
                correlation.put(y, x, 0d);
            }

        // find second peak value
        Core.MinMaxLocResult mmr2 = Core.minMaxLoc(correlation);
        double peak2_value = mmr2.maxVal;

        correlation.release();
        return peak1_value / peak2_value;
    }

    public PivResultData extendedSearchAreaPiv_update(String resultName, boolean fft, ProgressUpdateInterface progressUpdate) {
        double[][] dr1 = new double[fieldRows][fieldCols];
        double[][] dc1 = new double[fieldRows][fieldCols];

        double[][] mag = new double[fieldRows][fieldCols];
        double[][] sig2noise = new double[fieldRows][fieldCols];

        // Update progress bar in PIV runner
        int progressCounter = 1;
        if (null != progressUpdate) {
            progressUpdate.setProgressMax(fieldCols * fieldRows);
            pivGuiUpdates = true;
        }

        for (int i = 0; i < fieldRows; i++) {
            for (int j = 0; j < fieldCols; j++) {
                Mat window_a_1 = Mat.zeros(windowSize, windowSize, CvType.CV_8U);
                Mat window_b_1 = Mat.zeros(windowSize, windowSize, CvType.CV_8U);

                if (pivGuiUpdates)
                    progressUpdate.updateProgressIteration(progressCounter++);

//             Select first the largest window, work like usual from the top left corner the left edge goes as:
//            # e.g. 0, (search_area_size - overlap), 2*(search_area_size - overlap),....

                int i1t = i * (windowSize - overlap);

//              same for top-bottom
                int j1l = j * (windowSize - overlap);

                Rect rectWin_a = new Rect(j1l, i1t, windowSize, windowSize);
                Mat window_a = new Mat(grayFrame1, rectWin_a);

                Rect rectWin_b = new Rect(j1l, i1t, windowSize, windowSize);
                Mat window_b = new Mat(grayFrame2, rectWin_b);

                double win1_avg = mean(window_a).val[0];
                double win2_avg = mean(window_b).val[0];

                subtract(window_a, new Scalar(win1_avg), window_a_1);
                subtract(window_b, new Scalar(win2_avg), window_b_1);

                Mat corr;
                if (fft) {
                    corr = fftPIV(window_a_1, window_b_1);
                } else {
                    corr = openCvPIV(window_a_1, window_b_1);
                }
                Core.MinMaxLocResult mmr = Core.minMaxLoc(corr);

                int c = (int) mmr.maxLoc.x;
                int r = (int) mmr.maxLoc.y;

                try {
                    double bottomCenter = corr.get(r-1, c)[0];
                    double topCenter = corr.get(r+1, c)[0];
                    double center = corr.get(r, c)[0];
                    double leftCenter = corr.get(r, c-1)[0];
                    double rightCenter = corr.get(r, c+1)[0];

                    double epsr = (Math.log(bottomCenter) - Math.log(topCenter)) / (2 * (Math.log(bottomCenter) - 2 * Math.log(center) + Math.log(topCenter)));
                    double epsc = (Math.log(leftCenter) - Math.log(rightCenter)) / (2 * (Math.log(leftCenter) - 2 * Math.log(center) + Math.log(rightCenter)));

                    epsr = Double.isNaN(epsr)? 0.0 : epsr;
                    epsc = Double.isNaN(epsc)? 0.0 : epsc;

                    if (fft) {
                        dr1[i][j] = (windowSize / 2d) - (r + epsr);
                        dc1[i][j] = (windowSize / 2d) - (c + epsc);
                    } else {
                        dr1[i][j] = (windowSize - 1) - (r + epsr);
                        dc1[i][j] = (windowSize - 1) - (c + epsc);
                    }

                } catch (Exception e) {
                    dr1[i][j] = 0.0;
                    dc1[i][j] = 0.0;
                }
                mag[i][j] = Math.sqrt(Math.pow(dr1[i][j], 2) + Math.pow(dc1[i][j], 2));
                sig2noise[i][j] = sig2Noise_update(corr, mmr);

                // cleanup mats
                window_a_1.release();
                window_b_1.release();
                window_a.release();
                window_b.release();
                corr.release();
                System.gc();
            }
        }

        return new PivResultData(resultName, dc1, dr1, mag, sig2noise, getCoordinates(), cols, rows, dt);
    }

    public double[][] getCoordinates() {
        double[] x = new double[fieldCols];
        double[] y = new double[fieldRows];

        for (int i = 0; i < fieldCols; i++) {
            x[i] = i * (windowSize - overlap) + (windowSize) / 2.0;
        }

        for (int j = 0; j < fieldRows; j++) {
            y[j] = j * (windowSize - overlap) + (windowSize) / 2.0;
        }
        return new double[][] {x, y};
    }

    private void saveToFile(String data, String stepName) {
        try {
            File txtFile = new File(outputDirectory, stepName + "_" + textFileSaveName);

            FileOutputStream fileOutputStream = new FileOutputStream(txtFile, true);
            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException ex) {
            Log.d("", ex.getMessage());
        } catch (IOException ex) {
            Log.d("", ex.getMessage());
        }
    }

    public void saveVectorsValues(PivResultData pivResultData, String stepName) {
        // delete vector field file if it already exists
        File txtFile = new File(outputDirectory, stepName + "_" + textFileSaveName);
        if (txtFile.exists() && txtFile.isFile()) {
            txtFile.delete();
        }

        // Header
        saveToFile("x,y,u,v,q", stepName);

        double ux, vy, q, x, y;
        ArrayList<String> toPrint = new ArrayList<>();

        // flip the vertical positions so the origin is on the bottom left instead of top left
        for (int i = pivResultData.getInterrY().length-1; i >= 0; i--) {
            for (int j = 0; j < pivResultData.getInterrX().length; j++) {
                x = pivResultData.getInterrX()[j];
                y = pivResultData.getInterrY()[i];
                ux = pivResultData.getU()[i][j];
                vy = pivResultData.getV()[i][j];
                q = pivResultData.getSig2Noise()[i][j];

                toPrint.add(String.valueOf(x));
                toPrint.add(String.valueOf(y));
                toPrint.add(String.valueOf(ux));
                toPrint.add(String.valueOf(vy));
                toPrint.add(String.valueOf(q));

                StringJoiner sj1 = new StringJoiner(",  ");
                sj1.add(toPrint.get(0)).add(toPrint.get(1)).add(toPrint.get(2)).add(toPrint.get(3)).add(toPrint.get(4));
                saveToFile(sj1.toString(), stepName);
                toPrint.clear();
            }
        }
    }

    public void saveVectorCentimeters(PivResultData pivCorrelation, double pixelToCM, String stepName) {
        double ux, vy, q, x, y;
        ArrayList<String> toPrint = new ArrayList<>();

        File txtFile = new File(outputDirectory, stepName + "_" + textFileSaveName);
        if (txtFile.exists() && txtFile.isFile()) {
            txtFile.delete();
        }

        // Header
        saveToFile("x (cm),y (cm),u (cm/s),v (cm/s),q", stepName);

        for (int i = 0; i < pivCorrelation.getInterrY().length; i++) {
            for (int j = 0; j < pivCorrelation.getInterrX().length; j++) {
                x = pivCorrelation.getInterrX()[j] * pixelToCM;
                y = pivCorrelation.getInterrY()[i] * pixelToCM;
                ux = (pivCorrelation.getU()[i][j] * pixelToCM) / dt;
                vy = (pivCorrelation.getV()[i][j]  * pixelToCM) / dt;
                q = pivCorrelation.getSig2Noise()[i][j];

                toPrint.add(String.valueOf(x));
                toPrint.add(String.valueOf(y));
                toPrint.add(String.valueOf(ux));
                toPrint.add(String.valueOf(vy));
                toPrint.add(String.valueOf(q));

                StringJoiner sj1 = new StringJoiner(",  ");
                sj1.add(toPrint.get(0)).add(toPrint.get(1)).add(toPrint.get(2)).add(toPrint.get(3)).add(toPrint.get(4));
                saveToFile(sj1.toString(), stepName);
                toPrint.clear();
            }
        }
    }

    public void saveVorticityValues(double[][] vortMap, String stepName) {
        // delete old vortmap file if it exists
        File txtFile = new File(outputDirectory, stepName + "_" + textFileSaveName);
        if (txtFile.exists() && txtFile.isFile()) {
            txtFile.delete();
        }

        // Header
        saveToFile("x,y,vorticity", stepName);

        double v;
        ArrayList<String> toPrint = new ArrayList<>();
        for (int y = 0; y < vortMap.length; y++) {
            for (int x = 0; x < vortMap[0].length; x++) {
                v = vortMap[y][x];

                toPrint.add(String.valueOf(x));
                toPrint.add(String.valueOf(y));
                toPrint.add(String.valueOf(v));

                StringJoiner sj1 = new StringJoiner(",  ");
                sj1.add(toPrint.get(0)).add(toPrint.get(1)).add(toPrint.get(2));
                saveToFile(sj1.toString(), stepName);
                toPrint.clear();
            }
        }
    }

    public void saveBaseImage(String stepName) {
        saveImage(frame1, stepName);
    }

    public static Bitmap createLinedBitmap(List<Point> points, int rows, int cols) {
        Mat transparentBackground = new Mat(rows, cols, CV_8UC4, new Scalar(255, 255, 255, 0));

        int red = ((Color.RED >> 16) & 0xFF) * 255;
        int yred = ((Color.YELLOW >> 16) & 0xFF) * 255;
        int ygreen = ((Color.YELLOW >> 8) & 0xFF) * 255;

        Point prevP = null;
        for (Point p : points) {
            // draw point
            Point bottomLeft = new Point(p.x - 4, p.y -4);
            Point topRight = new Point(p.x + 4, p.y + 4);
            Imgproc.rectangle(transparentBackground, bottomLeft, topRight, new Scalar(red, 0, 0, 255), -1);
            if (null != prevP) {
                Imgproc.line(transparentBackground, prevP, p, new Scalar(yred, ygreen, 0, 255), 3, 8);
            }
            prevP = p;
        }

        Mat resized = resizeMat(transparentBackground);
        Bitmap bmp = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888);
        bmp.setHasAlpha(true);
        Utils.matToBitmap(resized, bmp, true);

        //clean up mats
        transparentBackground.release();
        resized.release();
        System.gc();
        return bmp;
    }

    public static Bitmap createTransparentBitmap(int rows, int cols) {
        Mat transparentBackground = new Mat(rows, cols, CV_8UC4, new Scalar(255, 255, 255, 0));
        Mat resized = resizeMat(transparentBackground);
        Bitmap bmp = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888);
        bmp.setHasAlpha(true);
        Utils.matToBitmap(resized, bmp, true);

        //clean up mats
        transparentBackground.release();
        resized.release();
        System.gc();
        return bmp;
    }

    public static Bitmap createVectorFieldBitmap(PivResultData pivResultData,
                                                 ArrowDrawOptions arrowOptions, int rows, int cols) {
        Mat transparentBackground = new Mat(rows, cols, CV_8UC4, new Scalar(255, 255, 255, 0));

        int lineType = arrowOptions.lineType;
        int thickness = arrowOptions.thickness;
        double tipLength = arrowOptions.tipLength;
        double scale = arrowOptions.scale;

        double[][] u = pivResultData.getU();
        double[][] v = pivResultData.getV();
        double[] x = pivResultData.getInterrX();
        double[] y = pivResultData.getInterrY();

        double dx, dy;
        Point startPoint = null, endPoint = null;

        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < x.length; j++) {
                dx = u[i][j];
                dy = -v[i][j];

                if (dx < 0 && dy > 0) {
                    startPoint = new Point(x[j], y[i]);
                    endPoint = new Point((x[j] - (Math.abs(dx) * scale)),
                            (y[i] - (Math.abs(dy) * scale)));
                } else if (dx > 0 && dy > 0) {
                    startPoint = new Point(x[j], y[i]);
                    endPoint = new Point((x[j] + (Math.abs(dx) * scale)),
                            (y[i] - (Math.abs(dy) * scale)));
                } else if (dx > 0 && dy < 0) {
                    startPoint = new Point(x[j], y[i]);
                    endPoint = new Point((x[j] + (Math.abs(dx) * scale)),
                            (y[i] + (Math.abs(dy) * scale)));
                } else if (dx < 0 && dy < 0) {
                    startPoint = new Point(x[j], y[i]);
                    endPoint = new Point((x[j] - (Math.abs(dx) * scale)),
                            (y[i] + (Math.abs(dy) * scale)));
                } else if (dx == 0 && dy < 0) {
                    startPoint = new Point(x[j], y[i]);
                    endPoint = new Point(x[j], (y[i] + (Math.abs(dy) * scale)));
                } else if (dx == 0 && dy > 0) {
                    startPoint = new Point(x[j], y[i]);
                    endPoint = new Point(x[j], (y[i] - (Math.abs(dy) * scale)));
                } else if (dx < 0 && dy == 0) {
                    startPoint = new Point(x[j], y[i]);
                    endPoint = new Point((x[j] - (Math.abs(dx) * scale)), y[i]);
                } else if (dx > 0 && dy == 0) {
                    startPoint = new Point(x[j], y[i]);
                    endPoint = new Point((x[j] + (Math.abs(dx) * scale)), y[i]);
                } else if (dx == 0 && dy == 0) {
                    startPoint = new Point(x[j], y[i]);
                    endPoint = new Point(x[j], y[i]);
                }

                int red = ((arrowOptions.color >> 16) & 0xFF) * 255;
                int green = ((arrowOptions.color >> 8) & 0xFF) * 255;
                int blue = ((arrowOptions.color) & 0xFF) * 255;

                Imgproc.arrowedLine(transparentBackground, startPoint, endPoint, new Scalar(red, green, blue, 255),
                        thickness, lineType, 0, tipLength);
            }
        }

        Mat resized = resizeMat(transparentBackground);
        Bitmap bmp = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888);
        bmp.setHasAlpha(true);
        Utils.matToBitmap(resized, bmp, true);

        //clean up mats
        transparentBackground.release();
        resized.release();
        System.gc();
        return bmp;
    }

    public static Bitmap createSelectionImage(double imgX, double imgY, int rows, int cols, int stepX,
                                              int stepY, int color) {

        Mat transparentBackground = new Mat(rows, cols, CV_8UC4, new Scalar(255, 255, 255, 0));

        // origin,end points (assuming x, y are center of the grid)
        Point origin = new Point(imgX-(Math.round(stepX/2f)),
                imgY-(Math.round(stepY/2f)));
        Point end = new Point(imgX+(Math.round(stepX/2f)),
                imgY+(Math.round(stepY/2f)));

        // highlight color
        int red = ((color >> 16) & 0xFF) * 255;
        int green = ((color >> 8) & 0xFF) * 255;
        int blue = ((color) & 0xFF) * 255;

        // Draw user selection
        Imgproc.rectangle(transparentBackground, origin, end, new Scalar(red, green, blue, 255), 5);

        // Mat to bitmap
        Mat resized = resizeMat(transparentBackground);
        Bitmap bmp = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888);
        bmp.setHasAlpha(true);
        Utils.matToBitmap(resized, bmp, true);

        // cleanup
        transparentBackground.release();
        resized.release();
        System.gc();
        return bmp;
    }

    public void saveImage(Mat image1, String stepName) {
        File pngFile = new File(outputDirectory, stepName + "_" + imageFileSaveName);
        Mat resized = resizeMat(image1);
        Imgcodecs.imwrite(pngFile.getAbsolutePath(), resized);

        //clean up mats
        resized.release();
        System.gc();
    }

    public static Bitmap loadAndResizeBitmap(String bmpPath, int width) {
        Bitmap orig = BitmapFactory.decodeFile(bmpPath);
        return resizeBitmap(orig, width);
    }

    public static Bitmap resizeBitmap(Bitmap orig, int width) {
        float aspectRatio = orig.getWidth() / (float) orig.getHeight();
        int height = Math.round(width / aspectRatio);

        Bitmap newBmp = Bitmap.createScaledBitmap(orig, width, height, true);
        orig.recycle();
        return newBmp;
    }

    private static Mat resizeMat(Mat mat) {
        Mat resized = new Mat();
        // TODO fix hard code
        Size scaleSize = new Size(2560, 1440);
        Imgproc.resize(mat, resized, scaleSize, 0, 0, INTER_CUBIC);
        return resized;
    }

    public static Bitmap createColorMapBitmap(double[][] mapValues, int threshMin, int threshMax,
                                              Integer openCVColorMapCode) {
        Mat mapValuesMat = new Mat(mapValues.length, mapValues[0].length, CV_8UC1);
        double[] minMax = findMinMax2D(mapValues);
        List<int[]> transparentCoords = findTransparentCoords(mapValuesMat, mapValues, threshMin,
                threshMax, minMax[0], minMax[1]);

        Mat colorMap;
        if (null != openCVColorMapCode) {
            colorMap = createOpenCVColorMap(mapValuesMat, transparentCoords, openCVColorMapCode);
        } else {
            colorMap = createRedBlueColorMap(mapValuesMat, transparentCoords);
        }

        Mat resized = resizeMat(colorMap);
        Bitmap result = Bitmap.createBitmap(resized.cols(), resized.rows(), Bitmap.Config.ARGB_8888);
        result.setHasAlpha(true);
        Utils.matToBitmap(resized, result, true);

        //clean up mats
        mapValuesMat.release();
        colorMap.release();
        resized.release();
        System.gc();

        return result;
    }

    private static List<int[]> findTransparentCoords(Mat valuesMat, double[][] values, int threshMin,
                                                     int threshMax, double min, double max) {
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

    private static Mat createOpenCVColorMap(Mat valuesMat, List<int[]> transparentCoords,
                                            Integer openCVColorMapCode) {
        // Create colormap
        Mat colorMapImage = new Mat(valuesMat.rows(), valuesMat.cols(), valuesMat.type());
        Imgproc.applyColorMap(valuesMat, colorMapImage, openCVColorMapCode);

        // Convert to four channels (transparent channel)
        cvtColor(colorMapImage, colorMapImage, COLOR_BGR2BGRA);

        // Set our thresholded coordinates to transparent (255, 255, 255, 0)
        for (int t = 0; t < transparentCoords.size(); t++) {
            colorMapImage.put(transparentCoords.get(t)[0], transparentCoords.get(t)[1],
                    new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0});
        }
        return colorMapImage;
    }

    private static Mat createRedBlueColorMap(Mat normalizedValues, List<int[]> transparentCoords) {
        // create colormap
        Mat colorMapImage = new Mat(normalizedValues.rows(), normalizedValues.cols(),
                normalizedValues.type());

        // convert to four channels
        cvtColor(colorMapImage, colorMapImage, COLOR_GRAY2BGRA);

        int midpoint = 127;
        for (int row = 0; row < normalizedValues.rows(); row++) {
            for (int col = 0; col < normalizedValues.cols(); col++) {
                double value = normalizedValues.get(row, col)[0];

                if (value < midpoint) {
                    // blue with normalized value assigned to alpha
                    colorMapImage.put(row, col, new byte[]{(byte)0, (byte)0, (byte)255, (byte)value});
                } else if (value > midpoint) {
                    // red with normalized value assigned to alpha
                    colorMapImage.put(row, col, new byte[]{(byte)255, (byte)0, (byte)0, (byte)value});
                } else {  // value == midpoint
                    // completely transparent
                    colorMapImage.put(row, col, new byte[]{(byte)0, (byte)0, (byte)0, (byte)0});
                }
            }
        }

        // change our transparent values
        for (int t = 0; t < transparentCoords.size(); t++) {
            colorMapImage.put(transparentCoords.get(t)[0], transparentCoords.get(t)[1],
                    new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0});
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

    private static double median(List<Double> medianList) {
        if (medianList.size() == 0)
            return 0d;

        Collections.sort(medianList);

        double median;
        int half = medianList.size() / 2;
        if (medianList.size() % 2 == 0) {
            median = (medianList.get(half) + medianList.get(half - 1)) / 2d;
        } else {
            median = medianList.get(half);
        }
        return median;
    }

    private static double findMedian(double[][] arr, int k, int l) {
        List<Double> medianList = new ArrayList<>();
        for (int ki = k-1; ki <= k+1; ki++) {
            for (int li = l-1; li <= l+1; li++) {
                if (ki < 0 || ki >= arr.length || li < 0 || li >= arr[0].length) {
                    continue;
                } else if (ki == k && li == l) {
                    continue;
                }
                medianList.add(arr[ki][li]);
            }
        }
        return median(medianList);
    }

    private static double findMedian(double[][] arr, int k, int l, double subValue) {
        List<Double> medianList = new ArrayList<>();
        for (int ki = k-1; ki <= k+1; ki++) {
            for (int li = l-1; li <= l+1; li++) {
                if (ki < 0 || ki >= arr.length || li < 0 || li >= arr[0].length) {
                    continue;
                } else if (ki == k && li == l) {
                    continue;
                }
                medianList.add(Math.abs(arr[ki][li]) - subValue);
            }
        }
        return median(medianList);
    }

    public PivResultData vectorPostProcessing(PivResultData passResult, boolean replacement,
                                              String resultName) {

        double[][] dr1_p = new double[fieldRows][fieldCols];
        double[][] dc1_p = new double[fieldRows][fieldCols];
        double[][] mag_p = new double[fieldRows][fieldCols];

        double[][] u = passResult.getU();
        double[][] v = passResult.getV();

        double sm_r, sm_c, rm_r, rm_c, sigma_s_r, sigma_s_c, r_r, r_c;

        for (int k = 0; k < fieldRows; k++) {
            for (int l = 0; l < fieldCols; l++) {
                // Find median values
                sm_r = findMedian(v, k, l);
                sm_c = findMedian(u, k, l);
                rm_r = findMedian(v, k, l, sm_r);
                rm_c = findMedian(u, k, l, sm_c);

                //Normalization factor
                sigma_s_r = rm_r + 0.1d;
                sigma_s_c = rm_c + 0.1d;

                //absolute deviation of pixel displacement with respect to the median pixel displacement of the 8 nearest neighbors
                r_r = Math.abs(v[k][l] - sm_r) / sigma_s_r;
                r_c = Math.abs(u[k][l] - sm_c) / sigma_s_c;

                // DONT ERASE COMMENTED LINE BELOW IN CASE WE NEED TO USE A SIMILAR LOGIC LATER
                //if (pivCorrelation.get("magnitude")[k][l] * dt < nMaxUpper && pivCorrelation.get("sig2Noise")[k][l] > qMin && r_r < _e && r_c < _e) {
                if (passResult.getSig2Noise()[k][l] > qMin && r_r < _e && r_c < _e && passResult.getMag()[k][l] < windowSize*0.5) {
                    dr1_p[k][l] = passResult.getV()[k][l];
                    dc1_p[k][l] = passResult.getU()[k][l];
                    mag_p[k][l] = passResult.getMag()[k][l];
                } else {
                    dr1_p[k][l] = 0.0d;
                    dc1_p[k][l] = 0.0d;
                }
            }
        }

        PivResultData processedResult = new PivResultData(resultName, dc1_p, dr1_p, mag_p,
                passResult.getSig2Noise(), getCoordinates(), cols, rows, dt);

        if (replacement) {
            resultName += PivResultData.REPLACE;
            processedResult = replaceMissingVectors(processedResult, resultName);
        }

        return processedResult;
    }

    public PivResultData calculateMultipass(PivResultData pivResultData, String resultName, boolean fft,
                                            ProgressUpdateInterface progressUpdate) {
        double[][] dr_new = new double[fieldRows][fieldCols];
        double[][] dc_new = new double[fieldRows][fieldCols];

        double[][] dr2 = new double[fieldRows][fieldCols];
        double[][] dc2 = new double[fieldRows][fieldCols];

        double[][] eps_r_new = new double[fieldRows][fieldCols];
        double[][] eps_c_new = new double[fieldRows][fieldCols];

        double[][] mag = new double[fieldRows][fieldCols];
        double[][] sig2noise = new double[fieldRows][fieldCols];

        double[][] u = pivResultData.getU();
        double[][] v = pivResultData.getV();
        double[] x = pivResultData.getInterrX();
        double[] y = pivResultData.getInterrY();

        // add padding to grayscale frames
        Mat paddedGray1 = new Mat();
        Mat paddedGray2 = new Mat();
        int padding = windowSize + 1;
        Core.copyMakeBorder(grayFrame1, paddedGray1, padding, padding, padding, padding, Core.BORDER_CONSTANT, Scalar.all(0d));
        Core.copyMakeBorder(grayFrame2, paddedGray2, padding, padding, padding, padding, Core.BORDER_CONSTANT, Scalar.all(0d));

        int progressCounter = 1;

        for (int ii = 0; ii < fieldRows; ii++) {
            for (int jj = 0; jj < fieldCols; jj++) {
                Mat window_a_1 = Mat.zeros(windowSize, windowSize, CvType.CV_8U);
                Mat window_b_1 = Mat.zeros(windowSize, windowSize, CvType.CV_8U);

                if (pivGuiUpdates)
                    progressUpdate.updateProgressIteration(progressCounter++);

                //if pixel displacements from 1st pass are zero keep them as zero in 2nd phase
                if (v[ii][jj] == 0d && u[ii][jj] == 0d) {
                    dr2[ii][jj] = 0.0;
                    dc2[ii][jj] = 0.0;
                    sig2noise[ii][jj] = 0.0;
                } else { //vectors are good
                    //subtract/add half the pixel displacement from interrogation region center
                    //to  find the center of the new interrogation region based on the direction
                    //of the pixel displacement

                    int ushift = (int) Math.round(u[ii][jj]/2);
                    int vshift = (int) Math.round(v[ii][jj]/2);

                    double IA1_x_int = x[jj] - (ushift);
                    double IA1_y_int = y[ii] - (vshift);

                    double IA2_x_int = x[jj] + (ushift);
                    double IA2_y_int = y[ii] + (vshift);

                    //Interrogation window for Image 1
                    int IA1_x_s = (int) Math.round((IA1_x_int - (windowSize / 2f) + padding));
                    int IA1_y_s = (int) Math.round((IA1_y_int - (windowSize / 2f) + padding));
                    Rect rectWin_a = new Rect((IA1_x_s - 1), (IA1_y_s - 1), windowSize, windowSize);

                    //Interrogation window for Image 2
                    int IA2_x_s = (int) Math.round((IA2_x_int - (windowSize / 2f) + padding));
                    int IA2_y_s = (int) Math.round((IA2_y_int - (windowSize / 2f) + padding));
                    Rect rectWin_b = new Rect((IA2_x_s - 1), (IA2_y_s - 1), windowSize, windowSize);

                    Mat IA1_new_t = new Mat(paddedGray1, rectWin_a);
                    Mat IA2_new_t = new Mat(paddedGray2, rectWin_b);

                    //Subtract the means from the windows
                    double i1_avg_new = mean(IA1_new_t).val[0];
                    double i2_avg_new = mean(IA2_new_t).val[0];
                    subtract(IA1_new_t, new Scalar(i1_avg_new), window_a_1);
                    subtract(IA2_new_t, new Scalar(i2_avg_new), window_b_1);

                    //Find the correlation
                    Mat corr;
                    if (fft) {
                        corr = fftPIV(window_a_1, window_b_1);
                    } else {
                        corr = openCvPIV(window_a_1, window_b_1);
                    }

                    Core.MinMaxLocResult mmr = Core.minMaxLoc(corr);

                    int c = (int) mmr.maxLoc.x;
                    int r = (int) mmr.maxLoc.y;

                    try {
                        double epsr_new = (Math.log(corr.get(r - 1, c)[0])
                                - Math.log(corr.get(r + 1, c)[0]))
                                / (2 * (Math.log(corr.get(r - 1, c)[0])
                                - 2 * Math.log(corr.get(r, c)[0])
                                + Math.log(corr.get(r + 1, c)[0])));

                        double epsc_new = (Math.log(corr.get(r, c - 1)[0])
                                - Math.log(corr.get(r, c + 1)[0]))
                                / (2 * (Math.log(corr.get(r, c - 1)[0])
                                - 2 * Math.log(corr.get(r, c)[0])
                                + Math.log(corr.get(r, c + 1)[0])));

                        eps_r_new[ii][jj] = Double.isNaN(epsr_new)? 0.0 : epsr_new;
                        eps_c_new[ii][jj] = Double.isNaN(epsc_new)? 0.0 : epsc_new;

                        if (fft) {
                            dr_new[ii][jj] = (windowSize / 2d) - (r + eps_r_new[ii][jj]);
                            dc_new[ii][jj] = (windowSize / 2d) - (c + eps_c_new[ii][jj]);
                        } else {
                            dr_new[ii][jj] = (windowSize - 1) - (r + eps_r_new[ii][jj]);
                            dc_new[ii][jj] = (windowSize - 1) - (c + eps_c_new[ii][jj]);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //Add new pixel displacement to pixel displacements from 1st pass
                    dc2[ii][jj] = ushift*2 + dc_new[ii][jj];
                    dr2[ii][jj] = vshift*2 + dr_new[ii][jj];

                    sig2noise[ii][jj] = sig2Noise_update(corr, mmr);

                    //cleanup mats
                    IA1_new_t.release();
                    IA2_new_t.release();
                    corr.release();
                }
                mag[ii][jj] = Math.sqrt(Math.pow(dr2[ii][jj], 2) + Math.pow(dc2[ii][jj], 2));

                //cleanup mats
                window_a_1.release();
                window_b_1.release();
                System.gc();
            }
        }
        // cleanup mats
        paddedGray1.release();
        paddedGray2.release();

        return new PivResultData(resultName, dc2, dr2, mag, sig2noise,
                getCoordinates(), cols, rows, dt);
    }

    private static double getCubicInterpolation_u(double[][] u, int ii, int jj) {
        List<Double> valueList = new ArrayList<>();
        for (int i = ii - 2; i <= ii +2; i++) {
            if (i < 0 || i >= u.length) {
                valueList.add(0d);
            } else if (i == ii) {
                continue;
            } else {
                valueList.add(u[i][jj]);
            }
        }
        return cubicInterpolator(valueList);
    }

    private static double getCubicInterpolation_v(double[][] v, int ii, int jj) {
        List<Double> valueList = new ArrayList<>();
        for (int j = jj - 2; j <= jj +2; j++) {
            if (j < 0 || j >= v.length) {
                valueList.add(0d);
            } else if (j == jj) {
                continue;
            } else {
                valueList.add(v[ii][j]);
            }
        }
        return cubicInterpolator(valueList);
    }

    public PivResultData replaceMissingVectors(PivResultData pivResultData, String resultName) {
        double[][] dr2 = new double[fieldRows][fieldCols];
        double[][] dc2 = new double[fieldRows][fieldCols];

        double[][] mag = new double[fieldRows][fieldCols];
        double[][] sig2noise = new double[fieldRows][fieldCols];

        double[][] u = pivResultData.getU();
        double[][] v = pivResultData.getV();

        for (int ii = 0; ii < fieldRows; ii++) {
            for (int jj = 0; jj < fieldCols; jj++) {
                //if pixel displacements from 1st pass are zero calculate cubic interpolation
                if (v[ii][jj] == 0d && u[ii][jj] == 0d) {
                    dc2[ii][jj] = getCubicInterpolation_u(u, ii, jj);
                    dr2[ii][jj] = getCubicInterpolation_v(v, ii, jj);
                } else {
                    dr2[ii][jj] = v[ii][jj];
                    dc2[ii][jj] = u[ii][jj];
                }
                mag[ii][jj] = Math.sqrt(Math.pow(dr2[ii][jj], 2) + Math.pow(dc2[ii][jj], 2));
                sig2noise[ii][jj] = pivResultData.getSig2Noise()[ii][jj];
            }
        }

        return new PivResultData(resultName, dc2, dr2, mag, sig2noise,
                getCoordinates(), cols, rows, dt);
    }

    public static double cubicInterpolator(List<Double> values) {
        //Specific to the equally spaced 4 point cubic coefficient matrix, may be removed with substitution described in sub-routine
        double[][] L = {{1, 0, 0, 0}, {0.421875, 1, 0, 0}, {0.015625, 0.33333333, 1, 0}, {0, 0, 0, 1}};
        double[][] U = {{64, 16, 4, 1}, {0, 2.25, 1.3125, 0.578125}, {0, 0, 0.5, 0.7916666667}, {0, 0, 0, 1}};

        //find the z values in the LU solution
        double z0 = values.get(0) / L[0][0];
        double z1 = (values.get(1) - z0 * L[1][0]) / L[1][1];
        double z2 = (values.get(2) - z0 * L[2][0] - z1 * L[2][1]) / L[2][2];
        double z3 = (values.get(3) - z0 * L[3][0] - z1 * L[3][1] - z2 * L[3][2]) / L[3][3];

        //Use the z values to solve for the coeffs vector
        double D = (z3 / U[3][3]);
        double C = ((z2 - U[2][3] * D) / U[2][2]);
        double B = (z1 - U[1][2] * C - U[1][3] * D) / U[1][1];
        double A = (z0 - U[0][1] * B - U[0][2] * C - U[0][3] * D) / U[0][0];

        //output the y value at x = 2
        return 8 * A + 4 * B + 2 * C + D;
    }

    public static double checkMaxDisplacement(double[][] magnitude) {
        double maxValue = magnitude[0][0];
        for (int j = 0; j < magnitude.length; j++) {
            for (int i = 0; i < magnitude[j].length; i++) {
                if (magnitude[j][i] > maxValue) {
                    maxValue = magnitude[j][i];
                }
            }
        }
        return maxValue;
    }

    public static void calculateVorticityMap(PivResultData pivCorrelation) {
        double[][] u = pivCorrelation.getU();
        double[][] v = pivCorrelation.getV();
        int nc = u[0].length;
        int nr = u.length;
        int gap = (int) (pivCorrelation.getInterrX()[1] - pivCorrelation.getInterrX()[0]);

        // Don't divide by zero
        if (gap == 0) { return; }

        double[][] vortMap = new double[nr][nc];
        for (int r = 1; r < nr - 1; r++) {
            for (int c = 1; c < nc - 1; c++) {
                vortMap[r][c] = (((v[r][c + 1] - v[r][c - 1]) - (u[r + 1][c] - u[r - 1][c]))) / gap;
            }
        }
        pivCorrelation.setVorticity(vortMap);
    }

    public int getFieldRows() {
        return fieldRows;
    }

    public int getFieldCols() {
        return fieldCols;
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private static double[][] debugMat(Mat toDebug) {
        double[][] result = new double[toDebug.rows()][toDebug.cols()];
        for (int row = 0; row < toDebug.rows(); row++) {
            for (int col = 0; col < toDebug.cols(); col++) {
                result[row][col] = toDebug.get(row, col)[0];
            }
        }
        return result;
    }
}
