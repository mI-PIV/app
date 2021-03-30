package com.onrpiv.uploadmedia.Utilities;

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

    public static BoolIntStructure checkUserInputIntClamp(String userInput, int min, int max) {
        BoolIntStructure result = checkUserInputInt(userInput);

        int integer = Math.min(result.getInt(), max);
        integer = Math.max(integer, min);

        return new BoolIntStructure(result.getBool(), integer);
    }
}
