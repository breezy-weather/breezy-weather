## Create a new Weather provider

Choose a unique identifier for your weather provider, with only lowercase letters. Examples:
- AccuWeather becomes `accu`
- Open-Meteo becomes `openmeteo`

Copy:
```
app/src/main/java/org/breezyweather/sources/openweather/
```
to:
```
app/src/main/java/org/breezyweather/sources/<yourproviderid>/
```

We will use OpenWeather as a base as it is the most “apply to most situations” provider, without having too many specific code that most providers don’t need.
But at each step, you can have a look at what already exists for this provider if you feel like something you want to implement might already have been done on other providers.


### API key (optional)

If you need an API key or any kind of secret, you will to need declare it in `app/build.gradle` as `breezy.<yourproviderid>.key`.
Then declare the value in `local.properties` which is private and will not be committed.


### Preferences

*TODO: describe how to add a new parameter in settings for custom API key*


### API

Let’s edit the API interface, and only implement the forecast API as a starting point.

In `app/src/main/java/org/breezyweather/source/<yourproviderid>/json/<technicalname>`, add the data class that will be constructed from the json returned by the API.

Use `@SerialName` when the name of the field is not the same as what is in the json returned by the API.
Example:
```kotlin
@SerialName("is_day") val isDay: Boolean?
```

As in the example, make as many fields as possible nullable so that in case the API doesn’t return some fields for some locations, it doesn’t fail. The serializer is configured to make nullable fields null in case the field is not in the JSON response, so you don’t need to declare `= null` as default value.


### Service and converter

Rename `OpenWeatherService` with your provider name and completes basic information.

As a starting point, we will only implement weather part, but here is the full list of interfaces/classes you can implement:

| Class/Interface          | Use case                                                                                                                                                                                                                                       |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `HttpSource()`           | Currently does nothing except requiring to provide a link to privacy policy, which will be mandatory to accept in the future                                                                                                                   |
| `WeatherSource`          | Your provider can provide hourly forecast for a given lon/lat. If your provider doesn’t accept lon/lat but cities-only, you will have to implement `LocationSearchSource` and `ReverseGeocodingSource`                                         |
| `LocationSearchSource`   | Your provider is able to return a list of `Location` object from a query, containing at least the TimeZone of the location. If your provider doesn’t include TimeZone, don’t implement it, and this will default to Open-Meteo location search |
| `ReverseGeocodingSource` | Your provider is able to return one `Location` (you can pick the first one if you have many) from lon/lat. If you don’t have this feature available, don’t implement it and locations created with your provider will only have lon/lat        |     
| `ConfigurableSource`     | You want to allow your user to change preferences, for example API key.                                                                                                                                                                        |

Let’s focus on the `requestWeather()` function now. You will need to adapt the existing converter class.
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

*Note: Weather converters are currently being rewritten so that you don’t have to call the `CommonConverter` anymore.*

Add your service in the constructor of the `SourceManager` class.

You’re done!

Try build the app, fix errors and complete weather data one bit at a time.
