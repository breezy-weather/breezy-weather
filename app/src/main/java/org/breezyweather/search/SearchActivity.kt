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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.source.LocationSearchSource
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
        val dialogOpenState = remember { mutableStateOf(false) }
        var text by rememberSaveable { mutableStateOf("") }
        var latestTextSearch by rememberSaveable { mutableStateOf("") }
        val enabledSourceState = viewModel.enabledSource.collectAsState()
        val weatherSource = sourceManager.getWeatherSourceOrDefault(enabledSourceState.value)
        val locationSearchSource = if (weatherSource !is LocationSearchSource) {
            sourceManager.getDefaultLocationSearchSource()
        } else weatherSource

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
                                    text = stringResource(R.string.weather_data_by).replace("$", weatherSource.name),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(weatherSource.color)
                                )
                                if (locationSearchSource.locationSearchAttribution != weatherSource.name) {
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                                    Text(
                                        text = stringResource(R.string.location_results_by).replace("$", locationSearchSource.locationSearchAttribution),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DayNightTheme.colors.bodyColor
                                    )
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { dialogOpenState.value = true },
                            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                        ) {
                            Icon(Icons.Filled.Tune, stringResource(R.string.location_search_change_weather_source))
                        }
                    }
                )
            }
        ) { paddings ->
            Surface(
                //shape = shape,
                color = SearchBarDefaults.colors().containerColor,
                contentColor = contentColorFor(SearchBarDefaults.colors().containerColor),
                tonalElevation = SearchBarDefaults.Elevation,
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
                    Divider(color = SearchBarDefaults.colors().dividerColor)
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
                                    headlineContent = { Text(location.place()) },
                                    supportingContent = { Text(location.administrationLevels()) },
                                    modifier = Modifier.clickable {
                                        finishSelf(location)
                                    }
                                )
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
                                    text = stringResource(R.string.location_search_no_results)
                                        .replace("$1", locationSearchSource.name)
                                        .replace("$2", latestTextSearch),
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

        if (dialogOpenState.value) {
            val uriHandler = LocalUriHandler.current
            AlertDialog(
                onDismissRequest = { dialogOpenState.value = false },
                title = {
                    Text(
                        text = stringResource(R.string.location_search_weather_source),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(sourceManager.getWeatherSources()) {
                            RadioButton(
                                selected = weatherSource.id == it.id,
                                onClick = {
                                    dialogOpenState.value = false
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
                        onClick = { dialogOpenState.value = false }
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { uriHandler.openUri("https://github.com/breezy-weather/breezy-weather/blob/main/docs/PROVIDERS.md") }
                    ) {
                        Text(
                            text = stringResource(R.string.action_help_me_choose),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            )
        }
    }

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