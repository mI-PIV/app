package com.onrpiv.uploadmedia.Utilities;


import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;

import java.io.File;

public class VideoCreator {

    public static boolean createAndSaveVideo(File framesDir, String output) {
        OpenCVLoader.initDebug();

        File[] frames = framesDir.listFiles();
        if (null == frames) { return false; }

        Mat sizeMat = Imgcodecs.imread(frames[0].getAbsolutePath());
        Size frameSize = sizeMat.size();

        VideoWriter writer = new VideoWriter();
        writer.open(output, VideoWriter.fourcc('M', 'J', 'P', 'G'), 2.0d, frameSize, true);
        sizeMat.release();

        if (!writer.isOpened()) { return false; }

        for (int i = 0; i < frames.length; i++) {
            Mat frame = Imgcodecs.imread(frames[i].getAbsolutePath());
            writer.write(frame);
            frame.release();
        }
        writer.release();
        return true;
    }
}
