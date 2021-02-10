package com.onrpiv.uploadmedia.Utilities;

public class ArrowDrawOptions
{
    public int lineType = 8;
    public int thickness = 2;
    public double tipLength = 0.2d;

    public ArrowDrawOptions()
    {
        //EMPTY
    }

    public ArrowDrawOptions(int lineType, int thickness, double tipLength)
    {
        this.lineType = lineType;
        this.thickness = thickness;
        this.tipLength = tipLength;
    }

}
