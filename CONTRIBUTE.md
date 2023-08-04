# Contributions

## Rules for inclusion of new weather sources

To be candidate for inclusion in the project, a weather source must:
- have a free-tier available with hourly forecast at minimum
- not require credit card information to have a free key (OpenWeather is exempted as it didn’t require it at the time it was implemented)

Additionally, we usually don’t accept sources that are just frontends to other sources (for example, if they use AccuWeather data, we will just use AccuWeather directly).

Examples of weather sources that don’t fit:
- Apple WeatherKit (no free-tier)
- Microsoft Azure (free-tier requires credit card info)
- Weatherbit (free-tier doesn’t have hourly)


## Git setup for pull requests

Fork the project on GitHub.

Clone the project locally, then add our repository as `upstream` remote:
```
git remote add upstream https://github.com/breezy-weather/breezy-weather
```

Create a new branch for your pull request, for example:
```
git checkout -B mynewprovider
```

You can start working on it!


## Create a new Weather source

Choose a unique identifier for your weather source, with only lowercase letters. Examples:
- AccuWeather becomes `accu`
- Open-Meteo becomes `openmeteo`

Copy:
```
app/src/main/java/org/breezyweather/sources/openweather/
```
to:
```
app/src/main/java/org/breezyweather/sources/<yoursourceid>/
```

We will use OpenWeather as a base as it is the most “apply to most situations” source, without having too many specific code that most sources don’t need.
But at each step, you can have a look at what already exists for this source if you feel like something you want to implement might already have been done on other sources.


### API key (optional)

If you need an API key or any kind of secret, you will to need declare it in `app/build.gradle` as `breezy.<yoursourceid>.key`.
Then declare the value in `local.properties` which is private and will not be committed.


### API

Let’s edit the API interface, and only implement the forecast API as a starting point.

In `app/src/main/java/org/breezyweather/source/<yoursourceid>/json/<technicalname>`, add the data class that will be constructed from the json returned by the API.

Use `@SerialName` when the name of the field is not the same as what is in the json returned by the API.
Example:
```kotlin
@SerialName("is_day") val isDay: Boolean?
```

As in the example, make as many fields as possible nullable so that in case the API doesn’t return some fields for some locations, it doesn’t fail. The serializer is configured to make nullable fields null in case the field is not in the JSON response, so you don’t need to declare `= null` as default value.


### Service and converter

Rename `OpenWeatherService` with your source name and completes basic information.

As a starting point, we will only implement weather part, but here is the full list of interfaces/classes you can implement:

| Class/Interface          | Use case                                                                                                                                                                                                                                   |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `HttpSource()`           | Currently does nothing except requiring to provide a link to privacy policy, which will be mandatory to accept in the future                                                                                                               |
| `WeatherSource`          | Your source can provide hourly forecast for a given lon/lat. If your source doesn’t accept lon/lat but cities-only, you will have to implement `LocationSearchSource` and `ReverseGeocodingSource`                                         |
| `LocationSearchSource`   | Your source is able to return a list of `Location` object from a query, containing at least the TimeZone of the location. If your source doesn’t include TimeZone, don’t implement it, and this will default to Open-Meteo location search |
| `ReverseGeocodingSource` | Your source is able to return one `Location` (you can pick the first one if you have many) from lon/lat. If you don’t have this feature available, don’t implement it and locations created with your source will only have lon/lat        |     
| `ConfigurableSource`     | You want to allow your user to change preferences, for example API key.                                                                                                                                                                    |

Let’s focus on the `requestWeather()` function now. You will need to adapt the existing converter class.
The goal of a converter class is to normalize the data we received into Breezy Weather data objects.

Here is the minimum code you need to put in your converter:
```kotlin
fun convert(
    location: Location,
    weatherResult: MySourceWeatherResult
): WeatherWrapper {
    return WeatherWrapper()
}
```

Yes, of course, you won’t have any data that way, but it’s just to show you that all data is non-mandatory. You can have a look at the non-mandatory parameters of the WeatherResultWrapper object and complete bit by bit the data as you feel.

Add your service in the constructor of the `SourceManager` class.

You’re done, you can try building the app and test that you have empty data.

**IMPORTANT**: please don’t try to “calculate” missing data. For example, if you have hourly air quality available in your source, but not daily air quality, don’t try to calculate the daily air quality from hourly data! The app already takes care of completing any missing data for you. And if you feel that something that could be completed is not, please open an issue and we will improve the app to do so for all sources.

**Additional note**: the Daily object expects two half days, which most sources don’t provide.
As explained in other documents, the daytime halfday is expected from 06:00 to 17:59 and the nighttime halfday is expected from 18:00 to 05:59 (or 29:59 to keep current day notation).
- If your source has half days with different hours, please follow their recommendations (for example, ColorfulClouds uses 08:00 to 19:59 and 20:00 to 07:59 (or 31:59)).
- If your source has no half day, a typical mistake you can make is to put the minimum temperature of the day as temperature of the night. However, your source probably gives you the minimum temperature from the past overnight, not from the night to come, so make sure to pick the correct data!

Once your source is complete (you use all available data from the API and available in Breezy Weather), please rebase and submit it as a pull request (see instructions below). Please allow Breezy Weather maintainers to make adjustments (but we won’t write the source for you, you will have to make significant implementation).


## Submit a pull request

Since you started working on your pull request, many commits might have been added, so you will need to rebase:
```
git fetch upstream
git rebase upstream main
```

(it it can’t find `upstream`, check instructions at the top of this document)

If you are working on a new provider, you will usually not have any conflict, unless a new provider was added in the meantime in `SourceManager`, but in that case, you will find it easy to fix the conflict.

Then, you can push (with `--force` argument as you are rewriting history).

Please test your changes and if it works and you made multiple commits, please stash them as it makes reviewing easier. For example, if you made 2 commits, you can use:
```
git reset --soft HEAD~2
```

You can make a new commit, and once again, push your changes adding the `--force` argument.
