package wangdaye.com.geometricweather.ui.widgets.weatherView.circularSkyView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Weather icon control view.
 * */

public class WeatherIconControlView extends FrameLayout {

    private OnWeatherIconChangingListener mIconListener;
    private AnimListener mAnimListener;

    private boolean mRose = false;

    private float mCurrentAngle;
    private int mIconX;
    private int mIconY;
    private int mIconSize;
    private int mRadius;

    private class AnimRise extends Animation {

        //     90
        // 180      0
        //------------
        private final float mAngleFrom;
        private final float mAngleTo;

        AnimRise() {
            mAngleFrom = 180;
            mAngleTo = 90;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            ensureIconOffset(mAngleFrom, mAngleTo, interpolatedTime);

            getChildAt(2).setTranslationX(mIconX + mIconSize);
            getChildAt(2).setTranslationY(mIconY + mIconSize);
        }
    }

    private class AnimFall extends Animation {

        //     90
        // 180      0
        //------------
        private final float mAngleFrom;
        private final float mAngleTo;

        AnimFall() {
            mAngleFrom = mCurrentAngle;
            mAngleTo = 0;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            ensureIconOffset(mAngleFrom, mAngleTo, interpolatedTime);

            getChildAt(2).setTranslationX(mIconX + mIconSize);
            getChildAt(2).setTranslationY(mIconY + mIconSize);
        }
    }

    private class AnimListener implements Animation.AnimationListener {

        private boolean mCanceled;
        private final int mType;

        static final int END_TYPE = 0;
        static final int CONTINUE_TYPE = 1;

        AnimListener(int type) {
            mCanceled = false;
            mType = type;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            // do nothing.
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!mCanceled) {
                switch (mType) {
                    case END_TYPE:
                        break;

                    case CONTINUE_TYPE:
                        animRise();
                        break;
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // do nothing.
        }
    }

    public WeatherIconControlView(Context context) {
        super(context);
    }

    public WeatherIconControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WeatherIconControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // draw.

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = DisplayUtils.getTabletListAdaptiveWidth(getContext(), getMeasuredWidth());

        if (DisplayUtils.isTabletDevice(getContext())) {
            mIconSize = (int) (width / Constants.UNIT_RADIUS_RATIO * 1.3);
        } else {
            mIconSize = (int) (width / Constants.UNIT_RADIUS_RATIO * 1.8);
        }

        mCurrentAngle = 0;
        mIconX = -mIconSize;
        mIconY = -mIconSize;
        mRadius = (int) (width / Constants.UNIT_RADIUS_RATIO * 4);

        // animatable icon.
        getChildAt(2).measure(
                MeasureSpec.makeMeasureSpec(mIconSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mIconSize, MeasureSpec.EXACTLY)
        );
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View child;

        child = getChildAt(0);
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

        child = getChildAt(1);
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

        final int adaptiveWidth = DisplayUtils.getTabletListAdaptiveWidth(getContext(), getMeasuredWidth());
        child = getChildAt(2);
        child.layout(
                (getMeasuredWidth() - adaptiveWidth) / 2 - mIconSize,
                -mIconSize,
                (getMeasuredWidth() - adaptiveWidth) / 2,
                0
        );
    }

    // control.

    public void showWeatherIcon() {
        if (mRose) {
            animFall();
        } else {
            mRose = true;
            animRise();
        }
    }

    private void ensureIconOffset(float angleFrom, float angleTo, float time) {
        mCurrentAngle = angleFrom + (angleTo - angleFrom) * time;
        double radians = Math.toRadians(mCurrentAngle);
        mIconX = (int) (getMeasuredWidth() / 2 + mRadius * Math.cos(radians) - mIconSize / 2);
        mIconY = (int) (getMeasuredHeight() - mRadius * Math.sin(radians) - mIconSize / 2);
    }

    private void animRise() {
        if (mIconListener != null) {
            mIconListener.OnWeatherIconChanging();
        }

        if (mAnimListener != null) {
            mAnimListener.mCanceled = true;
        }
        mAnimListener =  new AnimListener(AnimListener.END_TYPE);

        AnimRise animation = new AnimRise();
        animation.setDuration(800);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(mAnimListener);

        clearAnimation();
        startAnimation(animation);
    }

    private void animFall() {
        if (mAnimListener != null) {
            mAnimListener.mCanceled = true;
        }
        mAnimListener =  new AnimListener(AnimListener.CONTINUE_TYPE);

        AnimFall animation = new AnimFall();
        animation.setDuration(400);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(mAnimListener);

        clearAnimation();
        startAnimation(animation);
    }

    // interface.

    interface OnWeatherIconChangingListener {
        void OnWeatherIconChanging();
    }

    public void setOnWeatherIconChangingListener(OnWeatherIconChangingListener l) {
        mIconListener = l;
    }
}
