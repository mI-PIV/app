package com.onrpiv.uploadmedia.Utilities;

import android.content.Context;

import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileIO {

    private static File getFile(Context context, String userName, int experimentNumber, String fileName) {
        File outputDir = PathUtil.getExperimentNumberedDirectory(context, userName, experimentNumber);
        return new File(outputDir, fileName+".obj");
    }

    public static void writePIVData(HashMap<String, PivResultData> resultData, PivParameters parameters,
                                    Context context, String userName, int newExpTotal) {
        // loop through our result data hashmap and write data
        for (String key : resultData.keySet()) {
            PivResultData data = resultData.get(key);
            write(data, context, userName, newExpTotal, key);
        }

        // write our parameter file
        write(parameters, context, userName, newExpTotal, PivParameters.IO_FILENAME);
    }

    public static boolean checkParametersFile(Context context, String userName) {
        File userDir = PathUtil.getUserDirectory(context, userName);
        File paramsFile = new File(userDir, PivParameters.IO_FILENAME);
        return paramsFile.exists();
    }

    public static void writeUserParametersFile(PivParameters parameters, Context context, String userName) {
        write(parameters, context, userName, PivParameters.IO_FILENAME);
    }

    public static PivParameters readUserParametersFile(Context context, String userName) {
        File userDir = PathUtil.getUserDirectory(context, userName);
        File paramsFile = new File(userDir, PivParameters.IO_FILENAME);
        return (PivParameters) read(paramsFile);
    }

    public static List<Integer> getSavedExperimentsDict(Context context, String userName) {
        List<Integer> experimentNumbersWithData = new ArrayList<>();
        int totalExperiments = PersistedData.getTotalExperiments(context, userName);

        // search the experiments for saved objects
        for (int i = 0; i <= totalExperiments; i++) {
            File paramFile = getFile(context, userName, i, PivParameters.IO_FILENAME);
            File singleFile = getFile(context, userName, i, PivResultData.SINGLE);
            File multiFile = getFile(context, userName, i, PivResultData.MULTI);
            if (paramFile.exists() && singleFile.exists() && multiFile.exists()) {
                experimentNumbersWithData.add(i);
            }
        }
        return experimentNumbersWithData;
    }

    public static Object read(Context context, String userName, int experimentNumber, String fileName) {
        File inputFile = getFile(context, userName, experimentNumber, fileName);
        return read(inputFile);
    }

    public static Object read(File inputFile) {
        Object object = null;
        try {
            FileInputStream f = new FileInputStream(inputFile);
            ObjectInputStream o = new ObjectInputStream(f);
            object = o.readObject();
            o.close();
            f.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return object;
    }

    public static void write(Object object, Context context, String userName, int experimentNumber, String fileName) {
        File outputFile = getFile(context, userName, experimentNumber, fileName);
        write(object, outputFile);
    }

    public static void write(Object obj, Context context, String userName, String fileName) {
        File userDir = PathUtil.getUserDirectory(context, userName);
        File outputFile = new File(userDir, fileName+".obj");
        write(obj, outputFile);
    }

    public static void write(Object object, File outputFile) {
        try {
            FileOutputStream f = new FileOutputStream(outputFile);
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(object);
            o.close();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
