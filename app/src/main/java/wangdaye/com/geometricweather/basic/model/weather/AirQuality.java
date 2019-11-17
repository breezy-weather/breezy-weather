package wangdaye.com.geometricweather.basic.model.weather;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;

/**
 * DailyAirQuality quality.
 *
 * default unit : {@link wangdaye.com.geometricweather.basic.model.option.unit.AirQualityUnit#MUGPCUM}
 * */
public class AirQuality {

    @Nullable private String aqiText;
    @Nullable private Integer aqiIndex;
    @Nullable private Float pm25;
    @Nullable private Float pm10;
    @Nullable private Float so2;
    @Nullable private Float no2;
    @Nullable private Float o3;
    @Nullable private Float co;

    public static final int AQI_INDEX_1 = 50;
    public static final int AQI_INDEX_2 = 100;
    public static final int AQI_INDEX_3 = 150;
    public static final int AQI_INDEX_4 = 200;
    public static final int AQI_INDEX_5 = 300;

    public AirQuality(@Nullable String aqiText,
                      @Nullable Integer aqiIndex,
                      @Nullable Float pm25,
                      @Nullable Float pm10,
                      @Nullable Float so2,
                      @Nullable Float no2,
                      @Nullable Float o3,
                      @Nullable Float co) {
        this.aqiText = aqiText;
        this.aqiIndex = aqiIndex;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.so2 = so2;
        this.no2 = no2;
        this.o3 = o3;
        this.co = co;
    }

    @Nullable
    public String getAqiText() {
        return aqiText;
    }

    @Nullable
    public Integer getAqiIndex() {
        return aqiIndex;
    }

    @Nullable
    public Float getPM25() {
        return pm25;
    }

    @Nullable
    public Float getPM10() {
        return pm10;
    }

    @Nullable
    public Float getSO2() {
        return so2;
    }

    @Nullable
    public Float getNO2() {
        return no2;
    }

    @Nullable
    public Float getO3() {
        return o3;
    }

    @Nullable
    public Float getCO() {
        return co;
    }

    @ColorInt
    public int getAqiColor(Context context) {
        if (aqiIndex == null) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (aqiIndex <= AQI_INDEX_1) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (aqiIndex <= AQI_INDEX_2) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (aqiIndex <= AQI_INDEX_3) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (aqiIndex <= AQI_INDEX_4) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (aqiIndex <= AQI_INDEX_5) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public int getPm25Color(Context context) {
        if (pm25 == null) {
            return Color.TRANSPARENT;
        } else if (pm25 <= 35) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (pm25 <= 75) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (pm25 <= 115) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (pm25 <= 150) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (pm25 <= 250) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public int getPm10Color(Context context) {
        if (pm10 == null) {
            return Color.TRANSPARENT;
        } else if (pm10 <= 50) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (pm10 <= 150) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (pm10 <= 250) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (pm10 <= 350) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (pm10 <= 420) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public int getSo2Color(Context context) {
        if (so2 == null) {
            return Color.TRANSPARENT;
        } else if (so2 <= 50) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (so2 <= 150) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (so2 <= 475) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (so2 <= 800) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (so2 <= 1600) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public int getNo2Color(Context context) {
        if (no2 == null) {
            return Color.TRANSPARENT;
        } else if (no2 <= 40) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (no2 <= 80) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (no2 <= 180) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (no2 <= 280) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (no2 <= 565) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public int getO3Color(Context context) {
        if (o3 == null) {
            return Color.TRANSPARENT;
        } else if (o3 <= 160) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (o3 <= 200) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (o3 <= 300) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (o3 <= 400) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (o3 <= 800) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    @ColorInt
    public int getCOColor(Context context) {
        if (co == null) {
            return Color.TRANSPARENT;
        } else if (co <= 5) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (co <= 10) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (co <= 35) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (co <= 60) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (co <= 90) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    public boolean isValid() {
        return aqiIndex != null
                || aqiText != null
                || pm25 != null
                || pm10 != null
                || so2 != null
                || no2 != null
                || o3 != null
                || co != null;
    }

    public boolean isValidIndex() {
        return aqiIndex != null && aqiIndex > 0;
    }
}
