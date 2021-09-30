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
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

import java.io.File;
import java.util.Arrays;


public class BackgroundSub {
    public static Mat[] doubleFrameSubtraction(Mat grayFrame1, Mat grayFrame2) {
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
        for (int row = 0; row < diffMat.rows(); row++) {
            for (int col = 0; col < diffMat.cols(); col++) {
                double intensity = diffMat.get(row, col)[0];
                if (intensity > 0) {
                    frame1New.put(row, col, intensity);
                } else if (intensity < 0) {
                    frame2New.put(row, col, intensity * -1d);
                }
            }
        }

        // cleanup mats created in the method
        diffMat.release();
        signedFrame1.release();
        signedFrame2.release();

        return new Mat[] {frame1New, frame2New};
    }

    public static void subtractBackground(File framesDir, boolean saveFrames) {
        subtract(framesDir, saveFrames);
    }

    public static void subtractBackground(Context context, String username, boolean saveFrames) {
        // using this method will use the last created frames directory
        int framesDirNum = PersistedData.getTotalFrameDirectories(context, username);
        subtractBackground(context, username, framesDirNum, saveFrames);
    }

    public static void subtractBackground(Context context, String username, int framesDirNum,
                                          boolean saveFrames) {
        File framesDir = PathUtil.getFramesNumberedDirectory(context, username, framesDirNum);
        subtract(framesDir, saveFrames);
    }

    public static void showLatestBackground(Context context, String userName) {
        int totalFrameDirs = (PersistedData.getTotalFrameDirectories(context, userName));
        File framesNumDir = PathUtil.getFramesNumberedDirectory(context,
                userName, totalFrameDirs);
        Bitmap background = BitmapFactory.decodeFile(
                new File(framesNumDir, "BACKGROUND.jpg").getAbsolutePath());
        Bitmap resizedBackground = PivFunctions.resizeBitmap(background, 600);
        PhotoView backgroundImage = new PhotoView(context);
        backgroundImage.setImageBitmap(resizedBackground);
        AlertDialog popup = new AlertDialog.Builder(context)
                .setView(backgroundImage)
                .setCancelable(false)
                .setTitle("Video Background")
                .setPositiveButton("Okay", null)
                .show();
    }

    private static void subtract(File framesDir, boolean saveFrames) {
        OpenCVLoader.initDebug();
        BackgroundSubtractor backSub = Video.createBackgroundSubtractorMOG2();

        File[] frames = framesDir.listFiles();
        Arrays.sort(frames);

        for (File frame : frames) {
            // read frame
            Mat frameMat = Imgcodecs.imread(frame.getAbsolutePath());

            // apply the frame to the background subtractor
            Mat fg = new Mat();
            backSub.apply(frameMat, fg);

            // release the mats
            fg.release();
            frameMat.release();
        }

        // get the background model
        Mat background = new Mat();
        backSub.getBackgroundImage(background);

        if (saveFrames) {
            for (File frame : frames) {
                // read frame
                Mat frameMat = Imgcodecs.imread(frame.getAbsolutePath());

                // subtract the background model from the frame
                Mat diffMat = new Mat();
                Core.subtract(frameMat, background, diffMat);

                // write the image over the original extracted frame
                Imgcodecs.imwrite(frame.getAbsolutePath(), diffMat);

                // release our mats
                frameMat.release();
                diffMat.release();
            }
        }

        // save our background model to the frames video set dir
        Imgcodecs.imwrite(new File(framesDir, "BACKGROUND.jpg").getAbsolutePath(), background);
        background.release();
    }
}
