package basic.option.appearance;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import android.content.Context;
import android.text.TextUtils;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.options.appearance.CardDisplay;

//@PrepareForTest(TextUtils.class)
public class CardDisplayTest {

    @BeforeAll
    public static void setup() throws Exception {
        try (MockedStatic<TextUtils> utilities = mockStatic(TextUtils.class)) {
            utilities.when(() -> TextUtils.isEmpty(anyString()))
                    .thenReturn(false);

            assertFalse(TextUtils.isEmpty(anyString()));
        }
    }

    @Test
    public void toCardDisplayList() {
        String value = "daily_overview&hourly_overview&air_quality&allergen&life_details&sunrise_sunset";

        List<CardDisplay> list = CardDisplay.toCardDisplayList(value);

        assertEquals(list.get(0), CardDisplay.CARD_DAILY_OVERVIEW);
        assertEquals(list.get(1), CardDisplay.CARD_HOURLY_OVERVIEW);
        assertEquals(list.get(2), CardDisplay.CARD_AIR_QUALITY);
        assertEquals(list.get(3), CardDisplay.CARD_ALLERGEN);
        assertEquals(list.get(4), CardDisplay.CARD_LIFE_DETAILS);
        assertEquals(list.get(5), CardDisplay.CARD_SUNRISE_SUNSET);
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

        assertEquals(CardDisplay.toValue(list), value);
    }

    @Test
    public void getSummary() {
        Context context = mock(Context.class);
        doReturn("Name").when(context).getString(anyInt());

        List<CardDisplay> list = new ArrayList<>();
        list.add(CardDisplay.CARD_DAILY_OVERVIEW);
        list.add(CardDisplay.CARD_HOURLY_OVERVIEW);
        list.add(CardDisplay.CARD_AIR_QUALITY);
        list.add(CardDisplay.CARD_ALLERGEN);
        list.add(CardDisplay.CARD_LIFE_DETAILS);
        list.add(CardDisplay.CARD_SUNRISE_SUNSET);

        String value = "Name, Name, Name, Name, Name, Name";

        MatcherAssert.assertThat(CardDisplay.getSummary(context, list), is(value));
    }
}
