package unitTests;

import com.onrpiv.uploadmedia.pivFunctions.PivParameters;
import com.onrpiv.uploadmedia.pivFunctions.PivResultData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import testUtils.PivRunnerTestObj;
import testUtils.ReadCsvFile;

public class PIVTests {
    private final ClassLoader classLoader = getClass().getClassLoader();
    private final PivParameters defaultParameters = new PivParameters();

    @Before
    public void before() {
        OpenCVLoader.initDebug();
    }


    @Test
    public void PIV_shear_dx5() {
        // load frames and data
        File frame1 = new File(classLoader.getResource("Images_Duct_dx5_dz0/imageA.png").getFile());
        File frame2 = new File(classLoader.getResource("Images_Duct_dx5_dz0/imageB.png").getFile());
        File data = new File(classLoader.getResource("Images_Duct_dx5_dz0/real_data.txt").getFile());
        List<String[]> dataList = ReadCsvFile.readCsv(data);

        // init piv
        PivRunnerTestObj piv = new PivRunnerTestObj(defaultParameters, frame1, frame2);

        // run piv
        PivResultData results = piv.runSinglePass();

        // compare results with data
//        Assert.assertArrayEquals();
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
