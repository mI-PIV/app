package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistedData {
    final static private String
            FRAME_DIRECTORY_NUMBER = "framesDirNum",
            EXPERIMENT_NUMBER = "experimentNumber";

    public static String getSharedPreferencesName(String userName) {
        return "miPIV_" + userName;
    }

    public static int getTotalFrameDirectories(Context context, String userName) {
        return getPersistedData(context, userName).getInt(FRAME_DIRECTORY_NUMBER, 0);
    }

    public static void setTotalFrameDirectories(Context context, String userName, int totalFrameDirs) {
        getPersistedData(context, userName).edit().putInt(FRAME_DIRECTORY_NUMBER, totalFrameDirs).apply();
    }

    public static int getTotalExperiments(Context context, String userName) {
        return getPersistedData(context, userName).getInt(EXPERIMENT_NUMBER, 0);
    }

    public static void setTotalExperiments(Context context, String userName, int totalExpDirs) {
        getPersistedData(context, userName).edit().putInt(EXPERIMENT_NUMBER, totalExpDirs).apply();
    }

    public static void clearUserPersistedData(Context context, String userName) {
         getPersistedData(context, userName).edit().clear().apply();
    }

    private static SharedPreferences getPersistedData(Context context, String userName) {
        return context.getSharedPreferences(getSharedPreferencesName(userName), Context.MODE_PRIVATE);
    }
}
