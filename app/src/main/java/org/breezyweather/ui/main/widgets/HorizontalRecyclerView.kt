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
package org.breezyweather.ui.main.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * This recycler view handles touch events when used inside an already horizontally scrollable/swipeable container
 */
open class HorizontalRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RecyclerView(context, attrs, defStyle) {
    private var mPointerId = 0
    private var mInitialX = 0f
    private var mInitialY = 0f
    private val mTouchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop
    private var mBeingDragged = false
    private var mHorizontalDragged = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mBeingDragged = false
                mHorizontalDragged = false

                mPointerId = ev.getPointerId(0)
                mInitialX = ev.x
                mInitialY = ev.y

                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                mPointerId = ev.getPointerId(index)
                mInitialX = ev.getX(index)
                mInitialY = ev.getY(index)
            }

            MotionEvent.ACTION_MOVE -> {
                val index = ev.findPointerIndex(mPointerId)
                if (index == -1) {
                    Log.e(TAG, "Invalid pointerId=$mPointerId in onTouchEvent")
                } else {
                    val x = ev.getX(index)
                    val y = ev.getY(index)

                    if (!mBeingDragged && !mHorizontalDragged) {
                        if (abs(x - mInitialX) > mTouchSlop || abs(y - mInitialY) > mTouchSlop) {
                            mBeingDragged = true
                            if (abs(x - mInitialX) > abs(y - mInitialY)) {
                                mHorizontalDragged = true
                            } else {
                                parent.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val index = ev.actionIndex
                val id = ev.getPointerId(index)
                if (mPointerId == id) {
                    val newIndex = if (index == 0) 1 else 0

                    this.mPointerId = ev.getPointerId(newIndex)
                    mInitialX = ev.getX(newIndex).toInt().toFloat()
                    mInitialY = ev.getY(newIndex).toInt().toFloat()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mBeingDragged = false
                mHorizontalDragged = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        return super.onInterceptTouchEvent(ev) && mBeingDragged && mHorizontalDragged
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                mPointerId = ev.getPointerId(index)
                mInitialX = ev.getX(index)
                mInitialY = ev.getY(index)
            }

            MotionEvent.ACTION_MOVE -> {
                val index = ev.findPointerIndex(mPointerId)
                if (index == -1) {
                    Log.e(TAG, "Invalid pointerId=$mPointerId in onTouchEvent")
                } else {
                    val x = ev.getX(index)
                    val y = ev.getY(index)

                    if (!mBeingDragged && !mHorizontalDragged) {
                        mBeingDragged = true
                        if (abs(x - mInitialX) > abs(y - mInitialY)) {
                            mHorizontalDragged = true
                        } else {
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val index = ev.actionIndex
                val id = ev.getPointerId(index)
                if (mPointerId == id) {
                    val newIndex = if (index == 0) 1 else 0

                    this.mPointerId = ev.getPointerId(newIndex)
                    mInitialX = ev.getX(newIndex).toInt().toFloat()
                    mInitialY = ev.getY(newIndex).toInt().toFloat()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mBeingDragged = false
                mHorizontalDragged = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        return super.onTouchEvent(ev)
    }

    companion object {
        private const val TAG = "HorizontalRecyclerView"
    }
}
