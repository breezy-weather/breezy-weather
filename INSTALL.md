*This page is still work-in-progress while we work on adding Breezy Weather to F-Droid default repository*

Breezy Weather team recommends you install the **standard** flavor of the Breezy Weather app from our own F-Droid repo for the best experience. [Install instructions](https://github.com/breezy-weather/fdroid-repo/blob/main/README.md)

Below you will find details about available flavors and installation methods.

Note that all releases are signed with the same signature, so you can easily switch between flavors or installation methods at any time.


## Flavors

**Breezy Weather** comes in 3 flavors:
- **Standard**: this is the recommended version of Breezy Weather. It is fully open source and contains no proprietary components to our knowledge.
- **Google Play**: this is the same version as Standard, except it also includes the proprietary Fused location component from Google Play Services. This component is useful if you use “Current location” feature AND have Google Play Services installed on your device. Otherwise, you should use the standard version.
- **F-Droid**: compared to the standard version, it removes support for any weather sources which are not libre and self-hostable. At the moment, only Open-Meteo and Bright Sky (DWD) match the criteria, but Pirate Weather roadmap indicates they will also become open source at some point.

| Feature                               | Standard | Google Play | F-Droid                                   |
|---------------------------------------|----------|-------------|-------------------------------------------|
| Technical name                        | `basic`  | `gplay`     | `fdroid`                                  |
| Fused location (Google Play Services) | ❌        | ✅           | ❌                                         |
| Weather alerts                        | ✅        | ✅           | Germany-only with Bright Sky (DWD) source |
| Temperature normals                   | ✅        | ✅           | ❌                                         |

| Worldwide weather sources | Standard         | Google Play      | F-Droid  |
|---------------------------|------------------|------------------|----------|
| Open-Meteo                | ✅                | ✅                | ✅        |
| AccuWeather               | ✅                | ✅                | ❌        |
| MET Norway                | ✅                | ✅                | ❌        |
| OpenWeather               | ✅                | ✅                | ❌        |
| Pirate Weather            | Requires API key | Requires API key | Planned¹ |
| HERE                      | Requires API key | Requires API key | ❌        |
| Météo-France              | ✅                | ✅                | ❌        |
| DMI                       | ✅                | ✅                | ❌        |

¹ As per Pirate Weather #1 priority in roadmap: https://docs.pirateweather.net/en/latest/roadmap/

| National weather sources            | Standard         | Google Play      | F-Droid  |
|-------------------------------------|------------------|------------------|----------|
| China                               | ✅                | ✅                | ❌        |
| National Weather Service (NWS)      | ✅                | ✅                | ❌        |
| Bright Sky (DWD)                    | ✅                | ✅                | ✅        |
| ECCC                                | ✅                | ✅                | ❌        |
| Israel Meteorological Service (IMS) | ✅                | ✅                | ❌        |
| SMHI                                | ✅                | ✅                | ❌        |
| MET Éireann                         | ✅                | ✅                | ❌        |


## Installation methods of Breezy Weather

**Breezy Weather** releases are available to install from:
- **Breezy Weather’s F-Droid repository** allows updates for Breezy Weather to be available faster than on the default F-Droid repository. This is the recommended way of installing Breezy Weather if you use a F-Droid-compatible app. To do this, you will need to [add our repo to your favorite F-Droid app](https://github.com/breezy-weather/fdroid-repo/blob/main/README.md).
- **Default F-Droid repository** offers the F-Droid flavor which is great for people who wants to be sure the app doesn’t by mistake connect to non-free network sources. However, there is usually an important delay in delivering our updates (about a week) which is why, for the F-Droid flavor, we recommend to use either Obtainium or GitHub releases methods instead.
- **Izzy’s F-Droid repository** is a popular F-Droid repo with a collection of various open source apps. You may want to get updates from this method if you already use this repo for other apps, otherwise it may be a bit overwhelming. [IzzyOnDroid F-Droid Repository website](https://apt.izzysoft.de/fdroid/)
- **Obtainium** is an app that allows you to install and update apps directly from their releases pages, and receive notifications when new releases are made available. [Link to Obtainium page](https://github.com/ImranR98/Obtainium/blob/main/README.md)
- **GitHub releases** is where releases are published and then pushed to other installation methods. If you have a GitHub account, you can subscribe to be notified of updates, however it’s more convenient to use an app which is compatible with F-Droid repositories to track updates.

| Differences              | Breezy Weather’s F-Droid repository | Default F-Droid repository | Izzy’s F-Droid repository | Obtainium | GitHub releases |
|--------------------------|-------------------------------------|----------------------------|---------------------------|-----------|-----------------|
| Pre-releases             | Optional                            | ❌                          | ❌                         | Optional  | Optional        |
| Delay for updates        | Immediate                           | Usually about a week       | Every day at 18:00 UTC    | Immediate | Immediate       |
| APK matches source code¹ | ✅                                   | ✅                          | ✅                         | ✅         | ✅               |

¹ APK are automatically generated by a [GitHub action](https://github.com/breezy-weather/breezy-weather/blob/main/.github/workflows/push.yml). For default F-Droid repository, there is a second check with the [reproducible build](https://f-droid.org/en/docs/Reproducible_Builds/) mechanism from F-Droid team.

| Available flavors | Breezy Weather’s F-Droid repository | Default F-Droid repository | Izzy’s F-Droid repository | Obtainium | GitHub releases |
|-------------------|-------------------------------------|----------------------------|---------------------------|-----------|-----------------|
| Standard          | ✅                                   | ❌                          | ✅                         | ✅         | ✅               |
| Google Play       | Planned                             | ❌                          | ❌                         | ✅         | ✅               |
| F-Droid           | Planned                             | ✅                          | ❌                         | ✅         | ✅               |

What about Google Play Store? It costs money, and we do not [comply with Google Play policy](https://github.com/breezy-weather/breezy-weather/issues/31), so there are no plans to add the app to Google Play Store at the moment.