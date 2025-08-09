# Maps utils

Fork of [Maps SDK for Android Utility Library](https://github.com/googlemaps/android-maps-utils).
Includes code up to the [2025-08-06 commit](https://github.com/googlemaps/android-maps-utils/commit/014c64e7d06c98864092b58f701b20e301ef8d30).

Differences with original library:
- Only supports a small subset of features we need for Breezy Weather
- Add an utility to decode [polylines](https://developers.google.com/maps/documentation/utilities/polylinealgorithm?hl=en) (no longer used by Breezy Weather, but still there just in case)
- No dependency on proprietary Google Play Services
- Rewritten in Kotlin
