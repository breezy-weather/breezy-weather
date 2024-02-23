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

package org.breezyweather.daily.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.basic.models.options.unit.ProbabilityUnit
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import org.breezyweather.daily.adapter.holder.*
import org.breezyweather.daily.adapter.model.*
import org.breezyweather.databinding.ItemWeatherDailyPollenBinding
import org.breezyweather.domain.weather.model.isIndexValid
import org.breezyweather.settings.SettingsManager
import java.util.TimeZone

class DailyWeatherAdapter(context: Context, timeZone: TimeZone, daily: Daily, spanCount: Int) :
    RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder>() {
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
        mModelList = ArrayList()
        daily.day?.let { day ->
            mModelList.add(LargeTitle(context.getString(R.string.daytime)))
            mModelList.add(Overview(day, true))
            day.wind?.let { wind ->
                if (wind.isValid) {
                    mModelList.add(DailyWind(wind))
                }
            }
            mModelList.addAll(getHalfDayOptionalModelList(context, day))
        }
        daily.night?.let { night ->
            mModelList.add(Line())
            mModelList.add(LargeTitle(context.getString(R.string.nighttime)))
            mModelList.add(Overview(night, false))
            night.wind?.let { wind ->
                if (wind.isValid) {
                    mModelList.add(DailyWind(wind))
                }
            }
            mModelList.addAll(getHalfDayOptionalModelList(context, night))
        }
        mModelList.add(Line())
        daily.airQuality?.let { airQuality ->
            if (airQuality.isIndexValid) {
                mModelList.add(
                    Title(
                        R.drawable.weather_haze_mini_xml,
                        context.getString(R.string.air_quality)
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
                        context.getString(if (pollen.isMoldValid) R.string.pollen_and_mold else R.string.pollen)
                    )
                )
                mModelList.add(DailyPollen(pollen))
            }
        }
        daily.uV?.let { uV ->
            if (uV.isValid) {
                mModelList.add(Title(R.drawable.ic_uv, context.getString(R.string.uv_index)))
                mModelList.add(DailyUV(uV))
            }
        }
        if (daily.sun?.isValid == true || daily.moon?.isValid == true || daily.moonPhase?.isValid == true) {
            mModelList.add(LargeTitle(context.getString(R.string.ephemeris)))
            mModelList.add(DailyAstro(timeZone, daily.sun, daily.moon, daily.moonPhase))
        }
        if (daily.degreeDay?.isValid == true || daily.hoursOfSun != null) {
            mModelList.add(Line())
            mModelList.add(LargeTitle(context.getString(R.string.details)))
            daily.degreeDay?.let { degreeDay ->
                if (degreeDay.isValid) {
                    val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
                    if ((degreeDay.heating ?: 0.0) > 0) {
                        mModelList.add(
                            ValueIcon(
                                context.getString(R.string.temperature_degree_day_heating),
                                temperatureUnit.getDegreeDayValueText(
                                    context,
                                    degreeDay.heating!!
                                ),
                                R.drawable.ic_mode_heat
                            )
                        )
                    } else if ((degreeDay.cooling ?: 0.0) > 0) {
                        mModelList.add(
                            ValueIcon(
                                context.getString(R.string.temperature_degree_day_cooling),
                                temperatureUnit.getDegreeDayValueText(
                                    context,
                                    degreeDay.cooling!!
                                ),
                                R.drawable.ic_mode_cool
                            )
                        )
                    }
                }
            }
            daily.hoursOfSun?.let { hoursOfSun ->
                mModelList.add(
                    ValueIcon(
                        context.getString(R.string.hours_of_sun),
                        DurationUnit.H.getValueText(context, hoursOfSun),
                        R.drawable.ic_hours_of_sun

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

    private fun getHalfDayOptionalModelList(context: Context, halfDay: HalfDay): List<ViewModel> {
        val list: MutableList<ViewModel> = ArrayList()
        // temperature.
        val temperature = halfDay.temperature
        val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
        if (temperature?.feelsLikeTemperature != null) {
            list.add(Title(R.drawable.ic_device_thermostat, context.getString(R.string.temperature)))
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
            list.add(Margin())
        }

        // precipitation.
        val precipitation = halfDay.precipitation
        val precipitationUnit = SettingsManager.getInstance(context).precipitationUnit
        if ((precipitation?.total ?: 0.0) > 0) {
            list.add(Title(R.drawable.ic_water, context.getString(R.string.precipitation)))
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
            list.add(Title(R.drawable.ic_water_percent, context.getString(R.string.precipitation_probability)))
            list.add(
                Value(
                    context.getString(R.string.precipitation_total),
                    ProbabilityUnit.PERCENT.getValueText(context, probability!!.total!!.toInt())
                )
            )
            if ((probability.rain ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_rain),
                        ProbabilityUnit.PERCENT.getValueText(context, probability.rain!!.toInt())
                    )
                )
            }
            if ((probability.snow ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_snow),
                        ProbabilityUnit.PERCENT.getValueText(context, probability.snow!!.toInt())
                    )
                )
            }
            if ((probability.ice ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_ice),
                        ProbabilityUnit.PERCENT.getValueText(context, probability.ice!!.toInt())
                    )
                )
            }
            if ((probability.thunderstorm ?: 0.0) > 0) {
                list.add(
                    Value(
                        context.getString(R.string.precipitation_thunderstorm),
                        ProbabilityUnit.PERCENT.getValueText(context, probability.thunderstorm!!.toInt())
                    )
                )
            }
            list.add(Margin())
        }

        // precipitation duration.
        val duration = halfDay.precipitationDuration
        if ((duration?.total ?: 0.0) > 0) {
            list.add(Title(R.drawable.ic_time, context.getString(R.string.precipitation_duration)))
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
