package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistedData {
    final static private String
            FRAME_DIRECTORY_NUMBER = "framesDirNum",
            FPS = "fps",
            EXPERIMENT_NUMBER = "experimentNumber";

    public static String getSharedPreferencesName(String userName) {
        return "miPIV_" + userName;
    }

    public static String getFrameDirPath(Context context, String userName, String setName) {
        return getPersistedData(context, userName).getString(setName, null);
    }

    public static void setFrameDirPath(Context context, String userName, String path, String setName) {
        getPersistedData(context, userName).edit().putString(setName, path).apply();
    }

    public static int getTotalExperiments(Context context, String userName) {
        return getPersistedData(context, userName).getInt(EXPERIMENT_NUMBER, 0);
    }

    public static void setTotalExperiments(Context context, String userName, int totalExpDirs) {
        getPersistedData(context, userName).edit().putInt(EXPERIMENT_NUMBER, totalExpDirs).apply();
    }

    public static void setFrameDirFPS(Context context, String userName, String framesetName, int fps) {
        getPersistedData(context, userName).edit().putInt(FPS+framesetName, fps).apply();
    }

    public static int getFrameDirFPS(Context context, String userName, String framesetName) {
        return getPersistedData(context, userName).getInt(FPS+framesetName,20);
    }

    public static void clearUserPersistedData(Context context, String userName) {
         getPersistedData(context, userName).edit().clear().apply();
    }

    private static SharedPreferences getPersistedData(Context context, String userName) {
        return context.getSharedPreferences(getSharedPreferencesName(userName), Context.MODE_PRIVATE);
    }
}
