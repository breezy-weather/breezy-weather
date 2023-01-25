package wangdaye.com.geometricweather.common.ui.widgets

import android.content.Context
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import wangdaye.com.geometricweather.R
import kotlin.math.ln

// helper.

@Composable
fun getWidgetSurfaceColor(elevation: Dp): Color {
    val surface = MaterialTheme.colorScheme.surface

    if (elevation == 0.dp) {
        return surface
    }

    return MaterialTheme
        .colorScheme
        .surfaceTint
        .copy(alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f)
        .compositeOver(surface)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun generateCollapsedScrollBehavior(): TopAppBarScrollBehavior {
    return TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
}

// scaffold.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        content = content,
    )
}

// list items.

val defaultCardListItemElevation = 2.dp

fun getCardListItemMarginDp(context: Context) = context
    .resources
    .getDimension(R.dimen.little_margin)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3CardListItem(
    elevation: Dp = defaultCardListItemElevation,
    content: @Composable ColumnScope.() -> Unit,
) = Card(
    modifier = Modifier
        .padding(
            start = dimensionResource(R.dimen.little_margin),
            end = dimensionResource(R.dimen.little_margin),
            top = dimensionResource(R.dimen.little_margin),
            bottom = 0.dp
        ),
    shape = RoundedCornerShape(
        size = dimensionResource(R.dimen.material3_card_list_item_corner_radius)
    ),
    colors = CardDefaults.cardColors(
        containerColor = getWidgetSurfaceColor(elevation),
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    content = content
)