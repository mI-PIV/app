package com.onrpiv.uploadmedia.Utilities;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;


public class FpsExtractor {

    public static String extractFps(String videoPath) {
        VideoCapture capture = new VideoCapture(videoPath);
        double fps = capture.get(Videoio.CAP_PROP_FPS);
        capture.release();
        return Integer.toString((int) Math.round(fps));
    }
}
