package org.breezyweather.background.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Build
import android.util.Log
import breezyweather.data.location.LocationRepository
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationDuration
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.Wind
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.breezyweather.BuildConfig
import org.breezyweather.background.provider.json.BreezyAirQuality
import org.breezyweather.background.provider.json.BreezyAlert
import org.breezyweather.background.provider.json.BreezyAstro
import org.breezyweather.background.provider.json.BreezyBulletin
import org.breezyweather.background.provider.json.BreezyCurrent
import org.breezyweather.background.provider.json.BreezyDaily
import org.breezyweather.background.provider.json.BreezyDegreeDay
import org.breezyweather.background.provider.json.BreezyDoubleUnit
import org.breezyweather.background.provider.json.BreezyHalfDay
import org.breezyweather.background.provider.json.BreezyHourly
import org.breezyweather.background.provider.json.BreezyMinutely
import org.breezyweather.background.provider.json.BreezyMoonPhase
import org.breezyweather.background.provider.json.BreezyNormals
import org.breezyweather.background.provider.json.BreezyPercent
import org.breezyweather.background.provider.json.BreezyPollen
import org.breezyweather.background.provider.json.BreezyPollutant
import org.breezyweather.background.provider.json.BreezyPrecipitation
import org.breezyweather.background.provider.json.BreezyPrecipitationDuration
import org.breezyweather.background.provider.json.BreezyPrecipitationProbability
import org.breezyweather.background.provider.json.BreezyTemperature
import org.breezyweather.background.provider.json.BreezyUV
import org.breezyweather.background.provider.json.BreezyWeather
import org.breezyweather.background.provider.json.BreezyWind
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.common.basic.models.options.unit.PressureUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getConcentration
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getIndexName
import org.breezyweather.domain.weather.model.getMinutelyDescription
import org.breezyweather.domain.weather.model.getMinutelyTitle
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.validPollens
import org.breezyweather.domain.weather.model.validPollutants
import org.breezyweather.settings.SettingsManager
import java.text.NumberFormat
import javax.inject.Inject

class WeatherContentProvider : ContentProvider() {

    @Inject internal lateinit var locationRepository: Lazy<LocationRepository>

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Disable the content provider on SDK < 23 since it grants dangerous
            // permissions at install-time
            Log.w(TAG, "Content provider read is only available for SDK >= 23")
            return null
        }

        if (!isAllowedPackage()) {
            Log.w(TAG, "Content provider is disabled for this app")
            return null
        }

        return when (uriMatcher.match(uri)) {
            URI_VERSION -> queryVersion()
            URI_LOCATION -> queryWeather()
            else -> {
                Log.w(TAG, "Unrecognized URI $uri")
                null
            }
        }
    }

    private fun isAllowedPackage(): Boolean {
        // Can check callingPackage here
        return true
    }

    private fun queryVersion(): Cursor {
        val columns = arrayOf(Version.COLUMN_MAJOR, Version.COLUMN_MINOR)
        val matrixCursor = MatrixCursor(columns).apply {
            addRow(arrayOf<Any>(Version.MAJOR, Version.MINOR))
        }
        return matrixCursor
    }

    private fun queryWeather(): Cursor {
        val columns = arrayOf(
            Location.COLUMN_ID,
            Location.COLUMN_LATITUDE,
            Location.COLUMN_LONGITUDE,
            Location.COLUMN_TIMEZONE,
            Location.COLUMN_COUNTRY,
            Location.COLUMN_COUNTRY_CODE,
            Location.COLUMN_ADMIN1,
            Location.COLUMN_ADMIN1_CODE,
            Location.COLUMN_ADMIN2,
            Location.COLUMN_ADMIN2_CODE,
            Location.COLUMN_ADMIN3,
            Location.COLUMN_ADMIN3_CODE,
            Location.COLUMN_ADMIN4,
            Location.COLUMN_ADMIN4_CODE,
            Location.COLUMN_CITY,
            Location.COLUMN_DISTRICT,
            Location.COLUMN_WEATHER
        )
        val locations = runBlocking {
            locationRepository.value.getAllLocations(withParameters = false)
        }
        val matrixCursor = MatrixCursor(columns).apply {
            locations.forEach {
                if (it.weather?.current != null) {
                    addRow(
                        arrayOf<Any?>(
                            it.cityId,
                            it.latitude,
                            it.longitude,
                            it.timeZone,
                            it.country,
                            it.countryCode,
                            it.admin1,
                            it.admin1Code,
                            it.admin2,
                            it.admin2Code,
                            it.admin3,
                            it.admin3Code,
                            it.admin4,
                            it.admin4Code,
                            it.city,
                            it.district,
                            Json.encodeToString(getWeatherData(it))
                            // TODO: sources/credits
                        )
                    )
                }
            }
        }
        return matrixCursor
    }

    private fun getWeatherData(
        location: breezyweather.domain.location.model.Location,
    ): BreezyWeather? {
        val settings = SettingsManager.getInstance(context!!)
        val temperatureUnit = settings.temperatureUnit
        val precipitationUnit = settings.precipitationUnit
        val distanceUnit = settings.distanceUnit
        val pressureUnit = settings.pressureUnit
        val percentUnit = NumberFormat.getPercentInstance(context!!.currentLocale)

        // TODO: Need to create a lib for these classes so they can be included in other projects
        return location.weather?.let { weather ->
            BreezyWeather(
                refreshTime = weather.base.refreshTime?.time,
                bulletin = BreezyBulletin(
                    dailyForecast = weather.current?.dailyForecast,
                    hourlyForecast = weather.current?.hourlyForecast,
                    minutelyForecastTitle = weather.getMinutelyTitle(context!!),
                    minutelyForecastDescription = weather.getMinutelyDescription(context!!, location)
                ),
                current = getCurrent(
                    weather.current,
                    temperatureUnit,
                    distanceUnit,
                    pressureUnit,
                    percentUnit
                ),
                daily = getDaily(
                    weather.dailyForecast,
                    temperatureUnit,
                    precipitationUnit,
                    distanceUnit,
                    percentUnit
                ),
                hourly = getHourly(
                    weather.hourlyForecast,
                    temperatureUnit,
                    precipitationUnit,
                    distanceUnit,
                    pressureUnit,
                    percentUnit
                ),
                minutely = getMinutely(
                    weather.minutelyForecast,
                    precipitationUnit
                ),
                alerts = getAlerts(
                    weather.alertList
                ),
                normals = getNormals(
                    weather.normals,
                    temperatureUnit
                )
            )
        }
    }

    private fun getNormals(
        normals: Normals?,
        temperatureUnit: TemperatureUnit,
    ): BreezyNormals? {
        return normals?.let {
            BreezyNormals(
                month = it.month,
                daytimeTemperature = getTemperatureDoubleUnit(it.daytimeTemperature, temperatureUnit),
                nighttimeTemperature = getTemperatureDoubleUnit(it.nighttimeTemperature, temperatureUnit)
            )
        }
    }

    private fun getCurrent(
        current: Current?,
        temperatureUnit: TemperatureUnit,
        distanceUnit: DistanceUnit,
        pressureUnit: PressureUnit,
        percentUnit: NumberFormat,
    ): BreezyCurrent? {
        return current?.let { cur ->
            BreezyCurrent(
                weatherText = cur.weatherText,
                weatherCode = cur.weatherCode?.id,
                temperature = getTemperature(cur.temperature, temperatureUnit),
                wind = getWind(cur.wind, distanceUnit),
                uV = cur.uV?.let { BreezyUV(it.index) },
                airQuality = getAirQuality(cur.airQuality),
                relativeHumidity = getPercentUnit(cur.relativeHumidity, percentUnit, 0),
                dewPoint = getTemperatureDoubleUnit(cur.dewPoint, temperatureUnit),
                pressure = getPressureDoubleUnit(cur.pressure, pressureUnit),
                cloudCover = getPercentUnit(cur.cloudCover?.toDouble(), percentUnit, 0),
                visibility = getDistanceDoubleUnit(cur.visibility, distanceUnit),
                ceiling = getDistanceDoubleUnit(cur.ceiling, distanceUnit)
            )
        }
    }

    private fun getDaily(
        daily: List<Daily>?,
        temperatureUnit: TemperatureUnit,
        precipitationUnit: PrecipitationUnit,
        distanceUnit: DistanceUnit,
        percentUnit: NumberFormat,
    ): List<BreezyDaily>? {
        return daily?.map { day ->
            BreezyDaily(
                date = day.date.time,
                day = getHalfDay(
                    day.day,
                    temperatureUnit,
                    precipitationUnit,
                    distanceUnit,
                    percentUnit
                ),
                night = getHalfDay(
                    day.night,
                    temperatureUnit,
                    precipitationUnit,
                    distanceUnit,
                    percentUnit
                ),
                degreeDay = day.degreeDay?.let {
                    BreezyDegreeDay(
                        heating = getDegreeDayTemperatureDoubleUnit(
                            it.heating,
                            temperatureUnit
                        ),
                        cooling = getDegreeDayTemperatureDoubleUnit(
                            it.cooling,
                            temperatureUnit
                        )
                    )
                },
                sun = getAstro(day.sun),
                moon = getAstro(day.moon),
                moonPhase = day.moonPhase?.let {
                    BreezyMoonPhase(
                        angle = it.angle,
                        description = it.getDescription(context!!)
                    )
                },
                airQuality = getAirQuality(day.airQuality),
                pollen = getPollen(day.pollen),
                uV = day.uV?.let { BreezyUV(it.index) },
                sunshineDuration = getDurationUnit(day.sunshineDuration)
            )
        }
    }

    private fun getHourly(
        hourly: List<Hourly>?,
        temperatureUnit: TemperatureUnit,
        precipitationUnit: PrecipitationUnit,
        distanceUnit: DistanceUnit,
        pressureUnit: PressureUnit,
        percentUnit: NumberFormat,
    ): List<BreezyHourly>? {
        return hourly?.map { hour ->
            BreezyHourly(
                date = hour.date.time,
                isDaylight = hour.isDaylight,
                weatherText = hour.weatherText,
                weatherCode = hour.weatherCode?.id,
                temperature = getTemperature(hour.temperature, temperatureUnit),
                precipitation = getPrecipitation(hour.precipitation, precipitationUnit),
                precipitationProbability = getPrecipitationProbability(hour.precipitationProbability, percentUnit),
                wind = getWind(hour.wind, distanceUnit),
                airQuality = getAirQuality(hour.airQuality),
                uV = hour.uV?.let { BreezyUV(it.index) },
                relativeHumidity = getPercentUnit(hour.relativeHumidity, percentUnit, 0),
                dewPoint = getTemperatureDoubleUnit(hour.dewPoint, temperatureUnit),
                pressure = getPressureDoubleUnit(hour.pressure, pressureUnit),
                cloudCover = getPercentUnit(hour.cloudCover?.toDouble(), percentUnit, 0),
                visibility = getDistanceDoubleUnit(hour.visibility, distanceUnit)
            )
        }
    }

    private fun getMinutely(
        minutely: List<Minutely>?,
        precipitationUnit: PrecipitationUnit,
    ): List<BreezyMinutely>? {
        return minutely?.map { minute ->
            BreezyMinutely(
                date = minute.date.time,
                minuteInterval = minute.minuteInterval,
                precipitationIntensity = getPrecipitationDoubleUnit(minute.precipitationIntensity, precipitationUnit)
            )
        }
    }

    private fun getAstro(astro: Astro?): BreezyAstro? {
        return astro?.let {
            BreezyAstro(
                riseDate = it.riseDate?.time,
                setDate = it.setDate?.time
            )
        }
    }

    private fun getHalfDay(
        halfDay: HalfDay?,
        temperatureUnit: TemperatureUnit,
        precipitationUnit: PrecipitationUnit,
        distanceUnit: DistanceUnit,
        percentUnit: NumberFormat,
    ): BreezyHalfDay? {
        return halfDay?.let { hd ->
            BreezyHalfDay(
                weatherText = hd.weatherText,
                weatherPhase = hd.weatherPhase,
                weatherCode = hd.weatherCode?.id,
                temperature = getTemperature(hd.temperature, temperatureUnit),
                precipitation = getPrecipitation(hd.precipitation, precipitationUnit),
                precipitationProbability = getPrecipitationProbability(hd.precipitationProbability, percentUnit),
                precipitationDuration = getPrecipitationDuration(hd.precipitationDuration),
                wind = getWind(hd.wind, distanceUnit),
                cloudCover = getPercentUnit(hd.cloudCover?.toDouble(), percentUnit, 0)
            )
        }
    }

    private fun getTemperature(
        temperature: Temperature?,
        temperatureUnit: TemperatureUnit,
    ): BreezyTemperature? {
        return temperature?.let {
            BreezyTemperature(
                temperature = getTemperatureDoubleUnit(it.temperature, temperatureUnit),
                realFeelTemperature = getTemperatureDoubleUnit(it.realFeelTemperature, temperatureUnit),
                realFeelShaderTemperature = getTemperatureDoubleUnit(it.realFeelShaderTemperature, temperatureUnit),
                apparentTemperature = getTemperatureDoubleUnit(it.apparentTemperature, temperatureUnit),
                windChillTemperature = getTemperatureDoubleUnit(it.windChillTemperature, temperatureUnit),
                wetBulbTemperature = getTemperatureDoubleUnit(it.wetBulbTemperature, temperatureUnit)
            )
        }
    }

    private fun getTemperatureDoubleUnit(
        temperature: Double?,
        temperatureUnit: TemperatureUnit,
    ): BreezyDoubleUnit? {
        return temperature?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "c",
                preferredUnitValue = temperatureUnit.convertUnit(it),
                preferredUnitUnit = temperatureUnit.id,
                preferredUnitFormatted = temperatureUnit.getValueText(context!!, it, 0),
                preferredUnitFormattedShort = temperatureUnit.getShortValueText(context!!, it)
            )
        }
    }

    /**
     * FIXME: Not consistant with getTemperatureDoubleUnit on formatted
     * (missing equivalent ValueText functions)
     */
    private fun getDegreeDayTemperatureDoubleUnit(
        temperature: Double?,
        temperatureUnit: TemperatureUnit,
    ): BreezyDoubleUnit? {
        return temperature?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "c",
                preferredUnitValue = temperatureUnit.convertDegreeDayUnit(it),
                preferredUnitUnit = temperatureUnit.id,
                preferredUnitFormatted = temperatureUnit.getDegreeDayValueText(context!!, it),
                preferredUnitFormattedShort = temperatureUnit.getDegreeDayValueText(context!!, it)
            )
        }
    }

    private fun getPrecipitation(
        precipitation: Precipitation?,
        precipitationUnit: PrecipitationUnit,
    ): BreezyPrecipitation? {
        return precipitation?.let {
            BreezyPrecipitation(
                total = getPrecipitationDoubleUnit(it.total, precipitationUnit),
                thunderstorm = getPrecipitationDoubleUnit(it.thunderstorm, precipitationUnit),
                rain = getPrecipitationDoubleUnit(it.rain, precipitationUnit),
                snow = getPrecipitationDoubleUnit(it.snow, precipitationUnit),
                ice = getPrecipitationDoubleUnit(it.ice, precipitationUnit)
            )
        }
    }

    private fun getPrecipitationDoubleUnit(
        precipitation: Double?,
        precipitationUnit: PrecipitationUnit,
    ): BreezyDoubleUnit? {
        return precipitation?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "mm",
                preferredUnitValue = precipitationUnit.convertUnit(it),
                preferredUnitUnit = precipitationUnit.id,
                preferredUnitFormatted = precipitationUnit.getValueText(context!!, it),
                preferredUnitFormattedShort = precipitationUnit.getValueText(context!!, it)
            )
        }
    }

    private fun getPrecipitationProbability(
        precipitationProbability: PrecipitationProbability?,
        percentUnit: NumberFormat,
    ): BreezyPrecipitationProbability? {
        return precipitationProbability?.let {
            BreezyPrecipitationProbability(
                total = getPercentUnit(it.total, percentUnit),
                thunderstorm = getPercentUnit(it.thunderstorm, percentUnit),
                rain = getPercentUnit(it.rain, percentUnit),
                snow = getPercentUnit(it.snow, percentUnit),
                ice = getPercentUnit(it.ice, percentUnit)
            )
        }
    }

    private fun getPrecipitationDuration(
        precipitationDuration: PrecipitationDuration?,
    ): BreezyPrecipitationDuration? {
        return precipitationDuration?.let {
            BreezyPrecipitationDuration(
                total = getDurationUnit(it.total),
                thunderstorm = getDurationUnit(it.thunderstorm),
                rain = getDurationUnit(it.rain),
                snow = getDurationUnit(it.snow),
                ice = getDurationUnit(it.ice)
            )
        }
    }

    private fun getWind(
        wind: Wind?,
        distanceUnit: DistanceUnit,
    ): BreezyWind? {
        return wind?.let {
            BreezyWind(
                degree = wind.degree,
                speed = getDistanceDoubleUnit(wind.speed, distanceUnit),
                gusts = getDistanceDoubleUnit(wind.gusts, distanceUnit)
            )
        }
    }

    private fun getDistanceDoubleUnit(
        distance: Double?,
        distanceUnit: DistanceUnit,
    ): BreezyDoubleUnit? {
        return distance?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "m",
                preferredUnitValue = distanceUnit.convertUnit(it),
                preferredUnitUnit = distanceUnit.id,
                preferredUnitFormatted = distanceUnit.getValueText(context!!, it),
                preferredUnitFormattedShort = distanceUnit.getValueText(context!!, it)
            )
        }
    }

    private fun getPressureDoubleUnit(
        pressure: Double?,
        pressureUnit: PressureUnit,
    ): BreezyDoubleUnit? {
        return pressure?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "mb",
                preferredUnitValue = pressureUnit.convertUnit(it),
                preferredUnitUnit = pressureUnit.id,
                preferredUnitFormatted = pressureUnit.getValueText(context!!, it),
                preferredUnitFormattedShort = pressureUnit.getValueText(context!!, it)
            )
        }
    }

    private fun getPercentUnit(
        percent: Double?,
        percentUnit: NumberFormat,
        digits: Int = 0,
    ): BreezyPercent? {
        return percent?.let {
            BreezyPercent(
                value = it,
                formatted = percentUnit.apply {
                    maximumFractionDigits = digits
                }.format(it.div(100.0))
            )
        }
    }

    private fun getDurationUnit(
        duration: Double?,
    ): BreezyDoubleUnit? {
        return duration?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "h",
                preferredUnitValue = it,
                preferredUnitUnit = "h",
                preferredUnitFormatted = DurationUnit.H.getValueText(context!!, it),
                preferredUnitFormattedShort = DurationUnit.H.getValueText(context!!, it)
            )
        }
    }

    private fun getAirQuality(
        airQuality: AirQuality?,
    ): BreezyAirQuality? {
        return airQuality?.let {
            if (airQuality.isValid) {
                BreezyAirQuality(
                    index = airQuality.getIndex(),
                    indexColor = airQuality.getColor(context!!),
                    pollutants = airQuality.validPollutants.associate {
                        it.id to BreezyPollutant(
                            id = it.id,
                            name = airQuality.getName(context!!, it),
                            concentration = airQuality.getConcentration(it),
                            index = airQuality.getIndex(it),
                            color = airQuality.getColor(context!!, it)
                        )
                    }
                )
            } else {
                null
            }
        }
    }

    /**
     * TODO: Support for source-based pollen index
     */
    private fun getPollen(
        pollen: Pollen?,
    ): Map<String, BreezyPollen>? {
        return pollen?.let {
            if (it.isValid) {
                it.validPollens.associate { component ->
                    component.id to BreezyPollen(
                        id = component.id,
                        name = it.getName(context!!, component),
                        concentration = it.getConcentration(component), // TODO: Remove when from source
                        indexName = it.getIndexName(context!!, component), // TODO: getIndexNameFromSource when applies
                        color = it.getColor(context!!, component)
                    )
                }
            } else {
                null
            }
        }
    }

    private fun getAlerts(alertList: List<Alert>?): List<BreezyAlert>? {
        return alertList?.map { alert ->
            BreezyAlert(
                alertId = alert.alertId,
                startDate = alert.startDate?.time,
                endDate = alert.endDate?.time,
                headline = alert.headline,
                description = alert.description,
                instruction = alert.instruction,
                source = alert.source,
                severity = alert.severity.id,
                color = alert.color
            )
        }
    }

    override fun getType(uri: Uri): String? {
        // MIME types are not relevant (for now at least)
        return null
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? {
        // This content provider is read-only for now, so we always return null
        return null
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int {
        // This content provider is read-only for now, so we always return 0
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int {
        // This content provider is read-only for now, so we always return 0
        return 0
    }

    // TODO: Need to create a lib for these column names so they can be included in other projects
    object Version {
        const val COLUMN_MAJOR = "major" // Renamed, changed type or deleted fields
        const val COLUMN_MINOR = "minor" // Added features/fields
        const val MAJOR = 0
        const val MINOR = 1
    }

    object Location {
        /**
         * Unique ID of the location
         * The ID can change, so don’t rely on it between different queries
         */
        const val COLUMN_ID = "id"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_TIMEZONE = "timezone"
        const val COLUMN_COUNTRY = "country"
        const val COLUMN_COUNTRY_CODE = "country_code"
        const val COLUMN_ADMIN1 = "admin1"
        const val COLUMN_ADMIN1_CODE = "admin1_code"
        const val COLUMN_ADMIN2 = "admin2"
        const val COLUMN_ADMIN2_CODE = "admin2_code"
        const val COLUMN_ADMIN3 = "admin3"
        const val COLUMN_ADMIN3_CODE = "admin3_code"
        const val COLUMN_ADMIN4 = "admin4"
        const val COLUMN_ADMIN4_CODE = "admin4_code"
        const val COLUMN_CITY = "city"
        const val COLUMN_DISTRICT = "district"
        const val COLUMN_WEATHER = "weather"
    }

    companion object {
        private const val TAG = "Breezy"

        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.weather"

        private const val URI_VERSION = 0
        private const val URI_LOCATION = 1

        private val uriMatcher = object : UriMatcher(NO_MATCH) {
            init {
                addURI(AUTHORITY, "version", URI_VERSION)
                addURI(AUTHORITY, "location", URI_LOCATION)
            }
        }
    }
}
