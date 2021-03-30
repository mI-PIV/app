package com.onrpiv.uploadmedia.Utilities;

import android.graphics.Color;

public class ArrowDrawOptions
{
    public int lineType = 8;
    public int thickness = 2;
    public double tipLength = 0.2d;
    public double scale = 1.0d;
    public Color color = Color.valueOf(Color.WHITE);

    public ArrowDrawOptions()
    {
        //EMPTY
    }

    public ArrowDrawOptions(int lineType, int thickness, double tipLength, double scale)
    {
        this.lineType = lineType;
        this.thickness = thickness;
        this.tipLength = tipLength;
        this.scale = scale;
    }

    public ArrowDrawOptions(double scale) {
        this.scale = scale;
    }

    public ArrowDrawOptions(Color color) {
        this.color = color;
    }

    public ArrowDrawOptions(Color color, double scale) {
        this.color = color;
        this.scale = scale;
    }
}
