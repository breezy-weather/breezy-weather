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

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.geometry.Rect
import org.breezyweather.R

internal class BreezyTextActionModeCallback(
    val onActionModeDestroy: ((mode: ActionMode?) -> Unit)? = null,
    var rect: Rect = Rect.Zero,
    var onCopyRequested: (() -> Unit)? = null,
    var onSelectAllRequested: (() -> Unit)? = null,
    var onTranslateRequested: (() -> Unit)? = null,
    var onShareRequested: (() -> Unit)? = null,
) : ActionMode.Callback {
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        requireNotNull(menu) { "onCreateActionMode requires a non-null menu" }
        requireNotNull(mode) { "onCreateActionMode requires a non-null mode" }

        onCopyRequested?.let { addMenuItem(menu, MenuItemOption.Copy) }
        onSelectAllRequested?.let { addMenuItem(menu, MenuItemOption.SelectAll) }
        onTranslateRequested?.let { addMenuItem(menu, MenuItemOption.Translate) }
        onShareRequested?.let { addMenuItem(menu, MenuItemOption.Share) }
        return true
    }

    // this method is called to populate new menu items when the actionMode was invalidated
    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        if (mode == null || menu == null) return false
        updateMenuItems(menu)
        // should return true so that new menu items are populated
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item!!.itemId) {
            MenuItemOption.Copy.id -> onCopyRequested?.invoke()
            MenuItemOption.SelectAll.id -> onSelectAllRequested?.invoke()
            MenuItemOption.Translate.id -> onTranslateRequested?.invoke()
            MenuItemOption.Share.id -> onShareRequested?.invoke()
            else -> return false
        }
        mode?.finish()
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        onActionModeDestroy?.invoke(mode)
    }

    @VisibleForTesting
    internal fun updateMenuItems(menu: Menu) {
        addOrRemoveMenuItem(menu, MenuItemOption.Copy, onCopyRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.SelectAll, onSelectAllRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.Translate, onTranslateRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.Share, onShareRequested)
    }

    internal fun addMenuItem(menu: Menu, item: MenuItemOption) {
        menu
            .add(0, item.id, item.order, item.titleResource)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }

    private fun addOrRemoveMenuItem(menu: Menu, item: MenuItemOption, callback: (() -> Unit)?) {
        when {
            callback != null && menu.findItem(item.id) == null -> addMenuItem(menu, item)
            callback == null && menu.findItem(item.id) != null -> menu.removeItem(item.id)
        }
    }
}

internal enum class MenuItemOption(val id: Int) {
    Copy(0),
    SelectAll(1),
    Translate(2),
    Share(3),
    ;

    val titleResource: Int
        get() =
            when (this) {
                Copy -> android.R.string.copy
                SelectAll -> android.R.string.selectAll
                Translate -> R.string.action_translate
                Share -> R.string.action_share
            }

    /** This item will be shown before all items that have order greater than this value. */
    val order = id
}
