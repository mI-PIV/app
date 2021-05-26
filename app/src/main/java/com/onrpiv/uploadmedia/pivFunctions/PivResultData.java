package com.onrpiv.uploadmedia.pivFunctions;

public class PivResultData {
    private final String name;
    private double[][] _u;
    private double[][] _v;
    private double[][] _mag;
    private double[][] _sig2noise;
    private double[] _interrX;
    private double[] _interrY;
    private double[][] vorticityValues;
    private int rows;
    private int cols;

    // intent keys
    public static final String
            REPLACED = "replaced",
            CORRELATION = "pivCorrelation",
            INTERR_CENTERS = "interrCenters",
            VORTICITY = "vorticityValues",
            MULTI = "pivCorrelationMulti",
            REPLACE2 = "pivReplaceMissing2",
            ROWS = "rows",
            COLS = "cols",
            USERNAME = "username";

    public PivResultData(String name) {
        this.name = name;
    }

    public double[][] getMag() {
        return _mag;
    }

    public void setMag(double[][] magnitude) {
        _mag = magnitude;
    }

    public double[][] getSig2Noise() {
        return _sig2noise;
    }

    public void setSig2Noise(double[][] sig2Noise) {
        _sig2noise = sig2Noise;
    }

    public double[][] getU() {
        return _u;
    }

    public void setU(double[][] u) {
        _u = u;
    }

    public double[][] getV() {
        return _v;
    }

    public void setV(double[][] v) {
        _v = v;
    }

    public double[] getInterrX() {
        return _interrX;
    }

    public void setInterrX(double[] interrX) {
        _interrX = interrX;
    }

    public double[] getInterrY() {
        return _interrY;
    }

    public void setInterrY(double[] interrY) {
        _interrY = interrY;
    }

    public void setInterrCenters(double[][] interrCenters) {
        _interrX = interrCenters[0];
        _interrY = interrCenters[1];
    }

    public double[][] getVorticityValues() {
        return vorticityValues;
    }

    public void setVorticityValues(double[][] vorticityValues) {
        this.vorticityValues = vorticityValues;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }
}
