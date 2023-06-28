package org.breezyweather.settings.activities

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.GeoViewModel
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.getCardListItemMarginDp
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.common.ui.widgets.insets.bottomInsetItem
import org.breezyweather.settings.preference.sectionFooterItem
import org.breezyweather.settings.preference.sectionHeaderItem
import org.breezyweather.common.extensions.workManager
import org.breezyweather.theme.compose.BreezyWeatherTheme
import javax.inject.Inject

/**
 * Partially taken from Tachiyomi
 * Apache License, Version 2.0
 *
 * https://github.com/tachiyomiorg/tachiyomi/blob/6263a527772f4cce8b3b164b87d7b526773ad7ad/app/src/main/java/eu/kanade/presentation/more/settings/screen/debug/WorkerInfoScreen.kt
 */
class WorkerInfoActivity : GeoActivity() {
    private lateinit var viewModel: WorkerInfoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initModel()

        setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }
    }

    private fun initModel() {
        viewModel = ViewModelProvider(this)[WorkerInfoViewModel::class.java]
    }

    @Composable
    private fun ContentView() {
        val scrollBehavior = generateCollapsedScrollBehavior()

        val screenModel = remember { viewModel }
        val enqueued by screenModel.enqueued.collectAsState()
        val finished by screenModel.finished.collectAsState()
        val running by screenModel.running.collectAsState()

        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.settings_background_updates_worker_info_title),
                    onBackPressed = { finish() },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(),
                    //.horizontalScroll(rememberScrollState()),
                contentPadding = it,
            ) {
                sectionHeaderItem(R.string.settings_background_updates_worker_info_enqueued)
                item { SectionText(enqueued) }
                sectionFooterItem(R.string.settings_background_updates_worker_info_enqueued)

                sectionHeaderItem(R.string.settings_background_updates_worker_info_finished)
                item { SectionText(finished) }
                sectionFooterItem(R.string.settings_background_updates_worker_info_finished)

                sectionHeaderItem(R.string.settings_background_updates_worker_info_running)
                item { SectionText(running) }
                sectionFooterItem(R.string.settings_background_updates_worker_info_running)

                bottomInsetItem(
                    extraHeight = getCardListItemMarginDp(this@WorkerInfoActivity).dp
                )
            }
        }
    }

    @Composable
    private fun SectionText(text: String) {
        Box(modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin))) {
            Text(
                text = text,
                //softWrap = false,
                fontFamily = FontFamily.Monospace,
            )
        }
    }

    @Preview
    @Composable
    private fun DefaultPreview() {
        BreezyWeatherTheme(lightTheme = isSystemInDarkTheme()) {
            ContentView()
        }
    }
}

@HiltViewModel
class WorkerInfoViewModel @Inject constructor(application: Application) : GeoViewModel(application) {
    private val workManager = application.workManager
    private val ioCoroutineScope = MainScope()

    val finished = workManager
        .getWorkInfosLiveData(WorkQuery.fromStates(WorkInfo.State.SUCCEEDED, WorkInfo.State.FAILED, WorkInfo.State.CANCELLED))
        .asFlow()
        .map(::constructString)
        .stateIn(ioCoroutineScope, SharingStarted.WhileSubscribed(), "")

    val running = workManager
        .getWorkInfosLiveData(WorkQuery.fromStates(WorkInfo.State.RUNNING))
        .asFlow()
        .map(::constructString)
        .stateIn(ioCoroutineScope, SharingStarted.WhileSubscribed(), "")

    val enqueued = workManager
        .getWorkInfosLiveData(WorkQuery.fromStates(WorkInfo.State.ENQUEUED))
        .asFlow()
        .map(::constructString)
        .stateIn(ioCoroutineScope, SharingStarted.WhileSubscribed(), "")

    private fun constructString(list: List<WorkInfo>) = buildString {
        if (list.isEmpty()) {
            appendLine("-")
        } else {
            list.fastForEach { workInfo ->
                appendLine("Id: ${workInfo.id}")
                appendLine("Tags:")
                workInfo.tags.forEach {
                    appendLine(" - $it")
                }
                appendLine("State: ${workInfo.state}")
                appendLine()
            }
        }
    }
}