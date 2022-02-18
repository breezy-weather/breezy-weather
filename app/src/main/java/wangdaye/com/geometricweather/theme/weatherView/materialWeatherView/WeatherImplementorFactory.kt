package wangdaye.com.geometricweather.theme.weatherView.materialWeatherView

import android.content.Context
import android.graphics.Color
import wangdaye.com.geometricweather.theme.weatherView.WeatherView.WeatherKindRule
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import wangdaye.com.geometricweather.theme.weatherView.WeatherView
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor.SunImplementor
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor.MeteorShowerImplementor
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor.HailImplementor
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor.RainImplementor
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor.SnowImplementor
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor.WindImplementor
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Size
import wangdaye.com.geometricweather.R

object WeatherImplementorFactory {

    @JvmStatic
    fun getWeatherImplementor(
        @WeatherKindRule weatherKind: Int,
        daytime: Boolean,
        @Size(2) sizes: IntArray?
    ): WeatherAnimationImplementor? = when (weatherKind) {
        WeatherView.WEATHER_KIND_CLEAR -> if (daytime) {
            SunImplementor(sizes)
        } else {
            MeteorShowerImplementor(sizes)
        }

        WeatherView.WEATHER_KIND_CLOUDY -> if (daytime) {
            CloudImplementor(sizes, CloudImplementor.TYPE_CLOUDY_DAY)
        } else {
            CloudImplementor(sizes, CloudImplementor.TYPE_CLOUDY_NIGHT)
        }

        WeatherView.WEATHER_KIND_CLOUD -> if (daytime) {
            CloudImplementor(sizes, CloudImplementor.TYPE_CLOUD_DAY)
        } else {
            CloudImplementor(sizes, CloudImplementor.TYPE_CLOUD_NIGHT)
        }

        WeatherView.WEATHER_KIND_FOG ->
            CloudImplementor(sizes, CloudImplementor.TYPE_FOG)

        WeatherView.WEATHER_KIND_HAIL -> if (daytime) {
            HailImplementor(sizes, HailImplementor.TYPE_HAIL_DAY)
        } else {
            HailImplementor(sizes, HailImplementor.TYPE_HAIL_NIGHT)
        }

        WeatherView.WEATHER_KIND_HAZE ->
            CloudImplementor(sizes, CloudImplementor.TYPE_HAZE)

        WeatherView.WEATHER_KIND_RAINY -> if (daytime) {
            RainImplementor(sizes, RainImplementor.TYPE_RAIN_DAY)
        } else {
            RainImplementor(sizes, RainImplementor.TYPE_RAIN_NIGHT)
        }

        WeatherView.WEATHER_KIND_SNOW -> if (daytime) {
            SnowImplementor(sizes, SnowImplementor.TYPE_SNOW_DAY)
        } else {
            SnowImplementor(sizes, SnowImplementor.TYPE_SNOW_NIGHT)
        }

        WeatherView.WEATHER_KIND_THUNDERSTORM -> RainImplementor(
            sizes,
            RainImplementor.TYPE_THUNDERSTORM
        )

        WeatherView.WEATHER_KIND_THUNDER -> CloudImplementor(
            sizes,
            CloudImplementor.TYPE_THUNDER
        )

        WeatherView.WEATHER_KIND_WIND ->
            WindImplementor(sizes)

        WeatherView.WEATHER_KIND_SLEET -> if (daytime) {
            RainImplementor(sizes, RainImplementor.TYPE_SLEET_DAY)
        } else {
            RainImplementor(sizes, RainImplementor.TYPE_SLEET_NIGHT)
        }

        else -> null
    }

    @JvmStatic
    @ColorInt
    fun getWeatherThemeColor(
        context: Context?,
        @WeatherKindRule weatherKind: Int,
        daytime: Boolean
    ): Int = when (weatherKind) {
        WeatherView.WEATHER_KIND_CLEAR -> if (daytime) {
            SunImplementor.getThemeColor()
        } else {
            MeteorShowerImplementor.getThemeColor()
        }

        WeatherView.WEATHER_KIND_CLOUDY -> if (daytime) {
            CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUDY_DAY)
        } else {
            CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUDY_NIGHT)
        }

        WeatherView.WEATHER_KIND_CLOUD -> if (daytime) {
            CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUD_DAY)
        } else {
            CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUD_NIGHT)
        }

        WeatherView.WEATHER_KIND_FOG ->
            CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_FOG)

        WeatherView.WEATHER_KIND_HAIL -> if (daytime) {
            HailImplementor.getThemeColor(context, HailImplementor.TYPE_HAIL_DAY)
        } else {
            HailImplementor.getThemeColor(context, HailImplementor.TYPE_HAIL_NIGHT)
        }

        WeatherView.WEATHER_KIND_HAZE ->
            CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_HAZE)

        WeatherView.WEATHER_KIND_RAINY -> if (daytime) {
            RainImplementor.getThemeColor(context, RainImplementor.TYPE_RAIN_DAY)
        } else {
            RainImplementor.getThemeColor(context, RainImplementor.TYPE_RAIN_NIGHT)
        }

        WeatherView.WEATHER_KIND_SLEET -> if (daytime) {
            RainImplementor.getThemeColor(context, RainImplementor.TYPE_SLEET_DAY)
        } else {
            RainImplementor.getThemeColor(context, RainImplementor.TYPE_SLEET_NIGHT)
        }

        WeatherView.WEATHER_KIND_SNOW -> if (daytime) {
            SnowImplementor.getThemeColor(context, SnowImplementor.TYPE_SNOW_DAY)
        } else {
            SnowImplementor.getThemeColor(context, SnowImplementor.TYPE_SNOW_NIGHT)
        }

        WeatherView.WEATHER_KIND_THUNDERSTORM ->
            RainImplementor.getThemeColor(context, RainImplementor.TYPE_THUNDERSTORM)

        WeatherView.WEATHER_KIND_THUNDER ->
            CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_THUNDER)

        WeatherView.WEATHER_KIND_WIND ->
            WindImplementor.getThemeColor()

        else -> Color.TRANSPARENT
    }

    @JvmStatic
    @DrawableRes
    fun getBackgroundId(
        @WeatherKindRule weatherKind: Int,
        daylight: Boolean,
    ): Int = when (weatherKind) {
        WeatherView.WEATHER_KIND_CLEAR -> if (daylight) {
            R.drawable.weather_background_clear_day
        } else {
            R.drawable.weather_background_clear_night
        }

        WeatherView.WEATHER_KIND_CLOUD -> if (daylight) {
            R.drawable.weather_background_partly_cloudy_day
        } else {
            R.drawable.weather_background_partly_cloudy_night
        }

        WeatherView.WEATHER_KIND_CLOUDY -> if (daylight) {
            R.drawable.weather_background_cloudy_day
        } else {
            R.drawable.weather_background_cloudy_night
        }

        WeatherView.WEATHER_KIND_FOG ->
            R.drawable.weather_background_fog

        WeatherView.WEATHER_KIND_HAIL -> if (daylight) {
            R.drawable.weather_background_hail_day
        } else {
            R.drawable.weather_background_hail_night
        }

        WeatherView.WEATHER_KIND_HAZE ->
            R.drawable.weather_background_haze

        WeatherView.WEATHER_KIND_RAINY -> if (daylight) {
            R.drawable.weather_background_rain_day
        } else {
            R.drawable.weather_background_rain_night
        }

        WeatherView.WEATHER_KIND_SLEET -> if (daylight) {
            R.drawable.weather_background_sleet_day
        } else {
            R.drawable.weather_background_sleet_night
        }
        WeatherView.WEATHER_KIND_SNOW -> if (daylight) {
            R.drawable.weather_background_snow_day
        } else {
            R.drawable.weather_background_snow_night
        }

        WeatherView.WEATHER_KIND_THUNDER ->
            R.drawable.weather_background_thunder

        WeatherView.WEATHER_KIND_THUNDERSTORM ->
            R.drawable.weather_background_thunderstrom

        WeatherView.WEATHER_KIND_WIND ->
            R.drawable.weather_background_wind

        else ->
            R.drawable.weather_background_default
    }
}