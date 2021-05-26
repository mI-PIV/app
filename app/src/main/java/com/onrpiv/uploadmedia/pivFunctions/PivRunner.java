package com.onrpiv.uploadmedia.pivFunctions;

import android.app.ProgressDialog;
import android.content.Context;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ArrowDrawOptions;
import com.onrpiv.uploadmedia.Utilities.Camera.CameraCalibration;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


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

    public List<PivResultData> Run() {
        // create new experiment directory
        int newExpTotal = (PersistedData.getTotalExperiments(context, userName) + 1);
        File experimentDir = PathUtil.getExperimentNumberedDirectory(userName, newExpTotal);
        PersistedData.setTotalExperiments(context, userName, newExpTotal);

        final String imgFileSaveName = PathUtil.getExperimentImageFileSuffix(newExpTotal);
        final String txtFileSaveName = PathUtil.getExperimentTextFileSuffix(newExpTotal);

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

        final List<PivResultData> resultData = new ArrayList<>();


        //---------------------------------Using Threads--------------------------------------//
        Thread thread = new Thread() {
            @Override
            public void run() {
                pDialog.setMessage("Calculating PIV");
                // single pass
                PivResultData singlePassResult = pivFunctions.extendedSearchAreaPiv_update(PivResultData.SINGLE);
                singlePassResult.setInterrCenters(pivFunctions.getCoordinates());

                // Save first frame for output base image
                pivFunctions.saveBaseImage("Base");

                pDialog.setMessage("Calculating Pixels per Metric");
                CameraCalibration calibration = new CameraCalibration(context);
                double pixelToCmRatio = calibration.calibrate(frame1File.getAbsolutePath(), frame2File.getAbsolutePath());
                if (calibration.foundTriangle) {
                    pivFunctions.saveVectorCentimeters(singlePassResult, pixelToCmRatio, "CENTIMETERS");
                }

                pDialog.setMessage("Calculating vorticity");
                String vortStep = "Vorticity";
                PivFunctions.calculateVorticityMap(singlePassResult);
                pivFunctions.saveVortMapFile(singlePassResult.getVorticityValues(), vortStep);
                pivFunctions.saveColorMapImage(singlePassResult.getVorticityValues(), vortStep);

                pDialog.setMessage("Saving single pass data");
                ArrowDrawOptions arrowDrawOptions = new ArrowDrawOptions();

                String step = "SinglePass";
                pivFunctions.saveVectors(singlePassResult, step);
                pivFunctions.createVectorField(singlePassResult, step, arrowDrawOptions);

                pDialog.setMessage("Processing Single Pass PIV");
                PivResultData pivCorrelationProcessed =
                        pivFunctions.vectorPostProcessing(singlePassResult, "PostProcessing");

                pDialog.setMessage("Saving post processing data");
                String stepPro = "VectorPostProcess";
                pivFunctions.saveVectors(pivCorrelationProcessed, stepPro);
                pivFunctions.createVectorField(pivCorrelationProcessed, stepPro, arrowDrawOptions);

                resultData.add(singlePassResult);
                resultData.add(pivCorrelationProcessed);

                PivResultData pivCorrelationMulti;

                if (parameters.isReplace()) {
                    pDialog.setMessage("Calculating multi-pass PIV");
                    PivResultData pivReplaceMissing = pivFunctions.replaceMissingVectors(pivCorrelationProcessed, null);
                    pivCorrelationMulti = pivFunctions.calculateMultipass(pivReplaceMissing, PivResultData.MULTI);

                    String stepMulti = "Multipass";
                    pivFunctions.saveVectors(pivCorrelationMulti, stepMulti);
                    pivFunctions.createVectorField(pivCorrelationMulti, stepMulti, arrowDrawOptions);
                    pDialog.setMessage("Calculating replaced vectors");
                    PivResultData pivReplaceMissing2 = pivFunctions.replaceMissingVectors(pivCorrelationMulti, PivResultData.REPLACE2);
                    resultData.add(pivReplaceMissing2);

                    String stepReplace2 = "Replaced2";
                    pivFunctions.saveVectors(pivReplaceMissing2, stepReplace2);
                    pivFunctions.createVectorField(pivReplaceMissing2, stepReplace2, arrowDrawOptions);

                    parameters.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivReplaceMissing2.getMag()));
                } else {
                    pDialog.setMessage("Calculating multi-pass PIV");
                    pivCorrelationMulti = pivFunctions.calculateMultipass(pivCorrelationProcessed, PivResultData.MULTI);

                    String stepMulti = "Multipass";
                    pivFunctions.saveVectors(pivCorrelationMulti, stepMulti);
                    pivFunctions.createVectorField(pivCorrelationMulti, stepMulti, arrowDrawOptions);

                    parameters.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivCorrelationMulti.getMag()));
                }
                resultData.add(pivCorrelationMulti);

                if (pDialog.isShowing()) pDialog.dismiss();
            }
        };
        //-------------------------------Thread End-------------------------------------------//

        thread.start();

        return resultData;
    }
}
