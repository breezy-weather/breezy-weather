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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import org.breezyweather.R
import org.breezyweather.common.extensions.getTypefaceFromTextAppearance
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Ink page indicator.
 */
class InkPageIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle), SwipeSwitchLayout.OnPagerSwipeListener, View.OnAttachStateChangeListener {
    // configurable attributes
    private val mDotDiameter: Int
    private val mGap: Int
    private val mAnimDuration: Long
    private val mUnselectedColour: Int
    private val mSelectedColour: Int

    // derived from attributes
    private val mDotRadius: Float
    private val mHalfDotRadius: Float
    private val mAnimHalfDuration: Long
    private var mDotTopY = 0f
    private var mDotCenterY = 0f
    private var mDotBottomY = 0f

    // ViewPager
    private var mSwitchView: SwipeSwitchLayout? = null

    // state
    private var mPageCount = 0
    private var mCurrentPage = 0
    private var mPreviousPage = 0
    private var mSelectedDotX = 0f
    private var mSelectedDotInPosition = false
    private var mDotCenterX: FloatArray = FloatArray(0)
    private var mJoiningFractions: FloatArray = FloatArray(0)
    private var mRetreatingJoinX1 = 0f
    private var mRetreatingJoinX2 = 0f
    private var mDotRevealFractions: FloatArray = FloatArray(0)
    private var mIsAttachedToWindow = false
    private var mPageChanging = false
    private var mShowing: Boolean

    // drawing
    private val mUnselectedPaint = Paint().apply {
        isAntiAlias = true
    }
    private val mSelectedPaint = Paint().apply {
        isAntiAlias = true
    }
    private val mTextPaint = Paint().apply {
        isAntiAlias = true
    }
    private val mCombinedUnselectedPath = Path()
    private val mUnselectedDotPath = Path()
    private val mUnselectedDotLeftPath = Path()
    private val mUnselectedDotRightPath = Path()
    private val mRectF = RectF()

    // animation
    private var mMoveAnimation: ValueAnimator? = null
    private val mJoiningAnimationSet: AnimatorSet? = null
    private var mRetreatAnimation: PendingRetreatAnimator? = null
    private var mRevealAnimations: Array<PendingRevealAnimator> = arrayOf()
    private val mInterpolator: Interpolator = FastOutSlowInInterpolator()
    private val mShowAnimator: ObjectAnimator
    private val mDismissAnimator: ObjectAnimator

    // working values for beziers
    private var endX1 = 0f
    private var endY1 = 0f
    private var endX2 = 0f
    private var endY2 = 0f
    private var controlX1 = 0f
    private var controlY1 = 0f
    private var controlX2 = 0f
    private var controlY2 = 0f

    init {
        val density = context.resources.displayMetrics.density.toInt()

        // Load attributes
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.InkPageIndicator, defStyle, 0)
        mDotDiameter = a.getDimensionPixelSize(R.styleable.InkPageIndicator_dotDiameter, DEFAULT_DOT_SIZE * density)
        mDotRadius = mDotDiameter / 2f
        mHalfDotRadius = mDotRadius / 2f
        mGap = a.getDimensionPixelSize(R.styleable.InkPageIndicator_dotGap, DEFAULT_GAP * density)
        mAnimDuration = a.getInteger(R.styleable.InkPageIndicator_animationDuration, DEFAULT_ANIM_DURATION).toLong()
        mAnimHalfDuration = mAnimDuration / 2
        mUnselectedColour = a.getColor(R.styleable.InkPageIndicator_pageIndicatorColor, DEFAULT_UNSELECTED_COLOUR)
        mSelectedColour = a.getColor(R.styleable.InkPageIndicator_currentPageIndicatorColor, DEFAULT_SELECTED_COLOUR)
        a.recycle()
        mUnselectedPaint.color = mUnselectedColour
        mSelectedPaint.color = mSelectedColour
        mTextPaint.apply {
            color = mSelectedColour
            typeface = getContext().getTypefaceFromTextAppearance(R.style.subtitle_text)
        }

        // create paths & rect now – reuse & rewind later
        addOnAttachStateChangeListener(this)
        mShowing = false
        alpha = 0f
        mShowAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, MAX_ALPHA).apply {
            duration = 100
        }
        mDismissAnimator = ObjectAnimator.ofFloat(this, "alpha", MAX_ALPHA, 0f).apply {
            duration = 200
            startDelay = 600
        }
    }

    fun setSwitchView(switchView: SwipeSwitchLayout) {
        mSwitchView = switchView
        switchView.setOnPageSwipeListener(this)
        setPageCount(switchView.totalCount)
        setCurrentPageImmediate()
    }

    fun setDisplayState(show: Boolean) {
        if (mShowing == show) return

        mShowing = show
        mDismissAnimator.cancel()
        if (show) {
            mShowAnimator.cancel()
            if (alpha != MAX_ALPHA) {
                mShowAnimator.setFloatValues(alpha, 0.7f)
                mShowAnimator.start()
            }
        } else {
            mDismissAnimator.start()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (mIsAttachedToWindow) {
            if (position < 0 || position > mPageCount - 1) {
                return
            }
            var fraction = positionOffset
            val currentPosition = if (mPageChanging) mPreviousPage else mCurrentPage
            var leftDotPosition = position
            // when swiping from #2 to #1 ViewPager reports position as 1 and a descending offset
            // need to convert this into our left-dot-based 'coordinate space'
            if (currentPosition != position) {
                fraction = 1f - positionOffset

                // if user scrolls completely to next page then the position param updates to that
                // new page but we're not ready to switch our 'current' page yet so adjust for that
                if (fraction == 1f) {
                    leftDotPosition = min(currentPosition, position)
                }
            }
            setJoiningFraction(leftDotPosition, fraction)
        }
    }

    override fun onPageSelected(position: Int) {
        if (mIsAttachedToWindow) {
            // this is the main event we're interested in!
            setSelectedPage(position)
        } else {
            // when not attached, don't animate the move, just store immediately
            setCurrentPageImmediate()
        }
    }

    private fun setPageCount(pages: Int) {
        mPageCount = pages
        resetState()
        requestLayout()
    }

    fun setCurrentIndicatorColor(@ColorInt color: Int) {
        mSelectedPaint.color = color
        mTextPaint.color = color
        invalidate()
    }

    fun setIndicatorColor(@ColorInt color: Int) {
        mUnselectedPaint.color = color
        invalidate()
    }

    private fun calculateDotPositions(width: Int, height: Int) {
        val left = paddingLeft
        val top = paddingTop
        val right = width - paddingRight
        val bottom = height - paddingBottom
        val requiredWidth = requiredWidth
        val startLeft = left + (right - left - requiredWidth) / 2f + mDotRadius
        mDotCenterX = FloatArray(mPageCount) { i ->
            startLeft + i * (mDotDiameter + mGap)
        }
        // todo just top aligning for now… should make this smarter
        mDotTopY = top.toFloat()
        mDotCenterY = top + mDotRadius
        mDotBottomY = (top + mDotDiameter).toFloat()
        setCurrentPageImmediate()
    }

    private fun calculateTextSize(): Float {
        return (mDotDiameter + mGap / 2) * 1.2F
    }

    private fun setCurrentPageImmediate() {
        mCurrentPage = mSwitchView?.position ?: 0
        if (mDotCenterX.isNotEmpty() && (mMoveAnimation == null || !mMoveAnimation!!.isStarted)) {
            mSelectedDotX = mDotCenterX[mCurrentPage]
        }
    }

    private fun resetState() {
        mJoiningFractions = FloatArray(mPageCount - 1) { 0f }
        mDotRevealFractions = FloatArray(mPageCount) { 0f }
        mRetreatingJoinX1 = INVALID_FRACTION
        mRetreatingJoinX2 = INVALID_FRACTION
        mSelectedDotInPosition = true
        if (measuredHeight != 0 || measuredWidth != 0) {
            calculateDotPositions(measuredWidth, measuredHeight)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = desiredHeight
        val height: Int = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> min(
                desiredHeight,
                MeasureSpec.getSize(heightMeasureSpec)
            )
            else -> desiredHeight
        }
        val desiredWidth = desiredWidth
        val width: Int = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.AT_MOST -> min(
                desiredWidth,
                MeasureSpec.getSize(widthMeasureSpec)
            )
            else -> desiredWidth
        }
        setMeasuredDimension(width, height)
        calculateDotPositions(width, height)
    }

    private val desiredHeight: Int
        get() = paddingBottom + if (mPageCount > 7) {
            calculateTextSize().toInt()
        } else {
            mDotDiameter
        }
    private val requiredWidth: Int
        get() = mPageCount * mDotDiameter + (mPageCount - 1) * mGap
    private val desiredWidth: Int
        get() = paddingLeft + requiredWidth + paddingRight

    override fun onViewAttachedToWindow(view: View) {
        mIsAttachedToWindow = true
    }

    override fun onViewDetachedFromWindow(view: View) {
        mIsAttachedToWindow = false
    }

    override fun onDraw(canvas: Canvas) {
        if (mSwitchView == null || mPageCount == 0) return
        if (mPageCount > 7) {
            val cx = measuredWidth / 2
            mTextPaint.textAlign = Paint.Align.CENTER
            mTextPaint.textSize = calculateTextSize()
            val fontMetrics = mTextPaint.fontMetrics
            val baseLineY = paddingTop - fontMetrics.top - fontMetrics.bottom
            canvas.drawText(
                (mCurrentPage + 1).toString() + "/" + mPageCount,
                cx.toFloat(),
                baseLineY,
                mTextPaint
            )
            return
        }
        drawUnselected(canvas)
        drawSelected(canvas)
    }

    private fun drawUnselected(canvas: Canvas) {
        mCombinedUnselectedPath.rewind()

        // draw any settled, revealing or joining dots
        for (page in 0 until mPageCount) {
            val nextXIndex = if (page == mPageCount - 1) page else page + 1
            val unselectedPath = getUnselectedPath(
                page,
                mDotCenterX[page],
                mDotCenterX[nextXIndex],
                if (page == mPageCount - 1) INVALID_FRACTION else mJoiningFractions[page],
                mDotRevealFractions[page]
            )
            unselectedPath.addPath(mCombinedUnselectedPath)
            mCombinedUnselectedPath.addPath(unselectedPath)
        }
        // draw any retreating joins
        if (mRetreatingJoinX1 != INVALID_FRACTION) {
            val retreatingJoinPath = retreatingJoinPath
            mCombinedUnselectedPath.addPath(retreatingJoinPath)
        }
        canvas.drawPath(mCombinedUnselectedPath, mUnselectedPaint)
    }

    /**
     * Unselected dots can be in 6 states:
     *
     *
     * #1 At rest
     * #2 Joining neighbour, still separate
     * #3 Joining neighbour, combined curved
     * #4 Joining neighbour, combined straight
     * #5 Join retreating
     * #6 Dot re-showing / revealing
     *
     *
     * It can also be in a combination of these states e.g. joining one neighbour while
     * retreating from another.  We therefore create a Path so that we can examine each
     * dot pair separately and later take the union for these cases.
     *
     *
     * This function returns a path for the given dot **and any action to it's right** e.g. joining
     * or retreating from it's neighbour
     *
     * @param page
     * @return
     */
    private fun getUnselectedPath(
        page: Int,
        centerX: Float,
        nextCenterX: Float,
        joiningFraction: Float,
        dotRevealFraction: Float,
    ): Path {
        mUnselectedDotPath.rewind()
        if ((joiningFraction == 0f || joiningFraction == INVALID_FRACTION) &&
            dotRevealFraction == 0f &&
            !(page == mCurrentPage && mSelectedDotInPosition)
        ) {
            // case #1 – At rest
            mUnselectedDotPath.addCircle(
                mDotCenterX[page],
                mDotCenterY,
                mDotRadius,
                Path.Direction.CW
            )
        }
        if (joiningFraction > 0f && joiningFraction <= 0.5f && mRetreatingJoinX1 == INVALID_FRACTION) {
            // case #2 – Joining neighbour, still separate

            // start with the left dot
            mUnselectedDotLeftPath.rewind()

            // start at the bottom center
            mUnselectedDotLeftPath.moveTo(centerX, mDotBottomY)

            // semi circle to the top center
            mRectF.set(centerX - mDotRadius, mDotTopY, centerX + mDotRadius, mDotBottomY)
            mUnselectedDotLeftPath.arcTo(mRectF, 90f, 180f, true)

            // cubic to the right middle
            endX1 = centerX + mDotRadius + joiningFraction * mGap
            endY1 = mDotCenterY
            controlX1 = centerX + mHalfDotRadius
            controlY1 = mDotTopY
            controlX2 = endX1
            controlY2 = endY1 - mHalfDotRadius
            mUnselectedDotLeftPath.cubicTo(
                controlX1,
                controlY1,
                controlX2,
                controlY2,
                endX1,
                endY1
            )

            // cubic back to the bottom center
            endX2 = centerX
            endY2 = mDotBottomY
            controlX1 = endX1
            controlY1 = endY1 + mHalfDotRadius
            controlX2 = centerX + mHalfDotRadius
            controlY2 = mDotBottomY
            mUnselectedDotLeftPath.cubicTo(
                controlX1,
                controlY1,
                controlX2,
                controlY2,
                endX2,
                endY2
            )
            mUnselectedDotPath.addPath(mUnselectedDotLeftPath)

            // now do the next dot to the right
            mUnselectedDotRightPath.rewind()

            // start at the bottom center
            mUnselectedDotRightPath.moveTo(nextCenterX, mDotBottomY)

            // semi circle to the top center
            mRectF.set(nextCenterX - mDotRadius, mDotTopY, nextCenterX + mDotRadius, mDotBottomY)
            mUnselectedDotRightPath.arcTo(mRectF, 90f, -180f, true)

            // cubic to the left middle
            endX1 = nextCenterX - mDotRadius - joiningFraction * mGap
            endY1 = mDotCenterY
            controlX1 = nextCenterX - mHalfDotRadius
            controlY1 = mDotTopY
            controlX2 = endX1
            controlY2 = endY1 - mHalfDotRadius
            mUnselectedDotRightPath.cubicTo(
                controlX1,
                controlY1,
                controlX2,
                controlY2,
                endX1,
                endY1
            )

            // cubic back to the bottom center
            endX2 = nextCenterX
            endY2 = mDotBottomY
            controlX1 = endX1
            controlY1 = endY1 + mHalfDotRadius
            controlX2 = endX2 - mHalfDotRadius
            controlY2 = mDotBottomY
            mUnselectedDotRightPath.cubicTo(
                controlX1,
                controlY1,
                controlX2,
                controlY2,
                endX2,
                endY2
            )
            mUnselectedDotPath.addPath(mUnselectedDotRightPath)
        }
        if (joiningFraction > 0.5f && joiningFraction < 1f && mRetreatingJoinX1 == INVALID_FRACTION) {
            // case #3 – Joining neighbour, combined curved

            // adjust the fraction so that it goes from 0.3 -> 1 to produce a more realistic 'join'
            val adjustedFraction = (joiningFraction - 0.2f) * 1.25f

            // start in the bottom left
            mUnselectedDotPath.moveTo(centerX, mDotBottomY)

            // semi-circle to the top left
            mRectF.set(centerX - mDotRadius, mDotTopY, centerX + mDotRadius, mDotBottomY)
            mUnselectedDotPath.arcTo(mRectF, 90f, 180f, true)

            // bezier to the middle top of the join
            endX1 = centerX + mDotRadius + mGap / 2
            endY1 = mDotCenterY - adjustedFraction * mDotRadius
            controlX1 = endX1 - adjustedFraction * mDotRadius
            controlY1 = mDotTopY
            controlX2 = endX1 - (1 - adjustedFraction) * mDotRadius
            controlY2 = endY1
            mUnselectedDotPath.cubicTo(
                controlX1,
                controlY1,
                controlX2,
                controlY2,
                endX1,
                endY1
            )

            // bezier to the top right of the join
            endX2 = nextCenterX
            endY2 = mDotTopY
            controlX1 = endX1 + (1 - adjustedFraction) * mDotRadius
            controlY1 = endY1
            controlX2 = endX1 + adjustedFraction * mDotRadius
            controlY2 = mDotTopY
            mUnselectedDotPath.cubicTo(
                controlX1,
                controlY1,
                controlX2,
                controlY2,
                endX2,
                endY2
            )

            // semi-circle to the bottom right
            mRectF.set(nextCenterX - mDotRadius, mDotTopY, nextCenterX + mDotRadius, mDotBottomY)
            mUnselectedDotPath.arcTo(mRectF, 270f, 180f, true)

            // bezier to the middle bottom of the join
            // endX1 stays the same
            endY1 = mDotCenterY + adjustedFraction * mDotRadius
            controlX1 = endX1 + adjustedFraction * mDotRadius
            controlY1 = mDotBottomY
            controlX2 = endX1 + (1 - adjustedFraction) * mDotRadius
            controlY2 = endY1
            mUnselectedDotPath.cubicTo(
                controlX1,
                controlY1,
                controlX2,
                controlY2,
                endX1,
                endY1
            )

            // bezier back to the start point in the bottom left
            endX2 = centerX
            endY2 = mDotBottomY
            controlX1 = endX1 - (1 - adjustedFraction) * mDotRadius
            controlY1 = endY1
            controlX2 = endX1 - adjustedFraction * mDotRadius
            controlY2 = endY2
            mUnselectedDotPath.cubicTo(
                controlX1,
                controlY1,
                controlX2,
                controlY2,
                endX2,
                endY2
            )
        }
        if (joiningFraction == 1f && mRetreatingJoinX1 == INVALID_FRACTION) {
            // case #4 Joining neighbour, combined straight technically we could use case 3 for this
            // situation as well but assume that this is an optimization rather than faffing around
            // with beziers just to draw a rounded rect
            mRectF.set(centerX - mDotRadius, mDotTopY, nextCenterX + mDotRadius, mDotBottomY)
            mUnselectedDotPath.addRoundRect(mRectF, mDotRadius, mDotRadius, Path.Direction.CW)
        }

        // case #5 is handled by #getRetreatingJoinPath()
        // this is done separately so that we can have a single retreating path spanning
        // multiple dots and therefore animate it's movement smoothly
        if (dotRevealFraction > MINIMAL_REVEAL) {
            // case #6 – previously hidden dot revealing
            mUnselectedDotPath.addCircle(
                centerX,
                mDotCenterY,
                dotRevealFraction * mDotRadius,
                Path.Direction.CW
            )
        }
        return mUnselectedDotPath
    }

    private val retreatingJoinPath: Path
        get() {
            mUnselectedDotPath.rewind()
            mRectF.set(mRetreatingJoinX1, mDotTopY, mRetreatingJoinX2, mDotBottomY)
            mUnselectedDotPath.addRoundRect(mRectF, mDotRadius, mDotRadius, Path.Direction.CW)
            return mUnselectedDotPath
        }

    private fun drawSelected(canvas: Canvas) {
        canvas.drawCircle(mSelectedDotX, mDotCenterY, mDotRadius, mSelectedPaint)
    }

    private fun setSelectedPage(now: Int) {
        if (now == mCurrentPage) return
        mPageChanging = true
        mPreviousPage = mCurrentPage
        mCurrentPage = now
        val steps = abs(now - mPreviousPage)
        if (steps > 1) {
            if (now > mPreviousPage) {
                for (i in 0 until steps) {
                    setJoiningFraction(mPreviousPage + i, 1f)
                }
            } else {
                for (i in -1 downTo -steps + 1) {
                    setJoiningFraction(mPreviousPage + i, 1f)
                }
            }
        }

        // create the anim to move the selected dot – this animator will kick off
        // retreat animations when it has moved 75% of the way.
        // The retreat animation in turn will kick of reveal anims when the
        // retreat has passed any dots to be revealed
        mMoveAnimation = createMoveSelectedAnimator(mDotCenterX[now], mPreviousPage, now, steps)
            .also { it.start() }
    }

    private fun createMoveSelectedAnimator(
        moveTo: Float,
        was: Int,
        now: Int,
        steps: Int,
    ): ValueAnimator {
        // Set up a pending retreat anim – this starts when the move is 75% complete
        mRetreatAnimation = PendingRetreatAnimator(
            was,
            now,
            steps,
            if (now > was) {
                RightwardStartPredicate(moveTo - (moveTo - mSelectedDotX) * 0.25f)
            } else {
                LeftwardStartPredicate(moveTo + (mSelectedDotX - moveTo) * 0.25f)
            }
        ).apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    resetState()
                    mPageChanging = false
                }
            })
        }

        // also create the actual move animator
        val moveSelected = ValueAnimator.ofFloat(mSelectedDotX, moveTo).apply {
            addUpdateListener { valueAnimator: ValueAnimator ->
                // todo avoid autoboxing
                mSelectedDotX = valueAnimator.animatedValue as Float
                mRetreatAnimation!!.startIfNecessary(mSelectedDotX)
                postInvalidateOnAnimation()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    // set a flag so that we continue to draw the unselected dot in the target position
                    // until the selected dot has finished moving into place
                    mSelectedDotInPosition = false
                }

                override fun onAnimationEnd(animation: Animator) {
                    // set a flag when anim finishes so that we don't draw both selected & unselected
                    // page dots
                    mSelectedDotInPosition = true
                }
            })
            // slightly delay the start to give the joins a chance to run
            // unless dot isn't in position yet – then don't delay!
            startDelay = if (mSelectedDotInPosition) mAnimDuration / 4L else 0L
            duration = mAnimDuration * 3L / 4L
            interpolator = mInterpolator
        }
        return moveSelected
    }

    private fun setJoiningFraction(leftDot: Int, fraction: Float) {
        if (leftDot < mJoiningFractions.size) {
            /*if (leftDot == 1) {
                LogHelper.log("PageIndicator", "dot 1 fraction:\t$fraction")
            }*/
            mJoiningFractions[leftDot] = fraction
            postInvalidateOnAnimation()
        }
    }

    private fun clearJoiningFractions() {
        mJoiningFractions.map { 0f }
        postInvalidateOnAnimation()
    }

    private fun setDotRevealFraction(dot: Int, fraction: Float) {
        if (dot < mDotRevealFractions.size) {
            mDotRevealFractions[dot] = fraction
        }
        postInvalidateOnAnimation()
    }

    private fun cancelJoiningAnimations() {
        if (mJoiningAnimationSet != null && mJoiningAnimationSet.isRunning) {
            mJoiningAnimationSet.cancel()
        }
    }

    /**
     * A [ValueAnimator] that starts once a given predicate returns true.
     */
    abstract inner class PendingStartAnimator(
        private var predicate: StartPredicate,
    ) : ValueAnimator() {
        private var hasStarted = false
        fun startIfNecessary(currentValue: Float) {
            if (!hasStarted && predicate.shouldStart(currentValue)) {
                start()
                hasStarted = true
            }
        }
    }

    /**
     * An Animator that shows and then shrinks a retreating join between the previous and newly
     * selected pages.  This also sets up some pending dot reveals – to be started when the retreat
     * has passed the dot to be revealed.
     */
    inner class PendingRetreatAnimator(
        was: Int,
        now: Int,
        steps: Int,
        predicate: StartPredicate,
    ) : PendingStartAnimator(predicate) {
        init {
            duration = mAnimHalfDuration
            interpolator = mInterpolator

            // work out the start/end values of the retreating join from the direction we're
            // travelling in.  Also look at the current selected dot position, i.e. we're moving on
            // before a prior anim has finished.
            val initialX1 = if (now > was) {
                min(mDotCenterX[was], mSelectedDotX) - mDotRadius
            } else {
                mDotCenterX[now] - mDotRadius
            }
            val finalX1 = if (now > was) {
                mDotCenterX[now] - mDotRadius
            } else {
                mDotCenterX[now] - mDotRadius
            }
            val initialX2 = if (now > was) {
                mDotCenterX[now] + mDotRadius
            } else {
                max(mDotCenterX[was], mSelectedDotX) + mDotRadius
            }
            val finalX2 = if (now > was) {
                mDotCenterX[now] + mDotRadius
            } else {
                mDotCenterX[now] + mDotRadius
            }

            // hold on to the indexes of the dots that will be hidden by the retreat so that
            // we can initialize their revealFraction's i.e. make sure they're hidden while the
            // reveal animation runs
            val dotsToHide = IntArray(steps)
            if (initialX1 != finalX1) { // rightward retreat
                setFloatValues(initialX1, finalX1)
                // create the reveal animations that will run when the retreat passes them
                mRevealAnimations = Array(steps) { i ->
                    dotsToHide[i] = was + i
                    PendingRevealAnimator(
                        was + i,
                        RightwardStartPredicate(mDotCenterX[was + i])
                    )
                }
                addUpdateListener { valueAnimator: ValueAnimator ->
                    // todo avoid autoboxing
                    mRetreatingJoinX1 = valueAnimator.animatedValue as Float
                    postInvalidateOnAnimation()
                    // start any reveal animations if we've passed them
                    for (pendingReveal in mRevealAnimations) {
                        pendingReveal.startIfNecessary(mRetreatingJoinX1)
                    }
                }
            } else { // (initialX2 != finalX2) leftward retreat
                setFloatValues(initialX2, finalX2)
                // create the reveal animations that will run when the retreat passes them
                mRevealAnimations = Array(steps) { i ->
                    dotsToHide[i] = was - i
                    PendingRevealAnimator(
                        was - i,
                        LeftwardStartPredicate(mDotCenterX[was - i])
                    )
                }
                addUpdateListener { valueAnimator: ValueAnimator ->
                    // todo avoid autoboxing
                    mRetreatingJoinX2 = valueAnimator.animatedValue as Float
                    postInvalidateOnAnimation()
                    // start any reveal animations if we've passed them
                    for (pendingReveal in mRevealAnimations) {
                        pendingReveal.startIfNecessary(mRetreatingJoinX2)
                    }
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    cancelJoiningAnimations()
                    clearJoiningFractions()
                    // we need to set this so that the dots are hidden until the reveal anim runs
                    for (dot in dotsToHide) {
                        setDotRevealFraction(dot, MINIMAL_REVEAL)
                    }
                    mRetreatingJoinX1 = initialX1
                    mRetreatingJoinX2 = initialX2
                    postInvalidateOnAnimation()
                }

                override fun onAnimationEnd(animation: Animator) {
                    mRetreatingJoinX1 = INVALID_FRACTION
                    mRetreatingJoinX2 = INVALID_FRACTION
                    postInvalidateOnAnimation()
                }
            })
        }
    }

    /**
     * An Animator that animates a given dot's revealFraction i.e. scales it up
     */
    inner class PendingRevealAnimator(
        dot: Int,
        predicate: StartPredicate,
    ) : PendingStartAnimator(predicate) {
        private val mDot: Int

        init {
            setFloatValues(MINIMAL_REVEAL, 1f)
            mDot = dot
            duration = mAnimHalfDuration
            interpolator = mInterpolator
            addUpdateListener { valueAnimator: ValueAnimator ->
                // todo avoid autoboxing
                setDotRevealFraction(
                    mDot,
                    valueAnimator.animatedValue as Float
                )
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    setDotRevealFraction(mDot, 0f)
                    postInvalidateOnAnimation()
                }
            })
        }
    }

    /**
     * A predicate used to start an animation when a test passes
     */
    abstract inner class StartPredicate(
        protected var thresholdValue: Float,
    ) {
        abstract fun shouldStart(currentValue: Float): Boolean
    }

    /**
     * A predicate used to start an animation when a given value is greater than a threshold
     */
    inner class RightwardStartPredicate(
        thresholdValue: Float,
    ) : StartPredicate(thresholdValue) {
        override fun shouldStart(currentValue: Float): Boolean {
            return currentValue > thresholdValue
        }
    }

    /**
     * A predicate used to start an animation then a given value is less than a threshold
     */
    inner class LeftwardStartPredicate(
        thresholdValue: Float,
    ) : StartPredicate(thresholdValue) {
        override fun shouldStart(currentValue: Float): Boolean {
            return currentValue < thresholdValue
        }
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        mCurrentPage = savedState.currentPage
        requestLayout()
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.currentPage = mCurrentPage
        return savedState
    }

    internal class SavedState : BaseSavedState {
        var currentPage = 0

        constructor(superState: Parcelable?) : super(superState)
        private constructor(parcel: Parcel) : super(parcel) {
            currentPage = parcel.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(currentPage)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        // defaults
        private const val DEFAULT_DOT_SIZE = 8 // dp
        private const val DEFAULT_GAP = 12 // dp
        private const val DEFAULT_ANIM_DURATION = 400 // ms
        private const val DEFAULT_UNSELECTED_COLOUR = -0x7f000001 // 50% white
        private const val DEFAULT_SELECTED_COLOUR = -0x1 // 100% white

        // constants
        private const val INVALID_FRACTION = -1f
        private const val MINIMAL_REVEAL = 0.00001f
        private const val MAX_ALPHA = 0.7f
    }
}
