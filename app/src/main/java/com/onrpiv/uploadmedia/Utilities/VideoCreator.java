package com.onrpiv.uploadmedia.Utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;

public class VideoCreator {

    public static void createAndSaveVideo(Context context, File frameDir, File outputDir,
                                          String fileName, int fps) {

        String outputFull = outputDir.getAbsolutePath() + fileName;
        String fullInputPattern = "'" + frameDir.getAbsolutePath() + "/*.jpg'";

        String[] command = {"-framerate", ""+fps, "-pattern_type", "glob", "-i", fullInputPattern,
                "-c:v", "libx264", "-pix_fmt", "yuv420p", outputFull};
        execFfmpeg(command, context);
    }

    private static void execFfmpeg(final String[] command, final Context context) {
        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Creating video and saving video to the gallery...");
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setCancelable(false);
        pDialog.show();

        FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    Toast.makeText(context, "Video created and saved", Toast.LENGTH_SHORT).show();
                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    Log.d("FFMPEG", "Video creation cancelled!");
                } else {
                    Toast.makeText(context, "Video creation FAILED", Toast.LENGTH_SHORT).show();
                    Log.e("FFMPEG", "Async video creation failed with return code = " + returnCode);
                }
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        });
    }
}
