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

package org.breezyweather.db.generators

import io.objectbox.BoxStore
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Normals
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.db.entities.WeatherEntity

object WeatherEntityGenerator {
    fun generate(location: Location, weather: Weather): WeatherEntity {
        return WeatherEntity(

            // base.
            formattedId = location.formattedId,
            refreshTime = weather.base.refreshTime,
            mainUpdateTime = weather.base.mainUpdateTime,
            airQualityUpdateTime = weather.base.airQualityUpdateTime,
            allergenUpdateTime = weather.base.allergenUpdateTime,
            minutelyUpdateTime = weather.base.minutelyUpdateTime,
            alertsUpdateTime = weather.base.alertsUpdateTime,
            normalsUpdateTime = weather.base.normalsUpdateTime,

            // current
            weatherText = weather.current?.weatherText,
            weatherCode = weather.current?.weatherCode,

            temperature = weather.current?.temperature?.temperature,
            realFeelTemperature = weather.current?.temperature?.realFeelTemperature,
            realFeelShaderTemperature = weather.current?.temperature?.realFeelShaderTemperature,
            apparentTemperature = weather.current?.temperature?.apparentTemperature,
            windChillTemperature = weather.current?.temperature?.windChillTemperature,
            wetBulbTemperature = weather.current?.temperature?.wetBulbTemperature,

            windDegree = weather.current?.wind?.degree,
            windSpeed = weather.current?.wind?.speed,
            windGusts = weather.current?.wind?.gusts,

            uvIndex = weather.current?.uV?.index,

            pm25 = weather.current?.airQuality?.pM25,
            pm10 = weather.current?.airQuality?.pM10,
            so2 = weather.current?.airQuality?.sO2,
            no2 = weather.current?.airQuality?.nO2,
            o3 = weather.current?.airQuality?.o3,
            co = weather.current?.airQuality?.cO,

            relativeHumidity = weather.current?.relativeHumidity,
            dewPoint = weather.current?.dewPoint,
            pressure = weather.current?.pressure,
            cloudCover = weather.current?.cloudCover,
            visibility = weather.current?.visibility,
            ceiling = weather.current?.ceiling,
            dailyForecast = weather.current?.dailyForecast,
            hourlyForecast = weather.current?.hourlyForecast,

            // normals
            normalsMonth = weather.normals?.month,
            normalsDaytimeTemperature = weather.normals?.daytimeTemperature,
            normalsNighttimeTemperature = weather.normals?.nighttimeTemperature
        )
    }

    fun generate(
        weatherEntity: WeatherEntity?,
        boxStore: BoxStore
    ): Weather? {
        return if (weatherEntity == null) {
            null
        } else Weather(
            Base(
                weatherEntity.refreshTime,
                weatherEntity.mainUpdateTime,
                weatherEntity.airQualityUpdateTime,
                weatherEntity.allergenUpdateTime,
                weatherEntity.minutelyUpdateTime,
                weatherEntity.alertsUpdateTime,
                weatherEntity.normalsUpdateTime
            ),
            Current(
                weatherEntity.weatherText,
                weatherEntity.weatherCode,
                Temperature(
                    weatherEntity.temperature,
                    weatherEntity.realFeelTemperature,
                    weatherEntity.realFeelShaderTemperature,
                    weatherEntity.apparentTemperature,
                    weatherEntity.windChillTemperature,
                    weatherEntity.wetBulbTemperature
                ),
                Wind(
                    weatherEntity.windDegree,
                    weatherEntity.windSpeed,
                    weatherEntity.windGusts
                ),
                UV(weatherEntity.uvIndex),
                AirQuality(
                    weatherEntity.pm25,
                    weatherEntity.pm10,
                    weatherEntity.so2,
                    weatherEntity.no2,
                    weatherEntity.o3,
                    weatherEntity.co
                ),
                weatherEntity.relativeHumidity,
                weatherEntity.dewPoint,
                weatherEntity.pressure,
                weatherEntity.cloudCover,
                weatherEntity.visibility,
                weatherEntity.ceiling,
                weatherEntity.dailyForecast,
                weatherEntity.hourlyForecast
            ),
            Normals(
                weatherEntity.normalsMonth,
                weatherEntity.normalsDaytimeTemperature,
                weatherEntity.normalsNighttimeTemperature
            ),
            DailyEntityGenerator.generate(weatherEntity.getDailyEntityList(boxStore)),
            HourlyEntityGenerator.generateModuleList(weatherEntity.getHourlyEntityList(boxStore)),
            MinutelyEntityGenerator.generate(weatherEntity.getMinutelyEntityList(boxStore)),
            AlertEntityGenerator.generate(weatherEntity.getAlertEntityList(boxStore))
        )
    }

}
