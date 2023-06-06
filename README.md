# GeometricWeather

![Geometric Weather](/work/preview-header-android.png?raw=true)

### Download app
* [CoolAPK](http://www.coolapk.com/apk/wangdaye.com.geometricweather)
* [GooglePlay](https://play.google.com/store/apps/details?id=wangdaye.com.geometricweather)
* [F-Droid](https://f-droid.org/packages/wangdaye.com.geometricweather)

### Contact me
* By Email: wangdayeeeeee@gmail.com
* Or GitHub issue

### How to run
Clone this project and build it by AndroidStudio.

### Build variants
You can select a specific build variants in AndroidStudio.
There are 3 build variants now. Specifically, the `fdroid` variant dose not contain any closed source 3rd-party SDK, such as Baidu Location Service and Bugly. The `gplay` variant integrated the Google Play Service to improve accuracy of location. And finally, the `public` variant contains all closed source 3rd-party SDK which is not exist in `fdroid` version except the Google Play Service.

### Weather providers

See [weather providers](PROVIDERS.md).

### Weather icon extensions
If you want to build your own weather icon-pack, please read this document:
* [IconProvider-For-GeometricWeather](https://github.com/WangDaYeeeeee/IconProvider-For-GeometricWeather)

Also, you will find some icon-packs made by me here:
* [IconPacks](https://github.com/WangDaYeeeeee/IconProvider-For-GeometricWeather/tree/master/apk)

By the way, GeometricWeather is compatible with Chronus Weather IconPacks. You can download them from Google Play or any other app store you have.

### Help me to improve the translation
You can contact me by Email, or just submit a pull request.

### Contribute code
If you want to contribute code to help me to improve GeometricWeather, please make changes on the `dev` branch.

### License
* [LICENSE](/LICENSE)

### Also see
* [GeometricWeather For iOS](https://github.com/WangDaYeeeeee/GeometricWeather-iOS)

### What I gonna do next
* Complete the migration to Material You.
* More AppWidget with Material You style.
* Refactoring the polling process based on Kotlin coroutine.
* Modularize the entire project.
