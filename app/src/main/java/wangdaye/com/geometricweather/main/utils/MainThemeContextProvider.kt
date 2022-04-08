package wangdaye.com.geometricweather.main.utils

import android.annotation.SuppressLint
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import wangdaye.com.geometricweather.GeometricWeather
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.main.MainActivity
import wangdaye.com.geometricweather.theme.ThemeManager
import java.util.*

class MainThemeContextProvider(
    private val host: MainActivity
): LifecycleEventObserver {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: MainThemeContextProvider? = null

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

            instance = MainThemeContextProvider(mainActivity)
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
        fun getContext(
            lightTheme: Boolean
        ) = instance?.let {
            if (lightTheme) {
                it.lightContext
            } else {
                it.darkContext
            }
        } ?: GeometricWeather.instance

        @JvmStatic
        fun getContext(
            location: Location
        ) = getContext(
            lightTheme = instance?.let {
                MainModuleUtils.isHomeLightTheme(it.host, location.isDaylight)
            } ?: true
        )

        @JvmStatic
        fun getContext(
            weather: Weather,
            timeZone: TimeZone
        ) = getContext(
            lightTheme = instance?.let {
                MainModuleUtils.isHomeLightTheme(it.host, weather.isDaylight(timeZone))
            } ?: true
        )
    }

    val lightContext = ThemeManager
        .getInstance(host)
        .generateThemeContext(context = host, lightTheme = true)
    val darkContext = ThemeManager
        .getInstance(host)
        .generateThemeContext(context = host, lightTheme = false)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val currentState = host.lifecycle.currentState
        if (currentState == Lifecycle.State.DESTROYED) {
            unbind()
        }
    }
}