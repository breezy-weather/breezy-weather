# Contributions

## Rules for contributions from outside collaborators

### Summary

Before you start any contribution:
1. Make sure to choose an issue/idea tagged “Open to contributions”. Comment on that issue/idea that you want to work on it
2. If you plan on using AI, make sure to read carefully the AI guidelines before starting any code writing. Mention in the linked issue/idea the tools you want to use and their license terms for redistribution. If you don’t plan on using any, mention it as well.

Wait for an [org member](https://github.com/orgs/breezy-weather/people) approval before you start working on it. We can understand you want to start working right away, but if you don’t wait for approval, you may waste your time if it’s not approved.

When your contribution is ready:
3. You ensure there is no linting issue
4. Your code is up-to-date with current `main` branch
5. Your patch contains 1 single commit
6. You cannot create the pull request (only org members can, so we ensure contributors are following our guidelines). You link in the issue/idea to your branch and a maintainer will take care of creating the pull request for you (don’t worry, you will be credited as the author of the commit)


### The contribution was discussed beforehand

While we welcome pull requests, before implementing any new feature/improvement, we ask you to come talk to us, to be sure it goes in the right direction. We don’t want you to spend time implementing something we don’t want (see “Rules for new features/improvements requests” section below) or implementing it the wrong way. Same goes for bug reports, as we want to make sure the contribution guidelines are correctly followed.

That's why you can only contribute to [existing issues tagged “Open to contributions”](https://github.com/breezy-weather/breezy-weather/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22Open%20to%20contributions%22), or [existing ideas tagged “Open to contributions”](https://github.com/breezy-weather/breezy-weather/discussions?discussions_q=is%3Aopen+label%3A%22Open+to+contributions%22) *after* you’ve expressed interest in that discussion.


### AI guidelines

AI has numerous ethical concerns (such as copyright violations and huge use of energy and water), so we prefer you only use it if strictly necessary.

If you use AI or any kind of assistance tool, you use it as an assistant, meaning you're responsible for:
1. **Understanding** every line of code and documentation you submit, and be able to explain the approach during review.
2. **Ensuring it is correct, safe and appropriate**: these tools are good at generating plausible but meaningless content. So, you need to carefully review it, to avoid lowering the project code quality, or requiring unfair amount of human effort from developers and users to review contributions and detect the mistakes resulting from the use of AI.
3. Ensuring it can be **licensed under LGPLv3** (the project license): do not use tools whose terms forbid using their output in LGPLv3-licensed projects or impose additional restrictions on redistribution.
4. **Transparency**: disclose when AI was used. Which tools were used, and what they were used for.

If you can't follow them, don’t use AI.

Otherwise, before starting any contribution, mention in the linked issue/idea the tools used and their license terms for redistribution.


### Linting

You can check linting issues with `./gradlew spotlessCheck`.

You can apply `./gradlew spotlessApply` if necessary.


### Before submitting

Since you started working on your pull request, many commits might have been added, so you will need to rebase.

First, make sure you added our repo as `upstream` remote:
```
git remote add upstream https://github.com/breezy-weather/breezy-weather
```

Then:
```
git fetch upstream
git rebase upstream main
```

Fix conflicts if there are any.

Then, you can push (you may need the `--force` argument if you already pushed, as you are rewriting history).

Please test your changes.

If you made multiple commits, please stash them as it makes reviewing easier. Unless the commits are about different things. Then, you need to split them into multiple pull requests.

For example, if you made 2 commits, you can use:
```
git reset --soft HEAD~2
```

You can make a new commit, and once again, push your changes adding the `--force` argument.


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

So, the idea is to make a fair use of preferences, so if it covers too narrow of a case, it won’t be implemented.

You can read [Niagara’s design principles](https://help.niagaralauncher.app/article/8-niagaras-design-principles) for a similar take on the matter (although due to the nature of this weather app, the “universal” criteria doesn’t always apply to us).


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
