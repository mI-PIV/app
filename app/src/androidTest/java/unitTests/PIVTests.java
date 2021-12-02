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
    private final ClassLoader classLoader = getClass().getClassLoader();
    private final PivParameters defaultParameters = new PivParameters();

    @Before
    public void before() {
        OpenCVLoader.initDebug();
    }


    @Test
    public void PIV_shear_dx5() {
        // load frames and data
//        File test = new File(ClassLoader.getSystemClassLoader().getResource("Images_Duct_dx5_dz0/imageA.png").getFile());
//        File frame1 = new File(this.getClass().getClassLoader().getResource("Images_Duct_dx5_dz0/imageA.png").getFile());
//        File frame2 = new File(this.getClass().getClassLoader().getResource("Images_Duct_dx5_dz0/imageB.png").getFile());
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

//        File resDir = context.getDir("resources", Context.MODE_PRIVATE);
//        File frame1 = new File(resDir, "Images_Duct_dx5_dz0/imageA.png");
//        File frame2 = new File(resDir, "Images_Duct_dx5_dz0/imageB.png");


//        File frame1 = new File(context.getClassLoader().getResource("imagea_dx5.png").getFile());
//        File frame2 = new File(context.getClassLoader().getResource("imageb_dx5.png").getFile());

        Mat frame1 = null;
        Mat frame2 = null;
        try {
            frame1 = Utils.loadResource(context, R.drawable.imagea_dx5);
            frame2 = Utils.loadResource(context, R.drawable.imageb_dx5);
        } catch (IOException e) {
            e.printStackTrace();
        }


//
//        Uri f1Uri = Uri.parse(f1.getAbsolutePath());
//        Uri f2Uri = Uri.parse(f2.getAbsolutePath());
//
//        File frame1 = new File(f1Uri.getPath());
//        File frame2 = new File(f2Uri.getPath());

//        File frame1 = new File(classLoader.getResource("imageA.png").getFile());
//        File frame2 = new File(classLoader.getResource("imageB.png").getFile());
//        File data = new File(classLoader.getResource("Images_Duct_dx5_dz0/real_data.txt").getFile());
//        List<String[]> dataList = ReadCsvFile.readCsv(data);

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
