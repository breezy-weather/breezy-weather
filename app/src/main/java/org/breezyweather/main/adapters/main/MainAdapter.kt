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

package org.breezyweather.main.adapters.main

import android.animation.Animator
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import org.breezyweather.common.basic.models.options.appearance.CardDisplay
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.weather.model.hasMinutelyPrecipitation
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.domain.weather.model.validAirQuality
import org.breezyweather.main.MainActivity
import org.breezyweather.main.adapters.main.holder.AbstractMainCardViewHolder
import org.breezyweather.main.adapters.main.holder.AbstractMainViewHolder
import org.breezyweather.main.adapters.main.holder.AirQualityViewHolder
import org.breezyweather.main.adapters.main.holder.AstroViewHolder
import org.breezyweather.main.adapters.main.holder.DailyViewHolder
import org.breezyweather.main.adapters.main.holder.DetailsViewHolder
import org.breezyweather.main.adapters.main.holder.FooterViewHolder
import org.breezyweather.main.adapters.main.holder.HeaderViewHolder
import org.breezyweather.main.adapters.main.holder.HourlyViewHolder
import org.breezyweather.main.adapters.main.holder.PollenViewHolder
import org.breezyweather.main.adapters.main.holder.PrecipitationNowcastViewHolder
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherView

class MainAdapter(
    activity: MainActivity, host: RecyclerView, weatherView: WeatherView, location: Location?,
    provider: ResourceProvider, listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean
) : RecyclerView.Adapter<AbstractMainViewHolder?>() {
    private lateinit var mActivity: MainActivity
    private var mHost: RecyclerView? = null
    private var mWeatherView: WeatherView? = null
    private var mLocation: Location? = null
    private var mProvider: ResourceProvider? = null
    private val mViewTypeList: MutableList<Int> = mutableListOf()
    private var mFirstCardPosition: Int? = null
    private var mPendingAnimatorList: MutableList<Animator>? = null
    private var mHeaderCurrentTemperatureTextHeight = 0
    private var mListAnimationEnabled = false
    private var mItemAnimationEnabled = false

    init {
        update(activity, host, weatherView, location, provider, listAnimationEnabled, itemAnimationEnabled)
    }

    fun update(
        activity: MainActivity, host: RecyclerView, weatherView: WeatherView, location: Location?,
        provider: ResourceProvider, listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean
    ) {
        mActivity = activity
        mHost = host
        mWeatherView = weatherView
        mLocation = location
        mProvider = provider
        mViewTypeList.clear()
        mFirstCardPosition = null
        mPendingAnimatorList = mutableListOf()
        mHeaderCurrentTemperatureTextHeight = -1
        mListAnimationEnabled = listAnimationEnabled
        mItemAnimationEnabled = itemAnimationEnabled
        location?.weather?.let { weather ->
            val cardDisplayList = SettingsManager.getInstance(activity).cardDisplayList
            mViewTypeList.add(ViewType.HEADER)
            for (c in cardDisplayList) {
                if (c === CardDisplay.CARD_PRECIPITATION_NOWCAST &&
                    (!weather.hasMinutelyPrecipitation || weather.minutelyForecast.size < 3)
                ) {
                    continue
                }
                if (c === CardDisplay.CARD_DAILY_OVERVIEW && weather.dailyForecast.isEmpty()) {
                    continue
                }
                if (c === CardDisplay.CARD_HOURLY_OVERVIEW && weather.nextHourlyForecast.isEmpty()) {
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
                if (c === CardDisplay.CARD_SUNRISE_SUNSET &&
                    (weather.dailyForecast.isEmpty() || weather.today?.sun?.isValid != true)
                ) {
                    continue
                }
                if (c === CardDisplay.CARD_LIVE &&
                    (weather.current == null ||
                        (DetailsViewHolder.availableDetails(
                            activity,
                            SettingsManager.getInstance(activity).detailDisplayList,
                            SettingsManager.getInstance(activity).detailDisplayUnlisted,
                            weather.current!!,
                            location.isDaylight
                        )).isEmpty()
                    )
                ) {
                    continue
                }
                mViewTypeList.add(getViewType(c))
            }
            mViewTypeList.add(ViewType.FOOTER)
            ensureFirstCard()
        }
    }

    fun setNullWeather() {
        mViewTypeList.clear()
        ensureFirstCard()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractMainViewHolder = when (viewType) {
        ViewType.HEADER -> HeaderViewHolder(parent, mWeatherView!!)
        ViewType.PRECIPITATION_NOWCAST -> PrecipitationNowcastViewHolder(parent)
        ViewType.DAILY -> DailyViewHolder(parent)
        ViewType.HOURLY -> HourlyViewHolder(parent)
        ViewType.AIR_QUALITY -> AirQualityViewHolder(parent)
        ViewType.POLLEN -> PollenViewHolder(parent)
        ViewType.ASTRO -> AstroViewHolder(parent)
        ViewType.LIVE -> DetailsViewHolder(parent)
        ViewType.FOOTER -> FooterViewHolder(ComposeView(parent.context))
        else -> FooterViewHolder(ComposeView(parent.context))
    }

    override fun onBindViewHolder(holder: AbstractMainViewHolder, position: Int) {
        mLocation?.let {
            if (holder is AbstractMainCardViewHolder) {
                holder.onBindView(
                    mActivity,
                    mLocation!!,
                    mProvider!!,
                    mListAnimationEnabled,
                    mItemAnimationEnabled,
                    mFirstCardPosition != null && mFirstCardPosition == position
                )
            } else {
                holder.onBindView(mActivity, mLocation!!, mProvider!!, mListAnimationEnabled, mItemAnimationEnabled)
            }
            mHost!!.post { holder.checkEnterScreen(mHost!!, mPendingAnimatorList ?: ArrayList(), mListAnimationEnabled) }
        }
    }

    override fun onViewRecycled(holder: AbstractMainViewHolder) {
        holder.onRecycleView()
    }

    override fun getItemCount() = mViewTypeList.size

    override fun getItemViewType(position: Int) = mViewTypeList[position]

    private fun ensureFirstCard() {
        mFirstCardPosition = null
        for (i in 0 until itemCount) {
            val type = getItemViewType(i)
            if (setOf(ViewType.PRECIPITATION_NOWCAST, ViewType.DAILY, ViewType.HOURLY, ViewType.AIR_QUALITY, ViewType.POLLEN, ViewType.ASTRO, ViewType.LIVE).contains(type)) {
                mFirstCardPosition = i
                return
            }
        }
    }

    val headerTop: Int
        get() {
            if (mHeaderCurrentTemperatureTextHeight <= 0 && itemCount > 0) {
                val holder = mHost!!.findViewHolderForAdapterPosition(0) as AbstractMainViewHolder?
                if (holder is HeaderViewHolder) {
                    mHeaderCurrentTemperatureTextHeight = holder.headerTop
                }
            }
            return mHeaderCurrentTemperatureTextHeight
        }

    fun onScroll() {
        var holder: AbstractMainViewHolder?
        for (i in 0 until itemCount) {
            holder = mHost!!.findViewHolderForAdapterPosition(i) as AbstractMainViewHolder?
            holder?.checkEnterScreen(mHost!!, mPendingAnimatorList ?: ArrayList(), mListAnimationEnabled)
        }
    }

    companion object {
        private fun getViewType(cardDisplay: CardDisplay): Int = when (cardDisplay) {
            CardDisplay.CARD_PRECIPITATION_NOWCAST -> ViewType.PRECIPITATION_NOWCAST
            CardDisplay.CARD_DAILY_OVERVIEW -> ViewType.DAILY
            CardDisplay.CARD_HOURLY_OVERVIEW -> ViewType.HOURLY
            CardDisplay.CARD_AIR_QUALITY -> ViewType.AIR_QUALITY
            CardDisplay.CARD_POLLEN -> ViewType.POLLEN
            CardDisplay.CARD_SUNRISE_SUNSET -> ViewType.ASTRO
            else -> ViewType.LIVE
        }
    }
}
