package org.breezyweather.theme.resource.providers

import android.animation.Animator
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.IntRange
import androidx.annotation.Size
import org.breezyweather.BreezyWeather
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.ui.images.pixel.PixelMoonDrawable
import org.breezyweather.common.ui.images.pixel.PixelSunDrawable
import org.breezyweather.theme.resource.utils.Constants
import org.breezyweather.theme.resource.utils.ResourceUtils

class PixelResourcesProvider(defaultProvider: ResourceProvider) : IconPackResourcesProvider(
    BreezyWeather.instance,
    BreezyWeather.instance.packageName,
    defaultProvider
) {
    override fun getDrawableUri(resName: String): Uri {
        return ResourceUtils.getDrawableUri(super.packageName, "drawable", resName)
    }

    override val packageName: String
        get() = super.packageName + ".Pixel"
    override var providerName: String?
        get() = "Pixel"
        set(providerName) {
            super.providerName = providerName
        }
    override val providerIcon: Drawable
        get() = getWeatherIcon(WeatherCode.PARTLY_CLOUDY, true)

    // weather icon.
    @Size(3)
    override fun getWeatherIcons(code: WeatherCode?, dayTime: Boolean): Array<Drawable?> {
        return arrayOf(getWeatherIcon(code, dayTime), null, null)
    }

    override fun getWeatherIconName(code: WeatherCode?, daytime: Boolean): String {
        return super.getWeatherIconName(code, daytime) + Constants.SEPARATOR + "pixel"
    }

    override fun getWeatherIconName(
        code: WeatherCode?, daytime: Boolean,
        @IntRange(from = 1, to = 3) index: Int
    ): String? {
        return if (index == 1) {
            getWeatherIconName(code, daytime)
        } else null
    }

    // animator.
    @Size(3)
    override fun getWeatherAnimators(code: WeatherCode?, dayTime: Boolean): Array<Animator?> {
        return arrayOf(null, null, null)
    }

    override fun getWeatherAnimatorName(
        code: WeatherCode?, daytime: Boolean,
        @IntRange(from = 1, to = 3) index: Int
    ): String? {
        return null
    }

    override val sunDrawable: Drawable
        // sun and moon.
        get() = PixelSunDrawable()
    override val moonDrawable: Drawable
        get() = PixelMoonDrawable()
    override val sunDrawableClassName: String
        get() = PixelSunDrawable::class.java.toString()
    override val moonDrawableClassName: String
        get() = PixelMoonDrawable::class.java.toString()

    companion object {
        fun isPixelIconProvider(packageName: String): Boolean {
            return packageName == BreezyWeather.instance.packageName + ".Pixel"
        }
    }
}
