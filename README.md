# Breezy Weather

Breezy Weather is a fork of [GeometricWeather](https://github.com/WangDaYeeeeee/GeometricWeather) distributed under LGPL-v3 license.

It aims to be a continuation of the original project, adding new features, providers, modernizing code, fixing bugs, updating dependencies for security reasons, etc.

Additions already available:
* New providers (Open-Meteo, Met Norway)
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
* New icon and new applicationId, clean up repo
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
* Add onboarding instead of asking for permissions straight away
* Add per location settings (for example, for Open-Meteo being able to choose the model (by default it’s “Best”))
* Add wind gust
* Add radar map
* Show the publish time in "Details" card (current) so that we know if details are fresh
* Organize settings better (with years, it has become kind of a mess)
* Add a search setting
* Reorganize translations so that we can have them on Weblate (move string-array to list of @string, tag notranslate strings)
* Use LeakPlumber to identify memory leaks
* New icon set?
* Maintain and improve accessibility (if you are concerned, please get in touch!)
* In About, add dependencies used by Breezy Weather and their licences
* In About, tag translators by language and only show translators for current language (to avoid having an infinite list!)
* Fix bottom, see also https://issuetracker.google.com/issues/36911528


### Download app
Currently on alpha, you can download debug builds (artifacts) from GitHub actions if you are logged in.

Very soon as pre-releases on GitHub, and on IzzyDroid if possible.

When moving to beta, it will be available on F-Droid if they agree.

Google Play releases will be available once it is stable enough.


### Contact me
* Matrix server: `#geometric-weather:matrix.org`
* GitHub issue

### How to run
Clone this project and build it with AndroidStudio.

### Build variants
You can select a specific build variants in AndroidStudio.
There are 3 build variants now. Specifically, the `fdroid` variant does not contain any closed source 3rd-party SDK, such as Baidu Location Service and Bugly. The `gplay` variant integrated the Google Play Service to improve accuracy of location. And finally, the `public` variant contains all closed source 3rd-party SDK which is not exist in `fdroid` version except the Google Play Service.

### Weather providers

**AccuWeather** is the most complete provider.

**Open-Meteo** is the only free and open source provider on this list, and probably also the most privacy-friendly. It is nearly as complete as **AccuWeather**, however lacks major features (reverse geocoding, alerts and realtime precipitations). Air Quality and Pollen are available and remains to be implemented with appropriate credits and acknowledgement.

For more details, see [weather providers](PROVIDERS.md).

### Weather icon extensions
If you want to build your own weather icon-pack, please read this document:
* [IconProvider-For-GeometricWeather](https://github.com/WangDaYeeeeee/IconProvider-For-GeometricWeather)

Also, you will find some icon-packs made by WangDaYeeeeee here:
* [IconPacks](https://github.com/WangDaYeeeeee/IconProvider-For-GeometricWeather/tree/master/apk)

By the way, Breezy Weather is compatible with Chronus Weather IconPacks. You can download them from Google Play or any other app store you have.

### Help me to improve the translation
You can submit a pull request, or wait for the project to be available on Weblate (soon).

### License
* [LICENSE](/LICENSE)
