package wangdaye.com.geometricweather.common.basic.models.options.provider

import android.content.Context
import androidx.annotation.ColorInt
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options._basic.Utils
import wangdaye.com.geometricweather.common.basic.models.options._basic.VoiceEnum

enum class WeatherSource(
    override val id: String,
    @ColorInt val sourceColor: Int,
    val sourceUrl: String
): VoiceEnum {

    OPEN_METEO("openmeteo", -0x0077ff, "Open-Meteo.com CC BY 4.0"),
    ACCU("accu", -0x10a7dd, "accuweather.com"),
    METNO("metno", -0xdba791, "met.no / nominatim.org"),
    OWM("owm", -0x1491b5, "openweathermap.org"),
    MF("mf", -0xffa76e, "meteofrance.com"),
    CAIYUN("caiyun", -0xa14472, "caiyunapp.com");

    companion object {

        @JvmStatic
        fun getInstance(
            value: String?
        ): WeatherSource {
            return with (value) {
                when {
                    equals("openmeteo", ignoreCase = true) -> OPEN_METEO
                    equals("accu", ignoreCase = true) -> ACCU
                    equals("metno", ignoreCase = true) -> METNO
                    equals("owm", ignoreCase = true) -> OWM
                    equals("mf", ignoreCase = true) -> MF
                    equals("caiyun", ignoreCase = true) || equals("cn", ignoreCase = true) -> CAIYUN
                    else -> ACCU
                }
            }
        }
    }

    override val valueArrayId = R.array.weather_source_values
    override val nameArrayId = R.array.weather_sources
    override val voiceArrayId = R.array.weather_source_voices

    override fun getName(context: Context) = Utils.getName(context, this)
    override fun getVoice(context: Context) = Utils.getVoice(context, this)
}