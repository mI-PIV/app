package unitTests;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import org.junit.Before;
import org.junit.Test;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;

import testUtils.PivRunnerTestObj;

public class PIVTests {
    private final PivParameters defaultParameters = new PivParameters();
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Before
    public void before() {
        OpenCVLoader.initDebug();
    }


    @Test
    public void PIV_shear_dx5() {
        // load frames
        Mat frame1;
        Mat frame2;
        try {
            frame1 = Utils.loadResource(context, R.drawable.imagea_dx5);
            frame2 = Utils.loadResource(context, R.drawable.imageb_dx5);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //load data
//        InputStream dataStream = context.getResources().openRawResource(R.raw.real_data_dx5);
//        List<String[]> data = ReadCsvFile.readCsv(dataStream);

        // init piv
        PivRunnerTestObj piv = new PivRunnerTestObj(defaultParameters, frame1, frame2);

        // run piv
        PivResultData results = piv.runSinglePass();

        // compare results with data
//        Assert.assertArrayEquals();

        frame1.release();
        frame2.release();
    }

    @Test
    public void PIV_shear_dx10() {

    }

    @Test
    public void PIV_shear_dx20() {

    }

    @Test
    public void PIV_shear_dx25() {

    }
}
