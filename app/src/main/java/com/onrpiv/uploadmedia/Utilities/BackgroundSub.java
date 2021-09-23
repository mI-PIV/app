package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

import java.io.File;
import java.util.Arrays;


public class BackgroundSub {
    private final BackgroundSubtractor backSub;

    public BackgroundSub() {
        OpenCVLoader.initDebug();
        backSub = Video.createBackgroundSubtractorMOG2();
    }

    public void applyFrame(Mat frame) {
        Mat fg = new Mat();
        backSub.apply(frame, fg);
        fg.release();
    }

    public Mat getBackground() {
        Mat background = new Mat();
        backSub.getBackgroundImage(background);
        return background;
    }

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
        Mat frame1New = new Mat(), frame2New = new Mat();
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

    public void subtractBackground(File framesDir) {
        subtract(framesDir);
    }

    public void subtractBackground(Context context, String username) {
        int framesDirNum = PersistedData.getTotalFrameDirectories(context, username);
        subtractBackground(context, username, framesDirNum);
    }

    public void subtractBackground(Context context, String username, int framesDirNum) {
        File framesDir = PathUtil.getFramesNumberedDirectory(context, username, framesDirNum);
        subtract(framesDir);
    }

    private void subtract(File framesDir) {
        File[] frames = framesDir.listFiles();
        Arrays.sort(frames);

        for (File frame : frames) {
            Mat frameMat = Imgcodecs.imread(frame.getAbsolutePath());
            applyFrame(frameMat);
            frameMat.release();
        }

        Mat background = getBackground();

        for (File frame : frames) {
            Mat frameMat = Imgcodecs.imread(frame.getAbsolutePath());
            Mat diffMat = new Mat();
            Core.subtract(frameMat, background, diffMat);
            Imgcodecs.imwrite(frame.getAbsolutePath(), diffMat);

            frameMat.release();
            diffMat.release();
        }

        Imgcodecs.imwrite(new File(framesDir, "BACKGROUND.jpg").getAbsolutePath(), background);
        background.release();
    }
}
