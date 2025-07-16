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

package org.breezyweather.ui.main.adapters.main

import android.animation.Animator
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import org.breezyweather.common.basic.models.options.appearance.CardDisplay
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.hasMinutelyPrecipitation
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.domain.weather.model.validAirQuality
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.main.adapters.main.holder.AbstractMainCardViewHolder
import org.breezyweather.ui.main.adapters.main.holder.AbstractMainViewHolder
import org.breezyweather.ui.main.adapters.main.holder.AirQualityViewHolder
import org.breezyweather.ui.main.adapters.main.holder.AlertViewHolder
import org.breezyweather.ui.main.adapters.main.holder.ClockViewHolder
import org.breezyweather.ui.main.adapters.main.holder.DailyViewHolder
import org.breezyweather.ui.main.adapters.main.holder.FooterViewHolder
import org.breezyweather.ui.main.adapters.main.holder.HeaderViewHolder
import org.breezyweather.ui.main.adapters.main.holder.HourlyViewHolder
import org.breezyweather.ui.main.adapters.main.holder.HumidityViewHolder
import org.breezyweather.ui.main.adapters.main.holder.MoonViewHolder
import org.breezyweather.ui.main.adapters.main.holder.PollenViewHolder
import org.breezyweather.ui.main.adapters.main.holder.PrecipitationNowcastViewHolder
import org.breezyweather.ui.main.adapters.main.holder.PrecipitationViewHolder
import org.breezyweather.ui.main.adapters.main.holder.PressureViewHolder
import org.breezyweather.ui.main.adapters.main.holder.SunViewHolder
import org.breezyweather.ui.main.adapters.main.holder.UvViewHolder
import org.breezyweather.ui.main.adapters.main.holder.VisibilityViewHolder
import org.breezyweather.ui.main.adapters.main.holder.WindViewHolder
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherView
import java.util.Calendar
import java.util.Collections
import java.util.Date

class MainAdapter(
    activity: MainActivity,
    host: RecyclerView,
    weatherView: WeatherView,
    location: Location?,
    provider: ResourceProvider,
    listAnimationEnabled: Boolean,
    itemAnimationEnabled: Boolean,
) : RecyclerView.Adapter<AbstractMainViewHolder?>() {
    private lateinit var mActivity: MainActivity
    private var mHost: RecyclerView? = null
    private var mWeatherView: WeatherView? = null
    private var mLocation: Location? = null
    private var mProvider: ResourceProvider? = null
    private val mViewTypeList: MutableList<Int> = mutableListOf()
    private val mViewHasAnimatedList: MutableList<Boolean> = mutableListOf()
    private var mPendingAnimatorList: MutableList<Animator>? = null
    private var mHeaderCurrentTemperatureTextHeight = 0
    private var mListAnimationEnabled = false
    private var mItemAnimationEnabled = false
    private var selectedDailyTab: String? = null
    private var selectedHourlyTab: String? = null

    init {
        update(activity, host, weatherView, location, provider, listAnimationEnabled, itemAnimationEnabled)
    }

    fun update(
        activity: MainActivity,
        host: RecyclerView,
        weatherView: WeatherView,
        location: Location?,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        mActivity = activity
        mHost = host
        mWeatherView = weatherView
        mLocation = location
        mProvider = provider
        mViewTypeList.clear()
        mViewHasAnimatedList.clear()
        mPendingAnimatorList = mutableListOf()
        mHeaderCurrentTemperatureTextHeight = -1
        mListAnimationEnabled = listAnimationEnabled
        mItemAnimationEnabled = itemAnimationEnabled
        location?.weather?.let { weather ->
            val cardDisplayList = SettingsManager.getInstance(activity).cardDisplayList
            mViewTypeList.add(ViewType.HEADER)
            if (location.weather?.alertList?.any { it.endDate == null || it.endDate!!.time > Date().time } == true) {
                mViewTypeList.add(ViewType.ALERT)
            }
            for (c in cardDisplayList) {
                if (c === CardDisplay.CARD_NOWCAST &&
                    (!weather.hasMinutelyPrecipitation || weather.minutelyForecast.size < 3)
                ) {
                    continue
                }
                if (c === CardDisplay.CARD_DAILY_FORECAST && weather.dailyForecast.isEmpty()) {
                    continue
                }
                if (c === CardDisplay.CARD_HOURLY_FORECAST && weather.nextHourlyForecast.isEmpty()) {
                    continue
                }
                if (c === CardDisplay.CARD_PRECIPITATION) {
                    val cal = Date().toCalendarWithTimeZone(location.javaTimeZone)
                    val currentHour = cal[Calendar.HOUR_OF_DAY]
                    val precipitation = if (currentHour < 5) {
                        weather.todayIndex?.let { todayIndex ->
                            weather.dailyForecast.getOrElse(todayIndex.minus(1)) { null }?.night?.precipitation
                        }
                    } else if (currentHour < 17) {
                        weather.today?.day?.precipitation
                    } else {
                        weather.today?.night?.precipitation
                    }
                    if (precipitation?.total == null) {
                        continue
                    }
                }
                if (c === CardDisplay.CARD_WIND && weather.current?.wind?.isValid != true) {
                    continue
                }
                if (c === CardDisplay.CARD_AIR_QUALITY && weather.validAirQuality == null) {
                    continue
                }
                if (c === CardDisplay.CARD_POLLEN &&
                    (weather.dailyForecast.isEmpty() || weather.today?.pollen?.isIndexValid != true)
                ) {
                    continue
                }
                if (c === CardDisplay.CARD_HUMIDITY && weather.current?.relativeHumidity == null) {
                    continue
                }
                if (c === CardDisplay.CARD_UV && weather.current?.uV?.index == null) {
                    continue
                }
                if (c === CardDisplay.CARD_VISIBILITY && weather.current?.visibility == null) {
                    continue
                }
                if (c === CardDisplay.CARD_PRESSURE && weather.current?.pressure == null) {
                    continue
                }
                if (c === CardDisplay.CARD_SUN &&
                    (weather.dailyForecast.isEmpty() || weather.today?.sun == null)
                ) {
                    continue
                }
                if (c === CardDisplay.CARD_MOON &&
                    (weather.dailyForecast.isEmpty() || weather.today?.moon?.isValid != true)
                ) {
                    continue
                }
                mViewTypeList.add(getViewType(c))
            }
            mViewTypeList.add(ViewType.FOOTER)
        }
        for (i in 0..mViewTypeList.size) {
            mViewHasAnimatedList.add(false)
        }
    }

    fun setNullWeather() {
        mViewHasAnimatedList.clear()
        mViewTypeList.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractMainViewHolder = when (viewType) {
        ViewType.HEADER -> HeaderViewHolder(parent)
        ViewType.ALERT -> AlertViewHolder(parent)
        ViewType.PRECIPITATION_NOWCAST -> PrecipitationNowcastViewHolder(parent)
        ViewType.DAILY -> DailyViewHolder(parent)
        ViewType.HOURLY -> HourlyViewHolder(parent)
        ViewType.PRECIPITATION -> PrecipitationViewHolder(parent)
        ViewType.WIND -> WindViewHolder(parent)
        ViewType.HUMIDITY -> HumidityViewHolder(parent)
        ViewType.UV -> UvViewHolder(parent)
        ViewType.AIR_QUALITY -> AirQualityViewHolder(parent)
        ViewType.POLLEN -> PollenViewHolder(parent)
        ViewType.VISIBILITY -> VisibilityViewHolder(parent)
        ViewType.PRESSURE -> PressureViewHolder(parent)
        ViewType.SUN -> SunViewHolder(parent)
        ViewType.MOON -> MoonViewHolder(parent)
        ViewType.CLOCK -> ClockViewHolder(parent)
        ViewType.FOOTER -> FooterViewHolder(ComposeView(parent.context))
        else -> FooterViewHolder(ComposeView(parent.context))
    }

    override fun onBindViewHolder(holder: AbstractMainViewHolder, position: Int) {
        mLocation?.let {
            if (holder is AbstractMainCardViewHolder) {
                if (holder is DailyViewHolder || holder is HourlyViewHolder) {
                    holder.onBindView(
                        mActivity,
                        mLocation!!,
                        mProvider!!,
                        mListAnimationEnabled && mViewHasAnimatedList.getOrElse(position) { null } != true,
                        mItemAnimationEnabled && mViewHasAnimatedList.getOrElse(position) { null } != true,
                        if (holder is HourlyViewHolder) selectedHourlyTab else selectedDailyTab,
                        if (holder is HourlyViewHolder) {
                            { tab -> selectedHourlyTab = tab }
                        } else {
                            { tab -> selectedDailyTab = tab }
                        }
                    )
                } else {
                    holder.onBindView(
                        mActivity,
                        mLocation!!,
                        mProvider!!,
                        mListAnimationEnabled && mViewHasAnimatedList.getOrElse(position) { null } != true,
                        mItemAnimationEnabled && mViewHasAnimatedList.getOrElse(position) { null } != true
                    )
                }
            } else {
                holder.onBindView(
                    mActivity,
                    mLocation!!,
                    mProvider!!,
                    mListAnimationEnabled && mViewHasAnimatedList.getOrElse(position) { null } != true,
                    mItemAnimationEnabled && mViewHasAnimatedList.getOrElse(position) { null } != true
                )
            }
            mHost!!.post {
                holder.checkEnterScreen(
                    mHost!!,
                    mPendingAnimatorList ?: ArrayList(),
                    mListAnimationEnabled && mViewHasAnimatedList.getOrElse(position) { null } != true
                )
            }
        }
    }

    override fun onViewRecycled(holder: AbstractMainViewHolder) {
        holder.onRecycleView()
    }

    override fun getItemCount() = mViewTypeList.size

    override fun getItemViewType(position: Int) = mViewTypeList[position]

    fun onScroll() {
        for (i in 0 until itemCount) {
            val holder = mHost!!.findViewHolderForAdapterPosition(i) as AbstractMainViewHolder?
            holder?.checkEnterScreen(
                mHost!!,
                mPendingAnimatorList ?: ArrayList(),
                mListAnimationEnabled && mViewHasAnimatedList.getOrElse(i) { null } != true
            )?.let { hasAnimated ->
                if (hasAnimated) {
                    mViewHasAnimatedList[i] = true
                }
            }
        }
    }

    fun isDraggable(position: Int): Boolean {
        return mViewTypeList[position] !in arrayOf(
            ViewType.HEADER,
            ViewType.ALERT,
            ViewType.FOOTER
        )
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val fromCard = getCardDisplay(getItemViewType(fromPosition))
        val toCard = getCardDisplay(getItemViewType(toPosition))

        if (fromCard == null || toCard == null) {
            // LogHelper.log(msg = "[Homepage Drag & Drop] No matching cards")
            return false
        }

        val cardDisplayList = SettingsManager.getInstance(mActivity).cardDisplayList
        val fromCardPosition = cardDisplayList.indexOf(fromCard)
        val toCardPosition = cardDisplayList.indexOf(toCard)

        if (fromCardPosition == -1 || toCardPosition == -1) {
            // LogHelper.log(msg = "[Homepage Drag & Drop] Can’t find one of the two cards positions")
            return false
        }

        Collections.swap(mViewTypeList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)

        // LogHelper.log(msg = "[Homepage Drag & Drop] Before: ${CardDisplay.toValue(cardDisplayList)}")
        Collections.swap(cardDisplayList, fromCardPosition, toCardPosition)
        // LogHelper.log(msg = "[Homepage Drag & Drop] After: ${CardDisplay.toValue(cardDisplayList)}")
        SettingsManager.getInstance(mActivity).cardDisplayList = cardDisplayList

        return true
    }

    val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return makeMovementFlags(
                if (isDraggable(viewHolder.layoutPosition)) {
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                } else {
                    0
                },
                0
            )
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ): Boolean {
            if (!isDraggable(target.layoutPosition)) {
                return false
            }

            return onItemMove(viewHolder.layoutPosition, target.layoutPosition)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    })

    companion object {
        private fun getViewType(cardDisplay: CardDisplay): Int = when (cardDisplay) {
            CardDisplay.CARD_NOWCAST -> ViewType.PRECIPITATION_NOWCAST
            CardDisplay.CARD_DAILY_FORECAST -> ViewType.DAILY
            CardDisplay.CARD_HOURLY_FORECAST -> ViewType.HOURLY
            CardDisplay.CARD_PRECIPITATION -> ViewType.PRECIPITATION
            CardDisplay.CARD_WIND -> ViewType.WIND
            CardDisplay.CARD_AIR_QUALITY -> ViewType.AIR_QUALITY
            CardDisplay.CARD_POLLEN -> ViewType.POLLEN
            CardDisplay.CARD_HUMIDITY -> ViewType.HUMIDITY
            CardDisplay.CARD_UV -> ViewType.UV
            CardDisplay.CARD_VISIBILITY -> ViewType.VISIBILITY
            CardDisplay.CARD_PRESSURE -> ViewType.PRESSURE
            CardDisplay.CARD_SUN -> ViewType.SUN
            CardDisplay.CARD_MOON -> ViewType.MOON
            CardDisplay.CARD_CLOCK -> ViewType.CLOCK
        }

        private fun getCardDisplay(viewType: Int): CardDisplay? = when (viewType) {
            ViewType.PRECIPITATION_NOWCAST -> CardDisplay.CARD_NOWCAST
            ViewType.DAILY -> CardDisplay.CARD_DAILY_FORECAST
            ViewType.HOURLY -> CardDisplay.CARD_HOURLY_FORECAST
            ViewType.PRECIPITATION -> CardDisplay.CARD_PRECIPITATION
            ViewType.WIND -> CardDisplay.CARD_WIND
            ViewType.AIR_QUALITY -> CardDisplay.CARD_AIR_QUALITY
            ViewType.POLLEN -> CardDisplay.CARD_POLLEN
            ViewType.HUMIDITY -> CardDisplay.CARD_HUMIDITY
            ViewType.UV -> CardDisplay.CARD_UV
            ViewType.VISIBILITY -> CardDisplay.CARD_VISIBILITY
            ViewType.PRESSURE -> CardDisplay.CARD_PRESSURE
            ViewType.SUN -> CardDisplay.CARD_SUN
            ViewType.MOON -> CardDisplay.CARD_MOON
            ViewType.CLOCK -> CardDisplay.CARD_CLOCK
            else -> null
        }
    }
}
