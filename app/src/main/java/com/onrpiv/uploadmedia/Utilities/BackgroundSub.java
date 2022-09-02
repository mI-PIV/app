package com.onrpiv.uploadmedia.Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.github.chrisbanes.photoview.PhotoView;
import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;


public class BackgroundSub {
    public final static String BCKGRND_FILENAME = "BACKGROUND", SUB1_FILENAME = "BACKSUB1",
    FILE_EXTENSION = ".jpg";

    public static Mat[] doubleFrameSubtraction(Mat grayFrame1, Mat grayFrame2, ProgressUpdateInterface progress) {
        // M. Honkanen, H. Nobach; "Background extraction from double-frame PIV images"; 2005

        // subtract
        Mat diffMat = new Mat(grayFrame1.size(), CvType.CV_32S);
        Mat signedFrame1 = new Mat();
        Mat signedFrame2 = new Mat();
        grayFrame1.convertTo(signedFrame1, CvType.CV_32S);
        grayFrame2.convertTo(signedFrame2, CvType.CV_32S);
        Core.subtract(signedFrame1, signedFrame2, diffMat);

        // all positive values go to frame1 and all negative go to frame2
        Mat frame1New = Mat.zeros(diffMat.size(), grayFrame1.type());
        Mat frame2New = Mat.zeros(diffMat.size(), grayFrame2.type());

        if (null != progress) {
            progress.setProgressMax(diffMat.rows());
        }
        for (int row = 0; row < diffMat.rows(); row++) {
            for (int col = 0; col < diffMat.cols(); col++) {
                double intensity = diffMat.get(row, col)[0];
                if (intensity > 0) {
                    frame1New.put(row, col, intensity);
                } else if (intensity < 0) {
                    frame2New.put(row, col, -intensity);
                }
            }
            if (null != progress) {
                progress.updateProgressIteration(row);
            }
        }

        // cleanup mats created in the method
        diffMat.release();
        signedFrame1.release();
        signedFrame2.release();

        return new Mat[] {frame1New, frame2New};
    }

    public static Mat[] allFrameSubtraction(File framesDir, Mat frame1, Mat frame2, ProgressUpdateInterface progress) {
        Mat background = getBackground(framesDir, progress);
        return subtract(background, frame1, frame2);
    }

    public static void saveBackground(File framesDir) {
        Mat background = getBackground(framesDir, null);
        background.release();
    }

    public static AlertDialog.Builder showLatestBackground(Context context, String userName,
                                                           String frameDirName) {
        File framesNameDir = PathUtil.getFramesNamedDirectory(context, userName, frameDirName);
        Bitmap background = BitmapFactory.decodeFile(
                new File(framesNameDir, BCKGRND_FILENAME+FILE_EXTENSION).getAbsolutePath());
        Bitmap resizedBackground = PivFunctions.resizeBitmap(background, 600);
        PhotoView backgroundImage = new PhotoView(context);
        backgroundImage.setImageBitmap(resizedBackground);
        return new AlertDialog.Builder(context)
                .setView(backgroundImage);
    }

    private static Mat getBackground(File framesDir, ProgressUpdateInterface progress) {
        File background = new File(framesDir, BCKGRND_FILENAME+FILE_EXTENSION);
        if (background.exists()){
            return Imgcodecs.imread(background.getAbsolutePath());
        } else {
            File[] frames = framesDir.listFiles();
            Arrays.sort(Objects.requireNonNull(frames));
            Mat backgroundMat = calculateBackground(frames, framesDir, progress);
            Imgcodecs.imwrite(background.getAbsolutePath(), backgroundMat);
            return backgroundMat;
        }
    }

    private static Mat calculateBackground(File[] frames, File framesDir, ProgressUpdateInterface progress) {
        OpenCVLoader.initDebug();
        BackgroundSubtractor backSub = Video.createBackgroundSubtractorMOG2();
        if (null != progress) {
            progress.setProgressMax(frames.length);
        }
        int i = 1;
        for (File frame : frames) {
            // read frame
            Mat frameMat = Imgcodecs.imread(frame.getAbsolutePath());

            // apply the frame to the background subtractor
            Mat fg = new Mat();
            backSub.apply(frameMat, fg);

            // update progress
            if (null != progress) {
                progress.updateProgressIteration(i++);
            }

            // release the mats
            fg.release();
            frameMat.release();
        }
        // get the background model
        Mat background = new Mat();
        backSub.getBackgroundImage(background);
        Imgcodecs.imwrite(new File(framesDir, BCKGRND_FILENAME+FILE_EXTENSION).getAbsolutePath(), background);
        return background;
    }

    private static Mat[] subtract(Mat background, Mat frame1, Mat frame2) {
        // subtract frames from background
        Mat diff1 = new Mat();
        Mat diff2 = new Mat();
        Core.subtract(frame1, background, diff1);
        Core.subtract(frame2, background, diff2);

        // convert to grayscale
        Mat grayDiff1 = new Mat(), grayDiff2 = new Mat();
        Imgproc.cvtColor(diff1, grayDiff1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(diff2, grayDiff2, Imgproc.COLOR_BGR2GRAY);

        // normalize grayscale frames
        Core.normalize(grayDiff1, grayDiff1, 0d, 255d, Core.NORM_MINMAX);
        Core.normalize(grayDiff2, grayDiff2, 0d, 255d, Core.NORM_MINMAX);

        // store result
        Mat[] backgroundSubtractedFrames = new Mat[]{grayDiff1, grayDiff2};

        // release mats
        diff1.release();
        diff2.release();

        return backgroundSubtractedFrames;
    }
}
