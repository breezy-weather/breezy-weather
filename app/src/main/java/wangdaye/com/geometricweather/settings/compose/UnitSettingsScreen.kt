package wangdaye.com.geometricweather.settings.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options.unit.*
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.settings.preference.PreferenceScreen
import wangdaye.com.geometricweather.settings.preference.generateListPreferenceModel

@Composable
fun UnitSettingsScreen(
    context: Context
) = PreferenceScreen(
    modelList = listOf(
        generateListPreferenceModel(
            titleId = R.string.settings_title_temperature_unit,
            selectedKey = SettingsManager.getInstance(context).temperatureUnit.id,
            valueArrayId = R.array.temperature_unit_values,
            nameArrayId = R.array.temperature_units,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .temperatureUnit = TemperatureUnit.getInstance(it)
            },
        ),
        generateListPreferenceModel(
            titleId = R.string.settings_title_distance_unit,
            selectedKey = SettingsManager.getInstance(context).distanceUnit.id,
            valueArrayId = R.array.distance_unit_values,
            nameArrayId = R.array.distance_units,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .distanceUnit = DistanceUnit.getInstance(it)
            },
        ),
        generateListPreferenceModel(
            titleId = R.string.settings_title_precipitation_unit,
            selectedKey = SettingsManager.getInstance(context).precipitationUnit.id,
            valueArrayId = R.array.precipitation_unit_values,
            nameArrayId = R.array.precipitation_units,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .precipitationUnit = PrecipitationUnit.getInstance(it)
            },
        ),
        generateListPreferenceModel(
            titleId = R.string.settings_title_pressure_unit,
            selectedKey = SettingsManager.getInstance(context).pressureUnit.id,
            valueArrayId = R.array.pressure_unit_values,
            nameArrayId = R.array.pressure_units,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .pressureUnit = PressureUnit.getInstance(it)
            },
        ),
        generateListPreferenceModel(
            titleId = R.string.settings_title_speed_unit,
            selectedKey = SettingsManager.getInstance(context).speedUnit.id,
            valueArrayId = R.array.speed_unit_values,
            nameArrayId = R.array.speed_units,
            onValueChanged = {
                SettingsManager
                    .getInstance(context)
                    .speedUnit = SpeedUnit.getInstance(it)
            },
        )
    ).map {
        mutableStateOf(it)
    }
)