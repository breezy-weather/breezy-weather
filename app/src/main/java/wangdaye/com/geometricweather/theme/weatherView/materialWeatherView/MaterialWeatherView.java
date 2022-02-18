package wangdaye.com.geometricweather.theme.weatherView.materialWeatherView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.graphics.ColorUtils;

import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.weatherView.WeatherView;

public class MaterialWeatherView extends ViewGroup
        implements WeatherView {

    @Nullable private MaterialPainterView mCurrentView;
    @Nullable private MaterialPainterView mPreviousView;
    @Nullable private Animator mSwitchAnimator;

    @WeatherKindRule private int mWeatherKind;
    private boolean mDaytime;

    private final int mFirstCardMarginTop;
    private int mScrollTransparentTriggerDistance;

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

        mFirstCardMarginTop = (int) (getResources().getDisplayMetrics().heightPixels * 0.66);
        mScrollTransparentTriggerDistance = mFirstCardMarginTop;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mScrollTransparentTriggerDistance = mFirstCardMarginTop - insets.top;
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

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

    private static int getBrighterColor(int color){
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] - 0.25F;
        hsv[2] = hsv[2] + 0.25F;
        return Color.HSVToColor(hsv);
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

        // remove previous painter view.

        if (mPreviousView != null) {
            removeView(mPreviousView);
        }

        // generate new painter view.

        mPreviousView = mCurrentView;
        mCurrentView = new MaterialPainterView(
                getContext(),
                weatherKind,
                daytime,
                mDrawable,
                mPreviousView != null ? mPreviousView.getScrollRate() : 0f,
                mGravitySensorEnabled
        );

        // add new painter view.

        mCurrentView.setAlpha(0f);
        addView(mCurrentView);

        if (mPreviousView == null) {
            mCurrentView.setAlpha(1f);
            return;
        }

        // execute switch animation.

        AnimatorSet set = new AnimatorSet();
        set.setDuration(SWITCH_ANIMATION_DURATION);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // clear previous painter view when animation ended.
                if (mPreviousView != null) {
                    removeView(mPreviousView);
                    mPreviousView = null;
                }
            }
        });
        set.playTogether(
                ObjectAnimator.ofFloat(
                        mCurrentView,
                        "alpha",
                        mCurrentView.getAlpha(),
                        1f
                ),
                ObjectAnimator.ofFloat(
                        mPreviousView,
                        "alpha",
                        mPreviousView.getAlpha(),
                        0f
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
                Math.min(1, 1.0 * scrollY / mScrollTransparentTriggerDistance)
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

    @Override
    public int[] getThemeColors(boolean lightTheme) {
        int color = getBackgroundColor();
        if (!lightTheme) {
            color = getBrighterColor(color);
        }
        return new int[] {
                color,
                color,
                ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))
        };
    }

    public static int[] getThemeColors(
            Context context,
            @WeatherKindRule int weatherKind,
            boolean lightTheme
    ) {
        int color = innerGetBackgroundColor(context, weatherKind, lightTheme);
        if (!lightTheme) {
            color = getBrighterColor(color);
        }
        return new int[] {
                color,
                color,
                ColorUtils.setAlphaComponent(color, (int) (0.5 * 255))
        };
    }

    @Override
    public int getBackgroundColor() {
        return innerGetBackgroundColor(getContext(), mWeatherKind, mDaytime);
    }

    private static int innerGetBackgroundColor(
            Context context,
            @WeatherKindRule int weatherKind,
            boolean daytime
    ) {
        return WeatherImplementorFactory.getWeatherThemeColor(
                context,
                weatherKind,
                daytime
        );
    }

    @Override
    public int getHeaderHeight() {
        return mFirstCardMarginTop;
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

    @Override
    public void setSystemBarStyle(
            Context context,
            Window window,
            boolean statusShader,
            boolean lightStatus,
            boolean navigationShader,
            boolean lightNavigation
    ) {
        DisplayUtils.setSystemBarStyle(
                context,
                window,
                true,
                statusShader,
                lightNavigation,
                navigationShader,
                lightNavigation
        );
    }

    @Override
    public void setSystemBarColor(
            Context context,
            Window window,
            boolean statusShader,
            boolean lightStatus,
            boolean navigationShader,
            boolean lightNavigation
    ) {
        DisplayUtils.setSystemBarColor(
                context,
                window,
                true,
                statusShader,
                lightNavigation,
                navigationShader,
                lightNavigation
        );
    }
}