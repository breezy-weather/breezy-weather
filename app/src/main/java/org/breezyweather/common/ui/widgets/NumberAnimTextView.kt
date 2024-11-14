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

package org.breezyweather.common.ui.widgets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.text.BidiFormatter
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * 数字增加动画的　TextView
 *
 * @author bakumon
 * @date 16-11-26
 * @url https://github.com/Bakumon/NumberAnimTextView
 */
@SuppressLint("AppCompatCustomView")
class NumberAnimTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : TextView(context, attrs, defStyleAttr) {
    /**
     * 起始值 默认 0
     */
    private var mNumStart = "0"

    /**
     * 结束值
     */
    private var mNumEnd: String? = null

    /**
     * 动画总时间 默认 2000 毫秒
     */
    var duration: Long = 2000

    /**
     * 前缀
     */
    var prefixString = ""

    /**
     * 后缀
     */
    var postfixString = ""

    /**
     * 是否开启动画
     */
    var isAnimEnabled = true

    /**
     * 是否是整数
     */
    private var isInt = false
    private var animator: ValueAnimator? = null
    fun setNumberString(number: String) {
        setNumberString("0", number)
    }

    @SuppressLint("SetTextI18n")
    fun setNumberString(numberStart: String, numberEnd: String) {
        mNumStart = numberStart
        mNumEnd = numberEnd
        if (checkNumString(numberStart, numberEnd)) {
            // 数字合法　开始数字动画
            start()
        } else {
            // 数字不合法　直接调用　setText　设置最终值
            text = prefixString + BidiFormatter.getInstance()
                .unicodeWrap(numberEnd) + postfixString
        }
    }

    /**
     * 校验数字的合法性
     *
     * @param numberStart 　开始的数字
     * @param numberEnd   　结束的数字
     * @return 合法性
     */
    private fun checkNumString(numberStart: String, numberEnd: String): Boolean {
        val regexInteger = "-?\\d*"
        isInt = numberEnd.matches(regexInteger.toRegex()) && numberStart.matches(regexInteger.toRegex())
        if (isInt) {
            return true
        }
        val regexDecimal = "-?[1-9]\\d*.\\d*|-?0.\\d*[1-9]\\d*"
        if ("0" == numberStart) {
            if (numberEnd.matches(regexDecimal.toRegex())) {
                return true
            }
        }
        return numberEnd.matches(regexDecimal.toRegex()) && numberStart.matches(regexDecimal.toRegex())
    }

    @SuppressLint("SetTextI18n")
    private fun start() {
        if (!isAnimEnabled) {
            // 禁止动画
            text = prefixString + format(BigDecimal(mNumEnd)) + postfixString
            return
        }
        val f = BidiFormatter.getInstance()
        animator = ValueAnimator.ofObject(
            BigDecimalEvaluator(),
            BigDecimal(mNumStart),
            BigDecimal(mNumEnd)
        ).apply {
            duration = this@NumberAnimTextView.duration
            interpolator = DecelerateInterpolator(3f)
            addUpdateListener { valueAnimator: ValueAnimator ->
                val value = valueAnimator.animatedValue as BigDecimal
                text = prefixString + f.unicodeWrap(format(value)) + postfixString
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    text = prefixString + f.unicodeWrap(mNumEnd) + postfixString
                }
            })
        }.also { it.start() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    /**
     * 格式化 BigDecimal ,小数部分时保留两位小数并四舍五入
     *
     * @param bd 　BigDecimal
     * @return 格式化后的 String
     */
    private fun format(bd: BigDecimal): String {
        val pattern = StringBuilder()
        if (isInt) {
            pattern.append("#,###")
        } else {
            var length = 0
            val s1 = mNumStart.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val s2 = mNumEnd!!.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val s = if (s1.size > s2.size) s1 else s2
            if (s.size > 1) {
                // 小数部分
                val decimals = s[1]
                if (decimals != null) {
                    length = decimals.length
                }
            }
            pattern.append("#,##0")
            if (length > 0) {
                pattern.append(".")
                for (i in 0 until length) {
                    pattern.append("0")
                }
            }
        }
        val df = DecimalFormat(pattern.toString())
        return df.format(bd)
    }

    private class BigDecimalEvaluator : TypeEvaluator<Any> {
        override fun evaluate(fraction: Float, startValue: Any, endValue: Any): Any {
            val start = startValue as BigDecimal
            val end = endValue as BigDecimal
            val result = end.subtract(start)
            return result.multiply(BigDecimal(fraction.toDouble())).add(start)
        }
    }
}
