package basic.option.unit;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import android.annotation.SuppressLint;

import org.junit.jupiter.api.Test;

public class UnitUtilsTest {

    @Test
    public void formatFloat() {
        assertThat(formatFloat(14.34234f), anyOf(equalTo("14.34"), equalTo("14,34")));
        assertThat(formatFloat(14.34834f), anyOf(equalTo("14.35"), equalTo("14,35")));
        assertThat(formatFloat(14.34834f, 3), anyOf(equalTo("14.348"), equalTo("14,348")));
        assertThat(formatFloat(14.34864f, 3), anyOf(equalTo("14.349"), equalTo("14,349")));
    }

    @Test
    public void formatInt() {
        assertEquals(formatInt(14), "14");
        assertEquals(formatInt(16), "16");
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
