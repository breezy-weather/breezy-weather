package wangdaye.com.geometricweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options.unit.AirQualityAlgorithmUnit
import wangdaye.com.geometricweather.common.basic.models.options.unit.DistanceUnit
import wangdaye.com.geometricweather.common.basic.models.options.unit.PrecipitationUnit
import wangdaye.com.geometricweather.common.basic.models.options.unit.PressureUnit
import wangdaye.com.geometricweather.common.basic.models.options.unit.SpeedUnit
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.settings.preference.composables.ListPreferenceView
import wangdaye.com.geometricweather.settings.preference.composables.PreferenceScreen
import wangdaye.com.geometricweather.settings.preference.listPreferenceItem

@Composable
fun UnitSettingsScreen(
    context: Context,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    listPreferenceItem(R.string.settings_title_temperature_unit) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).temperatureUnit.id,
            valueArrayId = R.array.temperature_unit_values,
            nameArrayId = R.array.temperature_units,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .temperatureUnit = TemperatureUnit.getInstance(it)
            },
        )
    }
    listPreferenceItem(R.string.settings_title_distance_unit) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).distanceUnit.id,
            valueArrayId = R.array.distance_unit_values,
            nameArrayId = R.array.distance_units,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .distanceUnit = DistanceUnit.getInstance(it)
            },
        )
    }
    listPreferenceItem(R.string.settings_title_precipitation_unit) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).precipitationUnit.id,
            valueArrayId = R.array.precipitation_unit_values,
            nameArrayId = R.array.precipitation_units,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .precipitationUnit = PrecipitationUnit.getInstance(it)
            },
        )
    }
    listPreferenceItem(R.string.settings_title_pressure_unit) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).pressureUnit.id,
            valueArrayId = R.array.pressure_unit_values,
            nameArrayId = R.array.pressure_units,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .pressureUnit = PressureUnit.getInstance(it)
            },
        )
    }
    listPreferenceItem(R.string.settings_title_speed_unit) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).speedUnit.id,
            valueArrayId = R.array.speed_unit_values,
            nameArrayId = R.array.speed_units,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .speedUnit = SpeedUnit.getInstance(it)
            },
        )
    }
    listPreferenceItem(R.string.settings_title_air_quality_algorithm) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).airQualityAlgorithmUnit.id,
            valueArrayId = R.array.air_quality_levels_values,
            nameArrayId = R.array.air_quality_levels,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .airQualityAlgorithmUnit = AirQualityAlgorithmUnit.getInstance(it)
            },
        )
    }
}