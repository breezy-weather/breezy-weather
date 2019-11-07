package wangdaye.com.geometricweather.basic.model.option.appearance;

import android.content.Context;

import androidx.annotation.StringRes;

import wangdaye.com.geometricweather.R;

public enum CardDisplay {

    CARD_DAILY_OVERVIEW("daily_overview", R.string.daily_overview),
    CARD_HOURLY_OVERVIEW("hourly_overview", R.string.hourly_overview),
    CARD_AIR_QUALITY("air_quality", R.string.air_quality),
    CARD_ALLERGEN("allergen", R.string.allergen),
    CARD_SUNRISE_SUNSET("sunrise_sunset", R.string.sunrise_sunset),
    CARD_LIFE_DETAILS("life_details", R.string.life_details);

    private String value;
    private @StringRes int nameId;

    CardDisplay(String value, int nameId) {
        this.value = value;
        this.nameId = nameId;
    }

    public String getCardValue() {
        return value;
    }

    public String getCardName(Context context) {
        return context.getString(nameId);
    }
}
