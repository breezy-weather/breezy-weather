# Day details

*To be completed*

## Degree day

Degree day is basically the heat or cool need of a building.

This need will vary depending on where you live, on outdoor temperature, on quality of your building, etc.

If the weather source doesn’t have this info available, we will calculate using a generic formula (the one used in EU) that way:
- If the daily mean outdoor air temperature ((min + max of the day)/2) is between 15 °C and 24 °C, then there is no heating or cooling degree day.
- If daily mean is < 15 °C, heating degree day = 18 °C - daily mean. For example, if daily mean is 13 °C, heating degree day = 5 °C
- If daily mean is > 24 °C, cooling degree day = daily mean - 21 °C. For example, if daily mean is 27 °C, cooling degree day = 6 °C

While the calculation is made using °C, this value is expressed in the unit you selected in preference.

More info: https://ec.europa.eu/eurostat/statistics-explained/index.php?title=Heating_and_cooling_degree_days_-_statistics