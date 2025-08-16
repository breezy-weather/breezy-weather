# Contributions

## Rules for contributions

While we welcome pull requests, before implementing any new feature/improvement, we ask you to come talk to us, to be sure it goes in the right direction. We don’t want you to spend time implementing something we don’t want (see “Rules for new features/improvements requests” section below) or implementing it the wrong way.

You can also contribute to [existing issues tagged “Open to contributions”](https://github.com/breezy-weather/breezy-weather/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22Open%20to%20contributions%22), or [existing ideas tagged “Open to contributions”](https://github.com/breezy-weather/breezy-weather/discussions?discussions_q=is%3Aopen+label%3A%22Open+to+contributions%22).

Prerequisites for pull requests contributions:
1. You are contributing to an issue/idea tagged “Open to contributions”, or an [org member](https://github.com/orgs/breezy-weather/people) gave you permission to work on it.


## Rules for new features/improvements requests

### General direction

Breezy Weather wants to be:
- a general weather app covering most of what you can expect from a weather app, but not *all* of what you can expect. For advanced usage, some specialized apps will always cover it better
- usable without having to be an expert to find anything in the app
- mainly target small displays, so we don’t want to fit too many things, as we also want to let the design breathe a bit


### New preference

Probably, the most requested thing. “If you don’t want to make that feature for everyone, you can still make it a preference”.

Currently, we already have more than 50 preferences (not even counting widgets preferences and sources preferences!), which already provides a lot of customizability.

I know what you’re going to tell me “If there are already that many options, just ONE additional option won’t hurt”, but truth is if I had on top of existing preferences implemented every ONE preference people requested since this project began, I would have doubled the number of preferences (and I’m only writing this 1.5 months after the project began), and things people are mostly looking for would be hard to find in the myriad of options.

At the same time, with the existing preferences, some people can’t even find things, we already spend a lot of time helping people to find what they are looking for, and it shouldn’t be that way. Some people may even just drop the app because it's too hard to use. This is really not something we want.

Additionally, any added preferences means implementing it, make the code execute conditionally for everyone, and maintain it (test it, handle bug reports, etc). What looks like a simple option can represent a lot of work.

So, the idea is to make a fair use of preferences, so if it covers too narrow of a case, it won’t be implemented.


### New weather sources

To be candidate for inclusion in the project, a weather source must not require private information such as credit card or phone number to have a free key.

To be accepted as a main source, a source must have hourly forecast. A source can be implemented as a secondary-only source if they don’t have hourly data but other secondary features.

Only features behind a free-tier will be accepted inside the project, so that any contributor can keep maintaining it in the long term.

Additionally, we usually don’t accept sources that are just frontends to other sources (for example, if they use AccuWeather data, we will just use AccuWeather directly).

Examples of weather sources that don’t fit:
- Apple WeatherKit (no free-tier)
- Microsoft Azure (free-tier requires credit card info)
- Weatherbit (free-tier only has “current” feature, with only 50 requests per day, so it’s not worth the maintenance cost)

Note that some national sources don’t have endpoints by coordinates, or reverse geocoding (find nearest city/station), so we can’t support them.


## Git setup for pull requests

### Init

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


### Submit

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


## Weather sources

### Create a new Weather source

Choose a unique identifier for your weather source, with only lowercase letters. Examples:
- AccuWeather becomes `accu`
- Open-Meteo becomes `openmeteo`

Copy:
```
app/src/main/java/org/breezyweather/sources/pirateweather/
```
to:
```
app/src/main/java/org/breezyweather/sources/<yoursourceid>/
```

We will use Pirate Weather as a base as it is the most “apply to most situations” source, without having too many specific code that most sources don’t need.
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

Rename `PirateWeatherService` with your source name and completes basic information.

As a starting point, we will only implement weather part, but here is the full list of interfaces/classes you can implement:

| Class/Interface               | Use case                                                                                                                                                                                                                                   |
|-------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `HttpSource()`                | Currently does nothing except requiring to provide a link to privacy policy, which will be mandatory to accept in the future                                                                                                               |
| `WeatherSource`               | Your source can provide weather data for a given lon/lat. If your source doesn’t accept lon/lat but cities-only, you will have to implement `LocationParametersSource`                                                                     |
| `LocationParametersSource`    | Your source needs location parameters, such as the code of a city. This code can be found by calling an endpoint with lon/lat, or a station list can be fetch to find the nearest station given the coordinates.                           |
| `LocationSearchSource`        | Your source is able to return a list of `Location` object from a query, containing at least the TimeZone of the location. If your source doesn’t include TimeZone, don’t implement it, and this will default to Open-Meteo location search |
| `ReverseGeocodingSource`      | Your source is able to return one `Location` (you can pick the first one if you have many) from lon/lat. If you don’t have this feature available, don’t implement it and locations created with your source will only have lon/lat        |
| `ConfigurableSource`          | You want to allow your user to change preferences, for example API key.                                                                                                                                                                    |

For most complex needs, always have a look at existing sources. If you need to add a new type of pollen for your source, please contact us first as it is a non-trivial change to the code.

In the `requestWeather()`, all properties of the `WeatherWrapper` are optional, so you can start implementing bit by bit, so you can easily test the first data.

Add your service in the constructor of the `SourceManager` class.

You’re done, you can try building the app and test that you have empty data.

**IMPORTANT**: please don’t try to “calculate” missing data. For example, if you have hourly air quality available in your source, but not daily air quality, don’t try to calculate the daily air quality from hourly data! The app already takes care of completing any missing data for you. And if you feel that something that could be completed is not, please open an issue and we will improve the app to do so for all sources.

**Additional note**: the Daily object expects two half days, which most sources don’t provide.
As explained in other documents, the daytime half-day is expected from 06:00 to 17:59 and the nighttime half-day is expected from 18:00 to 05:59 (or 29:59 to keep current day notation).
- If your source has half days with different hours, please follow their recommendations (for example, ColorfulClouds uses 08:00 to 19:59 and 20:00 to 07:59 (or 31:59)).
- If your source has no half day, a typical mistake you can make is to put the minimum temperature of the day as temperature of the night. However, your source probably gives you the minimum temperature from the past overnight, not from the night to come, so make sure to pick the correct data!

Once your source is complete (you use all available data from the API and available in Breezy Weather), please rebase and submit it as a pull request (see instructions above). Please allow Breezy Weather maintainers to make adjustments (but we won’t write the source for you, you will have to make significant implementation).
