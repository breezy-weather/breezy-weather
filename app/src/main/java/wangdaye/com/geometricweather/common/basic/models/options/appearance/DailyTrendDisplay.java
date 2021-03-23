package wangdaye.com.geometricweather.common.basic.models.options.appearance;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;

public enum DailyTrendDisplay {

    TAG_TEMPERATURE("temperature", R.string.temperature),
    TAG_AIR_QUALITY("air_quality", R.string.air_quality),
    TAG_WIND("wind", R.string.wind),
    TAG_UV_INDEX("uv_index", R.string.uv_index),
    TAG_PRECIPITATION("precipitation", R.string.precipitation);

    private final String value;
    private @StringRes final int nameId;

    DailyTrendDisplay(String value, int nameId) {
        this.value = value;
        this.nameId = nameId;
    }

    public String getTagValue() {
        return value;
    }

    public String getTagName(Context context) {
        return context.getString(nameId);
    }

    @NonNull
    public static List<DailyTrendDisplay> toDailyTrendDisplayList(String value) {
        if (TextUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        try {
            String[] cards = value.split("&");

            List<DailyTrendDisplay> list = new ArrayList<>();
            for (String card : cards) {
                switch (card) {
                    case "temperature":
                        list.add(TAG_TEMPERATURE);
                        break;

                    case "air_quality":
                        list.add(TAG_AIR_QUALITY);
                        break;

                    case "wind":
                        list.add(TAG_WIND);
                        break;

                    case "uv_index":
                        list.add(TAG_UV_INDEX);
                        break;

                    case "precipitation":
                        list.add(TAG_PRECIPITATION);
                        break;
                }
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @NonNull
    public static String toValue(@NonNull List<DailyTrendDisplay> list) {
        StringBuilder builder = new StringBuilder();
        for (DailyTrendDisplay v : list) {
            builder.append("&").append(v.getTagValue());
        }
        if (builder.length() > 0 && builder.charAt(0) == '&') {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    @NonNull
    public static String getSummary(Context context, @NonNull List<DailyTrendDisplay> list) {
        StringBuilder builder = new StringBuilder();
        for (DailyTrendDisplay v : list) {
            builder.append(",").append(v.getTagName(context));
        }
        if (builder.length() > 0 && builder.charAt(0) == ',') {
            builder.deleteCharAt(0);
        }
        return builder.toString().replace(",", ", ");
    }
}
