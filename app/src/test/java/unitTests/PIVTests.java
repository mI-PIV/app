package unitTests;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

import testUtils.ReadCsvFile;

public class PIVTests {
    private final ClassLoader classLoader = getClass().getClassLoader();

    @Test
    public void PIV_shear_dx5_ReturnsTrue() {
        assert classLoader != null;
        File frame1 = new File(classLoader.getResource("Images_Duct_dx5_dz0/imageA.png").getFile());
        File frame2 = new File(classLoader.getResource("Images_Duct_dx5_dz0/imageB.png").getFile());
        File data = new File(classLoader.getResource("Images_Duct_dx5_dz0/real_data.txt").getFile());
        List<String[]> dataList = ReadCsvFile.readCsv(data);

//        Assert.assertArrayEquals();

        Assert.assertEquals(0, 0);
    }

    @Test
    public void PIV_shear_dx10_ReturnsTrue() {

    }

    @Test
    public void PIV_shear_dx20_ReturnsTrue() {

    }

    @Test
    public void PIV_shear_dx25_ReturnsTrue() {

    }
}
