package wangdaye.com.geometricweather.wallpaper

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.toSize
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import wangdaye.com.geometricweather.theme.compose.DayNightTheme
import wangdaye.com.geometricweather.theme.compose.GeometricWeatherTheme
import wangdaye.com.geometricweather.theme.compose.rememberThemeRipple

class LiveWallpaperConfigActivity : GeoActivity() {

    private lateinit var weatherKindValueNow: MutableState<String>
    private lateinit var weatherKinds: Array<String>
    private lateinit var weatherKindValues: Array<String>

    private lateinit var dayNightTypeValueNow: MutableState<String>
    private lateinit var dayNightTypeKinds: Array<String>
    private lateinit var dayNightTypeValues: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weatherKindValueNow = mutableStateOf(
            LiveWallpaperConfigManager.getInstance(this).weatherKind
        )
        weatherKinds = resources.getStringArray(R.array.live_wallpaper_weather_kinds)
        weatherKindValues = resources.getStringArray(R.array.live_wallpaper_weather_kind_values)

        dayNightTypeValueNow = mutableStateOf(
            LiveWallpaperConfigManager.getInstance(this).dayNightType
        )
        dayNightTypeKinds = resources.getStringArray(R.array.live_wallpaper_day_night_types)
        dayNightTypeValues = resources.getStringArray(R.array.live_wallpaper_day_night_type_values)

        setContent {
            GeometricWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }
    }

    @Composable
    private fun ContentView() {
        Scaffold(
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.settings_title_live_wallpaper),
                    onBackPressed = { finish() },
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                items(1) {
                    Spinner(
                        currentVal = weatherKindValueNow,
                        names = weatherKinds,
                        values = weatherKindValues,
                        titleId = R.string.feedback_live_wallpaper_weather_kind,
                    )
                    Spinner(
                        currentVal = dayNightTypeValueNow,
                        names = dayNightTypeKinds,
                        values = dayNightTypeValues,
                        titleId = R.string.feedback_live_wallpaper_day_night_type,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.normal_margin)),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Button(
                            onClick = {
                                LiveWallpaperConfigManager.update(
                                    this@LiveWallpaperConfigActivity,
                                    weatherKindValueNow.value,
                                    dayNightTypeValueNow.value,
                                )
                                finish()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.done),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Spinner(
        currentVal: MutableState<String>,
        names: Array<String>,
        values: Array<String>,
        @StringRes titleId: Int,
    ) {
        val expanded = remember { mutableStateOf(false) }
        val textFieldSize = remember { mutableStateOf(Size.Zero) }

        val icon = if (expanded.value) {
            Icons.Filled.ArrowDropUp
        } else {
            Icons.Filled.ArrowDropDown
        }
        val label = stringResource(titleId)

        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))
        ) {
            OutlinedTextField(
                value = names[values.indexOf(currentVal.value)],
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldSize.value = coordinates.size.toSize()
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberThemeRipple(),
                        onClick = { expanded.value = !expanded.value },
                    ),
                label = {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            expanded.value = !expanded.value
                        },
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                },
                readOnly = true,
                enabled = false,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
            )
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                modifier = Modifier
                    .width(with(LocalDensity.current){ textFieldSize.value.width.toDp() })
            ) {
                names.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item,
                                color = DayNightTheme.colors.titleColor,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        onClick = {
                            currentVal.value = values[index]
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}