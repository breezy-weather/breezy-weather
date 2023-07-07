package org.breezyweather.theme.resource.providers

import android.animation.Animator
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.theme.resource.utils.ResourceUtils
import org.breezyweather.theme.resource.utils.ResourceUtils.getDrawableUri

abstract class ResourceProvider {
    protected open fun getDrawableUri(resName: String): Uri {
        return getDrawableUri(packageName, "drawable", resName)
    }

    abstract val packageName: String
    abstract var providerName: String?
    abstract val providerIcon: Drawable?
    override fun equals(other: Any?): Boolean {
        return if (other is ResourceProvider) {
            other.packageName == packageName
        } else false
    }

    // weather icon.
    abstract fun getWeatherIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    abstract fun getWeatherIconUri(code: WeatherCode?, dayTime: Boolean): Uri
    @Size(3)
    abstract fun getWeatherIcons(code: WeatherCode?, dayTime: Boolean): Array<Drawable?>

    // animator.
    @Size(3)
    abstract fun getWeatherAnimators(code: WeatherCode?, dayTime: Boolean): Array<Animator?>

    // minimal icon.
    abstract fun getMinimalLightIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    abstract fun getMinimalLightIconUri(code: WeatherCode?, dayTime: Boolean): Uri
    abstract fun getMinimalGreyIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    abstract fun getMinimalGreyIconUri(code: WeatherCode?, dayTime: Boolean): Uri
    abstract fun getMinimalDarkIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    abstract fun getMinimalDarkIconUri(code: WeatherCode?, dayTime: Boolean): Uri
    abstract fun getMinimalXmlIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    @RequiresApi(api = Build.VERSION_CODES.M)
    abstract fun getMinimalIcon(code: WeatherCode?, dayTime: Boolean): Icon

    // shortcut.
    abstract fun getShortcutsIcon(code: WeatherCode?, dayTime: Boolean): Drawable
    abstract fun getShortcutsForegroundIcon(code: WeatherCode?, dayTime: Boolean): Drawable

    // sun and moon.
    abstract val sunDrawable: Drawable

    abstract val moonDrawable: Drawable

    companion object {
        @JvmStatic
        protected fun getResId(context: Context, resName: String, type: String): Int {
            return ResourceUtils.getResId(context, resName, type)
        }
    }
}
