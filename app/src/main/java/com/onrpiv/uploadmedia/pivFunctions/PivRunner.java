package com.onrpiv.uploadmedia.pivFunctions;

import android.app.ProgressDialog;
import android.content.Context;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.ArrowDrawOptions;
import com.onrpiv.uploadmedia.Utilities.Camera.CameraCalibration;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;

import java.io.File;
import java.util.Map;


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

    public PivResultData Run() {
        // create new experiment directory
        int newExpTotal = (PersistedData.getTotalExperiments(context, userName) + 1);
        File experimentDir = PathUtil.getExperimentNumberedDirectory(userName, newExpTotal);
        PersistedData.setTotalExperiments(context, userName, newExpTotal);

        final PivResultData resultData = new PivResultData();

        final String imgFileSaveName = PathUtil.getExperimentImageFileSuffix(newExpTotal);
        final String txtFileSaveName = PathUtil.getExperimentTextFileSuffix(newExpTotal);


        final PivFunctions pivFunctions = new PivFunctions(frame1File.getAbsolutePath(),
                frame2File.getAbsolutePath(),
                "peak2peak",
                parameters,
                experimentDir,
                imgFileSaveName,
                txtFileSaveName);

        resultData.setCols(pivFunctions.getCols());
        resultData.setRows(pivFunctions.getRows());

        // progress dialog
        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage(context.getString(R.string.msg_loading));
        pDialog.setCancelable(false);
        if (!pDialog.isShowing()) pDialog.show();


        //---------------------------------Using Threads--------------------------------------//
        Thread thread = new Thread() {
            @Override
            public void run() {
                pDialog.setMessage("Calculating PIV");
                Map<String, double[][]> pivCorrelation = pivFunctions.extendedSearchAreaPiv_update();

                Map<String, double[]> interrCenters = pivFunctions.getCoordinates();

                ArrowDrawOptions arrowDrawOptions = new ArrowDrawOptions();
                arrowDrawOptions.scale = 5d;

                // Save first frame for output base image
                pivFunctions.saveBaseImage("Base");

                pDialog.setMessage("Calculating Pixels per Metric");
                CameraCalibration calibration = new CameraCalibration(context);
                double pixelToCmRatio = calibration.calibrate(frame1File.getAbsolutePath(), frame2File.getAbsolutePath());
                if (calibration.foundTriangle) {
                    pivFunctions.saveVectorCentimeters(pivCorrelation, interrCenters, pixelToCmRatio, "CENTIMETERS");
                }

                pDialog.setMessage("Calculating vorticity");
                String vortStep = "Vorticity";
                double[][] vorticityValues = PivFunctions.calculateVorticityMap(pivCorrelation,
                        (int) (interrCenters.get("x")[1] - interrCenters.get("x")[0]));
                pivFunctions.saveVortMapFile(vorticityValues, vortStep);
                pivFunctions.saveColorMapImage(vorticityValues, vortStep);

                pDialog.setMessage("Saving single pass data");
                String step = "SinglePass";
                pivFunctions.saveVectors(pivCorrelation, interrCenters, step);
                pivFunctions.createVectorField(pivCorrelation, interrCenters, step, arrowDrawOptions);
                Map<String, double[][]> pivCorrelationProcessed = pivFunctions.vectorPostProcessing(pivCorrelation);

                pDialog.setMessage("Saving post processing data");
                String stepPro = "VectorPostProcess";
                pivFunctions.saveVectors(pivCorrelationProcessed, interrCenters, stepPro);
                pivFunctions.createVectorField(pivCorrelationProcessed, interrCenters, stepPro, arrowDrawOptions);

                Map<String, double[][]> pivReplaceMissing2;
                Map<String, double[][]> pivCorrelationMulti;
                Map<String, double[][]> pivReplaceMissing;

                if (parameters.isReplace()) {
                    pDialog.setMessage("Calculating multi-pass PIV");
                    pivReplaceMissing = pivFunctions.replaceMissingVectors(pivCorrelationProcessed);
                    pivCorrelationMulti = pivFunctions.calculateMultipass(pivReplaceMissing, interrCenters);

                    String stepMulti = "Multipass";
                    pivFunctions.saveVectors(pivCorrelationMulti, interrCenters, stepMulti);
                    pivFunctions.createVectorField(pivCorrelationMulti, interrCenters, stepMulti, arrowDrawOptions);
                    pivReplaceMissing2 = pivFunctions.replaceMissingVectors(pivCorrelationMulti);

                    pDialog.setMessage("Calculating replaced vectors");
                    String stepReplace2 = "Replaced2";
                    pivFunctions.saveVectors(pivReplaceMissing2, interrCenters, stepReplace2);
                    pivFunctions.createVectorField(pivReplaceMissing2, interrCenters, stepReplace2, arrowDrawOptions);

                    parameters.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivReplaceMissing2));

                    resultData.setPivReplaceMissing(pivReplaceMissing);
                    resultData.setPivReplaceMissing2(pivReplaceMissing2);

                } else {

                    pDialog.setMessage("Calculating multi-pass PIV");
                    pivCorrelationMulti = pivFunctions.calculateMultipass(pivCorrelationProcessed,
                            interrCenters);

                    String stepMulti = "Multipass";
                    pivFunctions.saveVectors(pivCorrelationMulti, interrCenters, stepMulti);
                    pivFunctions.createVectorField(pivCorrelationMulti, interrCenters, stepMulti, arrowDrawOptions);

                    parameters.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivCorrelationMulti));
                }

                // load up our resultData structure
                resultData.setPivCorrelation(pivCorrelation);
                resultData.setInterrCenters(interrCenters);
                resultData.setVorticityValues(vorticityValues);
                resultData.setPivCorrelationMulti(pivCorrelationMulti);
                resultData.setPivCorrelationProcessed(pivCorrelationProcessed);


                if (pDialog.isShowing()) pDialog.dismiss();
            }
        };
        //-------------------------------Thread End-------------------------------------------//

        thread.start();

        return resultData;
    }
}
