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

    ACCU("accu", -0x10a7dd, "accuweather.com"),
    OWM("owm", -0x1491b5, "openweathermap.org"),
    METNO("metno", -0xdba791, "met.no / nominatim.org"),
    MF("mf", -0xffa76e, "meteofrance.com"),
    CAIYUN("caiyun", -0xa14472, " caiyunapp.com");

    companion object {

        @JvmStatic
        fun getInstance(
            value: String
        ): WeatherSource {
            if (value.lowercase().contains("metno")) {
                return METNO
            }
            if (value.lowercase().contains("owm")) {
                return OWM
            }
            if (value.lowercase().contains("mf")) {
                return MF
            }
            if (value.lowercase().contains("caiyun")
                || value.lowercase().contains("cn")) {
                return CAIYUN
            }
            return ACCU
        }
    }

    override val valueArrayId = R.array.weather_source_values
    override val nameArrayId = R.array.weather_sources
    override val voiceArrayId = R.array.weather_source_voices

    override fun getName(context: Context) = Utils.getName(context, this)
    override fun getVoice(context: Context) = Utils.getVoice(context, this)
}