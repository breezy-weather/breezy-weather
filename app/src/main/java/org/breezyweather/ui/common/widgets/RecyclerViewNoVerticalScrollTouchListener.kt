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

package org.breezyweather.ui.common.widgets

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class RecyclerViewNoVerticalScrollTouchListener : RecyclerView.OnItemTouchListener {
    private var mLastX = 0f
    private var mLastY = 0f

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastX = e.x
                mLastY = e.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(e.x - mLastX) > abs(e.y - mLastY)) {
                    rv.parent.requestDisallowInterceptTouchEvent(true)
                }
                mLastX = e.x
                mLastY = e.y
            }
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}
