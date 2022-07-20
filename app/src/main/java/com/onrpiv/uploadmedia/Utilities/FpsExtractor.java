package com.onrpiv.uploadmedia.Utilities;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;


public class FpsExtractor {

    public static String extractFps(String videoPath) {
        int fps = 30; // default

        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(videoPath);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/") && format.containsKey(MediaFormat.KEY_CAPTURE_RATE)) {
                    fps = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            extractor.release();
        }

        return Integer.toString(fps);
    }
}
