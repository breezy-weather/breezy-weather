package org.breezyweather.main.adapters.main.holder

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.index.PollutantIndex
import org.breezyweather.common.ui.widgets.ArcProgress
import org.breezyweather.main.adapters.AqiAdapter
import org.breezyweather.main.utils.MainThemeColorProvider
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.resource.providers.ResourceProvider
import org.breezyweather.theme.weatherView.WeatherViewController.getWeatherKind

class AirQualityViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(R.layout.container_main_aqi, parent, false)
) {
    private val mTitle: TextView = itemView.findViewById(R.id.container_main_aqi_title)
    private val mProgress: ArcProgress = itemView.findViewById(R.id.container_main_aqi_progress)
    private val mRecyclerView: RecyclerView = itemView.findViewById(R.id.container_main_aqi_recyclerView)
    private var mAdapter: AqiAdapter? = null
    private var mAqiIndex = 0
    private var mEnable = false
    private var mAttachAnimatorSet: AnimatorSet? = null

    @SuppressLint("DefaultLocale")
    override fun onBindView(
        activity: GeoActivity, location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean, firstCard: Boolean
    ) {
        super.onBindView(
            activity, location, provider,
            listAnimationEnabled, itemAnimationEnabled, firstCard
        )
        location.weather!!.current?.let { current ->
            current.airQuality?.let { airQuality ->
                mAqiIndex = airQuality.getIndex() ?: 0
                mEnable = true
                mTitle.setTextColor(
                    ThemeManager.getInstance(context)
                        .weatherThemeDelegate
                        .getThemeColors(
                            context,
                            getWeatherKind(current.weatherCode),
                            location.isDaylight
                        )[0]
                )
                if (itemAnimationEnabled) {
                    mProgress.apply {
                        progress = 0f
                        setText(String.format("%d", 0))
                        setProgressColor(ContextCompat.getColor(context, R.color.colorLevel_1), MainThemeColorProvider.isLightTheme(context, location))
                        setArcBackgroundColor(MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline))
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
            }
        }
        mAdapter = AqiAdapter(context, location, itemAnimationEnabled)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    @SuppressLint("DefaultLocale")
    override fun onEnterScreen() {
        if (itemAnimationEnabled && mEnable) {
            mLocation?.weather?.current?.airQuality?.let { airQuality ->
                val aqiColor = airQuality.getColor(mProgress.context)
                val progressColor = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    ContextCompat.getColor(context, R.color.colorLevel_1),
                    aqiColor
                )
                progressColor.addUpdateListener { animation: ValueAnimator ->
                    mProgress.setProgressColor(animation.animatedValue as Int, MainThemeColorProvider.isLightTheme(context, mLocation!!))
                }
                val backgroundColor = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    MainThemeColorProvider.getColor(mLocation!!.isDaylight, com.google.android.material.R.attr.colorOutline),
                    ColorUtils.setAlphaComponent(aqiColor, (255 * 0.1).toInt())
                )
                backgroundColor.addUpdateListener { animation: ValueAnimator -> mProgress.setArcBackgroundColor((animation.animatedValue as Int)) }
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