## Weather update process

*Latest update of this document: 2024-07-14 (v5.2.6)*

1. If the location refreshed is current location, the first thing done is refresh the current longitude and latitude from your location source.
    - If it fails, it will fallback to latest known longitude and latitude, and continue to the next step.
    - If we don’t have latest known longitude and latitude (meaning there has never been any successful refresh), the refresh process for this location ends now.
    - If refreshing manually, an error will be displayed in both cases.
    - If refreshing in the background, an error will only be displayed if we don’t have any latest known longitude and latitude.
2. If the location refreshed is current location AND (longitude OR latitude OR main weather source changed), we try to get from the main weather source a reverse geocoding update (updated city/province/country info). If the main weather source doesn’t support reverse geocoding, we fallback to an offline reverse geocoding that only supports country reverse geocoding.

Info: Some weather sources need what we call "location parameters" before we can get weather. An example is that we have a longitude/latitude, but some weather sources only want a city identifier or grid forecast identifier on the weather endpoint. In such cases, we ask the weather source to provide us these information before we can proceed to actual weather request. These information are then saved, and usually no longer requested if it is a manually added location or only if longitude/latitude changed if current location.

3. If main weather data is no longer valid (see caching section below) and if needed, we update location parameters for main weather source.
4. If main weather data is no longer valid (see caching section below), we ask main weather source to provide us refreshed weather data for anything it supports, except any feature that was set up to use a secondary source. Otherwise, it fallbacks to previous saved data and move to next step.
5. If we have secondary weather sources set up, we group all features requested to the same source together to make a single request and we proceed for each of them the same way as main weather source: if any of the secondary data is no longer valid (see caching section below), refresh location parameters if needed and requested updated secondary weather data. Otherwise, fallback to previous saved data and move to next step.
6. We gather all data from all sources together and complete it with:
    - missing past data back to yesterday 00:00: this allows us to show you yesterday info when sources only support forecast and not past data
    - extrapolated missing data (such as wet bulb temperature, humidity from dew point OR dew point from humidity, weather codes from all known data, etc)
    - computed missing data (such as sunrise/sunset, moonrise/moonset and moon phase)

This process can happen in the background automatically or manually with slight behavior differences between the two:


## Background (or automatic) weather update

By default, background updates happens every 1.5 hours. You can disable or change this value in `Settings` > `Background updates`.

Android is in charge of executing that work more or less at your selected refresh rate preference. If, for some reason, it doesn’t work, you may try the troubleshooting options from `Settings` > `Background updates`.

At your selected refresh rate, this is what happens:
- The weather for the first location is refreshed
- The weather for secondary locations are:
  - not refreshed if you don’t use multi cities widget or notification-widget, or data sharing feature (they will, however, be automatically refreshed if outdated as soon as you try to access them in the app)
  - otherwise, refreshed once a day at most to avoid battery/bandwidth consumption in the background from less-used locations

When weather for the location(s) is done refreshing in the background, the following tasks are executed:
- If enabled, notifications are sent for each new severe alerts for the first location
    - You will NOT receive notifications of alerts with minor severity (minimal to no known threat to life or property), but you may still receive notifications for alerts of unknown severity
    - You will NOT receive notifications of alerts for locations other than your first location
    - You will NOT receive notifications of updated existing alerts, even if the description or severity changed, unless the weather source decided to remove the previous alert and create a new alert
    - You will NOT receive notifications of alerts if the alert source for your first location doesn’t support alerts for your country/location
    - You will NOT receive notifications of alerts if `Settings` > `Notifications` > `Notifications of weather alerts` is not enabled
- If enabled and if there are precipitation minute by minute for the first location, a notification of this precipitation is sent
    - You will NOT receive notifications of precipitation for locations other than your first location
    - You will NOT receive notifications of precipitation if the precipitation minute by minute source for your first location doesn’t support precipitation minute by minute for your country/location
    - You will NOT receive notifications of precipitation if `Settings` > `Notifications` > `Notifications of precipitation` is not enabled
- If used, widgets are updated with the latest weather data
- If used, the notification-widget is updated with the latest weather data
- If enabled, weather data of the first location is sent to Gadgetbridge
- Shortcuts (available on long press on icon on home screen) are updated
- If enabled and if it’s been more than 24 hours since last check, app updates are checked


## Manual weather update

By "manual" update, we talk about weather update happening on the main screen of the app.

This can happen when:
- You swipe to refresh
- From your location list, you access a location that has not been refreshed for more than your background update refresh rate (or if set to never, 1.5 hours)
- You change the sources used for your location
- [Open-Meteo] You change weather models used

When weather refresh for the location begins, the following task is executed:
- If enabled and if it’s been more than 24 hours since last check, app updates are checked

When weather for the location is done refreshing manually, the following tasks are executed:
- If used, widgets are updated with the latest weather data
- If used, the notification-widget is updated with the latest weather data
- If enabled, weather data of the first location is sent to Gadgetbridge
- Shortcuts (available on long press on icon on home screen) are updated


## Caching

Breezy Weather has another caching layer on top of existing caching mechanisms (HTTP headers).

Basically, depending on the features requested and your latest time of refresh, the same previous weather data may be restored.

Given different features have different caching delay, you may end up with a mix of restored weather data for some features, and newer data for other data.

| Feature         | Non-restricted sources                                                | Restricted sources¹                                                    |
|-----------------|-----------------------------------------------------------------------|------------------------------------------------------------------------|
| Main weather    | 5 min                                                                 | 15 min                                                                 |
| Current weather | 1 min                                                                 | 15 min                                                                 |
| Air quality     | 5 min                                                                 | 1 hour                                                                 |
| Allergen        | 5 min                                                                 | 1 hour                                                                 |
| Minutely        | 1 min if minutely list is not empty, 5 min otherwise                  | 5 min if minutely list is not empty, 15 min otherwise                  |
| Alerts          | 1 min if alert list is not empty, 5 min otherwise                     | 5 min if alert list is not empty, 1 hour otherwise                     |
| Normals         | Once a month (when it becomes obsolete), or 5 min if current location | Once a month (when it becomes obsolete), or 15 min if current location |

¹ Restricted sources are: OpenWeather (unless you use your own API key)
