*This page is still work-in-progress while we work on adding Breezy Weather to F-Droid default repository*

Breezy Weather team recommends you install the **standard** flavor of the Breezy Weather app from our own F-Droid repo for the best experience. [Install instructions](https://github.com/breezy-weather/fdroid-repo/blob/main/README.md)

Below you will find details about available flavors and installation methods.

Note that all releases are signed with the same signature, so you can easily switch between flavors or installation methods at any time.


## Flavors

**Breezy Weather** comes in 3 flavors:
- **Standard**: this is the recommended version of Breezy Weather. It is fully open source and contains no proprietary components to our knowledge.
- **Google Play**: this is the same version as Standard, except it also includes the proprietary Fused location component from Google Play Services. This component is useful if you use “Current location” feature AND have Google Play Services installed on your device. Otherwise, you should use the standard version.
- **F-Droid**: compared to the standard version, it only includes weather sources which are libre and self-hostable: Open-Meteo, Bright Sky (DWD) and Recosanté. In the future, we expect Pirate Weather to become open source as well.

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

| Secondary weather sources | Standard         | Google Play      | F-Droid  |
|---------------------------|------------------|------------------|----------|
| ATMO AuRA                 | ✅                | ✅                | ❌        |
| Recosanté                 | ✅                | ✅                | ✅        |


## Installation methods of Breezy Weather

**Important note**: if you want to receive updates from F-Droid repos, and you use multiple repos, we recommend using a client that allows you to pin which repo you want to get updates from to avoid cross-updates of flavors. Such clients include Obtainium or official F-Droid client nightly build (no stable release yet).

**Breezy Weather** releases are available to install from:
- **Breezy Weather’s F-Droid repository** allows updates for Breezy Weather to be available faster than on the default F-Droid repository. All 3 flavors of Breezy Weather are available to choose from our repos. This is the recommended way of installing Breezy Weather if you use a F-Droid-compatible app. To do this, you will need to [add our repo to your favorite F-Droid app](https://github.com/breezy-weather/fdroid-repo/blob/main/README.md).
- **Izzy F-Droid repository** offers the Standard flavor which is our 2nd recommended choice, if you prefer to have an independent review of the app. Updates are fast (less than 24 hours).
- **Default F-Droid repository** *(not yet available)* offers the F-Droid flavor which is great for people who wants to be sure the app doesn’t by mistake connect to non-free network sources. However, there is usually an important delay in delivering our updates (about a week) which is why, for the F-Droid flavor, we recommend to use our F-Droid flavor repo to get updates faster.
- **Obtainium** is an app that allows you to install and update apps directly from their releases pages, and receive notifications when new releases are made available. You can also choose F-Droid repos as your source of update. [Link to Obtainium page](https://github.com/ImranR98/Obtainium/blob/main/README.md)
- **GitHub releases** is where releases are published and then pushed to other installation methods. If you have a GitHub account, you can subscribe to be notified of updates, however it’s more convenient to use an app which is compatible with F-Droid repositories to track updates.

| Differences              | [F-Droid repo] Breezy Weather | [F-Droid repo] Izzy    | [F-Droid repo] Default repo | Obtainium | GitHub releases |
|--------------------------|-------------------------------|------------------------|-----------------------------|-----------|-----------------|
| Pre-releases             | Optional                      | ❌                      | ❌                           | Optional  | Optional        |
| Delay for updates        | Immediate                     | Every day at 18:00 UTC | Usually about a week        | Immediate | Immediate       |
| APK matches source code¹ | ✅                             | ✅                      | ✅                           | ✅         | ✅               |
| Independently managed    | ❌                             | ✅                      | ✅                           | ✅²        | ❌               |

¹ APK are automatically generated by a [GitHub action](https://github.com/breezy-weather/breezy-weather/blob/main/.github/workflows/push.yml). For default F-Droid repository, there is a second check with the [reproducible build](https://f-droid.org/en/docs/Reproducible_Builds/) mechanism from F-Droid team.
² Only if you choose F-Droid default repo or Izzy repo as your source of update

| Available flavors | [F-Droid repo] Breezy Weather | [F-Droid repo] Izzy | [F-Droid repo] Default repo | Obtainium | GitHub releases |
|-------------------|-------------------------------|---------------------|-----------------------------|-----------|-----------------|
| Standard          | ✅                             | ✅                   | ❌                           | ✅         | ✅               |
| Google Play       | ✅                             | ❌                   | ❌                           | ✅         | ✅               |
| F-Droid           | ✅                             | ❌                   | ✅                           | ✅         | ✅               |

What about Google Play Store? It costs money, and we do not [comply with Google Play policy](https://github.com/breezy-weather/breezy-weather/issues/31), so there are no plans to add the app to Google Play Store at the moment.
