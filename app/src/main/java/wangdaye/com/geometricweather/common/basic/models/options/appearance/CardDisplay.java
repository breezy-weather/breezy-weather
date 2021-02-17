package wangdaye.com.geometricweather.common.basic.models.options.appearance;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;

public enum CardDisplay {

    CARD_DAILY_OVERVIEW("daily_overview", R.string.daily_overview),
    CARD_HOURLY_OVERVIEW("hourly_overview", R.string.hourly_overview),
    CARD_AIR_QUALITY("air_quality", R.string.air_quality),
    CARD_ALLERGEN("allergen", R.string.allergen),
    CARD_SUNRISE_SUNSET("sunrise_sunset", R.string.sunrise_sunset),
    CARD_LIFE_DETAILS("life_details", R.string.life_details);

    private final String value;
    private @StringRes final int nameId;

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

    @NonNull
    public static List<CardDisplay> toCardDisplayList(String value) {
        if (TextUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        try {
            String[] cards = value.split("&");

            List<CardDisplay> list = new ArrayList<>();
            for (String card : cards) {
                switch (card) {
                    case "daily_overview":
                        list.add(CARD_DAILY_OVERVIEW);
                        break;

                    case "hourly_overview":
                        list.add(CARD_HOURLY_OVERVIEW);
                        break;

                    case "air_quality":
                        list.add(CARD_AIR_QUALITY);
                        break;

                    case "allergen":
                        list.add(CARD_ALLERGEN);
                        break;

                    case "life_details":
                        list.add(CARD_LIFE_DETAILS);
                        break;

                    case "sunrise_sunset":
                        list.add(CARD_SUNRISE_SUNSET);
                        break;
                }
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @NonNull
    public static String toValue(@NonNull List<CardDisplay> list) {
        StringBuilder builder = new StringBuilder();
        for (CardDisplay v : list) {
            builder.append("&").append(v.getCardValue());
        }
        if (builder.length() > 0 && builder.charAt(0) == '&') {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    @NonNull
    public static String getSummary(Context context, @NonNull List<CardDisplay> list) {
        StringBuilder builder = new StringBuilder();
        for (CardDisplay item : list) {
            builder.append(",").append(item.getCardName(context));
        }
        if (builder.length() > 0 && builder.charAt(0) == ',') {
            builder.deleteCharAt(0);
        }
        return builder.toString().replace(",", ", ");
    }
}
