package com.onrpiv.uploadmedia.Utilities.UserInput;


public class BoolIntStructure {
    private int myInt;
    private boolean myBool;

    public BoolIntStructure(boolean b, int i) {
        myBool = b;
        myInt = i;
    }

    public int getInt() {
        return myInt;
    }

    public void setInt(int i) {
        this.myInt = i;
    }

    public boolean getBool() {
        return myBool;
    }

    public void setBool(boolean myBool) {
        this.myBool = myBool;
    }
}
