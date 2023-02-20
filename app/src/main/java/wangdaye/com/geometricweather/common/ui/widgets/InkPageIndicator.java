package wangdaye.com.geometricweather.common.ui.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.annotation.ColorInt;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.util.Arrays;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

/**
 * Ink page indicator.
 * */

public class InkPageIndicator extends View
        implements SwipeSwitchLayout.OnPagerSwipeListener, View.OnAttachStateChangeListener {

    // defaults
    private static final int DEFAULT_DOT_SIZE = 8;                      // dp
    private static final int DEFAULT_GAP = 12;                          // dp
    private static final int DEFAULT_ANIM_DURATION = 400;               // ms
    private static final int DEFAULT_UNSELECTED_COLOUR = 0x80ffffff;    // 50% white
    private static final int DEFAULT_SELECTED_COLOUR = 0xffffffff;      // 100% white

    // constants
    private static final float INVALID_FRACTION = -1f;
    private static final float MINIMAL_REVEAL = 0.00001f;

    // configurable attributes
    private int mDotDiameter;
    private int mGap;
    private long mAnimDuration;
    private int mUnselectedColour;
    private int mSelectedColour;

    // derived from attributes
    private float mDotRadius;
    private float mHalfDotRadius;
    private long mAnimHalfDuration;
    private float mDotTopY;
    private float mDotCenterY;
    private float mDotBottomY;

    // ViewPager
    private SwipeSwitchLayout mSwitchView;

    // state
    private int mPageCount;
    private int mCurrentPage;
    private int mPreviousPage;
    private float mSelectedDotX;
    private boolean mSelectedDotInPosition;
    private float[] mDotCenterX;
    private float[] mJoiningFractions;
    private float mRetreatingJoinX1;
    private float mRetreatingJoinX2;
    private float[] mDotRevealFractions;
    private boolean mIsAttachedToWindow;
    private boolean mPageChanging;
    private boolean mShowing;

    // drawing
    private final Paint mUnselectedPaint;
    private final Paint mSelectedPaint;
    private final Paint mTextPaint;
    private Path mCombinedUnselectedPath;
    private final Path mUnselectedDotPath;
    private final Path mUnselectedDotLeftPath;
    private final Path mUnselectedDotRightPath;
    private final RectF mRectF;

    // animation
    private ValueAnimator mMoveAnimation;
    private AnimatorSet mJoiningAnimationSet;
    private PendingRetreatAnimator mRetreatAnimation;
    private PendingRevealAnimator[] mRevealAnimations;
    private final Interpolator mInterpolator;
    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mDismissAnimator;

    // working values for beziers
    float endX1;
    float endY1;
    float endX2;
    float endY2;
    float controlX1;
    float controlY1;
    float controlX2;
    float controlY2;

    private static final float MAX_ALPHA = 0.7F;

    public InkPageIndicator(Context context) {
        this(context, null, 0);
    }

    public InkPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InkPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final int density = (int) context.getResources().getDisplayMetrics().density;

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.InkPageIndicator, defStyle, 0);

        mDotDiameter = a.getDimensionPixelSize(R.styleable.InkPageIndicator_dotDiameter,
                DEFAULT_DOT_SIZE * density);
        mDotRadius = mDotDiameter / 2f;
        mHalfDotRadius = mDotRadius / 2f;
        mGap = a.getDimensionPixelSize(R.styleable.InkPageIndicator_dotGap,
                DEFAULT_GAP * density);
        mAnimDuration = (long) a.getInteger(R.styleable.InkPageIndicator_animationDuration,
                DEFAULT_ANIM_DURATION);
        mAnimHalfDuration = mAnimDuration / 2;
        mUnselectedColour = a.getColor(R.styleable.InkPageIndicator_pageIndicatorColor,
                DEFAULT_UNSELECTED_COLOUR);
        mSelectedColour = a.getColor(R.styleable.InkPageIndicator_currentPageIndicatorColor,
                DEFAULT_SELECTED_COLOUR);

        a.recycle();

        mUnselectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnselectedPaint.setColor(mUnselectedColour);
        mSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectedPaint.setColor(mSelectedColour);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mSelectedColour);
        mTextPaint.setTypeface(
                DisplayUtils.getTypefaceFromTextAppearance(getContext(), R.style.subtitle_text)
        );
        mInterpolator = new FastOutSlowInInterpolator();

        // create paths & rect now – reuse & rewind later
        mCombinedUnselectedPath = new Path();
        mUnselectedDotPath = new Path();
        mUnselectedDotLeftPath = new Path();
        mUnselectedDotRightPath = new Path();
        mRectF = new RectF();

        addOnAttachStateChangeListener(this);

        mShowing = false;
        setAlpha(0);

        mShowAnimator = ObjectAnimator.ofFloat(
                this, "alpha", 0, MAX_ALPHA
        ).setDuration(100);

        mDismissAnimator = ObjectAnimator.ofFloat(
                this, "alpha", MAX_ALPHA, 0
        ).setDuration(200);
        mDismissAnimator.setStartDelay(600);
    }

    public void setSwitchView(SwipeSwitchLayout switchView) {
        mSwitchView = switchView;
        switchView.setOnPageSwipeListener(this);
        setPageCount(switchView.getTotalCount());
        setCurrentPageImmediate();
    }

    public void setDisplayState(boolean show) {
        if (mShowing == show) {
            return;
        }

        mShowing = show;

        mDismissAnimator.cancel();
        if (show) {
            mShowAnimator.cancel();
            if (getAlpha() != MAX_ALPHA) {
                mShowAnimator.setFloatValues(getAlpha(), 0.7f);
                mShowAnimator.start();
            }
        } else {
            mDismissAnimator.start();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mIsAttachedToWindow) {
            if (position < 0 || position > mPageCount - 1) {
                return;
            }

            float fraction = positionOffset;
            int currentPosition = mPageChanging ? mPreviousPage : mCurrentPage;
            int leftDotPosition = position;
            // when swiping from #2 to #1 ViewPager reports position as 1 and a descending offset
            // need to convert this into our left-dot-based 'coordinate space'
            if (currentPosition != position) {
                fraction = 1f - positionOffset;

                // if user scrolls completely to next page then the position param updates to that
                // new page but we're not ready to switch our 'current' page yet so adjust for that
                if (fraction == 1f) {
                    leftDotPosition = Math.min(currentPosition, position);
                }
            }
            setJoiningFraction(leftDotPosition, fraction);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (mIsAttachedToWindow) {
            // this is the main event we're interested in!
            setSelectedPage(position);
        } else {
            // when not attached, don't animate the move, just store immediately
            setCurrentPageImmediate();
        }
    }

    private void setPageCount(int pages) {
        mPageCount = pages;
        resetState();
        requestLayout();
    }

    public void setCurrentIndicatorColor(@ColorInt int color) {
        mSelectedPaint.setColor(color);
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setIndicatorColor(@ColorInt int color) {
        mUnselectedPaint.setColor(color);
        invalidate();
    }

    private void calculateDotPositions(int width, int height) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = width - getPaddingRight();
        int bottom = height - getPaddingBottom();

        int requiredWidth = getRequiredWidth();
        float startLeft = left + ((right - left - requiredWidth) / 2f) + mDotRadius;

        mDotCenterX = new float[mPageCount];
        for (int i = 0; i < mPageCount; i++) {
            mDotCenterX[i] = startLeft + i * (mDotDiameter + mGap);
        }
        // todo just top aligning for now… should make this smarter
        mDotTopY = top;
        mDotCenterY = top + mDotRadius;
        mDotBottomY = top + mDotDiameter;

        setCurrentPageImmediate();
    }

    private void setCurrentPageImmediate() {
        if (mSwitchView != null) {
            mCurrentPage = mSwitchView.getPosition();
        } else {
            mCurrentPage = 0;
        }
        if (mDotCenterX != null && mDotCenterX.length > 0 && (mMoveAnimation == null || !mMoveAnimation.isStarted())) {
            mSelectedDotX = mDotCenterX[mCurrentPage];
        }
    }

    private void resetState() {
        mJoiningFractions = new float[mPageCount - 1];
        Arrays.fill(mJoiningFractions, 0f);
        mDotRevealFractions = new float[mPageCount];
        Arrays.fill(mDotRevealFractions, 0f);
        mRetreatingJoinX1 = INVALID_FRACTION;
        mRetreatingJoinX2 = INVALID_FRACTION;
        mSelectedDotInPosition = true;
        if (getMeasuredHeight() != 0 || getMeasuredWidth() != 0) {
            calculateDotPositions(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        setPadding(0, 0, 0, insets.bottom);
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredHeight = getDesiredHeight();
        int height;
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                height = MeasureSpec.getSize(heightMeasureSpec);
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(desiredHeight, MeasureSpec.getSize(heightMeasureSpec));
                break;
            default: // MeasureSpec.UNSPECIFIED
                height = desiredHeight;
                break;
        }

        int desiredWidth = getDesiredWidth();
        int width;
        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                width = MeasureSpec.getSize(widthMeasureSpec);
                break;
            case MeasureSpec.AT_MOST:
                width = Math.min(desiredWidth, MeasureSpec.getSize(widthMeasureSpec));
                break;
            default: // MeasureSpec.UNSPECIFIED
                width = desiredWidth;
                break;
        }
        setMeasuredDimension(width, height);
        calculateDotPositions(width, height);
    }

    private int getDesiredHeight() {
        return getPaddingTop() + mDotDiameter + getPaddingBottom();
    }

    private int getRequiredWidth() {
        return mPageCount * mDotDiameter + (mPageCount - 1) * mGap;
    }

    private int getDesiredWidth() {
        return getPaddingLeft() + getRequiredWidth() + getPaddingRight();
    }

    @Override
    public void onViewAttachedToWindow(View view) {
        mIsAttachedToWindow = true;
    }

    @Override
    public void onViewDetachedFromWindow(View view) {
        mIsAttachedToWindow = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mSwitchView == null || mPageCount == 0) return;
        if (mPageCount > 7) {
            int cx = getMeasuredWidth() / 2;
            int cy = getMeasuredHeight() / 2;

            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setTextSize(mDotDiameter + mGap / 2);
            Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
            int baseLineY = (int) (cy - fontMetrics.top - fontMetrics.bottom);
            canvas.drawText((mCurrentPage + 1) + "/" + mPageCount, cx, baseLineY, mTextPaint);

            return;
        }

        drawUnselected(canvas);
        drawSelected(canvas);
    }

    private void drawUnselected(Canvas canvas) {

        mCombinedUnselectedPath.rewind();

        // draw any settled, revealing or joining dots
        for (int page = 0; page < mPageCount; page++) {
            int nextXIndex = page == mPageCount - 1 ? page : page + 1;
            Path unselectedPath = getUnselectedPath(page,
                    mDotCenterX[page],
                    mDotCenterX[nextXIndex],
                    page == mPageCount - 1 ? INVALID_FRACTION : mJoiningFractions[page],
                    mDotRevealFractions[page]);
            unselectedPath.addPath(mCombinedUnselectedPath);
            mCombinedUnselectedPath.addPath(unselectedPath);
        }
        // draw any retreating joins
        if (mRetreatingJoinX1 != INVALID_FRACTION) {
            Path retreatingJoinPath = getRetreatingJoinPath();
            mCombinedUnselectedPath.addPath(retreatingJoinPath);
        }

        canvas.drawPath(mCombinedUnselectedPath, mUnselectedPaint);
    }

    /**
     * Unselected dots can be in 6 states:
     * <p>
     * #1 At rest
     * #2 Joining neighbour, still separate
     * #3 Joining neighbour, combined curved
     * #4 Joining neighbour, combined straight
     * #5 Join retreating
     * #6 Dot re-showing / revealing
     * <p>
     * It can also be in a combination of these states e.g. joining one neighbour while
     * retreating from another.  We therefore create a Path so that we can examine each
     * dot pair separately and later take the union for these cases.
     * <p>
     * This function returns a path for the given dot **and any action to it's right** e.g. joining
     * or retreating from it's neighbour
     *
     * @param page
     * @return
     */
    private Path getUnselectedPath(int page,
                                   float centerX,
                                   float nextCenterX,
                                   float joiningFraction,
                                   float dotRevealFraction) {

        mUnselectedDotPath.rewind();

        if ((joiningFraction == 0f || joiningFraction == INVALID_FRACTION)
                && dotRevealFraction == 0f
                && !(page == mCurrentPage && mSelectedDotInPosition == true)) {

            // case #1 – At rest
            mUnselectedDotPath.addCircle(mDotCenterX[page], mDotCenterY, mDotRadius, Path.Direction.CW);
        }

        if (joiningFraction > 0f && joiningFraction <= 0.5f
                && mRetreatingJoinX1 == INVALID_FRACTION) {

            // case #2 – Joining neighbour, still separate

            // start with the left dot
            mUnselectedDotLeftPath.rewind();

            // start at the bottom center
            mUnselectedDotLeftPath.moveTo(centerX, mDotBottomY);

            // semi circle to the top center
            mRectF.set(centerX - mDotRadius, mDotTopY, centerX + mDotRadius, mDotBottomY);
            mUnselectedDotLeftPath.arcTo(mRectF, 90, 180, true);

            // cubic to the right middle
            endX1 = centerX + mDotRadius + (joiningFraction * mGap);
            endY1 = mDotCenterY;
            controlX1 = centerX + mHalfDotRadius;
            controlY1 = mDotTopY;
            controlX2 = endX1;
            controlY2 = endY1 - mHalfDotRadius;
            mUnselectedDotLeftPath.cubicTo(controlX1, controlY1,
                    controlX2, controlY2,
                    endX1, endY1);

            // cubic back to the bottom center
            endX2 = centerX;
            endY2 = mDotBottomY;
            controlX1 = endX1;
            controlY1 = endY1 + mHalfDotRadius;
            controlX2 = centerX + mHalfDotRadius;
            controlY2 = mDotBottomY;
            mUnselectedDotLeftPath.cubicTo(controlX1, controlY1,
                    controlX2, controlY2,
                    endX2, endY2);

            mUnselectedDotPath.addPath(mUnselectedDotLeftPath);

            // now do the next dot to the right
            mUnselectedDotRightPath.rewind();

            // start at the bottom center
            mUnselectedDotRightPath.moveTo(nextCenterX, mDotBottomY);

            // semi circle to the top center
            mRectF.set(nextCenterX - mDotRadius, mDotTopY, nextCenterX + mDotRadius, mDotBottomY);
            mUnselectedDotRightPath.arcTo(mRectF, 90, -180, true);

            // cubic to the left middle
            endX1 = nextCenterX - mDotRadius - (joiningFraction * mGap);
            endY1 = mDotCenterY;
            controlX1 = nextCenterX - mHalfDotRadius;
            controlY1 = mDotTopY;
            controlX2 = endX1;
            controlY2 = endY1 - mHalfDotRadius;
            mUnselectedDotRightPath.cubicTo(controlX1, controlY1,
                    controlX2, controlY2,
                    endX1, endY1);

            // cubic back to the bottom center
            endX2 = nextCenterX;
            endY2 = mDotBottomY;
            controlX1 = endX1;
            controlY1 = endY1 + mHalfDotRadius;
            controlX2 = endX2 - mHalfDotRadius;
            controlY2 = mDotBottomY;
            mUnselectedDotRightPath.cubicTo(controlX1, controlY1,
                    controlX2, controlY2,
                    endX2, endY2);
            mUnselectedDotPath.addPath(mUnselectedDotRightPath);
        }

        if (joiningFraction > 0.5f && joiningFraction < 1f
                && mRetreatingJoinX1 == INVALID_FRACTION) {

            // case #3 – Joining neighbour, combined curved

            // adjust the fraction so that it goes from 0.3 -> 1 to produce a more realistic 'join'
            float adjustedFraction = (joiningFraction - 0.2f) * 1.25f;

            // start in the bottom left
            mUnselectedDotPath.moveTo(centerX, mDotBottomY);

            // semi-circle to the top left
            mRectF.set(centerX - mDotRadius, mDotTopY, centerX + mDotRadius, mDotBottomY);
            mUnselectedDotPath.arcTo(mRectF, 90, 180, true);

            // bezier to the middle top of the join
            endX1 = centerX + mDotRadius + (mGap / 2);
            endY1 = mDotCenterY - (adjustedFraction * mDotRadius);
            controlX1 = endX1 - (adjustedFraction * mDotRadius);
            controlY1 = mDotTopY;
            controlX2 = endX1 - ((1 - adjustedFraction) * mDotRadius);
            controlY2 = endY1;
            mUnselectedDotPath.cubicTo(controlX1, controlY1,
                    controlX2, controlY2,
                    endX1, endY1);

            // bezier to the top right of the join
            endX2 = nextCenterX;
            endY2 = mDotTopY;
            controlX1 = endX1 + ((1 - adjustedFraction) * mDotRadius);
            controlY1 = endY1;
            controlX2 = endX1 + (adjustedFraction * mDotRadius);
            controlY2 = mDotTopY;
            mUnselectedDotPath.cubicTo(controlX1, controlY1,
                    controlX2, controlY2,
                    endX2, endY2);

            // semi-circle to the bottom right
            mRectF.set(nextCenterX - mDotRadius, mDotTopY, nextCenterX + mDotRadius, mDotBottomY);
            mUnselectedDotPath.arcTo(mRectF, 270, 180, true);

            // bezier to the middle bottom of the join
            // endX1 stays the same
            endY1 = mDotCenterY + (adjustedFraction * mDotRadius);
            controlX1 = endX1 + (adjustedFraction * mDotRadius);
            controlY1 = mDotBottomY;
            controlX2 = endX1 + ((1 - adjustedFraction) * mDotRadius);
            controlY2 = endY1;
            mUnselectedDotPath.cubicTo(controlX1, controlY1,
                    controlX2, controlY2,
                    endX1, endY1);

            // bezier back to the start point in the bottom left
            endX2 = centerX;
            endY2 = mDotBottomY;
            controlX1 = endX1 - ((1 - adjustedFraction) * mDotRadius);
            controlY1 = endY1;
            controlX2 = endX1 - (adjustedFraction * mDotRadius);
            controlY2 = endY2;
            mUnselectedDotPath.cubicTo(controlX1, controlY1,
                    controlX2, controlY2,
                    endX2, endY2);
        }
        if (joiningFraction == 1 && mRetreatingJoinX1 == INVALID_FRACTION) {

            // case #4 Joining neighbour, combined straight technically we could use case 3 for this
            // situation as well but assume that this is an optimization rather than faffing around
            // with beziers just to draw a rounded rect
            mRectF.set(centerX - mDotRadius, mDotTopY, nextCenterX + mDotRadius, mDotBottomY);
            mUnselectedDotPath.addRoundRect(mRectF, mDotRadius, mDotRadius, Path.Direction.CW);
        }

        // case #5 is handled by #getRetreatingJoinPath()
        // this is done separately so that we can have a single retreating path spanning
        // multiple dots and therefore animate it's movement smoothly

        if (dotRevealFraction > MINIMAL_REVEAL) {

            // case #6 – previously hidden dot revealing
            mUnselectedDotPath.addCircle(centerX, mDotCenterY, dotRevealFraction * mDotRadius,
                    Path.Direction.CW);
        }

        return mUnselectedDotPath;
    }

    private Path getRetreatingJoinPath() {
        mUnselectedDotPath.rewind();
        mRectF.set(mRetreatingJoinX1, mDotTopY, mRetreatingJoinX2, mDotBottomY);
        mUnselectedDotPath.addRoundRect(mRectF, mDotRadius, mDotRadius, Path.Direction.CW);
        return mUnselectedDotPath;
    }

    private void drawSelected(Canvas canvas) {
        canvas.drawCircle(mSelectedDotX, mDotCenterY, mDotRadius, mSelectedPaint);
    }

    private void setSelectedPage(int now) {
        if (now == mCurrentPage) return;

        mPageChanging = true;
        mPreviousPage = mCurrentPage;
        mCurrentPage = now;
        final int steps = Math.abs(now - mPreviousPage);

        if (steps > 1) {
            if (now > mPreviousPage) {
                for (int i = 0; i < steps; i++) {
                    setJoiningFraction(mPreviousPage + i, 1f);
                }
            } else {
                for (int i = -1; i > -steps; i--) {
                    setJoiningFraction(mPreviousPage + i, 1f);
                }
            }
        }

        // create the anim to move the selected dot – this animator will kick off
        // retreat animations when it has moved 75% of the way.
        // The retreat animation in turn will kick of reveal anims when the
        // retreat has passed any dots to be revealed
        mMoveAnimation = createMoveSelectedAnimator(mDotCenterX[now], mPreviousPage, now, steps);
        mMoveAnimation.start();
    }

    private ValueAnimator createMoveSelectedAnimator(
            final float moveTo, int was, int now, int steps) {

        // create the actual move animator
        ValueAnimator moveSelected = ValueAnimator.ofFloat(mSelectedDotX, moveTo);

        // also set up a pending retreat anim – this starts when the move is 75% complete
        mRetreatAnimation = new PendingRetreatAnimator(was, now, steps,
                now > was ?
                        new RightwardStartPredicate(moveTo - ((moveTo - mSelectedDotX) * 0.25f)) :
                        new LeftwardStartPredicate(moveTo + ((mSelectedDotX - moveTo) * 0.25f)));
        mRetreatAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetState();
                mPageChanging = false;
            }
        });
        moveSelected.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // todo avoid autoboxing
                mSelectedDotX = (Float) valueAnimator.getAnimatedValue();
                mRetreatAnimation.startIfNecessary(mSelectedDotX);
                ViewCompat.postInvalidateOnAnimation(InkPageIndicator.this);
            }
        });
        moveSelected.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                // set a flag so that we continue to draw the unselected dot in the target position
                // until the selected dot has finished moving into place
                mSelectedDotInPosition = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // set a flag when anim finishes so that we don't draw both selected & unselected
                // page dots
                mSelectedDotInPosition = true;
            }
        });
        // slightly delay the start to give the joins a chance to run
        // unless dot isn't in position yet – then don't delay!
        moveSelected.setStartDelay(mSelectedDotInPosition ? mAnimDuration / 4l : 0l);
        moveSelected.setDuration(mAnimDuration * 3l / 4l);
        moveSelected.setInterpolator(mInterpolator);
        return moveSelected;
    }

    private void setJoiningFraction(int leftDot, float fraction) {
        if (leftDot < mJoiningFractions.length) {

            if (leftDot == 1) {
                //Log.d("PageIndicator", "dot 1 fraction:\t" + fraction);
            }

            mJoiningFractions[leftDot] = fraction;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void clearJoiningFractions() {
        Arrays.fill(mJoiningFractions, 0f);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void setDotRevealFraction(int dot, float fraction) {
        if(dot < mDotRevealFractions.length) {
            mDotRevealFractions[dot] = fraction;
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void cancelJoiningAnimations() {
        if (mJoiningAnimationSet != null && mJoiningAnimationSet.isRunning()) {
            mJoiningAnimationSet.cancel();
        }
    }

    /**
     * A {@link ValueAnimator} that starts once a given predicate returns true.
     */
    public abstract class PendingStartAnimator extends ValueAnimator {

        protected boolean hasStarted;
        protected StartPredicate predicate;

        public PendingStartAnimator(StartPredicate predicate) {
            super();
            this.predicate = predicate;
            hasStarted = false;
        }

        public void startIfNecessary(float currentValue) {
            if (!hasStarted && predicate.shouldStart(currentValue)) {
                start();
                hasStarted = true;
            }
        }
    }

    /**
     * An Animator that shows and then shrinks a retreating join between the previous and newly
     * selected pages.  This also sets up some pending dot reveals – to be started when the retreat
     * has passed the dot to be revealed.
     */
    public class PendingRetreatAnimator extends PendingStartAnimator {

        public PendingRetreatAnimator(int was, int now, int steps, StartPredicate predicate) {
            super(predicate);
            setDuration(mAnimHalfDuration);
            setInterpolator(mInterpolator);

            // work out the start/end values of the retreating join from the direction we're
            // travelling in.  Also look at the current selected dot position, i.e. we're moving on
            // before a prior anim has finished.
            final float initialX1 = now > was ? Math.min(mDotCenterX[was], mSelectedDotX) - mDotRadius
                    : mDotCenterX[now] - mDotRadius;
            final float finalX1 = now > was ? mDotCenterX[now] - mDotRadius
                    : mDotCenterX[now] - mDotRadius;
            final float initialX2 = now > was ? mDotCenterX[now] + mDotRadius
                    : Math.max(mDotCenterX[was], mSelectedDotX) + mDotRadius;
            final float finalX2 = now > was ? mDotCenterX[now] + mDotRadius
                    : mDotCenterX[now] + mDotRadius;

            mRevealAnimations = new PendingRevealAnimator[steps];
            // hold on to the indexes of the dots that will be hidden by the retreat so that
            // we can initialize their revealFraction's i.e. make sure they're hidden while the
            // reveal animation runs
            final int[] dotsToHide = new int[steps];
            if (initialX1 != finalX1) { // rightward retreat
                setFloatValues(initialX1, finalX1);
                // create the reveal animations that will run when the retreat passes them
                for (int i = 0; i < steps; i++) {
                    mRevealAnimations[i] = new PendingRevealAnimator(was + i,
                            new RightwardStartPredicate(mDotCenterX[was + i]));
                    dotsToHide[i] = was + i;
                }
                addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        // todo avoid autoboxing
                        mRetreatingJoinX1 = (Float) valueAnimator.getAnimatedValue();
                        ViewCompat.postInvalidateOnAnimation(InkPageIndicator.this);
                        // start any reveal animations if we've passed them
                        for (PendingRevealAnimator pendingReveal : mRevealAnimations) {
                            pendingReveal.startIfNecessary(mRetreatingJoinX1);
                        }
                    }
                });
            } else { // (initialX2 != finalX2) leftward retreat
                setFloatValues(initialX2, finalX2);
                // create the reveal animations that will run when the retreat passes them
                for (int i = 0; i < steps; i++) {
                    mRevealAnimations[i] = new PendingRevealAnimator(was - i,
                            new LeftwardStartPredicate(mDotCenterX[was - i]));
                    dotsToHide[i] = was - i;
                }
                addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        // todo avoid autoboxing
                        mRetreatingJoinX2 = (Float) valueAnimator.getAnimatedValue();
                        ViewCompat.postInvalidateOnAnimation(InkPageIndicator.this);
                        // start any reveal animations if we've passed them
                        for (PendingRevealAnimator pendingReveal : mRevealAnimations) {
                            pendingReveal.startIfNecessary(mRetreatingJoinX2);
                        }
                    }
                });
            }

            addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    cancelJoiningAnimations();
                    clearJoiningFractions();
                    // we need to set this so that the dots are hidden until the reveal anim runs
                    for (int dot : dotsToHide) {
                        setDotRevealFraction(dot, MINIMAL_REVEAL);
                    }
                    mRetreatingJoinX1 = initialX1;
                    mRetreatingJoinX2 = initialX2;
                    ViewCompat.postInvalidateOnAnimation(InkPageIndicator.this);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mRetreatingJoinX1 = INVALID_FRACTION;
                    mRetreatingJoinX2 = INVALID_FRACTION;
                    ViewCompat.postInvalidateOnAnimation(InkPageIndicator.this);
                }
            });
        }
    }

    /**
     * An Animator that animates a given dot's revealFraction i.e. scales it up
     */
    public class PendingRevealAnimator extends PendingStartAnimator {

        private int mDot;

        public PendingRevealAnimator(int dot, StartPredicate predicate) {
            super(predicate);
            setFloatValues(MINIMAL_REVEAL, 1f);
            mDot = dot;
            setDuration(mAnimHalfDuration);
            setInterpolator(mInterpolator);
            addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    // todo avoid autoboxing
                    setDotRevealFraction(mDot,
                            (Float) valueAnimator.getAnimatedValue());
                }
            });
            addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setDotRevealFraction(mDot, 0f);
                    ViewCompat.postInvalidateOnAnimation(InkPageIndicator.this);
                }
            });
        }
    }

    /**
     * A predicate used to start an animation when a test passes
     */
    public abstract class StartPredicate {

        protected float thresholdValue;

        public StartPredicate(float thresholdValue) {
            this.thresholdValue = thresholdValue;
        }

        abstract boolean shouldStart(float currentValue);

    }

    /**
     * A predicate used to start an animation when a given value is greater than a threshold
     */
    public class RightwardStartPredicate extends StartPredicate {

        public RightwardStartPredicate(float thresholdValue) {
            super(thresholdValue);
        }

        boolean shouldStart(float currentValue) {
            return currentValue > thresholdValue;
        }
    }

    /**
     * A predicate used to start an animation then a given value is less than a threshold
     */
    public class LeftwardStartPredicate extends StartPredicate {

        public LeftwardStartPredicate(float thresholdValue) {
            super(thresholdValue);
        }

        boolean shouldStart(float currentValue) {
            return currentValue < thresholdValue;
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
