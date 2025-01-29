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

package org.breezyweather.ui.daily.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.databinding.ItemWeatherDailyPollenBinding
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.ui.daily.DailyWeatherActivity
import org.breezyweather.ui.daily.adapter.holder.AirQualityHolder
import org.breezyweather.ui.daily.adapter.holder.AstroHolder
import org.breezyweather.ui.daily.adapter.holder.LargeTitleHolder
import org.breezyweather.ui.daily.adapter.holder.LineHolder
import org.breezyweather.ui.daily.adapter.holder.MarginHolder
import org.breezyweather.ui.daily.adapter.holder.OverviewHolder
import org.breezyweather.ui.daily.adapter.holder.PollenHolder
import org.breezyweather.ui.daily.adapter.holder.TitleHolder
import org.breezyweather.ui.daily.adapter.holder.UVHolder
import org.breezyweather.ui.daily.adapter.holder.ValueHolder
import org.breezyweather.ui.daily.adapter.holder.ValueIconHolder
import org.breezyweather.ui.daily.adapter.holder.WindHolder
import org.breezyweather.ui.daily.adapter.model.DailyAirQuality
import org.breezyweather.ui.daily.adapter.model.DailyAstro
import org.breezyweather.ui.daily.adapter.model.DailyPollen
import org.breezyweather.ui.daily.adapter.model.DailyUV
import org.breezyweather.ui.daily.adapter.model.DailyWind
import org.breezyweather.ui.daily.adapter.model.LargeTitle
import org.breezyweather.ui.daily.adapter.model.Line
import org.breezyweather.ui.daily.adapter.model.Margin
import org.breezyweather.ui.daily.adapter.model.Overview
import org.breezyweather.ui.daily.adapter.model.Title
import org.breezyweather.ui.daily.adapter.model.Value
import org.breezyweather.ui.daily.adapter.model.ValueIcon
import java.text.NumberFormat
import java.util.Calendar

class DailyWeatherAdapter(
    activity: DailyWeatherActivity,
    location: Location,
    daily: Daily,
    pollenIndexSource: PollenIndexSource?,
    spanCount: Int,
) : RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder>() {
    private val mModelList: MutableList<ViewModel>
    private val mSpanCount: Int = spanCount

    interface ViewModel {
        val code: Int
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun onBindView(model: ViewModel, position: Int)
    }

    var spanSizeLookup: GridLayoutManager.SpanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            val viewType = getItemViewType(position)
            return if (Value.isCode(viewType)) 1 else mSpanCount
        }
    }

    init {
        // model list.
        mModelList = mutableListOf()
        val cal = daily.date.toCalendarWithTimeZone(location.javaTimeZone)
        val thisDayNormals = if (location.weather?.normals?.month == cal[Calendar.MONTH]) {
            location.weather!!.normals
        } else {
            null
        }
        daily.day?.let { day ->
            mModelList.add(LargeTitle(activity.getString(R.string.daytime)))
            mModelList.add(Overview(day, true))
            day.wind?.let { wind ->
                if (wind.isValid) {
                    mModelList.add(DailyWind(wind))
                }
            }
            mModelList.addAll(
                getHalfDayOptionalModelList(activity, day, thisDayNormals?.daytimeTemperature)
            )
        }
        daily.night?.let { night ->
            mModelList.add(Line())
            mModelList.add(LargeTitle(activity.getString(R.string.nighttime)))
            mModelList.add(Overview(night, false))
            night.wind?.let { wind ->
                if (wind.isValid) {
                    mModelList.add(DailyWind(wind))
                }
            }
            mModelList.addAll(
                getHalfDayOptionalModelList(activity, night, thisDayNormals?.nighttimeTemperature)
            )
        }
        mModelList.add(Line())
        daily.airQuality?.let { airQuality ->
            if (airQuality.isIndexValid) {
                mModelList.add(
                    Title(
                        R.drawable.weather_haze_mini_xml,
                        activity.getString(R.string.air_quality)
                    )
                )
                mModelList.add(DailyAirQuality(airQuality))
            }
        }
        daily.pollen?.let { pollen ->
            if (pollen.isIndexValid) {
                mModelList.add(
                    Title(
                        R.drawable.ic_allergy,
                        activity.getString(if (pollen.isMoldValid) R.string.pollen_and_mold else R.string.pollen)
                    )
                )
                mModelList.add(DailyPollen(pollen, pollenIndexSource))
            }
        }
        daily.uV?.let { uV ->
            if (uV.isValid) {
                mModelList.add(
                    Title(
                        R.drawable.ic_uv,
                        activity.getString(R.string.uv_index)
                    )
                )
                mModelList.add(DailyUV(uV))
            }
        }
        if (daily.sun?.isValid == true || daily.moon?.isValid == true || daily.moonPhase?.isValid == true) {
            mModelList.add(LargeTitle(activity.getString(R.string.ephemeris)))
            mModelList.add(DailyAstro(location, daily.sun, daily.moon, daily.moonPhase))
        }
        if (daily.degreeDay?.isValid == true || daily.sunshineDuration != null) {
            mModelList.add(Line())
            mModelList.add(LargeTitle(activity.getString(R.string.details)))
            daily.degreeDay?.let { degreeDay ->
                if (degreeDay.isValid) {
                    val temperatureUnit = SettingsManager.getInstance(activity).temperatureUnit
                    if ((degreeDay.heating ?: 0.0) > 0) {
                        mModelList.add(
                            ValueIcon(
                                activity.getString(R.string.temperature_degree_day_heating),
                                temperatureUnit.getDegreeDayValueText(
                                    activity,
                                    degreeDay.heating!!
                                ),
                                R.drawable.ic_mode_heat
                            )
                        )
                    } else if ((degreeDay.cooling ?: 0.0) > 0) {
                        mModelList.add(
                            ValueIcon(
                                activity.getString(R.string.temperature_degree_day_cooling),
                                temperatureUnit.getDegreeDayValueText(
                                    activity,
                                    degreeDay.cooling!!
                                ),
                                R.drawable.ic_mode_cool
                            )
                        )
                    }
                }
            }
            daily.sunshineDuration?.let { sunshineDuration ->
                mModelList.add(
                    ValueIcon(
                        activity.getString(R.string.sunshine_duration),
                        DurationUnit.H.getValueText(activity, sunshineDuration),
                        R.drawable.ic_sunshine_duration
                    )
                )
            }
        }
        mModelList.add(Margin())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (LargeTitle.isCode(viewType)) {
            return LargeTitleHolder(parent)
        } else if (Overview.isCode(viewType)) {
            return OverviewHolder(parent)
        } else if (Line.isCode(viewType)) {
            return LineHolder(parent)
        } else if (Margin.isCode(viewType)) {
            return MarginHolder(parent)
        } else if (Value.isCode(viewType)) {
            return ValueHolder(parent)
        } else if (ValueIcon.isCode(viewType)) {
            return ValueIconHolder(parent)
        } else if (Title.isCode(viewType)) {
            return TitleHolder(parent)
        } else if (ValueIcon.isCode(viewType)) {
            return ValueIconHolder(parent)
        } else if (DailyAirQuality.isCode(viewType)) {
            return AirQualityHolder(parent)
        } else if (DailyAstro.isCode(viewType)) {
            return AstroHolder(parent)
        } else if (DailyPollen.isCode(viewType)) {
            return PollenHolder(
                ItemWeatherDailyPollenBinding.inflate(
                    LayoutInflater.from(parent.context)
                )
            )
        } else if (DailyUV.isCode(viewType)) {
            return UVHolder(parent)
        } else if (DailyWind.isCode(viewType)) {
            return WindHolder(parent)
        }
        throw RuntimeException("Invalid viewType.")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindView(mModelList[position], position)
    }

    override fun getItemViewType(position: Int) = mModelList[position].code

    override fun getItemCount() = mModelList.size

    private fun getHalfDayOptionalModelList(
        context: Context,
        halfDay: HalfDay,
        normals: Double?,
    ): List<ViewModel> {
        val list = mutableListOf<ViewModel>()
        // temperature.
        val temperature = halfDay.temperature
        val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
        if (temperature?.feelsLikeTemperature != null || normals != null) {
            list.add(
                Title(
                    R.drawable.ic_device_thermostat,
                    context.getString(R.string.temperature)
                )
            )
            if (temperature?.feelsLikeTemperature != null) {
                temperature.realFeelTemperature?.let {
                    list.add(
                        Value(
                            context.getString(R.string.temperature_real_feel),
                            temperatureUnit.getValueText(context, it)
                        )
                    )
                }
                temperature.realFeelShaderTemperature?.let {
                    list.add(
                        Value(
                            context.getString(R.string.temperature_real_feel_shade),
                            temperatureUnit.getValueText(context, it)
                        )
                    )
                }
                temperature.apparentTemperature?.let {
                    list.add(
                        Value(
                            context.getString(R.string.temperature_apparent),
                            temperatureUnit.getValueText(context, it)
                        )
                    )
                }
                temperature.windChillTemperature?.let {
                    list.add(
                        Value(
                            context.getString(R.string.temperature_wind_chill),
                            temperatureUnit.getValueText(context, it)
                        )
                    )
                }
                temperature.wetBulbTemperature?.let {
                    list.add(
                        Value(
                            context.getString(R.string.temperature_wet_bulb),
                            temperatureUnit.getValueText(context, it)
                        )
                    )
                }
            }
            normals?.let {
                list.add(
                    Value(
                        context.getString(R.string.temperature_normal_short),
                        temperatureUnit.getValueText(context, it)
                    )
                )
            }
            list.add(Margin())
        }

        // precipitation.
        val precipitation = halfDay.precipitation
        val precipitationUnit = SettingsManager.getInstance(context).precipitationUnit
        if ((precipitation?.total ?: 0.0) > 0) {
            list.add(
                Title(
                    R.drawable.ic_water,
                    context.getString(R.string.precipitation)
                )
            )
            list.add(
                Value(
                    context.getString(R.string.precipitation_total),
                    precipitationUnit.getValueText(context, precipitation!!.total!!)
                )
            )
            if ((precipitation.rain ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_rain),
                        precipitationUnit.getValueText(context, precipitation.rain!!)
                    )
                )
            }
            if ((precipitation.snow ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_snow),
                        precipitationUnit.getValueText(context, precipitation.snow!!)
                    )
                )
            }
            if ((precipitation.ice ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_ice),
                        precipitationUnit.getValueText(context, precipitation.ice!!)
                    )
                )
            }
            if ((precipitation.thunderstorm ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_thunderstorm),
                        precipitationUnit.getValueText(context, precipitation.thunderstorm!!)
                    )
                )
            }
            list.add(Margin())
        }

        // precipitation probability.
        val probability = halfDay.precipitationProbability
        if ((probability?.total ?: 0.0) > 0) {
            val percentUnit = NumberFormat.getPercentInstance(context.currentLocale).apply {
                maximumFractionDigits = 0
            }
            list.add(
                Title(
                    R.drawable.ic_water_percent,
                    context.getString(R.string.precipitation_probability)
                )
            )
            list.add(
                Value(
                    context.getString(R.string.precipitation_total),
                    percentUnit.format(probability!!.total!!.div(100.0))
                )
            )
            if ((probability.rain ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_rain),
                        percentUnit.format(probability.rain!!.div(100.0))
                    )
                )
            }
            if ((probability.snow ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_snow),
                        percentUnit.format(probability.snow!!.div(1000))
                    )
                )
            }
            if ((probability.ice ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_ice),
                        percentUnit.format(probability.ice!!.div(1000))
                    )
                )
            }
            if ((probability.thunderstorm ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_thunderstorm),
                        percentUnit.format(probability.thunderstorm!!.div(1000))
                    )
                )
            }
            list.add(Margin())
        }

        // precipitation duration.
        val duration = halfDay.precipitationDuration
        if ((duration?.total ?: 0.0) > 0) {
            list.add(
                Title(
                    R.drawable.ic_time,
                    context.getString(R.string.precipitation_duration)
                )
            )
            list.add(
                Value(
                    context.getString(R.string.precipitation_total),
                    DurationUnit.H.getValueText(context, duration!!.total!!)
                )
            )
            if ((duration.rain ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_rain),
                        DurationUnit.H.getValueText(context, duration.rain!!)
                    )
                )
            }
            if ((duration.snow ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_snow),
                        DurationUnit.H.getValueText(context, duration.snow!!)
                    )
                )
            }
            if ((duration.ice ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_ice),
                        DurationUnit.H.getValueText(context, duration.ice!!)
                    )
                )
            }
            if ((duration.thunderstorm ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_thunderstorm),
                        DurationUnit.H.getValueText(context, duration.thunderstorm!!)
                    )
                )
            }
            list.add(Margin())
        }
        return list
    }
}
