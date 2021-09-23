package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

import java.io.File;
import java.util.Arrays;


public class BackgroundSub {
    private final BackgroundSubtractor backSub;
    private final Mat fgMask;

    public BackgroundSub() {
        OpenCVLoader.initDebug();
        backSub = Video.createBackgroundSubtractorMOG2();
        fgMask = new Mat();
    }

    public void applyFrame(Mat frame) {
        backSub.apply(frame, fgMask);
    }

    public Mat getFgMask() {
        return fgMask;
    }

    public Mat getBackground() {
        Mat background = new Mat();
        backSub.getBackgroundImage(background);
        return background;
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
