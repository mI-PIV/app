package com.onrpiv.uploadmedia.pivFunctions;

import android.content.Context;

import androidx.collection.ArrayMap;

import com.google.firebase.crashlytics.CustomKeysAndValues;
import com.onrpiv.uploadmedia.Utilities.Camera.Calibration.CameraCalibrationResult;
import com.onrpiv.uploadmedia.Utilities.PersistedData;

import java.io.Serializable;

public class PivParameters implements Serializable {
    private static final long serialVersionUID = 42L;
    private int windowSize = 64, overlap = 32, frameOne, frameTwo, sampleRate;
    private String frameSetName;
    private double nMaxUpper, nMaxLower, maxDisplacement = 0.0, qMin = 1.0, dt = 1.0, E = 2.0;
    private boolean replace = true, fft = true, negativeFilter = false;
    private int backgroundSelection = -1;
    private CameraCalibrationResult cameraCalibrationResult;

    public final static int
            BACKGROUNDSUB_NONE = -1,
            BACKGROUNDSUB_TWOFRAME = 0,
            BACKGROUNDSUB_ALLFRAME = 1;

    public final static String
            WINDOW_SIZE_KEY = "windowSize",
            OVERLAP_KEY = "overlap",
            NUM_MAX_UPPER_KEY = "nMaxUpper",
            NUM_MAX_LOWER_KEY = "nMaxLower",
            MAX_DISPLACEMENT_KEY = "maxDisplacement",
            QMIN_KEY = "qMin",
            DT_KEY = "dt",
            E_KEY = "E",
            REPLACE_KEY = "replace",
            FFT_KEY = "fft",
            IO_FILENAME = "PIVParameters",
            NEG_FILTER = "negativeFilter";

    public PivParameters() {
        // testing constructor
    }

    public PivParameters(Context context, String userName, String frameSetName, int frameOne,
                         int frameTwo) {
        this.frameSetName = frameSetName;
        this.frameOne = frameOne;
        this.frameTwo = frameTwo;
        setWindowSize(windowSize);
        setOverlap(overlap);
        setnMaxUpper(nMaxUpper);
        setnMaxLower(nMaxLower);
        setMaxDisplacement(maxDisplacement);
        setqMin(qMin);
        int fps = PersistedData.getFrameDirFPS(context, userName, frameSetName);
        setDt(fps);
        setE(E);
        setFFT(fft);
    }

    public PivParameters(ArrayMap<String, String> parameterDictionary) {
        // Unload the dictionary
        for (String key: parameterDictionary.keySet())
        {
            String value = parameterDictionary.get(key);
            if (null == value) continue;
            setValue(key, value);
        }
    }

    public void setValue(String key, String value) {
        switch (key)
        {
            case WINDOW_SIZE_KEY:
                windowSize = Integer.parseInt(value);
                break;
            case OVERLAP_KEY:
                overlap = Integer.parseInt(value);
                break;
            case NUM_MAX_UPPER_KEY:
                nMaxUpper = Double.parseDouble(value);
                break;
            case NUM_MAX_LOWER_KEY:
                nMaxLower = Double.parseDouble(value);
                break;
            case MAX_DISPLACEMENT_KEY:
                maxDisplacement = Double.parseDouble(value);
                break;
            case QMIN_KEY:
                qMin = Double.parseDouble(value);
                break;
            case DT_KEY:
                // incoming 'value' is FPS
                setDt((int) Double.parseDouble(value));
                break;
            case E_KEY:
                E = Double.parseDouble(value);
                break;
            case FFT_KEY:
                fft = Boolean.parseBoolean(value);
                break;
            case REPLACE_KEY:
                replace = Boolean.parseBoolean(value);
                break;
            default:
                break;
        }
    }

    public int getOverlap() {
        return overlap;
    }

    public void setOverlap(int overlap) {
        this.overlap = overlap;
    }

    public double getnMaxUpper() {
        return nMaxUpper;
    }

    public void setnMaxUpper(double nMaxUpper) {
        this.nMaxUpper = nMaxUpper;
    }

    public double getnMaxLower() {
        return nMaxLower;
    }

    public void setnMaxLower(double nMaxLower) {
        this.nMaxLower = nMaxLower;
    }

    public double getMaxDisplacement() {
        return maxDisplacement;
    }

    public void setMaxDisplacement(double maxDisplacement) {
        this.maxDisplacement = maxDisplacement;
    }

    public double getqMin() {
        return qMin;
    }

    public void setqMin(double qMin) {
        this.qMin = qMin;
    }

    public double getDt() {
        return dt;
    }

    public void setDt(double dt) {
        this.dt = dt;
    }

    public void setDt(int fps) {
        int deltaFrameNums = frameTwo - frameOne;
        dt = (double) deltaFrameNums / (double) fps;
    }

    public int getFPS() {
        int deltaFrameNums = frameTwo - frameOne;
        return (int) Math.round(deltaFrameNums / dt);
    }

    public double getE() {
        return E;
    }

    public void setE(double e) {
        E = e;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public int getBackgroundSelection() {
        return backgroundSelection;
    }

    public void setBackgroundSelection(int selection) {
        backgroundSelection = selection;
    }

    public String getFrameSetName() {
        return frameSetName;
    }

    public int getFrame1Index(){
        return frameOne;
    }

    public int getFrame2Index(){
        return frameTwo;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public boolean isFFT() {
        return fft;
    }

    public void setFFT(boolean fft) {
        this.fft = fft;
    }

    public void setFrameOne(int frameOneIndex) {
        frameOne = frameOneIndex;
    }

    public void setFrameTwo(int frameTwoIndex) {
        frameTwo = frameTwoIndex;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setFrameSetName(String frameSetName) {
        this.frameSetName = frameSetName;
    }

    public void setNegativeFilter(boolean negativeFilter) {
        this.negativeFilter = negativeFilter;
    }

    public boolean isNegativeFilter() {
        return negativeFilter;
    }

    public CameraCalibrationResult getCameraCalibrationResult() {
        return cameraCalibrationResult;
    }

    public void setCameraCalibration(CameraCalibrationResult cameraCalibration) {
        cameraCalibrationResult = cameraCalibration;
    }

    public String prettyPrintData_small() {
        return "Window: " + windowSize + " Overlap: " + overlap + "\nFrames " + frameOne + " to "
                + frameTwo + " in Set " + frameSetName;
    }

    public String prettyPrintData_comprehensive() {
        String result = "Frame set: " + frameSetName + "\n";
        result += "Frame index start: " + frameOne + "\t";
        result += "Frame index end: " + frameTwo + "\n";
        result += "Window size: " + windowSize + "\t\t";
        result += "Overlap: " + overlap + "\n";
        // minQ thresh "1.0"
        result += "Minimum Q: " + qMin + "\t\t";
        // median thresh "2.0"
        result += "Median Threshold: " + E + "\n";
        // interpolation (replace)
        result += "Interpolation: " + replace;
        return result;
    }

    public String outputPrint() {
        String output = "Piv Parameters\n";
        output += prettyPrintData_comprehensive();
        return output;
    }

    public CustomKeysAndValues getCrashlyticsKeyPairs() {
        return new CustomKeysAndValues.Builder()
                .putInt("windowSize", windowSize)
                .putInt("overlap", overlap)
                .putInt("frame1idx", frameOne)
                .putInt("frame2idx", frameTwo)
                .putInt("background", backgroundSelection)
                .putBoolean("replace", replace)
                .putBoolean("fft", fft)
                .putBoolean("negFilter", negativeFilter)
                // Add other parameters here if needed
                .build();
    }
}
