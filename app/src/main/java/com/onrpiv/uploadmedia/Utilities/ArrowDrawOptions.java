package com.onrpiv.uploadmedia.Utilities;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Size;

public class ArrowDrawOptions
{
    public int lineType = 8;
    public int thickness = 2;
    public double tipLength = 0.2d;
    public double scale = 1d;
    public int color = Color.RED;

    private final double lengthRatio = 0.0025;

    public ArrowDrawOptions() {
        // EMPTY
    }

    public ArrowDrawOptions(int lineType, int thickness, double tipLength, double scale) {
        this.lineType = lineType;
        this.thickness = thickness;
        this.tipLength = tipLength;
        this.scale = lengthRatio * getScreenSize().getWidth() * scale;
    }

    public ArrowDrawOptions(double scale) {
        this.scale = scale;
    }

    public ArrowDrawOptions(int color) {
        this.color = color;
        scale = lengthRatio * getScreenSize().getWidth();
    }

    public ArrowDrawOptions(int color, double scale) {
        this.color = color;
        this.scale = lengthRatio * getScreenSize().getWidth() * scale;
    }

    public Bundle saveInstanceBundle(Bundle outState) {
        outState.putInt("color", color);
        outState.putInt("lineType", lineType);
        outState.putInt("thickness", thickness);
        outState.putDouble("tipLength", tipLength);
        outState.putDouble("scale", scale);
        return outState;
    }

    public ArrowDrawOptions loadInstanceBundle(Bundle inState) {
        color = inState.getInt("color");
        lineType = inState.getInt("lineType");
        thickness = inState.getInt("thickness");
        tipLength = inState.getDouble("tipLength");
        scale = inState.getDouble("scale");
        return this;
    }

    private static Size getScreenSize() {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        return new Size(width, height);
    }
}
