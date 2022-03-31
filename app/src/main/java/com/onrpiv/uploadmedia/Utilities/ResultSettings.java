package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import com.onrpiv.uploadmedia.Utilities.ColorMap.ColorMap;

import java.util.ArrayList;
import java.util.Arrays;

public class ResultSettings {
    public final static String
            VEC_SINGLE="singlepass",
            VEC_MULTI="multipass",
            VEC_REPLACED="replaced",
            BACKGROUND="background",
            BACKGRND_SOLID="solid",
            BACKGRND_IMG="image",
            BACKGRND_SUB="bgsub";

    public final static String
            BCKGRND_SOLID_PRETTY="Solid Color",
            BCKGRND_FRAME_PRETTY="Frame",
            BCKGRND_SUB_PRETTY="Background Subtracted";

    public boolean vecFieldChanged = false;
    public boolean vortMapChanged = false;
    public boolean backgroundChanged = false;
    public boolean selectionChanged = false;

    private boolean vecDisplay = true;
    private String vecOption = VEC_REPLACED;

    private ArrowDrawOptions arrowDrawOptions;

    private boolean calibrated = false;

    private boolean vortDisplay = false;
    private ColorMap vortColorMap = new ColorMap();
    private int vortTransVals_min = 120;
    private int vortTransVals_max = 135;

    private String background = BACKGROUND;
    private int backgroundColor = Color.WHITE;
    private int selectColor = Color.YELLOW;

    private final Context context;

    public ResultSettings(Context context) {
        this.context = context;
        vortColorMap = vortColorMap.getColorMap("redblue", context);
        arrowDrawOptions = new ArrowDrawOptions();
    }

    public static int[] getColors() {
        String[] colorStrings = new String[]{
                "red", "blue", "green", "black", "white", "gray", "cyan", "magenta", "yellow",
                "lightgray", "darkgray", "grey", "lightgrey", "darkgrey", "aqua", "fuchsia",
                "lime", "maroon", "navy", "olive", "purple", "silver", "teal", "redblue"
        };

        ArrayList<Integer> colors = new ArrayList<>();
        for (String colorString : colorStrings) {
            Integer newColor = null;
            try {
                newColor = Color.parseColor(colorString);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            if (null != newColor && !colors.contains(newColor))
                colors.add(newColor);
        }

        Integer[] colorsArray = new Integer[colors.size()];
        colorsArray = colors.toArray(colorsArray);
        return Arrays.stream(colorsArray).mapToInt(Integer::intValue).toArray();
    }

    public int getVortTransVals_min() {
        return vortTransVals_min;
    }

    public void setVortTransVals_min(float vortTransVals_min) {
        this.vortTransVals_min = (int) vortTransVals_min;
        vortMapChanged = true;
    }

    public int getVortTransVals_max() {
        return vortTransVals_max;
    }

    public void setVortTransVals_max(float vortTransVals_max) {
        this.vortTransVals_max = (int) vortTransVals_max;
        vortMapChanged = true;
    }

    public boolean getVecDisplay() {
        return vecDisplay;
    }

    public void setVecDisplay(boolean vecDisplay) {
        this.vecDisplay = vecDisplay;
        vecFieldChanged = true;
    }

    public String getVecOption() {
        return vecOption;
    }

    public void setVecOption(String vecOption) {
        this.vecOption = vecOption;
        vecFieldChanged = true;
    }

    public int getArrowColor() {
        return arrowDrawOptions.color;
    }

    public void setArrowColor(int arrowColor) {
        arrowDrawOptions.color = arrowColor;
        vecFieldChanged = true;
    }

    public double getArrowScale() {
        return arrowDrawOptions.scale;
    }

    public void setArrowScale(double arrowScale) {
        arrowDrawOptions.scale = arrowScale;
        vecFieldChanged = true;
    }

    public boolean getVortDisplay() {
        return vortDisplay;
    }

    public void setVortDisplay(boolean vortDisplay) {
        this.vortDisplay = vortDisplay;
        vortMapChanged = true;
    }

    public ColorMap getVortColorMap() {
        return vortColorMap;
    }

    public void setVortColorMap(ColorMap vortColorMap) {
        this.vortColorMap = vortColorMap;
        vortMapChanged = true;
    }

    public float[] getVortTransVals() {
        return new float[]{vortTransVals_min, vortTransVals_max};
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
        backgroundChanged = true;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        backgroundChanged = true;
    }

    public boolean getCalibrated() {
        return calibrated;
    }

    public void setCalibrated(boolean bool) {
        calibrated = bool;
    }

    public String formatInfoString_pixel(float x, float y, float u, float v, float vort) {
        return "x: " + ((int) x) + "\t\t\t" + "y: " + ((int) y) +
                "\nU: " + u + " px\t\t\t" + "V: " + v + " px" +
                "\nVorticity: " + vort;
    }

    public String formatInfoString_physical(float x, float y, float u, float v, float vort, double conversion) {
        return "conversion factor xy (px -> cm): " + conversion + "\n" +
                "x: " + ((int) x) + "\t\t\t" + "y: " + ((int) y) +
                "\nU: " + u + " cm/s\t\t\t" + "V: " + v + " cm/s" +
                "\nVorticity: " + vort;
    }

    public String debugString(int pivX, int pivY, double imgX, double imgY, float viewX, float viewY) {
        return "pivX: " + pivX + "\t\t\t" + "pivY: " + pivY + "\n" +
                "viewX: " + viewX + "\t\t\t" + "viewY: " + viewY + "\n" +
                "bmpX: " + imgX + "\t\t\t" + "bmpY: " + imgY;
    }

    public void resetBools() {
        vecFieldChanged = false;
        vortMapChanged = false;
        backgroundChanged = false;
        selectionChanged = false;
    }

    public int getSelectColor() {
        return selectColor;
    }

    public void setSelectColor(int selectColor) {
        this.selectColor = selectColor;
        selectionChanged = true;
    }

    public Bundle saveInstanceBundle(Bundle outState) {
        outState.putBoolean("vecDisplay_rs", vecDisplay);
        outState.putString("vecOption_rs", vecOption);
        outState.putInt("arrowColor_rs", arrowDrawOptions.color);
        outState.putBoolean("vortDisplay_rs", vortDisplay);
        outState.putInt("vortTransVals_min_rs", vortTransVals_min);
        outState.putInt("vortTransVals_max_rs", vortTransVals_max);
        outState.putString("background_rs", background);
        outState.putInt("backgroundColor_rs", backgroundColor);
        outState.putInt("selectColor_rs", selectColor);

        outState = vortColorMap.saveInstanceBundle(outState);

        return outState;
    }

    public Bundle saveInstanceBundle() {
        Bundle outState = new Bundle();
        return saveInstanceBundle(outState);
    }

    public ResultSettings loadInstanceBundle(Bundle inState) {
        vecDisplay = inState.getBoolean("vecDisplay_rs");
        vecOption = inState.getString("vecOption_rs");
        arrowDrawOptions = new ArrowDrawOptions(inState.getInt("arrowColor_rs"));
        vortDisplay = inState.getBoolean("vortDisplay_rs");
        vortTransVals_min = inState.getInt("vortTransVals_min_rs");
        vortTransVals_max = inState.getInt("vortTransVals_max_rs");
        background = inState.getString("background_rs");
        backgroundColor = inState.getInt("backgroundColor_rs");
        selectColor = inState.getInt("selectColor_rs");
        vortColorMap = new ColorMap().loadInstanceBundle(inState, context);
        return this;
    }
}
