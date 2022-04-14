package wangdaye.com.geometricweather.common.ui.widgets.insets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.settings.preference.SectionFooter

private val AppBarHeight = 64.dp
private val AppBarHorizontalPadding = 4.dp
private val TitleInsetWithoutIcon = Modifier.width(16.dp - AppBarHorizontalPadding)
private val TitleIconModifier = Modifier
    .fillMaxHeight()
    .width(62.dp - AppBarHorizontalPadding)

@Composable
fun FitStatusBarTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    Box {
        Column {
            Spacer(
                modifier = Modifier
                    .background(backgroundColor)
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .fillMaxWidth(),
            )
            TopAppBar(
                modifier = modifier.height(AppBarHeight),
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                elevation = elevation,
            ) {
                if (navigationIcon == null) {
                    Spacer(TitleInsetWithoutIcon)
                } else {
                    Row(TitleIconModifier, verticalAlignment = Alignment.CenterVertically) {
                        CompositionLocalProvider(
                            LocalContentAlpha provides ContentAlpha.high,
                            content = navigationIcon
                        )
                    }
                }

                Row(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    title()
                }

                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Row(
                        Modifier.fillMaxHeight(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier
                .background(backgroundColor)
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .fillMaxWidth()
                .align(Alignment.TopCenter),
        )
    }
}

@Composable
fun FitStatusBarTopAppBar(
    title: String,
    onBackPressed: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) = FitStatusBarTopAppBar(
    title = {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
            )
        )
    },
    navigationIcon = {
        IconButton(onClick = onBackPressed) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    },
    actions = actions,
    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
)

@Composable
fun FitNavigationBarBottomAppBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = AppBarDefaults.BottomAppBarElevation,
    contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Box {
        Column {
            BottomAppBar(
                modifier = modifier,
                containerColor = containerColor,
                contentColor = contentColor,
                tonalElevation = tonalElevation,
                contentPadding = contentPadding,
                content = content,
            )
        }
        Spacer(
            modifier = Modifier
                .background(containerColor)
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )
    }
}

enum class BottomInsetKey { INSTANCE }
fun LazyListScope.bottomInsetItem() = item(
    key = { BottomInsetKey.INSTANCE },
    contentType = { BottomInsetKey.INSTANCE },
) {
    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
}