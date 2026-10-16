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

package org.breezyweather.ui.radar

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import org.breezyweather.BreezyWeather
import org.breezyweather.common.utils.helpers.LogHelper
import java.io.ByteArrayInputStream

/**
 * A web client that blocks URLs based on an allow list of domains
 * You can pass blocked URLs that will always be blocked even if part of allowed domains
 */
class RadarWebViewClient(
    val allowedDomains: Array<String>,
    val blockedUrls: Array<String>,
) : WebViewClient() {

    /*
     * TODO: This should allow to open external URLs in external browser
     */
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean {
        if (!request.url.toString().startsWith("https://")) {
            if (BreezyWeather.instance.debugMode) {
                LogHelper.log(msg = "[shouldOverrideUrlLoading] Blocked non-HTTPS URL: ${request.url}")
            }
            return true
        }

        blockedUrls.forEach { bu ->
            if (request.url.toString().startsWith(bu)) {
                if (BreezyWeather.instance.debugMode) {
                    LogHelper.log(msg = "[shouldOverrideUrlLoading] Blocked URL: ${request.url}")
                }
                return true
            }
        }

        allowedDomains.forEach { ad ->
            if (request.url.host == ad) {
                if (BreezyWeather.instance.debugMode) {
                    LogHelper.log(msg = "[shouldOverrideUrlLoading] Allowed URL: ${request.url}")
                }
                return false
            }
        }

        if (BreezyWeather.instance.debugMode) {
            LogHelper.log(msg = "[shouldOverrideUrlLoading] Blocked unknown URL: ${request.url}")
        }

        return true
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        if (!request.url.toString().startsWith("https://")) {
            if (BreezyWeather.instance.debugMode) {
                LogHelper.log(msg = "[shouldInterceptRequest] Blocked non-HTTPS URL: ${request.url}")
            }
            return WebResourceResponse(
                "text/plain",
                "utf-8",
                403,
                "Blocked",
                emptyMap(),
                ByteArrayInputStream(ByteArray(0))
            )
        }

        blockedUrls.forEach { bu ->
            if (request.url.toString().startsWith(bu)) {
                if (BreezyWeather.instance.debugMode) {
                    LogHelper.log(msg = "[shouldInterceptRequest] Blocked URL: ${request.url}")
                }
                return WebResourceResponse(
                    "text/plain",
                    "utf-8",
                    403,
                    "Blocked",
                    emptyMap(),
                    ByteArrayInputStream(ByteArray(0))
                )
            }
        }

        allowedDomains.forEach { ad ->
            if (request.url.host == ad) {
                if (BreezyWeather.instance.debugMode) {
                    LogHelper.log(msg = "[shouldInterceptRequest] Allowed URL: ${request.url}")
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        if (BreezyWeather.instance.debugMode) {
            LogHelper.log(msg = "[shouldInterceptRequest] Blocked unknown URL: ${request.url}")
        }

        return WebResourceResponse(
            "text/plain",
            "utf-8",
            403,
            "Blocked",
            emptyMap(),
            ByteArrayInputStream(ByteArray(0))
        )
    }
}
