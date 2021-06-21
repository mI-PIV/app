package com.onrpiv.uploadmedia.Utilities.IO;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class FileReader extends FileIO {
    // TODO piv parameter reader static methods
    public static <K, V> ArrayMap<K, V> readDictionary(Context context, String userName,
                                                       int experimentNumber, String fileName) {
        File inputFile = getFile(context, userName, experimentNumber, fileName);
        BufferedReader br = null;
        ArrayMap<K, V> resultDictionary = new ArrayMap<>();

        try {
            String currentLine;
            br = new BufferedReader(new java.io.FileReader(inputFile));

            // parse
            while (null != (currentLine = br.readLine())) {

                String[] splitLine = currentLine.split(":");

                if (splitLine.length < 2) continue;
                String keyString = splitLine[0];
                String valueString = splitLine[1];

                // TODO should we use an interface so the class parses the key and value? I think so
                K key = (K) keyString;
                V value = (V) valueString;

                resultDictionary.put(key, value);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FILE_READER", e.toString());
        } finally {
            try {
                if (null != br) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.e("FILE_READER", ex.toString());
            }
        }

        return resultDictionary;
    }
    // TODO piv result data reader
    // TODO popup/fragment to load the data (probably in the imageviewer activity)
}
