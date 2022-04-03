package wangdaye.com.geometricweather.theme

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatDelegate
import wangdaye.com.geometricweather.common.basic.livedata.EqualtableLiveData
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.DarkMode
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.theme.weatherView.WeatherThemeDelegate
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.MaterialWeatherThemeDelegate
import java.util.*

class ThemeManager private constructor(
    weatherThemeDelegate: WeatherThemeDelegate,
    darkMode: DarkMode,
) {

    companion object {

        @Volatile
        private var instance: ThemeManager? = null

        @JvmStatic
        fun getInstance(context: Context): ThemeManager {
            if (instance == null) {
                synchronized(ThemeManager::class) {
                    if (instance == null) {
                        instance = ThemeManager(
                            weatherThemeDelegate = MaterialWeatherThemeDelegate(),
                            darkMode = SettingsManager.getInstance(context).getDarkMode(),
                        )
                    }
                }
            }
            return instance!!
        }

        private fun generateHomeUIMode(
            darkMode: DarkMode,
            daylight: Boolean
        ): Int = when (darkMode) {
            DarkMode.AUTO -> if (daylight) {
                AppCompatDelegate.MODE_NIGHT_NO
            } else {
                AppCompatDelegate.MODE_NIGHT_YES
            }
            DarkMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            DarkMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            DarkMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        private fun generateGlobalUIMode(
            darkMode: DarkMode
        ): Int = when (darkMode) {
            DarkMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            DarkMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    val homeUIMode: EqualtableLiveData<Int>
    val globalUIMode: EqualtableLiveData<Int>
    val daylight: EqualtableLiveData<Boolean>

    val weatherThemeDelegate: WeatherThemeDelegate

    private var darkMode: DarkMode
    private val typedValue = TypedValue()

    val isDaylight: Boolean
    get() = daylight.value ?: true

    init {
        val isDaylight = DisplayUtils.isDaylight(TimeZone.getDefault())

        this.homeUIMode = EqualtableLiveData(
            generateHomeUIMode(
                darkMode = darkMode,
                daylight = isDaylight
            )
        )
        this.globalUIMode = EqualtableLiveData(
            generateGlobalUIMode(
                darkMode = darkMode
            )
        )
        this.daylight = EqualtableLiveData(isDaylight)

        this.weatherThemeDelegate = weatherThemeDelegate

        this.darkMode = darkMode
    }

    fun update(
        darkMode: DarkMode? = null,
        location: Location? = null,
    ) {
        darkMode?.let {
            this.darkMode = it
        }
        location?.let {
            this.daylight.setValue(it.isDaylight)
        }

        homeUIMode.setValue(
            generateHomeUIMode(
                darkMode = this.darkMode,
                daylight = this.daylight.value!!
            )
        )
        globalUIMode.setValue(
            generateGlobalUIMode(
                darkMode = this.darkMode
            )
        )
    }

    fun getThemeColor(context: Context, @AttrRes id: Int): Int {
        context.theme.resolveAttribute(id, typedValue, true)
        return typedValue.data
    }
}