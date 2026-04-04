# OACP Integration — Breezy Weather

How voice control was added to Breezy Weather using the OACP Kotlin SDK.

## Overview

Three capabilities exposed via OACP:

| Capability | Dispatch | What it does |
|-----------|----------|-------------|
| `check_weather` | **broadcast** | Returns current weather (background, no UI) |
| `check_forecast` | **broadcast** | Returns forecast data (background, no UI) |
| `open_weather` | **activity** | Opens the Breezy Weather app |

## Architecture

This app demonstrates the **broadcast-first** pattern. Most actions return data without opening the app. Only `open_weather` needs the UI.

- **Broadcast actions** → `OacpReceiver` subclass handles in background, returns `OacpResult` with data
- **Activity action** → Intent filter on main Activity, Hark calls `startActivity()` directly

## Files added

| File | Purpose |
|------|---------|
| `app/libs/oacp-android-release.aar` | OACP Kotlin SDK |
| `app/src/main/assets/oacp.json` | Capability manifest with rich metadata |
| `app/src/main/assets/OACP.md` | LLM context for disambiguation |
| `app/src/main/java/.../oacp/OacpActionReceiver.kt` | Broadcast handler |

## Files modified

### `app/build.gradle.kts`
```kotlin
implementation(files("libs/oacp-android-release.aar"))
implementation("androidx.annotation:annotation:1.7.1")
```

### `AndroidManifest.xml`

Activity intent filter for `open_weather`:
```xml
<intent-filter>
    <action android:name="${applicationId}.oacp.ACTION_OPEN_WEATHER" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

Receiver for broadcast actions:
```xml
<receiver android:name=".oacp.OacpActionReceiver" android:exported="true">
    <intent-filter>
        <action android:name="${applicationId}.oacp.ACTION_CHECK_WEATHER" />
        <action android:name="${applicationId}.oacp.ACTION_CHECK_FORECAST" />
    </intent-filter>
</receiver>
```

### `OacpActionReceiver.kt`
```kotlin
class OacpActionReceiver : OacpReceiver() {
    override fun onAction(context: Context, action: String, params: OacpParams, requestId: String?): OacpResult? {
        return when {
            action.endsWith(".oacp.ACTION_CHECK_WEATHER") -> {
                val location = params.getString("location")
                // TODO: Query real weather data from app's database
                OacpResult.success("Current weather for ${location ?: "current location"}")
            }
            action.endsWith(".oacp.ACTION_CHECK_FORECAST") -> {
                val location = params.getString("location")
                OacpResult.success("Forecast for ${location ?: "current location"}")
            }
            else -> null
        }
    }
}
```

## Testing

```bash
# Verify manifest
adb shell content read --uri "content://org.breezyweather.debug.oacp/manifest"

# Test broadcast (background)
adb shell am broadcast -a org.breezyweather.debug.oacp.ACTION_CHECK_WEATHER -p org.breezyweather.debug

# Test activity (opens app)
adb shell am start -a org.breezyweather.debug.oacp.ACTION_OPEN_WEATHER -n org.breezyweather.debug/.main.MainActivity
```

## Next steps

- Integrate real weather data queries in `OacpActionReceiver` (read from app's local database/cache)
- Return structured weather data (temperature, conditions, humidity) in `OacpResult`
