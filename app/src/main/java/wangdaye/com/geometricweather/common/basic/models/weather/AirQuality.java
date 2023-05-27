package wangdaye.com.geometricweather.common.basic.models.weather;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.Serializable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options.unit.AirQualityCOUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.AirQualityUnit;
import wangdaye.com.geometricweather.settings.SettingsManager;

/**
 * DailyAirQuality quality.
 *
 * default unit : {@link AirQualityUnit#MUGPCUM},
 *                {@link AirQualityCOUnit#MGPCUM}
 * */
public class AirQuality implements Serializable {

    @Nullable private final String aqiText;
    @Nullable private final Integer aqiIndex;
    @Nullable private final Float pm25;
    @Nullable private final Float pm10;
    @Nullable private final Float so2;
    @Nullable private final Float no2;
    @Nullable private final Float o3;
    @Nullable private final Float co;

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

    public String getLevelName(Context context, int level) {
        if (level > 0 && level < context.getResources().getIntArray(SettingsManager.getInstance(context).getAirQualityLevelUnit().getColorsArrayId()).length) {
            return context.getResources().getStringArray(SettingsManager.getInstance(context).getAirQualityLevelUnit().getLevelsArrayId())[level - 1];
        } else {
            return context.getResources().getStringArray(SettingsManager.getInstance(context).getAirQualityLevelUnit().getLevelsArrayId())[0];
        }
    }

    @Nullable
    public String getAqiText(Context context) {
        if (aqiIndex == null) {
            return null;
        } else if (aqiIndex <= AQI_INDEX_1) {
            return this.getLevelName(context, 1);
        } else if (aqiIndex <= AQI_INDEX_2) {
            return this.getLevelName(context, 2);
        } else if (aqiIndex <= AQI_INDEX_3) {
            return this.getLevelName(context, 3);
        } else if (aqiIndex <= AQI_INDEX_4) {
            return this.getLevelName(context, 4);
        } else if (aqiIndex <= AQI_INDEX_5) {
            return this.getLevelName(context, 5);
        } else {
            return this.getLevelName(context, 6);
        }
    }

    /**
     * @deprecated Use getAqiText(Context context) instead
     */
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

    @ColorInt public int getLevelColor(Context context, int level) {
        if (level > 0 && level < context.getResources().getIntArray(SettingsManager.getInstance(context).getAirQualityLevelUnit().getColorsArrayId()).length) {
            return context.getResources().getIntArray(SettingsManager.getInstance(context).getAirQualityLevelUnit().getColorsArrayId())[level - 1];
        } else {
            return Color.TRANSPARENT;
        }
    }

    @ColorInt public int getLevelColorForPolluant(Context context, Float polluantValue, int polluantArrayId) {
        if (polluantValue == null) {
            return Color.TRANSPARENT;
        }

        int level = 0;
        for (int i = 0; i < context.getResources().getIntArray(polluantArrayId).length; ++i) {
            if (polluantValue > context.getResources().getIntArray(polluantArrayId)[i]) {
                level = i;
            }
        }

        return context.getResources().getIntArray(SettingsManager.getInstance(context).getAirQualityLevelUnit().getColorsArrayId())[level];
    }

    @ColorInt
    public int getAqiColor(Context context) {
        if (aqiIndex == null) {
            return this.getLevelColor(context, 1);
        } else if (aqiIndex <= AQI_INDEX_1) {
            return this.getLevelColor(context, 1);
        } else if (aqiIndex <= AQI_INDEX_2) {
            return this.getLevelColor(context, 2);
        } else if (aqiIndex <= AQI_INDEX_3) {
            return this.getLevelColor(context, 3);
        } else if (aqiIndex <= AQI_INDEX_4) {
            return this.getLevelColor(context, 4);
        } else if (aqiIndex <= AQI_INDEX_5) {
            return this.getLevelColor(context, 5);
        } else {
            return this.getLevelColor(context, 6);
        }
    }

    @ColorInt
    public int getPm25Color(Context context) {
        return getLevelColorForPolluant(context, pm25, SettingsManager.getInstance(context).getAirQualityLevelUnit().getPm25valuesArrayId());
    }

    @ColorInt
    public int getPm10Color(Context context) {
        return getLevelColorForPolluant(context, pm10, SettingsManager.getInstance(context).getAirQualityLevelUnit().getPm10valuesArrayId());
    }

    @ColorInt
    public int getSo2Color(Context context) {
        return getLevelColorForPolluant(context, so2, SettingsManager.getInstance(context).getAirQualityLevelUnit().getSo2valuesArrayId());
    }

    @ColorInt
    public int getNo2Color(Context context) {
        return getLevelColorForPolluant(context, no2, SettingsManager.getInstance(context).getAirQualityLevelUnit().getNo2valuesArrayId());
    }

    @ColorInt
    public int getO3Color(Context context) {
        return getLevelColorForPolluant(context, o3, SettingsManager.getInstance(context).getAirQualityLevelUnit().getO3valuesArrayId());
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

    public int getPM25Max(Context context) {
        int arrayId = SettingsManager.getInstance(context).getAirQualityLevelUnit().getPm25valuesArrayId();
        return context.getResources().getIntArray(arrayId)[context.getResources().getIntArray(arrayId).length - 1];
    }

    public int getPM10Max(Context context) {
        int arrayId = SettingsManager.getInstance(context).getAirQualityLevelUnit().getPm10valuesArrayId();
        return context.getResources().getIntArray(arrayId)[context.getResources().getIntArray(arrayId).length - 1];
    }

    public int getSO2Max(Context context) {
        int arrayId = SettingsManager.getInstance(context).getAirQualityLevelUnit().getNo2valuesArrayId();
        return context.getResources().getIntArray(arrayId)[context.getResources().getIntArray(arrayId).length - 1];
    }

    public int getNO2Max(Context context) {
        int arrayId = SettingsManager.getInstance(context).getAirQualityLevelUnit().getNo2valuesArrayId();
        return context.getResources().getIntArray(arrayId)[context.getResources().getIntArray(arrayId).length - 1];
    }

    public int getO3Max(Context context) {
        int arrayId = SettingsManager.getInstance(context).getAirQualityLevelUnit().getO3valuesArrayId();
        return context.getResources().getIntArray(arrayId)[context.getResources().getIntArray(arrayId).length - 1];
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
