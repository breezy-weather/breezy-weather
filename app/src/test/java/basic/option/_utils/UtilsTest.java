package basic.option._utils;

import android.content.res.Resources;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.models.options._utils.Utils;

@RunWith(PowerMockRunner.class)
public class UtilsTest {

    @Test
    public void getNameByValue() {
        Resources res = PowerMockito.mock(Resources.class);
        PowerMockito.when(res.getStringArray(R.array.dark_modes)).thenReturn(new String[] {
                "Automatic", "Follow system", "Always light", "Always dark"
        });
        PowerMockito.when(res.getStringArray(R.array.dark_mode_values)).thenReturn(new String[] {
                "auto", "system", "light", "dark"
        });
        Assert.assertEquals(
                Utils.getNameByValue(res, "auto", R.array.dark_modes, R.array.dark_mode_values),
                "Automatic"
        );
    }
}
