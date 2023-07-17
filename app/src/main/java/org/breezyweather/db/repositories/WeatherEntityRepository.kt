package org.breezyweather.db.repositories

import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.db.ObjectBox.boxStore
import org.breezyweather.db.entities.WeatherEntity
import org.breezyweather.db.entities.WeatherEntity_
import org.breezyweather.db.generators.*

object WeatherEntityRepository {
    // insert.
    fun writeWeather(location: Location, weather: Weather) {
        boxStore.callInTxNoException {
            deleteWeather(location)
            insertWeatherEntity(WeatherEntityGenerator.generate(location, weather))
            DailyEntityRepository.insertDailyList(
                DailyEntityGenerator.generate(
                    location.cityId,
                    location.weatherSource,
                    weather.dailyForecast
                )
            )
            HourlyEntityRepository.insertHourlyList(
                HourlyEntityGenerator.generateEntityList(
                    location.cityId,
                    location.weatherSource,
                    weather.hourlyForecast
                )
            )
            MinutelyEntityRepository.insertMinutelyList(
                MinutelyEntityGenerator.generate(
                    location.cityId,
                    location.weatherSource,
                    weather.minutelyForecast
                )
            )
            AlertEntityRepository.insertAlertList(
                AlertEntityGenerator.generate(
                    location.cityId,
                    location.weatherSource,
                    weather.alertList
                )
            )
            if (weather.dailyForecast.isNotEmpty() && weather.dailyForecast[0].day?.temperature != null && weather.dailyForecast[0].night?.temperature != null) {
                HistoryEntityRepository.insertHistoryEntity(
                    HistoryEntityGenerator.generate(
                        location.cityId, location.weatherSource, weather
                    )
                )
            }
            if (weather.yesterday != null) {
                HistoryEntityRepository.insertHistoryEntity(
                    HistoryEntityGenerator.generate(
                        location.cityId, location.weatherSource, weather.yesterday!!
                    )
                )
            }
            true
        }
    }

    fun insertWeatherEntity(entity: WeatherEntity) {
        boxStore.boxFor(WeatherEntity::class.java).put(entity)
    }

    // delete.
    fun deleteWeather(location: Location) {
        boxStore.callInTxNoException {
            deleteWeather(
                selectWeatherEntityList(
                    location.cityId,
                    location.weatherSource
                )
            )
            HistoryEntityRepository.deleteLocationHistoryEntity(
                HistoryEntityRepository.selectHistoryEntityList(
                    location.cityId,
                    location.weatherSource
                )
            )
            DailyEntityRepository.deleteDailyEntityList(
                DailyEntityRepository.selectDailyEntityList(
                    location.cityId,
                    location.weatherSource
                )
            )
            HourlyEntityRepository.deleteHourlyEntityList(
                HourlyEntityRepository.selectHourlyEntityList(
                    location.cityId,
                    location.weatherSource
                )
            )
            MinutelyEntityRepository.deleteMinutelyEntityList(
                MinutelyEntityRepository.selectMinutelyEntityList(
                    location.cityId,
                    location.weatherSource
                )
            )
            AlertEntityRepository.deleteAlertList(
                AlertEntityRepository.selectLocationAlertEntity(
                    location.cityId,
                    location.weatherSource
                )
            )
            true
        }
    }

    fun deleteWeather(entityList: List<WeatherEntity>) {
        boxStore.boxFor(WeatherEntity::class.java).remove(entityList)
    }

    // select.
    fun readWeather(location: Location): Weather? {
        val weatherEntity = selectWeatherEntity(location.cityId, location.weatherSource) ?: return null
        val historyEntity = HistoryEntityRepository.selectYesterdayHistoryEntity(
            location.cityId, location.weatherSource, weatherEntity.publishDate, location.timeZone
        )
        return WeatherEntityGenerator.generate(weatherEntity, historyEntity, boxStore)
    }

    fun selectWeatherEntity(cityId: String, source: String): WeatherEntity? {
        val entityList = selectWeatherEntityList(cityId, source)
        return if (entityList.isEmpty()) null else entityList[0]
    }

    fun selectWeatherEntityList(cityId: String, source: String): List<WeatherEntity> {
        val query = boxStore.boxFor(WeatherEntity::class.java)
            .query(
                WeatherEntity_.cityId.equal(cityId)
                    .and(WeatherEntity_.weatherSource.equal(source))
            ).build()
        val results = query.find()
        query.close()
        return results
    }
}
