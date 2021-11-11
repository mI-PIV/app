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
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

import java.io.File;
import java.util.Arrays;


public class BackgroundSub {
    public final static String BCKGRND_FILENAME = "BACKGROUND",
    SUB1_FILENAME = "BACKSUB1", SUB2_FILENAME = "BACKSUB2",
    FILE_EXTENSION = ".jpg";

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

    public static Mat[] allFrameSubtraction(File framesDir, int frame1Index, int frame2Index) {
        File[] frames = getFramesPaths(framesDir);
        Mat background = calculateBackground(frames);
        return subtract(frames, background, frame1Index, frame2Index);
    }

    public static void saveBackground(File framesDir) {
        File[] frames = getFramesPaths(framesDir);
        Mat background = calculateBackground(frames);
        Imgcodecs.imwrite(new File(framesDir, BCKGRND_FILENAME+FILE_EXTENSION).getAbsolutePath(), background);
        background.release();
    }

    public static AlertDialog.Builder showLatestBackground(Context context, String userName) {
        int totalFrameDirs = (PersistedData.getTotalFrameDirectories(context, userName));
        File framesNumDir = PathUtil.getFramesNumberedDirectory(context,
                userName, totalFrameDirs);
        Bitmap background = BitmapFactory.decodeFile(
                new File(framesNumDir, BCKGRND_FILENAME+FILE_EXTENSION).getAbsolutePath());
        Bitmap resizedBackground = PivFunctions.resizeBitmap(background, 600);
        PhotoView backgroundImage = new PhotoView(context);
        backgroundImage.setImageBitmap(resizedBackground);
        return new AlertDialog.Builder(context)
                .setView(backgroundImage)
                .setCancelable(false)
                .setTitle("Video Extracted Background")
                .setPositiveButton("Okay", null);
    }

    private static File[] getFramesPaths(File framesDir) {
        File[] frames = framesDir.listFiles();
        Arrays.sort(frames);
        return frames;
    }

    private static Mat calculateBackground(File[] frames) {
        OpenCVLoader.initDebug();
        BackgroundSubtractor backSub = Video.createBackgroundSubtractorMOG2();
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
        return background;
    }

    private static Mat[] subtract(File[] frames, Mat background, int frame1Index, int frame2Index) {
        // zero index fix
        frame1Index--;
        frame2Index--;

        // read in frames
        Mat frame1Mat = Imgcodecs.imread(frames[frame1Index].getAbsolutePath());
        Mat frame2Mat = Imgcodecs.imread(frames[frame2Index].getAbsolutePath());

        // subtract frames from background
        Mat diff1 = new Mat();
        Mat diff2 = new Mat();
        Core.subtract(frame1Mat, background, diff1);
        Core.subtract(frame2Mat, background, diff2);

        // convert to grayscale
        Mat grayDiff1 = new Mat(), grayDiff2 = new Mat();
        Imgproc.cvtColor(diff1, grayDiff1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(diff2, grayDiff2, Imgproc.COLOR_BGR2GRAY);

        // store result
        Mat[] backgroundSubtractedFrames = new Mat[]{grayDiff1, grayDiff2};

        // release mats
        frame1Mat.release();
        frame2Mat.release();
        diff1.release();
        diff2.release();

        return backgroundSubtractedFrames;
    }
}
