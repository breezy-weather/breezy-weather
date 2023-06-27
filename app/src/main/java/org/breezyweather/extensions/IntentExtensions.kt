package org.breezyweather.extensions

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