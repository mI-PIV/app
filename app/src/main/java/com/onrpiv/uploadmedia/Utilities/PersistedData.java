package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistedData {
    final static private String
            FRAME_DIRECTORY_NUMBER = "framesDirNum",
            FPS = "fps",
            EXPERIMENT_NUMBER = "experimentNumber",
            CALIBRATION_NUMBER = "calibrationNumber";

    public static String getSharedPreferencesName(String userName) {
        return "miPIV_" + userName;
    }

    public static int getTotalFrameDirectories(Context context, String userName) {
        return getPersistedData(context, userName).getInt(FRAME_DIRECTORY_NUMBER, 0);
    }

    public static void setTotalFrameDirectories(Context context, String userName, int totalFrameDirs) {
        getPersistedData(context, userName).edit().putInt(FRAME_DIRECTORY_NUMBER, totalFrameDirs).apply();
    }

    public static int getFrameDirPath(Context context, String userName, String path) {
        return getPersistedData(context, userName).getInt(path, 0);
    }

    public static void setFrameDirPath(Context context, String userName, String path, int dirNum) {
        getPersistedData(context, userName).edit().putInt(path, dirNum).apply();
    }

    public static int getTotalExperiments(Context context, String userName) {
        return getPersistedData(context, userName).getInt(EXPERIMENT_NUMBER, 0);
    }

    public static void setTotalExperiments(Context context, String userName, int totalExpDirs) {
        getPersistedData(context, userName).edit().putInt(EXPERIMENT_NUMBER, totalExpDirs).apply();
    }

//    public static int getTotalCalibrations(Context context, String userName) {
//        return getPersistedData(context, userName).getInt(CALIBRATION_NUMBER, 0);
//    }
//
//    public static void setTotalCalibrations(Context context, String userName, int totalCalibrations) {
//        getPersistedData(context, userName).edit().putInt(CALIBRATION_NUMBER, totalCalibrations).apply();
//    }

    public static void setFrameDirFPS(Context context, String userName, int frameDir, int fps) {
        getPersistedData(context, userName).edit().putInt(FPS+frameDir, fps).apply();
    }

    public static int getFrameDirFPS(Context context, String userName, int frameDir) {
        return getPersistedData(context, userName).getInt(FPS+frameDir,20);
    }

    public static void clearUserPersistedData(Context context, String userName) {
         getPersistedData(context, userName).edit().clear().apply();
    }

    private static SharedPreferences getPersistedData(Context context, String userName) {
        return context.getSharedPreferences(getSharedPreferencesName(userName), Context.MODE_PRIVATE);
    }
}
