package com.onrpiv.uploadmedia.pivFunctions;

import android.util.ArrayMap;

public class PivParameters {
    private int windowSize = 64, overlap = 32;
    private double nMaxUpper, nMaxLower, maxDisplacement = 0.0, qMin = 1.3, dt = 1.0, E = 2.0;
    private boolean replace = true;

    // Use the map/dictionary for quickly moving values between Activities; if needed
    public final ArrayMap<String, String> parameterDictionary;

    public final static String WINDOW_SIZE_KEY = "windowSize",
            OVERLAP_KEY = "overlap",
            NUM_MAX_UPPER_KEY = "nMaxUpper",
            NUM_MAX_LOWER_KEY = "nMaxLower",
            MAX_DISPLACEMENT_KEY = "maxDisplacement",
            QMIN_KEY = "qMin",
            DT_KEY = "dt",
            E_KEY = "E",
            REPLACE_KEY = "replace";


    public PivParameters()
    {
        parameterDictionary = new ArrayMap<>();
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
        this.parameterDictionary = parameterDictionary;

        // Unload the dictionary
        for (String key: parameterDictionary.keySet())
        {
            String value = parameterDictionary.get(key);
            if (null == value) continue;

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
    }

    public int getOverlap() {
        return overlap;
    }

    public void setOverlap(int overlap) {
        parameterDictionary.put(OVERLAP_KEY, Integer.toString(overlap));
        this.overlap = overlap;
    }

    public double getnMaxUpper() {
        return nMaxUpper;
    }

    public void setnMaxUpper(double nMaxUpper) {
        parameterDictionary.put(NUM_MAX_UPPER_KEY, Double.toString(nMaxUpper));
        this.nMaxUpper = nMaxUpper;
    }

    public double getnMaxLower() {
        return nMaxLower;
    }

    public void setnMaxLower(double nMaxLower) {
        parameterDictionary.put(NUM_MAX_LOWER_KEY, Double.toString(nMaxLower));
        this.nMaxLower = nMaxLower;
    }

    public double getMaxDisplacement() {
        return maxDisplacement;
    }

    public void setMaxDisplacement(double maxDisplacement) {
        parameterDictionary.put(MAX_DISPLACEMENT_KEY, Double.toString(maxDisplacement));
        this.maxDisplacement = maxDisplacement;
    }

    public double getqMin() {
        return qMin;
    }

    public void setqMin(double qMin) {
        parameterDictionary.put(QMIN_KEY, Double.toString(qMin));
        this.qMin = qMin;
    }

    public double getDt() {
        return dt;
    }

    public void setDt(double dt) {
        parameterDictionary.put(DT_KEY, Double.toString(dt));
        this.dt = dt;
    }

    public double getE() {
        return E;
    }

    public void setE(double e) {
        parameterDictionary.put(E_KEY, Double.toString(e));
        E = e;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        parameterDictionary.put(WINDOW_SIZE_KEY, Integer.toString(windowSize));
        this.windowSize = windowSize;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        parameterDictionary.put(REPLACE_KEY, String.valueOf(replace));
        this.replace = replace;
    }
}
