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
    public Thread pivRunningThread;
    private final PivParameters parameters;
    private final Context context;
    private final File frame1File;
    private final File frame2File;
    private final File expDir;
    private final String userName;
    private final int index;

    private Activity imageActivity;
    private ProgressDialog pDialog;
    private final ProgressUpdateInterface progressUpdate;
    private final boolean showProgress;


    public PivRunner(Context context, String userName, PivParameters parameters, File frame1File,
                     File frame2File, File expDir, int index, boolean showProgress) {
        this.parameters = parameters;
        this.frame1File = frame1File;
        this.frame2File = frame2File;
        this.context = context;
        this.userName = userName;
        progressUpdate = showProgress? this : null;
        this.showProgress = showProgress;
        this.expDir = expDir;
        this.index = index;
    }

    public HashMap<String, PivResultData> Run() {
        if (null == expDir) {
            PathUtil.createNewExperimentDirectory(context, userName);
        }
        final int expTotal = PersistedData.getTotalExperiments(context, userName);

        final String imgFileSaveName = PathUtil.getExperimentImageFileSuffix(expTotal);
        final String txtFileSaveName = PathUtil.getExperimentTextFileSuffix(expTotal);

        imageActivity = (Activity) context;

        final PivFunctions pivFunctions = new PivFunctions(frame1File.getAbsolutePath(),
                frame2File.getAbsolutePath(),
                "peak2peak",
                parameters,
                expDir,
                imgFileSaveName,
                txtFileSaveName);

        // save parameters plain text
        pivFunctions.saveParametersPlainText(parameters);

        // background sub
        final int backgroundSelection = parameters.getBackgroundSelection();

        // progress dialog
        if (showProgress) {
            pDialog = new ProgressDialog(context);
            pDialog.setMessage(context.getString(R.string.msg_loading));
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            if (!pDialog.isShowing()) pDialog.show();
        }

        final HashMap<String, PivResultData> resultData = new HashMap<>();


        //---------------------------------Using Threads--------------------------------------//
        pivRunningThread = new Thread() {
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
                pivFunctions.saveBaseImage("Base_" + index);


                ////////////////////////////////////////////////////////////////////////////////////
                //// SINGLE PASS ////
                ////////////////////////////////////////////////////////////////////////////////////
                setMessage("Calculating PIV");
                setMessage("Calculating single pass PIV");
                PivResultData singlePassResult = pivFunctions.extendedSearchAreaPiv_update(PivResultData.SINGLE,
                        parameters.isFFT(), progressUpdate);
                singlePassResult.setBackgroundSubtracted(backgroundSub);

                // save raw single pass
                resultData.put(singlePassResult.getName(), singlePassResult);
                pivFunctions.saveVectorsValues(singlePassResult, singlePassResult.getName()+ "_" + index);

                PivResultData singlePassProcessed =
                        pivFunctions.vectorPostProcessing(singlePassResult, true,
                                PivResultData.SINGLE+PivResultData.PROCESSED);

                // single pass vorticity
                PivFunctions.calculateVorticityMap(singlePassProcessed);

                // save post-processed single pass
                resultData.put(singlePassProcessed.getName(), singlePassProcessed);
                pivFunctions.saveVectorsValues(singlePassProcessed, singlePassProcessed.getName()+ "_" + index);


                ////////////////////////////////////////////////////////////////////////////////////
                //// MULTI PASS ////
                ////////////////////////////////////////////////////////////////////////////////////
                setMessage("Calculating Multi-Pass PIV");
                PivResultData multipassResults = pivFunctions.calculateMultipass(singlePassProcessed,
                        PivResultData.MULTI, parameters.isFFT(), progressUpdate);

                resultData.put(multipassResults.getName(), multipassResults);
                pivFunctions.saveVectorsValues(multipassResults, multipassResults.getName()+ "_" + index);

                PivResultData multiPassProcessed = pivFunctions.vectorPostProcessing(multipassResults,
                        parameters.isReplace(), PivResultData.MULTI+PivResultData.PROCESSED);

                parameters.setMaxDisplacement(PivFunctions.checkMaxDisplacement(multiPassProcessed.getMag()));

                // save multi pass post-processed
                resultData.put(multiPassProcessed.getName(), multiPassProcessed);
                pivFunctions.saveVectorsValues(multiPassProcessed, multiPassProcessed.getName()+ "_" + index);


                ////////////////////////////////////////////////////////////////////////////////////
                //// VORTICITY ////
                ////////////////////////////////////////////////////////////////////////////////////
                setMessage("Calculating vorticity");
                PivFunctions.calculateVorticityMap(multipassResults);
                pivFunctions.saveVorticityValues(multipassResults.getVorticityValues(), "Vorticity_" + index);


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
                            "CENTIMETERS_" + singlePassResult.getName()+ "_" + index);
                    singlePassResult.setCalibrated(true);

                    // multipass
                    multipassResults.setPixelToPhysicalRatio(parameters.getCameraCalibrationResult().ratio,
                            pivFunctions.getFieldRows(), pivFunctions.getFieldCols());

                    pivFunctions.saveVectorCentimeters(multipassResults,
                            parameters.getCameraCalibrationResult().ratio,
                            "CENTIMETERS_" + multipassResults.getName());
                    multipassResults.setCalibrated(true);

                    // multipass processed
                    multiPassProcessed.setPixelToPhysicalRatio(parameters.getCameraCalibrationResult().ratio,
                            pivFunctions.getFieldRows(), pivFunctions.getFieldCols());

                    pivFunctions.saveVectorCentimeters(multiPassProcessed,
                            parameters.getCameraCalibrationResult().ratio,
                            "CENTIMETERS_" + multiPassProcessed.getName()+ "_" + index);
                    multiPassProcessed.setCalibrated(true);
                }

                ////////////////////////////////////////////////////////////////////////////////////
                //// SAVE DATA ////
                ////////////////////////////////////////////////////////////////////////////////////
                setMessage("Saving data...");
                FileIO.writePIVData(resultData, parameters, context, userName, expTotal);

                if (null != pDialog && pDialog.isShowing()) pDialog.dismiss();
            }
        };
        //-------------------------------Thread End-------------------------------------------//
        pivRunningThread.start();

        return resultData;
    }

    private void setMessage(String msg) {
        if (null == pDialog)
            return;
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
