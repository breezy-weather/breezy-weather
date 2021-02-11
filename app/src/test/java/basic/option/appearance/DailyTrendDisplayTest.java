package basic.option.appearance;

import android.content.Context;
import android.text.TextUtils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.models.options.appearance.DailyTrendDisplay;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class DailyTrendDisplayTest {

    @BeforeClass
    public static void setup() throws Exception {
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.class, "isEmpty", anyString()).thenReturn(false);
    }

    @Test
    public void toDailyTrendDisplayList() {
        String value = "temperature&air_quality&wind&uv_index&precipitation";

        List<DailyTrendDisplay> list = DailyTrendDisplay.toDailyTrendDisplayList(value);

        Assert.assertEquals(list.get(0), DailyTrendDisplay.TAG_TEMPERATURE);
        Assert.assertEquals(list.get(1), DailyTrendDisplay.TAG_AIR_QUALITY);
        Assert.assertEquals(list.get(2), DailyTrendDisplay.TAG_WIND);
        Assert.assertEquals(list.get(3), DailyTrendDisplay.TAG_UV_INDEX);
        Assert.assertEquals(list.get(4), DailyTrendDisplay.TAG_PRECIPITATION);
    }

    @Test
    public void toValue() {
        List<DailyTrendDisplay> list = new ArrayList<>();
        list.add(DailyTrendDisplay.TAG_TEMPERATURE);
        list.add(DailyTrendDisplay.TAG_AIR_QUALITY);
        list.add(DailyTrendDisplay.TAG_WIND);
        list.add(DailyTrendDisplay.TAG_UV_INDEX);
        list.add(DailyTrendDisplay.TAG_PRECIPITATION);

        String value = "temperature&air_quality&wind&uv_index&precipitation";

        Assert.assertEquals(DailyTrendDisplay.toValue(list), value);
    }

    @Test
    public void getSummary() {
        Context context = PowerMockito.mock(Context.class);
        doReturn("Name").when(context).getString(anyInt());

        List<DailyTrendDisplay> list = new ArrayList<>();
        list.add(DailyTrendDisplay.TAG_TEMPERATURE);
        list.add(DailyTrendDisplay.TAG_AIR_QUALITY);
        list.add(DailyTrendDisplay.TAG_WIND);
        list.add(DailyTrendDisplay.TAG_UV_INDEX);
        list.add(DailyTrendDisplay.TAG_PRECIPITATION);

        String value = "Name, Name, Name, Name, Name";

        Assert.assertThat(DailyTrendDisplay.getSummary(context, list), is(value));
    }
}
