package com.onrpiv.uploadmedia.pivFunctions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.Camera.CameraCalibration;
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

        final Activity imageActivity = (Activity) context;

        final PivFunctions pivFunctions = new PivFunctions(frame1File.getAbsolutePath(),
                frame2File.getAbsolutePath(),
                "peak2peak",
                parameters,
                experimentDir,
                imgFileSaveName,
                txtFileSaveName);

        // progress dialog
        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage(context.getString(R.string.msg_loading));
        pDialog.setCancelable(false);
        if (!pDialog.isShowing()) pDialog.show();

        final HashMap<String, PivResultData> resultData = new HashMap<>();


        //---------------------------------Using Threads--------------------------------------//
        Thread thread = new Thread() {
            @Override
            public void run() {
                pDialog.setMessage("Calculating PIV");
                setMessage(imageActivity, "Calculating PIV", pDialog);
                // single pass
                PivResultData singlePassResult = pivFunctions.extendedSearchAreaPiv_update(PivResultData.SINGLE);
                singlePassResult.setInterrCenters(pivFunctions.getCoordinates());

                // Save first frame for output base image
                pivFunctions.saveBaseImage("Base");

                setMessage(imageActivity, "Calculating Pixels per Metric", pDialog);
                CameraCalibration calibration = new CameraCalibration(context);
                double pixelToCmRatio = calibration.calibrate(frame1File.getAbsolutePath(), frame2File.getAbsolutePath());
                if (calibration.foundTriangle) {
                    pivFunctions.saveVectorCentimeters(singlePassResult, pixelToCmRatio, "CENTIMETERS");
                }

                setMessage(imageActivity, "Calculating single pass vorticity", pDialog);
                String vortStep = "Vorticity";
                PivFunctions.calculateVorticityMap(singlePassResult);
                pivFunctions.saveVorticityValues(singlePassResult.getVorticityValues(), vortStep);

                setMessage(imageActivity, "Saving single pass data", pDialog);

                String step = "SinglePass";
                pivFunctions.saveVectorsValues(singlePassResult, step);

                setMessage(imageActivity, "Processing Single Pass PIV", pDialog);
                PivResultData pivCorrelationProcessed =
                        pivFunctions.vectorPostProcessing(singlePassResult, "PostProcessing");

                setMessage(imageActivity, "Saving post processing data", pDialog);
                String stepPro = "VectorPostProcess";
                pivFunctions.saveVectorsValues(pivCorrelationProcessed, stepPro);
                resultData.put(PivResultData.SINGLE, singlePassResult);

                PivResultData pivCorrelationMulti;

                if (parameters.isReplace()) {
                    setMessage(imageActivity, "Calculating multi-pass PIV", pDialog);
                    PivResultData pivReplaceMissing = pivFunctions.replaceMissingVectors(pivCorrelationProcessed, null);

                    setMessage(imageActivity, "Calculating replacement vorticity", pDialog);
                    PivFunctions.calculateVorticityMap(pivReplaceMissing);
                    pivFunctions.saveVorticityValues(pivReplaceMissing.getVorticityValues(), "Replace_Vorticity");

                    pivCorrelationMulti = pivFunctions.calculateMultipass(pivReplaceMissing, PivResultData.MULTI);

                    setMessage(imageActivity, "Calculating multi-pass vorticity", pDialog);
                    PivFunctions.calculateVorticityMap(pivCorrelationMulti);
                    pivFunctions.saveVorticityValues(pivCorrelationMulti.getVorticityValues(), "Multi_Vorticity");

                    String stepMulti = "Multipass";
                    pivFunctions.saveVectorsValues(pivCorrelationMulti, stepMulti);
                    setMessage(imageActivity, "Calculating replaced vectors", pDialog);
                    PivResultData pivReplaceMissing2 = pivFunctions.replaceMissingVectors(pivCorrelationMulti, PivResultData.REPLACE2);
                    resultData.put(PivResultData.REPLACE2, pivReplaceMissing2);

                    String stepReplace2 = "Replaced2";
                    pivFunctions.saveVectorsValues(pivReplaceMissing2, stepReplace2);

                    parameters.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivReplaceMissing2.getMag()));
                } else {
                    setMessage(imageActivity, "Calculating multi-pass PIV", pDialog);
                    pivCorrelationMulti = pivFunctions.calculateMultipass(pivCorrelationProcessed, PivResultData.MULTI);

                    String stepMulti = "Multipass";
                    pivFunctions.saveVectorsValues(pivCorrelationMulti, stepMulti);

                    parameters.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivCorrelationMulti.getMag()));
                }
                resultData.put(PivResultData.MULTI, pivCorrelationMulti);

                if (pDialog.isShowing()) pDialog.dismiss();
            }
        };
        //-------------------------------Thread End-------------------------------------------//

        thread.start();

        return resultData;
    }

    private void setMessage(Activity activity, String msg, ProgressDialog pDialog) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pDialog.setMessage(msg);
            }
        });
    }
}
