# Breezy Weather

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/01.png?raw=true" alt="" style="width: 300px" />

Breezy Weather is a fork of [GeometricWeather](https://github.com/WangDaYeeeeee/GeometricWeather) (LGPL-v3 license), currently no longer updated.

It aims to add new features, providers, modernizing code, fixing bugs, updating dependencies for security reasons, etc., while keep having a smooth user and developer experience in mind.

<details><summary>Additions already available</summary>

* New providers (Open-Meteo, MET Norway)
* Additional data for other providers
* New header design for homepage
* New organization for settings
* More Material 3 components
* Add hourly air quality
* Add Plume AQI scale for air quality widget
* Allow to disable background animation
* Documentation
* Translation updates via Weblate
* Tons of fixes
* Many non-visible improvements to the code
</details>

<hr />

### Download app

Versions 4.0.x-alpha available from:
* [GitHub](https://github.com/breezy-weather/breezy-weather/releases)
* [Obtainium](https://github.com/ImranR98/Obtainium) (enable “Include preleases” when adding it)

Will be added to IzzyDroid soon, if possible.

Upcoming versions 4.1.x-beta will be available on F-Droid if they agree.

Upcoming versions 4.2.x will be released on Google Play Store once it is stable enough and is compliant with Google Play policy.

<hr />

### Help

* [Homepage explanations](docs/HOMEPAGE.md)
* [Weather providers comparison](docs/PROVIDERS.md)
* [Update translation on Weblate](https://hosted.weblate.org/projects/breezy-weather/breezy-weather-android/#information)
   * English regional variants must be updated on GitHub if they differ from the original English file
   * French translation is maintained by repo maintainers

<hr />

### Contact us

* Matrix server: `#breezy-weather:matrix.org`
* GitHub discussions or issues

<hr />

### Icon packs

Breezy Weather is compatible with Chronus Weather icon packs and [Geometric Weather icon packs](https://github.com/breezy-weather/breezy-weather-icon-packs/blob/main/README.md).

Currently, there are no Breezy Weather icon packs, but you can create one by [following instructions here](https://github.com/breezy-weather/breezy-weather-icon-packs/blob/main/INSTRUCTIONS.md)

<hr />

### Build variants

Differences between build variants:

| Variant                | fdroid | gplay | public |
|------------------------|--------|-------|--------|
| Instant App            | ❌      | ✅     | ✅      |
| Google Play Services   | ❌      | ❌     | ✅      |
| Baidu Location Service | ❌      | ❌     | ✅      |
| AMAP                   | ❌      | ❌     | ✅      |

<hr />

### License

* [GNU Lesser General Public License v3.0](/LICENSE)
