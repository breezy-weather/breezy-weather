# Breezy Weather unit conversion and formatting library

Android library to handle:
- Unit conversion
- Formatting in various languages, including on devices without ICU support or with missing CLDR data, with a simplified backport (no handling of plural and non-nominative rules)

Syntax inspired by [Kotlin duration](https://kotlinlang.org/docs/time-measurement.html#time-sources).

Some precision may be lost during conversions.

Very early work

Remains to do :

- Speed unit
- Temperature unit
- Air quality concentration unit
- Pollen concentration unit
- Add missing non-English Android translations (for the units we use in Breezy Weather)
- Plus and minus operations
- Parse from string


# Summary of supported units

Android translations are provided without plural rules.

## Distance

Android translations are only in English at the moment.

| Unit          | `NumberFormatter` | `MeasureFormat` | Android translations |
|---------------|-------------------|-----------------|----------------------|
| Meter         | Android >= 11     | Android 7 to 10 | Android < 7          |
| Kilometer     | Android >= 11     | Android 7 to 10 | Android < 7          |
| Mile          | Android >= 11     | Android 7 to 10 | Android < 7          |
| Nautical mile | Android >= 11     | Android 7 to 10 | Android < 7          |
| Foot          | Android >= 11     | Android 7 to 10 | Android < 7          |


## Precipitation

Android translations are only in English at the moment.

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


## Pressure

| Unit                  | `NumberFormatter` | `MeasureFormat` | Android translations |
|-----------------------|-------------------|-----------------|----------------------|
| Pascal                | ❌                 | ❌               | ✅                    |
| Hectopascal           | Android >= 11     | Android 7 to 10 | Android < 7          |
| Kilopascal            | ❌                 | ❌               | ✅                    |
| Atmosphere            | Android >= 11     | ❌               | Android < 11         |
| Millimeter of mercury | Android >= 11     | Android 7 to 10 | Android < 7          |
| Inch of mercury       | Android >= 11     | Android 7 to 10 | Android < 7          |


## Air pollutant concentration

Android translations are only in English at the moment.

| Unit                      | `NumberFormatter` | `MeasureFormat` | Android translations |
|---------------------------|-------------------|-----------------|----------------------|
| Microgram per cubic meter | Android >= 11     | Android 8 to 10 | Android < 8          |
| Milligram per cubic meter | Android >= 11     | Android 8 to 10 | Android < 8          |


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


# License

- This library: GNU Lesser General Public License v3.0
- The Unicode CLDR data: Copyright © 1991-Present Unicode, Inc. [Unicode License v3](https://www.unicode.org/license.txt)
