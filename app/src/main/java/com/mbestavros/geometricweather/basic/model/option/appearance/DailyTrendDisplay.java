package com.mbestavros.geometricweather.basic.model.option.appearance;

import android.content.Context;

import androidx.annotation.StringRes;

import com.mbestavros.geometricweather.R;

public enum DailyTrendDisplay {

    TAG_TEMPERATURE("temperature", R.string.temperature),
    TAG_AIR_QUALITY("air_quality", R.string.air_quality),
    TAG_WIND("wind", R.string.wind),
    TAG_UV_INDEX("uv_index", R.string.uv_index),
    TAG_PRECIPITATION("precipitation", R.string.precipitation);

    private String value;
    private @StringRes int nameId;

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
}
