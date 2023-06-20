package org.breezyweather.theme.weatherView.materialWeatherView;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.annotation.Size;

import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.theme.weatherView.WeatherView;

public class MaterialWeatherView extends ViewGroup
        implements WeatherView {

    @Nullable private MaterialPainterView mCurrentView;
    @Nullable private MaterialPainterView mPreviousView;
    @Nullable private Animator mSwitchAnimator;

    @WeatherKindRule private int mWeatherKind;
    private boolean mDaytime;

    private int mFirstCardMarginTop;

    private boolean mGravitySensorEnabled;
    private boolean mDrawable;

    private static final long SWITCH_ANIMATION_DURATION = 300;

    /**
     * This class is used to implement different kinds of weather animations.
     * */
    public static abstract class WeatherAnimationImplementor {

        public abstract void updateData(
                @Size(2) int[] canvasSizes,
                long interval,
                float rotation2D,
                float rotation3D
        );

        // return true if finish drawing.
        public abstract void draw(
                @Size(2) int[] canvasSizes,
                Canvas canvas,
                float scrollRate,
                float rotation2D,
                float rotation3D
        );
    }

    public static abstract class RotateController {

        public abstract void updateRotation(double rotation, double interval);

        public abstract double getRotation();
    }

    public MaterialWeatherView(Context context) {
        super(context);

        setWeather(WeatherView.WEATHER_KING_NULL, true, null);

        mGravitySensorEnabled = true;
        mDrawable = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // FIXME: It's no longer 0.66, see HeaderViewHolder.onBindView
        mFirstCardMarginTop = (int) (getResources().getDisplayMetrics().heightPixels * 0.66);

        for (int index = 0; index < getChildCount(); index ++) {
            final View child = getChildAt(index);

            child.measure(
                    MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY)
            );
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        for (int index = 0; index < getChildCount(); index ++) {
            final View child = getChildAt(index);

            child.layout(
                    0,
                    0,
                    child.getMeasuredWidth(),
                    child.getMeasuredHeight()
            );
        }
    }

    // interface.

    // weather view.

    @Override
    public void setWeather(
            @WeatherKindRule int weatherKind,
            boolean daytime,
            @Nullable ResourceProvider provider
    ) {
        // do nothing if weather not change.

        if (mWeatherKind == weatherKind && mDaytime == daytime) {
            return;
        }

        // cache weather state.

        mWeatherKind = weatherKind;
        mDaytime = daytime;

        // cancel the previous switch animation if necessary.

        if (mSwitchAnimator != null) {
            mSwitchAnimator.cancel();
            mSwitchAnimator = null;
        }

        // stop current painting work.

        if (mCurrentView != null) {
            mCurrentView.setDrawable(false);
        }

        // generate new painter view or update painter cache.

        MaterialPainterView prev = mPreviousView;
        mPreviousView = mCurrentView;
        mCurrentView = prev;
        if (mCurrentView == null) {
            mCurrentView = new MaterialPainterView(
                    getContext(),
                    weatherKind,
                    daytime,
                    mDrawable,
                    mPreviousView != null ? mPreviousView.getScrollRate() : 0f,
                    mGravitySensorEnabled
            );
            addView(mCurrentView);
        } else {
            mCurrentView.update(weatherKind, daytime, mGravitySensorEnabled);
            mCurrentView.setDrawable(mDrawable);
        }

        // execute switch animation.

        if (mPreviousView == null) {
            mCurrentView.setAlpha(1f);
            return;
        }

        AnimatorSet set = new AnimatorSet();
        set.setDuration(SWITCH_ANIMATION_DURATION);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(
                ObjectAnimator.ofFloat(
                        mCurrentView,
                        "alpha",
                        0f, 1f
                ),
                ObjectAnimator.ofFloat(
                        mPreviousView,
                        "alpha",
                        mPreviousView.getAlpha(), 0f
                )
        );

        mSwitchAnimator = set;
        mSwitchAnimator.start();
    }

    @Override
    public void onClick() {
        // do nothing.
    }

    @Override
    public void onScroll(int scrollY) {
        float scrollRate = (float) (
                Math.min(1, 1.0 * scrollY / (mFirstCardMarginTop))
        );

        if (mCurrentView != null) {
            mCurrentView.setScrollRate(scrollRate);
        }
        if (mPreviousView != null) {
            mPreviousView.setScrollRate(scrollRate);
        }
    }

    @Override
    public int getWeatherKind() {
        return mWeatherKind;
    }

    public void setDrawable(boolean drawable) {
        if (mDrawable == drawable) {
            return;
        }
        mDrawable = drawable;

        if (mCurrentView != null) {
            mCurrentView.setDrawable(drawable);
        }
        if (mPreviousView != null) {
            mPreviousView.setDrawable(drawable);
        }
    }

    @Override
    public void setGravitySensorEnabled(boolean enabled) {
        mGravitySensorEnabled = enabled;
    }
}