package org.breezyweather.main.adapters

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.index.PollutantIndex
import org.breezyweather.common.basic.models.options.unit.AirQualityCOUnit
import org.breezyweather.common.basic.models.options.unit.AirQualityUnit
import org.breezyweather.common.ui.widgets.RoundProgress
import org.breezyweather.main.utils.MainThemeColorProvider

class AqiAdapter(context: Context, location: Location, executeAnimation: Boolean) :
    RecyclerView.Adapter<AqiAdapter.ViewHolder>() {
    private val mLightTheme: Boolean
    private val mItemList: MutableList<AqiItem>
    private val mHolderList: MutableList<ViewHolder>

    class AqiItem(
        @field:ColorInt val color: Int,
        val progress: Float,
        val max: Float,
        val title: String,
        val content: String,
        val talkBack: String,
        val executeAnimation: Boolean
    )

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mItem: AqiItem? = null
        private var mLightTheme: Boolean? = null
        private var mExecuteAnimation = false
        private var mAttachAnimatorSet: AnimatorSet? = null
        private val mTitle: TextView
        private val mContent: TextView
        private val mProgress: RoundProgress

        init {
            mTitle = itemView.findViewById(R.id.item_aqi_title)
            mContent = itemView.findViewById(R.id.item_aqi_content)
            mProgress = itemView.findViewById(R.id.item_aqi_progress)
        }

        fun onBindView(lightTheme: Boolean, item: AqiItem) {
            val context = itemView.context
            mItem = item
            mLightTheme = lightTheme
            mExecuteAnimation = item.executeAnimation
            itemView.contentDescription = item.talkBack
            mTitle.text = item.title
            mTitle.setTextColor(MainThemeColorProvider.getColor(lightTheme, R.attr.colorTitleText))
            mContent.text = item.content
            mContent.setTextColor(MainThemeColorProvider.getColor(lightTheme, R.attr.colorBodyText))
            if (mExecuteAnimation) {
                mProgress.apply {
                    progress = 0f
                    setProgressColor(ContextCompat.getColor(context, R.color.colorLevel_1))
                    setProgressBackgroundColor(
                        MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorOutline)
                    )
                }
            } else {
                mProgress.apply {
                    progress = (100.0 * item.progress / item.max).toInt().toFloat()
                    setProgressColor(item.color)
                    setProgressBackgroundColor(
                        ColorUtils.setAlphaComponent(item.color, (255 * 0.1).toInt())
                    )
                }
            }
        }

        fun executeAnimation() {
            if (mExecuteAnimation) {
                mItem?.let { item ->
                    mExecuteAnimation = false
                    val progressColor = ValueAnimator.ofObject(
                        ArgbEvaluator(),
                        ContextCompat.getColor(itemView.context, R.color.colorLevel_1),
                        item.color
                    )
                    progressColor.addUpdateListener { animation: ValueAnimator ->
                        mProgress.setProgressColor((animation.animatedValue as Int))
                    }
                    val backgroundColor = ValueAnimator.ofObject(
                        ArgbEvaluator(),
                        MainThemeColorProvider.getColor(mLightTheme ?: false, com.google.android.material.R.attr.colorOutline),
                        ColorUtils.setAlphaComponent(item.color, (255 * 0.1).toInt())
                    )
                    backgroundColor.addUpdateListener { animation: ValueAnimator -> mProgress.setProgressBackgroundColor((animation.animatedValue as Int)) }
                    val aqiNumber = ValueAnimator.ofObject(FloatEvaluator(), 0, item.progress)
                    aqiNumber.addUpdateListener { animation: ValueAnimator ->
                        mProgress.progress = 100.0f * animation.animatedValue as Float / item.max
                    }
                    mAttachAnimatorSet = AnimatorSet().apply {
                        playTogether(progressColor, backgroundColor, aqiNumber)
                        interpolator = DecelerateInterpolator(3f)
                        duration = (item.progress / item.max * 5000).toLong()
                        start()
                    }
                }
            }
        }

        fun cancelAnimation() {
            mAttachAnimatorSet?.let {
                if (it.isRunning) it.cancel()
            }
            mAttachAnimatorSet = null
        }
    }

    init {
        mLightTheme = MainThemeColorProvider.isLightTheme(context, location)
        mItemList = ArrayList()
        if (location.weather?.current?.airQuality != null && location.weather.current.airQuality.isValid) {
            val airQuality = location.weather.current.airQuality
            // We use air quality index for the progress bar instead of concentration for more realistic bar
            if (airQuality.pM25 != null) {
                mItemList.add(
                    AqiItem(
                        airQuality.getColor(context, PollutantIndex.PM25),
                        airQuality.getIndex(PollutantIndex.PM25)!!.toFloat(),
                        PollutantIndex.indexExcessivePollution.toFloat(),
                        "PM2.5",
                        AirQualityUnit.MUGPCUM.getValueText(context, airQuality.pM25),
                        context.getString(R.string.air_quality_pm25_voice)
                                + ", "
                                + AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.pM25),
                        executeAnimation
                    )
                )
            }
            if (airQuality.pM10 != null) {
                mItemList.add(
                    AqiItem(
                        airQuality.getColor(context, PollutantIndex.PM10),
                        airQuality.getIndex(PollutantIndex.PM10)!!.toFloat(),
                        PollutantIndex.indexExcessivePollution.toFloat(),
                        "PM10",
                        AirQualityUnit.MUGPCUM.getValueText(context, airQuality.pM10),
                        context.getString(R.string.air_quality_pm10_voice)
                                + ", " + AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.pM10),
                        executeAnimation
                    )
                )
            }
            if (airQuality.o3 != null) {
                mItemList.add(
                    AqiItem(
                        airQuality.getColor(context, PollutantIndex.O3),
                        airQuality.getIndex(PollutantIndex.O3)!!.toFloat(),
                        PollutantIndex.indexExcessivePollution.toFloat(),
                        "O₃",
                        AirQualityUnit.MUGPCUM.getValueText(context, airQuality.o3),
                        context.getString(R.string.air_quality_o3_voice)
                                + ", " + AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.o3),
                        executeAnimation
                    )
                )
            }
            if (airQuality.nO2 != null) {
                mItemList.add(
                    AqiItem(
                        airQuality.getColor(context, PollutantIndex.NO2),
                        airQuality.getIndex(PollutantIndex.NO2)!!.toFloat(),
                        PollutantIndex.indexExcessivePollution.toFloat(),
                        "NO₂",
                        AirQualityUnit.MUGPCUM.getValueText(context, airQuality.nO2),
                        context.getString(R.string.air_quality_no2_voice)
                                + ", " + AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.nO2),
                        executeAnimation
                    )
                )
            }
            if (airQuality.sO2 != null && airQuality.sO2 > 0) {
                mItemList.add(
                    AqiItem(
                        airQuality.getColor(context, PollutantIndex.SO2),
                        airQuality.getIndex(PollutantIndex.SO2)!!.toFloat(),
                        PollutantIndex.indexExcessivePollution.toFloat(),
                        "SO₂",
                        AirQualityUnit.MUGPCUM.getValueText(context, airQuality.sO2),
                        context.getString(R.string.air_quality_so2_voice)
                                + ", " + AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.sO2),
                        executeAnimation
                    )
                )
            }
            if (airQuality.cO != null && airQuality.cO > 0) {
                mItemList.add(
                    AqiItem(
                        airQuality.getColor(context, PollutantIndex.CO),
                        airQuality.getIndex(PollutantIndex.CO)!!.toFloat(),
                        PollutantIndex.indexExcessivePollution.toFloat(),
                        "CO",
                        AirQualityCOUnit.MGPCUM.getValueText(context, airQuality.cO),
                        context.getString(R.string.air_quality_co_voice)
                                + ", " + AirQualityCOUnit.MGPCUM.getValueVoice(context, airQuality.cO),
                        executeAnimation
                    )
                )
            }
        }
        mHolderList = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aqi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindView(mLightTheme, mItemList[position])
        if (mItemList[position].executeAnimation) {
            mHolderList.add(holder)
        }
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun executeAnimation() {
        for (viewHolder in mHolderList) {
            viewHolder.executeAnimation()
        }
    }

    fun cancelAnimation() {
        for (viewHolder in mHolderList) {
            viewHolder.cancelAnimation()
        }
        mHolderList.clear()
    }
}
