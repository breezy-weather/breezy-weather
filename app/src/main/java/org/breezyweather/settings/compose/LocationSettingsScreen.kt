package org.breezyweather.settings.compose

import android.content.Context
import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.provider.LocationProvider
import org.breezyweather.common.extensions.openApplicationDetailsSettings
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.*
import org.breezyweather.settings.preference.composables.*

@Composable
fun LocationSettingsScreen(
    context: Activity,
    accessCoarseLocationPermissionState: PermissionState,
    accessFineLocationPermissionState: PermissionState,
    accessBackgroundLocationPermissionState: PermissionState,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    sectionHeaderItem(R.string.settings_location_section_general)
    listPreferenceItem(R.string.settings_location_service) { id ->
        ListPreferenceView(
            titleId = id,
            selectedKey = SettingsManager.getInstance(context).locationProvider.id,
            valueArrayId = R.array.location_service_values,
            nameArrayId = R.array.location_services,
            onValueChanged = { sourceId ->
                SettingsManager
                    .getInstance(context)
                    .locationProvider = LocationProvider.getInstance(sourceId)

                SnackbarHelper.showSnackbar(
                    context.getString(R.string.settings_changes_apply_after_restart),
                    context.getString(R.string.action_restart)
                ) {
                    BreezyWeather.instance.recreateAllActivities()
                }
            }
        )
    }
    sectionFooterItem(R.string.settings_location_section_general)

    sectionHeaderItem(R.string.location_service_native)
    clickablePreferenceItem(R.string.settings_location_access_switch_title) { id ->
        PreferenceView(
            titleId = id,
            summaryId = if (accessCoarseLocationPermissionState.status == PermissionStatus.Granted) R.string.settings_location_access_switch_summaryOn else R.string.settings_location_access_switch_summaryOff,
            onClick = {
                if (accessCoarseLocationPermissionState.status != PermissionStatus.Granted) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        accessCoarseLocationPermissionState.launchPermissionRequest()
                    } else {
                        context.openApplicationDetailsSettings()
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.revokeSelfPermissionOnKill(Manifest.permission.ACCESS_COARSE_LOCATION)
                        SnackbarHelper.showSnackbar(context.getString(R.string.settings_location_access_permission_revoked))
                    } else {
                        SnackbarHelper.showSnackbar(context.getString(R.string.settings_location_access_permission_already_granted))
                    }
                }
            }
        )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        clickablePreferenceItem(R.string.settings_location_access_background_title) { id ->
            PreferenceView(
                titleId = id,
                summaryId = if (accessBackgroundLocationPermissionState.status == PermissionStatus.Granted) R.string.settings_location_access_background_summaryOn else R.string.settings_location_access_background_summaryOff,
                enabled = accessCoarseLocationPermissionState.status == PermissionStatus.Granted,
                onClick = {
                    if (accessBackgroundLocationPermissionState.status != PermissionStatus.Granted) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            accessBackgroundLocationPermissionState.launchPermissionRequest()
                        } else {
                            context.openApplicationDetailsSettings()
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.revokeSelfPermissionOnKill(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            SnackbarHelper.showSnackbar(context.getString(R.string.settings_location_access_permission_revoked))
                        } else {
                            SnackbarHelper.showSnackbar(context.getString(R.string.settings_location_access_permission_already_granted))
                        }
                    }
                }
            )
        }
    }
    clickablePreferenceItem(R.string.settings_location_access_precise_title) { id ->
        PreferenceView(
            titleId = id,
            summaryId = if (accessFineLocationPermissionState.status == PermissionStatus.Granted) R.string.settings_location_access_precise_summaryOn else R.string.settings_location_access_precise_summaryOff,
            enabled = accessCoarseLocationPermissionState.status == PermissionStatus.Granted,
            onClick = {
                if (accessFineLocationPermissionState.status != PermissionStatus.Granted) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        accessFineLocationPermissionState.launchPermissionRequest()
                    } else {
                        context.openApplicationDetailsSettings()
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.revokeSelfPermissionOnKill(Manifest.permission.ACCESS_FINE_LOCATION)
                        SnackbarHelper.showSnackbar(context.getString(R.string.settings_location_access_permission_revoked))
                    } else {
                        SnackbarHelper.showSnackbar(context.getString(R.string.settings_location_access_permission_already_granted))
                    }
                }
            }
        )
    }
    sectionFooterItem(R.string.location_service_native)

    sectionHeaderItem(R.string.location_service_baidu_ip)
    editTextPreferenceItem(R.string.settings_location_baidu_ip_location_ak) { id ->
        EditTextPreferenceView(
            titleId = id,
            summary = { context, content ->
                content.ifEmpty {
                    context.getString(R.string.settings_weather_provider_default_value)
                }
            },
            content = SettingsManager.getInstance(context).customBaiduIpLocationAk,
            onValueChanged = {
                SettingsManager.getInstance(context).customBaiduIpLocationAk = it
            }
        )
    }
    sectionFooterItem(R.string.location_service_baidu_ip)

    bottomInsetItem()
}