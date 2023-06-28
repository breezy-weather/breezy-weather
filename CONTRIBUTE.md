## Create a new Weather provider

At each step, have a look at what already exists for other providers if you don’t know what to do.

### Add parameters

Declare new variables in `app/build.gradle`. At minimum, you will need a base URL.
If required, an API key can be added in `local.properties`. Name it that way: `breezy.<providershortid>.key`

*TODO: describe how to add a new parameter in settings for API key*


### Add WeatherSource
In `app/src/main/java/org/breezyweather/common/basic/models/options/provider/WeatherSource.kt`, add a new entry:
- First parameter is a technical name, keep it alphabetical lowercase only.
- Color is reversed, basically 00 becomes FF and FF becomes 00. You can use this tool to help you: https://www.calculator.net/hex-calculator.html?number1=FF&c2op=-&number2=50&calctype=op&x=0&y=0
- Source URL will be displayed at the bottom of each location. It’s the mandatory attribution for data.

Don’t forget to add your new WeatherSource in the `getInstance()` function below.


### Translations
Please edit `R.array.weather_sources` to add the technical name (first parameter of your `WeatherSource`);

Add an entry in the same position as `R.array.weather_sources` for `R.array.weather_source_values` and `R.array.weather_source_voices` for each language that requires it (put it in English if you don't speak that language, usually no translation is needed).


### API
Copy the `app/src/main/java/org/breezyweather/weather/openweather/` folder as a base.

Let’s edit the API interface, and only implement the forecast API as a starting point.

In `app/src/main/java/org/breezyweather/weather/<yourprovider>/json/<technicalname>`, add the data class that will be constructed from the json returned by the API.

Use @SerialName when the name of the field is not the same as what is in the json returned by the API.
Example:
```kotlin
@SerialName("is_day") val isDay: Boolean?
```

As in the example, make as many fields as possible nullable so that in case the API doesn’t return some fields for some locations, it doesn’t fail. The serializer is configured to make nullable fields null in case the field is not in the JSON response, so you don’t need to declare `= null` as default value.

Add the API class as a provider in `app/src/main/java/org/breezyweather/weather/ApiModule.java` (copy an existing function).


### Service and converter
Copy `OpenMeteoWeatherService` from `app/src/main/java/org/breezyweather/weather/openweather/` and replace the OpenWeatherWeatherService copied earlier.

In the constructor, you can inject as many providers as you need.
As a starting point, inject your weather API for the weather data, and `OpenMeteoGeocodingApi` for the geocoding part.
You can still implement it your own geocoding later, but to get a running example, we will skip this part for now.
**Exception**: if your weather provider doesn’t accept latitude and longitude for the forecast API, but only a city ID for example, you will need to implement the geocoding part.

Replace `WeatherSource.OPEN_METEO` with your `WeatherSource` in the `requestLocationSearch` function.

Then focus on the `requestWeather()` function. You will need to create a converter class.
The goal of a converter class is to normalize the data we received into Breezy Weather data objects.

Here is the minimum code you need to put in your converter:
```kotlin
fun convert(
    context: Context,
    location: Location,
    weatherResult: MyProviderWeatherResult
): WeatherResultWrapper {
    return try {
        val weather = Weather(
            base = Base(cityId = location.cityId)
            /* Complete other parameters one bit at a time */
        )
        WeatherResultWrapper(weather)
    } catch (e: Exception) {
        if (BreezyWeather.instance.debugMode) {
            e.printStackTrace()
        }
        WeatherResultWrapper(null)
    }
}
```

Add your service in the `WeatherServiceSet` class.

You’re done!

Try build the app, fix errors and complete weather data one bit at a time.
