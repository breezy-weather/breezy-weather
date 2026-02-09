/*
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

package org.breezyweather.ui.about

import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
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
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.background.updater.interactor.GetApplicationRelease
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.withIOContext
import org.breezyweather.common.extensions.withUIContext
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.data.appContributors
import org.breezyweather.data.appTranslators
import org.breezyweather.ui.common.composables.AlertDialogLink
import org.breezyweather.ui.common.widgets.Material3ExpressiveCardListItem
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.getCardListItemMarginDp
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.common.widgets.insets.bottomInsetItem
import org.breezyweather.ui.settings.preference.LargeSeparatorItem
import org.breezyweather.ui.settings.preference.SmallSeparatorItem
import org.breezyweather.ui.settings.preference.largeSeparatorItem
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

    val contactLinks = buildList {
        add(
            AboutAppLinkItem(
                iconId = R.drawable.ic_code,
                titleId = R.string.about_source_code
            ) {
                linkToOpen.value = BuildConfig.SOURCE_CODE_LINK
                dialogLinkOpenState.value = true
            }
        )
        BuildConfig.CONTACT_MATRIX.takeIf { it.startsWith("https://") }?.let {
            add(
                AboutAppLinkItem(
                    iconId = R.drawable.ic_forum,
                    titleId = R.string.about_matrix
                ) {
                    linkToOpen.value = it
                    dialogLinkOpenState.value = true
                }
            )
        }
    }

    val isUpdateCheckerEnabled = remember {
        BreezyWeather.instance.isGitHubUpdateCheckerEnabled || BuildConfig.RELEASES_LINK.isNotEmpty()
    }

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
            contentPadding = it.plus(
                PaddingValues(horizontal = dimensionResource(R.dimen.normal_margin))
            )
        ) {
            item {
                Header()
            }
            if (isUpdateCheckerEnabled) {
                item {
                    AboutAppLink(
                        isFirst = true,
                        isLast = true,
                        icon = {
                            // Use crossfade animation to prevent the progress indicator from flickering when repeatedly
                            // pressing the update card as this causes the loading state to change back and forth almost
                            // instantly.
                            Crossfade(
                                targetState = isCheckingUpdates.value,
                                label = ""
                            ) { loading ->
                                when (loading) {
                                    false -> {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_sync),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    true -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        },
                        title = stringResource(R.string.about_check_for_app_updates),
                        onClick = {
                            if (BreezyWeather.instance.isGitHubUpdateCheckerEnabled) {
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
                                                    is GetApplicationRelease.Result.OsTooOld -> {
                                                        SnackbarHelper.showSnackbar(
                                                            context.getString(
                                                                R.string.about_update_check_eol
                                                            )
                                                        )
                                                    }
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
                            } else {
                                linkToOpen.value = BuildConfig.RELEASES_LINK
                                dialogLinkOpenState.value = true
                            }
                        }
                    )
                }
            }
            item {
                LargeSeparatorItem()
            }
            item {
                SectionTitle(stringResource(R.string.about_contact))
            }
            itemsIndexed(contactLinks) { index, item ->
                AboutAppLink(
                    iconId = item.iconId,
                    title = stringResource(item.titleId),
                    isFirst = index == 0,
                    isLast = index == contactLinks.lastIndex,
                    onClick = item.onClick
                )
                if (index != contactLinks.lastIndex) {
                    SmallSeparatorItem()
                }
            }

            largeSeparatorItem()
            item { SectionTitle(stringResource(R.string.about_app)) }
            if (activity != null) {
                itemsIndexed(aboutViewModel.getAboutAppLinks(activity)) { index, item ->
                    AboutAppLink(
                        iconId = item.iconId,
                        title = stringResource(item.titleId),
                        isFirst = index == 0,
                        isLast = index == aboutViewModel.getAboutAppLinks(activity).lastIndex,
                        onClick = item.onClick
                    )
                    if (index != aboutViewModel.getAboutAppLinks(activity).lastIndex) {
                        SmallSeparatorItem()
                    }
                }
            }

            largeSeparatorItem()
            item { SectionTitle(stringResource(R.string.about_contributors)) }
            itemsIndexed(appContributors) { index, item ->
                ContributorView(
                    name = item.name,
                    contribution = item.contribution,
                    isFirst = index == 0,
                    isLast = index == appContributors.lastIndex
                ) {
                    linkToOpen.value = item.link
                    if (linkToOpen.value.isNotEmpty()) {
                        dialogLinkOpenState.value = true
                    }
                }
                if (index != appContributors.lastIndex) {
                    SmallSeparatorItem()
                }
            }

            largeSeparatorItem()
            item { SectionTitle(stringResource(R.string.about_translators)) }
            itemsIndexed(filteredTranslators) { index, item ->
                ContributorView(
                    name = item.name,
                    isFirst = index == 0,
                    isLast = index == filteredTranslators.lastIndex
                ) {
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
                if (index != filteredTranslators.lastIndex) {
                    SmallSeparatorItem()
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
                .height(dimensionResource(R.dimen.small_margin))
                .fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.brand_name),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = versionFormatted,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin)),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium
    )
}

private val versionFormatted: String
    get() = when {
        BuildConfig.DEBUG -> "Debug ${BuildConfig.COMMIT_SHA}"
        else -> "Release ${BuildConfig.VERSION_NAME}"
    }

@Composable
private fun AboutAppLink(
    icon: @Composable () -> Unit,
    title: String,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onClick: () -> Unit,
) {
    Material3ExpressiveCardListItem(isFirst = isFirst, isLast = isLast) {
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
            icon()
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.normal_margin)))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun AboutAppLink(
    @DrawableRes iconId: Int,
    title: String,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onClick: () -> Unit,
) {
    AboutAppLink(
        icon = {
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        title = title,
        isFirst = isFirst,
        isLast = isLast,
        onClick = onClick
    )
}

@Composable
private fun ContributorView(
    name: String,
    @StringRes contribution: Int? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onClick: () -> Unit,
) {
    Material3ExpressiveCardListItem(isFirst = isFirst, isLast = isLast) {
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
                    color = MaterialTheme.colorScheme.onSurface,
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
