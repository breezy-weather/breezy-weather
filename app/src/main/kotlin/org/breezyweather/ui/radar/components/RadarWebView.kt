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

package org.breezyweather.ui.radar.components

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.breezyweather.ui.radar.RadarUiState
import org.breezyweather.ui.radar.RadarWebViewClient

@Composable
internal fun RadarWebView(
    radarUiState: RadarUiState,
    modifier: Modifier = Modifier,
) {
    SimpleWebView(
        initialUrl = radarUiState.webViewUrl!!,
        allowedDomains = radarUiState.allowedDomains!!,
        blockedUrls = radarUiState.blockedUrls!!,
        modifier = modifier
            .fillMaxSize()
    )
}

/**
 * An early version of Compose WebView should be available soon
 * https://issuetracker.google.com/issues/329866164
 * TODO: Make some privacy/security improvements
 * TODO: Add warning if running an old webview version
 * FIXME: Doesn't load tiles
 */
@Composable
fun SimpleWebView(
    initialUrl: String,
    allowedDomains: Array<String>,
    blockedUrls: Array<String>,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = RadarWebViewClient(allowedDomains, blockedUrls)
                settings.apply {
                    javaScriptEnabled = true
                    allowFileAccess = false
                    allowContentAccess = false
                }
                loadUrl(initialUrl)
            }
        },
        update = { wv ->
            if (wv.url != initialUrl) {
                wv.webViewClient = RadarWebViewClient(allowedDomains, blockedUrls)
                wv.loadUrl(initialUrl)
            }
        }
    )
}
