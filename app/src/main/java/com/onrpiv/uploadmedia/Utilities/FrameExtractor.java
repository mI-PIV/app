package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public class FrameExtractor {

    /**
     * Command for extracting images from video
     */
    public static void generateFrames(Context context, String userName, String videoPath, int framesDirNum, String fps, Callable<Void> successCallback){
        String fileExtn = ".jpg";

        double startMs= 0.0;
        double endMs= 1000.0;

        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
        String filePrefix = "EXTRACT_" + timeStamp + "_";

        File userDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/miPIV_" + userName + "/");
        File framesDirectory = new File(userDirectory, "Extracted_Frames");
        File framesOccurDir = new File(framesDirectory, Integer.toString(framesDirNum));
        if (!framesOccurDir.exists()) framesOccurDir.mkdirs();

        File jpegFile = new File(framesOccurDir, filePrefix + "%03d" + fileExtn);

        /* https://ffmpeg.org/ffmpeg.html
        ffmpeg command line options
        -y  overwrite any existing files
        -i  input video path
        -an blocks all audio streams of a file from being mapped for any output
        -r  force the frame rate of output file
        -ss where to start processing.
            The value is a time duration See more https://ffmpeg.org/ffmpeg-utils.html#Time-duration.
        -t  total duration or when to stop processing.
            The value is a time duration. See more https://ffmpeg.org/ffmpeg-utils.html#Time-duration.
         */
        String[] complexCommand = {"-y", "-i", videoPath, "-an", "-r", fps, "-ss", "" + startMs / 1000, "-t", "" + (endMs - startMs) / 1000, jpegFile.getAbsolutePath()};
        /*   Remove -r 1 if you want to extract all video frames as images from the specified time duration.*/
        execFFmpegBinary(complexCommand, context, successCallback);
    }


    /**
     * Executing ffmpeg binary
     */
    private static void execFFmpegBinary(final String[] command, final Context context, final Callable<Void> successCallback) {
        FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    Toast.makeText(context, "Frames Generation Completed", Toast.LENGTH_SHORT).show();
                    try {
                        successCallback.call();
                    } catch (Exception e) {
                        Log.e("FFMPEG", "Unable to call success callback!");
                        e.printStackTrace();
                    }
                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    Log.d("FFMPEG", "Frame extraction cancelled!");
                } else {
                    Toast.makeText(context, "Frames Generation FAILED", Toast.LENGTH_SHORT).show();
                    Log.e("FFMPEG", "Async frame extraction failed with return code = " + returnCode);
                }
            }
        });
    }
}
