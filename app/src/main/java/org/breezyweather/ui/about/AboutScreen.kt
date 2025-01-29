package org.breezyweather.ui.about

import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.background.updater.interactor.GetApplicationRelease
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.withIOContext
import org.breezyweather.common.extensions.withUIContext
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.data.appContributors
import org.breezyweather.data.appTranslators
import org.breezyweather.ui.common.composables.AlertDialogLink
import org.breezyweather.ui.common.widgets.Material3CardListItem
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.getCardListItemMarginDp
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.common.widgets.insets.bottomInsetItem
import org.breezyweather.ui.theme.compose.DayNightTheme
import org.breezyweather.ui.theme.compose.themeRipple

internal class AboutAppLinkItem(
    @DrawableRes val iconId: Int,
    @StringRes val titleId: Int,
    val onClick: () -> Unit,
)

@Composable
internal fun AboutScreen(
    onBackPressed: () -> Unit,
    aboutViewModel: AboutViewModel = viewModel(),
) {
    val scrollBehavior = generateCollapsedScrollBehavior()

    val scope = rememberCoroutineScope()
    val isCheckingUpdates = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = LocalActivity.current

    val uriHandler = LocalUriHandler.current
    val linkToOpen = rememberSaveable { mutableStateOf("") }
    val dialogLinkOpenState = rememberSaveable { mutableStateOf(false) }

    val locale = context.currentLocale
    val language = locale.language
    val languageWithCountry = locale.language + (if (!locale.country.isNullOrEmpty()) "_r" + locale.country else "")
    var filteredTranslators = appTranslators.filter {
        it.lang.contains(language) || it.lang.contains(languageWithCountry)
    }
    if (filteredTranslators.isEmpty()) {
        // No translators found? Language doesn’t exist, so defaulting to English
        filteredTranslators = appTranslators.filter { it.lang.contains("en") }
    }

    val contactLinks = arrayOf(
        AboutAppLinkItem(
            iconId = R.drawable.ic_code,
            titleId = R.string.about_source_code
        ) {
            linkToOpen.value = "https://github.com/breezy-weather/breezy-weather"
            dialogLinkOpenState.value = true
        },
        AboutAppLinkItem(
            iconId = R.drawable.ic_forum,
            titleId = R.string.about_matrix
        ) {
            linkToOpen.value = "https://matrix.to/#/#breezy-weather:matrix.org"
            dialogLinkOpenState.value = true
        }
    )

    Material3Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FitStatusBarTopAppBar(
                title = stringResource(R.string.action_about),
                onBackPressed = onBackPressed,
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = it
        ) {
            item {
                Header()
                AboutAppLink(
                    iconId = R.drawable.ic_sync, // TODO: Replace with a circular progress indicator
                    title = stringResource(R.string.about_check_for_app_updates),
                    onClick = {
                        if (BuildConfig.FLAVOR == "freenet") {
                            // GitHub is a non-free network, so we cannot automatically check for updates in the
                            // "freenet" flavor
                            // We ask for permission to manually check updates in the browser instead
                            linkToOpen.value =
                                "https://github.com/breezy-weather/breezy-weather/releases/latest"
                            dialogLinkOpenState.value = true
                        } else {
                            if (!isCheckingUpdates.value) {
                                scope.launch {
                                    isCheckingUpdates.value = true

                                    withUIContext {
                                        try {
                                            when (
                                                val result = withIOContext {
                                                    aboutViewModel.checkForUpdate(
                                                        context,
                                                        forceCheck = true
                                                    )
                                                }
                                            ) {
                                                is GetApplicationRelease.Result.NewUpdate -> {
                                                    SnackbarHelper.showSnackbar(
                                                        context.getString(
                                                            R.string.notification_app_update_available
                                                        ),
                                                        context.getString(R.string.action_download)
                                                    ) {
                                                        uriHandler.openUri(result.release.releaseLink)
                                                    }
                                                }

                                                is GetApplicationRelease.Result.NoNewUpdate -> {
                                                    SnackbarHelper.showSnackbar(
                                                        context.getString(R.string.about_no_new_updates)
                                                    )
                                                }
                                                /*is GetApplicationRelease.Result.OsTooOld -> {
                                                    SnackbarHelper.showSnackbar(
                                                        context.getString(
                                                            R.string.about_update_check_eol
                                                        )
                                                    )
                                                }*/
                                                else -> {}
                                            }
                                        } catch (e: Exception) {
                                            e.message?.let { msg ->
                                                SnackbarHelper.showSnackbar(
                                                    msg
                                                )
                                            }
                                            e.printStackTrace()
                                        } finally {
                                            isCheckingUpdates.value = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
                SectionTitle(stringResource(R.string.about_contact))
            }
            items(contactLinks) { item ->
                AboutAppLink(
                    iconId = item.iconId,
                    title = stringResource(item.titleId),
                    onClick = item.onClick
                )
            }

            item {
                SectionTitle(stringResource(R.string.about_app))
            }
            if (activity != null) {
                items(aboutViewModel.getAboutAppLinks(activity)) { item ->
                    AboutAppLink(
                        iconId = item.iconId,
                        title = stringResource(item.titleId),
                        onClick = item.onClick
                    )
                }
            }

            item { SectionTitle(stringResource(R.string.about_contributors)) }
            items(appContributors) { item ->
                ContributorView(name = item.name, contribution = item.contribution) {
                    linkToOpen.value = item.link
                    if (linkToOpen.value.isNotEmpty()) {
                        dialogLinkOpenState.value = true
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.about_translators)) }
            items(filteredTranslators) { item ->
                ContributorView(name = item.name) {
                    linkToOpen.value = when {
                        !item.github.isNullOrEmpty() -> "https://github.com/${item.github}"
                        !item.weblate.isNullOrEmpty() -> "https://hosted.weblate.org/user/${item.weblate}/"
                        !item.mail.isNullOrEmpty() -> "mailto:${item.mail}"
                        !item.url.isNullOrEmpty() -> item.url
                        else -> ""
                    }
                    if (linkToOpen.value.isNotEmpty()) {
                        dialogLinkOpenState.value = true
                    }
                }
            }

            bottomInsetItem(
                extraHeight = getCardListItemMarginDp(context).dp
            )
        }

        if (dialogLinkOpenState.value) {
            AlertDialogLink(
                onClose = { dialogLinkOpenState.value = false },
                linkToOpen = linkToOpen.value
            )
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_round),
            contentDescription = null,
            modifier = Modifier.size(72.dp)
        )
        Spacer(
            modifier = Modifier
                .height(dimensionResource(R.dimen.little_margin))
                .fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.breezy_weather),
            color = DayNightTheme.colors.titleColor,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = versionFormatted,
            color = DayNightTheme.colors.captionColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin)),
        color = DayNightTheme.colors.captionColor,
        style = MaterialTheme.typography.labelMedium
    )
}

private val versionFormatted: String
    get() = when {
        BuildConfig.DEBUG -> "Debug ${BuildConfig.COMMIT_SHA}"
        else -> "Stable ${BuildConfig.VERSION_NAME}"
    }

@Composable
private fun AboutAppLink(
    @DrawableRes iconId: Int,
    title: String,
    onClick: () -> Unit,
) {
    Material3CardListItem {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = themeRipple(),
                    onClick = onClick
                )
                .padding(dimensionResource(R.dimen.normal_margin)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                tint = DayNightTheme.colors.titleColor
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.normal_margin)))
            Text(
                text = title,
                color = DayNightTheme.colors.titleColor,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ContributorView(
    name: String,
    @StringRes contribution: Int? = null,
    onClick: () -> Unit,
) {
    Material3CardListItem {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = themeRipple(),
                    onClick = {
                        onClick()
                    }
                )
                .padding(dimensionResource(R.dimen.normal_margin))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    color = DayNightTheme.colors.titleColor,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (contribution != null) {
                Text(
                    text = stringResource(contribution),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
