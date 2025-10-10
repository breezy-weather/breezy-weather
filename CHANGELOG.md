# Old changelogs

- [Changelog for v5.x](docs/CHANGELOG_5.x.md)
- [Changelog for v4.x](docs/CHANGELOG_4.x.md)


# Version 6.1.x (not yet released)

The following features are already available in the current branch, but will be removed before each v6.0.x release and restored after, during the testing phase.

**New features**
- Content provider: allows (with your permission) other apps to query your weather data. [Read the announcement](https://github.com/breezy-weather/breezy-weather/discussions/2089)
- New broadcast: you can use `org.breezyweather.ACTION_UPDATE_NOTIFIER` (or `org.breezyweather.debug.ACTION_UPDATE_NOTIFIER` with the debug build) to be notified of updated locations (most common use case is coupled with the content provider)


# Version 6.0.12-rc (not yet released)

**Improvements and fixes**
- Daily/hourly forecast - Ensure the maximum value is always at a minimum defined value to ensure data is put in perspective, and remove threshold lines that weren’t very useful and cluttering the interface (wind, precipitation, cloud cover)
- Make 24-hour charts and nowcasting charts less prone to swipe to next screen
- Main screen - Fix moon icon disappearing past midnight
- Main screen - Fix blocks not appearing after fade in animation was interrupted due to fast scrolling
- Fix current air quality disappearing when refreshing too fast

**Weather sources**
- Nominatim - Add missing preference to change server instance
- Open-Meteo - Allow individual selection of new weather models: ECMWF IFS HRES 9 km, NCEP NAM U.S. Conus, MeteoSwiss

**Translations**
- Translations updated


# Version 6.0.11-rc (2025-09-03)

**Improvements and fixes**
- Fix crash when entering Appearance settings using 12-hour format with scheduled dark mode
- Current location - Fix details sometimes not saved to database (previous location details restored on restart of the app)
- Remove animations in the pressure block as it caused flickering
- Change default distance unit for Germany to kilometer, as per DWD usage
- Change default speed unit for Netherlands to meter per second, as per KNMI usage
- Fix threshold value for scattered cloud cover (@cloneofghosts)

**Translations**
- Translations updated


# Version 6.0.10-rc (2025-09-01)

**Improvements and fixes**
- Add instructions to pull to refresh instead of leaving a blank screen when weather failed to load initially (@Amitesh-exp)

**Weather sources**
- ECCC - Technical changes
- NCDR - Fix error when there is no alert (@chunshek)

**Translations**
- Translations updated


# Version 6.0.9-beta (2025-08-31)

**Improvements and fixes**
- Clarify which dark mode is currently used at system level in Appearance settings, which may help Xiaomi device owners detect a potential bug in the MIUI dark mode implementation
- Freenet - Improve wording of messages about non-free network services
- Freenet - Display the names of non-free network services in source lists to let the user know about the availability of other sources in the Standard flavor
- Android 11+ - Fix unneeded zeros sometimes showing in fractions

**Weather sources**
- IP.SB / Baidu IP Location - Don’t require Android location to be on

**Translations**
- Translations updated

**Technical**
- Fallback to latest known current data rather than current hour forecast when last successful refresh was less than 30 min ago


# Version 6.0.8-beta (2025-08-27)

**Improvements and fixes**
- Minor changes to weather blocks to improve accessibility (text size, color contrast, etc.)
- Widgets - Round temperature values
- Nowcasting block - Fix truncated start and end values

**Translations**
- Translations updated


# Version 6.0.7-beta (2025-08-26)

**Translations**
- Translations updated
- Add missing distance, speed and precipitation unit translations on Android < 7

**Technical**
- Added timezone deduction based on subdivision codes (@chunshek)

# Version 6.0.6-alpha (2025-08-24)

**Improvements and fixes**
- Fix crash on startup on Android 5.0, 5.1 and 6.0
- Fix crash on Android 7.0/7.1 when formatting some units
- Widgets - Fix crash on Android 9.0 to 11.0 with font size set to something other than 100%

**Weather sources**
- [HERE] Removed following recent restrictions on free API

**Translations**
- Translations updated


# Version 6.0.5-alpha (2025-08-23)

This version is still an experimental one, with a significant rewrite of the refresh process core, especially on current locations. Weather data for all locations will be reset due to a major technical change in the database. A simple refresh will bring it back.

**Removed features**
- Mean daytime/nighttime temperatures as threshold lines. Use a normals source instead
- [Met Office UK] Removed address lookup feature
- Pressure unit - Kilogram force per square centimeter

**Improvements and fixes**
- Main screen - Allow to move small blocks by drag & drop
- Main screen - The number of items displayed at once in daily/hourly forecast now depends on display size and font scale (previously always 5 in portrait, and 7 in landscape)
- Main screen - Show “Negligible” inside Pollen block if there is no pollen today instead of an empty block
- Main screen - Allow up to 5 blocks on a row depending on width display size and font scale
- Main screen - Move refresh time out of app bar when scrolling
- Main screen - Fix settings not applying immediately
- Main screen - Fix shooting stars getting stuck in the corner in landscape
- Details - Add a bottom margin at the end of each page, so that it doesn’t overlap with the floating button
- Details - Don’t animate charts when “Other element animations” is disabled
- Details - Air quality - Add individual charts for each pollutant
- Details - Humidity/Dewpoint/Cloud cover - Show min/max of the day
- Details - Pressure/Visibility - Fix sometimes wrong daily value
- Details - Fallback to current value on Today screen when daily value is missing
- Details - Add visibility and cloud cover scales
- Details - Fix top X-axis sometimes showing “-” for some sources
- Details - Charts are now slightly wider following the removal of start and end paddings by removing midnight labels
- Alerts - Add “Translate” and “Share” to text select actions
- Nowcasting chart/Precipitation notification - Fix slightly wrong ending time of precipitation report
- Settings - Improve the location-based dark mode preference to make it easier to understand
- Sources - Add a “Recommended” section to the Source selection screen
- Refresh - Fix a rare crash when Android fails to send us the current location
- Refresh - Add an error when air quality forecast times don’t match hourly forecast times (observed in India, for example)
- Refresh - Ensure range of (almost) all values provided by sources, so you no longer have to freak out when seeing -999° with PirateWeather or 1015° with Meteo AM
- Data sharing - Fix crash when sending too many locations (will now retry with less locations)
- Widgets - Improve UX of custom subtitle documentation (@codewithdipesh)
- Widgets - Improve line height on many widgets
- Widgets - Weekly - Spread day/night temperatures on 2 lines if necessary
- Widgets - Minor fixes
- Wallpaper - Due to some people running outdated versions of Breezy Weather just to see some gimmicks on their wallpaper, we bring back wallpaper animations behind a dangerous disabled-by-default option. We STRONGLY advise against enabling them.

**Weather sources**
- [AccuWeather] Restrict pollen to USA, Canada and Europe as it’s only available there (@chunshek)
- [China] Fix reversed color and severity for alerts (@chunshek)
- [EKUK] Fix failure to refresh air quality
- [FOSS Public Alert Server] Add support for this experimental source for alerts (@chunshek)
- [GeoSphere AT] Fix missing info in warnings
- [GeoSphere AT] Use the newer better endpoint for air quality
- [JMA] Added Thai translations (@chunshek)
- [LVGMC] Fix current observations (@chunshek)
- [NCDR] Added as alert source for Taiwan (@chunshek)
- [NCEI] Added support for normals (@chunshek)
- [Nominatim] Added as another location search
- [NSLC] Added as address lookup source for Taiwan (@chunshek)
- [NWS] Alerts - Updated terminology for Extreme Heat (@chunshek)
- [Open-Meteo] Restrict pollen to Europe as it’s only available there (@chunshek)
- [Pirate Weather] Add support for daily/hourly summaries
- [Veðurstofa Íslands] Added as forecast, current, alert and address lookup source for Iceland (@chunshek)
- [WMO SWIC] Avoid missing alerts which expired date was updated
- [ANAM-BF, DCCMS, DMN, DWR, EMI, GMet, IGEBU, INM, Mali-Météo, Météo Benin, Météo Tchad, Météo Togo, Mettelsat, MSD, Pirate Weather, SMA (Seychelles), SMA (Sudan), SSMS] Add to  ̀freenet` flavor (was missing despite being FOSS)

**Translations**
- Initial translation added for Íslenska (@chunshek)
- Translations updated
- Alternate calendar: add Hebrew calendar
- Alternate calendar: add more defaults based on regional preferences

**Technical**
- Current location process refactoring: coordinates, forced refresh when coordinates changed from more than 5 km
- Address lookup process refactoring to prepare for future ability to add a location manually by coordinates
- Experimental offline timezone deduction for address lookup sources missing the info or for Nominatim search service (@chunshek)
- Unit conversion/formatting refactoring. **Known temporary issue:** Some distance, speed and precipitation units are no longer translated on Android < 7


# Version 6.0.4-alpha (2025-07-23)

**Improvements and fixes**
- Main screen - Improvements to some cut off texts with different display sizes
- Main screen - Improve the “two blocks per row” threshold when using custom font scale
- Details - Fix precipitation probability details being expressed in precipitation unit instead of %
- Fix missing normals every other refresh

**Translations**
- Translations updated


# Version 6.0.3-alpha (2025-07-22)

**New features**
- Redesign of main screen in Material 3 Expressive
- New information previously not shown on main screen: current wind gusts, clock (block not enabled by default)
- Redesign background animations/colors to better adapt to the selected dark mode and avoid saturated colors with bad contrast

**Removed features**
- Main screen - Details in header
- Main screen - Details block
- Custom weather and time per location
- Details of each different “feels like”. Will now just display the source-preferred feels like value, or if not available, our own computed feels like

**Improvements and fixes**
- Fix nowcasting chart not honoring precipitation unit override
- Details - Fix feels like toggle not remembered through days
- Main screen - Fix tapping daily/hourly feels like forecast opening conditions with feels like toggle off
- Details - Display normals as deviation directly under daytime/nighttime temperature
- Improve display of precipitation details
- Details - Make tooltips persistent until you click outside the bounds of the tooltip
- Details - Show current air quality on Today page when no daily air quality is available

**Translations**
- Translations updated


# Version 6.0.2-alpha (2025-07-19)

**Improvements and fixes**
- Fix crash in some cases on old Android devices
- Fix notification icons not showing
- Make main screen top icons feel more intuitive

**Weather sources**
- [Météo-France] Better formatting for warnings


# Version 6.0.1-alpha (2025-07-17)

**New features**
- Twilight dates (dawn and dusk)

**Removed features**
- Sun & Moon data from sources. Will now always be computed by Breezy Weather for consistency

**Improvements and fixes**
- Details page - Fix floating action button not updating in real time (@min7-i)
- Details page - Charts - Fix area fill in Right to Left languages (@chunshek)
- Details page - Fix jumping of the chart when tapping on it
- Details page - Workaround missing top padding in the FAB menu for small device heights (@min7-i)
- Details page - Conditions - move long weather condition description to a dedicated Daily summary card (especially noticeable with AccuWeather source)
- Details page - Sun & Moon - Fix glitched charts (@chunshek)
- Main screen - Attempt to make horizontal swipes in daily/hourly trends less prone to switch to prev/next locations
- Main screen - Move “Settings” icon to location list to be able to display icons on main screen without a submenu.
- Main screen - Better animation for main screen current temperature when using Fahrenheit or Kelvin
- Main screen - Use Material 3 Expressive buttons for forecast buttons
- Main screen - Fix sun & moon direction in RtL languages
- Main screen - Fix air quality direction in RtL languages
- Main screen - Fix missing hourly visibility in some cases
- Settings - Material 3 Expressive theme
- Settings - Add shortcuts to daily/trend configuration from cards configuration
- Fix tint of “Open in another app” icon in landscape mode
- Improve the formatting of today/tomorrow notification
- Live wallpaper - Fix wallpaper animating when switching between apps
- Fix specific language for the app not remembered after reboot
- UV - Better computing of missing hourly UV from day UV (@chunshek)

**Translations**
- Translations updated
- Default units are now based on system region. It does not support Android 16 “Measurement system” preference yet, as there seems to be no way to access this value for now.
- Better number formatting on Android >= 7
- Better measure formatting on Android >= 7


# Version 6.0.0-alpha (2025-06-26)

**New features**
- Complete overhaul of the daily details page to offer a better visualization of the data, and more explanations about the different types of weather data
- Past hourly forecast can now be viewed in the details page

**Removed features**
- Main screen hourly forecast card will now only show the next 24 hours, as the rest of the forecast can now be seen with more readability in the daily details page.
- The dedicated pollen page accessed when tapping on the pollen card now no longer exists, and was replaced by the pollen page in daily details.
- Tapping on the main screen air quality card no longer show more details, but open the air quality page in daily details instead.
- Tapping on an hourly item in the main screen hourly forecast no longer opens a dialog, but now opens the day details page of the currently selected type of data

**Improvements and fixes**
- Redesigned main screen footer to support links to the sources, a link to the privacy policy, and icons for the sources for which it is mandatory
- Fix crash when using “Open in another app” when no app on the phone is able to open it

**Weather sources**
- [ECCC] Added UV index

**Translations**
- Translations updated
