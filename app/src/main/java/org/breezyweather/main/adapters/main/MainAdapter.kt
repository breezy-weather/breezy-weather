package org.breezyweather.main.adapters.main

import android.animation.Animator
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.appearance.CardDisplay
import org.breezyweather.main.adapters.main.holder.*
import org.breezyweather.settings.SettingsManager
import org.breezyweather.sources.SourceManager
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherView

class MainAdapter(
    activity: GeoActivity, host: RecyclerView, weatherView: WeatherView, location: Location?,
    private val sourceManager: SourceManager,
    provider: ResourceProvider, listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean
) : RecyclerView.Adapter<AbstractMainViewHolder?>() {
    private lateinit var mActivity: GeoActivity
    private var mHost: RecyclerView? = null
    private var mWeatherView: WeatherView? = null
    private var mLocation: Location? = null
    private var mProvider: ResourceProvider? = null
    private val mViewTypeList: MutableList<Int> = ArrayList()
    private var mFirstCardPosition: Int? = null
    private var mPendingAnimatorList: MutableList<Animator>? = null
    private var mHeaderCurrentTemperatureTextHeight = 0
    private var mListAnimationEnabled = false
    private var mItemAnimationEnabled = false

    init {
        update(activity, host, weatherView, location, provider, listAnimationEnabled, itemAnimationEnabled)
    }

    fun update(
        activity: GeoActivity, host: RecyclerView, weatherView: WeatherView, location: Location?,
        provider: ResourceProvider, listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean
    ) {
        mActivity = activity
        mHost = host
        mWeatherView = weatherView
        mLocation = location
        mProvider = provider
        mViewTypeList.clear()
        mFirstCardPosition = null
        mPendingAnimatorList = ArrayList()
        mHeaderCurrentTemperatureTextHeight = -1
        mListAnimationEnabled = listAnimationEnabled
        mItemAnimationEnabled = itemAnimationEnabled
        if (location?.weather != null) {
            val weather = location.weather
            val cardDisplayList = SettingsManager.getInstance(activity).cardDisplayList
            mViewTypeList.add(ViewType.HEADER)
            for (c in cardDisplayList) {
                if (c === CardDisplay.CARD_AIR_QUALITY && weather.validAirQuality == null) {
                    continue
                }
                if (c === CardDisplay.CARD_ALLERGEN
                    && (weather.dailyForecast.isEmpty() || weather.dailyForecast[0].allergen == null || !weather.dailyForecast[0].allergen!!.isValid)
                ) {
                    continue
                }
                if (c === CardDisplay.CARD_SUNRISE_SUNSET
                    && (weather.dailyForecast.isEmpty() || weather.dailyForecast[0].sun == null || !weather.dailyForecast[0].sun!!.isValid)
                ) {
                    continue
                }
                if (c === CardDisplay.CARD_LIVE
                    && (location.weather.current == null
                            || (DetailsViewHolder.availableDetails(activity, SettingsManager.getInstance(activity).detailDisplayUnlisted, location.weather.current, location.isDaylight)).isEmpty()
                    )) {
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
        ViewType.DAILY -> DailyViewHolder(parent)
        ViewType.HOURLY -> HourlyViewHolder(parent)
        ViewType.AIR_QUALITY -> AirQualityViewHolder(parent)
        ViewType.ALLERGEN -> AllergenViewHolder(parent)
        ViewType.ASTRO -> AstroViewHolder(parent)
        ViewType.LIVE -> DetailsViewHolder(parent)
        else -> FooterViewHolder(parent)
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
            } else if (holder is FooterViewHolder) {
                holder.onBindView(
                    mActivity,
                    mLocation!!,
                    mProvider!!,
                    mListAnimationEnabled,
                    mItemAnimationEnabled,
                    sourceManager.getWeatherSource(mLocation!!.weatherSource)
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
            if (type == ViewType.DAILY || type == ViewType.HOURLY || type == ViewType.AIR_QUALITY
                || type == ViewType.ALLERGEN || type == ViewType.ASTRO || type == ViewType.LIVE) {
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
            CardDisplay.CARD_DAILY_OVERVIEW -> ViewType.DAILY
            CardDisplay.CARD_HOURLY_OVERVIEW -> ViewType.HOURLY
            CardDisplay.CARD_AIR_QUALITY -> ViewType.AIR_QUALITY
            CardDisplay.CARD_ALLERGEN -> ViewType.ALLERGEN
            CardDisplay.CARD_SUNRISE_SUNSET -> ViewType.ASTRO
            else -> ViewType.LIVE
        }
    }
}
