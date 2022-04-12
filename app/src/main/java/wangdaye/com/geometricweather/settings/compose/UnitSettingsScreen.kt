package wangdaye.com.geometricweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options.unit.*
import wangdaye.com.geometricweather.settings.SettingsManager

@Composable
fun UnitSettingsScreen(context: Context) {
    LazyColumn(
        modifier = Modifier.fillMaxHeight()
    ) {
        item {
            val valueList = stringArrayResource(R.array.temperature_unit_values)
            val nameList = stringArrayResource(R.array.temperature_units)
            ListPreferenceView(
                title = stringResource(R.string.settings_title_temperature_unit),
                summary = { key -> nameList[valueList.indexOfFirst { it == key }] },
                selectedKey = SettingsManager.getInstance(context).temperatureUnit.id,
                keyNamePairList = valueList.zip(nameList),
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .temperatureUnit = TemperatureUnit.getInstance(it)
                }
            )
        }
        item {
            val valueList = stringArrayResource(R.array.distance_unit_values)
            val nameList = stringArrayResource(R.array.distance_units)
            ListPreferenceView(
                title = stringResource(R.string.settings_title_distance_unit),
                summary = { key -> nameList[valueList.indexOfFirst { it == key }] },
                selectedKey = SettingsManager.getInstance(context).distanceUnit.id,
                keyNamePairList = valueList.zip(nameList),
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .distanceUnit = DistanceUnit.getInstance(it)
                }
            )
        }
        item {
            val valueList = stringArrayResource(R.array.precipitation_unit_values)
            val nameList = stringArrayResource(R.array.precipitation_units)
            ListPreferenceView(
                title = stringResource(R.string.settings_title_precipitation_unit),
                summary = { key -> nameList[valueList.indexOfFirst { it == key }] },
                selectedKey = SettingsManager.getInstance(context).precipitationUnit.id,
                keyNamePairList = valueList.zip(nameList),
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .precipitationUnit = PrecipitationUnit.getInstance(it)
                }
            )
        }
        item {
            val valueList = stringArrayResource(R.array.pressure_unit_values)
            val nameList = stringArrayResource(R.array.pressure_units)
            ListPreferenceView(
                title = stringResource(R.string.settings_title_pressure_unit),
                summary = { key -> nameList[valueList.indexOfFirst { it == key }] },
                selectedKey = SettingsManager.getInstance(context).pressureUnit.id,
                keyNamePairList = valueList.zip(nameList),
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .pressureUnit = PressureUnit.getInstance(it)
                }
            )
        }
        item {
            val valueList = stringArrayResource(R.array.speed_unit_values)
            val nameList = stringArrayResource(R.array.speed_units)
            ListPreferenceView(
                title = stringResource(R.string.settings_title_speed_unit),
                summary = { key -> nameList[valueList.indexOfFirst { it == key }] },
                selectedKey = SettingsManager.getInstance(context).speedUnit.id,
                keyNamePairList = valueList.zip(nameList),
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .speedUnit = SpeedUnit.getInstance(it)
                }
            )
        }
        item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
    }
}