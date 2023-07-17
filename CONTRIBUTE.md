## Create a new Weather provider

At each step, have a look at what already exists for other providers if you don’t know what to do.

*Note: Weather converters are currently being rewritten so that you don’t have to call the `CommonConverter` anymore.*


### API key (optional)

If you need an API key or any kind of secret, you will to need declare it in `app/build.gradle` as `breezy.<yourproviderid>.key`.
Then declare the value in `local.properties` which is private and will not be committed.

*TODO: describe how to add a new parameter in settings for custom API key*


### API
Copy the `app/src/main/java/org/breezyweather/sources/openweather/` folder as a base.

Let’s edit the API interface, and only implement the forecast API as a starting point.

In `app/src/main/java/org/breezyweather/source/<yourproviderid>/json/<technicalname>`, add the data class that will be constructed from the json returned by the API.

Use @SerialName when the name of the field is not the same as what is in the json returned by the API.
Example:
```kotlin
@SerialName("is_day") val isDay: Boolean?
```

As in the example, make as many fields as possible nullable so that in case the API doesn’t return some fields for some locations, it doesn’t fail. The serializer is configured to make nullable fields null in case the field is not in the JSON response, so you don’t need to declare `= null` as default value.


### Service and converter
Rename `OpenWeatherService` with your provider name.
As a starting point, inject your weather API for the weather data.

If you need location search support and reverse geocoding, check other providers.
**Exception**: if your weather provider doesn’t accept latitude and longitude for the forecast API, but only a city ID for example, this step will be mandatory.

But let’s focus on the `requestWeather()` function first. You will need to create a converter class.
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

Add your service in the constructor of the `SourceManager` class.

You’re done!

Try build the app, fix errors and complete weather data one bit at a time.
