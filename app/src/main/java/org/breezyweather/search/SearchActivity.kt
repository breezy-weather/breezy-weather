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

package org.breezyweather.search

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.theme.compose.BreezyWeatherTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.source.LocationPreset
import org.breezyweather.common.ui.composables.SecondarySourcesPreference
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.Material3SearchBarInputField
import org.breezyweather.settings.preference.composables.RadioButton
import org.breezyweather.sources.SourceManager
import org.breezyweather.theme.compose.DayNightTheme
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : GeoActivity() {
    private lateinit var viewModel: SearchViewModel
    @Inject lateinit var sourceManager: SourceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initModel()
        setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }
    }

    @Composable
    private fun ContentView() {
        val context = LocalContext.current
        val dialogLocationSearchSourceOpenState = remember { mutableStateOf(false) }
        val dialogLocationSourcesOpenState = remember { mutableStateOf(false) }
        var selectedLocation: Location? by rememberSaveable { mutableStateOf(null) }
        var text by rememberSaveable { mutableStateOf("") }
        var latestTextSearch by rememberSaveable { mutableStateOf("") }
        val locationSearchSourceState = viewModel.locationSearchSource.collectAsState()
        val locationSearchSource = sourceManager.getLocationSearchSourceOrDefault(locationSearchSourceState.value)

        Material3Scaffold(
            bottomBar = {
                // Known padding issue when IME is open: https://issuetracker.google.com/issues/249727298
                BottomAppBar(
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
                                    color = DayNightTheme.colors.bodyColor
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { dialogLocationSearchSourceOpenState.value = true },
                            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                        ) {
                            Icon(Icons.Filled.Tune, stringResource(R.string.location_search_change_source))
                        }
                    }
                )
            }
        ) { paddings ->
            Surface(
                //shape = shape,
                color = SearchBarDefaults.colors().containerColor,
                contentColor = contentColorFor(SearchBarDefaults.colors().containerColor),
                tonalElevation = SearchBarDefaults.TonalElevation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    // Known padding issue when IME is open: https://issuetracker.google.com/issues/249727298
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
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(listResourceState.value.first) { location ->
                                ListItem(
                                    headlineContent = { Text(location.getPlace(context)) },
                                    supportingContent = { Text(location.administrationLevels()) },
                                    modifier = Modifier.clickable {
                                        selectedLocation = LocationPreset.getLocationWithPresetApplied(location)
                                        dialogLocationSourcesOpenState.value = true
                                    }
                                )
                            }
                        }
                        if (dialogLocationSourcesOpenState.value && selectedLocation != null) {
                            SecondarySourcesPreference(
                                sourceManager,
                                selectedLocation!!
                            ) { location: Location? ->
                                if (location != null) {
                                    finishSelf(location)
                                }
                                dialogLocationSourcesOpenState.value = false
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(dimensionResource(R.dimen.normal_margin))
                        ) {
                            if (latestTextSearch.isNotEmpty()
                                && listResourceState.value.second == LoadableLocationStatus.SUCCESS
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.location_search_no_results,
                                        locationSearchSource.name,
                                        latestTextSearch
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DayNightTheme.colors.titleColor
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                                Text(
                                    text = stringResource(R.string.location_search_no_results_advice),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DayNightTheme.colors.bodyColor
                                )
                            }
                        }
                    }
                }
            }
        }

        if (dialogLocationSearchSourceOpenState.value) {
            AlertDialog(
                onDismissRequest = { dialogLocationSearchSourceOpenState.value = false },
                title = {
                    Text(
                        text = stringResource(R.string.location_search_source),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
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
                            text = stringResource(R.string.action_cancel),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishSelf(null)
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
            val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    companion object {
        const val KEY_LOCATION = "location"
    }
}