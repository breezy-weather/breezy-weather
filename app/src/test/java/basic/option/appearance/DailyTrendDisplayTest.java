package basic.option.appearance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import android.content.Context;
import android.text.TextUtils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.options.appearance.DailyTrendDisplay;

//@PrepareForTest(TextUtils.class)
public class DailyTrendDisplayTest {

    @BeforeAll
    public static void setup() throws Exception {
        try (MockedStatic<TextUtils> utilities = mockStatic(TextUtils.class)) {
            utilities.when(() -> TextUtils.isEmpty(anyString()))
                .thenReturn(false);

            assertEquals(TextUtils.isEmpty(anyString()), false);
        }
    }

    @Test
    public void toDailyTrendDisplayList() {
        String value = "temperature&air_quality&wind&uv_index&precipitation";

        List<DailyTrendDisplay> list = DailyTrendDisplay.toDailyTrendDisplayList(value);

        assertEquals(list.get(0), DailyTrendDisplay.TAG_TEMPERATURE);
        assertEquals(list.get(1), DailyTrendDisplay.TAG_AIR_QUALITY);
        assertEquals(list.get(2), DailyTrendDisplay.TAG_WIND);
        assertEquals(list.get(3), DailyTrendDisplay.TAG_UV_INDEX);
        assertEquals(list.get(4), DailyTrendDisplay.TAG_PRECIPITATION);
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

        assertEquals(DailyTrendDisplay.toValue(list), value);
    }

    @Test
    public void getSummary() {
        Context context = mock(Context.class);
        doReturn("Name").when(context).getString(anyInt());

        List<DailyTrendDisplay> list = new ArrayList<>();
        list.add(DailyTrendDisplay.TAG_TEMPERATURE);
        list.add(DailyTrendDisplay.TAG_AIR_QUALITY);
        list.add(DailyTrendDisplay.TAG_WIND);
        list.add(DailyTrendDisplay.TAG_UV_INDEX);
        list.add(DailyTrendDisplay.TAG_PRECIPITATION);

        String value = "Name, Name, Name, Name, Name";

        assertThat(DailyTrendDisplay.getSummary(context, list), is(value));
    }
}
