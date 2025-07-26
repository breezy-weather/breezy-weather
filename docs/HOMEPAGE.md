# Main screen

This page intends to give some explanations to how features displayed in the main screen of Breezy Weather (version 6.0.2 and later) work and how they should be interpreted. Position of elements are described for a Left-to-Right display. [Click here to access main screen documentation of versions before 6.0.2](https://github.com/breezy-weather/breezy-weather/blob/v6.0.0-alpha/docs/HOMEPAGE.md).

Main screen shows the weather for the selected location, with an optional drawer showing location list on large display devices.


## Glossary

- Current weather: weather at the time of the last refresh
- Days splitting: Each day starts at 06:00 and ends at 29:59 (05:59 the day after). Days are split into two moments (or “half days”): daytime (06:00 to 17:59) and nighttime (18:00 to 29:59). *Some sources might use slightly different hours, such as China from 10:00 to 19:59 and from 20:00 to 31:59.*


## Theme

In the background, you can see an animation matching the current weather condition.

The color of the background depends on dark mode settings and whether the sun is up for the selected location:

| App dark mode¹   | Location-based dark mode             | Sun is up | Sun is down |
|------------------|--------------------------------------|-----------|-------------|
| Light            | Follow day/night                     | Light     | Dark        |
| Light            | Always dark when app is in dark mode | Light     | Dark        |
| Dark             | Follow day/night                     | Light     | Dark        |
| Dark             | Always dark when app is in dark mode | Dark      | Dark        |

¹If set to “Follow system”, pick the current system dark mode to read this table

This animation is dynamic by default, but you can choose a static one in the settings (Appearance).


## App bar

On top, you have a back button to access location list and city name of the location on the left. On the right, you have an “Open in another app” button that lets you open this location coordinates in a compatible application. You also have an Edit button that lets you access different options specific to the location, such as:
- Reorganize main screen (applies to all locations)
- (Current location only) Change location service
- Change main weather source or secondary weather sources (air quality, allergens, precipitations minute by minute, alerts)
- Change source-specific preferences

Just below, you can see the last time of refresh.


## Header

Above weather blocks, you have details about current weather:

- A description of the current weather condition
- Temperature
- (If different from temperature) Feels like temperature
- The temperature for the current and next half days:
  - Before 06:00, it will show yesterday nighttime min temperature (if available), followed by today daytime max temperature
  - Before 18:00, it will show today daytime max temperature, followed by today nighttime min temperature
  - After 18:00, it will show today nighttime min temperature, followed by tomorrow daytime max temperature

## Blocks

By default, blocks are configured to display in this order and can be reordered by drag & drop or from settings:

- Alerts (cannot be removed, will always show on top)
- Precipitation nowcasting
- Daily forecast
- Hourly forecast
- Precipitation
- Wind
- Air quality
- Pollen
- Humidity
- UV
- Visibility
- Pressure
- Sun
- Moon
- Clock (not enabled by default)

Some blocks may not show with some sources if the data is not available. Check [sources](SOURCES.md) for more info.

**All blocks can be tapped on to access more details.**


### Alerts

The first blocks is always the Alerts.

Current alerts are listed with their starting time and ending time, if they are valid for a specific period.
If there are no alerts currently, but there are some scheduled for the future, it will be written, and you will be able to tap on the text to see the details of these future alerts.


### Nowcasting precipitation

Nowcasting precipitation, not to be confused with Precipitation, are the precipitations in the very short term, minute by minute.

This block will show when you selected a compatible Nowcasting source AND there are precipitation in the next hour(s).

You can drag a guideline on the chart to see the expected precipitation quantity 5 minute by 5 minute.


### Daily forecast

Daily forecast is made of multiple tabs called trends. Order of daily trends can be changed in settings.

For each trend, the day of the week or "Today" is shown on top of each column, and just below, the day of the month and the month are shown.

When data is missing for the whole period, the tab will be hidden.


#### Temperature trends

From top to bottom:

- Weather condition of daytime (icon)
- Maximum temperature of daytime (graph)
- Minimum temperature of nighttime (graph)
- Weather condition of nighttime (icon)
- Maximum precipitation probability for the whole day (bar + text)

If weather source has normals data, it will show two lines with maximum and minimum temperature normals for the month of the first day in forecast. If normals are not available, it will fallback to use a median maximum and minimum temperature for the daily forecast period.


#### Air quality trends

Each column shows the maximum air quality index for the day (bar + text) from 00:00 to 23:59. More details about air quality index can be read in Air quality block section.

Bar color will have a different color according to air quality index:

- Green: from 0 to 20
- Yellow: from 21 to 50
- Orange: from 51 to 100
- Red: from 101 to 150
- Purple: from 151 to 250
- Brown: 251 and above

Two lines show two alert levels (20 and 150). It can be disabled in the settings (Appearance).


#### Wind trends

From top to bottom:

- Origin of the dominant wind with the highest speed during daytime (arrow icon)
- Highest wind speed of daytime (text + bar)
- Highest wind speed of nighttime (text + bar)
- Origin of the dominant wind with the highest speed during daytime (arrow icon)

Wind icons and speed bars have a different color according to Beaufort wind scale:

- Green: from 0 to 3
- Yellow: from 4 to 5
- Orange: from 6 to 7
- Red: from 8 to 9
- Purple: from 10 to 11
- Brown: 12 and above

Two lines are shown corresponding to show when wind speed is exceeding Gentle breeze level for daytime and for nighttime. It can be disabled in the settings (Appearance).


#### UV trends

Each column shows the maximum UV index for the day (bar + text).

Bar color will have a different color according to UV level:

- Green: from 0 to 2
- Yellow: from 3 to 5
- Orange: from 6 to 7
- Red: from 8 to 10
- Purple: 11 and above

A line shows the alert level (7). It can be disabled in the settings (Appearance).


#### Precipitation trends

From top to bottom:

- Weather condition of daytime (icon)
- Total of all precipitation for daytime (bar + text)
- Total of all precipitation for nighttime (bar + text)
- Weather condition of nighttime (icon)

If total of all precipitation is 0, it will not be shown.

If total of all precipitation for all days is 0, precipitation trends will be hidden.


#### Sunshine trends

Each column shows the sunshine duration in hours for the day (bar + text) from 00:00 to 23:59.

Bar color is always light orange. It’s filled relative to the maximum daylight duration (from sunrise to sunset) of the daily forecast period. For example, if there are 6 hours of sunshine today, and the maximum daylight duration is 12 hours, the bar will be half-filled (50%).


#### Feels like trends

Feels like temperature is the first available data from this list:

- Source-provided feels like temperature
- Computed apparent temperature
- Computed wind chill temperature
- Computed Humidex
- Temperature

From top to bottom:

- Weather condition of daytime (icon)
- Maximum feels like temperature of daytime (graph)
- Minimum feels like temperature of nighttime (graph)
- Weather condition of nighttime (icon)
- Maximum precipitation probability for the whole day (bar + text)

If weather source has normals data, it will show two lines with maximum and minimum temperature normals for the month of the first day in forecast. If normals are not available, it will fallback to use a median maximum and minimum temperature for the daily forecast period.


### Hourly forecast

Just like daily forecast, hourly is made of the same tabs called trends. Order of hourly trends can be changed in settings.

For each trend, the hour of the week is shown on top of each column. If it is the first hour listed or if it midnight, the subtitle will be highlighted.

Below trends, a minute by minute precipitation quantity graph will be shown if precipitations in the next hour are above 0.

When data is missing for the whole period, the tab will be hidden.


#### Temperature trends

From top to bottom:

- Weather condition of the hour (icon)
- Temperature of the hour (graph)
- Precipitation probability (text)


#### Air quality trends

Each column shows the air quality index for the hour (bar + text). More details about air quality index can be read in Air quality block section.

Bar color will have a different color according to air quality index:

- Green: from 0 to 20
- Yellow: from 21 to 50
- Orange: from 51 to 100
- Red: from 101 to 150
- Purple: from 151 to 250
- Brown: 251 and above

Two lines show two alert levels (20 and 150). It can be disabled in the settings (Appearance).


#### Wind trends

From top to bottom:

- Origin of the dominant wind of the hour (arrow icon)
- Wind speed of the hour (text + bar)

Wind icons and speed bars have a different color according to Beaufort wind scale:

- Green: from 0 to 3
- Yellow: from 4 to 5
- Orange: from 6 to 7
- Red: from 8 to 9
- Purple: from 10 to 11
- Brown: 12 and above


#### UV trends

Each column shows the UV index for the hour (bar + text).

Bar color will have a different color according to UV level:

- Green: from 0 to 2
- Yellow: from 3 to 5
- Orange: from 6 to 7
- Red: from 8 to 10
- Purple: 11 and above

A line shows the alert level (7). It can be disabled in the settings (Appearance).


#### Precipitation trends

From top to bottom:

- Weather condition of the hour (icon)
- Total of all precipitation for the hour (bar + text)

If total of all precipitation for all hours is 0, precipitation trends will be hidden.


#### Feels like trends

From top to bottom:

- Weather condition of the hour (icon)
- Feels like temperature of the hour (graph)


#### Humidity / Dew point trends

From top to bottom:

- Weather condition of the hour (icon)
- Dew point of the hour (graph)
- Relative humidity percentage (text)


#### Pressure trends

From top to bottom:

- Weather condition of the hour (icon)
- Pressure of the hour (graph)


#### Cloud cover trends

Each column shows the cloud cover percentage for the hour (bar + text).

Two lines show two levels (clear sky and partly cloudy). It can be disabled in the settings (Appearance).


#### Visibility trends

From top to bottom:

- Weather condition of the hour (icon)
- Visibility of the hour (graph)


### Precipitation

Precipitation, not to be confused with the nowcasting chart, will show the expected precipitation quantity for the current half day (daytime at daytime, nighttime at nightime).

It will display the type of precipitation (rain, snow, sleet) in the text and as icon, if the information is known.


### Wind

This blocks shows the current wind, with the wind origin as a background arrow.

Below is a small text:

- When there are wind gusts, it will show this information
- Otherwise, the wind origin is written there


### Air quality

Air quality uses the [2023 Plume index](https://plumelabs.files.wordpress.com/2023/06/plume_aqi_2023.pdf) as a reference, and for SO2 and CO, it uses a similar scale based on [WHO recommendations from 2021](https://apps.who.int/iris/handle/10665/345329).

Tap on the block for more info about the thresholds.

AQIs above 250 follow a linear progression.

An AQI is calculated for O3, NO2, PM10 and PM2.5 pollutant, and the general AQI is the maximum value of these four.

The ¾ circle shows the general AQI, and it’s filled with a different color for each category, so you can easily see the pollution level. The maximum value for the circle and bars is 250. At greater value, the circle will always be fulfilled.


### Pollen

*Currently show pollens for current day, will be updated in the future to show for the current hour.*

Shows a maximum of two most dominant pollens (excluding mold) for that day, with at least concentration > 0. The details of supported allergens by the source can be seen by tapping on the block and accessing the pollen details page.

For each displayed pollen:

- On the left, there is an icon filled with a color representing the risk level
- On the right, name of the pollen, and below its risk level


### Humidity

Shows the relative humidity percentage, and the dew point.

In the background, you can see the block being filled by a percentage close to the relative humidity percentage (there are 5 different background images, so it’s always approximate).


### UV index

Shows the current UV index.

In the bottom of the block, there are 5 dots, with one of them enabled with the color of the current UV level. You can find details about the UV scale by tapping on the block.


### Visibility

Shows the current visibility.

The small text below shows the visibility level.

Here are the thresholds:

| Level           | From  | To       |
|-----------------|-------|----------|
| Very poor       | 0 km  | 1 km     |
| Poor            | 1 km  | 4 km     |
| Moderate        | 4 km  | 10 km    |
| Good            | 10 km | 20 km    |
| Clear           | 20 km | 40 km    |
| Perfectly clear | 40 km | No limit |


### Pressure

Shows the current pressure at sea level.

The circle is filled from 963 mb/hPa to 1,063 mb/hPa (converted into your selected unit, if that’s the case).

This means that when the pressure is half filled (filled up to the top of the circle), the pressure is at a normal level (1,013.25 mb/hPa).


### Sun

Arc show the progression of sun during their course for the currently observed day. The starting point is the sunrise time (the first one after midnight) and the endpoint is the sunset time. The sun icon remains on the endpoint after the sun set for the rest of the day.

The textual information are:

- Sunrise on the left
- Sunset on the right

Special cases: when sun is always up, it will show “Polar day”, and when sun is always down, it will show “Polar night”.


### Moon

Arc show the progression of moon during their course for the currently observed day. The starting point is the moonrise time and the endpoint is the moonset time. The moon icon remains on the endpoint after the moon set for the rest of the day.

The textual information are:

- Moonrise on the left
- Moonset on the right
- Moon phase (icon + text) below

Special cases:

- if the moon already rose before midnight that day AND the moon is still up from yesterday AND yesterday info about the moon is available (this is always the case when a location has been added the day before and has not seen a forecast source change since the day before), then the moonrise and moonset time are those from yesterday. When that “yesterday moon” sets, the information about “today moon” will take over
- if the moon rises twice that day after midnight, only the first rise time after midnight for that day will be shown. The set time is the set time for that rise time, and can end up on next day. That case is extremely rare, but if that information is important to you, we recommend using dedicated astronomy tools instead, especially as some weather sources might behave differently than what we just described (which is the general and fallback case).


### Clock

The clock will show the current time in this location. It automatically updates every minute.

An analog clock is displayed if one of the following conditions is met:

- You’re running Android 12 or later
- The timezone of this location is the same as your device timezone


### Footer

In the footer, you have credits and acknowledgment for the forecast source used for this location, and you can access them for all other sources by tapping the “More” link.
