/*
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

package org.breezyweather.ui.settings.activities

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import breezyweather.domain.weather.reference.WeatherCode
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.doOnApplyWindowInsets
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.databinding.ActivityPreviewIconBinding
import org.breezyweather.ui.settings.adapters.WeatherIconAdapter
import org.breezyweather.ui.settings.dialogs.AdaptiveIconDialog
import org.breezyweather.ui.settings.dialogs.AnimatableIconDialog
import org.breezyweather.ui.settings.dialogs.MinimalIconDialog
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import org.breezyweather.ui.theme.resource.providers.DefaultResourceProvider
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import java.util.Locale

class PreviewIconActivity : BreezyActivity() {
    private lateinit var binding: ActivityPreviewIconBinding
    private lateinit var mProvider: ResourceProvider
    private val mItemList = mutableListOf<WeatherIconAdapter.Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewIconBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        WeatherCode.entries.forEach {
            mItemList.add(WeatherIcon(mProvider, it, true))
        }
        mItemList.add(WeatherIconAdapter.Line())

        mItemList.add(WeatherIconAdapter.Title(getString(R.string.nighttime)))
        WeatherCode.entries.forEach {
            mItemList.add(WeatherIcon(mProvider, it, false))
        }
        mItemList.add(WeatherIconAdapter.Line())

        val darkMode = this.isDarkMode
        mItemList.add(WeatherIconAdapter.Title("Minimal " + getString(R.string.daytime)))
        WeatherCode.entries.forEach {
            mItemList.add(MinimalIcon(mProvider, it, true, darkMode))
        }
        mItemList.add(WeatherIconAdapter.Line())

        mItemList.add(WeatherIconAdapter.Title("Minimal " + getString(R.string.nighttime)))
        WeatherCode.entries.forEach {
            mItemList.add(MinimalIcon(mProvider, it, false, darkMode))
        }
        mItemList.add(WeatherIconAdapter.Line())

        mItemList.add(WeatherIconAdapter.Title("Shortcuts " + getString(R.string.daytime)))
        WeatherCode.entries.forEach {
            mItemList.add(ShortcutIcon(mProvider, it, true))
        }
        mItemList.add(WeatherIconAdapter.Line())

        mItemList.add(WeatherIconAdapter.Title("Shortcuts " + getString(R.string.nighttime)))
        WeatherCode.entries.forEach {
            mItemList.add(ShortcutIcon(mProvider, it, false))
        }
        mItemList.add(WeatherIconAdapter.Line())

        mItemList.add(WeatherIconAdapter.Title(getString(R.string.ephemeris)))
        mItemList.add(SunIcon(mProvider))
        mItemList.add(MoonIcon(mProvider))
    }

    @SuppressLint("NonConstantResourceId")
    private fun initWidget() {
        binding.activityPreviewIconAppBar.injectDefaultSurfaceTintColor()

        binding.activityPreviewIconToolbar.apply {
            title = mProvider.providerName
            setNavigationOnClickListener { finish() }
            inflateMenu(R.menu.activity_preview_icon)
            setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.action_about ->
                        if (mProvider is DefaultResourceProvider) {
                            IntentHelper.startApplicationDetailsActivity(this@PreviewIconActivity)
                        } else {
                            IntentHelper.startApplicationDetailsActivity(
                                this@PreviewIconActivity,
                                mProvider.packageName
                            )
                        }
                }
                true
            }
            setBackgroundColor(
                ColorUtils.getWidgetSurfaceColor(
                    6f,
                    getThemeColor(androidx.appcompat.R.attr.colorPrimary),
                    getThemeColor(com.google.android.material.R.attr.colorSurface)
                )
            )
        }

        binding.activityPreviewIconRecyclerView.doOnApplyWindowInsets { view, insets ->
            view.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom
            )
        }
        val manager = GridLayoutManager(this, 4)
        manager.spanSizeLookup = WeatherIconAdapter.getSpanSizeLookup(4, mItemList)
        binding.activityPreviewIconRecyclerView.apply {
            layoutManager = manager
            adapter = WeatherIconAdapter(this@PreviewIconActivity, mItemList)
        }
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

    override fun onItemClicked(activity: BreezyActivity) {
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

    override fun onItemClicked(activity: BreezyActivity) {
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

    override fun onItemClicked(activity: BreezyActivity) {
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

    override fun onItemClicked(activity: BreezyActivity) {
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
