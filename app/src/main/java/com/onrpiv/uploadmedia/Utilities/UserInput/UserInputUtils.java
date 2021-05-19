package com.onrpiv.uploadmedia.Utilities.UserInput;

public class UserInputUtils {

    public static BoolIntStructure checkUserInputInt(String userInput)
    {
        boolean success = false;
        int integer;
        try {
            integer = Integer.parseInt(userInput);
            success = true;
        }
        catch (NumberFormatException e) {
            integer = 0;
        }
        return new BoolIntStructure(success, integer);
    }

    public static BoolDoubleStruct checkUserInputDouble(String userInput)
    {
        boolean success = false;
        double d;
        try {
            d = Double.parseDouble(userInput);
            success = true;
        }
        catch (NumberFormatException e) {
            d = 0d;
        }
        return new BoolDoubleStruct(success, d);
    }

    public static BoolIntStructure checkUserInputIntClamp(String userInput, int min, int max) {
        BoolIntStructure result = checkUserInputInt(userInput);

        int integer = Math.min(result.getInt(), max);
        integer = Math.max(integer, min);

        return new BoolIntStructure(result.getBool(), integer);
    }
}
