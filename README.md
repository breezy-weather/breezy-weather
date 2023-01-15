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

### Features comparison of weather providers

AccuWeather is the most complete provider, however you may sometimes find other providers to be more accurate for your location.

| Providers | AccuWeather | MET Norway | OpenWeatherMap | Météo-France |
| --- | --- | --- | --- | --- |
| Status | Maintained | Alpha test | Not maintained anymore (version 3.0 requires credit card information) | Maintained |
| API key | Optional | None | Required (must be compatible with version 2.5) | Optional |
| Country supported | Worldwide, some features not available everywhere | Worldwide, some features restricted to Nordic area | Worldwide, some features not available everywhere | Mostly France, including DROM-COM. AQI restricted to Auvergne-Rhône-Alpes |
| Current | Weather, Temperature, Precipitation, Wind, UV, Air Quality, Humidity, Pressure, Visibility, Dew point, Cloud Cover, Ceiling | Weather, Temperature, Precipitation, Precipitation Probability (Thunder, Rain), Wind, UV (may be available in the future), Humidity, Pressure | Weather, Temperature, Precipitation, Wind, UV, Air Quality, Humidity, Pressure, Visibility, Dew point, Cloud Cover | Weather, Temperature, Wind, UV, Air Quality |
| Yesterday | Temperature | Not available | Not available | May be available in the future |
| Daily | Weather, Temperature, Precipitation (Rain, Snow, Ice), Precipitation Probability (Thunderstorm, Rain, Snow, Ice), Precipitation Duration (Rain, Snow, Ice), Wind, Cloud Clover, Sunrise/Sunset, Moonrise/Moonset, Moon phase, Air Quality, Pollen, UV, Hours of sun | Weather, Temperature, Precipitation (Rain), Precipitation Probability (Thunderstorm, Rain), Wind, UV (may be implemented in the future), Hours of sun | Weather, Temperature, Precipitation (Rain, Snow), Precipitation Probability, Wind, Cloud Clover, Sunrise/Sunset, Air Quality, UV | Weather, Temperature, Precipitation (Rain, Snow), Precipitation Probability (Rain, Snow, Ice), Wind, Cloud Clover, Sunrise/Sunset, Moonrise/Moonset (may be available in the future), Moon phase, Air Quality, UV, Hours of sun |
| Hourly | Weather, Temperature, Precipitation (Rain, Snow, Ice), Precipitation Probability (Thunderstorm, Rain, Snow, Ice), Wind, UV | Weather, Temperature, Precipitation, Precipitation Probability (Thunder, Rain), Wind, UV (may be implemented in the future) | Weather, Temperature, Precipitation (Rain, Snow), Precipitation Probability, Wind, UV | Weather, Temperature, Precipitation (Rain, Snow), Precipitation Probability (Rain, Snow, Ice), Wind |
| Realtime | Weather, Rain, Cloud Cover | May be available in the future for Norway, Sweden, Finland and Denmark only | Not available | Rain (intensity estimated, not available everywhere) |
| Alerts | Yes (duplicate alerts issue) | May be available in the future for Norway only | Yes | Yes (incorrect phenomen time to be fixed) |

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
