package wangdaye.com.geometricweather.common.basic.models.weather

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import aqikotlin.library.algorithms.Eea
import aqikotlin.library.algorithms.Epa
import aqikotlin.library.algorithms.Mee
import aqikotlin.library.constants.AQI
import aqikotlin.library.constants.EEA
import aqikotlin.library.constants.EPA
import aqikotlin.library.constants.MEE
import aqikotlin.library.constants.POLLUTANT_CO_1H
import aqikotlin.library.constants.POLLUTANT_CO_8H
import aqikotlin.library.constants.POLLUTANT_NO2_1H
import aqikotlin.library.constants.POLLUTANT_O3_1H
import aqikotlin.library.constants.POLLUTANT_PM10
import aqikotlin.library.constants.POLLUTANT_PM25
import aqikotlin.library.constants.POLLUTANT_SO2_1H
import aqikotlin.library.utils.ConcentrationRounded
import wangdaye.com.geometricweather.common.utils.helpers.LogHelper
import wangdaye.com.geometricweather.settings.SettingsManager.Companion.getInstance
import java.io.Serializable
import kotlin.math.round

/**
 * DailyAirQuality quality.
 *
 * default unit : [AirQualityUnit.MUGPCUM],
 * [AirQualityCOUnit.MGPCUM]
 */
class AirQuality(
    val epaIndex: Int? = null,
    val meeIndex: Int? = null,
    val eeaIndex: Int? = null,
    val pM25: Float? = null,
    val pM10: Float? = null,
    val sO2: Float? = null,
    val nO2: Float? = null,
    val o3: Float? = null,
    val cO: Float? = null
) : Serializable {
    companion object {
        const val EPA_INDEX_3 = 150

        private fun getAlgorithm(context: Context) = getInstance(context).airQualityAlgorithmUnit.id

        private fun getConcentrationLevel(context: Context, type: String, level: Int): Int? {
            val algorithm = getAlgorithm(context)
            val algorithmClass = when (algorithm) {
                EPA -> Epa()
                MEE -> Mee()
                EEA -> Eea()
                else -> null
            } ?: return null
            val aqiValues: List<Number> = when (type) {
                AQI, POLLUTANT_PM25, POLLUTANT_PM10, POLLUTANT_O3_1H, POLLUTANT_SO2_1H, POLLUTANT_NO2_1H -> algorithmClass.lists[type]
                POLLUTANT_CO_1H -> when (algorithm) {
                    EPA -> algorithmClass.lists[POLLUTANT_CO_8H]
                    MEE -> algorithmClass.lists[type]
                    EEA -> null
                    else -> null
                }

                else -> null
            } ?: return null

            return aqiValues.getOrNull(Integer.min((level * 2) - 1, aqiValues.size - 1))?.toInt();
        }

        @JvmStatic
        fun getIndexFreshAir(context: Context): Int = getConcentrationLevel(context, AQI, 1) ?: 50

        @JvmStatic
        fun getIndexHighPollution(context: Context): Int = getConcentrationLevel(context, AQI, 3) ?: 150

        @JvmStatic
        fun getIndexExcessivePollution(context: Context): Int = getConcentrationLevel(context, AQI, 5) ?: 300

        @JvmStatic
        fun getPM25ExcessivePollution(context: Context): Int = getConcentrationLevel(context, POLLUTANT_PM25, 5) ?: 250

        @JvmStatic
        fun getPM10ExcessivePollution(context: Context): Int = getConcentrationLevel(context, POLLUTANT_PM10, 5) ?: 420

        @JvmStatic
        fun getSO2ExcessivePollution(context: Context): Int = getConcentrationLevel(context, POLLUTANT_SO2_1H, 5) ?: 1600

        @JvmStatic
        fun getNO2ExcessivePollution(context: Context): Int = getConcentrationLevel(context, POLLUTANT_NO2_1H, 5) ?: 565

        @JvmStatic
        fun getO3ExcessivePollution(context: Context): Int = getConcentrationLevel(context, POLLUTANT_O3_1H, 5) ?: 800

        @JvmStatic
        fun getCOExcessivePollution(context: Context): Int = getConcentrationLevel(context, POLLUTANT_CO_1H, 5) ?: 90
    }

    fun getIndex(context: Context): Int? = when (getAlgorithm(context)) {
        EPA -> epaIndex
        MEE -> meeIndex
        EEA -> eeaIndex
        else -> null
    }

    fun getAqiText(context: Context): String? {
        val algorithm = getAlgorithm(context)
        val value = when (algorithm) {
            EPA -> epaIndex
            MEE -> meeIndex
            EEA -> eeaIndex
            else -> null
        } ?: return null
        val algorithmClass = when (algorithm) {
            EPA -> Epa()
            MEE -> Mee()
            EEA -> Eea()
            else -> null
        } ?: return null

        val aqiValues = algorithmClass.lists[AQI]!!
        for ((i) in aqiValues.withIndex()) {
            if (value in aqiValues[i].toInt()..aqiValues[i + 1].toInt()) {
                return context.resources.getStringArray(
                    getInstance(context).airQualityAlgorithmUnit.levelsArrayId
                ).getOrNull(if (i == 0) 0 else (i / 2))
            }
        }
        return null
    }

    @ColorInt
    private fun getLevelColor(context: Context, type: String): Int {
        val algorithm = getAlgorithm(context)
        val concentration = when (type) {
            AQI -> when (algorithm) {
                EPA -> epaIndex
                MEE -> meeIndex
                EEA -> eeaIndex
                else -> null
            }

            POLLUTANT_PM25 -> pM25
            POLLUTANT_PM10 -> pM10
            POLLUTANT_O3_1H -> o3
            POLLUTANT_SO2_1H -> sO2
            POLLUTANT_NO2_1H -> nO2
            POLLUTANT_CO_1H -> cO
            else -> null
        } ?: return Color.TRANSPARENT
        val algorithmClass = when (algorithm) {
            EPA -> Epa()
            MEE -> Mee()
            EEA -> Eea()
            else -> null
        } ?: return Color.TRANSPARENT
        val aqiValues = when (type) {
            AQI, POLLUTANT_PM25, POLLUTANT_PM10, POLLUTANT_O3_1H, POLLUTANT_SO2_1H, POLLUTANT_NO2_1H -> algorithmClass.lists[type]
            POLLUTANT_CO_1H -> when (algorithm) {
                EPA -> algorithmClass.lists[POLLUTANT_CO_8H]
                MEE -> algorithmClass.lists[type]
                EEA -> null
                else -> null
            }

            else -> null
        } ?: return Color.TRANSPARENT
        val valueConverted = if (algorithm == EPA && type != AQI) {
            ConcentrationRounded(
                pollutantCode = when (type) {
                    POLLUTANT_PM25, POLLUTANT_PM10, POLLUTANT_O3_1H, POLLUTANT_SO2_1H, POLLUTANT_NO2_1H -> type
                    POLLUTANT_CO_1H -> POLLUTANT_CO_8H
                    else -> "" // Never happens
                },
                pollutantConcentration = concentration.toDouble(),
                algorithm = algorithm,
                convertIfRequired = true
            ).getRoundedConcentrationOnPollutantCode()
        } else {
            round(concentration.toDouble()).toInt()
        }

        for ((i) in aqiValues.withIndex()) {
            try {
                if (valueConverted.toDouble() in aqiValues[i].toDouble()..aqiValues[i + 1].toDouble()) {
                    return context.resources.getIntArray(
                        getInstance(context).airQualityAlgorithmUnit.colorsArrayId
                    ).getOrNull(if (i == 0) 0 else (i / 2)) ?: Color.TRANSPARENT
                }
            } catch (ignored: IndexOutOfBoundsException) {
                LogHelper.log("Air quality pollutant out of bounds")
                return context.resources.getIntArray(
                    getInstance(context).airQualityAlgorithmUnit.colorsArrayId
                )[context.resources.getIntArray(
                    getInstance(context).airQualityAlgorithmUnit.colorsArrayId
                ).size - 1]
            }
        }
        return Color.TRANSPARENT
    }

    @ColorInt
    fun getAqiColor(context: Context): Int = getLevelColor(context, AQI)

    @ColorInt
    fun getPm25Color(context: Context): Int = getLevelColor(context, POLLUTANT_PM25)

    @ColorInt
    fun getPm10Color(context: Context): Int = getLevelColor(context, POLLUTANT_PM10)

    @ColorInt
    fun getSo2Color(context: Context): Int = getLevelColor(context, POLLUTANT_SO2_1H)

    @ColorInt
    fun getNo2Color(context: Context): Int = getLevelColor(context, POLLUTANT_NO2_1H)

    @ColorInt
    fun getO3Color(context: Context): Int = getLevelColor(context, POLLUTANT_O3_1H)

    @ColorInt
    fun getCOColor(context: Context): Int = getLevelColor(context, POLLUTANT_CO_1H)

    val isValid: Boolean
        get() = epaIndex != null || pM25 != null || pM10 != null || sO2 != null || nO2 != null || o3 != null || cO != null
    val isValidIndex: Boolean
        get() = epaIndex != null && epaIndex > 0
}
