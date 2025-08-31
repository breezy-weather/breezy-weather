# Version 5.4.8 (2025-06-22)

**Improvements and fixes**
- Add “Open in another app” in home screen action bar, to allow viewing the exact coordinates of the location in a maps application.
- Add back alternate calendar to Daily details screen
- Fix wrong valid time indication for the Air quality card
- Show an error message when trying to add twice the same location with the same forecast source

**Weather sources**
- [CWA] Removed trust in HiPKI by Chunghwa Telecom Certificate Authority, following upstream change of CA
- [Nominatim] Fix not showing up in the `freenet` flavor
- [NWS] Will now report an error when data from the observation station is outdated, and fallback to current hour forecast data, instead of displaying outdated “current” info
- [Pirate Weather] Now available in the `freenet` flavor (was open sourced a few months ago)
- [Pirate Weather] Fix incorrect units for precipitation

**Translations**
- Translations updated
- Translation added and completed for Thai (@wwwwwwari, @achn-syps)


# Version 5.4.7 (2025-04-28)

**Improvements and fixes**
- Fix for “Network unavailable” when using VPN on Android < 10
- Fix for word-break to new line when using a custom system font (Samsung or Motorola devices for example)

**Weather sources**
- [Atmo France] fix for no pollen in Paris, Marseille and Lyon
- [CWA] Fix failure to refresh due to server side changes on 2025-04-28
- [CWA] Fix failure to refresh on Android < 14
- [Open Meteo] Update weather model list (adds KMA, migrate deprecated models)

**Translations**
- Translations updated


# Version 5.4.6 (2025-04-19)

**Improvements and fixes**
- Background update will no longer execute when VPN is on, but no other Internet-providing transport is enabled
- Background update errors will now be grouped by location
- When changing an unit, widgets will now update automatically, instead of waiting for the next refresh to apply the new units
- Adjusted thresholds for pollen levels of alder, birch, grass, mugwort, olive and ragweed

**Weather sources**
- [Atmo France] added as new Pollen source for France (still early, may not work in all regions)

**Translations**
- Translations updated

**Technical**
- [Android] Improved current location request (@min7-i)


# Version 5.4.5 (2025-03-03)

**Improvements and fixes**
- Fix refreshing errors (and potential missing place name) in the following case: using current location + using Android as location source + using country-specific sources + position remained the same

**Translations**
- Translations updated


# Version 5.4.4 (2025-03-02)

**Improvements and fixes**
- Fix crash of the nowcasting chart in some cases resulting in app always crashing on opening

**Accessibility**
- Fix content description of the nowcasting chart

**Translations**
- Translations updated


# Version 5.4.3 (2025-02-28)

**New features**
- Address lookup source: allows to choose a source different from forecast source for address lookup of current location. Docs were updated to use the terms “address lookup” where it was previously called “reverse geocoding”

**Removed features**
- [Live Wallpaper] Removed animations to fix an abnormal memory/power consumption. If you have expertise with shaders, please check #1665 for a possible way to bring them back

**Improvements and fixes**
- Add support for devices with 16 KB page sizes
- The location list drawer will now always be closed by default if you only have 1 location (@DogacTanriverdi)
- [Alert screen] Allow to select the description of the alert to copy it

**Weather sources**
- [Android Geocoder] added as possible Address lookup source on devices supporting it
- [MET Norway] fix missing as possible current source in some cases
- [Nominatim] added as possible Address lookup source
- [SMHI] fix failing to refresh in some cases (manually added locations will need to be re-added)

**Accessibility**
- Main screen: details in header should now be read in the correct order by screen readers
- Main screen: fix, clicking on a day or an hour will now correctly open details on screen readers
- Main screen: improved content description of detail items, daily and hourly trend items
- Main screen: removed unused ability to tap on cards which was confusing for screen readers
- Main screen: add detailed content description of the nowcasting chart
- Main screen: make daily/hourly tabs appear as tabs in screen readers
- Main screen: added headlines to allow for quick jump between cards, on Android >= 9
- Daily details screen: improved content descriptions
- Alerts: improved content description (in Main screen and Detailed alerts screen)
- Temperature units will now be read as "degree celsius" instead of "degree C" on Android >= 7

**Translations**
- Translations updated


# Version 5.4.2 (2025-02-08)

**Improvements and fixes**
- Refresh errors are now detailed again (regression introduced in v5.3)
- Redesigned daily details screen to be more in line with the rest of the app
  - Added tabs to the daily details screen to switch more easily between days
- Redesigned cards in location list to be more in line with the rest of the app
  - Locations with currently active alerts will now be highlighted
  - Last update of the location will be displayed on outdated locations (based on configured refresh rate)
  - Removed less relevant information from the location card
- Added ability to customize the name of a manually added location (in Edit location > Location preferences)
- Improve user experience of settings requiring the notification permission (@min7-i)
- Fix main screen displaying an incorrect current temperature, when the location has never been refreshed and forecast source failed to refresh

**Weather sources**
- [Open-Meteo] Fix snow quantity reported 10 times too low
- [AccuWeather] Fix missing pollen when not using AccuWeather as forecast source
- [AccuWeather] Fix missing forecast when using Developer portal (custom API keys)
- [CWA] Fix daily bulletin not being displayed in some cases (@chunshek)
- [HKO] Ignore non-sense values sent by the server (@chunshek)

**Translations**
- Translations updated
- Fix 12-hour formatting in Korean (only on Android >= 7)


# Version 5.4.1 (2024-01-03)

**Improvements and fixes**
- Fix crash of Weather source settings screen

**Weather sources**
- [ATMO GrandEst] Fix missing default key in release builds
- [Atmo Hauts-de-France] Fix missing default key in release builds
- [AtmoSud] Fix missing default key in release builds

**Translations**
- Translations updated


# Version 5.4.0 (2024-12-28)

**Improvements and fixes**
- Greatly improve weather update time, especially when using different sources on a single location (see technical section for details)
- Sources not configured will now be shown as disabled in the source list so that you know they exist
- When adding current location, you will no longer be able to select a source only compatible with some parts of the world. You will need to have your position found with a worldwide source at least once first.
- App will now report more strictly errors if you are using a non-compatible source (please report any false positive)
- It's now possible to always being able to select “None” as an option for air quality, pollen, nowcasting, alerts, normals sources.
- Fix normals not working in some cases
- “Location access is off” error introduced in v5.3 will no longer be reported when running in background

**Weather sources**
- [AccuWeather] When alerts by coordinates is unavailable, it will now fallback to alerts for your city instead of failing completely
- [ATMO GrandEst] Added as a new source for air quality in Grand Est (France)
- [Atmo Hauts-de-France] Added as a new source for air quality in Hauts-de-France (France)
- [AtmoSud] Added as a new source for air quality in Provence-Alpes-Côte d’Azur (France)
- [EKUK] Added as a new source for air quality in Estonia (@chunshek). Pollen will come later this year
- [EPD] Added as a new source for air quality in Hong Kong (@chunshek)
- [Ilmateenistus] Added as a new source for forecast in Estonia (@chunshek)
- [Météo-France] Add support for alerts in oversea territories
- [Météo-France] Fix alert error when refreshing at midnight
- [Météo-France] Deduplicated bulletin when the bulletin is the same for today and tomorrow
- [Météo-France] Add back support for normals in France, Monaco and Andorra

**Translations**
- Translations updated
- Fixed duplicate Indonesian and Hebrew languages in the standard flavor

**Technical**
- Major refactoring of sources to merge main and secondary sources logic
- Improved weather update process time by optimizing date parsing functions
- Improved weather update process time by running all sources of the location in parallel
- Remove refreshing locations in parallel in background in favor of previous point
- Optimized filesize of the Natural Earth offline geocoding


# Version 5.3.1 (2024-12-12)

**Improvements and fixes**
- Fix being notified of an older stable version when running a pre-release version
- Fix daily urticaceae value

**Weather sources**
- [ATMO AURA] Fix source no longer showing up in the region it is supported
- [BrightSky] Fix not working when using it as a secondary current source
- [CWA] Emergency fix for unscheduled server side change (@chunshek)

**Translations**
- Translation added for Irish (Aindriú Mac Giolla Eoin)
- Translation completed for Tamil (தமிழ் நேரம்)
- Translations updated


# Version 5.3.0 (2024-12-07)

**New features**
- Current weather source: It is now possible to select a “current weather” source different from the main (forecast) source
- Added support for more administration levels (province, regions, county, departments, etc) on locations. Existing manually added locations will need to be re-added
- Added night sky background for splash screen when using the app in dark mode (@min7-i)

**Improvements and fixes**
- Sources are now split by continents in the source dialog to make it easier to find a specific national source
- Sources now contains the country they are from in their name when relevant (@chunshek)
- When a source failed to refresh a specific feature, Breezy Weather will now attempt to continue refreshing other features while restoring the old data for failed features, when possible. A non-blocking error message mentioning the failed feature(s) will be visible in that case.
- Fix no error message when location permission was previously denied (@min7-i)
- New refresh error message: “Location access is off” when permission is granted but system disabled (@min7-i)
- Main screen is now able to show more than one error at a time (instead of only displaying the first one) (@min7-i)
- Allow to reset sources instance URL config by clearing it
- Nowcasting chart: honor disabled “Threshold lines on charts” preference
- Show minutes on the hourly chart on 24-hour system, so that users from a “not rounded hour” timezone such as India can see the correct minute
- Fix dialogs theme on main screen (@min7-i)
- Fix location preferences dialog being cut on small screens (@min7-i)
- Fix snackbar no longer appearing after changing some preferences in some cases (@min7-i)
- Fix snackbar appearing above some items (@min7-i)
- Fix freeze after applying some settings in some cases (@min7-i)
- Fix dark mode theme not immediately applied in the location list (@min7-i)
- Various fixes of alignment on tablets and/or landscape mode (@min7-i)
- Fix margin issues on today/tomorrow notifications on old Android versions (@min7-i)
- Fix dark mode not immediately changed on Edit location screen in case Dark mode setting is changed (@Mushfiq1060)
- Fix duplicate drawer + location list icons in portrait mode on tablets in some cases (@ecawthorne)
- Fix hourly pressure tab disappearing when pressure for the whole period is either all below or all above normal pressure threshold
- Fix hourly tabs disappearing sometimes (@ccyybn)

**Weather sources**
- **Worldwide**
  - [Open-Meteo] Add KNMI, DMI and UK Met Office as options for specific weather model to use
  - [WMO SWIC] Add support for more alert details (full description, instructions, translations) for most countries. Work remains to be done in some countries such as India or Argentina.
- **Africa**
  - [ANAM-BF] Added as a secondary alert source option for Burkina Faso. Also available in freenet flavor (@chunshek)
  - [ANAMET] Added as a secondary alert source option for Togo. Also available in freenet flavor (@chunshek)
  - [DCCMS] Added as a secondary alert/normals source option for Malawi. Also available in freenet flavor (@chunshek)
  - [DMN] Added as a secondary alert/normals source option for Niger. Also available in freenet flavor (@chunshek)
  - [DWR] Added as a secondary alert source option for Gambia. Also available in freenet flavor (@chunshek)
  - [EMI] Added as a secondary alert/normals source option for Ethiopia. Also available in freenet flavor (@chunshek)
  - [GMet] Added as a secondary alert source option for Ghana. Also available in freenet flavor (@chunshek)
  - [IGEBU] Added as a secondary alert source option for Burundi. Also available in freenet flavor (@chunshek)
  - [INM-GB] Added as a secondary alert source option for Guinea Bissau. Also available in freenet flavor (@chunshek)
  - [Mali-Météo] Added as a secondary alert source option for Mali. Also available in freenet flavor (@chunshek)
  - [Météo Bénin] Added as a secondary alert/normals source option for Bénin. Also available in freenet flavor (@chunshek)
  - [Météo Tchad] Added as a secondary alert/normals source option for Chad. Also available in freenet flavor (@chunshek)
  - [Mettelsat RDC] Added as a secondary alert source option for DR Congo. Also available in freenet flavor (@chunshek)
  - [MSD] Added as a secondary alert source option for Zimbabwe. Also available in freenet flavor (@chunshek)
  - [SMA Seychelles] Added as a secondary alert/normals source option for Seychelles. Also available in freenet flavor (@chunshek)
  - [SMA Sudan] Added as a secondary alert source option for Sudan. Also available in freenet flavor (@chunshek)
  - [SSMS] Added as a secondary alert source option for South-Sudan. Also available in freenet flavor (@chunshek)
- **Asia**
  - [BMD] Added as a main source option for Bangladesh (@chunshek)
  - [BMKG] Added as a main and secondary current/alert/air quality source option for Indonesia (@chunshek)
  - [CWA] Ensure the source will still work after CWA planned changes for after 2024-12-10 (@chunshek)
  - [CWA] Optimized refresh time (@chunshek)
  - [CWA] Add support for daily bulletin (@chunshek)
  - [CWA] Fix wind speed when >= 11 m/s (@chunshek)
  - [Hong Kong Observatory] Added as a main and secondary current/alert/normals source option for Hong Kong (@chunshek)
  - [IMD] Added as a main source option for India (@chunshek)
  - [JMA] Added as a main and secondary current/alert/normals source option for Japan (@chunshek)
  - [MGM] Added as a main and secondary current/alert/normals source option for Türkiye (@chunshek)
  - [NAMEM] Added as a main and secondary current/normals/air quality source option for Mongolia. (@chunshek)
  - [PAGASA] Added as a main source option for Philippines (@chunshek)
  - [SMG] Added as a main and secondary current/alert/air quality/normals source option for Macau. (@chunshek)
- **Europe**
  - [AEMET] Added as a main and secondary current/normals source option for Spain (@chunshek)
  - [IMS] Added as possible current source + add weather texts support + various improvements (@chunshek)
  - [IPMA] Added as a main and secondary alert source option for Portugal (@chunshek)
  - [LHMT] Added as a main and secondary current/alert source option for Lithuania (@chunshek)
  - [LVĢMC] Added as a main and secondary current/alert source option for Latvia (@chunshek)
  - [MET Norway] Fix alerts not working when used as a secondary source
  - [Met Office] Added as a main source option for United Kingdom. Requires an API key. (@bunburya)
  - [MeteoLux] Added as a main and secondary current/alert source option for Luxembourg. (@chunshek)
  - [Météo-France] Add support for next-day alerts
- **North America**
  - [NWS] Added as possible current source for United States (@chunshek)
  - [NWS] Various improvements in daily data, weather texts and alerts (@chunshek)
- Miscellaneous
  - Add translation for a few source names and privacy policies
  - Location presets updated for some countries following the addition of many weather sources (@chunshek)

**Translations**
- Translations updated

**Technical**
- Update to targetSdk 35
- Refactoring + new features in sources


# Version 5.2.8 (2024-09-15)

**Improvements and fixes**
- Show precipitation probability when > 0 % (previously > 5 %)
- [Tablet] If the location list is not empty, it will no longer open by default in portrait mode on tablets with large resolution

**Sources**
- [MET Norway] Add support for alerts in Norway-only as main or secondary source (API is in beta, so might not be perfectly reliable)
- [Open-Meteo] Fix precipitation nowcasting values being 4 times lower than expected
- [ECCC] Fix failure to refresh when wind direction received from observation station is empty
- [Gadgetbridge] Add missing fields (wind, UV, precipitation probability) (@joserebelo)

**Translations**
- Translations updated


# Version 5.2.7 (2024-08-08)

**Improvements and fixes**
- Change of behavior: on Android 10+, by default, day/night mode for locations is no longer enabled.
- Add temperature normals in the daily detailed view
- Fix trailing whitespaces returning inconsistent search results (@mags0ft)
- Fix location permission dialog being shown twice when resuming app (@ecawthorne)
- Fix prompt for background location permission not being shown
- Cards fade in and other elements preferences can’t work if animations are disabled at Android system level, greying out these options in this case (@ecawthorne)
- Add the app to the weather category. Might be useful for assistants or launchers. (@devycarol)

**Sources**
- [Servizio Meteo AM] Fix some failures to refresh (@chunshek)
- [Open-Meteo] Fix precipitation nowcasting when used as a secondary source (@min7-i)
- [China] Fix very small values in the nowcasting chart
- [Baidu IP location] Show “API unauthorized” error message when API key is invalid, instead of “Failed to find current location”

**Translations**
- Initial translation added for Occitan (mercé Quentin!)
- Translations updated


# Version 5.2.6 (2024-07-17)

**New features**
- [Standard version] Add an option to periodically check for app updates in the background. Available from `Settings` > `Background updates`. Disabled by default.
- Allow to manually check for updates from `Settings` > `About` (icon). In the `freenet` flavor where non-free networks are not allowed, the button only asks to open the link in an external browser.

**Improvements and fixes**
- Fix title not changing when going in a subcategory of settings (@min7-i)
- Fix crash in old Android versions when trying to set location permissions that don’t exist (@min7-i)

**Sources**
- [Servizio Meteo AM] Added as a possible primary source (@chunshek)
- [AccuWeather, IMS] Fix text not being in Hebrew when selected language is Hebrew
- Updated recommendations for new locations (existing locations must be modified manually, or re-added):

| Location | Source type | Old        | New               |
|----------|-------------|------------|-------------------|
| Italy    | Main source | Open-Meteo | Servizio Meteo AM |


# Version 5.2.5 (2024-06-27)

**Improvements and fixes**
- Add apparent temperature computing when missing (@chunshek). Will generally improve feels like temperature, as it avoids fallback to the -less useful- wet bulb temperature.
- Improve wind chill computing (@chunshek).
- Fix wrong category for UV when between two categories in some cases
- Add one more decimal precision to the following pressure units: inHg, atm, kgf/cm²
- Fix a day shift on some sources when adding a location from a different timezone that is already “tomorrow”
- Fix hourly feels like not working in some cases
- Fix “Edit location” dialog theme (@min7-i)
- Fix day/night theme not respecting sunset/sunrise in alerts and pollen pages (@min7-i)
- Recompute sun and moon data when sources return data from the wrong day (fixes some places being always “nighttime”)

**Sources**
- [CWA] Added as available experimental main source in Taiwan (@chunshek)
- [CWA] Added as available experimental secondary source in Taiwan for air quality, alerts and normals (@chunshek)
- [NWS] Fix parsing issues when probability of thunder contains decimals
- [Météo-France] Fix not showing as a suggested normals secondary source when not using Météo-France as main source
- [ECCC] Fix refreshing issues in locations observing midnight sun
- [China] Fix hourly forecast being shifted by one hour (@JiunnTarn)
- [Open-Meteo] Disable ability to select more than one model at a time which would result in a failure to refresh

**Translations**
- Translations updated
- Hebrew translation added and completed (@nvurgaft, Doge)
- Initial translation for Galician added (@adrianhermida)


# Version 5.2.4 (2024-06-01)

**Improvements and fixes**
- Reorganized location list actions
- Fix back button not correctly intercepted in widget config on Android >= 13 (@min7-i)
- Fix notification-related settings not propagated immediately on Android >= 13 (@min7-i)
- Fix today/tomorrow notification being silent on some devices (@min7-i)
- Reduce lag on refresh on low-end devices/slow sources (@jayyuz)
- Fix another crash case when sending data to Gadgetbridge (@kosmoz)

**Translations**
- Translations updated
- Dansk translation completed (thanks Rasmus!)


# Version 5.2.3 (2024-05-05)

**Improvements and fixes**
- Fix nowcasting values on sources with values 10-minute by 10-minute or more
- [Alert page] Status bar black text on dark background when theme is light with day/night mode enabled (@suyashgupta25)
- Adjust contrast of some caption texts in dark mode (@suyashgupta25)

**Sources**
- [Open-Meteo] Allow to select “GFS GraphCast” model individually
- [AccuWeather] Prevent user from selecting incompatible preferences

**Translations**
- Translations updated
- Initial translation added for Bengali (thanks Manab Ray!)


# Version 5.2.2 (2024-04-24)

**Improvements and fixes**
- Cap refresh rate of live wallpaper to 60 Hz, to avoid consuming too much battery

**Sources**
- [GeoSphere Austria] Fix precipitation values
- [Pirate Weather] Fix parsing error due to server changes

**Translations**
- Translations updated
- Tamil added (thanks Naveen!)


# Version 5.2.1 (2024-04-20)

**Improvements and fixes**
- We no longer send notifications for alerts with minimal to no known threat to life or property, to avoid spamming in some countries where these kind of alerts are sent daily. Alerts with unknown severity may still be sent.
- Moved “Edit location” button from footer to a pencil icon on top right
- Fix sun or moon not showing in ephemeris card with some sources when the sun/moon is setting the day after
- Fix crash when sending data to Gadgetbridge in some cases
- Fix moving to next location when swiping on the nowcasting chart (@min7-i)

**Translations**
- Translations updated
- Fix formatting of Chinese calendar day of the month (@CoelacanthusHex)


# Version 5.2.0 (2024-04-15)

**IMPORTANT changes**
- `gplay` flavor was removed, Fused location is now available natively in all flavors for Android >= 12 with no proprietary library. Migration path: use the `standard` flavor
- `fdroid` flavor was renamed `freenet` to avoid confusion with the store. Obtainium users need to take action to change their regular expression to catch the new name
- OpenWeatherMap OneCall API will no longer be available without requiring billing information in June 2024. As a consequence, we switched to other endpoints that unfortunately don’t support alerts and minutely. You will have to use a different secondary source if you want to keep receiving these info. If you use a custom API key, you might want to make sure your API key is subscribed to the following products: Current and 3-hourly.
- If you were using “Send data to Gadgetbridge” feature, you will need to go back to `Settings` > `Widgets` to define which apps you want to send data to
- Lunar calendar is no longer available on Android < 7.0

**New feature**
- New (better) chart for precipitation nowcasting: more accurate with a bar chart, has light/medium/heavy thresholds to help interpret the data, has start time and end time on the marker, now supports RtL languages
- Alternate calendars (from `Settings` > `Appearance`) for Android >= 7.0. Currently supported: Chinese, Dangi, Indian National, Islamic and Persian.

**Sources**
- [GeoSphere Austria] Added as main source in Austria and nearby
- [GeoSphere Austria] Added as secondary source for air quality in Europe and nearby
- [GeoSphere Austria] Added as secondary source for precipitation nowcasting in Austria and nearby
- [GeoSphere Austria] Added as secondary source for alerts in Austria
- [Gadgetbridge] We are now able to send data for secondary locations
- [AccuWeather] Fix fail to refresh when using a language not supported by AccuWeather (noticed on Central Kurdish, Esperanto and Interlingua), now fallbacks to English
- [Open-Meteo] Fix workaround location search issue when result list is empty (will show instructions instead of an error)
- [Open-Meteo] Fix issues related to DST
- [Open-Meteo] Fix location search failed when at least one result didn’t have a timezone info
- [DMI] Add severity/colors to alerts
- [Android location source] Added support for native Fused on Android >= 12

**Improvements**
- Improved privacy of the “Send Gadgetbridge data” which now allows you to select which apps you want to send data to, instead of all compatible apps.
- Added source for each alert for compatible sources
- [Widgets] “Material You - Current” automatically adapt size to the frame on Android >= 12
- [Widgets] “Material You - Current” add preview
- [Widgets] Added a custom subtitle keyword for a summary of pollen indexes
- Background updates: secondary locations (when used) are now refreshed once a day
- Add a new error message “Server unavailable”
- Improved time picker for forecast notification (now supports 12-hour system), also fixes a crash on Android 14 QPR2 (@min7-i)

**Fixes**
- [Widgets] “Material You - Current” Tap to open app now works again
- [Widgets] Day + Vertical - To fix a crash when using Android < 12, the clock when displayed as analog will no longer set the correct timezone on Android < 12 (this feature is not available on these versions)
- Fix wrong number displayed on the daily feels like chart
- Fix current location keeping old timezone on some sources, when travelling to a different timezone
- Fix “Stay informed” card stays visible after allowing notifications via settings / app-info
- Fix extra padding on bottom of keyboard on location search (@min7-i)
- The sensor manager is no longer queried if the gravity sensor preference is disabled (fix a trigger on GrapheneOS)
- Fix back button in a sub settings menu exited settings instead of going back to main settings screen (@min7-i)
- Fix color of the “change source” button on location search (@min7-i)
- Fix live wallpaper refresh rate (was capped at 60 Hz)
- Fix non-configured sources showing as options for secondary sources

**Translations**
- Translations updated
- Initial translation added for Eesti (thanks Priit Jõerüüt!)


# Version 5.1.8 (2024-03-30)

Due to a technical change, if you had set up a different language than your system language, you will have to set it again on update.

**New features**
- Added ability to change the background weather and time for each location

**Weather sources**
- [WMO SWIC] Switch to newer v3.0 endpoint for alerts, should be more reliable and much faster
- [WMO SWIC] Updated alert colors to v3.0
- Default alert colors for sources not providing colors (DMI, ECCC, OpenWeather, Pirate Weather) were also updated to match alert colors of WMO SWIC v3.0

**Improvements and fixes**
- Precipitation nowcast is now a dedicated card
- Fix crash on Android < 7.0
- Fix live wallpaper non-auto weather kind
- A new error type “Invalid or incomplete data received from server” was added
- Fix display issue when changing main source sometimes resulted in empty value for secondary sources
- Add error handling to “change instance URL” feature of self-hostable sources
- [Widgets] Fix clock and dates didn’t honor timezone of the location (used system timezone instead)
- [Widgets] Fix clock font preference reset every time configuration screen was entered
- [Widgets] Fix Material You - Forecast not being available on some devices
- [Widgets] Fix Material You - Current proportions

**Translations**
- Translations updated
- Basic support added for British English (supports a few spelling differences), also added to Australian English and Canadian English (new)
- Improved date formatting work for Android 7.0+ is now complete
- Chinese is now handled as Simplified Chinese and Traditional Chinese to ease the work of translators
- Fix - All non-weather-related texts are now properly updated on language change (weather-related texts still require a refresh of locations)


# Version 5.1.7 (2024-03-20)

**Improvements and fixes**
- [Privacy] If you enabled sending data to Gadgetbridge, Breezy Weather will no longer share your longitude, latitude and if your location is your current position with Gadgetbridge or any other app.
- [Privacy] The “Send weather data to Gadgetbridge” option was renamed “Broadcast my weather data to other apps” as any app can “pretend to be Gadgetbridge”. A confirmation dialog will appear to make the user confirm they fully understand the risks.
- [Privacy] The “Send weather data to Gadgetbridge” (which is off by default, unless you enabled it) will be turned off on all devices on update to let users decide if they still want to enable it.
- For sources not providing colors, we now make the color of the alert dynamic based on severity instead of always orange. This scale is the same as the one used by World Meteorological Organization and may differ with colors usually provided by your national provider. For sources without severity, this will fallback to Unknown color.

**Weather sources**
- Updated recommendations for new locations (existing locations must be modified manually, or re-added):

| Location  | Source type | Old         | New                |
|-----------|-------------|-------------|--------------------|
| Hong Kong | Alert       | AccuWeather | WMO Severe Weather |

**Translations**
- Translations updated


# Version 5.1.6 (2024-03-19)

**Improvements and fixes**
- Allow to enable Gadgetbridge if Smartspacer generic weather plugin is installed
- Fix cypress, hazel and hornbeam pollen not correctly saved to database

**Weather sources**
- [Open-Meteo] Added ability to choose your own self-hosted instance in the settings
- [Bright Sky (DWD)] Added ability to choose your own self-hosted instance in the settings
- [Recosanté] Added ability to choose your own self-hosted instance in the settings

**Translations**
- Translations updated
- Added Interlingua (very limited support at the moment) (thanks @softinterlingua)
- Date formatting was improved on Android 7.0+ for many languages, including languages not currently supported. On lower Android versions, it will fallback to ISO 8601 formatting for short format, and weekday / day / month / year for long format. More improvements will come in later versions (mostly on widgets).


# Version 5.1.5 (2024-03-16)

*Just a re-release of v5.1.4 to include some technical build adjustments to prepare for release in default F-Droid repo.*

**Translations**
- Translations updated


# Version 5.1.4-beta (2024-03-16)

**New features**
- We now have our own repo for F-Droid and Google Play flavors of Breezy Weather. If you want to switch from our current repo (Standard flavor) to a different flavor, you should remove the repo for the Standard flavor first to avoid getting updates for the wrong flavor. [Link to Breezy Weather F-Droid repos](https://github.com/breezy-weather/fdroid-repo/blob/main/README.md)

**Weather sources**
- [World Meteorological Organization (WMO) Severe Weather] Added experimental support as secondary alert source. Translations are not supported (yet). Please report as soon as possible if you encounter a refresh error, making sure you mention the country of your location in the issue.
- [ECCC] Add sunshine duration
- [Natural Earth] Improve detection of small islands/countries
- [Recosanté] Fix refresh error on Friday
- Updated recommendations for new locations (existing locations must be modified manually, or re-added):

| Location | Source type | Old         | New         |
|----------|-------------|-------------|-------------|
| France   | Pollen      | Open-Meteo  | Recosanté   |

**Improvements and fixes**
- Background weather update will now only process the first location, unless you have multi cities usage. This makes the process more green and efficient, and other secondary locations will automatically be updated when you open them instead (this means that if you don’t check these locations very often, it will no longer uselessly refresh them in the background). A document has been written to describe in details how the full weather update process work: https://github.com/breezy-weather/breezy-weather/blob/main/docs/UPDATES.md
- Fix AccuWeather being the default weather source instead of automatic recommendations
- Fix "Please wait a few seconds before retrying" when adding a location
- Fix some sources not being available for current location if current location didn’t change
- Fix notification-widget not being restored on reboot

**Translations**
- Translations updated


# Version 5.1.3-beta (2024-03-13)

**New features**
- Per-location per-source preferences

**Weather sources**
- [Open-Meteo] Allow to choose weather models per-location

**Improvements and fixes**
- Add troubleshooting for “Source no longer available” error

**Translations**
- Translations updated


# Version 5.1.2-beta (2024-03-13)

**New features**
- Sunshine duration daily chart (for compatible sources)

**Improvements and fixes**
- “Hours of sun” was removed and split into:
  - “Daylight duration” which was moved to Sun section
  - “Sunshine duration” for compatible sources
- Icon pack selection dialog was rewritten in Material 3
- You will now have a confirmation dialog before opening a link
- [Text widget] Fix custom subtitle being cut if on multiple lines

**Translations**
- Translations updated


# Version 5.1.1-beta (2024-03-12)

**Improvements and fixes**
- Main screen pollen card will now only show pollens with at least one day where concentration > 0. Behavior remains the same on daily page and pollen details page.
- App should no longer prank you by changing the name of a location you just added from search

**Weather sources**
- [Recosanté] Add as secondary pollen source for France.
- [Open-Meteo] Replaced “hours of sun” with “sunshine duration” since we have that info in the API
- [Bright Sky (DWD)] Replaced “hours of sun” with “sunshine duration” since we have that info in the API
- [Bright Sky (DWD)] Added to the `fdroid` flavor, since it’s open source.

**Translations**
- Translations updated


# Version 5.1.0-beta (2024-03-08)

**New features**
- Breezy Weather now comes in 3 flavors:
  - Standard: this is the version you used so far
  - Google Play: same as Standard + proprietary Fused location from Google Play Services. Useful if you use “Current location” feature AND have Google Play Services installed on your device. Otherwise, you should use the standard version.
  - F-Droid: this version removes support for any weather sources which are not libre and self-hostable, so you are sure to get a 100% FOSS experience and not connect by mistake to a non-free network. There are plans to add this version to the default F-Droid repository.

**Improvements and fixes**
- [Widgets] Custom subtitle - A keyword was added for current and daily air quality index
- [Widgets] Text widget - Add support for custom subtitle
- [Widgets] Text widget - Add ability to hide header (i.e. only show custom subtitle)
- [Widgets] Trend widgets - Make text easier to read (@min7-i)
- Add a second way to delete location from location preferences (Edit button in footer) (will only show if there is at least 2 locations in the list as location list cannot be empty)
- Better mapping for icons of Chronus icon packs (@min7-i)
- Fix weather data refresh failure on Android < 7.1.1 for weather sources using Let’s encrypt certificates
- Fix loop issues when manually refreshing and encountering weather refresh failures (will now wait at least 10 seconds before retrying)

**Weather sources**
- [Israel Meteorological Service] Added as main source
- [Israel Meteorological Service] Added as secondary alert source
- [MET IE] Fix 12-hour system issue
- [MET IE] Add warnings as main source in Ireland-only
- [MET IE] Add warnings as secondary source in Ireland-only
- [MET IE] Add reverse geocoding in Ireland-only
- Updated recommendations for new locations (existing locations must be modified manually, or re-added):

| Location                      | Source type | Old         | New                           |
|-------------------------------|-------------|-------------|-------------------------------|
| Israel, West Bank, Gaza Strip | Main source | Open-Meteo  | Israel Meteorological Service |
| Ireland                       | Alerts      | AccuWeather | MET Éireann                   |

**Removed features**
- Pixel icon pack is no longer bundled. You will have to [download it manually](https://github.com/breezy-weather/pixel-icon-provider/releases).
- All other non-free assets were replaced/removed

**Translations**
- Translations updated


# Version 5.0.3-alpha (2024-03-02)

**Improvements and fixes**
- You can now choose to always use a specific weather source for new locations instead of Breezy Weather recommendations (Settings > Weather sources)
- Bring back “auto” mode for Live Wallpaper
- Add colors to cloud cover hourly chart
- Use a different font for temperature icon in status bar to improve readability (@min7-i)

**Weather sources**
- [Natural Earth] Added as an offline country-only reverse geocoding source. Helps providing the compatible weather sources for each location.
- [ECCC] Fix missing observation text
- Fix mismatching text with current weather condition in some very rare cases

**Translations**
- Translations updated


# Version 5.0.2-alpha (2024-02-27)

**Improvements and fixes**
- Don’t display “Feels like” tab in Daily and Hourly trends if there is no data
- Fallback to real temperature if feels like temperature is missing in hourly feels like chart to avoid weird looking chart
- Compute missing relative humidity if we have temperature and dew point (useful for DWD)
- Fix weather text showing “Clear sky” when weather condition is unknown
- Remove the ability to select a main weather source incompatible with that country
- Fix widgets not updating after a widget config change
- Fix contrast issue when using monochrome theme (@min7-i)

**Translations**
- Translations updated
- Comma were replaced by 、 in Chinese and Japanese languages. If you use a different character than “,” for comma in your language, please open an issue on GitHub (not available through Weblate).


# Version 5.0.1-alpha (2024-02-26)

**Fixes**
- Fix crash when adding more than 4 or 5 current details in header
- Fix weather source failing in loop (noticed with NWS source, for example)
- Fallback to real temperature if feels like temperature is missing in daily feels like chart to avoid weird looking chart


# Version 5.0.0-alpha (2024-02-26)

We found a proprietary dependency in the database component. To get rid of it, we chose to use a different database component and not offer any migration. You will have to re-add your locations. Your preferences are safe.

This an **alpha release**. Use it at your own risk. If you do, report any issue you found to help stabilizing the app! Stay on v4.6.14 for now if you want a stable release and are not concerned about the proprietary dependency.

Unfinished things that will be in a future 5.0.x version:
- Bring back temporarily removed "auto" option for live wallpaper
- Offline reverse geocoding to get location presets for current location
- [UX] Improve weather sources dialog to be less overwhelming
- Maybe release a stable v4.6.15 that inform users that next version will empty location list, if there are too many reports

**New features and main improvements**
- Design refresh of current details in header
- New daily chart: feels like temperature
- New hourly chart: feels like temperature
- New hourly chart: humidity / dew point
- New hourly chart: pressure
- New hourly chart: cloud cover
- New hourly chart: visibility
- Better precipitation notifications, now based on precipitation minute by minute (not supported by all weather sources)
- Location search no longer asks you to choose your weather source, which may have been confusing. Instead, you will make a search, and when tapping on a location in the result list, location will be set up with a preset of main and secondary sources we recommend that varies depending on the country of that location. You are able to change these sources before confirming adding of that location. At the moment, this only affects manually added locations and experience of adding of current location remains unchanged due to technical reasons (no reverse geocoding available).
- Following that change, the purpose of the “Change source” button on bottom of location search is to change the default location search source (which was previously found rather hidden in settings) instead of the weather source to use with locations.

**Weather sources**
- [Danmarks Meteorologiske Institut (DMI)] Added support as a worldwide main source, with alerts for Denmark-only
- [Danmarks Meteorologiske Institut (DMI)] Added support as a secondary source for alerts in Denmark-only
- [China] Added support as a secondary source for minutely, alerts and air quality in China-only
- [National Weather Service (NWS)] Added support as a main source in United States of America and territories-only
- [National Weather Service (NWS)] Added support as a secondary source for alerts in United States of America and territories-only
- [Bright Sky (DWD)] Added support as a main source in Germany-only
- [Bright Sky (DWD)] Added support as a secondary source for alerts in Germany-only
- [Environment and Climate Change Canada] Added support as a main source in Canada-only
- [Environment and Climate Change Canada] Added support as a secondary source for alerts and normals in Canada-only
- [SMHI] Added support as a main source in Sweden-only
- [MET Éireann] Added support as a main source in Ireland-only (alerts not yet supported)
- [AccuWeather] Added support as a secondary-source for air quality, pollen and normals
- [AccuWeather] Fix normals (missing in January, shifted by one month on other months)
- [MET Norway] Fix precipitation in the next hour failing in some cases
- [PirateWeather] Retrieve 168 hours of forecast
- [PirateWeather] Fix precipitation probability
- [HERE] Remove support for NWS alerts, they were never filtered according to the location (was returning all alerts from USA!), so use directly NWS source instead
- [HERE] Remove as a location search source as other sources are more reliable to be used with the new location search page

**Other improvements and fixes**
- [Weather refresh] Various improvements to missing data computing/extrapolating
- [Alerts] Show alert color icon on alert details page
- [Location list] Swiping left on a manually added location now opens the weather sources preferences instead of (un)setting resident location status
- [Location weather sources preferences] Clarified features supported by main source in secondary weather sources preferences. For exemple, at the moment of writing, Open-Meteo doesn't support alerts. If your main source is Open-Meteo and you don't have a secondary source set for Alerts, it showed “Main source”, now it will show “None” instead.
- [Location weather sources preferences] Fix secondary source choices not updating when changing main weather source (if main source supports secondary features, it wouldn't be showing in secondary sources list)
- [Location weather sources preferences] Fix a display issue of “alert source” instead of “normals source” in secondary weather sources settings of a location
- [Location weather sources preferences] Fix duplicate location when main weather source is changed to an existing same location with same weather source
- [Location weather sources preferences] Fix when changing main weather source, saving previously redirected to first location in location list. Now, it will stay on the correct location
- [Notifications] Fix crash when today/tomorrow notification was received on Android 14
- [Notification-widget] Fix temperature icon in status bar being cut off on some devices (@min7-i)
- [Weather refresh] When background updates are disabled, when opening a location, it will now automatically refresh if it was last refreshed more than 1.5 hours ago
- [Settings] When background updates are disabled, make user aware that “Notifications of weather alerts” and “Notifications of precipitations” are unavailable by greying the options in settings
- [Widgets] Fix clock day vertical widget in vertical or tile mode was showing decimals to current temperature
- Fix UV index description being sometimes wrong
- Fix all alert dialogs that had too much padding in content compared to title.

**Removed features**
- Resident locations. Undocumented, encourages bad practices (two identical locations to be refreshed) and terrible technical implementation. May be brought back differently if you can provide well-thought use cases, functional specifications, and a better technical implementation.
- Widget layout option “Oreo (Google Sans)” (Oreo without Google Sans remains available). We don’t have licensing rights of the Google Sans font. Customizing font on widgets was also an unofficial feature that was removed with a security update released in 2021, so that feature didn’t work for most devices anyway.

**Translations**
- Translation added and completed for Беларуская (thanks @kilimov25!)
- Translation added and completed for Македонски (thanks @ikocevski7 and @kilimov25!)
- Initial translation added for Esperanto
- Initial translation added for فارسی (thanks Aspen!)
- Translations updated
