# Version 4.6.14-beta (2023-01-07)

**Weather sources**
- [AccuWeather] Fix snow quantity being 10 times lower
- [AccuWeather] Request texts in Imperial units if precipitation unit in app is configured as inch
- [Météo-France] Fix normals (missing in January, shifted by one month on other months)

**Other improvements and fixes**
- Fix incorrect degree day in Fahrenheit
- Show 2 decimals instead of 1 when precipitation unit is inch
- Fix air quality index being always shown as 250 when index > 250

**Translations**
- Initial translation added for Hindi (thanks Chandra Mohan Jha!)
- Translations updated


# Version 4.6.13-beta (2023-01-03)

**Other improvements and fixes**
- Compute wet bulb temperature from approximate formula when data is missing
- Daily and tomorrow notifications: allow to expand to see full text (thanks @danielzhang130!)
- [Baidu IP location] Fix not working with default API key
- [Baidu IP location] Show a more specific error when API limit is reached

**Translations**
- Translations updated

**Technical**
- Targets SDK 34 (Android 14)


# Version 4.6.12-beta (2023-12-05)

**Weather sources**
- [Open-Meteo] Extend Air quality forecast to 7 days where available

**Other improvements and fixes**
- By default, Breezy Weather will no longer refresh in background if your battery is low. It used to be opt-in. It can still be disabled in settings.
- Possible improvements to the weather update in background for some devices

**Translations**
- Bosanski added (thanks @SecularSteve!)
- Dansk added (thanks @peetabix!)
- Translations updated


# Version 4.6.11-beta (2023-11-08)

**Other improvements and fixes**
- Fix some weather texts not being updated after a forced refresh after a language change

**Translations**
- Translations updated


# Version 4.6.10-beta (2023-10-20)

Due to a technical change, any existing alerts will be cleared when updating to this version. They will be back on your next weather refresh.

**Weather sources**
- [PirateWeather] Order alerts by priority

**Other improvements and fixes**
- Technical changes to try to make notifications of alerts more reliable when they are duplicate (send a new notification if the priority changed, for example)
- Fix weekly widgets opening wrong days if yesterday is reported (@Cod3dDOT)
- Fix missing unit and conversion on Notification-widget native style

**Translations**
- Croatian added (thanks @Spajki001!)
- Translations updated


# Version 4.6.9-beta (2023-10-10)

**Weather sources**
- Fix fail to refresh for some sources when locale uses a different numbering system than (Western) Arabic numbers.

**Translations**
- Swedish added (thanks P.O!)
- Kabyle added (thanks ButterflyOfFire!)
- Translations updated


# Version 4.6.8-beta (2023-09-23)

**Weather sources**
- [Open-Meteo] Add support for new current, with better accuracy (15 min vs 1 hour when possible), and additional current variables (previously extrapolated from hourly forecast)

**Translations**
- Translations updated


# Version 4.6.7-beta (2023-09-19)

- Fix issues with trend widgets (@Cod3dDOT)
- Fix issues encountered when sharing crash logs
- Translations updated


# Version 4.6.6-beta (2023-09-16)

**Behavior changes**
- Temperature unit will now follow locale preference if it was never defined. Example: you were using the app with the temperature unit set as Celsius (the previous default) in English (US). It will now switch to Fahrenheit (but you can still of course redefine it to something else). This change is mostly to help first-time users having directly their temperature in the unit that makes the most sense to them.
- No longer allow to switch day/night temperature order in widgets to remove a confusion about which night it is about
- “Day / Night” option for widget backgrounds was replaced by “Follow app preference” to improve consistency (@Cod3dDOT)

**Other improvements and fixes**
- Add an algorithm for weather icon computing for sources without weather icon by half days (@Cod3dDOT). MET Norway and Open-Meteo will benefit the most from it.
- [Gadgetbridge] Send additional data for versions 0.76+ (@joserebelo)
- GPS and live wallpaper are no longer marked as required Android features to avoid some issues installing the app on some devices
- Translations updated


# Version 4.6.5-beta (2023-09-01)

**Weather sources**
- [Open-Meteo] Add experimental support for precipitation in the next hour as a primary source (works best in Europe)
- [Open-Meteo] Add experimental support for precipitation in the next hour as a secondary source (works best in Europe)
- [Open-Meteo] Show pressure at sea level instead of surface pressure to make it consistent with other sources
- [MET Norway] Add cloud cover (@Cod3dDOT)

**Other fixes**
- [Gadgetbridge] Fix probability of precipitation (@Cod3dDOT)
- [Gadgetbridge] Fix daily forecasts (will only start from tomorrow now)
- [Notification-widget] Fix temperature icon in status bar not displaying if unit is set to Kelvin. Temperature icon in status bar is no longer supported on Android 5.0 and 5.1 due to technical constraints.

**Translations**
- Dutch translation completed (thank you @BabyBenefactor!)
- Hungarian translation completed (thank you Viktor Blaskó!)
- Initial support for Basque (thank you @desertorea!)
- Translations updated


# Version 4.6.4-beta (2023-08-21)

**New features**
- Experimental support for Gadgetbridge added (@Cod3dDOT)

**Other fixes**
- Fix current location unable to refresh if choosing a main source that has no reverse geocoding support
- Fix wrong radius for sun animation (@Cod3dDOT)

**Translations**
- Translations updated


# Version 4.6.3-beta (2023-08-20)

**Other fixes**
- Fix wrong threshold lines name on daily and hourly trends widgets

**Translations**
- Serbian translation completed (thank you @nexiRS!)
- Translations updated


# Version 4.6.2-beta (2023-08-18)

**Weather sources**
- [AccuWeather] Fix would report no alerts if at least one alert didn’t contain a start or end time (can happen with flood alerts for example)

**Other fixes**
- Fix missing a day or two at the end of the daily forecast
- Fix crash when a location was not updated for more than "half the length of daily forecast" days


# Version 4.6.1-beta (2023-08-17)

**Weather sources**
- [Météo France] Alerts: add "Bulletin de Vigilance météo" support
- [Météo France] Alerts: add "Situation actuelle", "Prévisibilité et incertitudes" and other stuff like that to existing alerts

**Other fixes**
- Fix missing monochrome icon


# Version 4.6.0-beta (2023-08-16)

This new version introduces major changes in the way weather refreshes. While it brings nice new features with it, as there are now many different combination for weather sources that we can’t fully test, we welcome your bug reports about any regression introduced in this version, so we can fix them before new release becomes stable.

**New data**
- Yesterday is now shown as a day in daily forecast (on supported sources).
- Temperature normals added (with a fallback to average temperature when not supported by source).
- As a consequence “yesterday temperature lines” have been replaced by “temperature normals lines”.

**Weather refresh**
- If weather source doesn’t already provide it, app will now keep history back to yesterday 00:00. This will allow you to see details of the previous day, and will ensure a day is always full (daytime was notably missing on MET Norway source after 18:00). Works best 48 hours after a location has been added.
- When one or more secondary weather sources fail, app will refresh other sources that succeeded and restore cached data for failing sources. An error will still be displayed to inform you in case you need to take action.
- App will now tell you which source is failing.
- When changing a source, app will now automatically refresh data instead of expecting the user to force refresh manually.
- Secondary weather sources will no longer silently fail.
- Background update will now silently fail in the following cases to avoid unnecessary notification spam: network unavailable, server timeout. No weather update will happen.
- Background update will now silently fail in the following cases to avoid unnecessary notification spam: location permission missing (background location permission missing will still be reported), Android location not enabled, current location could not be found. Weather update will happen with previously found position. If current location was never found at least once, it will be reported as an error (will NOT silently fail).
- App will no longer show any message about data still being valid or refresh restrictions, it will automatically handle it, including merging still valid data with invalid data that just got refreshed (can happen when using multiple sources).

**Weather sources**
- [AccuWeather] Add support for temperature normals.
- [Météo-France] Add support for temperature normals.
- [Météo-France] Add support for temperature normals as a secondary source.

**Design**
- Logo got a refresh.
- Add a third place to the location list icon to not make it confusing with “itinerary” icon.
- A circled dot symbol is now shown next to location name on main screen if using current location.

**Other improvements and fixes**
- Fix incorrect alert source leading to crash in some cases.
- Required swipe distance to go to next location is now 50 % screen width (instead of 20 %) to make it less prone to errors.
- Current location will no longer wait 10 seconds to get a GPS fix if the info is available sooner
- Wind direction is fixed in hourly trends.
- Fix missing credits for secondary features when same as main source.
- Make alerts always take full width on alerts full page

**Translations**
- Initial support for Norwegian Bokmål added (thank you @Visnes!)
- Latvian translation is now complete (thank you Niks Rodžers!)
- Translations updated


# Version 4.5.4-beta (2023-08-10)

Some users reported issues with API limit reached, please check if you use OpenWeather or Pirate Weather as your main or secondary weather source!
Improvements to the error handling will be worked on for next major version.

**Weather sources**
- [PirateWeather] Bundled API key removed as monthly limit was reached on the 10th of the month (meaning it can’t be used again before Sep 1st) and we don’t want make false hopes to users by showing it in the list of available sources.

**Other fixes**
- Fix air quality data disappearing when allergen set as secondary source (but not air quality), and allergen data disappearing when air quality set as secondary source (but not allergen)
- Day / Night mode for locations has been moved to a new preference. It will also apply to some dialogs of the location now (others are still WIP).
- Change location list icon to use multiple place points instead of a bookmark (for "saved" locations)
- Fix clock icon tint on Android versions < 7.0

**Translations**
- Latvian translation added (thanks Eduards Lusts!), only app name translated for the moment
- Translations updated


# Version 4.5.3-beta (2023-08-08)

Emergency fix for current location not working on sources with reverse geocoding feature.


# Version 4.5.2-beta (2023-08-08)

**New features**
- Add Beaufort scale as an option for wind speed “unit”

**Weather sources**
- [HERE] Fix description of weather condition

**Other fixes**
- Fix live wallpaper being black after reboot
- Also apply "Yesterday line" option on widgets (@Cod3dDOT + @papjul)
- Fix cancelled weather sources changes being remembered (but not applied) on reopening preferences dialog
- Remove "Refresh" shortcut (feature no longer existed)
- Add abuse prevention mechanism

**Translations**
- Put AM/PM instead of :00 on hourly trends for devices configured to not use 24 hours system
- Translations updated


# Version 4.5.1-beta (2023-08-06)

**New features**
- Allow to change main source for a manually added location.

**Weather sources**
- [AccuWeather] Was not showing in secondary source list, fixed
- [MET Norway] Add weather text
- [OpenWeather] Add more days of air quality

**Other fixes**
- Fix wind direction on daily/hourly trends
- Fix some widgets crash when weather text was missing (detected on MET Norway)
- Fix wrong daytime/nighttime computing on some sources when location timezone is different than device timezone
- Add missing vertical scrolls for phones with small displays on location settings

**Translations**
- Some Open-Meteo weather texts were reworded to make it easier to make them common with other sources
- Dutch and Russian updated


# Version 4.5.0-beta (2023-08-04)

Due to a technical change, cached weather data will be cleared on update.

**New features**
- Support for secondary weather sources, allows you to complete missing data with other sources (air quality, allergens, precipitations by minute, alerts). Available from the “Edit” button in the footer of the main screen.
- IP.SB is now available as an alternative to GPS and Baidu IP Location. It’s based on IP so it provides less accurate results than GPS, but it’s fast and provides rather close results in our experience. Use it if you would like to use current location without giving your exact GPS position to weather source.
- Add a setting to allow changing the default location search source for weather sources which don’t have a location search feature.
- Add GeoNames as optional location search source. This source has fuzzy search support, but is rate-limited so only switch if you need it.
- Add privacy policy of app and sources in the About section (Info icon) of Settings
- Show licenses of our dependencies in the About section (@Cod3dDOT)

**Data**
- Initial implementation of wind gusts on compatible weather sources. Display only in daily details at the moment.

**Animations**
- When animations are disabled, it will now for some of them show static elements (clouds, sun, stars) (@Cod3dDOT)
- Reduce meteor spawning on clear night condition (@Cod3dDOT)
- Fix sensor stuttering (@Cod3dDOT)
- Fix Fog condition always showing Clear instead

**Weather sources**
- [HERE] Full support added (@Cod3dDOT)
- [Open-Meteo] Add air quality and allergens support as secondary source
- [AccuWeather] Add minutely and alerts support as secondary source
- [AccuWeather] Fix locations displayed in parenthesis (existing locations will need to be re-added)
- [AccuWeather] Fix missing location name when localized name was not available (will now fallback to English name)
- [AccuWeather] Fix allergens card being shown on countries where it is not supported
- [AccuWeather] Fix weather data refresh failure when using Developer portal
- [MET Norway] Add air quality and minutely support as secondary source
- [OpenWeather] Add minutely and alerts support as secondary source
- [OpenWeather] Fix precipitation probability (@Cod3dDOT)
- [Pirate Weather] Add minutely and alerts support as secondary source
- [Météo-France] Add minutely and alerts support as secondary source
- [ATMO AuRA] Is no longer provided as part of Météo-France. Needs to be added as a secondary source for compatible locations.

**Other fixes**
- Fix location no longer updated when tapped for the first time
- Fix contrast issue on Main screen Allergen card in some cases
- Fix "Material You - Current" widget not being resizable to 2 cells on height (@Cod3dDOT)
- Fix reverse geocoding would replace longitude and latitude of current location resulting in weather data for city coordinates instead of current coordinates
- Fix database keeping old "current location" weather data, making app data size grow
- Fix overwrite of weather data when "current location" and a manually added city shared the same city and weather source (issue particularly noticeable with AccuWeather)

**Translations**
- Slovak added (Kuko)
- Ukrainian added (@Cod3dDOT)
- Others updated


# Version 4.4.1-beta (2023-07-28)

- Add Allergens support for Open-Meteo source (Europe only at the moment). Allergens are different than AccuWeather ones (North America only).
- Uses universal scale for all allergens, regardless of weather sources
- New per-location settings dialog (will be improved in next versions)
- Allergens are now sorted by name
- Add support for Pirate Weather (thanks Cod3d.!)
- Fix crash on Android < 7.0
- Updated translations


# Version 4.4.0-beta (2023-07-25)

This version brings new logic that automatically completes missing data (for example, uses hourly data to extrapolate daily value for this data). This logic which was initially implemented per source is now transparently implemented for all sources. This means that even novice developers that want to add new sources will now be able to do it without having to care about that, making this task easier than ever.

If you notice any data that was available on v4.3.0-beta for a source and is now missing on v4.4.0-beta, please report it in a GitHub issue.

- When current details are missing from a source, app will now pick the closest hour forecast. Following this change, some sources will now have more current details.
- Sun & Moon & Moon phase is now available for all sources, it will be computed if data is missing from source.
- This fixes an issue with MET Norway where icons were always daytime on days 2+ due to missing sun info
- This also fixes many errors with midnight sun and polar night.
- Fix day and night temperature for OpenWeather which was completely broken and could show higher temperature at night than during the day.
- When dew point is missing and relative humidity and air temperature are available, it will be automatically computed.
- When degree day is missing, it will be calculated according to EU formula (check [Day details documentation](docs/DAY_DETAILS.md) for more info).
- When degree day is 0, it will no longer be shown.
- When temperature < 10 °C and wind speed > 4.8 km/h, and wind chill temperature is missing, it will be automatically computed.
- Fix top appbar/status bar visibility issues
- Make hourly trends less compressed.
- Add a “Help me choose” button on weather selection dialogs.
- Fix OpenWeather icons being always daytime.
- Fix alert list page was always scrolled to bottom
- When tapping on an alert (either from notification or from main screen), it will now jump to the top of this alert
- Fix cards no longer animated when entering screen
- Fix changing icon pack required a full restart of the app
- Revert swipe when trying to make the location list empty or cancelling a weather source update on current location.
- Remove clunky last daily forecast on Open-Meteo, MF, MET No, OpenWeather to avoid showing incomplete/incorrect data
- “Alerts to follow” message will no longer take you to past alerts, and alert list page will no longer show past alerts
- Fix contrast issue with next hour precipitation graph in light theme
- Fix search failure on Open-Meteo / GeoNames when country code was empty (Antarctica, for example)
- Updated translations.


# Version 4.3.0-beta (2023-07-20)

/!\ Custom API keys were reset in this version, following a move to a separate config store from the main app /!\

- [Regression fix] Sometimes, app would get stuck refreshing. It should no longer happen now.
- [Regression fix] If app fails to find current location, it will now refresh weather data for latest known position. In background, it will silently ignore error, while on main screen, it will show a snackbar to let user know that while it was refreshed, there was a problem finding current position.
- Add “start on boot” workaround for non-standard devices (such as MIUI) which didn’t have background workers resume after reboot
- Weather source for current location can now be chosen directly from the location list instead of going to settings, which was unintuitive and could be confusing.
- Add no network error on location search
- Throw error when trying to locate outside China when using Baidu IP location, instead of positioning on 0, 0
- Update resident feature to work with a 20 km radius instead of relying on 0.8 degrees.
- 中国 provider search now allows you to search for Chinese cities by its English name.
- [Technical] Many improvements to providers implementation for developers
- Added Kurdish Sorani translation (thanks anyone00!)
- Updated translations

# Version 4.2.1-beta (2023-07-16)

- Bring back detailed error (API key missing, API limit reached, no network, etc) on refresh failure instead of just “Weather failed”
- Added more errors (server timeout, failed to find location within reasonable time, parsing error, etc)
- Added detailed error for the location search as well
- Every setting change forced refreshing weather data, now it will only happen on language change or on current location provider change to avoid unnecessary refreshes
- Fix widgets were force refreshing weather data
- Fix issue when adding widgets
- Updated translations

# Version 4.2.0-beta (2023-07-15)

Background updates logic was entirely rewritten in this version.

- Refresh progress is now displayed in notification when weather update is happening
- If there are locations which failed to refresh in background, a notification will inform you and you will be able to access an error log for details (experimental).
- When having multiple locations, refreshes are done in parallel
- A preference was added to be able to ignore background refreshes when battery is low
- Today and tomorrow forecast notifications are now sent even if network is not available at requested time
- Fix alert notification logic being executed multiple times resulting in same alert being sent multiple times (this doesn’t fix duplicate alerts emitted as different alerts by AccuWeather).
- Fix app bar was not completely disappearing on main screen when scrolling fast
- Added Lithuanian translation (thanks Deividas Paukštė!)
- Updated translations


# Version 4.1.3-beta (2023-07-14)

- Add Nowcast for Nordic Area on MET No provider
- Add Air Quality for Norway on MET No provider
- Fix "Tap here to see alerts" not working
- Fix alert colors sometimes having low contrast
- Migrate to new sunrise API version for MET No, fix weather not being able to refresh when there were two moon rise during the same day
- Fix incorrect data being displayed in daily/hourly trends when data was unavailable (data from previous hours/days was being redrawn)
- Daily/Hourly trends will no longer show 0 when data is unavailable
- Fix lines being disconnected when weather icon is missing in daily/hourly trends
- Add "No direction" icon for wind on daily/hourly trends
- Keep moonrise/set and phase only for first day on Météo-France, instead of reusing data from first day on all days
- Fix precipitation talkback was always saying "No precipitation" in Daily precipitation trend
- Restore scroll behavior from Geometric Weather on main screen
- Fix display of refresh time on main screen on tablets
- Only display translators of the current language in settings
- Fix incorrect night temperature on OpenWeather and MF providers
- Fix Vietnamese language was not showing
- Added Bulgarian translation (thanks elgratea!)
- Updated translations

# Version 4.1.2-beta (2023-07-11)

- The following data can now handle decimals: temperatures (including all feels like temperature), dew point, UV index. Due to the technical nature of the change, existing cached data will be deleted on update and will be available again on next refresh.
- Alerts are now more visible and alert color is used when the weather provider supports it
- Add more info when tapping on air quality card or specific pollutant
- Cards now show on top right at what time they apply
- Make top bar small center aligned for location list, which fixes a stuttering issue at the same occasion
- Location search will now show advices if there are no search results instead of doing nothing
- No longer ask for notification permission (Android 13+) when adding location, show a dismissible info on location list instead
- Add additional settings from Android app info
- Fix Chronus icon packs not working
- Revert let Android 13 users revoke their permissions
- Translation updates

# Version 4.1.1-beta (2023-07-09)

- Location list is now empty on first install and app only requires location permissions if user wants to add its current location
- Add a dialog box for notification permission request
- Fix systematic crash on opening app on Android below R.
- Update Vietnamese translation.

# Version 4.1.0-beta (2023-07-08)

- Most of the code has been rewritten in Kotlin, which will make the app more reliable in the future.
- All notification channels are now declared on startup, so you can disable them before receiving at least one notification.
- Add precipitations in next hour for China provider.
- Fix current location not working for some providers.
- Fix crash in daily view when air quality is not available.
- Fix readability for Update method setting.
- Fix Beijing sometimes being added as location.
- Fix semi colon spacing in Hourly dialog.
- Fix Today sometimes not showing on the right column in notification-widget.
- Many null-safety checks added to avoid potential crashes, especially in widgets.
- Fix tap on a day on week widget didn’t open day detail if view style was 5 days.
- Fix don’t open location list when tapping on alerts.
- Fix first letter of weather text is not capitalized with OpenWeather provider.
- Fix wallpaper settings not opening in some cases.
- Fix tile not collapsing after being tapped on.
- Fix sensor was always queried regardless of gravity sensor preference.
- Fix weather data refresh when hourly temperature is partially available from API.
- Translation updates, Vietnamese added (thanks minb!).
- Support for weather data on cLock on CyanogenMod 5.0 was removed (unmaintained dependency, I’m not even sure it still worked).

# Version 4.0.5-alpha (2023-06-29)

- Add troubleshooting settings.
- Add more AccuWeather settings.
- Make widgets reconfigurable on recent Android versions.
- Update German, Italian and Portuguese (Brazil) translations.
- Fix location search results for AccuWeather (province was saved as city name, and city as district). Already existing locations must be re-added.
- Add small Celsius/Fahrenheit unit for temperature on main screen header.
- Fix back button in location list.
- Fix switch for Persistent notification widget
- Fix disabled settings could be switched on/off
- Fix could not re-add hourly air quality trend
- Last weather provider used for location search is now remembered.
- Don’t display Details/Live card if there is no data to display.
- Replace direction with arrow in wind short descriptions to help a bit in tight spaces.
- Various fixes and code improvements.

# Version 4.0.4-alpha (2023-06-26)

- Add current, hourly and daily air quality for Open-Meteo with credits.
- New settings organization and better wording. Some settings were reset to default.
- Fix daily trends and hourly trends configuration.
- Fix icon packs link.
- Fix some contrasts issues.

# Version 4.0.3-alpha (2023-06-25)

- Add current, hourly and daily air quality for AccuWeather.
- Add more detailed error messages.
- Fix icon color in light mode.
- Fix details not showing when all details were removed from header.

# Version 4.0.2-alpha (2023-06-25)

- Current details shown on header can now be configured in settings. Details not shown on header will display in the Details card.
- Fix refresh time not updating
- Update icons
- Make background immersive when app is loading
- Translation updates: Chinese (simplified), German

# Version 4.0.1-alpha (2023-06-24)

- Translation available from Weblate
- Make dividers thinner on main screen
- Fix vertical alignment of refresh time on main screen
- Fix monochrome icon
- Optimize new icons
- Require a restart when changing "Background animation" or "Gravity sensor" settings

# Version 4.0.0-alpha (2023-06-21)

Initial version of Breezy Weather fork

- New providers (Open-Meteo, MET Norway)
- Additional data for other providers
- New header design for main screen
- More Material 3 components
- Add hourly air quality
- Add Plume AQI scale for air quality widget
- Allow to disable background animation
- Documentation
- Translation updates thanks to contributors
- Tons of fixes
- Many non-visible improvements to the code
