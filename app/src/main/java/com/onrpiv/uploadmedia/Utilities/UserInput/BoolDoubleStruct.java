package com.onrpiv.uploadmedia.Utilities.UserInput;

public class BoolDoubleStruct {
    private double myDouble;
    private boolean myBool;

    public BoolDoubleStruct(boolean b, double d) {
        myBool = b;
        myDouble = d;
    }

    public double getDouble() {
        return myDouble;
    }

    public void setDouble(double d) {
        this.myDouble = d;
    }

    public boolean getBool() {
        return myBool;
    }

    public void setBool(boolean myBool) {
        this.myBool = myBool;
    }
}
