package unitTests;

import org.junit.Test;

import java.io.File;

public class PIVTests {
    private static final String res = "Images_Duct_";
    private final ClassLoader classLoader = getClass().getClassLoader();

    @Test
    public void PIV_shear_dx5_ReturnsTrue() {
        File frame1 = new File(classLoader.getResource(res + "dx5_dz0/imageA.png").getFile());
        File frame2 = new File(classLoader.getResource(res + "dx5_dz0/imageB.png").getFile());
        File data = new File(classLoader.getResource(res + "dx5_dz0/real_data.txt").getFile());


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
