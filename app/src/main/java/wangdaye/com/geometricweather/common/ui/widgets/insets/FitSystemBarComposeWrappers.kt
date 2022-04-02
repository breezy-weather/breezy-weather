package wangdaye.com.geometricweather.common.ui.widgets.insets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FitStatusBarTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
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
                title = title,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                elevation = elevation,
            )
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
fun FitNavigationBarBottomAppBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    cutoutShape: Shape? = null,
    elevation: Dp = AppBarDefaults.BottomAppBarElevation,
    contentPadding: PaddingValues = AppBarDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Box {
        Column {
            BottomAppBar(
                modifier = modifier,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                cutoutShape = cutoutShape,
                elevation = elevation,
                contentPadding = contentPadding,
                content = content,
            )
        }
        Spacer(
            modifier = Modifier
                .background(backgroundColor)
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )
    }
}