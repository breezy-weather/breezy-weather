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
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.MaterialToolbar
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.insets.FitBothSideBarView
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.ui.widgets.insets.FitSystemBarAppBarLayout
import org.breezyweather.common.ui.widgets.insets.FitSystemBarRecyclerView
import org.breezyweather.common.ui.widgets.insets.FitSystemBarViewPager
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import java.util.*

/**
 * Daily weather activity.
 */
class DailyWeatherActivity : GeoActivity() {
    private var mToolbar: MaterialToolbar? = null
    private var mTitle: TextView? = null
    private var mSubtitle: TextView? = null
    private var mIndicator: TextView? = null
    private var mFormattedId: String? = null
    private var mPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_daily)
        initData()
        initWidget()
    }

    private fun initData() {
        mFormattedId = intent.getStringExtra(KEY_FORMATTED_LOCATION_ID)
        mPosition = intent.getIntExtra(KEY_CURRENT_DAILY_INDEX, 0)
    }

    private fun initWidget() {
        val appBarLayout = findViewById<FitSystemBarAppBarLayout>(R.id.activity_weather_daily_appBar)
        appBarLayout.injectDefaultSurfaceTintColor()
        mToolbar = findViewById<MaterialToolbar>(R.id.activity_weather_daily_toolbar).also {
            it.setBackgroundColor(
                ColorUtils.getWidgetSurfaceColor(
                    6f,
                    ThemeManager.getInstance(this).getThemeColor(this, androidx.appcompat.R.attr.colorPrimary),
                    ThemeManager.getInstance(this).getThemeColor(this, com.google.android.material.R.attr.colorSurface)
                )
            )
            it.setNavigationOnClickListener { finish() }
        }
        mTitle = findViewById(R.id.activity_weather_daily_title)
        mSubtitle = findViewById<TextView>(R.id.activity_weather_daily_subtitle).also {
            it.visibility = if (SettingsManager.getInstance(this).language.isChinese) View.VISIBLE else View.GONE
        }
        mIndicator = findViewById(R.id.activity_weather_daily_indicator)
        val formattedId = mFormattedId
        AsyncHelper.runOnIO({ emitter: AsyncHelper.Emitter<Location> ->
            var location: Location? = null
            if (!formattedId.isNullOrEmpty()) {
                location = LocationEntityRepository.readLocation(formattedId)
            }
            if (location == null) {
                location = LocationEntityRepository.readLocationList()[0]
            }
            emitter.send(
                location.copy(weather = WeatherEntityRepository.readWeather(location)),
                true
            )
        }, { location: Location?, _: Boolean ->
            if (location == null) {
                finish()
                return@runOnIO
            }
            val weather = location.weather
            if (weather == null) {
                finish()
                return@runOnIO
            }
            selectPage(
                weather.dailyForecast[mPosition],
                location.timeZone,
                mPosition,
                weather.dailyForecast.size
            )
            val viewList: MutableList<View> = ArrayList(weather.dailyForecast.size)
            val titleList: MutableList<String> = ArrayList(weather.dailyForecast.size)
            weather.dailyForecast.forEachIndexed { i, daily ->
                val rv = FitSystemBarRecyclerView(this)
                rv.removeFitSide(FitBothSideBarView.SIDE_TOP)
                rv.addFitSide(FitBothSideBarView.SIDE_BOTTOM)
                rv.clipToPadding = false
                val dailyWeatherAdapter = DailyWeatherAdapter(this, location.timeZone, daily, 3)
                val gridLayoutManager = GridLayoutManager(this, 3)
                gridLayoutManager.spanSizeLookup = dailyWeatherAdapter.spanSizeLookup
                rv.adapter = dailyWeatherAdapter
                rv.layoutManager = gridLayoutManager
                viewList.add(rv)
                titleList.add((i + 1).toString())
            }
            val pager = findViewById<FitSystemBarViewPager>(R.id.activity_weather_daily_pager)
            pager.adapter = FitSystemBarViewPager.FitBottomSystemBarPagerAdapter(pager, viewList, titleList)
            pager.pageMargin = this.dpToPx(1f).toInt()
            pager.setPageMarginDrawable(
                ColorDrawable(
                    ThemeManager.getInstance(this).getThemeColor(this, com.google.android.material.R.attr.colorOutline)
                )
            )
            pager.currentItem = mPosition
            pager.clearOnPageChangeListeners()
            pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    // do nothing.
                }

                override fun onPageSelected(position: Int) {
                    selectPage(
                        weather.dailyForecast[position],
                        location.timeZone,
                        position,
                        weather.dailyForecast.size
                    )
                }

                override fun onPageScrollStateChanged(state: Int) {
                    // do nothing.
                }
            })
        })
    }

    @SuppressLint("SetTextI18n")
    private fun selectPage(daily: Daily, timeZone: TimeZone, position: Int, size: Int) {
        mTitle?.text = daily.date.getFormattedDate(timeZone, getString(R.string.date_format_widget_long))
        mSubtitle?.text = daily.lunar
        mToolbar?.contentDescription = mTitle?.text.toString() + ", " + mSubtitle?.text
        mIndicator?.text = if (daily.isToday(timeZone)) {
            getString(R.string.short_today)
        } else (position + 1).toString() + "/" + size
    }

    companion object {
        const val KEY_FORMATTED_LOCATION_ID = "FORMATTED_LOCATION_ID"
        const val KEY_CURRENT_DAILY_INDEX = "CURRENT_DAILY_INDEX"
    }
}
