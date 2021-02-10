package wangdaye.com.geometricweather.ui.widget.weatherView.circularSkyView;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageView;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.databinding.ContainerCircularSkyViewBinding;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpter.ImageHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Circular sky weather view.
 * */

public class CircularSkyWeatherView extends FrameLayout
        implements WeatherView, WeatherIconControlView.OnWeatherIconChangingListener {

    @WeatherKindRule private int mWeatherKind = WEATHER_KING_NULL;
    @Nullable private String mIconProvider;
    @ColorInt private int mBackgroundColor;
    private boolean mDaytime;

    private ContainerCircularSkyViewBinding mBinding;

    @Size(3) private Drawable[] mIconDrawables;
    @Size(3) private Animator[] mIconAnimators;
    @Size(2) private Animator[] mStarShineAnimators;

    private int mInsetTop;
    private int mFirstCardMarginTop;

    private class StarAlphaAnimation extends Animation {

        private final float mStartAlpha;
        private final float mEndAlpha;

        StarAlphaAnimation(float startAlpha, float endAlpha) {
            this.mStartAlpha = startAlpha;
            this.mEndAlpha = endAlpha;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mBinding.starContainer.setAlpha(mStartAlpha + (mEndAlpha - mStartAlpha) * interpolatedTime);
        }
    }

    private final AnimatorListenerAdapter[] mStarShineAnimatorListeners = new AnimatorListenerAdapter[] {

            new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mStarShineAnimators[0].start();
                }
            },

            new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mStarShineAnimators[1].start();
                }
            }
    };

    public CircularSkyWeatherView(Context context) {
        super(context);
        initialize();
    }

    public CircularSkyWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CircularSkyWeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @SuppressLint("InflateParams")
    private void initialize() {
        mDaytime = TimeManager.getInstance(getContext()).isDayTime();

        mBackgroundColor = getBackgroundColor();
        setBackgroundColor(mBackgroundColor);

        mBinding = ContainerCircularSkyViewBinding.inflate(LayoutInflater.from(getContext()));
        /*
        controlView = (WeatherIconControlView) LayoutInflater.from(getContext()).inflate(
                R.layout.container_circular_sky_view, this, false);*/
        mBinding.controller.setOnWeatherIconChangingListener(this);
        addView(mBinding.getRoot());

        if (mDaytime) {
            mBinding.starContainer.setAlpha(0);
        } else {
            mBinding.starContainer.setAlpha(1);
        }

        mIconDrawables = new Drawable[] {null, null, null};
        mIconAnimators = new Animator[] {null, null, null};

        AppCompatImageView[] starts = new AppCompatImageView[] {
                findViewById(R.id.star_1),
                findViewById(R.id.star_2)};
        ImageHelper.load(getContext(), starts[0], R.drawable.star_1);
        ImageHelper.load(getContext(), starts[1], R.drawable.star_2);

        mStarShineAnimators = new AnimatorSet[] {
                (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.start_shine_1),
                (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.start_shine_2)};
        for (int i = 0; i < mStarShineAnimators.length; i ++) {
            mStarShineAnimators[i].addListener(mStarShineAnimatorListeners[i]);
            mStarShineAnimators[i].setTarget(starts[i]);
            mStarShineAnimators[i].start();
        }

        mInsetTop = 0;
        mFirstCardMarginTop = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec)
        );

        measureChildren(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(
                        (int) (DisplayUtils.getTabletListAdaptiveWidth(getContext(), getMeasuredWidth())
                                / Constants.UNIT_RADIUS_RATIO * 5
                                + mInsetTop + DisplayUtils.dpToPx(getContext(), 56)),
                        MeasureSpec.AT_MOST
                )
        );

        mFirstCardMarginTop = (int) (
                mBinding.circularSky.getMeasuredHeight() - DisplayUtils.dpToPx(getContext(), 28));
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mInsetTop = insets.top;
        setPadding(0, mInsetTop, 0, 0);
        requestLayout();
        return false;
    }

    /**
     * @return Return true whether execute switch animation.
     * */
    public boolean showCircles() {
        if (mBinding.circularSky.showCircle(mDaytime)) {
            changeStarAlPha();
            return true;
        }
        return false;
    }

    private void changeStarAlPha() {
        mBinding.starContainer.clearAnimation();

        StarAlphaAnimation animation = new StarAlphaAnimation(
                mBinding.starContainer.getAlpha(), mDaytime ? 0 : 1);
        animation.setDuration(500);
        mBinding.starContainer.startAnimation(animation);
    }

    // interface.

    @Override
    public void setWeather(@WeatherKindRule int weatherKind, boolean daytime,
                           @Nullable ResourceProvider provider) {
        if (provider == null) {
            return;
        }
        if (mWeatherKind == weatherKind
                && mDaytime == daytime
                && provider.getPackageName().equals(mIconProvider)) {
            return;
        }

        mWeatherKind = weatherKind;
        mIconProvider = provider.getPackageName();
        mDaytime = daytime;

        WeatherCode weatherCode = WeatherViewController.getWeatherCode(weatherKind);

        mIconDrawables = ResourceHelper.getWeatherIcons(provider, weatherCode, daytime);
        mIconAnimators = ResourceHelper.getWeatherAnimators(provider, weatherCode, daytime);

        mBinding.controller.showWeatherIcon();

        int newColor = getBackgroundColor();
        if (showCircles() || newColor != mBackgroundColor) {
            mBackgroundColor = newColor;
            Drawable drawable = getBackground();
            if (drawable instanceof ColorDrawable) {
                ValueAnimator colorAnimator = ValueAnimator.ofObject(
                        new ArgbEvaluator(),
                        ((ColorDrawable) drawable).getColor(),
                        newColor
                );
                colorAnimator.addUpdateListener(animation ->
                        setBackgroundColor((Integer) animation.getAnimatedValue())
                );
                colorAnimator.setDuration(300);
                colorAnimator.start();
            } else {
                setBackgroundColor(newColor);
            }
        }
    }

    @Override
    public void onClick() {
        mBinding.circularSky.touchCircle();
        mBinding.icon.startAnimators();
    }

    @Override
    public void onScroll(int scrollY) {
        mBinding.controller.setTranslationY(
                -mBinding.circularSky.getMeasuredHeight()
                        * Math.min(1f, 1f * scrollY / mFirstCardMarginTop)
        );
    }

    @Override
    public int getWeatherKind() {
        return mWeatherKind;
    }

    @Override
    public int[] getThemeColors(boolean lightTheme) {
        return getThemeColors(getContext(), mDaytime);
    }

    public static int[] getThemeColors(Context context, boolean lightTheme) {
        return new int[] {
                lightTheme
                        ? ContextCompat.getColor(context, R.color.lightPrimary_3)
                        : ContextCompat.getColor(context, R.color.darkPrimary_1),
                ContextCompat.getColor(context, R.color.lightPrimary_5),
                ContextCompat.getColor(context, R.color.darkPrimary_1)
        };
    }

    @Override
    public int getBackgroundColor() {
        if (mDaytime) {
            return ContextCompat.getColor(getContext(), R.color.lightPrimary_5);
        } else {
            return ContextCompat.getColor(getContext(), R.color.darkPrimary_5);
        }
    }

    @Override
    public int getHeaderHeight() {
        return mFirstCardMarginTop;
    }

    @Override
    public void setDrawable(boolean drawable) {
        // do nothing.
    }

    @Override
    public void setGravitySensorEnabled(boolean enabled) {
        // do nothing.
    }

    @Override
    public void setSystemBarStyle(Context context, Window window,
                                  boolean statusShader, boolean lightStatus,
                                  boolean navigationShader, boolean lightNavigation) {
        DisplayUtils.setSystemBarStyle(context, window, true,
                statusShader, lightNavigation, navigationShader, lightNavigation);
    }

    @Override
    public void setSystemBarColor(Context context, Window window,
                                  boolean statusShader, boolean lightStatus,
                                  boolean navigationShader, boolean lightNavigation) {
        DisplayUtils.setSystemBarColor(context, window, true,
                statusShader, lightNavigation, navigationShader, lightNavigation);
    }

    @Override
    public void OnWeatherIconChanging() {
        mBinding.icon.setAnimatableIcon(mIconDrawables, mIconAnimators);
        mBinding.icon.startAnimators();
    }
}