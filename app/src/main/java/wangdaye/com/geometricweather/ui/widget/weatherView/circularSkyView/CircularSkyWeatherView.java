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
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.AnimatableIconView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Circular sky weather view.
 * */

public class CircularSkyWeatherView extends FrameLayout
        implements WeatherView, WeatherIconControlView.OnWeatherIconChangingListener {

    @WeatherKindRule private int weatherKind = WEATHER_KING_NULL;
    @Nullable private String iconProvider;
    @ColorInt private int backgroundColor;
    private boolean daytime;

    private WeatherIconControlView controlView;
    private CircleView circleView;
    private FrameLayout starContainer;
    private AnimatableIconView flagIcon;

    @Size(3) private Drawable[] iconDrawables;
    @Size(3) private Animator[] iconAnimators;
    @Size(2) private Animator[] starShineAnimators;

    private int insetTop;
    private int firstCardMarginTop;

    private class StarAlphaAnimation extends Animation {

        private float startAlpha;
        private float endAlpha;

        StarAlphaAnimation(float startAlpha, float endAlpha) {
            this.startAlpha = startAlpha;
            this.endAlpha = endAlpha;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            starContainer.setAlpha(startAlpha + (endAlpha - startAlpha) * interpolatedTime);
        }
    }

    private AnimatorListenerAdapter[] starShineAnimatorListeners = new AnimatorListenerAdapter[] {

            new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    starShineAnimators[0].start();
                }
            },

            new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    starShineAnimators[1].start();
                }
            }
    };

    public CircularSkyWeatherView(Context context) {
        super(context);
        this.initialize();
    }

    public CircularSkyWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public CircularSkyWeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @SuppressLint("InflateParams")
    private void initialize() {
        this.daytime = TimeManager.getInstance(getContext()).isDayTime();

        this.backgroundColor = getBackgroundColor();
        setBackgroundColor(backgroundColor);

        this.controlView = (WeatherIconControlView) LayoutInflater.from(getContext()).inflate(
                R.layout.container_circular_sky_view, this, false);
        controlView.setOnWeatherIconChangingListener(this);
        addView(controlView);

        this.controlView = findViewById(R.id.container_circular_sky_view_controller);

        this.circleView = findViewById(R.id.container_circular_sky_view_circularSkyView);

        this.starContainer = findViewById(R.id.container_circular_sky_view_starContainer);
        if (daytime) {
            starContainer.setAlpha(0);
        } else {
            starContainer.setAlpha(1);
        }

        this.flagIcon = findViewById(R.id.container_circular_sky_view_icon);

        this.iconDrawables = new Drawable[] {null, null, null};
        this.iconAnimators = new Animator[] {null, null, null};

        AppCompatImageView[] starts = new AppCompatImageView[] {
                findViewById(R.id.container_circular_sky_view_star_1),
                findViewById(R.id.container_circular_sky_view_star_2)};
        Glide.with(getContext())
                .load(R.drawable.star_1)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(starts[0]);
        Glide.with(getContext())
                .load(R.drawable.star_2)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(starts[1]);

        this.starShineAnimators = new AnimatorSet[] {
                (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.start_shine_1),
                (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.start_shine_2)};
        for (int i = 0; i < starShineAnimators.length; i ++) {
            starShineAnimators[i].addListener(starShineAnimatorListeners[i]);
            starShineAnimators[i].setTarget(starts[i]);
            starShineAnimators[i].start();
        }

        this.insetTop = 0;
        this.firstCardMarginTop = 0;
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
                                / Constants.UNIT_RADIUS_RATIO * 5 + insetTop),
                        MeasureSpec.AT_MOST
                )
        );

        this.firstCardMarginTop = (int) (circleView.getMeasuredHeight() - DisplayUtils.dpToPx(getContext(), 28));
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        insetTop = insets.top;
        setPadding(0, insetTop, 0, 0);
        requestLayout();
        return false;
    }

    /**
     * @return Return true whether execute switch animation.
     * */
    public boolean showCircles() {
        if (circleView.showCircle(daytime)) {
            changeStarAlPha();
            return true;
        }
        return false;
    }

    private void changeStarAlPha() {
        starContainer.clearAnimation();

        StarAlphaAnimation animation = new StarAlphaAnimation(starContainer.getAlpha(), daytime ? 0 : 1);
        animation.setDuration(500);
        starContainer.startAnimation(animation);
    }

    // interface.

    @Override
    public void setWeather(@WeatherKindRule int weatherKind, boolean daytime,
                           @Nullable ResourceProvider provider) {
        if (provider == null) {
            return;
        }
        if (this.weatherKind == weatherKind
                && this.daytime == daytime
                && provider.getPackageName().equals(iconProvider)) {
            return;
        }

        this.weatherKind = weatherKind;
        this.iconProvider = provider.getPackageName();
        this.daytime = daytime;

        WeatherCode weatherCode = WeatherViewController.getWeatherCode(weatherKind);

        iconDrawables = ResourceHelper.getWeatherIcons(provider, weatherCode, daytime);
        iconAnimators = ResourceHelper.getWeatherAnimators(provider, weatherCode, daytime);

        controlView.showWeatherIcon();

        int newColor = getBackgroundColor();
        if (showCircles() || newColor != backgroundColor) {
            backgroundColor = newColor;
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
        circleView.touchCircle();
        flagIcon.startAnimators();
    }

    @Override
    public void onScroll(int scrollY) {
        controlView.setTranslationY(
                -circleView.getMeasuredHeight()
                        * Math.min(1f, 1f * scrollY / firstCardMarginTop)
        );
    }

    @Override
    public int getWeatherKind() {
        return weatherKind;
    }

    @Override
    public int[] getThemeColors(boolean lightTheme) {
        return getThemeColors(getContext(), daytime);
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
        if (daytime) {
            return ContextCompat.getColor(getContext(), R.color.lightPrimary_5);
        } else {
            return ContextCompat.getColor(getContext(), R.color.darkPrimary_5);
        }
    }

    @Override
    public int getHeaderHeight() {
        return firstCardMarginTop;
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
    public void OnWeatherIconChanging() {
        flagIcon.setAnimatableIcon(iconDrawables, iconAnimators);
        flagIcon.startAnimators();
    }
}