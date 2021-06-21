package com.onrpiv.uploadmedia.Utilities.IO;

import android.content.Context;
import android.util.Log;

import androidx.collection.ArrayMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class FileWriter extends FileIO {
    public static <K, V> void writeDictionary(Context context, String userName, int experimentNumber,
                                              String fileName, ArrayMap<K, V> dictionary) {
        File outputFile = getFile(context, userName, experimentNumber, fileName);

        try {
            BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(outputFile));

            for (K key: dictionary.keySet()) {

                V value = dictionary.get(key);
                if (null == value) continue;

                // write
                bw.write(key.toString() +":"+ value.toString());
                bw.newLine();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Log.e("FILE_WRITER", e.toString());
        }
    }

    // TODO piv result data static method writer (transfer functions from pivfunctions)

}
