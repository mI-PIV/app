package testUtils;


import com.onrpiv.uploadmedia.pivFunctions.PivFunctions;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;
import com.onrpiv.uploadmedia.pivFunctions.PivRunner;

import java.io.File;
import java.util.HashMap;

public class PivRunnerTestObj {
    private final PivParameters _params;
    private final PivFunctions _piv;
    private final File _frame1;
    private final File _frame2;


    public PivRunnerTestObj(PivParameters params, File frame1, File frame2) {
        _params = params;
        _frame1 = frame1;
        _frame2 = frame2;
        _piv = new PivFunctions(
                _frame1.getAbsolutePath(),
                _frame2.getAbsolutePath(),
                null,
                _params,
                null,
                null,
                null
                );
    }

    public HashMap<String, PivResultData> runFullPIV() {
        PivRunner runner = new PivRunner(null, null, _params, _frame1, _frame2);
        return runner.Run();
    }

    public PivResultData runSinglePass() {
        // single pass
        PivResultData singlePassResult = _piv.extendedSearchAreaPiv_update(PivResultData.SINGLE, null);

        // vorticity
        PivFunctions.calculateVorticityMap(singlePassResult);

        return singlePassResult;
    }

    public PivResultData runPostProcessing(PivResultData singlePass) {
        return _piv.vectorPostProcessing(singlePass, "PostProcessing");
    }

    public PivResultData runMultiPass_withReplace(PivResultData postProcessed) {
        PivResultData pivReplaceMissing = _piv.replaceMissingVectors(postProcessed, null);
        PivResultData pivCorrelationMulti = _piv.calculateMultipass(pivReplaceMissing, PivResultData.MULTI, null);
        PivResultData pivReplaceMissing2 = _piv.replaceMissingVectors(pivCorrelationMulti, PivResultData.REPLACE2);
        PivFunctions.calculateVorticityMap(pivReplaceMissing2);
        _params.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivReplaceMissing2.getMag()));
        return pivReplaceMissing2;
    }

    public PivResultData runReplacement_withoutReplace(PivResultData postProcessed) {
        PivResultData pivCorrelationMulti = _piv.calculateMultipass(postProcessed, PivResultData.MULTI, null);
        _params.setMaxDisplacement(PivFunctions.checkMaxDisplacement(pivCorrelationMulti.getMag()));
        return pivCorrelationMulti;
    }
}
