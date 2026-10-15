# Contributions

## Rules for contributions from outside collaborators

### Before you start

Before working on anything, pick an issue or idea tagged “**Open to contributions**” and leave a comment saying you would like to work on it.

Then wait for approval from an [org member](https://github.com/orgs/breezy-weather/people) before you start.

**Why?** We welcome pull requests, but we want to make sure your work goes in the right direction. We don’t want you to implement something we don’t want (see [Rules for new features/improvements requests section](#rules-for-new-featuresimprovements-requests)), or to implement it in a way that doesn’t fit the project. The same applies to bug fixes: talking to us first helps make sure these guidelines are followed.


### Quality requirement

Once your contribution is approved, make sure that you:
1. **Understand your changes.** You must understand every line of code and documentation you submit, and be able to explain your approach during review.
2. **Check that it is correct, safe and appropriate**. Review your work carefully. Low-quality contributions lower the quality of the project and force maintainers and users to spend an unfair amount of time reviewing them.
3. **Respect the license**. Your contribution must be compatible with the project license **LGPL-v3**. Do not use tools whose terms forbid using their output in LGPLv3 projects or add restrictions on redistribution.


### Submitting your contribution

When your contribution is ready, check that:

1. There are no linting issues (see [Linting](#linting))
2. Your code is up-to-date with the current `main` branch (see [Rebasing](#rebasing-onto-main))
3. Your patch consists of a **single commit** (see [Squashing commits](#squashing-commits))
4. You have tested your changes.

You cannot open the pull request yourself: only org members can, which helps us make sure contributors follow these guidelines. Instead, **post a link to your branch in the issue or idea**, and a maintainer will create the pull for you. Don’t worry, you will still be credited as the author of the commit.


#### Linting

Check for linting issues with:

```
./gradlew spotlessCheck
```

Fix them automatically with:
```
./gradlew spotlessApply
```


#### Rebasing onto `main`

While you were working, new commits may have been added to `main`, so you will need to rebase your branch.

First, add our repository as a remote named `upstream` (you only need to do this once):
```
git remote add upstream https://github.com/breezy-weather/breezy-weather
```

Then fetch and rebase:
```
git fetch upstream
git rebase upstream/main
```

If there are conflicts, resolve them and continue the rebase (`git rebase --continue`).

Once done, push your branch. If you have already pushed it before, you will need the `--force` argument, because rebasing rewrites history:

```
git push --force
```

### Squashing commits

Your patch must contain a single commit, because it makes reviewing easier. If you made several commits, squash them into one.

Note: If your commits cover **different, unrelated changes**, don’t squash them all together. Split them into separates contributions instead, each with its own issue or idea and its own branch.

For example, if you made 2 commits:
```
git reset --soft HEAD~2
git commit
```

Then push again with `--force`.


## Rules for contributions for organization members

All organization members of Breezy Weather must follow the following rule:
- If someone has not followed the contribution guidelines and has published their code anyway, you are not allowed to copy that code. You may review the bug report to understand and analyze the issue, but you must then resolve it in your own way. For fixes that require only a few lines of code, it is possible that the two versions will be very similar, or even identical. Even if the contributor made the mistake of not following our warnings regarding the prohibition on contributing without our consent, they can still claim authorship of the code because it was written first, even if the code you wrote afterward is rightfully yours. In this case, you may decide to rewrite the code to avoid any misunderstanding. If this rewrite makes the code less optimized, it must be submitted to another member of the organization, who will impartially review the situation and the claims of both parties. This member of the organization will strive to reach a mutual agreement. If this proves impossible, they will decide either on a complete rewrite, a partial rewrite, or a complete commit revert. Since a rewrite may require writing code that is much less optimized to make it sufficiently different, it should be avoided, and a mutual agreement should be sought as a priority.


## Rules for new features/improvements requests

### General direction

Breezy Weather wants to be:
- a general weather app covering most of what you can expect from a weather app, but not *all* of what you can expect. For advanced usage, some specialized apps will always cover it better
- usable without having to be an expert to find anything in the app
- mainly target small displays, so we don’t want to fit too many things, as we also want to let the design breathe a bit


### New features

Probably, the most requested thing. “If you don’t want to make that feature for everyone, you can still make it a preference”.

Currently, we already have more than 50 preferences (not even counting widgets preferences and sources preferences!), which already provides a lot of customizability.

I know what you’re going to tell me “If there are already that many options, just ONE additional option won’t hurt”, but truth is if I had on top of existing preferences implemented every ONE preference people requested since this project began, I would have doubled the number of preferences (and I’m only writing this 1.5 months after the project began), and things people are mostly looking for would be hard to find in the myriad of options.

At the same time, with the existing preferences, some people can’t even find things, we already spend a lot of time helping people to find what they are looking for, and it shouldn’t be that way. Some people may even just drop the app because it's too hard to use. This is really not something we want.

Additionally, any added preferences means implementing it, make the code execute conditionally for everyone, and maintain it (test it, handle bug reports, etc). What looks like a simple option can represent a lot of work.

So, the idea is to make a fair use of preferences, so if it covers too narrow of a case, it won’t be implemented to avoid [feature creep](https://en.wikipedia.org/wiki/Feature_creep).

You can read [Niagara’s design principles](https://help.niagaralauncher.app/article/8-niagaras-design-principles) for a similar take on the matter (although due to the nature of this weather app, the “universal” criteria doesn’t always apply to us).

Usually, we will suggest your use our [content provider](https://github.com/breezy-weather/breezy-weather/discussions/2089) for the following cases:
- New widgets, or more generally updates to existing widgets, as we already know what we want to do in the next [widget overhaul](https://github.com/breezy-weather/breezy-weather/issues/937)
- New notifications, or configuration options for notifications (such as sending a notification based on some thresholds)
- A different design


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
