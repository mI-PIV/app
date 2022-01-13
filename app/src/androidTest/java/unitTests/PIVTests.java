package unitTests;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import testUtils.PivRunnerTestObj;
import testUtils.ReadCsvFile;

public class PIVTests {
    private final PivParameters defaultParameters = new PivParameters();
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Before
    public void before() {
        OpenCVLoader.initDebug();
    }


    @Test
    public void PIV_shear_dx5() {
        double[] comparison = PIV_test(R.drawable.test_dx5_a, R.drawable.test_dx5_b, R.raw.test_data_dx5);

        assert comparison != null;
        Assert.assertTrue(comparison[0] < 1.0d);
        Assert.assertTrue(comparison[1] < 1.0d);
    }

    @Test
    public void PIV_shear_dx10() {
        double[] comparison = PIV_test(R.drawable.test_dx10_a, R.drawable.test_dx10_b, R.raw.test_data_dx10);

        assert comparison != null;
        Assert.assertTrue(comparison[0] < 1.0d);
        Assert.assertTrue(comparison[1] < 1.0d);
    }

    @Test
    public void PIV_shear_dx20() {
        double[] comparison = PIV_test(R.drawable.test_dx20_a, R.drawable.test_dx20_b, R.raw.test_data_dx20);

        assert comparison != null;
        Assert.assertTrue(comparison[0] < 1.0d);
        Assert.assertTrue(comparison[1] < 1.0d);
    }

    @Test
    public void PIV_shear_dx25() {
        double[] comparison = PIV_test(R.drawable.test_dx25_a, R.drawable.test_dx25_b, R.raw.test_data_dx25);

        assert comparison != null;
        Assert.assertTrue(comparison[0] < 1.0d);
        Assert.assertTrue(comparison[1] < 1.0d);
    }

    private double[] PIV_test(int frame1ID, int frame2ID, int realDataID) {
        Mat frame1;
        Mat frame2;
        try {
            frame1 = Utils.loadResource(context, frame1ID);
            frame2 = Utils.loadResource(context, frame2ID);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        //load data
        InputStream dataStream = context.getResources().openRawResource(realDataID);
        List<String[]> data = ReadCsvFile.readCsv(dataStream);

        // init piv
        PivRunnerTestObj piv = new PivRunnerTestObj(defaultParameters, frame1, frame2);

        // run piv
        PivResultData results = piv.runSinglePass();

        // cleanup
        frame1.release();
        frame2.release();
        try {dataStream.close();} catch (IOException e){e.printStackTrace();}

        return compareData(data, results);
    }

    private double[] compareData(List<String[]> realData, PivResultData resultData) {
        List<Double> errorU = new ArrayList<>();
        List<Double> errorV = new ArrayList<>();

        realData.remove(0);  // remove header
        for (int x = 0; x < resultData.getU().length; x++) {
            for (int y = 0; y < resultData.getU()[x].length; y++) {
                // TODO index issue

                //result data
                double u = resultData.getU()[x][y];
                double v = resultData.getV()[x][y];

                // real data
                int xs = (x+1) * resultData.getStepX();
                int ys = (y+1) * resultData.getStepY();
                int oneDIndex = (xs-1) * resultData.getCols() + (ys-1);
                String[] realLine = realData.get(oneDIndex);
                int rx = Integer.parseInt(realLine[0]);
                int ry = Integer.parseInt(realLine[1]);
                double ru = Double.parseDouble(realLine[2]);
                double rv = Double.parseDouble(realLine[3]);

                errorU.add(Math.abs(u - ru));
                errorV.add(Math.abs(v - rv));
            }
        }
        // calc u error
        double eU = 0d;
        // TODO median instead of mean
        // TODO plot histogram of the errors

        for (double e : errorU)
            eU += e;
        eU = eU / errorU.size();

        // calc v error
        double eV = 0d;
        for (double e : errorV)
            eV += e;
        eV = eV / errorV.size();

        return new double[]{eU, eV};
    }
}
