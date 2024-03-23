package org.breezyweather.sources.wmosevereweather

import breezyweather.domain.location.model.Location

/**
 * Up-to-date as of 2024-03-15
 */
enum class WmoSevereWeatherAlertLibrary(
    val id: String,
    val supportedCountries: Set<String>
) {
    HONG_KONG("hk", setOf("HK")),
    USA("usa", setOf("US", "PR", "VI", "MP", "GU", "FM", "PW", "AS", "UM", "XB", "XH", "XQ", "XU", "XM", "QM", "XV", "XL", "QW")),
    CANADA("canada", setOf("CA")),
    GERMANY("germany", setOf("DE")),
    MYANMAR("myanmar", setOf("MM", "BU")),
    METEO_ALARM_V2(
        "meteoalarmV2",
        setOf(
            "AT", // Austria
            "BE", // Belgium
            "BA", // Bosnia and Herzegovina
            "BG", // Bulgaria
            "HR", // Croatia
            "CY", // Cyprus
            "CZ", // Czechia
            "DK", // Denmark
            "EE", "EW", // Estonia
            "FI", "AX", "SF", // Finland
            "FR", "FX", // France
            //"DE", // Germany: Already supported separately
            "GR", "EL", // Greece
            "HU", // Hungary
            //"IS", // Iceland: Already supported separately
            "IE", // Ireland
            "IL", "PS", // Israel + West Bank + Gaza Strip
            "IT", // Italy
            "LV", // Latvia
            "LT", // Lithuania
            "LU", // Luxembourg
            "MT", // Malta
            "MD", // Moldova
            "ME", // Montenegro
            "NL", // Netherlands
            "MK", // North Macedonia
            "NO", // Norway
            "PL", // Poland
            "PT", // Portugal
            "RO", // Romania
            "RS", // Serbia
            "SK", // Slovakia
            "SI", // Slovenia
            //"ES", "IC", "EA", // Spain: Already supported separately
            "SE", // Sweden
            "CH", // Switzerland
            "UA", // Ukraine
            "UK", "GB", "CQ", "GG", "IM", "JE", // United Kingdom
            "CS", // Old Czechoslovakia and later "Serbia and Montenegro"
            "YU" // Old Yugoslavia
        )
    ),
    ICELAND("iceland", setOf("IS")),
    SPAIN("spain", setOf("ES", "IC", "EA")),
    BARBADOS("barbados", setOf("BB")),
    NEW_ZEALAND("newzealand", setOf("NZ")),
    PAPUA_NEW_GUINEA("png", setOf("PG")),
    JAMAICA("jamaica", setOf("JA", "JM")),
    RUSSIAN("russian", setOf("RU")),
    INDONESIA("indonesia", setOf("ID", "RI")),
    ALL("all", setOf()), // All countries that use `url`. Example: Brazil, China, Chile, Mexico, Norway
    AUSTRALIA("australia", setOf("AU", "CC", "CX", /*"HM"*/"NF")),
    SOUTH_AFRICA("southAfrica", setOf("ZA")),
    OTHERS("others", setOf()), // All countries that use `capURL`. Example: India, Iran, Libya, Mozambique
    MACAO("macao", setOf("MO")),
    ALGERIA("algeria", setOf("DZ")),
    TRINIDAD_AND_TOBAGO("trinidadAndTobago", setOf("TT")),
    SAUDI_ARABIA("saudiArabia", setOf("SA")),
    THAILAND("thailand", setOf("TH")),
    ARGENTINA("argentina", setOf("AR", "RA")),
    BELARUS("belarus", setOf("BY")),
    KAZAKHSTAN("kazakhstan", setOf("KZ"));

    companion object {

        fun getInstance(
            value: String
        ) = WmoSevereWeatherAlertLibrary.entries.firstOrNull {
            it.id == value
        }

        fun getLibrariesForLocation(location: Location): List<String> {
            return WmoSevereWeatherAlertLibrary.entries.filter { library ->
                library.supportedCountries.any {
                    location.countryCode?.equals(it, ignoreCase = true) == true
                }
            }.ifEmpty {
                WmoSevereWeatherAlertLibrary.entries.filter {
                    it.supportedCountries.isEmpty()
                }
            }.map {
                it.id
            }
        }
    }
}
