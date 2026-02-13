/*
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

package org.breezyweather.sources.breezytz

import android.content.Context
import breezyweather.domain.location.model.Location
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.Feature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.extensions.parseRawGeoJson
import org.breezyweather.common.source.TimeZoneSource
import java.util.TimeZone
import javax.inject.Inject

/**
 * Offline timezone service
 * Based on tzdb 2025b
 * TODO:
 *  AQ - Antartica
 *  AU - Australia minor location splits
 *  CA - Canada minor location split (Labrador)
 *  GL - Greenland
 *  TF - Crozet islands are on a different timezone
 *  UM - US Minor Outlying Islands
 */
class BreezyTimeZoneService @Inject constructor(
    @ApplicationContext context: Context,
) : TimeZoneSource {

    override val id = "breezytz"
    override val name = "Breezy Time Zone"

    private val arGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_ar) }
    private val auGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_au) }
    private val brGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_br) }
    private val caGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_ca) }
    private val cdGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_cd) }
    private val clGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_cl) }
    private val cnGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_cn) }
    private val cyGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_cy) }
    private val ecGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_ec) }
    private val esGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_es) }
    private val fmGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_fm) }
    private val idGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_id) }
    private val kiGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_ki) }
    private val kzGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_kz) }
    private val mnGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_mn) }
    private val mxGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_mx) }
    private val myGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_my) }
    private val nzGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_nz) }
    private val pfGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_pf) }
    private val pgGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_pg) }
    private val psGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_ps) }
    private val ptGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_pt) }
    private val ruGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_ru) }
    private val uaGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_ua) }
    private val usGeoJson: GeoJsonParser by lazy { context.parseRawGeoJson(R.raw.breezytz_us) }

    override fun requestTimezone(
        context: Context,
        location: Location,
    ): Observable<TimeZone> {
        val timezone = when (location.countryCode?.uppercase()) {
            "AD" -> "Europe/Andorra"
            "AE" -> "Asia/Dubai"
            "AF" -> "Asia/Kabul"
            "AG" -> "America/Puerto_Rico"
            "AI" -> "America/Puerto_Rico"
            "AL" -> "Europe/Tirane"
            "AM" -> "Asia/Yerevan"
            "AO" -> "Africa/Lagos"
            "AQ" -> getTimeZoneForMultiTimeZoneCountry(location)
            "AR" -> getTimeZoneForMultiTimeZoneCountry(location, "America/Argentina/Buenos_Aires")
            "AS" -> "Pacific/Pago_Pago"
            "AT" -> "Europe/Vienna"
            "AU" -> getTimeZoneForMultiTimeZoneCountry(location)
            "AW" -> "America/Puerto_Rico"
            "AX" -> "Europe/Helsinki"
            "AZ" -> "Asia/Baku"
            "BA" -> "Europe/Belgrade"
            "BB" -> "America/Barbados"
            "BD" -> "Asia/Dhaka"
            "BE" -> "Europe/Brussels"
            "BF" -> "Africa/Abidjan"
            "BG" -> "Europe/Sofia"
            "BH" -> "Asia/Qatar"
            "BI" -> "Africa/Maputo"
            "BJ" -> "Africa/Lagos"
            "BL" -> "America/Puerto_Rico"
            "BM" -> "Atlantic/Bermuda"
            "BN" -> "Asia/Kuching"
            "BO" -> "America/La_Paz"
            "BQ" -> "America/Puerto_Rico"
            "BR" -> getTimeZoneForMultiTimeZoneCountry(location)
            "BS" -> "America/Toronto"
            "BT" -> "Asia/Thimphu"
            "BW" -> "Africa/Maputo"
            "BY" -> "Europe/Minsk"
            "BZ" -> "America/Belize"
            "CA" -> getTimeZoneForMultiTimeZoneCountry(location)
            "CC" -> "Asia/Yangon"
            "CD" -> getTimeZoneForMultiTimeZoneCountry(location)
            "CF" -> "Africa/Lagos"
            "CG" -> "Africa/Lagos"
            "CH" -> "Europe/Zurich"
            "CI" -> "Africa/Abidjan"
            "CK" -> "Pacific/Rarotonga"
            "CL" -> getTimeZoneForMultiTimeZoneCountry(location, "America/Santiago")
            "CM" -> "Africa/Lagos"
            "CN" -> getTimeZoneForMultiTimeZoneCountry(location, "Asia/Shanghai")
            "CO" -> "America/Bogota"
            "CR" -> "America/Costa_Rica"
            "CU" -> "America/Havana"
            "CV" -> "Atlantic/Cape_Verde"
            "CW" -> "America/Puerto_Rico"
            "CX" -> "Asia/Bangkok"
            "CY" -> getTimeZoneForMultiTimeZoneCountry(location, "Asia/Nicosia")
            "CZ" -> "Europe/Prague"
            "DE" -> "Europe/Berlin" // "Europe/Busingen" is identical
            "DJ" -> "Africa/Nairobi"
            "DK" -> "Europe/Berlin"
            "DM" -> "America/Puerto_Rico"
            "DO" -> "America/Santo_Domingo"
            "DZ" -> "Africa/Algiers"
            "EC" -> getTimeZoneForMultiTimeZoneCountry(location, "America/Guayaquil")
            "EE" -> "Europe/Tallinn"
            "EG" -> "Africa/Cairo"
            "EH" -> "Africa/El_Aaiun"
            "ER" -> "Africa/Nairobi"
            "ES" -> getTimeZoneForMultiTimeZoneCountry(location, "Europe/Madrid")
            "ET" -> "Africa/Nairobi"
            "FI" -> "Europe/Helsinki"
            "FJ" -> "Pacific/Fiji"
            "FK" -> "Atlantic/Stanley"
            "FM" -> getTimeZoneForMultiTimeZoneCountry(location)
            "FO" -> "Atlantic/Faroe"
            "FR" -> "Europe/Paris"
            "GA" -> "Africa/Lagos"
            "GB" -> "Europe/London"
            "GD" -> "America/Puerto_Rico"
            "GE" -> "Asia/Tbilisi"
            "GF" -> "America/Cayenne"
            "GG" -> "Europe/London"
            "GH" -> "Africa/Abidjan"
            "GI" -> "Europe/Gibraltar"
            "GL" -> getTimeZoneForMultiTimeZoneCountry(location, "America/Nuuk")
            "GM" -> "Africa/Abidjan"
            "GN" -> "Africa/Abidjan"
            "GP" -> "America/Puerto_Rico"
            "GQ" -> "Africa/Lagos"
            "GR" -> "Europe/Athens"
            "GS" -> "Atlantic/South_Georgia"
            "GT" -> "America/Guatemala"
            "GU" -> "Pacific/Guam"
            "GW" -> "Africa/Bissau"
            "GY" -> "America/Guyana"
            "HK" -> "Asia/Hong_Kong"
            "HN" -> "America/Tegucigalpa"
            "HR" -> "Europe/Belgrade"
            "HT" -> "America/Port-au-Prince"
            "HU" -> "Europe/Budapest"
            "ID" -> getTimeZoneForMultiTimeZoneCountry(location)
            "IE" -> "Europe/Dublin"
            "IL" -> "Asia/Jerusalem"
            "IM" -> "Europe/London"
            "IN" -> "Asia/Kolkata"
            "IO" -> "Indian/Chagos"
            "IQ" -> "Asia/Baghdad"
            "IR" -> "Asia/Tehran"
            "IS" -> "Africa/Abidjan"
            "IT" -> "Europe/Rome"
            "JE" -> "Europe/London"
            "JM" -> "America/Jamaica"
            "JO" -> "Asia/Amman"
            "JP" -> "Asia/Tokyo"
            "KE" -> "Africa/Nairobi"
            "KG" -> "Asia/Bishkek"
            "KH" -> "Asia/Bangkok"
            "KI" -> getTimeZoneForMultiTimeZoneCountry(location)
            "KM" -> "Africa/Nairobi"
            "KN" -> "America/Puerto_Rico"
            "KP" -> "Asia/Pyongyang"
            "KR" -> "Asia/Seoul"
            "KW" -> "Asia/Riyadh"
            "KY" -> "America/Panama"
            "KZ" -> getTimeZoneForMultiTimeZoneCountry(location, "Asia/Almaty")
            "LA" -> "Asia/Bangkok"
            "LB" -> "Asia/Beirut"
            "LC" -> "America/Puerto_Rico"
            "LI" -> "Europe/Zurich"
            "LK" -> "Asia/Colombo"
            "LR" -> "Africa/Monrovia"
            "LS" -> "Africa/Johannesburg"
            "LT" -> "Europe/Vilnius"
            "LU" -> "Europe/Brussels"
            "LV" -> "Europe/Riga"
            "LY" -> "Africa/Tripoli"
            "MA" -> "Africa/Casablanca"
            "MC" -> "Europe/Paris"
            "MD" -> "Europe/Chisinau"
            "ME" -> "Europe/Belgrade"
            "MF" -> "America/Puerto_Rico"
            "MG" -> "Africa/Nairobi"
            "MH" -> "Pacific/Majuro" // Links to "Pacific/Tarawa", and "Pacific/Kwajalein" is identical
            "MK" -> "Europe/Belgrade"
            "ML" -> "Africa/Abidjan"
            "MM" -> "Asia/Yangon"
            "MN" -> getTimeZoneForMultiTimeZoneCountry(location, "Asia/Ulaanbaatar")
            "MO" -> "Asia/Macau"
            "MP" -> "Pacific/Guam"
            "MQ" -> "America/Martinique"
            "MR" -> "Africa/Abidjan"
            "MS" -> "America/Puerto_Rico"
            "MT" -> "Europe/Malta"
            "MU" -> "Indian/Mauritius"
            "MV" -> "Indian/Maldives"
            "MW" -> "Africa/Maputo"
            "MX" -> getTimeZoneForMultiTimeZoneCountry(location)
            "MY" -> getTimeZoneForMultiTimeZoneCountry(location, "Asia/Kuala_Lumpur")
            "MZ" -> "Africa/Maputo"
            "NA" -> "Africa/Windhoek"
            "NC" -> "Pacific/Noumea"
            "NE" -> "Africa/Lagos"
            "NF" -> "Pacific/Norfolk"
            "NG" -> "Africa/Lagos"
            "NI" -> "America/Managua"
            "NL" -> "Europe/Brussels"
            "NO" -> "Europe/Berlin"
            "NP" -> "Asia/Kathmandu"
            "NR" -> "Pacific/Nauru"
            "NU" -> "Pacific/Niue"
            "NZ" -> getTimeZoneForMultiTimeZoneCountry(location, "Pacific/Auckland")
            "OM" -> "Asia/Dubai"
            "PA" -> "America/Panama"
            "PE" -> "America/Lima"
            "PF" -> getTimeZoneForMultiTimeZoneCountry(location)
            "PG" -> getTimeZoneForMultiTimeZoneCountry(location, "Pacific/Port_Moresby")
            "PH" -> "Asia/Manila"
            "PK" -> "Asia/Karachi"
            "PL" -> "Europe/Warsaw"
            "PM" -> "America/Miquelon"
            "PN" -> "Pacific/Pitcairn"
            "PR" -> "America/Puerto_Rico"
            "PS" -> getTimeZoneForMultiTimeZoneCountry(location)
            "PT" -> getTimeZoneForMultiTimeZoneCountry(location, "Europe/Lisbon")
            "PW" -> "Pacific/Palau"
            "PY" -> "America/Asuncion"
            "QA" -> "Asia/Qatar"
            "RE" -> "Asia/Dubai"
            "RO" -> "Europe/Bucharest"
            "RS" -> "Europe/Belgrade"
            "RU" -> getTimeZoneForMultiTimeZoneCountry(location)
            "RW" -> "Africa/Maputo"
            "SA" -> "Asia/Riyadh"
            "SB" -> "Pacific/Guadalcanal"
            "SC" -> "Asia/Dubai"
            "SD" -> "Africa/Khartoum"
            "SE" -> "Europe/Berlin"
            "SG" -> "Asia/Singapore"
            "SH" -> "Africa/Abidjan"
            "SI" -> "Europe/Belgrade"
            "SJ" -> "Europe/Berlin"
            "SK" -> "Europe/Prague"
            "SL" -> "Africa/Abidjan"
            "SM" -> "Europe/Rome"
            "SN" -> "Africa/Abidjan"
            "SO" -> "Africa/Nairobi"
            "SR" -> "America/Paramaribo"
            "SS" -> "Africa/Juba"
            "ST" -> "Africa/Sao_Tome"
            "SV" -> "America/El_Salvador"
            "SX" -> "America/Puerto_Rico"
            "SY" -> "Asia/Damascus"
            "SZ" -> "Africa/Johannesburg"
            "TC" -> "America/Grand_Turk"
            "TD" -> "Africa/Ndjamena"
            "TF" -> "GMT" // "Indian/Kerguelen" // Links to "Indian/Maldives". TODO: "Asia/Dubai" for Crozet islands
            "TG" -> "Africa/Abidjan"
            "TH" -> "Asia/Bangkok"
            "TJ" -> "Asia/Dushanbe"
            "TK" -> "Pacific/Fakaofo"
            "TL" -> "Asia/Dili"
            "TM" -> "Asia/Ashgabat"
            "TN" -> "Africa/Tunis"
            "TO" -> "Pacific/Tongatapu"
            "TR" -> "Europe/Istanbul"
            "TT" -> "America/Puerto_Rico"
            "TV" -> "Pacific/Tarawa"
            "TW" -> "Asia/Taipei"
            "TZ" -> "Africa/Nairobi"
            "UA" -> getTimeZoneForMultiTimeZoneCountry(location, "Europe/Kyiv")
            "UG" -> "Africa/Nairobi"
            "UM" -> "GMT" // TODO: arrayOf("Pacific/Johnston", "Pacific/Pago_Pago", "Pacific/Tarawa")
            "US" -> getTimeZoneForMultiTimeZoneCountry(location)
            "UY" -> "America/Montevideo"
            "UZ" -> "Asia/Tashkent" // "Asia/Samarkand" follows the same timezone
            "VA" -> "Europe/Rome"
            "VC" -> "America/Puerto_Rico"
            "VE" -> "America/Caracas"
            "VG" -> "America/Puerto_Rico"
            "VI" -> "America/Puerto_Rico"
            "VN" -> "Asia/Ho_Chi_Minh" // "Asia/Bangkok" follows the same timezone
            "VU" -> "Pacific/Efate"
            "WF" -> "Pacific/Tarawa"
            "WS" -> "Pacific/Apia"
            "XK" -> "Europe/Belgrade"
            "YE" -> "Asia/Riyadh"
            "YT" -> "Africa/Nairobi"
            "ZA" -> "Africa/Johannesburg"
            "ZM" -> "Africa/Maputo"
            "ZW" -> "Africa/Maputo"
            else -> "GMT"
        }

        return Observable.just(TimeZone.getTimeZone(timezone))
    }

    private fun isMatchingTimeZone(
        feature: Feature,
        location: Location,
    ): Boolean {
        return when (feature.geometry) {
            is GeoJsonPolygon -> (feature.geometry as GeoJsonPolygon).coordinates.any { polygon ->
                PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
            }
            is GeoJsonMultiPolygon -> (feature.geometry as GeoJsonMultiPolygon).polygons.any {
                it.coordinates.any { polygon ->
                    PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
                }
            }
            else -> false
        }
    }

    /**
     * @param location with a multi-timezone country code
     * @param defaultTimeZone fallback timezone value when not found
     */
    private fun getTimeZoneForMultiTimeZoneCountry(
        location: Location,
        defaultTimeZone: String = "GMT",
    ): String {
        // If we have defined geometry file for a country, look for the matching time zone shape.

        val geojson = when (location.countryCode?.uppercase()) {
            "AR" -> arGeoJson
            "AU" -> auGeoJson
            "BR" -> brGeoJson
            "CA" -> caGeoJson
            "CD" -> cdGeoJson
            "CL" -> clGeoJson
            "CN" -> cnGeoJson
            "CY" -> cyGeoJson
            "EC" -> ecGeoJson
            "ES" -> esGeoJson
            "FM" -> fmGeoJson
            "ID" -> idGeoJson
            "KI" -> kiGeoJson
            "KZ" -> kzGeoJson
            "MN" -> mnGeoJson
            "MX" -> mxGeoJson
            "MY" -> myGeoJson
            "NZ" -> nzGeoJson
            "PF" -> pfGeoJson
            "PG" -> pgGeoJson
            "PS" -> psGeoJson
            "PT" -> ptGeoJson
            "RU" -> ruGeoJson
            "UA" -> uaGeoJson
            "US" -> usGeoJson
            else -> null
        }

        val features = geojson?.features?.filter { feature ->
            val subdivisions = feature.getProperty("isoPart2")?.split(",") ?: emptyList()
            location.admin1Code in subdivisions || location.admin2Code in subdivisions
        }

        // Found only one feature matching the ISO 3166-2 code: return the time zone directly
        if (features?.size == 1) {
            features.first().getProperty("timezone")?.let { return it }
        }

        // Found multiple features matching the ISO 3166-2 code: match location against only those features
        if (!features.isNullOrEmpty()) {
            features
                .firstOrNull { isMatchingTimeZone(it, location) }
                ?.getProperty("timezone")
                ?.let { return it }
        }

        // Did not find any feature matching the ISO 3166-2 code: match location against all features
        geojson?.features
            ?.firstOrNull { isMatchingTimeZone(it, location) }
            ?.getProperty("timezone")
            ?.let { return it }

        // If there is no defined geometry or matching time zone shape,
        // see if default time zone has been set for the country.
        // This should be the case where multiple time zones are mere offshoots from the mainland
        // which has one time zone: e.g. Chile, China, Ecuador, Portugal, Spain
        // If no matching geometry and no default, fail as "GMT"
        return defaultTimeZone
    }
}
