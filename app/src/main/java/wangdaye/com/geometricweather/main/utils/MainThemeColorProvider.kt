package wangdaye.com.geometricweather.main.utils

import android.annotation.SuppressLint
import android.os.Looper
import androidx.annotation.AttrRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.main.MainActivity
import wangdaye.com.geometricweather.theme.ThemeManager

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
            lightTheme = MainModuleUtils.isHomeLightTheme(
                instance!!.host,
                location.isDaylight
            )
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
            lightTheme = location.isDaylight
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

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val currentState = host.lifecycle.currentState
        if (currentState == Lifecycle.State.DESTROYED) {
            unbind()
        }
    }
}