package org.breezyweather.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatDelegate
import org.breezyweather.R
import org.breezyweather.common.basic.livedata.EqualtableLiveData
import org.breezyweather.common.basic.models.options.DarkMode
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.weatherView.WeatherThemeDelegate
import org.breezyweather.theme.weatherView.materialWeatherView.MaterialWeatherThemeDelegate


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
                            darkMode = SettingsManager.getInstance(context).darkMode,
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
    private val typedValue = TypedValue()

    fun update(darkMode: DarkMode) {
        this.darkMode = darkMode

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

    @SuppressLint("ResourceType")
    fun getThemeColors(context: Context, @AttrRes ids: IntArray): IntArray {
        val a = context.theme.obtainStyledAttributes(ids)
        val colors = ids.mapIndexed { index, _ ->
            a.getColor(index, Color.TRANSPARENT)
        }
        a.recycle()

        return colors.toIntArray()
    }

    fun generateThemeContext(
        context: Context,
        lightTheme: Boolean
    ): Context = context.createConfigurationContext(
        Configuration(context.resources.configuration).apply {
            uiMode = uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()
            uiMode = uiMode or if (lightTheme) {
                Configuration.UI_MODE_NIGHT_NO
            } else {
                Configuration.UI_MODE_NIGHT_YES
            }
        }
    ).apply {
        setTheme(R.style.BreezyWeatherTheme)
    }
}