package com.onrpiv.uploadmedia.Utilities.IO;

import android.content.Context;

import com.onrpiv.uploadmedia.Utilities.PathUtil;

import java.io.File;

public class FileIO {
    protected static File getFile(Context context, String userName, int experimentNumber, String fileName) {
        File outputDir = PathUtil.getExperimentNumberedDirectory(context, userName, experimentNumber);
        return new File(outputDir, fileName+".csv");
    }
}
