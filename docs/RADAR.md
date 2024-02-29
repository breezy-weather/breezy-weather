# Radar

Breezy Weather offers no radar feature because there is no free radar API we can use that can compete with the other free websites listed below, and we don't intend to become a paid app, so it's best to just add to your homescreen a bookmark to one of the free websites below, that will be more feature-complete. Alternatively, if you are looking for a paid open source app, consider subscribing to OsmAnd Pro (also mentioned below).

Below is a list of suggested alternatives (you can submit a pull request to add more).


## Open source apps and Websites

Websites below often offer closed source apps as well, but adding a bookmark to your home screen is usually more privacy-conscious.

<table>
<thead>
    <tr>
        <th>Name</th>
        <th>Model</th>
        <th>Forecast length</th>
        <th>Sources</th>
    </tr>
</thead>
<tbody>
    <tr>
        <td><a href="https://osmand.net/">OsmAnd</a><br />(<a href="https://osmand.net/docs/user/plugins/weather/">Weather plugin</a>)</td>
        <td>Paid, subscription</td>
        <td>
            <ul>
                <li>1-hour step</li>
                <li>7 days</li>
            </ul>
        </td>
        <td>
            <ul>
                <li>GFS</li>
                <li>ECMWF</li>
            </ul>
        </td>
    </tr>
    <tr>
        <td><a href="https://www.rainviewer.com/weather-radar-map-live.html">RainViewer</a></td>
        <td>Free</td>
        <td>
            <ul>
                <li>10-min step</li>
                <li>2 next hours</li>
                <li>Past hour</li>
            </ul>
        </td>
        <td>
            <a href="https://www.rainviewer.com/sources.html">Full list</a>
        </td>
    </tr>
    <tr>
        <td><a href="https://www.ventusky.com/">Ventusky</a></td>
        <td>Free</td>
        <td>
            <ul>
                <li>1-hour step (3-hour and 6-hour steps after a few days)</li>
                <li>15 days</li>
                <li>Archive back to 1979</li>
            </ul>
        </td>
        <td>
            <ul>
                <li><strong>NOAA</strong>: GFS, HRRR, RTOFS, NBM</li>
                <li><strong>DWD</strong>: ICON, ICON (EU), ICON (DE)</li>
                <li>CMC: GEM</li>
                <li>FMI: SILAM</li>
                <li>ECMWF</li>
                <li>Met Office: UKMO, UKMO (UK)</li>
                <li>Météo France: AROME</li>
                <li>Meteorologisk institutt: MEPS (NO), WAVEWATCH (NO)</li>
            </ul>
        </td>
    </tr>
    <tr>
        <td><a href="https://www.windy.com/">Windy.com</a></td>
        <td>Freemium</td>
        <td>
            <ul>
                <li>Free:
                    <ul>
                        <li>3-hour step</li>
                        <li>6 days</li>
                    </ul>
                </li>
                <li>Paid:
                    <ul>
                        <li>1-hour step</li>
                        <li>10 days</li>
                        <li>1-year archive</li>
                    </ul>
                </li>
            </ul>
        </td>
        <td>
            <ul>
                <li>ECMWF 9 km</li>
                <li>UKV 2 km</li>
                <li>GFS 22 km</li>
                <li>ICON-D2 2.2 km, ICON-EU 7 km, ICON 13 km</li>
                <li>NEMS 4 km</li>
                <li>AROME 1.3 km</li>
            </ul>
        </td>
    </tr>
</tbody>
</table>


## Features

| Name                                                                 | Precipitation | Temperature | Wind | Clouds | Pressure | Waves | Air quality | Other                                                                            |
|----------------------------------------------------------------------|---------------|-------------|------|--------|----------|-------|-------------|----------------------------------------------------------------------------------|
| [OsmAnd](https://osmand.net/)                                        | ✅             | ✅           | ✅    | ✅      | ✅        | ❌     | ❌           |                                                                                  |
| [RainViewer](https://www.rainviewer.com/weather-radar-map-live.html) | ✅             | ❌           | ❌    | ❌      | ❌        | ❌     | ❌           |                                                                                  |
| [Ventusky](https://www.ventusky.com/)                                | ✅             | ✅           | ✅    | ✅      | ✅        | ✅     | ✅           |                                                                                  |
| [Windy.com](https://www.windy.com/)                                  | ✅             | ✅           | ✅    | ✅      | ✅        | ✅     | ✅           | Extreme forecast, Weather warnings, Outdoor map, Drought monitoring, Fire danger |
