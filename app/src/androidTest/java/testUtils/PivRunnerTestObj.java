package testUtils;


import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;

public class PivRunnerTestObj {
    private final PivParameters _params;
    private final PivFunctions _piv;


    public PivRunnerTestObj(PivParameters params, Mat frame1, Mat frame2) {
        _params = params;
        _piv = new PivFunctions(
                frame1,
                frame2,
                null,
                _params,
                null,
                null,
                null
                );
    }

//    public HashMap<String, PivResultData> runFullPIV() {
//        PivRunner runner = new PivRunner(null, null, _params, _frame1, _frame2);
//        return runner.Run();
//    }

    public PivResultData runSinglePass() {
        // single pass
        PivResultData singlePassResult = _piv.extendedSearchAreaPiv_update(PivResultData.SINGLE, true, null);

        // vorticity
        PivFunctions.calculateVorticityMap(singlePassResult);

        return singlePassResult;
    }

    public PivResultData runPostProcessing(PivResultData singlePass) {
        return _piv.vectorPostProcessing(singlePass, "PostProcessing");
    }

    public PivResultData runMultiPass_withReplace(PivResultData postProcessed) {
        boolean fft = true;
        PivResultData pivReplaceMissing = _piv.replaceMissingVectors(postProcessed, null);
        PivResultData pivCorrelationMulti = _piv.calculateMultipass(pivReplaceMissing, PivResultData.MULTI, fft, null);
        PivResultData pivReplaceMissing2 = _piv.replaceMissingVectors(pivCorrelationMulti, PivResultData.REPLACE2);
        PivFunctions.calculateVorticityMap(pivReplaceMissing2);
        _params.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivReplaceMissing2.getMag()));
        return pivReplaceMissing2;
    }

//    public PivResultData runReplacement_withoutReplace(PivResultData postProcessed) {
//        PivResultData pivCorrelationMulti = _piv.calculateMultipass(postProcessed, PivResultData.MULTI, null);
//        _params.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivCorrelationMulti.getMag()));
//        return pivCorrelationMulti;
//    }

    private String getCanonicalPath(File file) {
        String result = null;
        try {
            result = file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
