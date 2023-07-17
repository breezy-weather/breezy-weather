package org.breezyweather.common.source

import org.breezyweather.common.basic.models.weather.Weather

// TODO: As we move to modularization, we will have wrapper for every kind of weather data
// air quality, minutely, location, etc
class WeatherResultWrapper(val result: Weather?)