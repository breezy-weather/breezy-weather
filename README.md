# Breezy Weather

![Breezy Weather screenshot](fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot_1590799774.png?raw=true)

Breezy Weather is a fork of [GeometricWeather](https://github.com/WangDaYeeeeee/GeometricWeather) (LGPL-v3 license), currently no longer updated.

It aims to add new features, providers, modernizing code, fixing bugs, updating dependencies for security reasons, etc., while keep having a smooth user and developer experience in mind.

Additions already available:
* New providers (Open-Meteo, MET Norway)
* Additional data for other providers
* More Material 3 components
* Add hourly air quality
* Add Plume AQI scale for air quality widget
* Allow to disable background animation
* Documentation
* Translation updates thanks to contributors
* Tons of fixes
* Many non-visible improvements to the code

Future updates (in no particular order):
* Reorganize translations so that we can have them on Weblate (move string-array to list of @string, tag notranslate strings)
* Organize settings better (with years, it has become kind of a mess)
* Add onboarding instead of asking for permissions straight away + don’t add current position by default
* Add location from a map
* Complete air quality and pollen for Open-Meteo
* Add air quality for AccuWeather
* Make a nicer graph for hourly air quality/UV so that we can see more hours in the window
* Add humidity/dew point graph
* Fix Sun & Moon widget in RTL languages
* Round position to 2 digits for privacy reasons (1 km is more than enough)
* Add normals (what max temperature and min temperature look like on average for current month for the previous 30-year period)
* Add hourly per half day in the day details window
* Add some way to being alerted when a pollen reach a certain level
* More Material 3 migration
* Migrate as many things as possible to Kotlin and fix NPE
* Use coroutines
* Modularize geocoding providers and weather providers
* Modularize even more (for example, use Open-Meteo and get alerts from MF as Open-Meteo doesn't provide them)
* Add per location settings (for example, for Open-Meteo being able to choose the model (by default it’s “Best”))
* Add wind gust
* Add radar map
* Show the publish time in "Details" card (current) so that we know if details are fresh
* Add a search setting
* Fix memory leaks
* New icon set?
* Maintain and improve accessibility (if you are concerned, please get in touch!)
* In About, add dependencies used by Breezy Weather and their licences
* In About, tag translators by language and only show translators for current language (to avoid having an infinite list!)
* Fix bottom padding in search location, see also https://issuetracker.google.com/issues/36911528


### Download app

Currently on alpha, you can download debug builds (artifacts) from GitHub actions if you are logged in, or build it with Android Studio, or from command line with Gradle.

Very soon as pre-releases on GitHub, and on IzzyDroid if possible.

When moving to beta, it will be available on F-Droid if they agree.

Google Play releases will be available once it is stable enough and is compliant with Google Play policy.


### Help

* [Homepage explanations](docs/HOMEPAGE.md)
* [Weather providers comparison](docs/PROVIDERS.md)


### Contact us

* Matrix server: `#breezy-weather:matrix.org`
* GitHub discussions or issues


### Build variants

Differences between build variants:

| Variant                | fdroid | gplay | public |
|------------------------|--------|-------|--------|
| Instant App            | ❌      | ✅     | ✅      |
| Google Play Services   | ❌      | ❌     | ✅      |
| Baidu Location Service | ❌      | ❌     | ✅      |
| AMAP                   | ❌      | ❌     | ✅      |


### Weather icon extensions

If you want to build your own weather icon pack, please read this document:
* [Breezy Weather icon packs instructions](https://github.com/breezy-weather/breezy-weather-icon-packs)

You can find existing compatible icon packs made by WangDaYeeeeee here:
* [Geometric Weather icon packs](https://github.com/WangDaYeeeeee/IconProvider-For-GeometricWeather/tree/master/apk)

Breezy Weather is also compatible with Chronus Weather icon packs. You can download them from Google Play or any other app store you have.


### Translation improvements

You can submit a pull request, or wait for the project to be available on Weblate (soon).


### License

* [LGPL](/LICENSE)
