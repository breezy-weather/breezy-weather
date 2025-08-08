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

package org.breezyweather.sources.breezytz

import android.content.Context
import breezyweather.domain.location.model.Location
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.source.TimeZoneSource
import java.util.TimeZone
import javax.inject.Inject

/**
 * Offline timezone service
 * Based on tzdb 2025b
 * TODO: Missing countries with multiple timezones. See #2093
 */
class BreezyTimeZoneService @Inject constructor() : TimeZoneSource {

    override val id = "breezytz"
    override val name = "Breezy Time Zone"

    override fun requestTimezone(
        context: Context,
        location: Location,
    ): Observable<TimeZone> {
        val timeZonesForCountry = getTimeZonesForCountry(location.countryCode)

        // CASE 1 - Only one timezone for the country
        if (timeZonesForCountry.size == 1) {
            return Observable.just(TimeZone.getTimeZone(timeZonesForCountry[0]))
        }

        // CASE 2 - Multiple timezones for the country
        if (timeZonesForCountry.isNotEmpty()) {
            // TODO: Do something
            return Observable.just(TimeZone.getDefault())
        }

        // OTHER CASES - Fallback to ocean zones
        // Sign is intentionally inverted. See https://github.com/eggert/tz/blob/2025b/etcetera#L37-L43
        return Observable.just(
            when (location.longitude) {
                in 172.5..180.0 -> TimeZone.getTimeZone("Etc/GMT-12")
                in 157.5..172.5 -> TimeZone.getTimeZone("Etc/GMT-11")
                in 142.5..157.5 -> TimeZone.getTimeZone("Etc/GMT-10")
                in 127.5..142.5 -> TimeZone.getTimeZone("Etc/GMT-9")
                in 112.5..127.5 -> TimeZone.getTimeZone("Etc/GMT-8")
                in 97.5..112.5 -> TimeZone.getTimeZone("Etc/GMT-7")
                in 82.5..97.5 -> TimeZone.getTimeZone("Etc/GMT-6")
                in 67.5..82.5 -> TimeZone.getTimeZone("Etc/GMT-5")
                in 52.5..67.5 -> TimeZone.getTimeZone("Etc/GMT-4")
                in 37.5..52.5 -> TimeZone.getTimeZone("Etc/GMT-3")
                in 22.5..37.5 -> TimeZone.getTimeZone("Etc/GMT-2")
                in 7.5..22.5 -> TimeZone.getTimeZone("Etc/GMT-1")
                in -7.5..7.5 -> TimeZone.getTimeZone("Etc/GMT")
                in -22.5..-7.5 -> TimeZone.getTimeZone("Etc/GMT+1")
                in -37.5..-22.5 -> TimeZone.getTimeZone("Etc/GMT+2")
                in -52.5..-37.5 -> TimeZone.getTimeZone("Etc/GMT+3")
                in -67.5..-52.5 -> TimeZone.getTimeZone("Etc/GMT+4")
                in -82.5..-67.5 -> TimeZone.getTimeZone("Etc/GMT+5")
                in -97.5..-82.5 -> TimeZone.getTimeZone("Etc/GMT+6")
                in -112.5..-97.5 -> TimeZone.getTimeZone("Etc/GMT+7")
                in -127.5..-112.5 -> TimeZone.getTimeZone("Etc/GMT+8")
                in -142.5..-127.5 -> TimeZone.getTimeZone("Etc/GMT+9")
                in -157.5..-142.5 -> TimeZone.getTimeZone("Etc/GMT+10")
                in -172.5..-157.5 -> TimeZone.getTimeZone("Etc/GMT+11")
                in -180.0..-172.5 -> TimeZone.getTimeZone("Etc/GMT+12")
                else -> TimeZone.getDefault()
            }
        )
    }

    private fun getTimeZonesForCountry(countryCode: String?): Array<String> {
        if (countryCode.isNullOrEmpty()) {
            return arrayOf()
        }
        return when (countryCode.uppercase()) {
            "AD" -> arrayOf("Europe/Andorra")
            "AE" -> arrayOf("Asia/Dubai")
            "AF" -> arrayOf("Asia/Kabul")
            "AG" -> arrayOf("America/Puerto_Rico")
            "AI" -> arrayOf("America/Puerto_Rico")
            "AL" -> arrayOf("Europe/Tirane")
            "AM" -> arrayOf("Asia/Yerevan")
            "AO" -> arrayOf("Africa/Lagos")
            "AQ" -> arrayOf(
                "Antarctica/Casey",
                "Antarctica/Davis",
                "Antarctica/Mawson",
                "Antarctica/Palmer",
                "Antarctica/Rothera",
                "Antarctica/Troll",
                "Antarctica/Vostok",
                "Asia/Riyadh",
                "Asia/Singapore",
                "Pacific/Auckland",
                "Pacific/Port_Moresby"
            )
            "AR" -> arrayOf(
                "America/Argentina/Buenos_Aires",
                "America/Argentina/Catamarca",
                "America/Argentina/Cordoba",
                "America/Argentina/Jujuy",
                "America/Argentina/La_Rioja",
                "America/Argentina/Mendoza",
                "America/Argentina/Rio_Gallegos",
                "America/Argentina/Salta",
                "America/Argentina/San_Juan",
                "America/Argentina/San_Luis",
                "America/Argentina/Tucuman",
                "America/Argentina/Ushuaia"
            )
            "AS" -> arrayOf("Pacific/Pago_Pago")
            "AT" -> arrayOf("Europe/Vienna")
            "AU" -> arrayOf(
                "Antarctica/Macquarie",
                "Asia/Tokyo",
                "Australia/Adelaide",
                "Australia/Brisbane",
                "Australia/Broken_Hill",
                "Australia/Darwin",
                "Australia/Eucla",
                "Australia/Hobart",
                "Australia/Lindeman",
                "Australia/Lord_Howe",
                "Australia/Melbourne",
                "Australia/Perth",
                "Australia/Sydney"
            )
            "AW" -> arrayOf("America/Puerto_Rico")
            "AX" -> arrayOf("Europe/Helsinki")
            "AZ" -> arrayOf("Asia/Baku")
            "BA" -> arrayOf("Europe/Belgrade")
            "BB" -> arrayOf("America/Barbados")
            "BD" -> arrayOf("Asia/Dhaka")
            "BE" -> arrayOf("Europe/Brussels")
            "BF" -> arrayOf("Africa/Abidjan")
            "BG" -> arrayOf("Europe/Sofia")
            "BH" -> arrayOf("Asia/Qatar")
            "BI" -> arrayOf("Africa/Maputo")
            "BJ" -> arrayOf("Africa/Lagos")
            "BL" -> arrayOf("America/Puerto_Rico")
            "BM" -> arrayOf("Atlantic/Bermuda")
            "BN" -> arrayOf("Asia/Kuching")
            "BO" -> arrayOf("America/La_Paz")
            "BQ" -> arrayOf("America/Puerto_Rico")
            "BR" -> arrayOf(
                "America/Araguaina",
                "America/Bahia",
                "America/Belem",
                "America/Boa_Vista",
                "America/Campo_Grande",
                "America/Cuiaba",
                "America/Eirunepe",
                "America/Fortaleza",
                "America/Maceio",
                "America/Manaus",
                "America/Noronha",
                "America/Porto_Velho",
                "America/Recife",
                "America/Rio_Branco",
                "America/Santarem",
                "America/Sao_Paulo"
            )
            "BS" -> arrayOf("America/Toronto")
            "BT" -> arrayOf("Asia/Thimphu")
            "BW" -> arrayOf("Africa/Maputo")
            "BY" -> arrayOf("Europe/Minsk")
            "BZ" -> arrayOf("America/Belize")
            "CA" -> arrayOf(
                "America/Cambridge_Bay",
                "America/Dawson",
                "America/Dawson_Creek",
                "America/Edmonton",
                "America/Fort_Nelson",
                "America/Glace_Bay",
                "America/Goose_Bay",
                "America/Halifax",
                "America/Inuvik",
                "America/Iqaluit",
                "America/Moncton",
                "America/Panama",
                "America/Phoenix",
                "America/Puerto_Rico",
                "America/Rankin_Inlet",
                "America/Regina",
                "America/Resolute",
                "America/St_Johns",
                "America/Swift_Current",
                "America/Toronto",
                "America/Vancouver",
                "America/Whitehorse",
                "America/Winnipeg"
            )
            "CC" -> arrayOf("Asia/Yangon")
            "CD" -> arrayOf(
                "Africa/Lagos",
                "Africa/Maputo"
            )
            "CF" -> arrayOf("Africa/Lagos")
            "CG" -> arrayOf("Africa/Lagos")
            "CH" -> arrayOf("Europe/Zurich")
            "CI" -> arrayOf("Africa/Abidjan")
            "CK" -> arrayOf("Pacific/Rarotonga")
            "CL" -> arrayOf(
                "America/Coyhaique",
                "America/Punta_Arenas",
                "America/Santiago",
                "Pacific/Easter"
            )
            "CM" -> arrayOf("Africa/Lagos")
            "CN" -> arrayOf(
                "Asia/Shanghai",
                "Asia/Urumqi"
            )
            "CO" -> arrayOf("America/Bogota")
            "CR" -> arrayOf("America/Costa_Rica")
            "CU" -> arrayOf("America/Havana")
            "CV" -> arrayOf("Atlantic/Cape_Verde")
            "CW" -> arrayOf("America/Puerto_Rico")
            "CX" -> arrayOf("Asia/Bangkok")
            "CY" -> arrayOf(
                "Asia/Famagusta",
                "Asia/Nicosia"
            )
            "CZ" -> arrayOf("Europe/Prague")
            "DE" -> arrayOf(
                "Europe/Berlin",
                "Europe/Zurich"
            )
            "DJ" -> arrayOf("Africa/Nairobi")
            "DK" -> arrayOf("Europe/Berlin")
            "DM" -> arrayOf("America/Puerto_Rico")
            "DO" -> arrayOf("America/Santo_Domingo")
            "DZ" -> arrayOf("Africa/Algiers")
            "EC" -> arrayOf(
                "America/Guayaquil",
                "Pacific/Galapagos"
            )
            "EE" -> arrayOf("Europe/Tallinn")
            "EG" -> arrayOf("Africa/Cairo")
            "EH" -> arrayOf("Africa/El_Aaiun")
            "ER" -> arrayOf("Africa/Nairobi")
            "ES" -> arrayOf(
                "Africa/Ceuta",
                "Atlantic/Canary",
                "Europe/Madrid"
            )
            "ET" -> arrayOf("Africa/Nairobi")
            "FI" -> arrayOf("Europe/Helsinki")
            "FJ" -> arrayOf("Pacific/Fiji")
            "FK" -> arrayOf("Atlantic/Stanley")
            "FM" -> arrayOf(
                "Pacific/Guadalcanal",
                "Pacific/Kosrae",
                "Pacific/Port_Moresby"
            )
            "FO" -> arrayOf("Atlantic/Faroe")
            "FR" -> arrayOf("Europe/Paris")
            "GA" -> arrayOf("Africa/Lagos")
            "GB" -> arrayOf("Europe/London")
            "GD" -> arrayOf("America/Puerto_Rico")
            "GE" -> arrayOf("Asia/Tbilisi")
            "GF" -> arrayOf("America/Cayenne")
            "GG" -> arrayOf("Europe/London")
            "GH" -> arrayOf("Africa/Abidjan")
            "GI" -> arrayOf("Europe/Gibraltar")
            "GL" -> arrayOf(
                "America/Danmarkshavn",
                "America/Nuuk",
                "America/Scoresbysund",
                "America/Thule"
            )
            "GM" -> arrayOf("Africa/Abidjan")
            "GN" -> arrayOf("Africa/Abidjan")
            "GP" -> arrayOf("America/Puerto_Rico")
            "GQ" -> arrayOf("Africa/Lagos")
            "GR" -> arrayOf("Europe/Athens")
            "GS" -> arrayOf("Atlantic/South_Georgia")
            "GT" -> arrayOf("America/Guatemala")
            "GU" -> arrayOf("Pacific/Guam")
            "GW" -> arrayOf("Africa/Bissau")
            "GY" -> arrayOf("America/Guyana")
            "HK" -> arrayOf("Asia/Hong_Kong")
            "HN" -> arrayOf("America/Tegucigalpa")
            "HR" -> arrayOf("Europe/Belgrade")
            "HT" -> arrayOf("America/Port-au-Prince")
            "HU" -> arrayOf("Europe/Budapest")
            "ID" -> arrayOf(
                "Asia/Jakarta",
                "Asia/Jayapura",
                "Asia/Makassar",
                "Asia/Pontianak"
            )
            "IE" -> arrayOf("Europe/Dublin")
            "IL" -> arrayOf("Asia/Jerusalem")
            "IM" -> arrayOf("Europe/London")
            "IN" -> arrayOf("Asia/Kolkata")
            "IO" -> arrayOf("Indian/Chagos")
            "IQ" -> arrayOf("Asia/Baghdad")
            "IR" -> arrayOf("Asia/Tehran")
            "IS" -> arrayOf("Africa/Abidjan")
            "IT" -> arrayOf("Europe/Rome")
            "JE" -> arrayOf("Europe/London")
            "JM" -> arrayOf("America/Jamaica")
            "JO" -> arrayOf("Asia/Amman")
            "JP" -> arrayOf("Asia/Tokyo")
            "KE" -> arrayOf("Africa/Nairobi")
            "KG" -> arrayOf("Asia/Bishkek")
            "KH" -> arrayOf("Asia/Bangkok")
            "KI" -> arrayOf(
                "Pacific/Kanton",
                "Pacific/Kiritimati",
                "Pacific/Tarawa"
            )
            "KM" -> arrayOf("Africa/Nairobi")
            "KN" -> arrayOf("America/Puerto_Rico")
            "KP" -> arrayOf("Asia/Pyongyang")
            "KR" -> arrayOf("Asia/Seoul")
            "KW" -> arrayOf("Asia/Riyadh")
            "KY" -> arrayOf("America/Panama")
            "KZ" -> arrayOf(
                "Asia/Almaty",
                "Asia/Aqtau",
                "Asia/Aqtobe",
                "Asia/Atyrau",
                "Asia/Oral",
                "Asia/Qostanay",
                "Asia/Qyzylorda"
            )
            "LA" -> arrayOf("Asia/Bangkok")
            "LB" -> arrayOf("Asia/Beirut")
            "LC" -> arrayOf("America/Puerto_Rico")
            "LI" -> arrayOf("Europe/Zurich")
            "LK" -> arrayOf("Asia/Colombo")
            "LR" -> arrayOf("Africa/Monrovia")
            "LS" -> arrayOf("Africa/Johannesburg")
            "LT" -> arrayOf("Europe/Vilnius")
            "LU" -> arrayOf("Europe/Brussels")
            "LV" -> arrayOf("Europe/Riga")
            "LY" -> arrayOf("Africa/Tripoli")
            "MA" -> arrayOf("Africa/Casablanca")
            "MC" -> arrayOf("Europe/Paris")
            "MD" -> arrayOf("Europe/Chisinau")
            "ME" -> arrayOf("Europe/Belgrade")
            "MF" -> arrayOf("America/Puerto_Rico")
            "MG" -> arrayOf("Africa/Nairobi")
            "MH" -> arrayOf(
                "Pacific/Kwajalein",
                "Pacific/Tarawa"
            )
            "MK" -> arrayOf("Europe/Belgrade")
            "ML" -> arrayOf("Africa/Abidjan")
            "MM" -> arrayOf("Asia/Yangon")
            "MN" -> arrayOf(
                "Asia/Hovd",
                "Asia/Ulaanbaatar"
            )
            "MO" -> arrayOf("Asia/Macau")
            "MP" -> arrayOf("Pacific/Guam")
            "MQ" -> arrayOf("America/Martinique")
            "MR" -> arrayOf("Africa/Abidjan")
            "MS" -> arrayOf("America/Puerto_Rico")
            "MT" -> arrayOf("Europe/Malta")
            "MU" -> arrayOf("Indian/Mauritius")
            "MV" -> arrayOf("Indian/Maldives")
            "MW" -> arrayOf("Africa/Maputo")
            "MX" -> arrayOf(
                "America/Bahia_Banderas",
                "America/Cancun",
                "America/Chihuahua",
                "America/Ciudad_Juarez",
                "America/Hermosillo",
                "America/Matamoros",
                "America/Mazatlan",
                "America/Merida",
                "America/Mexico_City",
                "America/Monterrey",
                "America/Ojinaga",
                "America/Tijuana"
            )
            "MY" -> arrayOf(
                "Asia/Kuching",
                "Asia/Singapore"
            )
            "MZ" -> arrayOf("Africa/Maputo")
            "NA" -> arrayOf("Africa/Windhoek")
            "NC" -> arrayOf("Pacific/Noumea")
            "NE" -> arrayOf("Africa/Lagos")
            "NF" -> arrayOf("Pacific/Norfolk")
            "NG" -> arrayOf("Africa/Lagos")
            "NI" -> arrayOf("America/Managua")
            "NL" -> arrayOf("Europe/Brussels")
            "NO" -> arrayOf("Europe/Berlin")
            "NP" -> arrayOf("Asia/Kathmandu")
            "NR" -> arrayOf("Pacific/Nauru")
            "NU" -> arrayOf("Pacific/Niue")
            "NZ" -> arrayOf(
                "Pacific/Auckland",
                "Pacific/Chatham"
            )
            "OM" -> arrayOf("Asia/Dubai")
            "PA" -> arrayOf("America/Panama")
            "PE" -> arrayOf("America/Lima")
            "PF" -> arrayOf(
                "Pacific/Gambier",
                "Pacific/Marquesas",
                "Pacific/Tahiti"
            )
            "PG" -> arrayOf(
                "Pacific/Bougainville",
                "Pacific/Port_Moresby"
            )
            "PH" -> arrayOf("Asia/Manila")
            "PK" -> arrayOf("Asia/Karachi")
            "PL" -> arrayOf("Europe/Warsaw")
            "PM" -> arrayOf("America/Miquelon")
            "PN" -> arrayOf("Pacific/Pitcairn")
            "PR" -> arrayOf("America/Puerto_Rico")
            "PS" -> arrayOf(
                "Asia/Gaza",
                "Asia/Hebron"
            )
            "PT" -> arrayOf(
                "Atlantic/Azores",
                "Atlantic/Madeira",
                "Europe/Lisbon"
            )
            "PW" -> arrayOf("Pacific/Palau")
            "PY" -> arrayOf("America/Asuncion")
            "QA" -> arrayOf("Asia/Qatar")
            "RE" -> arrayOf("Asia/Dubai")
            "RO" -> arrayOf("Europe/Bucharest")
            "RS" -> arrayOf("Europe/Belgrade")
            "RU" -> arrayOf(
                "Asia/Anadyr",
                "Asia/Barnaul",
                "Asia/Chita",
                "Asia/Irkutsk",
                "Asia/Kamchatka",
                "Asia/Khandyga",
                "Asia/Krasnoyarsk",
                "Asia/Magadan",
                "Asia/Novokuznetsk",
                "Asia/Novosibirsk",
                "Asia/Omsk",
                "Asia/Sakhalin",
                "Asia/Srednekolymsk",
                "Asia/Tomsk",
                "Asia/Ust-Nera",
                "Asia/Vladivostok",
                "Asia/Yakutsk",
                "Asia/Yekaterinburg",
                "Europe/Astrakhan",
                "Europe/Kaliningrad",
                "Europe/Kirov",
                "Europe/Moscow",
                "Europe/Samara",
                "Europe/Saratov",
                "Europe/Simferopol",
                "Europe/Ulyanovsk",
                "Europe/Volgograd"
            )
            "RW" -> arrayOf("Africa/Maputo")
            "SA" -> arrayOf("Asia/Riyadh")
            "SB" -> arrayOf("Pacific/Guadalcanal")
            "SC" -> arrayOf("Asia/Dubai")
            "SD" -> arrayOf("Africa/Khartoum")
            "SE" -> arrayOf("Europe/Berlin")
            "SG" -> arrayOf("Asia/Singapore")
            "SH" -> arrayOf("Africa/Abidjan")
            "SI" -> arrayOf("Europe/Belgrade")
            "SJ" -> arrayOf("Europe/Berlin")
            "SK" -> arrayOf("Europe/Prague")
            "SL" -> arrayOf("Africa/Abidjan")
            "SM" -> arrayOf("Europe/Rome")
            "SN" -> arrayOf("Africa/Abidjan")
            "SO" -> arrayOf("Africa/Nairobi")
            "SR" -> arrayOf("America/Paramaribo")
            "SS" -> arrayOf("Africa/Juba")
            "ST" -> arrayOf("Africa/Sao_Tome")
            "SV" -> arrayOf("America/El_Salvador")
            "SX" -> arrayOf("America/Puerto_Rico")
            "SY" -> arrayOf("Asia/Damascus")
            "SZ" -> arrayOf("Africa/Johannesburg")
            "TC" -> arrayOf("America/Grand_Turk")
            "TD" -> arrayOf("Africa/Ndjamena")
            "TF" -> arrayOf(
                "Asia/Dubai",
                "Indian/Maldives"
            )
            "TG" -> arrayOf("Africa/Abidjan")
            "TH" -> arrayOf("Asia/Bangkok")
            "TJ" -> arrayOf("Asia/Dushanbe")
            "TK" -> arrayOf("Pacific/Fakaofo")
            "TL" -> arrayOf("Asia/Dili")
            "TM" -> arrayOf("Asia/Ashgabat")
            "TN" -> arrayOf("Africa/Tunis")
            "TO" -> arrayOf("Pacific/Tongatapu")
            "TR" -> arrayOf("Europe/Istanbul")
            "TT" -> arrayOf("America/Puerto_Rico")
            "TV" -> arrayOf("Pacific/Tarawa")
            "TW" -> arrayOf("Asia/Taipei")
            "TZ" -> arrayOf("Africa/Nairobi")
            "UA" -> arrayOf(
                "Europe/Kyiv",
                "Europe/Simferopol"
            )
            "UG" -> arrayOf("Africa/Nairobi")
            "UM" -> arrayOf(
                "Pacific/Pago_Pago",
                "Pacific/Tarawa"
            )
            "US" -> arrayOf(
                "America/Adak",
                "America/Anchorage",
                "America/Boise",
                "America/Chicago",
                "America/Denver",
                "America/Detroit",
                "America/Indiana/Indianapolis",
                "America/Indiana/Knox",
                "America/Indiana/Marengo",
                "America/Indiana/Petersburg",
                "America/Indiana/Tell_City",
                "America/Indiana/Vevay",
                "America/Indiana/Vincennes",
                "America/Indiana/Winamac",
                "America/Juneau",
                "America/Kentucky/Louisville",
                "America/Kentucky/Monticello",
                "America/Los_Angeles",
                "America/Menominee",
                "America/Metlakatla",
                "America/New_York",
                "America/Nome",
                "America/North_Dakota/Beulah",
                "America/North_Dakota/Center",
                "America/North_Dakota/New_Salem",
                "America/Phoenix",
                "America/Sitka",
                "America/Yakutat",
                "Pacific/Honolulu"
            )
            "UY" -> arrayOf("America/Montevideo")
            "UZ" -> arrayOf(
                "Asia/Samarkand",
                "Asia/Tashkent"
            )
            "VA" -> arrayOf("Europe/Rome")
            "VC" -> arrayOf("America/Puerto_Rico")
            "VE" -> arrayOf("America/Caracas")
            "VG" -> arrayOf("America/Puerto_Rico")
            "VI" -> arrayOf("America/Puerto_Rico")
            "VN" -> arrayOf(
                "Asia/Bangkok",
                "Asia/Ho_Chi_Minh"
            )
            "VU" -> arrayOf("Pacific/Efate")
            "WF" -> arrayOf("Pacific/Tarawa")
            "WS" -> arrayOf("Pacific/Apia")
            "YE" -> arrayOf("Asia/Riyadh")
            "YT" -> arrayOf("Africa/Nairobi")
            "ZA" -> arrayOf("Africa/Johannesburg")
            "ZM" -> arrayOf("Africa/Maputo")
            "ZW" -> arrayOf("Africa/Maputo")
            else -> arrayOf()
        }
    }
}
