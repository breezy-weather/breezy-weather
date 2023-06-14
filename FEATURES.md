# Features

This page intends to give some explanations to how some features of Breezy Weather work.

## Air quality

Air quality uses the [2023 Plume index](https://plumelabs.files.wordpress.com/2023/06/plume_aqi_2023.pdf) as a reference, and for SO2 and CO, it uses a similar scale based on [WHO recommendations from 2021](https://apps.who.int/iris/handle/10665/345329).

Here is the meaning of each category:

| Range    | Title          | Harmless exposure  |
|----------|----------------|--------------------|
| 0-20     | Excellent      | &gt; 1 year        |
| 20-50    | Fair           | &lt; 1 year        |
| 50-100   | Poor           | &lt; 1 day         |
| 100-150  | Unhealthy      | &lt; 1 hour        |
| 150-250  | Very unhealthy | &lt; a few minutes |
| &gt; 250 | Dangerous      | &lt; 1 minutes     |

Here are the thresholds:

| Pollutant | AQI 20 | AQI 50 | AQI 100 | AQI 150 | AQI 250 |
|-----------|--------|--------|---------|---------|---------|
| O3        | 50     | 100    | 160     | 240     | 480     |
| NO2       | 10     | 25     | 200     | 400     | 1000    |
| PM10      | 15     | 45     | 80      | 160     | 400     |
| PM2.5     | 5      | 15     | 30      | 60      | 150     |
| SO2       | 20     | 40     | 270     | 500     | 960     |
| CO        | 2      | 4      | 35      | 100     | 230     |

AQIs above 250 follow a linear progression.

An AQI is calculated for O3, NO2, PM10 and PM2.5 pollutant, and the general AQI is the maximum value of these four.

On the left, the ¾ circle shows the general AQI. On the right, you can see details of pollutants. Color bar is filled by the AQI value of the pollutant and not its measured value (to avoid having insignificant low-filled bars in non-polluted places in the world).

Both the circle and bars are filled with a different color for each category, so you can easily see the pollution level. The maximum value for the circle and bars is 250. At greater value, widgets will always be fulfilled.