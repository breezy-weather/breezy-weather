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

package org.breezyweather.ui.main.adapters

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
import breezyweather.domain.location.model.Location
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.R
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getConcentration
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.validAirQuality
import org.breezyweather.ui.common.widgets.RoundProgress
import org.breezyweather.ui.main.utils.MainThemeColorProvider

class AqiAdapter(
    context: Context,
    location: Location,
    executeAnimation: Boolean,
) : RecyclerView.Adapter<AqiAdapter.ViewHolder>() {
    private val mLightTheme: Boolean
    private val mItemList: ImmutableList<AqiItem>
    private val mHolderList: MutableList<ViewHolder>

    class AqiItem(
        val pollutantType: PollutantIndex,
        @field:ColorInt val color: Int,
        val progress: Float,
        val max: Float,
        val title: String,
        val content: String,
        val talkBack: String,
        val executeAnimation: Boolean,
    )

    class ViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        private var mItem: AqiItem? = null
        private var mLightTheme: Boolean? = null
        private var mExecuteAnimation = false
        private var mAttachAnimatorSet: AnimatorSet? = null
        private val mTitle: TextView = itemView.findViewById(R.id.item_aqi_title)
        private val mContent: TextView = itemView.findViewById(R.id.item_aqi_content)
        private val mProgress: RoundProgress = itemView.findViewById(R.id.item_aqi_progress)

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
            itemView.setOnClickListener {
                // TODO: Open daily details activity on air quality page
                /*IntentHelper.startDailyWeatherActivity(
                    context as GeoActivity,
                    location.formattedId,
                    location.weather!!.todayIndex,
                    ChartDisplay.TAG_AIR_QUALITY
                )*/
            }
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
                        MainThemeColorProvider.getColor(
                            mLightTheme ?: false,
                            com.google.android.material.R.attr.colorOutline
                        ),
                        ColorUtils.setAlphaComponent(item.color, (255 * 0.1).toInt())
                    )
                    backgroundColor.addUpdateListener { animation: ValueAnimator ->
                        mProgress.setProgressBackgroundColor((animation.animatedValue as Int))
                    }
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
        mItemList = buildList {
            location.weather?.validAirQuality?.let { airQuality ->
                // We use air quality index for the progress bar instead of concentration for more realistic bar
                // We use air quality index for the progress bar instead of concentration for more realistic bar
                listOf(PollutantIndex.PM25, PollutantIndex.PM10, PollutantIndex.O3, PollutantIndex.NO2)
                    .forEach { pollutantIndex ->
                        airQuality.getConcentration(pollutantIndex)?.let {
                            add(
                                AqiItem(
                                    pollutantIndex,
                                    airQuality.getColor(context, pollutantIndex),
                                    airQuality.getIndex(pollutantIndex)!!.toFloat(),
                                    PollutantIndex.indexExcessivePollution.toFloat(),
                                    context.getString(pollutantIndex.shortName),
                                    PollutantIndex.getUnit(pollutantIndex).getValueText(context, it),
                                    context.getString(pollutantIndex.voicedName) +
                                        context.getString(R.string.colon_separator) +
                                        PollutantIndex.getUnit(pollutantIndex).getValueVoice(context, it),
                                    executeAnimation
                                )
                            )
                        }
                    }
                listOf(PollutantIndex.SO2, PollutantIndex.CO)
                    .forEach { pollutantIndex ->
                        (airQuality.getConcentration(pollutantIndex) ?: 0.0).let {
                            if (it > 0) {
                                add(
                                    AqiItem(
                                        pollutantIndex,
                                        airQuality.getColor(context, pollutantIndex),
                                        airQuality.getIndex(pollutantIndex)!!.toFloat(),
                                        PollutantIndex.indexExcessivePollution.toFloat(),
                                        context.getString(pollutantIndex.shortName),
                                        PollutantIndex.getUnit(pollutantIndex).getValueText(context, it),
                                        context.getString(pollutantIndex.voicedName) +
                                            context.getString(R.string.colon_separator) +
                                            PollutantIndex.getUnit(pollutantIndex).getValueVoice(context, it),
                                        executeAnimation
                                    )
                                )
                            }
                        }
                    }
            }
        }.toImmutableList()
        mHolderList = mutableListOf()
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
