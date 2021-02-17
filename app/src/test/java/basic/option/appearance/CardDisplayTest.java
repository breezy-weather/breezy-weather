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

import wangdaye.com.geometricweather.common.basic.models.options.appearance.CardDisplay;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class CardDisplayTest {

    @BeforeClass
    public static void setup() throws Exception {
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.class, "isEmpty", anyString()).thenReturn(false);
    }

    @Test
    public void toCardDisplayList() {
        String value = "daily_overview&hourly_overview&air_quality&allergen&life_details&sunrise_sunset";

        List<CardDisplay> list = CardDisplay.toCardDisplayList(value);

        Assert.assertEquals(list.get(0), CardDisplay.CARD_DAILY_OVERVIEW);
        Assert.assertEquals(list.get(1), CardDisplay.CARD_HOURLY_OVERVIEW);
        Assert.assertEquals(list.get(2), CardDisplay.CARD_AIR_QUALITY);
        Assert.assertEquals(list.get(3), CardDisplay.CARD_ALLERGEN);
        Assert.assertEquals(list.get(4), CardDisplay.CARD_LIFE_DETAILS);
        Assert.assertEquals(list.get(5), CardDisplay.CARD_SUNRISE_SUNSET);
    }

    @Test
    public void toValue() {
        List<CardDisplay> list = new ArrayList<>();
        list.add(CardDisplay.CARD_DAILY_OVERVIEW);
        list.add(CardDisplay.CARD_HOURLY_OVERVIEW);
        list.add(CardDisplay.CARD_AIR_QUALITY);
        list.add(CardDisplay.CARD_ALLERGEN);
        list.add(CardDisplay.CARD_LIFE_DETAILS);
        list.add(CardDisplay.CARD_SUNRISE_SUNSET);

        String value = "daily_overview&hourly_overview&air_quality&allergen&life_details&sunrise_sunset";

        Assert.assertEquals(CardDisplay.toValue(list), value);
    }

    @Test
    public void getSummary() {
        Context context = PowerMockito.mock(Context.class);
        doReturn("Name").when(context).getString(anyInt());

        List<CardDisplay> list = new ArrayList<>();
        list.add(CardDisplay.CARD_DAILY_OVERVIEW);
        list.add(CardDisplay.CARD_HOURLY_OVERVIEW);
        list.add(CardDisplay.CARD_AIR_QUALITY);
        list.add(CardDisplay.CARD_ALLERGEN);
        list.add(CardDisplay.CARD_LIFE_DETAILS);
        list.add(CardDisplay.CARD_SUNRISE_SUNSET);

        String value = "Name, Name, Name, Name, Name, Name";

        Assert.assertThat(CardDisplay.getSummary(context, list), is(value));
    }
}
