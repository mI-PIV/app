package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

import java.io.File;
import java.util.Arrays;


public class BackgroundSub {
    public static Mat[] doubleFrameSubtraction(Mat frame1, Mat frame2) {
        // M. Honkanen, H. Nobach; "Background extraction from double-frame PIV images"; 2005

        // convert to gray-scale
        Mat frame1Gray = new Mat(), frame2Gray = new Mat();
        Imgproc.cvtColor(frame1, frame1Gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(frame2, frame2Gray, Imgproc.COLOR_BGR2GRAY);

        // subtract
        Mat diffMat = new Mat();
        Core.subtract(frame1Gray, frame2Gray, diffMat);

        // all positive values go to frame1 and all negative go to frame2
        Mat frame1New = Mat.zeros(diffMat.size(), CvType.CV_8S);
        Mat frame2New = Mat.zeros(diffMat.size(), CvType.CV_8S);
        for (int row = 0; row < diffMat.rows(); row++) {
            for (int col = 0; col < diffMat.cols(); col++) {
                double intensity = diffMat.get(row, col)[0];
                if (intensity > 0) {
                    frame1New.put(row, col, intensity);
                } else {
                    frame2New.put(row, col, intensity);
                }
            }
        }

        // change all the negative intensity values in frame2 to positive
        Mat frame2NewPos = new Mat();
        Core.multiply(frame2New, new Scalar(-1d), frame2NewPos);

        // cleanup mats created in the method
        frame1Gray.release();
        frame2Gray.release();
        diffMat.release();
        frame2New.release();

        return new Mat[] {frame1New, frame2NewPos};
    }

    public static void subtractBackground(Context context, String username) {
        // using this method will use the last created frames directory
        int framesDirNum = PersistedData.getTotalFrameDirectories(context, username);
        subtractBackground(context, username, framesDirNum);
    }

    public static void subtractBackground(Context context, String username, int framesDirNum) {
        File framesOrigDir = PathUtil.getFramesNumberedDirectoryOriginal(context, username, framesDirNum);
        File framesSubDir = PathUtil.getFramesNumberedDirectorySubtracted(context, username, framesDirNum);
        subtract(framesOrigDir, framesSubDir);
    }

    private static void subtract(File framesOrigDir, File framesSubDir) {
        OpenCVLoader.initDebug();
        BackgroundSubtractor backSub = Video.createBackgroundSubtractorMOG2();

        File[] frames = framesOrigDir.listFiles();
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

        for (File frame : frames) {
            // read frame
            Mat frameMat = Imgcodecs.imread(frame.getAbsolutePath());

            // subtract the background model from the frame
            Mat diffMat = new Mat();
            Core.subtract(frameMat, background, diffMat);

            // write the image to the 'sub' frame dir
            Imgcodecs.imwrite(new File(framesSubDir, frame.getName()).getAbsolutePath(), diffMat);

            // release our mats
            frameMat.release();
            diffMat.release();
        }

        // save our background model to the frames video set dir
        Imgcodecs.imwrite(new File(framesOrigDir.getParent(), "BACKGROUND.jpg").getAbsolutePath(), background);
        background.release();
    }
}
