package com.onrpiv.uploadmedia.pivFunctions;

public class PivResultData {
    private double[][] _u;
    private double[][] _v;
    private double[][] _mag;
    private double[][] _sig2noise;

//    private Map<String, double[][]> pivCorrelation;
//    private Map<String, double[]> interrCenters;
    private double[][] vorticityValues;
//    private Map<String, double[][]> pivCorrelationProcessed;
//    private Map<String, double[][]> pivReplaceMissing;
//    private Map<String, double[][]> pivCorrelationMulti;
//    private Map<String, double[][]> pivReplaceMissing2;
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

    //    public Map<String, double[][]> getPivCorrelation() {
//        return pivCorrelation;
//    }
//
//    public void setPivCorrelation(Map<String, double[][]> pivCorrelation) {
//        this.pivCorrelation = pivCorrelation;
//    }
//
//    public Map<String, double[]> getInterrCenters() {
//        return interrCenters;
//    }
//
//    public void setInterrCenters(Map<String, double[]> interrCenters) {
//        this.interrCenters = interrCenters;
//    }
//
//    public double[][] getVorticityValues() {
//        return vorticityValues;
//    }
//
//    public void setVorticityValues(double[][] vorticityValues) {
//        this.vorticityValues = vorticityValues;
//    }
//
//    public Map<String, double[][]> getPivCorrelationProcessed() {
//        return pivCorrelationProcessed;
//    }
//
//    public void setPivCorrelationProcessed(Map<String, double[][]> pivCorrelationProcessed) {
//        this.pivCorrelationProcessed = pivCorrelationProcessed;
//    }
//
//    public Map<String, double[][]> getPivReplaceMissing() {
//        return pivReplaceMissing;
//    }
//
//    public void setPivReplaceMissing(Map<String, double[][]> pivReplaceMissing) {
//        this.pivReplaceMissing = pivReplaceMissing;
//    }
//
//    public Map<String, double[][]> getPivCorrelationMulti() {
//        return pivCorrelationMulti;
//    }
//
//    public void setPivCorrelationMulti(Map<String, double[][]> pivCorrelationMulti) {
//        this.pivCorrelationMulti = pivCorrelationMulti;
//    }
//
//    public Map<String, double[][]> getPivReplaceMissing2() {
//        return pivReplaceMissing2;
//    }
//
//    public void setPivReplaceMissing2(Map<String, double[][]> pivReplaceMissing2) {
//        this.pivReplaceMissing2 = pivReplaceMissing2;
//    }

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
