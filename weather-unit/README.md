# Breezy Weather unit conversion and formatting library

Android library to handle:

- Unit conversion, with syntax inspired by [Kotlin duration](https://kotlinlang.org/docs/time-measurement.html#time-sources).
- Unit formatting in various languages, including on devices without ICU support or with missing CLDR data, with a simplified backport (no handling of plural and non-nominative rules)
- Number formatting
- Computing of some common weather data (apparent temperature, dew point, etc)

Remains to do:

- Complete unit testing
- Plus and minus operations
- Parse from string


# Summary of supported units

Android translations are provided without plural rules.

## Temperature

| Unit               | `NumberFormatter` | `MeasureFormat` | Android translations |
|--------------------|-------------------|-----------------|----------------------|
| Decidegree Celsius | ❌                 | ❌               | ✅¹                   |
| Degree Celsius     | Android >= 11     | Android 7 to 10 | Android < 7          |
| Degree Fahrenheit  | Android >= 11     | Android 7 to 10 | Android < 7          |
| Kelvin             | Android >= 11     | Android 7 to 10 | Android < 7          |

* ¹ Only English, French and Esperanto translations are provided.

Supports temperature deviations conversions (such as degree days).

Supported widths for Android translations:

| Narrow | Short | Long |
|--------|-------|------|
| ❌      | ✅     | ✅    |


## Distance

| Unit          | `NumberFormatter` | `MeasureFormat` | Android translations |
|---------------|-------------------|-----------------|----------------------|
| Meter         | Android >= 11     | Android 7 to 10 | Android < 7          |
| Kilometer     | Android >= 11     | Android 7 to 10 | Android < 7          |
| Mile          | Android >= 11     | Android 7 to 10 | Android < 7          |
| Nautical mile | Android >= 11     | Android 7 to 10 | Android < 7          |
| Foot          | Android >= 11     | Android 7 to 10 | Android < 7          |

Supported widths for Android translations:

| Narrow | Short | Long |
|--------|-------|------|
| ❌      | ✅     | ✅    |


## Speed

| Unit                  | `NumberFormatter` | `MeasureFormat` | Android translations |
|-----------------------|-------------------|-----------------|----------------------|
| Centimeter per second | Android >= 11     | Android 8 to 10 | Android < 8          |
| Meter per second¹     | Android >= 11     | Android 7 to 10 | Android < 7          |
| Kilometer per hour¹   | Android >= 11     | Android 7 to 10 | Android < 7          |
| Mile per hour¹        | Android >= 11     | Android 7 to 10 | Android < 7          |
| Knot                  | Android >= 11     | Android 8 to 10 | Android < 8          |
| Foot per second       | Android >= 11     | Android 7 to 10 | Android < 7          |
| Beaufort scale²       | Android >= 16     | ❌               | Android < 16         |

* ¹ Simplified backport where the “per unit” is combined with the duration unit below, so that for example “Mile per hour” in short width looks like “mi/h” instead of the “mph” used in some countries
* ² Not an unit, but a scale, so during conversions, uses the starting value in meters per second of the scale level

Supported widths for Android translations:

| Narrow | Short | Long |
|--------|-------|------|
| ❌      | ✅     | ✅    |


## Precipitation

| Unit                   | `NumberFormatter` | `MeasureFormat` | Android translations |
|------------------------|-------------------|-----------------|----------------------|
| Micrometer             | Android >= 11     | Android 7 to 10 | Android < 7          |
| Millimeter             | Android >= 11     | Android 7 to 10 | Android < 7          |
| Centimeter             | Android >= 11     | Android 7 to 10 | Android < 7          |
| Inch                   | Android >= 11     | Android 7 to 10 | Android < 7          |
| Liter per square meter | Android >= 11     | Android 8 to 10 | Android < 8          |

| Unit                            | `NumberFormatter` | `MeasureFormat` | Android translations |
|---------------------------------|-------------------|-----------------|----------------------|
| Micrometer per hour             | Android >= 11     | Android 8 to 10 | Android < 8          |
| Millimeter per hour             | Android >= 11     | Android 8 to 10 | Android < 8          |
| Centimeter per hour             | Android >= 11     | Android 8 to 10 | Android < 8          |
| Inch per hour                   | Android >= 11     | Android 8 to 10 | Android < 8          |
| Liter per square meter per hour | ❌                 | ❌               | ✅                    |

Supported widths for Android translations:

| Narrow | Short | Long |
|--------|-------|------|
| ❌      | ✅     | ✅    |


## Pressure

| Unit                  | `NumberFormatter` | `MeasureFormat` | Android translations |
|-----------------------|-------------------|-----------------|----------------------|
| Pascal                | ❌                 | ❌               | ✅                    |
| Hectopascal           | Android >= 11     | Android 7 to 10 | Android < 7          |
| Kilopascal            | ❌                 | ❌               | ✅                    |
| Atmosphere            | Android >= 11     | ❌               | Android < 11         |
| Millimeter of mercury | Android >= 11     | Android 7 to 10 | Android < 7          |
| Inch of mercury       | Android >= 11     | Android 7 to 10 | Android < 7          |

Supported widths for Android translations:

| Narrow | Short | Long |
|--------|-------|------|
| ❌      | ✅     | ✅    |


## Air pollutant concentration

| Unit                      | `NumberFormatter` | `MeasureFormat` | Android translations |
|---------------------------|-------------------|-----------------|----------------------|
| Microgram per cubic meter | Android >= 11     | Android 8 to 10 | Android < 8          |
| Milligram per cubic meter | Android >= 11     | Android 8 to 10 | Android < 8          |

Supported widths for Android translations:

| Narrow | Short | Long |
|--------|-------|------|
| ❌      | ✅     | ✅    |


## Pollen concentration

| Unit            | `NumberFormatter` | `MeasureFormat` | Android translations |
|-----------------|-------------------|-----------------|----------------------|
| Per cubic meter | ❌                 | ❌               | ✅                    |

Supported widths for Android translations:

| Narrow | Short | Long |
|--------|-------|------|
| ❌      | ✅     | ✅    |


## Duration

| Unit        | `NumberFormatter`¹ | `MeasureFormat` | Android translations |
|-------------|--------------------|-----------------|----------------------|
| Nanosecond  | Android >= 11      | Android 7 to 10 | Android < 7²         |
| Microsecond | Android >= 11      | Android 7 to 10 | Android < 7²         |
| Millisecond | Android >= 11      | Android 7 to 10 | Android < 7²         |
| Second      | Android >= 11      | Android 7 to 10 | Android < 7          |
| Minute      | Android >= 11      | Android 7 to 10 | Android < 7          |
| Hour        | Android >= 11      | Android 7 to 10 | Android < 7          |
| Day         | Android >= 11      | Android 7 to 10 | Android < 7²         |

* ¹ `NumberFormatter` supports only single duration, and will not be used when needing a formatting like `1 hour and 30 minutes`.
* ² Only English translations are provided.

Supported widths for Android translations:

| Narrow | Short | Long |
|--------|-------|------|
| ❌      | ✅     | ✅    |


## Ratio

| Unit     | `NumberFormatter`¹ | `NumberFormat` | Android translations |
|----------|--------------------|----------------|----------------------|
| Permille | Android >= 11      | ❌              | Android < 11¹        |
| Percent  | Android >= 11      | Android < 11   | ❌                    |
| Fraction | Android >= 11      | Android < 11   | N/A                  |

* ¹ Only English translations are provided.

Supported widths for `NumberFormat` and Android translations:

| Narrow | Short | Long |
|--------|-------|------|
| ❌      | ✅     | ❌    |


# License

- This library: GNU Lesser General Public License v3.0
- The Unicode CLDR data: Copyright © 1991-Present Unicode, Inc. [Unicode License v3](https://www.unicode.org/license.txt)
