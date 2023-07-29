package org.breezyweather.main.adapters.main.holder

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.CallSuper
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import io.github.giangpham96.expandable_text_compose.ExpandableText
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.ui.composables.LocationPreference
import org.breezyweather.main.MainActivity
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.resource.providers.ResourceProvider

class FooterViewHolder(
    private val composeView: ComposeView
) : AbstractMainViewHolder(composeView) {

    @SuppressLint("SetTextI18n")
    @CallSuper
    fun onBindView(
        context: Context, location: Location, provider: ResourceProvider,
        listAnimationEnabled: Boolean, itemAnimationEnabled: Boolean,
        mainWeatherSource: MainWeatherSource?
    ) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val cardMarginsVertical = ThemeManager.getInstance(context)
            .weatherThemeDelegate
            .getHomeCardMargins(context).toFloat()

        val creditsText = StringBuilder()
        creditsText.append(
            context.getString(R.string.weather_data_by)
                .replace("$", mainWeatherSource?.weatherAttribution ?: context.getString(R.string.null_data_text))
        )
        if (mainWeatherSource is SecondaryWeatherSource
            && mainWeatherSource.weatherAttribution != mainWeatherSource.allergenAttribution) {
            creditsText.append("\n")
                .append(
                    context.getString(R.string.weather_air_quality_and_pollen_data_by)
                        .replace("$", mainWeatherSource.allergenAttribution)
                )
        }

        composeView.setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ComposeView((context as MainActivity), location, creditsText.toString(), cardMarginsVertical.toInt())
            }
        }
    }

    @Composable
    fun ComposeView(activity: MainActivity, location: Location, creditsText: String, cardMarginsVertical: Int) {
        var expand by remember { mutableStateOf(false) }
        var dialogOpenState by remember { mutableStateOf(false) }

        val paddingTop = dimensionResource(R.dimen.little_margin) - cardMarginsVertical.dp
        Row(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        start = dimensionResource(R.dimen.normal_margin),
                        top = if (paddingTop > 0.dp) paddingTop else 0.dp,
                        end = dimensionResource(R.dimen.normal_margin),
                        bottom = dimensionResource(R.dimen.little_margin)
                    )
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExpandableText(
                originalText = creditsText,
                expandAction = stringResource(R.string.action_see_more),
                expand = expand,
                color = Color.White,
                expandActionColor = Color.White,
                limitedMaxLines = 3,
                animationSpec = spring(),
                modifier = Modifier
                    .weight(1f)
                    .clickable { expand = !expand }
            )
            TextButton(
                onClick = {
                    dialogOpenState = true
                }
            ) {
                Text(
                    text = stringResource(R.string.action_edit),
                    color = Color.White,
                    fontSize = dimensionResource(id = R.dimen.content_text_size).value.sp
                )
            }
        }

        if (dialogOpenState) {
            AlertDialog(
                onDismissRequest = { dialogOpenState = false },
                title = {
                    Text(
                        text = stringResource(R.string.action_settings),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                text = {
                    LocationPreference(activity, location, true) {
                        dialogOpenState = false
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialogOpenState = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_close),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }/*,
                dismissButton = if (true) { // If location list size > 1
                    {
                        Button(
                            onClick = {
                                //dialogOpenState = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.action_delete),
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                } else null*/
            )
        }
    }

    override fun getEnterAnimator(pendingAnimatorList: List<Animator>): Animator {
        return ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f).apply {
            duration = 450
            interpolator = FastOutSlowInInterpolator()
            startDelay = pendingAnimatorList.size * 150L
        }
    }
}
