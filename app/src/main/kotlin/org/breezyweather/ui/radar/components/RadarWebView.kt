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
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.ui.radar.RadarUiState

@Composable
internal fun RadarWebView(
    radarUiState: RadarUiState,
    modifier: Modifier = Modifier,
) {
    SimpleWebView(
        initialUrl = radarUiState.webViewUrl!!,
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
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        if (!request.url.toString().startsWith("https://")) {
                            return true
                        }
                        // TODO: Restrict URLs that can be contacted
                        LogHelper.log(msg = "Contacted URL: ${request.url}")
                        return false
                    }

                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest,
                    ): WebResourceResponse? {
                        // TODO: Restrict URLs that can be contacted
                        LogHelper.log(msg = "Contacted URL: ${request.url}")
                        return super.shouldInterceptRequest(view, request)
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    allowFileAccess = false
                    allowContentAccess = false
                }
                loadUrl(initialUrl)
            }
        }
    )
}
