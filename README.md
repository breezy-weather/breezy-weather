# GeometricWeather

![Geometric Weather](https://github.com/WangDaYeeeeee/GeometricWeather/blob/master/work/preview_header.png)

From the original app's
[Play Store](https://play.google.com/store/apps/details?id=wangdaye.com.geometricweather)
description:

> Geometric Weather is a light and powerful weather app that provides you with
> real-time temperature, air quality, 15-days weather forecast, and accurate
> time-sharing trends.

The original repository is
[here](https://github.com/WangDaYeeeeee/GeometricWeather).

While a fantastic app overall, the original includes several closed-source
binary blobs related to Chinese weather and location APIs.

This fork removes all such proprietary binaries and related code to create a
true FOSS option. I intend to keep it updated with upstream changes per release.

### Notes

The original app included a handful of binaries in its
[`app/libs`](https://github.com/WangDaYeeeeee/GeometricWeather/tree/master/app/libs)
directory. `libindoor.so` and `liblocSDK7.so` appear to be related to Baidu's
location and weather APIs from a cursory Google search.

The entire `libs` directory has been removed. To simplify things, all
China-specific API options have been removed as well; this means that
AccuWeather is the only supported weather API at present, and the app will only
use your device's built in location service.

I was inspired to do this from [this issue thread](https://github.com/WangDaYeeeeee/GeometricWeather/issues/9) 
for an F-Droid version. Hopefully, this helps the cause along.

## Versions

I've elected to produce two versions of the app:

- version 1: fully open-source, with no proprietary dependencies whatsoever, suitable for F-Droid distribution.
- version 2: almost entirely open-source, with the only exception being a dependency on the non-free Google Mobile Services location API (from upstream).

I've elected to produce version 2 because **there is a tangible difference for the better in location refresh times when GMS location is active.** Given that benefit, I _personally_ see no reason to be alarmed at its inclusion, especially if, like me, you use many other apps that require GMS anyway. That said, I understand and appreciate F-Droid's dependency requirements, and will happily produce both versions.

Rebasing instructions for both versions can be found on the wiki (no guarantees about readability, though).
