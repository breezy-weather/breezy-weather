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

package org.breezyweather.ui.settings.activities

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
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
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.basic.BreezyViewModel
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.workManager
import org.breezyweather.ui.common.widgets.Material3Scaffold
import org.breezyweather.ui.common.widgets.generateCollapsedScrollBehavior
import org.breezyweather.ui.common.widgets.getCardListItemMarginDp
import org.breezyweather.ui.common.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.ui.common.widgets.insets.bottomInsetItem
import org.breezyweather.ui.settings.preference.sectionFooterItem
import org.breezyweather.ui.settings.preference.sectionHeaderItem
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import javax.inject.Inject

/**
 * Partially taken from Mihon
 * Apache License, Version 2.0
 *
 * https://github.com/mihonapp/mihon/blob/5aec8f8018236a38106483da08f9cbc28261ac9b/app/src/main/java/eu/kanade/presentation/more/settings/screen/debug/WorkerInfoScreen.kt
 */
class WorkerInfoActivity : BreezyActivity() {
    private lateinit var viewModel: WorkerInfoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initModel()

        setContent {
            BreezyWeatherTheme {
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
                    scrollBehavior = scrollBehavior
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(),
                // .horizontalScroll(rememberScrollState()),
                contentPadding = it
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
                // softWrap = false,
                fontFamily = FontFamily.Monospace
            )
        }
    }

    @Preview
    @Composable
    private fun DefaultPreview() {
        BreezyWeatherTheme {
            ContentView()
        }
    }
}

@HiltViewModel
class WorkerInfoViewModel @Inject constructor(application: Application) : BreezyViewModel(application) {
    private val workManager = application.workManager
    private val ioCoroutineScope = MainScope()

    val finished = workManager
        .getWorkInfosFlow(
            WorkQuery.fromStates(WorkInfo.State.SUCCEEDED, WorkInfo.State.FAILED, WorkInfo.State.CANCELLED)
        )
        .map(::constructString)
        .stateIn(ioCoroutineScope, SharingStarted.WhileSubscribed(), "")

    val running = workManager
        .getWorkInfosFlow(WorkQuery.fromStates(WorkInfo.State.RUNNING))
        .map(::constructString)
        .stateIn(ioCoroutineScope, SharingStarted.WhileSubscribed(), "")

    val enqueued = workManager
        .getWorkInfosFlow(WorkQuery.fromStates(WorkInfo.State.ENQUEUED))
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
                if (workInfo.state == WorkInfo.State.ENQUEUED) {
                    appendLine(
                        "Next scheduled run: ${workInfo.nextScheduleTimeMillis.toDate()
                            .getFormattedDate("yyyy-MM-dd HH:mm")}"
                    )
                    appendLine("Attempt #${workInfo.runAttemptCount + 1}")
                }
                if (workInfo.state == WorkInfo.State.CANCELLED ||
                    workInfo.state == WorkInfo.State.FAILED
                ) {
                    appendLine("Stop reason code: ${workInfo.stopReason}")
                }
                appendLine()
            }
        }
    }
}
