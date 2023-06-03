package wangdaye.com.geometricweather.common.basic.models.weather

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.settings.SettingsManager.Companion.getInstance
import java.io.Serializable

/**
 * DailyAirQuality quality.
 *
 * default unit : [AirQualityUnit.MUGPCUM],
 * [AirQualityCOUnit.MGPCUM]
 */
class AirQuality(
    val aqiIndex: Int? = null,
    val pM25: Float? = null,
    val pM10: Float? = null,
    val sO2: Float? = null,
    val nO2: Float? = null,
    val o3: Float? = null,
    val cO: Float? = null
) : Serializable {
    companion object {
        const val AQI_INDEX_1 = 50
        const val AQI_INDEX_2 = 100
        const val AQI_INDEX_3 = 150
        const val AQI_INDEX_4 = 200
        const val AQI_INDEX_5 = 300
    }

    fun getLevelName(context: Context, level: Int): String {
        return if (level > 0 && level < context.resources.getIntArray(
                getInstance(
                    context
                ).airQualityLevelUnit.colorsArrayId
            ).size
        ) {
            context.resources.getStringArray(
                getInstance(
                    context
                ).airQualityLevelUnit.levelsArrayId
            )[level - 1]
        } else {
            context.resources.getStringArray(
                getInstance(
                    context
                ).airQualityLevelUnit.levelsArrayId
            )[0]
        }
    }

    fun getAqiText(context: Context): String? {
        return when (aqiIndex) {
            in 0..AQI_INDEX_1 -> getLevelName(context, 1)
            in AQI_INDEX_1..AQI_INDEX_2 -> getLevelName(context, 2)
            in AQI_INDEX_2..AQI_INDEX_3 -> getLevelName(context, 3)
            in AQI_INDEX_3..AQI_INDEX_4 -> getLevelName(context, 4)
            in AQI_INDEX_4..AQI_INDEX_5 -> getLevelName(context, 5)
            in AQI_INDEX_5..Int.MAX_VALUE -> getLevelName(context, 6)
            else -> null
        }
    }

    @ColorInt
    fun getLevelColor(context: Context, level: Int): Int {
        return if (level > 0 && level < context.resources.getIntArray(
                getInstance(
                    context
                ).airQualityLevelUnit.colorsArrayId
            ).size
        ) {
            context.resources.getIntArray(
                getInstance(
                    context
                ).airQualityLevelUnit.colorsArrayId
            )[level - 1]
        } else {
            Color.TRANSPARENT
        }
    }

    @ColorInt
    fun getLevelColorForPolluant(
        context: Context,
        polluantValue: Float?,
        polluantArrayId: Int
    ): Int {
        if (polluantValue == null) {
            return Color.TRANSPARENT
        }
        var level = 0
        for (i in context.resources.getIntArray(polluantArrayId).indices) {
            if (polluantValue > context.resources.getIntArray(polluantArrayId)[i]) {
                level = i
            }
        }
        return context.resources.getIntArray(
            getInstance(
                context
            ).airQualityLevelUnit.colorsArrayId
        )[level]
    }

    @ColorInt
    fun getAqiColor(context: Context): Int {
        return when (aqiIndex) {
            in 0..AQI_INDEX_1 -> getLevelColor(context, 1)
            in AQI_INDEX_1..AQI_INDEX_2 -> getLevelColor(context, 2)
            in AQI_INDEX_2..AQI_INDEX_3 -> getLevelColor(context, 3)
            in AQI_INDEX_3..AQI_INDEX_4 -> getLevelColor(context, 4)
            in AQI_INDEX_4..AQI_INDEX_5 -> getLevelColor(context, 5)
            in AQI_INDEX_5..Int.MAX_VALUE -> getLevelColor(context, 6)
            else -> Color.TRANSPARENT
        }
    }

    @ColorInt
    fun getPm25Color(context: Context): Int {
        return getLevelColorForPolluant(
            context,
            pM25,
            getInstance(context).airQualityLevelUnit.pm25valuesArrayId
        )
    }

    @ColorInt
    fun getPm10Color(context: Context): Int {
        return getLevelColorForPolluant(
            context,
            pM10,
            getInstance(context).airQualityLevelUnit.pm10valuesArrayId
        )
    }

    @ColorInt
    fun getSo2Color(context: Context): Int {
        return getLevelColorForPolluant(
            context,
            sO2,
            getInstance(context).airQualityLevelUnit.so2valuesArrayId
        )
    }

    @ColorInt
    fun getNo2Color(context: Context): Int {
        return getLevelColorForPolluant(
            context,
            nO2,
            getInstance(context).airQualityLevelUnit.no2valuesArrayId
        )
    }

    @ColorInt
    fun getO3Color(context: Context): Int {
        return getLevelColorForPolluant(
            context,
            o3,
            getInstance(context).airQualityLevelUnit.o3valuesArrayId
        )
    }

    @ColorInt
    fun getCOColor(context: Context): Int {
        return if (cO == null) {
            Color.TRANSPARENT
        } else if (cO <= 5) {
            ContextCompat.getColor(context, R.color.colorLevel_1)
        } else if (cO <= 10) {
            ContextCompat.getColor(context, R.color.colorLevel_2)
        } else if (cO <= 35) {
            ContextCompat.getColor(context, R.color.colorLevel_3)
        } else if (cO <= 60) {
            ContextCompat.getColor(context, R.color.colorLevel_4)
        } else if (cO <= 90) {
            ContextCompat.getColor(context, R.color.colorLevel_5)
        } else {
            ContextCompat.getColor(context, R.color.colorLevel_6)
        }
    }

    fun getPM25Max(context: Context): Int {
        val arrayId = getInstance(context).airQualityLevelUnit.pm25valuesArrayId
        return context.resources.getIntArray(arrayId)[context.resources.getIntArray(arrayId).size - 1]
    }

    fun getPM10Max(context: Context): Int {
        val arrayId = getInstance(context).airQualityLevelUnit.pm10valuesArrayId
        return context.resources.getIntArray(arrayId)[context.resources.getIntArray(arrayId).size - 1]
    }

    fun getSO2Max(context: Context): Int {
        val arrayId = getInstance(context).airQualityLevelUnit.no2valuesArrayId
        return context.resources.getIntArray(arrayId)[context.resources.getIntArray(arrayId).size - 1]
    }

    fun getNO2Max(context: Context): Int {
        val arrayId = getInstance(context).airQualityLevelUnit.no2valuesArrayId
        return context.resources.getIntArray(arrayId)[context.resources.getIntArray(arrayId).size - 1]
    }

    fun getO3Max(context: Context): Int {
        val arrayId = getInstance(context).airQualityLevelUnit.o3valuesArrayId
        return context.resources.getIntArray(arrayId)[context.resources.getIntArray(arrayId).size - 1]
    }

    val isValid: Boolean
        get() = aqiIndex != null || pM25 != null || pM10 != null || sO2 != null || nO2 != null || o3 != null || cO != null
    val isValidIndex: Boolean
        get() = aqiIndex != null && aqiIndex > 0
}
