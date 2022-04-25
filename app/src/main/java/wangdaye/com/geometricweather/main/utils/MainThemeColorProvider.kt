package wangdaye.com.geometricweather.main.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.annotation.AttrRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.DarkMode
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.main.MainActivity
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.theme.ThemeManager
import java.util.*
import kotlin.collections.HashMap

private val preloadAttrIds = intArrayOf(
    R.attr.colorPrimary,
    R.attr.colorOnPrimary,
    R.attr.colorPrimaryContainer,
    R.attr.colorOnPrimaryContainer,

    R.attr.colorSecondary,
    R.attr.colorOnSecondary,
    R.attr.colorSecondaryContainer,
    R.attr.colorOnSecondaryContainer,

    R.attr.colorTertiary,
    R.attr.colorOnTertiary,
    R.attr.colorTertiaryContainer,
    R.attr.colorOnTertiaryContainer,

    R.attr.colorTertiary,
    R.attr.colorErrorContainer,
    R.attr.colorOnError,
    R.attr.colorOnErrorContainer,

    android.R.attr.colorBackground,
    R.attr.colorOnBackground,

    R.attr.colorSurface,
    R.attr.colorOnSurface,
    R.attr.colorSurfaceVariant,
    R.attr.colorOnSurfaceVariant,

    R.attr.colorOutline,

    R.attr.colorTitleText,
    R.attr.colorBodyText,
    R.attr.colorCaptionText,
)

class MainThemeColorProvider(
    private val host: MainActivity
): LifecycleEventObserver {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: MainThemeColorProvider? = null

        @JvmStatic
        fun bind(mainActivity: MainActivity) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                throw IllegalStateException("Cannot bind context provider on a background thread")
            }
            if (mainActivity.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                return
            }

            instance?.let {
                if (it.host === mainActivity) {
                    return
                }
                unbind()
            }

            instance = MainThemeColorProvider(mainActivity)
            mainActivity.lifecycle.addObserver(instance!!)
        }

        @JvmStatic
        fun unbind() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                throw IllegalStateException("Cannot unbind context provider on a background thread")
            }

            instance?.let {
                it.host.lifecycle.removeObserver(it)
            }
            instance = null
        }

        @JvmStatic
        fun isLightTheme(
            context: Context,
            location: Location,
        ) = isLightTheme(
            context = context,
            daylight = location.isDaylight
        )

        @JvmStatic
        fun isLightTheme(
            context: Context,
            weather: Weather,
            timeZone: TimeZone,
        ) = isLightTheme(
            context = context,
            daylight = weather.isDaylight(timeZone)
        )

        @JvmStatic
        private fun isLightTheme(
            context: Context,
            daylight: Boolean,
        ) = when (SettingsManager.getInstance(context).darkMode) {
            DarkMode.AUTO -> instance?.host?.isDaylight ?: daylight
            DarkMode.SYSTEM -> !DisplayUtils.isDarkMode(context)
            DarkMode.LIGHT -> true
            DarkMode.DARK -> false
        }

        @JvmStatic
        fun getContext(
            lightTheme: Boolean
        ) = if (lightTheme) {
            instance!!.lightContext
        } else {
            instance!!.darkContext
        }

        @JvmStatic
        fun getContext(
            location: Location
        ) = getContext(
            lightTheme = isLightTheme(instance!!.host, location)
        )

        @JvmStatic
        fun getColor(
            lightTheme: Boolean,
            @AttrRes id: Int,
        ): Int {
            val cache = if (lightTheme) {
                instance!!.lightColorCache
            } else {
                instance!!.darkColorCache
            }

            cache[id]?.let {
                return it
            }

            val color = ThemeManager.getInstance(instance!!.host).getThemeColor(
                context = getContext(lightTheme),
                id = id
            )
            cache[id] = color
            return color
        }

        @JvmStatic
        fun getColor(
            location: Location,
            @AttrRes id: Int,
        ) = getColor(
            id = id,
            lightTheme = isLightTheme(instance!!.host, location)
        )
    }

    val lightContext = ThemeManager
        .getInstance(host)
        .generateThemeContext(context = host, lightTheme = true)
    private val lightColorCache = HashMap<Int, Int>()

    val darkContext = ThemeManager
        .getInstance(host)
        .generateThemeContext(context = host, lightTheme = false)
    private val darkColorCache = HashMap<Int, Int>()

    init {
        preloadAttrIds.zip(
            ThemeManager.getInstance(host).getThemeColors(lightContext, preloadAttrIds).zip(
                ThemeManager.getInstance(host).getThemeColors(darkContext, preloadAttrIds)
            )
        ).forEach { // attr id, <light color, dark color>
            lightColorCache[it.first] = it.second.first
            darkColorCache[it.first] = it.second.second
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val currentState = host.lifecycle.currentState
        if (currentState == Lifecycle.State.DESTROYED) {
            unbind()
        }
    }
}