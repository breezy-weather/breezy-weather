/**
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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.withStyledAttributes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import org.breezyweather.R

class TagView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private val mOutline: RectF = RectF()
    private val mPaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private var mChecked = false

    @ColorInt
    private var mCheckedBackgroundColor = 0

    @ColorInt
    private var mUncheckedBackgroundColor = 0

    init {
        context.withStyledAttributes(attrs, R.styleable.TagView, defStyleAttr, 0) {
            mChecked = getBoolean(R.styleable.TagView_checked, false)
            mCheckedBackgroundColor =
                getColor(R.styleable.TagView_checked_background_color, Color.WHITE)
            mUncheckedBackgroundColor =
                getColor(R.styleable.TagView_unchecked_background_color, Color.LTGRAY)
        }

        ViewCompat.setAccessibilityDelegate(
            this,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfoCompat,
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.roleDescription = context.resources.getString(androidx.compose.ui.R.string.tab)
                }
            }
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mOutline.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, viewOutline: Outline) {
                viewOutline.setRoundRect(
                    mOutline.left.toInt(),
                    mOutline.top.toInt(),
                    mOutline.right.toInt(),
                    mOutline.bottom.toInt(),
                    mOutline.height() / 2
                )
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        mPaint.color = if (mChecked) mCheckedBackgroundColor else mUncheckedBackgroundColor
        canvas.drawRoundRect(mOutline, mOutline.height() / 2, mOutline.height() / 2, mPaint)
        super.onDraw(canvas)
    }

    var isChecked: Boolean
        get() = mChecked
        set(checked) {
            mChecked = checked
            invalidate()
        }
    var checkedBackgroundColor: Int
        get() = mCheckedBackgroundColor
        set(checkedBackgroundColor) {
            mCheckedBackgroundColor = checkedBackgroundColor
            invalidate()
        }
    var uncheckedBackgroundColor: Int
        get() = mUncheckedBackgroundColor
        set(uncheckedBackgroundColor) {
            mUncheckedBackgroundColor = uncheckedBackgroundColor
            invalidate()
        }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.isSelected = isChecked
        info.isCheckable = false
        info.isClickable = !isChecked // TODO: Not working
    }
}
