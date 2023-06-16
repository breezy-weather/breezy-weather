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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.Material3SearchBarInputField
import org.breezyweather.settings.preference.composables.RadioButton
import org.breezyweather.theme.compose.DayNightTheme

@AndroidEntryPoint
class SearchActivity : GeoActivity() {
    private lateinit var viewModel: SearchViewModel

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
        val enabledSourceState = viewModel.enabledSource.collectAsState()

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
                                    text = stringResource(R.string.content_desc_data_by).replace("$", enabledSourceState.value.getName(LocalContext.current)),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(enabledSourceState.value.sourceColor)
                                )
                                if (!enabledSourceState.value.locationProvider.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.little_margin)))
                                    Text(
                                        text = stringResource(R.string.content_desc_location_results_by).replace("$", enabledSourceState.value.locationProvider!!),
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
                            Icon(Icons.Filled.Tune, stringResource(R.string.change_weather_source))
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
                            }
                        },
                        onQueryChange = { text = it },
                        active = true,
                        onActiveChange = {
                            if (!it) finishSelf(null)
                        },
                        placeholder = { Text(stringResource(R.string.feedback_search_location)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    Divider(color = SearchBarDefaults.colors().dividerColor)
                    val listResourceState = viewModel.listResource.collectAsState()
                    if (listResourceState.value.second == LoadableLocationStatus.LOADING) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
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
                }
            }
        }

        if (dialogOpenState.value) {
            AlertDialog(
                onDismissRequest = { dialogOpenState.value = false },
                title = {
                    Text(
                        text = stringResource(R.string.settings_title_weather_source),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(WeatherSource.values()) {
                            RadioButton(
                                selected = enabledSourceState.value.id == it.id,
                                onClick = {
                                    dialogOpenState.value = false
                                    viewModel.setEnabledSource(it)
                                    if (text.isNotEmpty()) {
                                        viewModel.requestLocationList(text)
                                    }
                                },
                                text = it.getName(LocalContext.current)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { dialogOpenState.value = false }
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
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