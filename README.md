# Breezy Weather

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/01.png?raw=true" alt="" style="width: 300px" />

Breezy Weather is a fork of [GeometricWeather](https://github.com/WangDaYeeeeee/GeometricWeather) (LGPL-v3 license), currently no longer updated.

It aims to add new features, providers, modernizing code, fixing bugs, updating dependencies for security reasons, etc., while keep having a smooth user and developer experience in mind.

Additions already available:
* New providers (Open-Meteo, MET Norway)
* Additional data for other providers
* New header design for homepage
* More Material 3 components
* Add hourly air quality
* Add Plume AQI scale for air quality widget
* Allow to disable background animation
* Documentation
* Translation updates thanks to contributors
* Tons of fixes
* Many non-visible improvements to the code


### Download app

Alpha releases available from:
* [GitHub](https://github.com/breezy-weather/breezy-weather/releases)
* [Obtainium](https://github.com/ImranR98/Obtainium)

Will be added to IzzyDroid soon, if possible.

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
