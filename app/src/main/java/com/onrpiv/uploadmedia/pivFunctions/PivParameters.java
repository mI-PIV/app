package com.onrpiv.uploadmedia.pivFunctions;

import androidx.collection.ArrayMap;

import java.io.Serializable;

public class PivParameters implements Serializable {
    private int windowSize = 64, overlap = 32, frameSet, frameOne, frameTwo;
    private double nMaxUpper, nMaxLower, maxDisplacement = 0.0, qMin = 1.3, dt = 1.0, E = 2.0;
    private boolean replace = true;
    private boolean backgroundSubtract = true;

    public final static String WINDOW_SIZE_KEY = "windowSize",
            OVERLAP_KEY = "overlap",
            NUM_MAX_UPPER_KEY = "nMaxUpper",
            NUM_MAX_LOWER_KEY = "nMaxLower",
            MAX_DISPLACEMENT_KEY = "maxDisplacement",
            QMIN_KEY = "qMin",
            DT_KEY = "dt",
            E_KEY = "E",
            REPLACE_KEY = "replace",
            IO_FILENAME = "PIVParameters";


    public PivParameters(int frameSet, int frameOne, int frameTwo)
    {
        this.frameSet = frameSet;
        this.frameOne = frameOne;
        this.frameTwo = frameTwo;
        setWindowSize(windowSize);
        setOverlap(overlap);
        setnMaxUpper(nMaxUpper);
        setnMaxLower(nMaxLower);
        setMaxDisplacement(maxDisplacement);
        setqMin(qMin);
        setDt(dt);
        setE(E);
    }

    public PivParameters(ArrayMap<String, String> parameterDictionary)
    {
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
                dt = Double.parseDouble(value);
                break;
            case E_KEY:
                E = Double.parseDouble(value);
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

    public boolean useBackgroundSubtraction() {
        return backgroundSubtract;
    }

    public void setBackgroundSubtract(boolean backSub) {
        backgroundSubtract = backSub;
    }

    public String prettyPrintData() {
        return "Window: " + windowSize + " Overlap: " + overlap + "\nFrames " + frameOne + " to "
                + frameTwo + " in Set " + frameSet;
    }
}
