/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.search

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.extensions.inputMethodManager
import org.breezyweather.domain.location.model.applyDefaultPreset
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.sources.SourceManager
import org.breezyweather.sources.getConfiguredLocationSearchSources
import org.breezyweather.sources.getLocationSearchSourceOrDefault
import org.breezyweather.sources.getWeatherSource
import org.breezyweather.ui.common.composables.AlertDialogNoPadding
import org.breezyweather.ui.common.composables.SecondarySourcesPreference
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.Material3SearchBarInputField
import org.breezyweather.ui.settings.preference.composables.RadioButton
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : BreezyActivity() {
    private lateinit var viewModel: SearchViewModel

    @Inject lateinit var sourceManager: SourceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initModel()
        setContent {
            BreezyWeatherTheme {
                ContentView()
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    @Composable
    private fun ContentView() {
        val context = LocalContext.current
        val dialogLocationSearchSourceOpenState = rememberSaveable { mutableStateOf(false) }
        val dialogLocationSourcesOpenState = rememberSaveable { mutableStateOf(false) }
        var selectedLocation: Location? by rememberSaveable { mutableStateOf(null) }
        var text by rememberSaveable { mutableStateOf("") }
        var latestTextSearch by rememberSaveable { mutableStateOf("") }
        val locationSearchSourceState = viewModel.locationSearchSource.collectAsState()
        val locationSearchSource = sourceManager.getLocationSearchSourceOrDefault(locationSearchSourceState.value)

        Material3Scaffold(
            bottomBar = {
                BottomAppBar(
                    windowInsets = WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    ),
                    actions = {
                        Box(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp).weight(100f)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(
                                        R.string.location_results_by,
                                        locationSearchSource.locationSearchAttribution
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    floatingActionButton = if (BuildConfig.FLAVOR != "freenet") {
                        {
                            FloatingActionButton(
                                onClick = { dialogLocationSearchSourceOpenState.value = true },
                                containerColor = FloatingActionButtonDefaults.containerColor,
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                            ) {
                                Icon(Icons.Filled.Tune, stringResource(R.string.location_search_change_source))
                            }
                        }
                    } else {
                        null
                    }
                )
            }
        ) { paddings ->
            Surface(
                // shape = shape,
                color = SearchBarDefaults.colors().containerColor,
                contentColor = contentColorFor(SearchBarDefaults.colors().containerColor),
                tonalElevation = SearchBarDefaults.TonalElevation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(paddings)
                ) {
                    Material3SearchBarInputField(
                        query = text,
                        onSearch = {
                            if (it.isNotEmpty()) {
                                hideKeyboard()
                                viewModel.requestLocationList(it)
                                latestTextSearch = it
                            }
                        },
                        onQueryChange = { text = it },
                        active = true,
                        onActiveChange = {
                            if (!it) finishSelf(null)
                        },
                        placeholder = { Text(stringResource(R.string.location_search_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    HorizontalDivider(color = SearchBarDefaults.colors().dividerColor)
                    val listResourceState = viewModel.listResource.collectAsState()
                    if (listResourceState.value.second == LoadableLocationStatus.LOADING) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    if (listResourceState.value.first.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(listResourceState.value.first) { location ->
                                ListItem(
                                    colors = ListItemDefaults
                                        .colors(containerColor = SearchBarDefaults.colors().containerColor),
                                    headlineContent = { Text(location.getPlace(context)) },
                                    supportingContent = { Text(location.administrationLevels()) },
                                    modifier = Modifier.clickable {
                                        val defaultSource = SettingsManager.getInstance(context).defaultForecastSource

                                        selectedLocation = when (defaultSource) {
                                            "auto" -> location.applyDefaultPreset(sourceManager)
                                            else -> {
                                                val source = sourceManager.getWeatherSource(defaultSource)
                                                if (source == null) {
                                                    location.applyDefaultPreset(sourceManager)
                                                } else {
                                                    location.copy(
                                                        forecastSource = source.id,
                                                        currentSource = if (SourceFeature.CURRENT in
                                                            source.supportedFeatures &&
                                                            source.isFeatureSupportedForLocation(
                                                                location,
                                                                SourceFeature.CURRENT
                                                            )
                                                        ) {
                                                            source.id
                                                        } else {
                                                            null
                                                        },
                                                        airQualitySource = if (SourceFeature.AIR_QUALITY in
                                                            source.supportedFeatures &&
                                                            source.isFeatureSupportedForLocation(
                                                                location,
                                                                SourceFeature.AIR_QUALITY
                                                            )
                                                        ) {
                                                            source.id
                                                        } else {
                                                            null
                                                        },
                                                        pollenSource = if (SourceFeature.POLLEN in
                                                            source.supportedFeatures &&
                                                            source.isFeatureSupportedForLocation(
                                                                location,
                                                                SourceFeature.POLLEN
                                                            )
                                                        ) {
                                                            source.id
                                                        } else {
                                                            null
                                                        },
                                                        minutelySource = if (SourceFeature.MINUTELY in
                                                            source.supportedFeatures &&
                                                            source.isFeatureSupportedForLocation(
                                                                location,
                                                                SourceFeature.MINUTELY
                                                            )
                                                        ) {
                                                            source.id
                                                        } else {
                                                            null
                                                        },
                                                        alertSource = if (SourceFeature.ALERT in
                                                            source.supportedFeatures &&
                                                            source.isFeatureSupportedForLocation(
                                                                location,
                                                                SourceFeature.ALERT
                                                            )
                                                        ) {
                                                            source.id
                                                        } else {
                                                            null
                                                        },
                                                        normalsSource = if (SourceFeature.NORMALS in
                                                            source.supportedFeatures &&
                                                            source.isFeatureSupportedForLocation(
                                                                location,
                                                                SourceFeature.NORMALS
                                                            )
                                                        ) {
                                                            source.id
                                                        } else {
                                                            null
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        dialogLocationSourcesOpenState.value = true
                                    }
                                )
                            }
                        }
                        if (dialogLocationSourcesOpenState.value && selectedLocation != null) {
                            SecondarySourcesPreference(
                                sourceManager = sourceManager,
                                location = selectedLocation!!,
                                onClose = { location: Location? ->
                                    if (location != null) {
                                        finishSelf(location)
                                    }
                                    dialogLocationSourcesOpenState.value = false
                                }
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(dimensionResource(R.dimen.normal_margin))
                        ) {
                            if (latestTextSearch.isNotEmpty() &&
                                listResourceState.value.second == LoadableLocationStatus.SUCCESS
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.location_search_no_results,
                                        locationSearchSource.name,
                                        latestTextSearch
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.small_margin)))
                                Text(
                                    text = stringResource(R.string.location_search_no_results_advice),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (dialogLocationSearchSourceOpenState.value) {
            AlertDialogNoPadding(
                onDismissRequest = { dialogLocationSearchSourceOpenState.value = false },
                title = {
                    Text(
                        text = stringResource(R.string.location_search_source),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(sourceManager.getConfiguredLocationSearchSources()) {
                            RadioButton(
                                selected = locationSearchSource.id == it.id,
                                onClick = {
                                    dialogLocationSearchSourceOpenState.value = false
                                    viewModel.setEnabledSource(it.id)
                                    if (text.isNotEmpty()) {
                                        viewModel.requestLocationList(text)
                                    }
                                },
                                text = it.name
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { dialogLocationSearchSourceOpenState.value = false }
                    ) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishSelf(null)
        }
    }

    // init.
    private fun initModel() {
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
    }

    // control.
    private fun finishSelf(location: Location?) {
        setResult(RESULT_OK, Intent().putExtra(KEY_LOCATION, location))
        ActivityCompat.finishAfterTransition(this)
    }

    private fun hideKeyboard() {
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    companion object {
        const val KEY_LOCATION = "location"
    }
}
