package wangdaye.com.geometricweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import wangdaye.com.geometricweather.BuildConfig
import wangdaye.com.geometricweather.GeometricWeather.Companion.instance
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options.provider.LocationProvider
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper
import wangdaye.com.geometricweather.db.repositories.LocationEntityRepository
import wangdaye.com.geometricweather.db.repositories.WeatherEntityRepository
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.settings.preference.bottomInsetItem
import wangdaye.com.geometricweather.settings.preference.clickablePreferenceItem
import wangdaye.com.geometricweather.settings.preference.composables.ListPreferenceView
import wangdaye.com.geometricweather.settings.preference.composables.PreferenceScreen
import wangdaye.com.geometricweather.settings.preference.composables.PreferenceView
import wangdaye.com.geometricweather.settings.preference.listPreferenceItem

@Composable
fun ServiceProviderSettingsScreen(
    context: Context,
    navController: NavHostController,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    listPreferenceItem(R.string.settings_title_weather_source) { id ->
        ListPreferenceView(
            titleId = id,
            valueArrayId = R.array.weather_source_values,
            nameArrayId = R.array.weather_sources,
            selectedKey = SettingsManager.getInstance(context).weatherSource.id,
            onValueChanged = { sourceId ->
                SettingsManager
                    .getInstance(context)
                    .weatherSource = WeatherSource.getInstance(sourceId)

                val locationList = LocationEntityRepository.readLocationList()
                val index = locationList.indexOfFirst { it.isCurrentPosition }
                if (index >= 0) {
                    locationList[index] = locationList[index].copy(
                        weather = null,
                        weatherSource = SettingsManager.getInstance(context).weatherSource
                    ).copy()
                    WeatherEntityRepository.deleteWeather(locationList[index])
                    LocationEntityRepository.writeLocationList(locationList)
                }
            }
        )
    }

    listPreferenceItem(R.string.settings_title_location_service) { id ->
        // read configuration from data store.
        var currentSelectedKey = SettingsManager.getInstance(context).locationProvider.id
        var valueList = stringArrayResource(R.array.location_service_values)
        var nameList = stringArrayResource(R.array.location_services)

        // clear invalid config by build flavor.
        if (BuildConfig.FLAVOR.contains("fdroid")
            || BuildConfig.FLAVOR.contains("gplay")) {
            valueList = arrayOf(valueList[1], valueList[3])
            nameList = arrayOf(nameList[1], nameList[3])
        }
        if (!valueList.contains(currentSelectedKey)) {
            currentSelectedKey = LocationProvider.NATIVE.id
        }

        ListPreferenceView(
            title = stringResource(id),
            summary = { _, key -> nameList[valueList.indexOfFirst { it == key }] },
            selectedKey = currentSelectedKey,
            valueArray = valueList,
            nameArray = nameList,
            onValueChanged = { sourceId ->
                SettingsManager
                    .getInstance(context)
                    .locationProvider = LocationProvider.getInstance(sourceId)

                SnackbarHelper.showSnackbar(
                    context.getString(R.string.feedback_restart),
                    context.getString(R.string.restart)
                ) {
                    instance.recreateAllActivities()
                }
            }
        )
    }

    clickablePreferenceItem(R.string.settings_title_service_provider_advanced) {
        PreferenceView(
            titleId = it,
            summaryId = R.string.settings_summary_service_provider_advanced,
        ) {
            navController.navigate(SettingsScreenRouter.ServiceProviderAdvanced.route)
        }
    }

    bottomInsetItem()
}