/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.main.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.annotation.AttrRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.DarkMode
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.theme.ThemeManager

private val preloadAttrIds = intArrayOf(
    androidx.appcompat.R.attr.colorPrimary,
    com.google.android.material.R.attr.colorOnPrimary,
    com.google.android.material.R.attr.colorPrimaryContainer,
    com.google.android.material.R.attr.colorOnPrimaryContainer,

    com.google.android.material.R.attr.colorSecondary,
    com.google.android.material.R.attr.colorOnSecondary,
    com.google.android.material.R.attr.colorSecondaryContainer,
    com.google.android.material.R.attr.colorOnSecondaryContainer,

    com.google.android.material.R.attr.colorTertiary,
    com.google.android.material.R.attr.colorOnTertiary,
    com.google.android.material.R.attr.colorTertiaryContainer,
    com.google.android.material.R.attr.colorOnTertiaryContainer,

    com.google.android.material.R.attr.colorTertiary,
    com.google.android.material.R.attr.colorErrorContainer,
    com.google.android.material.R.attr.colorOnError,
    com.google.android.material.R.attr.colorOnErrorContainer,

    android.R.attr.colorBackground,
    com.google.android.material.R.attr.colorOnBackground,

    com.google.android.material.R.attr.colorSurface,
    com.google.android.material.R.attr.colorOnSurface,
    com.google.android.material.R.attr.colorSurfaceVariant,
    com.google.android.material.R.attr.colorOnSurfaceVariant,

    com.google.android.material.R.attr.colorOutline,

    R.attr.colorTitleText,
    R.attr.colorBodyText,
    R.attr.colorCaptionText,
    R.attr.colorMainCardBackground,
    R.attr.colorPrecipitationProbability
)

class MainThemeColorProvider(
    private val host: MainActivity,
) : LifecycleEventObserver {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: MainThemeColorProvider? = null

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

            val provider = MainThemeColorProvider(mainActivity)
            mainActivity.lifecycle.addObserver(provider)
            instance = provider
        }

        fun unbind() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                throw IllegalStateException("Cannot unbind context provider on a background thread")
            }

            instance?.let {
                it.host.lifecycle.removeObserver(it)
            }
            instance = null
        }

        fun isLightTheme(
            context: Context,
            location: Location?,
        ) = isLightTheme(
            context = context,
            daylight = location?.isDaylight
        )

        fun isLightTheme(
            context: Context,
            daylight: Boolean?,
        ): Boolean = if (SettingsManager.getInstance(context).dayNightModeForLocations &&
            instance?.host?.isDaylight != null &&
            daylight != null
        ) {
            daylight
        } else {
            when (SettingsManager.getInstance(context).darkMode) {
                DarkMode.LIGHT -> true
                DarkMode.DARK -> false
                else -> !context.isDarkMode
            }
        }

        fun getContext(
            lightTheme: Boolean,
        ) = instance?.let {
            if (lightTheme) {
                it.lightContext
            } else {
                it.darkContext
            }
        }

        fun getContext(
            location: Location,
        ) = instance?.let {
            getContext(
                lightTheme = isLightTheme(it.host, location)
            )
        }

        fun getColor(
            lightTheme: Boolean,
            @AttrRes id: Int,
        ) = instance?.let { instance ->
            val cache = if (lightTheme) {
                instance.lightColorCache
            } else {
                instance.darkColorCache
            }

            if (cache[id] != null) {
                return@let cache[id]
            }

            val color = getContext(lightTheme)?.let {
                ThemeManager.getInstance(instance.host).getThemeColor(context = it, id = id)
            } ?: 0
            cache[id] = color
            return@let color
        } ?: 0

        fun getColor(
            location: Location?,
            @AttrRes id: Int,
        ) = instance?.let {
            getColor(
                id = id,
                lightTheme = isLightTheme(it.host, location)
            )
        } ?: 0
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
            ThemeManager.getInstance(host).getThemeColors(lightContext, preloadAttrIds)
                .zip(ThemeManager.getInstance(host).getThemeColors(darkContext, preloadAttrIds))
        ).forEach {
            // first = attr id, second = <light color, dark color>
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
