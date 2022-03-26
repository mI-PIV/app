package com.onrpiv.uploadmedia.pivFunctions;

import java.io.Serializable;

public class PivResultData implements Serializable {
    private final String name;
    private double[][] _u;
    private double[][] _v;
    private double[][] _mag;
    private double[][] _u_calib;
    private double[][] _v_calib;
    private double[][] _mag_calib;
    private double[][] _sig2noise;
    private double[] _interrX;
    private double[] _interrY;
    private double[][] _vort;
    private double[][] _vort_calib;
    private final double _dt;
    private int rows;
    private int cols;
    private int stepX;
    private int stepY;
    private boolean calibrated = false;
    private boolean backgroundSubtracted = false;
    private double pixelToPhysicalRatio = 1d;
    private String physicalMetric = "cm/s";

    // intent/io keys
    public static final String
            REPLACED_BOOL = "replaced",
            SINGLE = "singlepass",
            MULTI = "multipass",
            PROCESSED = "_processed",
            REPLACE = "_replaced",
            USERNAME = "username";

    public PivResultData(String name, double[][] u, double[][] v, double[][] mag,
                         double[][] sig2noise, double[][] interrCenters, int cols, int rows,
                         double dt) {
        this.name = name;
        _dt = dt;
        _sig2noise = sig2noise;
        this.rows = rows;
        this.cols = cols;
        setInterrCenters(interrCenters);
        setU(u);
        setV(v);
        setMag(mag);
    }

    public String getName() {
        return name;
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

    public double[][] getCalibratedU() {
        return _u_calib;
    }

    public void setCalibratedU(double[][] u_calibrated) {
        _u_calib = u_calibrated;
    }

    public double[][] getCalibratedV() {
        return _v_calib;
    }

    public void setCalibratedV(double[][] v_calibrated) {
        _v_calib = v_calibrated;
    }

    public double[][] getCalibratedMag() {
        return _mag_calib;
    }

    public void setCalibratedMag(double[][] mag_calibrated) {
        _mag_calib = mag_calibrated;
    }

    public double getPixelToPhysicalRatio() {
        return pixelToPhysicalRatio;
    }

    public void setPixelToPhysicalRatio(double ratio, int fieldRows, int fieldCols) {
        pixelToPhysicalRatio = ratio;
        calculatePhysicalVectors(fieldRows, fieldCols);
    }

    public String getPhysicalMetric() {
        return physicalMetric;
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
        stepX = (int) (interrCenters[0][1] - interrCenters[0][0]);
        stepY = (int) (interrCenters[1][1] - interrCenters[1][0]);
    }

    public double[][] getVorticityValues() {
        return _vort;
    }

    public void setVorticity(double[][] vort) {
        _vort = vort;
    }

    public double[][] getCalibratedVorticity() {
        return _vort_calib;
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

    public int getStepX() {
        return stepX;
    }

    public int getStepY() {
        return stepY;
    }

    public boolean isCalibrated() {
        return calibrated;
    }

    public void setCalibrated(boolean calibrated) {
        this.calibrated = calibrated;
    }

    public boolean isBackgroundSubtracted() {
        return backgroundSubtracted;
    }

    public void setBackgroundSubtracted(boolean bool) {
        backgroundSubtracted = bool;
    }

    private void calculatePhysicalVectors(int fieldRows, int fieldCols) {
        _u_calib = new double[fieldRows][fieldCols];
        _v_calib = new double[fieldRows][fieldCols];
        _mag_calib = new double[fieldRows][fieldCols];
        _vort_calib = new double[fieldRows][fieldCols];

        for (int i = 0; i < fieldRows; i++) {
            for (int j = 0; j < fieldCols; j++) {
                _u_calib[i][j] = (_u[i][j] / pixelToPhysicalRatio) / _dt;
                _v_calib[i][j] = (_v[i][j] / pixelToPhysicalRatio) / _dt;
                _mag_calib[i][j] = (_mag[i][j] / pixelToPhysicalRatio) / _dt;
                if (null != _vort) {  // some correlations won't have vorticity values
                    _vort_calib[i][j] = (_vort[i][j] / pixelToPhysicalRatio) / _dt;
                }
            }
        }
    }

//    private void applyTimeDelta(double[][] vectorComponent) {
//        for (int i = 0; i < _interrY.length; i++) {
//            for (int j = 0; j < _interrX.length; j++) {
//                vectorComponent[i][j] *= _dt;
//            }
//        }
//    }
}
