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

package org.breezyweather.daily

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.CalendarHelper
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.doOnApplyWindowInsets
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedMediumDayAndMonthInAdditionalCalendar
import org.breezyweather.common.extensions.getLongWeekdayDayMonth
import org.breezyweather.common.extensions.launchUI
import org.breezyweather.common.ui.widgets.insets.FitSystemBarViewPager
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.databinding.ActivityWeatherDailyBinding
import org.breezyweather.domain.weather.model.isToday
import org.breezyweather.sources.SourceManager
import org.breezyweather.theme.ThemeManager
import javax.inject.Inject

/**
 * Daily weather activity.
 * TODO: Consider moving this activity as a fragment of MainActivity, so we don't have to query the database twice
 */
@AndroidEntryPoint
class DailyWeatherActivity : GeoActivity() {

    @Inject lateinit var sourceManager: SourceManager

    @Inject lateinit var locationRepository: LocationRepository

    @Inject lateinit var weatherRepository: WeatherRepository

    private lateinit var binding: ActivityWeatherDailyBinding

    private var mFormattedId: String? = null
    private var mPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherDailyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initWidget()
    }

    private fun initData() {
        mFormattedId = intent.getStringExtra(KEY_FORMATTED_LOCATION_ID)
        mPosition = intent.getIntExtra(KEY_CURRENT_DAILY_INDEX, 0)
    }

    private fun initWidget() {
        binding.activityWeatherDailyAppBar.injectDefaultSurfaceTintColor()
        binding.activityWeatherDailyToolbar.apply {
            setBackgroundColor(
                ColorUtils.getWidgetSurfaceColor(
                    6f,
                    ThemeManager.getInstance(this@DailyWeatherActivity)
                        .getThemeColor(this@DailyWeatherActivity, androidx.appcompat.R.attr.colorPrimary),
                    ThemeManager.getInstance(this@DailyWeatherActivity)
                        .getThemeColor(this@DailyWeatherActivity, com.google.android.material.R.attr.colorSurface)
                )
            )
            setNavigationOnClickListener { finish() }
        }
        binding.activityWeatherDailySubtitle.visibility =
            if (CalendarHelper.getAlternateCalendarSetting(this) != null) View.VISIBLE else View.GONE
        val formattedId = mFormattedId

        lifecycleScope.launchUI {
            var location: Location? = null
            if (!formattedId.isNullOrEmpty()) {
                location = locationRepository.getLocation(formattedId, withParameters = false)
            }
            if (location == null) {
                location = locationRepository.getFirstLocation(withParameters = false)
            }
            if (location == null) {
                finish()
                return@launchUI
            }
            location = location.copy(
                weather = weatherRepository.getWeatherByLocationId(
                    location.formattedId,
                    withDaily = true,
                    withHourly = false,
                    withMinutely = false,
                    withAlerts = false
                )
            )
            val weather = location.weather
            if (weather == null) {
                finish()
                return@launchUI
            }
            selectPage(
                weather.dailyForecast[mPosition],
                location,
                mPosition,
                weather.dailyForecast.size
            )
            val viewList: MutableList<View> = ArrayList(weather.dailyForecast.size)
            val titleList: MutableList<String> = ArrayList(weather.dailyForecast.size)
            weather.dailyForecast.forEachIndexed { i, daily ->
                val rv = RecyclerView(this@DailyWeatherActivity)
                rv.clipToPadding = false
                rv.doOnApplyWindowInsets { view, insets ->
                    view.updatePadding(
                        left = insets.left,
                        right = insets.right,
                        bottom = insets.bottom
                    )
                }
                val pollenIndexSource = sourceManager.getPollenIndexSource(
                    if (!location.pollenSource.isNullOrEmpty()) {
                        location.pollenSource!!
                    } else {
                        location.forecastSource
                    }
                )
                val dailyWeatherAdapter = DailyWeatherAdapter(
                    this@DailyWeatherActivity,
                    location,
                    daily,
                    pollenIndexSource,
                    3
                )
                val gridLayoutManager = GridLayoutManager(this@DailyWeatherActivity, 3)
                gridLayoutManager.spanSizeLookup = dailyWeatherAdapter.spanSizeLookup
                rv.adapter = dailyWeatherAdapter
                rv.layoutManager = gridLayoutManager
                viewList.add(rv)
                titleList.add((i + 1).toString())
            }

            binding.activityWeatherDailyPager.apply {
                adapter = FitSystemBarViewPager.FitBottomSystemBarPagerAdapter(viewList, titleList)
                pageMargin = this@DailyWeatherActivity.dpToPx(1f).toInt()
                setPageMarginDrawable(
                    ColorDrawable(
                        ThemeManager.getInstance(this@DailyWeatherActivity)
                            .getThemeColor(
                                this@DailyWeatherActivity,
                                com.google.android.material.R.attr.colorOutline
                            )
                    )
                )
                currentItem = mPosition
                clearOnPageChangeListeners()
                addOnPageChangeListener(
                    object : ViewPager.OnPageChangeListener {
                        override fun onPageScrolled(
                            position: Int,
                            positionOffset: Float,
                            positionOffsetPixels: Int,
                        ) {
                            // do nothing.
                        }

                        override fun onPageSelected(position: Int) {
                            selectPage(
                                weather.dailyForecast[position],
                                location,
                                position,
                                weather.dailyForecast.size
                            )
                        }

                        override fun onPageScrollStateChanged(state: Int) {
                            // do nothing.
                        }
                    }
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun selectPage(daily: Daily, location: Location, position: Int, size: Int) {
        binding.activityWeatherDailyTitle.text = daily.date.getFormattedDate(
            getLongWeekdayDayMonth(this),
            location,
            this
        ).capitalize(this.currentLocale)
        binding.activityWeatherDailySubtitle.text = daily.date
            .getFormattedMediumDayAndMonthInAdditionalCalendar(location, this)
        binding.activityWeatherDailyToolbar.contentDescription = binding.activityWeatherDailyTitle.text.toString() +
            this.getString(R.string.comma_separator) + binding.activityWeatherDailySubtitle.text
        binding.activityWeatherDailyIndicator.text = if (daily.isToday(location)) {
            getString(R.string.short_today)
        } else {
            (position + 1).toString() + "/" + size
        }
    }

    companion object {
        const val KEY_FORMATTED_LOCATION_ID = "FORMATTED_LOCATION_ID"
        const val KEY_CURRENT_DAILY_INDEX = "CURRENT_DAILY_INDEX"
    }
}
