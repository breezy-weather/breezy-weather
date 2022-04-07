package wangdaye.com.geometricweather.theme

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatDelegate
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.livedata.EqualtableLiveData
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.options.DarkMode
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.theme.weatherView.WeatherThemeDelegate
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.MaterialWeatherThemeDelegate
import java.util.*

class ThemeManager private constructor(
    val weatherThemeDelegate: WeatherThemeDelegate,
    var darkMode: DarkMode,
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

        private fun generateGlobalUIMode(
            darkMode: DarkMode
        ): Int = when (darkMode) {
            DarkMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            DarkMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    val uiMode: EqualtableLiveData<Int> = EqualtableLiveData(
        generateGlobalUIMode(darkMode = darkMode)
    )
    var isDaylight = DisplayUtils.isDaylight(TimeZone.getDefault())

    private val typedValue = TypedValue()

    @JvmOverloads
    fun update(
        darkMode: DarkMode? = null,
        location: Location? = null,
    ) {
        darkMode?.let {
            this.darkMode = it
        }
        location?.let {
            this.isDaylight = it.isDaylight
        }

        uiMode.setValue(
            generateGlobalUIMode(
                darkMode = this.darkMode
            )
        )
    }

    fun getThemeColor(context: Context, @AttrRes id: Int): Int {
        context.theme.resolveAttribute(id, typedValue, true)
        return typedValue.data
    }

    fun generateThemeContext(
        context: Context,
        daylight: Boolean = isDaylight
    ): Context = context.createConfigurationContext(
        Configuration(context.resources.configuration).apply {
            uiMode = uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()
            uiMode = uiMode or if (daylight) {
                Configuration.UI_MODE_NIGHT_NO
            } else {
                Configuration.UI_MODE_NIGHT_YES
            }
        }
    ).apply {
        setTheme(R.style.GeometricWeatherTheme)
    }
}