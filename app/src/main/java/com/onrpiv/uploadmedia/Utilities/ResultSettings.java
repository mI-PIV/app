package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.widget.Button;

import com.onrpiv.uploadmedia.Utilities.ColorMap.ColorMap;

public class ResultSettings {
    public final static String
            VEC_SINGLE="singlepass",
            VEC_MULTI="multipass",
            VEC_REPLACED="replaced",
            BACKGROUND="background",
            BACKGRND_SOLID="solid",
            BACKGRND_IMG="image";

    public boolean vecFieldChanged = false;
    public boolean vortMapChanged = false;
    public boolean backgroundChanged = false;

    private boolean vecDisplay = false;
    private String vecOption = VEC_SINGLE;
    private int arrowColor = Color.RED;
    private double arrowScale = 1d;
    private boolean vortDisplay = false;
    private ColorMap vortColorMap = new ColorMap();
    private int vortTransVals_min = 120;
    private int vortTransVals_max = 135;

    private String background = BACKGROUND;
    private int backgroundColor = Color.WHITE;

    public ResultSettings(Context context, Resources res, String packageName) {
        vortColorMap = vortColorMap.getColorMap("jet", context, res, packageName);
    }

    public static int[] getColors() {
        String[] colorStrings = new String[]{
                "red", "blue", "green", "black", "white", "gray", "cyan", "magenta", "yellow",
                "lightgray", "darkgray", "grey", "lightgrey", "darkgrey", "aqua", "fuchsia",
                "lime", "maroon", "navy", "olive", "purple", "silver", "teal"
        };

        int[] colors = new int[colorStrings.length];
        for (int i = 0; i < colorStrings.length; i++) {
            colors[i] = Color.parseColor(colorStrings[i]);
        }
        return colors;
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
        return arrowColor;
    }

    public void setArrowColor(int arrowColor) {
        this.arrowColor = arrowColor;
        vecFieldChanged = true;
    }

    public double getArrowScale() {
        return arrowScale;
    }

    public void setArrowScale(double arrowScale) {
        this.arrowScale = arrowScale;
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

    public void resetBools() {
        vecFieldChanged = false;
        vortMapChanged = false;
        backgroundChanged = false;
    }
}
