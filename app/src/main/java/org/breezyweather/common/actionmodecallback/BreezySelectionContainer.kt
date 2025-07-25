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

package org.breezyweather.common.actionmodecallback

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView

/**
 * A selection container with the following options:
 * - Copy
 * - Select all
 * - Translate
 * - Share
 */
@Composable
fun BreezySelectionContainer(
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val breezyTextToolbar = remember { BreezyTextToolbar(view = view) }

    CompositionLocalProvider(LocalTextToolbar provides breezyTextToolbar) {
        SelectionContainer {
            content()
        }
    }
}
