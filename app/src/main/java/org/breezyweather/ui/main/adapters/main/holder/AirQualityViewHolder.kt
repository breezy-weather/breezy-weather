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

package org.breezyweather.ui.main.adapters.main.holder

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.validAirQuality
import org.breezyweather.ui.common.widgets.ArcProgress
import org.breezyweather.ui.main.adapters.AqiAdapter
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController

class AirQualityViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_aqi, parent, false)
) {
    private val mTitle: TextView = itemView.findViewById(R.id.container_main_aqi_title)
    private val mTime: TextView = itemView.findViewById(R.id.container_main_aqi_time)
    private val mProgress: ArcProgress = itemView.findViewById(R.id.container_main_aqi_progress)
    private val mRecyclerView: RecyclerView = itemView.findViewById(R.id.container_main_aqi_recyclerView)
    private var mAdapter: AqiAdapter? = null
    private var mAqiIndex = 0
    private var mEnable = false
    private var mAttachAnimatorSet: AnimatorSet? = null

    @SuppressLint("DefaultLocale")
    override fun onBindView(
        activity: GeoActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
        firstCard: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled, firstCard)

        val isDaily = (location.weather?.current?.airQuality?.isIndexValid != true)
        location.weather!!.validAirQuality?.let { airQuality ->
            mAqiIndex = airQuality.getIndex() ?: 0
            mEnable = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mTitle.isAccessibilityHeading = true
            }
            mTitle.setTextColor(
                ThemeManager.getInstance(context)
                    .weatherThemeDelegate
                    .getThemeColors(
                        context,
                        WeatherViewController.getWeatherKind(location),
                        WeatherViewController.isDaylight(location)
                    )[0]
            )
            mTime.text = if (isDaily) {
                context.getString(R.string.daily_today_short)
            } else {
                location.weather!!.base.refreshTime?.getFormattedTime(location, context, context.is12Hour)
            }
            mTime.contentDescription = if (isDaily) {
                context.getString(R.string.daily_today)
            } else {
                location.weather!!.base.refreshTime?.getFormattedTime(location, context, context.is12Hour)
            }
            if (itemAnimationEnabled) {
                mProgress.apply {
                    progress = 0f
                    setText(String.format("%d", 0))
                    setProgressColor(
                        ContextCompat.getColor(context, R.color.colorLevel_1),
                        MainThemeColorProvider.isLightTheme(context, location)
                    )
                    setArcBackgroundColor(
                        MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
                    )
                }
            } else {
                val aqiColor = airQuality.getColor(mProgress.context)
                mProgress.apply {
                    progress = mAqiIndex.toFloat()
                    setText(String.format("%d", mAqiIndex))
                    setProgressColor(aqiColor, MainThemeColorProvider.isLightTheme(context, location))
                    setArcBackgroundColor(ColorUtils.setAlphaComponent(aqiColor, (255 * 0.1).toInt()))
                }
            }
            mProgress.apply {
                setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorTitleText))
                setBottomText(airQuality.getName(context))
                setBottomTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
                contentDescription = mAqiIndex.toString() + ", " + airQuality.getName(context)
                max = PollutantIndex.indexExcessivePollution.toFloat()
            }
            itemView.setOnClickListener {
                IntentHelper.startDailyWeatherActivity(
                    context as GeoActivity,
                    location.formattedId,
                    location.weather!!.todayIndex,
                    DetailScreen.TAG_AIR_QUALITY
                )
            }
        }
        mAdapter = AqiAdapter(context, location, itemAnimationEnabled)
        mRecyclerView.adapter = mAdapter
        // Without this, the click event is not performed
        mRecyclerView.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                view.performClick()
            } else {
                false
            }
        }
        mRecyclerView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as GeoActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_AIR_QUALITY
            )
        }
        mRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    @SuppressLint("DefaultLocale")
    override fun onEnterScreen() {
        if (itemAnimationEnabled && mEnable) {
            mLocation!!.weather!!.validAirQuality?.let { airQuality ->
                val aqiColor = airQuality.getColor(mProgress.context)
                val progressColor = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    ContextCompat.getColor(context, R.color.colorLevel_1),
                    aqiColor
                )
                progressColor.addUpdateListener { animation: ValueAnimator ->
                    mProgress.setProgressColor(
                        animation.animatedValue as Int,
                        MainThemeColorProvider.isLightTheme(context, mLocation!!)
                    )
                }
                val backgroundColor = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    MainThemeColorProvider.getColor(
                        mLocation!!.isDaylight,
                        com.google.android.material.R.attr.colorOutline
                    ),
                    ColorUtils.setAlphaComponent(aqiColor, (255 * 0.1).toInt())
                )
                backgroundColor.addUpdateListener { animation: ValueAnimator ->
                    mProgress.setArcBackgroundColor((animation.animatedValue as Int))
                }
                val aqiNumber = ValueAnimator.ofObject(FloatEvaluator(), 0, mAqiIndex)
                aqiNumber.addUpdateListener { animation: ValueAnimator ->
                    mProgress.apply {
                        progress = (animation.animatedValue as Float)
                        setText(String.format("%d", mProgress.progress.toInt()))
                    }
                }
                mAttachAnimatorSet = AnimatorSet().apply {
                    playTogether(progressColor, backgroundColor, aqiNumber)
                    interpolator = DecelerateInterpolator()
                    duration = (1500 + mAqiIndex / 400f * 1500).toLong()
                    start()
                }
                mAdapter!!.executeAnimation()
            }
        }
    }

    override fun onRecycleView() {
        super.onRecycleView()
        mAttachAnimatorSet?.let {
            if (it.isRunning) it.cancel()
        }
        mAttachAnimatorSet = null
        mAdapter?.cancelAnimation()
    }
}
