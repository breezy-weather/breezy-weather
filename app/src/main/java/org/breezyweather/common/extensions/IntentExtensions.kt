/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.extensions

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.breezyweather.R

fun Uri.toShareIntent(context: Context, type: String): Intent {
    val uri = this

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        when (uri.scheme) {
            "http", "https" -> {
                putExtra(Intent.EXTRA_TEXT, uri.toString())
            }
            "content" -> {
                putExtra(Intent.EXTRA_STREAM, uri)
            }
        }
        clipData = ClipData.newRawUri(null, uri)
        setType(type)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    return Intent.createChooser(shareIntent, context.getString(R.string.action_share)).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
}