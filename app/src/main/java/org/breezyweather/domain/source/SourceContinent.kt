package org.breezyweather.domain.source

import breezyweather.domain.source.SourceContinent
import org.breezyweather.R

val SourceContinent.resourceName: Int?
    get() = when (this) {
        SourceContinent.WORLDWIDE -> R.string.weather_source_continent_worldwide
        SourceContinent.AFRICA -> R.string.weather_source_continent_africa
        SourceContinent.ASIA -> R.string.weather_source_continent_asia
        SourceContinent.EUROPE -> R.string.weather_source_continent_europe
        SourceContinent.NORTH_AMERICA -> R.string.weather_source_continent_north_america
        SourceContinent.OCEANIA -> R.string.weather_source_continent_oceania
        SourceContinent.SOUTH_AMERICA -> R.string.weather_source_continent_south_america
        else -> null
    }
