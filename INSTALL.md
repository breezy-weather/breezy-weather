# Simple instructions

Go to [Releases page](https://github.com/breezy-weather/breezy-weather/releases) and download the file with the following format `breezy-weather-vX.Y.Z_standard.apk`.

Install it and you’re done!


# Detailed instructions

## Flavors

**Breezy Weather** comes in 3 flavors that are always signed with the same signature, so you can easily switch at any time:
- **Standard**: this is the recommended version of Breezy Weather. It is fully open source and contains no proprietary components to our knowledge.
- **Google Play**: this is the same version as Standard, except it also includes the proprietary Fused location component from Google Play Services. This component can be useful if you use “Current location” feature AND have Google Play Services installed on your device. Otherwise, you should use the standard version.
- **F-Droid**: compared to the standard version, it only includes weather sources which are libre and self-hostable: Open-Meteo, Bright Sky (DWD) and Recosanté. In the future, we expect [Pirate Weather to become open source as well](https://docs.pirateweather.net/en/latest/roadmap/).

| Feature                                 | Standard | Google Play | F-Droid                                   |
|-----------------------------------------|----------|-------------|-------------------------------------------|
| Technical name                          | `basic`  | `gplay`     | `fdroid`                                  |
| Fused location (Google Play Services)   | ❌        | ✅           | ❌                                         |
| Weather alerts                          | ✅        | ✅           | Germany-only with Bright Sky (DWD) source |
| Temperature normals                     | ✅        | ✅           | ❌                                         |
| Non-self hostable/libre network sources | ✅        | ✅           | ❌                                         |


## Sources to get Breezy Weather from

**Breezy Weather** releases are available from the following sources:
- **[GitHub releases](https://github.com/breezy-weather/breezy-weather/releases)** is where all 3 flavors built by GitHub are published under APK format. Any Android device can install APK files without needing any particular app. If you have a GitHub account, you can subscribe to be notified of updates, however it’s more convenient to use a store app to track updates.
- **[Breezy Weather’s F-Droid repositories](https://github.com/breezy-weather/fdroid-repo/blob/main/README.md)** are maintained by Breezy Weather developers and allows you to choose from the 3 flavors from a F-Droid client that doesn’t support receiving updates from GitHub.
- **[Izzy F-Droid repository](https://apt.izzysoft.de/fdroid/index/info)** offers the Standard flavor which is our recommended choice if you would like someone to independently review the app before it gets published. Updates are fast (less than 24 hours).

| Differences             | GitHub releases | [F-Droid repo] Breezy Weather | [F-Droid repo] Izzy    |
|-------------------------|-----------------|-------------------------------|------------------------|
| Available flavors       | All             | All                           | Standard               |
| Pre-releases            | Optional        | Optional                      | ❌                      |
| Delay for updates       | Immediate       | Immediate                     | Every day at 18:00 UTC |
| APK matches source code | ✅               | ✅                             | ✅                      |
| Independently reviewed  | ❌               | ❌                             | ✅                      |

### Other not supported well-known sources

- Google Play Store:
  - Costs money
  - Is privacy invasive for the developer (requires sending your ID and giving your phone number)
  - We don’t [comply with Google Play policy](https://github.com/breezy-weather/breezy-weather/issues/31)
- F-Droid default repo: work is in progress!
- Accrescent: waiting for it to become stable (no ETA announced by upstream)


## Client configuration instructions

### Obtainium

[Link to Obtainium page](https://github.com/ImranR98/Obtainium/blob/main/README.md)

#### Getting updates from standard flavor of the Izzy repo

In the “Add App” screen, just add the following URL as App Source URL: https://apt.izzysoft.de/fdroid/index/apk/org.breezyweather

Tap the “Add” button at the very top, and you’re done!

#### Getting updates from GitHub releases

In the “Add App” screen:
1. Add the following URL: https://github.com/breezy-weather/breezy-weather
2. To receive updates for prereleases, enable “Include prereleases”
3. In the “Filter APKs by Regular Expression”, input the following depending on the flavor you want:
    - Standard: `standard`
    - F-Droid: `fdroid`
    - Google Play: `gplay`
4. Tap the “Add” button at the very top, and you’re done!


### F-Droid clients

Look for the Repositories option from your favorite client and add a new repository depending on the source you want to use:
   - Standard flavor from Izzy repo: https://apt.izzysoft.de/fdroid/repo
   - Standard flavor from Breezy Weather repo: https://breezy-weather.github.io/fdroid-repo/fdroid/repo
   - F-Droid flavor from Breezy Weather repo: https://breezy-weather.github.io/fdroid-repo/fdroid-version/fdroid/repo
   - Google Play flavor from Breezy Weather repo: https://breezy-weather.github.io/fdroid-repo/gplay-version/fdroid/repo

You should now be able to install Breezy Weather from your client.

**Important note:** If you add the F-Droid or Google Play flavor from our repos but you also have the Izzy repo installed, you should use a F-Droid client that supports setting which repo you want to get updates from, to avoid getting unexpected updates for the Standard flavor from the Izzy repo. This feature is called “preferred repo” in the official F-Droid client (v1.20.0 and later).
