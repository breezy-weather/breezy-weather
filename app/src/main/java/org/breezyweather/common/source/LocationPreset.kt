/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.source

import breezyweather.domain.location.model.Location
import org.breezyweather.BuildConfig
import java.util.Locale

/**
 * When a preset doesn't have a secondary source listed (null values), it will use main source
 * Current recommendations:
 * - Forecast source: national weather source or Open-Meteo
 * - Current: national weather source or Open-Meteo
 * - Air quality: national weather source or Open-Meteo
 * - Pollen: Open-Meteo
 * - Minutely: national weather source or Open-Meteo
 * - Alerts: national weather source or AccuWeather (or WMO if Accu is broken,
 *       but should be avoided as it may not be reliable in every country)
 * - Normals: national weather source or AccuWeather
 */
enum class LocationPreset(
    val forecast: String,
    val current: String?,
    val airQuality: String?,
    val pollen: String?,
    val minutely: String?,
    val alert: String?,
    val normals: String?,
) {
    DEFAULT(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "accu",
        normals = "accu"
    ),
    DEFAULT_FREENET(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = null,
        normals = null
    ),

    // North America
    CANADA(
        forecast = "eccc",
        current = "eccc",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "eccc",
        normals = "eccc"
    ),
    USA(
        forecast = "nws",
        current = "nws",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "nws",
        normals = "accu"
    ),

    // Europe
    /*AUSTRIA(
        forecast = "openmeteo", // GeoSphere too lightweight
        current = "openmeteo",
        airQuality = "geosphereat",
        pollen = "openmeteo",
        minutely = "geosphereat",
        alert = "geosphereat",
        normals = "geosphereat"
    ),*/
    ANDORRA(
        forecast = "mf",
        current = null,
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "mf",
        normals = "accu"
    ),
    DENMARK(
        forecast = "dmi",
        current = null,
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "metno",
        alert = "dmi",
        normals = "accu"
    ),
    GERMANY(
        forecast = "brightsky",
        current = "brightsky",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "brightsky",
        normals = "accu"
    ),
    GERMANY_FREENET(
        forecast = "brightsky",
        current = "brightsky",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "brightsky",
        normals = null
    ),
    ESTONIA(
        forecast = "ilmateenistus",
        current = null,
        airQuality = "ekuk",
        pollen = "openmeteo", // TODO: At pollen season
        minutely = "openmeteo",
        alert = "accu",
        normals = "accu"
    ),
    FINLAND(
        forecast = "metno",
        current = "metno",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "metno",
        alert = "accu",
        normals = "accu"
    ),
    FRANCE(
        forecast = "mf",
        current = "mf",
        airQuality = "openmeteo",
        pollen = "recosante",
        minutely = "mf",
        alert = "mf",
        normals = "mf"
    ),
    FRANCE_OVERSEAS(
        forecast = "mf",
        current = null,
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "mf",
        normals = "accu"
    ),
    FRANCE_FREENET(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = "recosante",
        minutely = "openmeteo",
        alert = null,
        normals = null
    ),
    IRELAND(
        forecast = "metie",
        current = null,
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "metie",
        normals = "accu"
    ),
    ITALY(
        forecast = "meteoam",
        current = "meteoam",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "accu",
        normals = "accu"
    ),
    LATVIA(
        forecast = "lvgmc",
        current = "lvgmc",
        airQuality = "lvgmc",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "accu",
        normals = "accu"
    ),
    LITHUANIA(
        forecast = "lhmt",
        current = "lhmt",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "lhmt",
        normals = "accu"
    ),
    LUXEMBOURG(
        forecast = "meteolux",
        current = "meteolux",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "meteolux",
        normals = "accu"
    ),
    MONACO(
        forecast = "mf",
        current = null,
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "accu",
        normals = "mf"
    ),
    NORWAY(
        forecast = "metno",
        current = "metno",
        airQuality = "metno",
        pollen = "openmeteo",
        minutely = "metno",
        alert = "metno",
        normals = "accu"
    ),
    PORTUGAL(
        forecast = "ipma",
        current = null,
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "ipma",
        normals = "accu"
    ),

    /*SPAIN( // Don't recommend a source with a rate-limited API key
        forecast = "aemet",
        current = "aemet",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "accu",
        normals = "aemet"
    ),*/
    SWEDEN(
        forecast = "smhi",
        current = null,
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "metno",
        alert = "accu",
        normals = "accu"
    ),

    // Asia
    // Do NOT set up other sources as only 中国 source is not rate-limited by the Great Firewall
    // Don’t add cwa for TAIWAN as it is a rate-limited source
    BANGLADESH(
        forecast = "bmd",
        current = null,
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "accu",
        normals = "accu"
    ),
    CHINA(
        forecast = "china",
        current = "china",
        airQuality = "china",
        pollen = null,
        minutely = "china",
        alert = "china",
        normals = null
    ),
    HONG_KONG(
        forecast = "hko",
        current = "hko",
        airQuality = "epdhk",
        pollen = null,
        minutely = "openmeteo",
        alert = "hko",
        normals = "hko"
    ),
    INDONESIA(
        forecast = "bmkg",
        current = "bmkg",
        airQuality = "bmkg",
        pollen = null,
        minutely = "openmeteo",
        alert = "bmkg",
        normals = "accu"
    ),
    INDIA(
        forecast = "imd",
        current = null,
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "accu",
        normals = "accu"
    ),
    ISRAEL(
        forecast = "ims",
        current = "ims",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "ims",
        normals = "accu"
    ),
    JAPAN(
        forecast = "jma",
        current = "jma",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "jma",
        normals = "jma"
    ),
    MACAO(
        forecast = "smg",
        current = "smg",
        airQuality = "smg",
        pollen = null,
        minutely = "openmeteo",
        alert = "smg",
        normals = "smg"
    ),
    MONGOLIA(
        forecast = "namem",
        current = "namem",
        airQuality = "namem",
        pollen = null,
        minutely = "openmeteo",
        alert = "accu",
        normals = "namem"
    ),
    PHILIPPINES(
        forecast = "pagasa",
        current = null,
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "accu",
        normals = "accu"
    ),
    TURKIYE(
        forecast = "mgm",
        current = "mgm",
        airQuality = "openmeteo",
        pollen = "openmeteo",
        minutely = "openmeteo",
        alert = "mgm",
        normals = "mgm"
    ),

    // Africa
    // TODO below
    BENIN(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "meteobenin",
        normals = "meteobenin"
    ),
    BURKINA_FASO(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "anambf",
        normals = null
    ),
    BURUNDI(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "igebu",
        normals = null
    ),
    CHAD(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "meteotchad",
        normals = "meteotchad"
    ),
    DR_CONGO(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "mettelsat",
        normals = null
    ),
    ETHIOPIA(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "ethiomet",
        normals = "ethiomet"
    ),
    GAMBIA(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "dwrgm",
        normals = null
    ),
    GHANA(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "gmet",
        normals = null
    ),
    GUINEA_BISSAU(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "inmgb",
        normals = null
    ),
    MALAWI(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "dccms",
        normals = "dccms"
    ),
    MALI(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "malimeteo",
        normals = null
    ),
    NIGER(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "dmnne",
        normals = "dmnne"
    ),
    SEYCHELLES(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "smasc",
        normals = "smasc"
    ),
    SOUTH_SUDAN(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "ssms",
        normals = null
    ),
    SUDAN(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "smasu",
        normals = null
    ),
    TOGO(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "anamet",
        normals = null
    ),
    ZIMBABWE(
        forecast = "openmeteo",
        current = "openmeteo",
        airQuality = "openmeteo",
        pollen = null,
        minutely = "openmeteo",
        alert = "msdzw",
        normals = null
    ),
    ;

    companion object {
        fun getLocationPreset(countryCode: String?): LocationPreset {
            if (countryCode.isNullOrEmpty()) return DEFAULT
            return if (BuildConfig.FLAVOR != "freenet") {
                when (countryCode.uppercase(Locale.ENGLISH)) {
                    // North America
                    "CA" -> CANADA
                    "US", "PR", "VI", "MP", "GU" -> USA

                    // Europe
                    "AD" -> ANDORRA
                    "DE" -> GERMANY
                    "DK", "FO", "GL" -> DENMARK
                    "EE" -> ESTONIA
                    "FI" -> FINLAND
                    "FR" -> FRANCE
                    "BL", "GF", "GP", "MF", "MQ", "NC", "PF", "PM", "RE", "WF", "YT" -> FRANCE_OVERSEAS
                    "IE" -> IRELAND
                    "IT", "SM", "VA" -> ITALY
                    "LT" -> LITHUANIA
                    "LU" -> LUXEMBOURG
                    "LV" -> LATVIA
                    "MC" -> MONACO
                    "NO", "SJ" -> NORWAY
                    "PT" -> PORTUGAL
                    "SE" -> SWEDEN

                    // Asia
                    "BD" -> BANGLADESH
                    "CN" -> CHINA
                    "HK" -> HONG_KONG
                    "ID" -> INDONESIA
                    "IL", "PS" -> ISRAEL
                    "IN" -> INDIA
                    "JP" -> JAPAN
                    "MN" -> MONGOLIA
                    "MO" -> MACAO
                    "PH" -> PHILIPPINES
                    "TR" -> TURKIYE

                    // Africa
                    "BF" -> BURKINA_FASO
                    "BI" -> BURUNDI
                    "BJ" -> BENIN
                    "CD" -> DR_CONGO
                    "ET" -> ETHIOPIA
                    "GH" -> GHANA
                    "GM" -> GAMBIA
                    "GW" -> GUINEA_BISSAU
                    "ML" -> MALI
                    "MW" -> MALAWI
                    "NE" -> NIGER
                    "SC" -> SEYCHELLES
                    "SD" -> SUDAN
                    "SS" -> SOUTH_SUDAN
                    "TD" -> CHAD
                    "TG" -> TOGO
                    "ZW" -> ZIMBABWE

                    else -> DEFAULT
                }
            } else {
                when (countryCode.uppercase(Locale.ENGLISH)) {
                    // Europe
                    "DE" -> GERMANY_FREENET
                    "FR" -> FRANCE_FREENET

                    // Africa
                    "BF" -> BURKINA_FASO
                    "BI" -> BURUNDI
                    "BJ" -> BENIN
                    "CD" -> DR_CONGO
                    "ET" -> ETHIOPIA
                    "GH" -> GHANA
                    "GM" -> GAMBIA
                    "GW" -> GUINEA_BISSAU
                    "ML" -> MALI
                    "MW" -> MALAWI
                    "NE" -> NIGER
                    "SC" -> SEYCHELLES
                    "SD" -> SUDAN
                    "SS" -> SOUTH_SUDAN
                    "TD" -> CHAD
                    "TG" -> TOGO
                    "ZW" -> ZIMBABWE

                    else -> DEFAULT_FREENET
                }
            }
        }

        fun getLocationWithPresetApplied(location: Location): Location {
            val locationPreset = getLocationPreset(location.countryCode)

            return location.copy(
                forecastSource = locationPreset.forecast,
                currentSource = locationPreset.current,
                airQualitySource = locationPreset.airQuality,
                pollenSource = locationPreset.pollen,
                minutelySource = locationPreset.minutely,
                alertSource = locationPreset.alert,
                normalsSource = if (location.isCurrentPosition && locationPreset.normals == "accu") {
                    // Special case: if current position, normals are queried at every coordinates
                    // change (instead of once a month), so we want to avoid presetting a minor
                    // feature an user might not be interested in, especially as AccuWeather has not
                    // the best privacy-policy
                    null
                } else {
                    locationPreset.normals
                },
                reverseGeocodingSource = if (location.isCurrentPosition) "nominatim" else null
            )
        }
    }
}
