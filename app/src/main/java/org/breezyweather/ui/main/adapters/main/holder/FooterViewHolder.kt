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

package org.breezyweather.ui.main.adapters.main.holder

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.CallSuper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import org.breezyweather.R
import org.breezyweather.common.extensions.splitKeeping
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.source.resourceName
import org.breezyweather.ui.common.composables.AlertDialogLink
import org.breezyweather.ui.common.composables.AlertDialogNoPadding
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import org.breezyweather.ui.theme.compose.DayNightTheme
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController

class FooterViewHolder(
    private val composeView: ComposeView,
) : AbstractMainViewHolder(composeView) {

    @SuppressLint("SetTextI18n")
    @CallSuper
    override fun onBindView(
        context: Context,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled)

        composeView.setContent {
            BreezyWeatherTheme(lightTheme = MainThemeColorProvider.isLightTheme(context, location)) {
                ComposeView(location)
            }
        }
    }

    @Composable
    fun ComposeView(
        location: Location,
        modifier: Modifier = Modifier,
    ) {
        val delegate = remember { ThemeManager.getInstance(context).weatherThemeDelegate }
        val dialogOpenState = remember { mutableStateOf(false) }
        val dialogLinkOpenState = remember { mutableStateOf(false) }
        val linkToOpen = rememberSaveable { mutableStateOf("") }
        val forecastSource = remember(location) {
            (context as MainActivity).sourceManager.getWeatherSource(location.forecastSource)
        }
        val moreClickableLinkAnnotation = remember {
            LinkAnnotation.Clickable(
                tag = context.getString(R.string.data_sources),
                styles = TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline)),
                linkInteractionListener = {
                    dialogOpenState.value = true
                }
            )
        }

        Row(
            modifier = modifier
                .padding(dimensionResource(R.dimen.normal_margin))
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    forecastSource?.supportedFeatures?.getOrElse(SourceFeature.FORECAST) { null }?.let {
                        if (forecastSource is HttpSource && forecastSource.attributionLinks.isNotEmpty()) {
                            val splits = it.splitKeeping(*forecastSource.attributionLinks.keys.toTypedArray())
                            splits.forEach { split ->
                                forecastSource.attributionLinks.getOrElse(split) { null }?.let { link ->
                                    withLink(
                                        LinkAnnotation.Clickable(
                                            tag = split,
                                            styles = TextLinkStyles(
                                                style = SpanStyle(textDecoration = TextDecoration.Underline)
                                            ),
                                            linkInteractionListener = {
                                                linkToOpen.value = link
                                                dialogLinkOpenState.value = true
                                            }
                                        )
                                    ) {
                                        append(split)
                                    }
                                } ?: append(split)
                            }
                        } else {
                            append(it)
                        }
                        append(stringResource(R.string.dot_separator))
                        withLink(moreClickableLinkAnnotation) { append(stringResource(R.string.action_more)) }
                    } ?: withLink(moreClickableLinkAnnotation) { append(stringResource(R.string.data_sources)) }
                },
                color = Color(
                    delegate.getOnBackgroundColor(
                        context,
                        WeatherViewController.getWeatherKind(location),
                        WeatherViewController.isDaylight(location)
                    )
                ),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }

        if (dialogOpenState.value) {
            val sources = mapOf(
                SourceFeature.FORECAST to location.forecastSource,
                SourceFeature.CURRENT to location.currentSource,
                SourceFeature.AIR_QUALITY to location.airQualitySource,
                SourceFeature.POLLEN to location.pollenSource,
                SourceFeature.MINUTELY to location.minutelySource,
                SourceFeature.ALERT to location.alertSource,
                SourceFeature.NORMALS to location.normalsSource,
                SourceFeature.REVERSE_GEOCODING to location.reverseGeocodingSource
            ).filter { !it.value.isNullOrEmpty() }.mapNotNull {
                if (it.key == SourceFeature.REVERSE_GEOCODING) {
                    (context as MainActivity).sourceManager.getReverseGeocodingSource(it.value!!)?.let { source ->
                        it.key to source
                    }
                } else {
                    (context as MainActivity).sourceManager.getWeatherSource(it.value!!)?.let { source ->
                        if (source.supportedFeatures.containsKey(it.key)) {
                            it.key to source
                        } else {
                            null
                        }
                    }
                }
            }.toMap()

            AlertDialogNoPadding(
                onDismissRequest = {
                    dialogOpenState.value = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialogOpenState.value = false
                        }
                    ) {
                        Text(stringResource(R.string.action_close))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.data_sources),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    LazyColumn {
                        sources.forEach { (sourceFeature, source) ->
                            item {
                                ListItem(
                                    colors = ListItemDefaults.colors(
                                        containerColor = AlertDialogDefaults.containerColor
                                    ),
                                    leadingContent = if (source is WeatherSource) {
                                        source.getAttributionIcon(
                                            !MainThemeColorProvider.isLightTheme(context, location)
                                        )?.let {
                                            {
                                                Icon(
                                                    painterResource(it),
                                                    contentDescription = null,
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier
                                                        .size(dimensionResource(R.dimen.material_icon_size))
                                                )
                                            }
                                        }
                                    } else {
                                        null
                                    },
                                    headlineContent = {
                                        Text(
                                            stringResource(sourceFeature.resourceName),
                                            color = DayNightTheme.colors.titleColor
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = buildAnnotatedString {
                                                if (sourceFeature == SourceFeature.REVERSE_GEOCODING) {
                                                    (source as ReverseGeocodingSource).reverseGeocodingAttribution
                                                } else {
                                                    (source as WeatherSource).supportedFeatures[sourceFeature]!!
                                                }.let {
                                                    if (source is HttpSource &&
                                                        source.attributionLinks.isNotEmpty()
                                                    ) {
                                                        val splits = it.splitKeeping(
                                                            *source.attributionLinks.keys.toTypedArray()
                                                        )
                                                        splits.forEach { split ->
                                                            source.attributionLinks.getOrElse(split) { null }
                                                                ?.let { link ->
                                                                    withLink(
                                                                        LinkAnnotation.Clickable(
                                                                            tag = split,
                                                                            styles = TextLinkStyles(
                                                                                style = SpanStyle(
                                                                                    textDecoration = TextDecoration
                                                                                        .Underline
                                                                                )
                                                                            ),
                                                                            linkInteractionListener = {
                                                                                linkToOpen.value = link
                                                                                dialogLinkOpenState.value = true
                                                                            }
                                                                        )
                                                                    ) {
                                                                        append(split)
                                                                    }
                                                                } ?: append(split)
                                                        }
                                                    } else {
                                                        append(it)
                                                    }
                                                }
                                            }
                                        )
                                    },
                                    trailingContent = if (source is HttpSource &&
                                        source.privacyPolicyUrl.isNotEmpty()
                                    ) {
                                        {
                                            IconButton(
                                                onClick = {
                                                    linkToOpen.value = source.privacyPolicyUrl
                                                    dialogLinkOpenState.value = true
                                                }
                                            ) {
                                                Icon(
                                                    painterResource(R.drawable.ic_shield_lock),
                                                    contentDescription = stringResource(R.string.about_privacy_policy),
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }

        if (dialogLinkOpenState.value) {
            AlertDialogLink(
                onClose = { dialogLinkOpenState.value = false },
                linkToOpen = linkToOpen.value
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
