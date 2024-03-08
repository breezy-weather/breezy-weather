# API keys

GitHub releases contain default API keys of the project that make all weather sources work by default (until API limits are reached).

If you want to self-build, you can add your own API keys in `local.properties` for sources to be configured by default in the build:
```properties
breezy.accu.portal=developer
breezy.accu.key=myapikey
breezy.accu.days=15
breezy.accu.hours=120
breezy.atmoaura.key=myapikey
breezy.baiduip.key=myapikey
breezy.geonames.key=myapikey
breezy.here.key=myapikey
breezy.mf.jwtKey=myapikey
breezy.mf.key=myapikey
breezy.openweather.key=myapikey
breezy.openweather.oneCallVersion=3.0
breezy.pirateweather.key=myapikey
```

You can omit any of the following properties to let the user configure their own API key in the settings, or only use API-key-less sources (such as Open-Meteo).


# Release management

*Instructions for members of the organization.*

1) Test your debug build.
2) Run tests `./gradlew testBasicReleaseUnitTest`.
3) Try to assemble a release `./gradlew assembleBasicRelease`.
4) Update versionCode and versionName in `app/build.gradle`.
5) Write changelog in `CHANGELOGS.md`.
6) Commit all changes.
7) Tag version beginning with a `v` (example: `git tag v5.1.0-beta -m "Version 5.1.0-beta"`).
8) Push with `git push --tags`
9) GitHub action will run and sign the release.
10) Update GitHub release notes draft and publish.
11) Update GitHub templates in `.github/` to show the new latest version.


# Decode crash logs from users

1) Save crash log in a text file `stacktrace.txt` and remove things like `2023-08-14 21:28:58.229 12804-12804 View org.breezyweather.debug` if they are present.
2) Download mapping-vX.X.X-beta.tar.gz from release page.
3) Unzip `mapping.txt`.
4) `~/Android/Sdk/tools/proguard/bin/retrace.sh mapping.txt stacktrace.txt`


# Translations

## Updated translations

When translations are updated from Weblate, if there are new contributors, add them in `app/src/main/java/org/breezyweather/settings/activities/AboutActivity.kt`.

## New language

If someone adds a new language in Weblate, there a couple of things to do in app.

Please check on Wikipedia the name of the language in the language, and copy/paste it for later. Then, go to Android settings to know the alphabetical order of the language to add it in the same order in Breezy Weather.

Then add it to `app/src/main/res/values/arrays.xml` in `languages` and a technical name in English in `language_values`.

Add the new enum and language technical name in `app/src/main/java/org/breezyweather/common/basic/models/options/appearance/Language.kt`. You will need to find a matching Locale in Android. For example, if the new language is `pt_rBR`, it will translate as `Locale("pt", "BR")`. The second parameter is optional, as you can see with other languages.


# Dependencies

## Update Gradle

Gradle must always be updated that way (replace with new version number):
```
./gradlew wrapper --gradle-version=8.6
```


____

# Weather sources API

Weather sources API can change: some versions may become deprecated, new endpoints may be added, new countries may be supported (when documented, we filter countries in app to avoid unnecessary calls on unsupported countries).

This section keep track of endpoints and when they were last checked.

## Open-Meteo

*Last checked: 2024-02-13*

| Endpoint            | Version  | Notes                                                                                                                                                                                                                              |
|---------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Geocoding — Search  | v1       | Partial support for Postal code, see [open-meteo/geocoding-api#8](https://github.com/open-meteo/geocoding-api/issues/8), missing admin codes, see [open-meteo/open-meteo#355](https://github.com/open-meteo/open-meteo/issues/355) |
| Weather — Forecast  | v1       |                                                                                                                                                                                                                                    |
| Air quality         | v1       |                                                                                                                                                                                                                                    |

Future additional endpoints/improvements for existing endpoints:
- Reverse geocoding, see [open-meteo/geocoding-api#6](https://github.com/open-meteo/geocoding-api/issues/6)
- Alerts, see [open-meteo/open-meteo#351](https://github.com/open-meteo/open-meteo/issues/351)
- Moon rise, set and phases, see [open-meteo/open-meteo#87](https://github.com/open-meteo/open-meteo/issues/87)
- Normals, see [open-meteo/open-meteo#361](https://github.com/open-meteo/open-meteo/issues/361)


## AccuWeather

*Last checked: 2024-02-13*

https://apidev.accuweather.com/developers/

| Endpoint               | Version | Notes                                                      |
|------------------------|---------|------------------------------------------------------------|
| Location — Translate   | v1      |                                                            |
| Location — Geoposition | v1      |                                                            |
| Current conditions     | v1      |                                                            |
| Daily                  | v1      | Up to 45 days, but useless                                 |
| Hourly                 | v1      | Up to 240 hours                                            |
| Minutely               | v1      | 1 minute precision                                         |
| Alerts by geoposition  | v1      |                                                            |
| Alerts by location     | v1      |                                                            |
| Air quality            | v2      | Up to 96 hours. TODO: Observational endpoint for SK and CN |
| Climo                  | v1      |                                                            |


## MET Norway

*Last checked: 2024-02-13*

| Endpoint          | Version | Notes                                                                                                     |
|-------------------|---------|-----------------------------------------------------------------------------------------------------------|
| Location forecast | 2.0     |                                                                                                           |
| Sunrise           | 3.0     | It is technically feasible to retrieve data for future days, but requires two calls for each, so we avoid |
| Nowcast           | 2.0     | Norway, Sweden, Finland and Denmark only                                                                  |
| Air quality       | 0.1     | Norway only                                                                                               |

Not yet implemented in app:

| Endpoint    | Version | Notes                                                                                                |
|-------------|---------|------------------------------------------------------------------------------------------------------|
| MET alerts  | 1.1     | Norway only by country code, there is a GeoJSON format but is heavily discouraged for production use |

No location search endpoint exists, it uses Open-Meteo instead.


## OpenWeather

*Last checked: 2024-02-13*

| Endpoint      | Version | Notes                 |
|---------------|---------|-----------------------|
| OneCall       | 3.0     | 2.5 is also supported |
| Air pollution | 2.5     |                       |

Not used:

| Endpoint    | Version | Notes                                 |
|-------------|---------|---------------------------------------|
| Geo         | 1.0     | Doesn’t have mandatory timezone field |
| Reverse geo | 1.0     | Doesn’t have mandatory timezone field |

Uses Open-Meteo for location search.


## Pirate Weather

*Last checked: 2024-02-13*

See: https://github.com/alexander0042/pirateweather/

| Endpoint    | Version |
|-------------|---------|
| Forecast    | v1.5.6  |

We should check regularly for additional fields we could use. Latest version checked is written above, everything more recent requires to check changelog.


## HERE

*Last checked: 2024-02-13*

| Endpoint            | Version    | Documentation                                                                                  |
|---------------------|------------|------------------------------------------------------------------------------------------------|
| Weather Destination | v3         | https://www.here.com/docs/bundle/here-destination-weather-api-v3-api-reference/page/index.html |
| Geocode             | v1 (7.116) | https://www.here.com/docs/bundle/geocoding-and-search-api-v7-api-reference/page/index.html     |
| Reverse geocode     | v1 (7.116) | https://www.here.com/docs/bundle/geocoding-and-search-api-v7-api-reference/page/index.html     |


## Météo-France

*Last checked: 2024-02-13*

| Endpoint    | Version |
|-------------|---------|
| Forecast    | v2      |
| Observation | v2      |
| Nowcast     | v3      |
| Ephemeris   | None    |
| Warning     | v3      |

Not used:

| Endpoint | Version | Notes                                                                                |
|----------|---------|--------------------------------------------------------------------------------------|
| Places   | V2      | Doesn’t have mandatory timezone field, miss many data on countries other than France |

Uses Open-Meteo for location search.


## Bright Sky

*Last checked: 2024-02-13*

| Endpoint        | Version |
|-----------------|---------|
| Weather         | v2.1    |
| Current weather | v2.1    |
| Alerts          | v2.1    |


## ECCC

*Last checked: 2024-01-26*

| Endpoint    | Version |
|-------------|---------|
| Location    | v2      |


## SMHI

*Last checked: 2024-01-26*

| Endpoint | Version |
|----------|---------|
| Weather  | 2       |


## DMI

*To be documented*


## MET Éireann

*To be documented*


## National Weather Service (NWS)

*To be documented*


## China

*Legacy source, undocumented*
