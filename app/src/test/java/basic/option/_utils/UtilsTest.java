package basic.option._utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.res.Resources;

import org.junit.jupiter.api.Test;

import org.breezyweather.R;
import org.breezyweather.common.basic.models.options._basic.Utils;

public class UtilsTest {

    @Test
    public void getNameByValue() {
        Resources res = mock(Resources.class);
        when(res.getStringArray(R.array.dark_modes)).thenReturn(new String[] {
                "Automatic", "Follow system", "Always light", "Always dark"
        });
        when(res.getStringArray(R.array.dark_mode_values)).thenReturn(new String[] {
                "auto", "system", "light", "dark"
        });
        assertEquals(
                Utils.INSTANCE.getNameByValue(res, "auto", R.array.dark_modes, R.array.dark_mode_values),
                "Automatic"
        );
    }
}
