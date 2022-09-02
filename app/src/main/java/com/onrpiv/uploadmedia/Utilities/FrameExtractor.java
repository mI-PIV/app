package com.onrpiv.uploadmedia.Utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Callable;

public class FrameExtractor {

    /**
     * Command for extracting images from video
     */
    public static void generateFrames(final Context context, final String userName, String videoPath,
                                      final String frameSetName, final String fps, float videoStart,
                                      float videoEnd, final Callable<Void> successCallback,
                                      final boolean backSub){
        // create and retrieve the new frames directory
        final File framesNameDir = PathUtil.getFramesNamedDirectory(context, userName,
                frameSetName);

        File jpegFile = new File(framesNameDir, "frame%04d.jpg");

        // Callback on frame extraction completion that checks if the directory is empty.
        final Callable<Void> thisCallback = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // If the frame dir is empty after extraction (something bad happened),
                // then we don't update the persisted data
                // and we don't call the activity's callback
                if (Objects.requireNonNull(framesNameDir.listFiles()).length < 1) return null;

                // persist fps for this frame dir
                PersistedData.setFrameDirFPS(context, userName, frameSetName, Integer.parseInt(fps));

                // persist path for this frame dir
                PersistedData.setFrameDirPath(context, userName, framesNameDir.getAbsolutePath(),
                        frameSetName);

                // background subtraction
                if (backSub)
                    BackgroundSub.saveBackground(framesNameDir);

                // call the activity's callback
                successCallback.call();
                return null;
            }
        };

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
        scale= Set the resolution of the frames. See more https://trac.ffmpeg.org/wiki/Scaling
         */
        String[] complexCommand = {"-y", "-i", videoPath, "-an", "-r", fps, "-ss", "" + videoStart, "-t", "" + (videoEnd - videoStart), jpegFile.getAbsolutePath()};
        execFFmpegBinary(complexCommand, context, videoPath, thisCallback);
    }


    public static void extractSingleFrame(Context context, String videoPath, String outPath, float videoStartTime,
                                            Callable<Void> successCallback) {
        String[] command = {"-y", "-ss", "" + videoStartTime, "-i", videoPath, "-frames:v", "1", outPath};
        execFFmpegBinary(command, context, videoPath, successCallback);
    }


    /**
     * Executing ffmpeg binary
     */
    private static void execFFmpegBinary(final String[] command, final Context context, final String videoPath, final Callable<Void> successCallback) {

        // Progress dialog
        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Extracting Frame(s)...");
        pDialog.setCancelable(false);
        pDialog.show();

        FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    Toast.makeText(context, "Frame Extraction Completed", Toast.LENGTH_SHORT).show();
                    try {
                        // If we retrieved the video from google drive, then delete the temp file we created
                        PathUtil.deleteIfTempFile(context, videoPath);
                        if (pDialog.isShowing()) { pDialog.dismiss(); }
                        successCallback.call();
                    } catch (Exception e) {
                        Log.e("FFMPEG", "Unable to call success callback!");
                        e.printStackTrace();
                    }
                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    Log.d("FFMPEG", "Frame extraction cancelled!");
                } else {
                    Toast.makeText(context, "Frame Extraction FAILED", Toast.LENGTH_SHORT).show();
                    Log.e("FFMPEG", "Async frame extraction failed with return code = " + returnCode);
                }
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        });
    }
}
