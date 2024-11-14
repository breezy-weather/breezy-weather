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

package org.breezyweather.settings.activities

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.weather.model.WeatherCode
import com.google.android.material.appbar.MaterialToolbar
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.doOnApplyWindowInsets
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.ui.widgets.insets.FitSystemBarAppBarLayout
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.settings.adapters.WeatherIconAdapter
import org.breezyweather.settings.dialogs.AdaptiveIconDialog
import org.breezyweather.settings.dialogs.AnimatableIconDialog
import org.breezyweather.settings.dialogs.MinimalIconDialog
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory
import org.breezyweather.theme.resource.providers.DefaultResourceProvider
import org.breezyweather.theme.resource.providers.ResourceProvider
import java.util.Locale

class PreviewIconActivity : GeoActivity() {
    private var mProvider: ResourceProvider? = null
    private val mItemList = mutableListOf<WeatherIconAdapter.Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_icon)
        initData()
        initWidget()
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        // do nothing.
    }

    private fun initData() {
        mProvider = ResourcesProviderFactory.getNewInstance(
            intent.getStringExtra(KEY_ICON_PREVIEW_ACTIVITY_PACKAGE_NAME)
        )

        mItemList.clear()

        mItemList.add(WeatherIconAdapter.Title(getString(R.string.daytime)))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.CLEAR, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.PARTLY_CLOUDY, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.CLOUDY, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.WIND, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.RAIN, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.SNOW, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.SLEET, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.HAIL, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.THUNDER, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.THUNDERSTORM, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.FOG, true))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.HAZE, true))
        mItemList.add(WeatherIconAdapter.Line())

        mItemList.add(WeatherIconAdapter.Title(getString(R.string.nighttime)))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.CLEAR, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.PARTLY_CLOUDY, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.CLOUDY, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.WIND, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.RAIN, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.SNOW, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.SLEET, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.HAIL, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.THUNDER, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.THUNDERSTORM, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.FOG, false))
        mItemList.add(WeatherIcon(mProvider!!, WeatherCode.HAZE, false))
        mItemList.add(WeatherIconAdapter.Line())

        val darkMode = this.isDarkMode
        mItemList.add(WeatherIconAdapter.Title("Minimal " + getString(R.string.daytime)))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.CLEAR, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.PARTLY_CLOUDY, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.CLOUDY, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.WIND, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.RAIN, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.SNOW, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.SLEET, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.HAIL, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.THUNDER, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.THUNDERSTORM, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.FOG, true, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.HAZE, true, darkMode))
        mItemList.add(WeatherIconAdapter.Line())

        mItemList.add(WeatherIconAdapter.Title("Minimal " + getString(R.string.nighttime)))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.CLEAR, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.PARTLY_CLOUDY, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.CLOUDY, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.WIND, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.RAIN, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.SNOW, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.SLEET, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.HAIL, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.THUNDER, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.THUNDERSTORM, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.FOG, false, darkMode))
        mItemList.add(MinimalIcon(mProvider!!, WeatherCode.HAZE, false, darkMode))
        mItemList.add(WeatherIconAdapter.Line())

        mItemList.add(WeatherIconAdapter.Title("Shortcuts " + getString(R.string.daytime)))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.CLEAR, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.PARTLY_CLOUDY, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.CLOUDY, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.WIND, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.RAIN, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.SNOW, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.SLEET, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.HAIL, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.THUNDER, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.THUNDERSTORM, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.FOG, true))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.HAZE, true))
        mItemList.add(WeatherIconAdapter.Line())

        mItemList.add(WeatherIconAdapter.Title("Shortcuts " + getString(R.string.nighttime)))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.CLEAR, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.PARTLY_CLOUDY, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.CLOUDY, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.WIND, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.RAIN, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.SNOW, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.SLEET, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.HAIL, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.THUNDER, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.THUNDERSTORM, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.FOG, false))
        mItemList.add(ShortcutIcon(mProvider!!, WeatherCode.HAZE, false))
        mItemList.add(WeatherIconAdapter.Line())

        mItemList.add(WeatherIconAdapter.Title(getString(R.string.ephemeris)))
        mItemList.add(SunIcon(mProvider!!))
        mItemList.add(MoonIcon(mProvider!!))
    }

    @SuppressLint("NonConstantResourceId")
    private fun initWidget() {
        val appBarLayout = findViewById<FitSystemBarAppBarLayout>(R.id.activity_preview_icon_appBar)
        appBarLayout.injectDefaultSurfaceTintColor()

        val toolbar = findViewById<MaterialToolbar>(R.id.activity_preview_icon_toolbar)
        toolbar.title = mProvider!!.providerName
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.activity_preview_icon)
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_about ->
                    if (mProvider is DefaultResourceProvider) {
                        IntentHelper.startApplicationDetailsActivity(this)
                    } else {
                        IntentHelper.startApplicationDetailsActivity(this, mProvider!!.packageName)
                    }
            }
            true
        }
        toolbar.setBackgroundColor(
            ColorUtils.getWidgetSurfaceColor(
                6f,
                ThemeManager.getInstance(this).getThemeColor(this, androidx.appcompat.R.attr.colorPrimary),
                ThemeManager.getInstance(this).getThemeColor(this, com.google.android.material.R.attr.colorSurface)
            )
        )

        val recyclerView = findViewById<RecyclerView>(R.id.activity_preview_icon_recyclerView)
        recyclerView.doOnApplyWindowInsets { view, insets ->
            view.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom
            )
        }
        val manager = GridLayoutManager(this, 4)
        manager.spanSizeLookup = WeatherIconAdapter.getSpanSizeLookup(4, mItemList)
        recyclerView.layoutManager = manager
        recyclerView.adapter = WeatherIconAdapter(this, mItemList)
    }

    companion object {
        const val KEY_ICON_PREVIEW_ACTIVITY_PACKAGE_NAME = "ICON_PREVIEW_ACTIVITY_PACKAGE_NAME"
    }
}

internal abstract class BaseWeatherIcon(
    protected var provider: ResourceProvider,
    protected var weatherCode: WeatherCode,
    protected var daytime: Boolean,
) : WeatherIconAdapter.WeatherIcon() {
    override val contentDescription: String
        get() {
            val name = weatherCode.name.lowercase(Locale.getDefault()).replace("_", " ")
            return name.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1)
        }
}

internal class WeatherIcon(
    provider: ResourceProvider,
    weatherCode: WeatherCode,
    daytime: Boolean,
) : BaseWeatherIcon(provider, weatherCode, daytime) {
    override val drawable: Drawable
        get() = ResourceHelper.getWeatherIcon(provider, weatherCode, daytime)

    override fun onItemClicked(activity: GeoActivity) {
        AnimatableIconDialog.show(activity, weatherCode, daytime, provider)
    }
}

internal class MinimalIcon(
    provider: ResourceProvider,
    weatherCode: WeatherCode,
    daytime: Boolean,
    private val mDarkMode: Boolean,
) : BaseWeatherIcon(provider, weatherCode, daytime) {
    override val drawable: Drawable
        get() = ResourceHelper.getWidgetNotificationIcon(provider, weatherCode, daytime, true, !mDarkMode)

    override fun onItemClicked(activity: GeoActivity) {
        MinimalIconDialog.show(activity, weatherCode, daytime, provider)
    }
}

internal class ShortcutIcon(
    provider: ResourceProvider,
    weatherCode: WeatherCode,
    daytime: Boolean,
) : BaseWeatherIcon(provider, weatherCode, daytime) {
    override val drawable: Drawable
        get() = ResourceHelper.getShortcutsIcon(provider, weatherCode, daytime)

    override fun onItemClicked(activity: GeoActivity) {
        AdaptiveIconDialog.show(activity, weatherCode, daytime, provider)
    }
}

internal open class SunIcon(
    protected var provider: ResourceProvider,
) : WeatherIconAdapter.WeatherIcon() {
    override val drawable: Drawable
        get() = ResourceHelper.getSunDrawable(provider)

    override val contentDescription: String
        get() = "Sun"

    override fun onItemClicked(activity: GeoActivity) {
        // do nothing.
    }
}

internal class MoonIcon(
    provider: ResourceProvider,
) : SunIcon(provider) {
    override val drawable: Drawable
        get() = ResourceHelper.getMoonDrawable(provider)

    override val contentDescription: String
        get() = "Moon"
}
