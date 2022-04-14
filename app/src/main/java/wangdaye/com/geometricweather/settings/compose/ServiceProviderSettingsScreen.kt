package wangdaye.com.geometricweather.settings.compose

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import wangdaye.com.geometricweather.BuildConfig
import wangdaye.com.geometricweather.GeometricWeather.Companion.instance
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options.provider.LocationProvider
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper
import wangdaye.com.geometricweather.db.DatabaseHelper
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.settings.preference.ListPreferenceView
import wangdaye.com.geometricweather.settings.preference.PreferenceView

@Composable
fun ServiceProviderSettingsScreen(
    context: Context,
    navController: NavHostController
) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
) {
    ListPreferenceView(
        title = stringResource(R.string.settings_title_weather_source),
        summary = { context, value ->
            val values = context.resources.getStringArray(R.array.weather_source_values)
            val names = context.resources.getStringArray(R.array.weather_sources)

            names[values.indexOfFirst { it == value }]
        },
        selectedKey = SettingsManager.getInstance(context).weatherSource.id,
        valueArray = stringArrayResource(R.array.weather_source_values),
        nameArray = stringArrayResource(R.array.weather_sources),
        onValueChanged = { sourceId ->
            SettingsManager
                .getInstance(context)
                .weatherSource = WeatherSource.getInstance(sourceId)

            val locationList = DatabaseHelper.getInstance(context).readLocationList()
            val index = locationList.indexOfFirst { it.isCurrentPosition }
            if (index >= 0) {
                locationList[index] = locationList[index].copy(
                    weather = null,
                    weatherSource = SettingsManager.getInstance(context).weatherSource
                ).copy()
                DatabaseHelper.getInstance(context).deleteWeather(locationList[index])
                DatabaseHelper.getInstance(context).writeLocationList(locationList)
            }
        }
    )

    // read configuration from data store.
    var currentSelectedKey = SettingsManager.getInstance(context).locationProvider.id
    var valueList = stringArrayResource(R.array.location_service_values)
    var nameList = stringArrayResource(R.array.location_services)

    // clear invalid config by build flavor.
    if (BuildConfig.FLAVOR.contains("fdroid")) {
        // Remove closed source providers if building the F-Droid flavor
        valueList = arrayOf(valueList[1], valueList[3])
        nameList = arrayOf(nameList[1], nameList[3])
    } else if (BuildConfig.FLAVOR.contains("gplay")) {
        // Remove closed source providers if building the Google Play flavor
        valueList = arrayOf(valueList[0], valueList[1], valueList[3])
        nameList = arrayOf(valueList[0], nameList[1], nameList[3])
    }
    if (!valueList.contains(currentSelectedKey)) {
        currentSelectedKey = LocationProvider.NATIVE.id
    }

    ListPreferenceView(
        title = stringResource(R.string.settings_title_location_service),
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

    PreferenceView(
        title = stringResource(R.string.settings_title_service_provider_advanced),
        summary = stringResource(R.string.settings_summary_service_provider_advanced),
    ) {
        navController.navigate(SettingsScreenRouter.ServiceProviderAdvanced.route)
    }

    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
}