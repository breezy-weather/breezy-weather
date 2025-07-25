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

import android.content.ClipData
import android.content.Intent
import android.os.Build
import android.view.ActionMode
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import org.breezyweather.R
import org.breezyweather.common.extensions.clipboardManager
import org.breezyweather.common.utils.helpers.SnackbarHelper

internal class BreezyTextToolbar(
    private val view: View,
) : TextToolbar {
    private var actionMode: ActionMode? = null
    private val textActionModeCallback: BreezyTextActionModeCallback =
        BreezyTextActionModeCallback(onActionModeDestroy = { actionMode = null })
    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
        onAutofillRequested: (() -> Unit)?,
    ) {
        textActionModeCallback.rect = rect
        textActionModeCallback.onCopyRequested = onCopyRequested
        textActionModeCallback.onSelectAllRequested = onSelectAllRequested
        textActionModeCallback.onTranslateRequested = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            {
                // Get selected text by copying it, then restore the previous clip
                val clipboardManager = view.context.clipboardManager
                val previousClipboard = clipboardManager.primaryClip
                onCopyRequested?.invoke()
                val text = clipboardManager.text
                if (previousClipboard != null) {
                    clipboardManager.setPrimaryClip(previousClipboard)
                } else {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, " "))
                }

                val intent = Intent().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        action = Intent.ACTION_TRANSLATE
                        putExtra(Intent.EXTRA_TEXT, text.trim())
                    } else {
                        action = Intent.ACTION_PROCESS_TEXT
                        type = "text/plain"
                        putExtra(Intent.EXTRA_PROCESS_TEXT, text.trim())
                        putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
                    }
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                try {
                    view.context.startActivity(Intent.createChooser(intent, ""))
                } catch (e: Exception) {
                    SnackbarHelper.showSnackbar(view.context.getString(R.string.action_translate_no_app))
                }
            }
        } else {
            null
        }
        textActionModeCallback.onShareRequested = {
            // Get selected text by copying it, then restore the previous clip
            val clipboardManager = view.context.clipboardManager
            val previousClipboard = clipboardManager.primaryClip
            onCopyRequested?.invoke()
            val text = clipboardManager.text
            if (previousClipboard != null) {
                clipboardManager.setPrimaryClip(previousClipboard)
            } else {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, " "))
            }

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, view.context.getString(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, text.trim())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                view.context.startActivity(Intent.createChooser(intent, ""))
            } catch (e: Exception) {
                SnackbarHelper.showSnackbar(view.context.getString(R.string.action_share_no_app))
            }
        }
        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    TextToolbarHelperMethods.startActionMode(
                        view,
                        BreezyFloatingTextActionModeCallback(textActionModeCallback),
                        ActionMode.TYPE_FLOATING
                    )
                } else {
                    view.startActionMode(BreezyPrimaryTextActionModeCallback(textActionModeCallback))
                }
        } else {
            actionMode?.invalidate()
        }
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {
        showMenu(
            rect = rect,
            onCopyRequested = onCopyRequested,
            onPasteRequested = onPasteRequested,
            onCutRequested = onCutRequested,
            onSelectAllRequested = onSelectAllRequested
        )
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }
}

/**
 * This class is here to ensure that the classes that use this API will get verified and can be AOT
 * compiled. It is expected that this class will soft-fail verification, but the classes which use
 * this method will pass.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
internal object TextToolbarHelperMethods {
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun startActionMode(
        view: View,
        actionModeCallback: ActionMode.Callback,
        type: Int,
    ): ActionMode? {
        return view.startActionMode(actionModeCallback, type)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun invalidateContentRect(actionMode: ActionMode) {
        actionMode.invalidateContentRect()
    }
}
