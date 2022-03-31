package com.onrpiv.uploadmedia.pivFunctions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.Utilities.FileIO;
import com.onrpiv.uploadmedia.Utilities.PathUtil;
import com.onrpiv.uploadmedia.Utilities.PersistedData;
import com.onrpiv.uploadmedia.Utilities.ProgressUpdateInterface;

import java.io.File;
import java.util.HashMap;


public class PivRunner implements ProgressUpdateInterface {
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
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if (!pDialog.isShowing()) pDialog.show();

        final HashMap<String, PivResultData> resultData = new HashMap<>();


        //---------------------------------Using Threads--------------------------------------//
        Thread thread = new Thread() {
            @Override
            public void run() {
                ////////////////////////////////////////////////////////////////////////////////////
                //// SETUP & BACKGROUND SUB ////
                ////////////////////////////////////////////////////////////////////////////////////
                boolean backgroundSub = false;
                if (backgroundSelection >= 0) {
                    setMessage("Subtracting frames");
                    backgroundSub = true;
                    pivFunctions.framesSubtraction(backgroundSelection, frame1File.getParentFile(),
                            parameters.getFrame1Index(), parameters.getFrame2Index());
                }

                // Save first frame for output base image
                pivFunctions.saveBaseImage("Base");


                ////////////////////////////////////////////////////////////////////////////////////
                //// SINGLE PASS ////
                ////////////////////////////////////////////////////////////////////////////////////
                setMessage("Calculating PIV");
                setMessage("Calculating single pass PIV");
                PivResultData singlePassResult = pivFunctions.extendedSearchAreaPiv_update(PivResultData.SINGLE,
                        parameters.isFFT(), PivRunner.this);
                singlePassResult.setBackgroundSubtracted(backgroundSub);

                // save raw single pass
                resultData.put(singlePassResult.getName(), singlePassResult);
                pivFunctions.saveVectorsValues(singlePassResult, singlePassResult.getName());

                PivResultData singlePassProcessed =
                        pivFunctions.vectorPostProcessing(singlePassResult, true,
                                PivResultData.SINGLE+PivResultData.PROCESSED);

                // single pass vorticity
                PivFunctions.calculateVorticityMap(singlePassProcessed);

                // save post-processed single pass
                resultData.put(singlePassProcessed.getName(), singlePassProcessed);
                pivFunctions.saveVectorsValues(singlePassProcessed, singlePassProcessed.getName());


                ////////////////////////////////////////////////////////////////////////////////////
                //// MULTI PASS ////
                ////////////////////////////////////////////////////////////////////////////////////
                setMessage("Calculating Multi-Pass PIV");
                PivResultData multipassResults = pivFunctions.calculateMultipass(singlePassProcessed,
                        PivResultData.MULTI, parameters.isFFT(), PivRunner.this);

                resultData.put(multipassResults.getName(), multipassResults);
                pivFunctions.saveVectorsValues(multipassResults, multipassResults.getName());

                PivResultData multiPassProcessed = pivFunctions.vectorPostProcessing(multipassResults,
                        parameters.isReplace(), PivResultData.MULTI+PivResultData.PROCESSED);

                parameters.setMaxDisplacement(PivFunctions.checkMaxDisplacement(multiPassProcessed.getMag()));

                // save multi pass post-processed
                resultData.put(multiPassProcessed.getName(), multiPassProcessed);
                pivFunctions.saveVectorsValues(multiPassProcessed, multiPassProcessed.getName());


                ////////////////////////////////////////////////////////////////////////////////////
                //// CALIBRATION ////
                ////////////////////////////////////////////////////////////////////////////////////
                if (parameters.getCameraCalibrationResult() != null) {
                    setMessage("Applying Camera Calibration");

                    // singlepass
                    singlePassResult.setPixelToPhysicalRatio(parameters.getCameraCalibrationResult().ratio,
                            pivFunctions.getFieldRows(), pivFunctions.getFieldCols());

                    pivFunctions.saveVectorCentimeters(singlePassResult,
                            parameters.getCameraCalibrationResult().ratio,
                            "CENTIMETERS_" + singlePassResult.getName());

                    // multipass
                    multipassResults.setPixelToPhysicalRatio(parameters.getCameraCalibrationResult().ratio,
                            pivFunctions.getFieldRows(), pivFunctions.getFieldCols());

                    pivFunctions.saveVectorCentimeters(multipassResults,
                            parameters.getCameraCalibrationResult().ratio,
                            "CENTIMETERS_" + multipassResults.getName());

                    // multipass processed
                    multiPassProcessed.setPixelToPhysicalRatio(parameters.getCameraCalibrationResult().ratio,
                            pivFunctions.getFieldRows(), pivFunctions.getFieldCols());

                    pivFunctions.saveVectorCentimeters(multiPassProcessed,
                            parameters.getCameraCalibrationResult().ratio,
                            "CENTIMETERS_" + multiPassProcessed.getName());
                }


                ////////////////////////////////////////////////////////////////////////////////////
                //// VORTICITY ////
                ////////////////////////////////////////////////////////////////////////////////////
                setMessage("Calculating vorticity");
                PivFunctions.calculateVorticityMap(multiPassProcessed);
                pivFunctions.saveVorticityValues(multiPassProcessed.getVorticityValues(), "Vorticity");


                ////////////////////////////////////////////////////////////////////////////////////
                //// SAVE DATA ////
                ////////////////////////////////////////////////////////////////////////////////////
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

    @Override
    public void updateProgressIteration(int iteration) {
        imageActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pDialog.setProgress(iteration);
            }
        });
    }

    @Override
    public void setProgressMax(int max) {
        imageActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pDialog.setMax(max);
            }
        });
    }
}
