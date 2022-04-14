package wangdaye.com.geometricweather.settings.preference

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource

// screen.

@JvmName("PreferenceScreenWithSection")
@Composable
fun PreferenceScreen(
    modelList: List<PreferenceSectionModel>
) = LazyColumn(
    modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
) {
    modelList.forEach { model ->
        model.sectionTitleId?.let { sectionHeaderItem(it) }
        preferenceItems(model.preferenceModelList)
        model.sectionTitleId?.let { sectionFooterItem(it) }
    }
    bottomInsetItem()
}

@Composable
fun PreferenceScreen(
    modelList: List<State<PreferenceModel>>
) = LazyColumn(
    modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
) {
    preferenceItems(modelList)
    bottomInsetItem()
}

// item.

fun LazyListScope.sectionHeaderItem(
    @StringRes sectionTitleId: Int
) = item(
    key = { sectionTitleId },
    contentType = { sectionTitleId },
) { SectionHeader(title = stringResource(sectionTitleId)) }

fun LazyListScope.sectionFooterItem(
    @StringRes sectionTitleId: Int
) = item(
    key = { -1 * sectionTitleId },
    contentType = { -1 },
) { SectionFooter() }

enum class BottomInsetKey { INSTANCE }
fun LazyListScope.bottomInsetItem() = item(
    key = { BottomInsetKey.INSTANCE },
    contentType = { BottomInsetKey.INSTANCE },
) {
    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
}

fun LazyListScope.preferenceItem(
    item: State<PreferenceModel>,
) = item(
    key = { item.value.titleId },
    contentType = { item::class.java },
) {
    PreferenceViewByModel(state = item)
}

fun LazyListScope.preferenceItems(
    items: List<State<PreferenceModel>>,
) = items(
    items = items,
    key = { it.value.titleId },
    contentType = { it::class.java },
) {
    PreferenceViewByModel(state = it)
}

@Composable
fun PreferenceViewByModel(
    state: State<PreferenceModel>
) {
    val modelState = remember { state }
    if (modelState.value is PreferenceModel.ClickablePreferenceModel) {
        val model = modelState.value as PreferenceModel.ClickablePreferenceModel
        PreferenceView(
            title = stringResource(model.titleId),
            summary = model.summaryId?.let { stringResource(model.summaryId) },
            enabled = model.enabled,
            onClick = model.onClick,
        )
        return
    }
    if (modelState.value is PreferenceModel.CheckboxPreferenceModel) {
        val model = modelState.value as PreferenceModel.CheckboxPreferenceModel
        CheckboxPreferenceView(
            title = stringResource(model.titleId),
            summary = model.summaryGenerator,
            checked = model.checked,
            enabled = model.enabled,
            onValueChanged = {
                model.checked = it
                model.onValueChanged(it)
            },
        )
        return
    }
    if (modelState.value is PreferenceModel.ListPreferenceModel) {
        val model = modelState.value as PreferenceModel.ListPreferenceModel
        ListPreferenceView(
            title = stringResource(model.titleId),
            summary = model.summaryGenerator,
            selectedKey = model.selectedKey,
            valueArray = stringArrayResource(model.valueArrayId),
            nameArray = stringArrayResource(model.nameArrayId),
            enabled = model.enabled,
            onValueChanged = {
                model.selectedKey = it
                model.onValueChanged(it)
            },
        )
        return
    }
    if (modelState.value is PreferenceModel.TimePickerPreferenceModel) {
        val model = modelState.value as PreferenceModel.TimePickerPreferenceModel
        TimePickerPreferenceView(
            title = stringResource(model.titleId),
            summary = model.summaryGenerator,
            currentTime = model.currentTime,
            enabled = model.enabled,
            onValueChanged = {
                model.currentTime = it
                model.onValueChanged(it)
            },
        )
        return
    }
    if (modelState.value is PreferenceModel.EditTextPreferenceModel) {
        val model = modelState.value as PreferenceModel.EditTextPreferenceModel
        EditTextPreferenceView(
            title = stringResource(model.titleId),
            summary = model.summaryGenerator,
            content = model.content,
            enabled = model.enabled,
            onValueChanged = {
                model.content = it
                model.onValueChanged(it)
            },
        )
        return
    }
}

fun LazyListScope.clickablePreferenceItem(
    @StringRes titleId: Int,
    content: @Composable (Int) -> Unit,
) = item(
    key = { titleId },
    contentType = { PreferenceModel.ClickablePreferenceModel::class.java },
) {
    content(titleId)
}