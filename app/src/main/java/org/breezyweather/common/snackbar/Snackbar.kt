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

package org.breezyweather.common.snackbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.behavior.SwipeDismissBehavior
import org.breezyweather.R
import org.breezyweather.common.extensions.doOnApplyWindowInsets

class Snackbar private constructor(
    private val mParent: ViewGroup,
    private val mCardStyle: Boolean,
) {
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG)
    annotation class Duration

    private val mContext: Context = mParent.context
    private val mView: SnackbarLayout

    @get:Duration
    var duration = 0
        private set
    private var mCallback: Callback? = null
    private var mAnimator: Animator? = null

    class Callback {
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(
            DISMISS_EVENT_SWIPE,
            DISMISS_EVENT_ACTION,
            DISMISS_EVENT_TIMEOUT,
            DISMISS_EVENT_MANUAL,
            DISMISS_EVENT_CONSECUTIVE
        )
        annotation class DismissEvent

        fun onDismissed(snackbar: Snackbar?, @DismissEvent event: Int) {}
        fun onShown(snackbar: Snackbar?) {}

        companion object {
            const val DISMISS_EVENT_SWIPE = 0
            const val DISMISS_EVENT_ACTION = 1
            const val DISMISS_EVENT_TIMEOUT = 2
            const val DISMISS_EVENT_MANUAL = 3
            const val DISMISS_EVENT_CONSECUTIVE = 4
        }
    }

    private val mManagerCallback: SnackbarManager.Callback = object : SnackbarManager.Callback {
        override fun show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, this@Snackbar))
        }

        override fun dismiss(event: Int) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0, this@Snackbar))
        }
    }

    init {
        mView = LayoutInflater.from(mContext).inflate(
            if (mCardStyle) R.layout.container_snackbar_layout_card else R.layout.container_snackbar_layout,
            mParent,
            false
        ) as SnackbarLayout

        view.doOnApplyWindowInsets { v, insets ->
            v.updatePadding(
                left = insets.left,
                right = insets.right
            )
        }
    }

    fun setAction(@StringRes resId: Int, listener: View.OnClickListener?): Snackbar {
        return setAction(mContext.getText(resId), listener)
    }

    fun setAction(text: CharSequence?, listener: View.OnClickListener?): Snackbar {
        return setAction(text, true, listener)
    }

    private fun setAction(
        text: CharSequence?,
        shouldDismissOnClick: Boolean,
        listener: View.OnClickListener?,
    ): Snackbar {
        mView.actionView?.let { tv ->
            if (text.isNullOrEmpty() || listener == null) {
                tv.visibility = View.GONE
                tv.setOnClickListener(null)
            } else {
                tv.visibility = View.VISIBLE
                tv.text = text
                tv.setOnClickListener { view: View? ->
                    listener.onClick(view)
                    if (shouldDismissOnClick) {
                        dispatchDismiss(Callback.DISMISS_EVENT_ACTION)
                    }
                }
            }
        }
        return this
    }

    fun setActionTextColor(colors: ColorStateList?): Snackbar {
        mView.actionView?.setTextColor(colors)
        return this
    }

    fun setActionTextColor(@ColorInt color: Int): Snackbar {
        mView.actionView?.setTextColor(color)
        return this
    }

    fun setText(message: CharSequence): Snackbar {
        mView.messageView?.text = message
        return this
    }

    fun setText(@StringRes resId: Int): Snackbar {
        return setText(mContext.getText(resId))
    }

    fun setDuration(@Duration duration: Int): Snackbar {
        this.duration = duration
        return this
    }

    val view: View
        get() = mView

    fun show() {
        SnackbarManager.instance.show(duration, mManagerCallback)
    }

    fun dismiss() {
        dispatchDismiss(Callback.DISMISS_EVENT_MANUAL)
    }

    private fun dispatchDismiss(@Callback.DismissEvent event: Int) {
        SnackbarManager.instance.dismiss(mManagerCallback, event)
    }

    fun setCallback(callback: Callback?): Snackbar {
        mCallback = callback
        return this
    }

    val isShown: Boolean
        get() = SnackbarManager.instance.isCurrent(mManagerCallback)
    val isShownOrQueued: Boolean
        get() = SnackbarManager.instance.isCurrentOrNext(mManagerCallback)

    fun showView() {
        if (mView.parent == null) {
            val lp = mView.layoutParams
            if (lp is CoordinatorLayout.LayoutParams) {
                lp.behavior = Behavior().apply {
                    setStartAlphaSwipeDistance(0.1f)
                    setEndAlphaSwipeDistance(0.6f)
                    setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END)
                    listener = object : SwipeDismissBehavior.OnDismissListener {
                        override fun onDismiss(view: View) {
                            dispatchDismiss(Callback.DISMISS_EVENT_SWIPE)
                        }

                        override fun onDragStateChanged(state: Int) {
                            when (state) {
                                SwipeDismissBehavior.STATE_DRAGGING, SwipeDismissBehavior.STATE_SETTLING ->
                                    SnackbarManager.instance.cancelTimeout(mManagerCallback)

                                SwipeDismissBehavior.STATE_IDLE ->
                                    SnackbarManager.instance.restoreTimeout(mManagerCallback)
                            }
                        }
                    }
                }
            }
            mParent.addView(mView)
        }
        mView.setOnAttachStateChangeListener(object : SnackbarLayout.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {}
            override fun onViewDetachedFromWindow(v: View?) {
                if (isShownOrQueued) {
                    sHandler.post { onViewHidden(Callback.DISMISS_EVENT_MANUAL) }
                }
            }
        })
        if (mView.isLaidOut) {
            animateViewIn()
        } else {
            mView.setOnLayoutChangeListener { _: View?, _: Int, _: Int, _: Int, _: Int ->
                animateViewIn()
                mView.setOnLayoutChangeListener(null)
            }
        }
    }

    private fun animateViewIn() {
        mAnimator?.cancel()
        mAnimator = SnackbarAnimationUtils.getEnterAnimator(mView, mCardStyle).apply {
            duration = ANIMATION_DURATION.toLong()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    mView.animateChildrenIn(
                        ANIMATION_DURATION - ANIMATION_FADE_DURATION,
                        ANIMATION_FADE_DURATION
                    )
                }

                override fun onAnimationEnd(animation: Animator) {
                    mCallback?.onShown(this@Snackbar)
                    SnackbarManager.instance.onShown(mManagerCallback)
                }
            })
        }.also { it.start() }
    }

    private fun animateViewOut(event: Int) {
        mAnimator?.cancel()
        mAnimator = ObjectAnimator.ofFloat(
            mView,
            "translationY",
            mView.translationY,
            mView.height.toFloat()
        ).apply {
            duration = ANIMATION_DURATION.toLong()
            interpolator = SnackbarAnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    mView.animateChildrenOut(0, ANIMATION_FADE_DURATION)
                }

                override fun onAnimationEnd(animation: Animator) {
                    onViewHidden(event)
                }
            })
        }.also { it.start() }
    }

    fun hideView(event: Int) {
        if (mView.visibility != View.VISIBLE || isBeingDragged) {
            onViewHidden(event)
        } else {
            animateViewOut(event)
        }
    }

    private fun onViewHidden(event: Int) {
        SnackbarManager.instance.onDismissed(mManagerCallback)
        mCallback?.onDismissed(this, event)
        val parent = mView.parent
        if (parent is ViewGroup) {
            parent.removeView(mView)
        }
    }

    private val isBeingDragged: Boolean
        get() {
            val lp = mView.layoutParams
            if (lp is CoordinatorLayout.LayoutParams) {
                val behavior = lp.behavior
                if (behavior is SwipeDismissBehavior<*>) {
                    return (behavior.dragState != SwipeDismissBehavior.STATE_IDLE)
                }
            }
            return false
        }

    open class SnackbarLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : ViewGroup(context, attrs) {
        private val mWindowInsets: Rect = Rect()
        var messageView: TextView? = null
            private set
        var actionView: Button? = null
            private set

        interface OnAttachStateChangeListener {
            fun onViewAttachedToWindow(v: View?)
            fun onViewDetachedFromWindow(v: View?)
        }

        private var mOnLayoutChangeListener: (
            (view: View?, left: Int, top: Int, right: Int, bottom: Int) -> Unit
        )? = null
        private var mOnAttachStateChangeListener: OnAttachStateChangeListener? = null

        init {
            context.withStyledAttributes(
                attrs,
                com.google.android.material.R.styleable.SnackbarLayout
            ) {}
            isClickable = true
            fitsSystemWindows = false
            LayoutInflater.from(context).inflate(layoutId, this)
            accessibilityLiveRegion = ACCESSIBILITY_LIVE_REGION_POLITE
        }

        @Deprecated("Deprecated in Java")
        override fun requestFitSystemWindows() {
            // Do not apply horizontal insets in home fragment
            val isHomeFragment = parent is CoordinatorLayout
            val insets = ViewCompat.getRootWindowInsets(this)
            val i = insets?.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout())
            if (i != null) {
                val rInsets = Rect(
                    if (isHomeFragment) 0 else i.left,
                    i.top,
                    if (isHomeFragment) 0 else i.right,
                    i.bottom
                )
                mWindowInsets.set(rInsets)
            }
        }

        @get:LayoutRes
        open val layoutId: Int
            get() = R.layout.container_snackbar_layout_inner

        override fun onFinishInflate() {
            super.onFinishInflate()
            messageView = findViewById(R.id.snackbar_text)
            actionView = findViewById(R.id.snackbar_action)
        }

        override fun generateLayoutParams(p: LayoutParams): LayoutParams {
            return MarginLayoutParams(p)
        }

        override fun generateDefaultLayoutParams(): LayoutParams {
            return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
            return MarginLayoutParams(context, attrs)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width: Int
            val height: Int
            val child = getChildAt(0)
            val widthUsed = mWindowInsets.left + mWindowInsets.right
            val heightUsed = mWindowInsets.bottom
            measureChildWithMargins(
                child,
                widthMeasureSpec,
                widthUsed,
                heightMeasureSpec,
                heightUsed
            )
            val lp = child.layoutParams as MarginLayoutParams
            width = child.measuredWidth + widthUsed + lp.leftMargin + lp.rightMargin + paddingLeft + paddingRight
            height = child.measuredHeight + heightUsed + lp.topMargin + lp.bottomMargin + paddingTop + paddingBottom
            setMeasuredDimension(width, height)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            val child = getChildAt(0)

            // Set left insets for system bars and evenly align the snackbar
            // within the safe drawing area.
            val x = mWindowInsets.left +
                (measuredWidth - child.measuredWidth - mWindowInsets.left - mWindowInsets.right).div(2)
            child.layout(x, 0, x + child.measuredWidth, child.measuredHeight)
            mOnLayoutChangeListener?.invoke(this, l, t, r, b)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            mOnAttachStateChangeListener?.onViewAttachedToWindow(this)
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            mOnAttachStateChangeListener?.onViewDetachedFromWindow(this)
        }

        fun animateChildrenIn(delay: Int, duration: Int) {
            messageView?.let { mView ->
                mView.alpha = 0f
                mView.animate()
                    .alpha(1f)
                    .setDuration(duration.toLong())
                    .setStartDelay(delay.toLong())
                    .start()

                actionView?.let { aView ->
                    if (aView.isVisible) {
                        aView.alpha = 0f
                        aView.animate()
                            .alpha(1f)
                            .setDuration(duration.toLong())
                            .setStartDelay(delay.toLong())
                            .start()
                    }
                }
            }
        }

        fun animateChildrenOut(delay: Int, duration: Int) {
            messageView?.let { mView ->
                mView.alpha = 1f
                mView.animate()
                    .alpha(0f)
                    .setDuration(duration.toLong())
                    .setStartDelay(delay.toLong())
                    .start()

                actionView?.let { aView ->
                    if (aView.isVisible) {
                        aView.alpha = 1f
                        mView.animate()
                            .alpha(0f)
                            .setDuration(duration.toLong())
                            .setStartDelay(delay.toLong())
                            .start()
                    }
                }
            }
        }

        fun setOnLayoutChangeListener(
            onLayoutChangeListener: ((view: View?, left: Int, top: Int, right: Int, bottom: Int) -> Unit)?,
        ) {
            mOnLayoutChangeListener = onLayoutChangeListener
        }

        fun setOnAttachStateChangeListener(listener: OnAttachStateChangeListener?) {
            mOnAttachStateChangeListener = listener
        }
    }

    class CardSnackbarLayout : SnackbarLayout {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        override val layoutId: Int
            get() = R.layout.container_snackbar_layout_inner_card
    }

    internal inner class Behavior : SwipeDismissBehavior<SnackbarLayout>() {
        override fun canSwipeDismissView(child: View): Boolean {
            return child is SnackbarLayout
        }

        override fun onInterceptTouchEvent(
            parent: CoordinatorLayout,
            child: SnackbarLayout,
            event: MotionEvent,
        ): Boolean {
            if (parent.isPointInChildBounds(child, event.x.toInt(), event.y.toInt())) {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> SnackbarManager.instance.cancelTimeout(mManagerCallback)

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                        SnackbarManager.instance.restoreTimeout(mManagerCallback)
                }
            }
            return super.onInterceptTouchEvent(parent, child, event)
        }
    }

    companion object {
        const val LENGTH_INDEFINITE = -2
        const val LENGTH_SHORT = -1
        const val LENGTH_LONG = 0
        const val ANIMATION_DURATION = 450
        const val ANIMATION_FADE_DURATION = 200
        private val sHandler: Handler = Handler(
            Looper.getMainLooper(),
            Handler.Callback { message: Message ->
                when (message.what) {
                    MSG_SHOW -> {
                        (message.obj as Snackbar).showView()
                        return@Callback true
                    }

                    MSG_DISMISS -> {
                        (message.obj as Snackbar).hideView(message.arg1)
                        return@Callback true
                    }
                }
                false
            }
        )
        private const val MSG_SHOW = 0
        private const val MSG_DISMISS = 1

        fun make(
            view: View,
            text: CharSequence,
            @Duration duration: Int,
            cardStyle: Boolean,
        ): Snackbar {
            val snackbar = Snackbar(findSuitableParent(view), cardStyle)
            snackbar.setText(text)
            snackbar.setDuration(duration)
            return snackbar
        }

        fun make(
            view: View,
            @StringRes resId: Int,
            @Duration duration: Int,
            cardStyle: Boolean,
        ): Snackbar {
            return make(view, view.resources.getText(resId), duration, cardStyle)
        }

        private fun findSuitableParent(viewP: View): ViewGroup {
            var view: View? = viewP
            do {
                if (view is CoordinatorLayout) {
                    // We've found a CoordinatorLayout, use it
                    return view
                } else if (view is FrameLayout) {
                    if (view.getId() == android.R.id.content) {
                        // If we've hit the decor content view, then we didn't find a CoL in the
                        // hierarchy, so use it.
                        return view
                    }
                }
                if (view != null) {
                    // Else, we will loop and crawl up the view hierarchy and try to find a parent
                    val parent = view.parent
                    view = if (parent is View) parent else null
                }
            } while (view != null)
            throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )
        }
    }
}
