package com.onrpiv.uploadmedia.pivFunctions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.Camera.CameraCalibration;
import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;

import java.io.File;
import java.util.HashMap;


public class PivRunner {
    private final PivParameters parameters;
    private final Context context;
    private final File frame1File;
    private final File frame2File;
    private final String userName;

    private Activity imageActivity;
    private ProgressDialog pDialog;


    public PivRunner(Context context, String userName, PivParameters parameters, File frame1File,
                     File frame2File) {
        this.parameters = parameters;
        this.frame1File = frame1File;
        this.frame2File = frame2File;
        this.context = context;
        this.userName = userName;
    }

    public HashMap<String, PivResultData> Run() {
        // create new experiment directory
        int newExpTotal = (PersistedData.getTotalExperiments(context, userName) + 1);
        File experimentDir = PathUtil.getExperimentNumberedDirectory(context, userName, newExpTotal);
        PersistedData.setTotalExperiments(context, userName, newExpTotal);

        final String imgFileSaveName = PathUtil.getExperimentImageFileSuffix(newExpTotal);
        final String txtFileSaveName = PathUtil.getExperimentTextFileSuffix(newExpTotal);

        imageActivity = (Activity) context;

        final PivFunctions pivFunctions = new PivFunctions(frame1File.getAbsolutePath(),
                frame2File.getAbsolutePath(),
                "peak2peak",
                parameters,
                experimentDir,
                imgFileSaveName,
                txtFileSaveName);

        // background sub
        final int backgroundSelection = parameters.getBackgroundSelection();

        // progress dialog
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(context.getString(R.string.msg_loading));
        pDialog.setCancelable(false);
        if (!pDialog.isShowing()) pDialog.show();

        final HashMap<String, PivResultData> resultData = new HashMap<>();


        //---------------------------------Using Threads--------------------------------------//
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (backgroundSelection >= 0) {
                    setMessage("Subtracting frames");

                    pivFunctions.framesSubtraction(backgroundSelection, frame1File.getParentFile(),
                            parameters.getFrame1Index(), parameters.getFrame2Index());
                }
                setMessage("Calculating PIV");
                setMessage("Calculating single pass PIV");
                // single pass
                PivResultData singlePassResult = pivFunctions.extendedSearchAreaPiv_update(PivResultData.SINGLE);

                // Save first frame for output base image
                pivFunctions.saveBaseImage("Base");

                setMessage("Calculating Pixels per Metric");
                CameraCalibration calibration = new CameraCalibration(context);
                double pixelToCmRatio = calibration.calibrate(frame1File.getAbsolutePath(), frame2File.getAbsolutePath());
                if (calibration.foundTriangle) {
                    pivFunctions.saveVectorCentimeters(singlePassResult, pixelToCmRatio, "CENTIMETERS");
                } else {
                    singlePassResult.setCalibrated(false);
                }

                setMessage("Calculating single pass vorticity");
                String vortStep = "Vorticity";
                PivFunctions.calculateVorticityMap(singlePassResult);
                pivFunctions.saveVorticityValues(singlePassResult.getVorticityValues(), vortStep);

                setMessage("Saving single pass data");

                String step = "SinglePass";
                pivFunctions.saveVectorsValues(singlePassResult, step);

                setMessage("Post-Processing Single Pass PIV");
                PivResultData pivCorrelationProcessed =
                        pivFunctions.vectorPostProcessing(singlePassResult, "PostProcessing");

                setMessage("Saving post processing data");
                String stepPro = "VectorPostProcess";
                pivFunctions.saveVectorsValues(pivCorrelationProcessed, stepPro);
                resultData.put(PivResultData.SINGLE, singlePassResult);

                PivResultData pivCorrelationMulti;

                if (parameters.isReplace()) {
                    setMessage("Calculating multi-pass PIV");
                    PivResultData pivReplaceMissing = pivFunctions.replaceMissingVectors(pivCorrelationProcessed, null);
                    pivCorrelationMulti = pivFunctions.calculateMultipass(pivReplaceMissing, PivResultData.MULTI);

                    String stepMulti = "Multipass";
                    pivFunctions.saveVectorsValues(pivCorrelationMulti, stepMulti);
                    setMessage("Calculating replaced vectors");
                    PivResultData pivReplaceMissing2 = pivFunctions.replaceMissingVectors(pivCorrelationMulti, PivResultData.REPLACE2);

                    setMessage("Calculating replacement vorticity");
                    PivFunctions.calculateVorticityMap(pivReplaceMissing2);
                    pivFunctions.saveVorticityValues(pivReplaceMissing2.getVorticityValues(), "Replace_Vorticity");

                    resultData.put(PivResultData.REPLACE2, pivReplaceMissing2);

                    String stepReplace2 = "Replaced2";
                    pivFunctions.saveVectorsValues(pivReplaceMissing2, stepReplace2);

                    parameters.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivReplaceMissing2.getMag()));
                } else {
                    setMessage("Calculating multi-pass PIV");
                    pivCorrelationMulti = pivFunctions.calculateMultipass(pivCorrelationProcessed, PivResultData.MULTI);

                    String stepMulti = "Multipass";
                    pivFunctions.saveVectorsValues(pivCorrelationMulti, stepMulti);

                    parameters.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivCorrelationMulti.getMag()));
                }

                setMessage("Calculating multi-pass vorticity");
                PivFunctions.calculateVorticityMap(pivCorrelationMulti);
                pivFunctions.saveVorticityValues(pivCorrelationMulti.getVorticityValues(), "Multi_Vorticity");

                resultData.put(PivResultData.MULTI, pivCorrelationMulti);

                // write our PivResultData object and our PivParameter object to a file for loading later
                setMessage("Saving data...");
                FileIO.writePIVData(resultData, parameters, context, userName, newExpTotal);

                if (pDialog.isShowing()) pDialog.dismiss();
            }
        };
        //-------------------------------Thread End-------------------------------------------//
        thread.start();

        return resultData;
    }

    private void setMessage(String msg) {
        imageActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pDialog.setMessage(msg);
            }
        });
    }
}
