package basic.option.unit;

import android.annotation.SuppressLint;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class UnitUtilsTest {

    @Test
    public void formatFloat() {
        Assert.assertEquals(formatFloat(14.34234f), "14.34");
        Assert.assertEquals(formatFloat(14.34834f), "14.35");
        Assert.assertEquals(formatFloat(14.34834f, 3), "14.348");
        Assert.assertEquals(formatFloat(14.34864f, 3), "14.349");
    }

    @Test
    public void formatInt() {
        Assert.assertEquals(formatInt(14), "14");
        Assert.assertEquals(formatInt(16), "16");
    }

    static String formatFloat(float value) {
        return formatFloat(value, 2);
    }

    static String formatFloat(float value, int decimalNumber) {
        return String.format(
                "%." + decimalNumber + "f",
                value
        );
    }

    @SuppressLint("DefaultLocale")
    static String formatInt(int value) {
        return String.format("%d", value);
    }
}
