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

<details><summary>Removed features</summary>

* CyanogenMod Weather SDK (no longer maintained, only supported Android 5.0 anyway)

Network Location Providers removed due to security concerns about outdated libraries (we need help on this! See [#49](https://github.com/breezy-weather/breezy-weather/issues/49) if you want to contribute):
* AMap Location SDK
* Baidu SDK (not Baidu IP Location)

</details>

<hr />

### Download app

Currently available to download from:
* [GitHub](https://github.com/breezy-weather/breezy-weather/releases)
* [Obtainium](https://github.com/ImranR98/Obtainium)
* IzzyDroid, *in progress*

Additional stores will be added as app gets more stable and once it complies with the policy of each store:
- Starting from **4.2.x**, on F-Droid.
- Starting from **5.x**, on Google Play Store. This version will introduce extension system for weather providers.

<hr />

### Help

* [Homepage explanations](docs/HOMEPAGE.md)
* [Weather providers comparison](docs/PROVIDERS.md)

<hr />

### Contact us

* Matrix server: `#breezy-weather:matrix.org`
* GitHub discussions or issues

<hr />


### Translations

Translation is done externally [on Weblate](https://hosted.weblate.org/projects/breezy-weather/breezy-weather-android/#information). Please read carefully project instructions if you want to help.

[![Translation progress report](https://camo.githubusercontent.com/c651422c22fc5743a6bf2003b86ed171e1852a8b90030c2e3bae322e32b9f778/68747470733a2f2f686f737465642e7765626c6174652e6f72672f776964676574732f627265657a792d776561746865722f2d2f627265657a792d776561746865722d616e64726f69642f686f72697a6f6e74616c2d6175746f2e737667)](https://hosted.weblate.org/projects/breezy-weather/breezy-weather-android/#information)

* English regional variants must be updated on GitHub if they differ from the original English file
* French translation is maintained by repo maintainers

<hr />

### Icon packs

Breezy Weather is compatible with Chronus Weather icon packs and [Geometric Weather icon packs](https://github.com/breezy-weather/breezy-weather-icon-packs/blob/main/README.md).

Currently, there are no Breezy Weather icon packs, but you can create one by [following instructions here](https://github.com/breezy-weather/breezy-weather-icon-packs/blob/main/INSTRUCTIONS.md)

<hr />

### Build variant

A variant called gplay is available and will be distributed on Google Play Store once ready.
It enables Instant App and bundles Google Network Location Provider (proprietary).

<hr />

### License

* [GNU Lesser General Public License v3.0](/LICENSE)
