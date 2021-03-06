package com.onrpiv.uploadmedia.Utilities.ColorMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ColorMap {
    private int id;
    private String name;
    private int openCV_code;
    private Drawable drawable;

    public ColorMap() {
        id = 0;
        name = "";
        openCV_code = 3;
        drawable = null;
    }

    public ColorMap setId(int id) {
        this.id = id;
        return this;
    }

    public int getId() {
        return id;
    }

    public ColorMap setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public ColorMap setOpenCV_code(int openCV_code) {
        this.openCV_code = openCV_code;
        return this;
    }

    public int getOpenCVCode() {
        return openCV_code;
    }

    public ColorMap setDrawable(Drawable drawable) {
        this.drawable = drawable;
        return this;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public ColorMap getColorMap(String name, Context context, Resources res, String packageName) {
        HashMap<String, Integer> openCVCodes = loadColormap_nameToOpenCV();
        this.name = name;
        this.id = res.getIdentifier("colormap_"+name, "drawable", packageName);
        this.drawable = ContextCompat.getDrawable(context, id);
        this.openCV_code = openCVCodes.get(name);
        return this;
    }

    public static ArrayList<ColorMap> loadColorMaps(Context context, Resources res, String packageName) {
        ArrayList<ColorMap> result = new ArrayList<>();
        ArrayList<String> names = getColorMapNames();
        HashMap<String, Integer> openCVCodes = loadColormap_nameToOpenCV();

        for (String name : names) {
            ColorMap colorMap = new ColorMap();
            colorMap.setName(name);

            int id = res.getIdentifier("colormap_"+name, "drawable", packageName);
            if (id == 0) {
                Log.e("KP", name+" has an id of zero.");
                continue;
            }
            colorMap.setId(id);

            Drawable drawable = ContextCompat.getDrawable(context, id);
            colorMap.setDrawable(drawable);

            colorMap.setOpenCV_code(openCVCodes.get(name));
            result.add(colorMap);
        }

        return result;
    }

    private static ArrayList<String> getColorMapNames() {
        String[] cmaps = new String[]{"autumn", "bone", "cool", "hot", "hsv", "jet",
                "ocean", "parula", "pink", "rainbow", "spring", "summer", "winter"};
        ArrayList<String> result = new ArrayList<>();
        Collections.addAll(result, cmaps);
        return result;
    }

    public static HashMap<Integer, String> loadColormap_openCVToName() {
        HashMap<Integer, String> colormap = new HashMap<>();
        colormap.put(Imgproc.COLORMAP_AUTUMN, "autumn");
        colormap.put(Imgproc.COLORMAP_BONE, "bone");
        colormap.put(Imgproc.COLORMAP_COOL, "cool");
        colormap.put(Imgproc.COLORMAP_HOT, "hot");
        colormap.put(Imgproc.COLORMAP_HSV, "hsv");
        colormap.put(Imgproc.COLORMAP_JET, "jet");
        colormap.put(Imgproc.COLORMAP_OCEAN, "ocean");
        colormap.put(Imgproc.COLORMAP_PARULA, "parula");
        colormap.put(Imgproc.COLORMAP_PINK, "pink");
        colormap.put(Imgproc.COLORMAP_RAINBOW, "rainbow");
        colormap.put(Imgproc.COLORMAP_SPRING, "spring");
        colormap.put(Imgproc.COLORMAP_SUMMER, "summer");
        colormap.put(Imgproc.COLORMAP_WINTER, "winter");
        return colormap;
    }

    public static HashMap<String, Integer> loadColormap_nameToOpenCV() {
        HashMap<String, Integer> colormap = new HashMap<>();
        colormap.put("autumn", Imgproc.COLORMAP_AUTUMN);
        colormap.put("bone", Imgproc.COLORMAP_BONE);
        colormap.put("cool", Imgproc.COLORMAP_COOL);
        colormap.put("hot", Imgproc.COLORMAP_HOT);
        colormap.put("hsv", Imgproc.COLORMAP_HSV);
        colormap.put("jet", Imgproc.COLORMAP_JET);
        colormap.put("ocean", Imgproc.COLORMAP_OCEAN);
        colormap.put("parula", Imgproc.COLORMAP_PARULA);
        colormap.put("pink", Imgproc.COLORMAP_PINK);
        colormap.put("rainbow", Imgproc.COLORMAP_RAINBOW);
        colormap.put("spring", Imgproc.COLORMAP_SPRING);
        colormap.put("summer", Imgproc.COLORMAP_SUMMER);
        colormap.put("winter", Imgproc.COLORMAP_WINTER);
        return colormap;
    }
}
