package wangdaye.com.geometricweather.common.ui.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class DrawerLayout extends ViewGroup {

    private @Nullable View mDrawer;
    private @Nullable View mContent;

    private boolean mUnfold;
    private @FloatRange(from = 0, to = 1) float mProgress; // 0 - fold, 1 - unfold.

    private @Nullable ValueAnimator mProgressAnimator;

    private static final int MIN_DRAWER_WIDTH_DP = 280;
    private static final int MAX_DRAWER_WIDTH_DP = 320;

    public DrawerLayout(Context context) {
        this(context, null);
    }

    public DrawerLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DrawerLayout, defStyleAttr, 0);
        mUnfold = a.getBoolean(R.styleable.DrawerLayout_unfold, false);
        mProgress = mUnfold ? 1 : 0;
        a.recycle();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() > 0) {
            mDrawer = getChildAt(0);
        }
        if (getChildCount() > 1) {
            mContent = getChildAt(1);
        }

        LayoutParams lp;
        if (mDrawer != null) {
            lp = mDrawer.getLayoutParams();
            int width = lp.width;
            if (width == LayoutParams.WRAP_CONTENT) {
                width = getMeasuredWidth()
                        - DisplayUtils.getTabletListAdaptiveWidth(getContext(), getMeasuredWidth());
                if (width == 0) {
                    width = LayoutParams.MATCH_PARENT;
                } else {
                    int minDrawerWidth = (int) DisplayUtils.dpToPx(getContext(), MIN_DRAWER_WIDTH_DP);
                    int maxDrawerWidth = (int) DisplayUtils.dpToPx(getContext(), MAX_DRAWER_WIDTH_DP);
                    width = Math.max(width, minDrawerWidth);
                    width = Math.min(width, maxDrawerWidth);
                }
            }
            mDrawer.measure(
                    getChildMeasureSpec(widthMeasureSpec, 0, width),
                    getChildMeasureSpec(heightMeasureSpec, 0, lp.height)
            );

            if (mContent != null) {
                lp = mContent.getLayoutParams();
                if (mDrawer.getMeasuredWidth() == getMeasuredWidth()) {
                    mContent.measure(
                            getChildMeasureSpec(widthMeasureSpec, 0, lp.width),
                            getChildMeasureSpec(heightMeasureSpec, 0, lp.height)
                    );
                } else {
                    final int widthUsed = (int) (mDrawer.getMeasuredWidth() * mProgress);
                    mContent.measure(
                            getChildMeasureSpec(widthMeasureSpec, widthUsed, lp.width),
                            getChildMeasureSpec(heightMeasureSpec, 0, lp.height)
                    );
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() > 0) {
            mDrawer = getChildAt(0);
        }
        if (getChildCount() > 1) {
            mContent = getChildAt(1);
        }

        if (DisplayUtils.isRtl(getContext())) {
            if (mDrawer != null) {
                mDrawer.layout(
                        (int) (getMeasuredWidth() - mDrawer.getMeasuredWidth() * mProgress),
                        0,
                        (int) (getMeasuredWidth() + mDrawer.getMeasuredWidth() * (1 - mProgress)),
                        mDrawer.getMeasuredHeight()
                );
            }
            if (mContent != null) {
                mContent.layout(
                        0,
                        0,
                        mDrawer.getLeft(),
                        mContent.getMeasuredHeight()
                );
            }
        } else {
            if (mDrawer != null) {
                mDrawer.layout(
                        (int) (mDrawer.getMeasuredWidth() * (mProgress - 1)),
                        0,
                        (int) (mDrawer.getMeasuredWidth() * mProgress),
                        mDrawer.getMeasuredHeight()
                );
            }
            if (mContent != null) {
                mContent.layout(
                        mDrawer.getRight(),
                        0,
                        mDrawer.getRight() + mContent.getMeasuredWidth(),
                        mContent.getMeasuredHeight()
                );
            }
        }
    }

    public boolean isUnfold() {
        return mUnfold;
    }

    public void setUnfold(boolean unfold) {
        if (mUnfold == unfold) {
            return;
        }

        mUnfold = unfold;

        if (mProgressAnimator != null) {
            mProgressAnimator.cancel();
            mProgressAnimator = null;
        }

        mProgressAnimator = generateProgressAnimator(mProgress, unfold ? 1 : 0);
        mProgressAnimator.start();
    }

    private ValueAnimator generateProgressAnimator(float from, float to) {
        ValueAnimator a = ValueAnimator.ofFloat(from, to);
        a.addUpdateListener(animation -> setProgress((Float) animation.getAnimatedValue()));
        a.setDuration((long) (Math.abs(from - to) * 450));
        a.setInterpolator(new DecelerateInterpolator(2f));
        return a;
    }

    private void setProgress(float progress) {
        mProgress = progress;
        requestLayout();
    }
}
