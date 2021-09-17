package com.onrpiv.uploadmedia.Utilities;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Size;

public class ArrowDrawOptions
{
    public int lineType = 8;
    public int thickness = 2;
    public double tipLength = 0.2d;
    public double scale;
    public int color = Color.RED;

    private final double lengthRatio = 0.0025;

    public ArrowDrawOptions()
    {
        scale = lengthRatio * getScreenSize().getWidth();
    }

    public ArrowDrawOptions(int lineType, int thickness, double tipLength, double scale)
    {
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

    private static Size getScreenSize() {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        return new Size(width, height);
    }
}
