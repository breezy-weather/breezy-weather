package wangdaye.com.geometricweather.ui.widget.weatherView.circularSkyView;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.StatusBarView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherViewController;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Circular sky weather view.
 * */

public class CircularSkyWeatherView extends FrameLayout
        implements WeatherView, WeatherIconControlView.OnWeatherIconChangingListener {

    @WeatherKindRule private int weatherKind = WEATHER_KING_NULL;
    private boolean daytime = true;

    private LinearLayout container;
    private StatusBarView statusBar;
    private WeatherIconControlView controlView;
    private CircleView circleView;
    private FrameLayout starContainer;
    private AppCompatImageView[] flagIcons;

    private int[] imageIds;
    private boolean animating = false;

    private AnimatorSet[] iconTouchAnimators;
    private AnimatorSet[] starShineAnimators;

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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.container_circular_sky_view, null);
        addView(view);

        this.container = findViewById(R.id.container_circular_sky_view);
        container.setBackgroundColor(getBackgroundColor());

        this.statusBar = findViewById(R.id.container_circular_sky_view_statusBar);
        setStatusBarColor();

        this.controlView = findViewById(R.id.container_circular_sky_view_controller);
        controlView.setOnWeatherIconChangingListener(this);

        this.circleView = findViewById(R.id.container_circular_sky_view_circularSkyView);

        this.starContainer = findViewById(R.id.container_circular_sky_view_starContainer);
        if (daytime) {
            starContainer.setAlpha(0);
        } else {
            starContainer.setAlpha(1);
        }

        findViewById(R.id.container_circular_sky_view_iconContainer).setVisibility(GONE);

        this.flagIcons = new AppCompatImageView[] {
                findViewById(R.id.container_circular_sky_view_icon_1),
                findViewById(R.id.container_circular_sky_view_icon_2),
                findViewById(R.id.container_circular_sky_view_icon_3)};
        imageIds = new int[3];
        iconTouchAnimators = new AnimatorSet[3];

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

        this.firstCardMarginTop = (int) (getResources().getDisplayMetrics().widthPixels / 6.8 * 5.0
                + DisplayUtils.getStatusBarHeight(getResources()) - DisplayUtils.dpToPx(getContext(), 28));
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

    private void setStatusBarColor() {
        if (daytime) {
            statusBar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightPrimary_5));
        } else {
            statusBar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.darkPrimary_5));
        }
    }

    private void setFlagIconsImage() {
        for (int i = 0; i < flagIcons.length; i ++) {
            if (imageIds[i] == 0) {
                flagIcons[i].setVisibility(GONE);
            } else {
                Glide.with(getContext())
                        .load(imageIds[i])
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(flagIcons[i]);
                flagIcons[i].setVisibility(VISIBLE);
            }
        }
    }

    private void changeStarAlPha() {
        starContainer.clearAnimation();

        StarAlphaAnimation animation = new StarAlphaAnimation(starContainer.getAlpha(), daytime ? 0 : 1);
        animation.setDuration(500);
        starContainer.startAnimation(animation);
    }

    // interface.

    @Override
    public void setWeather(@WeatherKindRule int weatherKind, boolean daytime) {
        if (this.weatherKind == weatherKind && this.daytime == daytime) {
            return;
        }

        this.weatherKind = weatherKind;
        this.daytime = daytime;

        String entityWeatherKind = WeatherViewController.getEntityWeatherKind(weatherKind);

        int[] animatorIds = WeatherHelper.getAnimatorId(entityWeatherKind, daytime);
        iconTouchAnimators = new AnimatorSet[animatorIds.length];
        for (int i = 0; i < iconTouchAnimators.length; i ++) {
            if (animatorIds[i] != 0) {
                iconTouchAnimators[i] = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), animatorIds[i]);
                iconTouchAnimators[i].addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        animating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animating = false;
                    }
                });
                iconTouchAnimators[i].setTarget(flagIcons[i]);
            }
        }

        imageIds = WeatherHelper.getWeatherIcon(entityWeatherKind, daytime);

        setStatusBarColor();
        controlView.showWeatherIcon();
        if (showCircles()) {
            Drawable drawable = container.getBackground();
            if (drawable instanceof ColorDrawable) {
                ValueAnimator colorAnimator = ValueAnimator.ofObject(
                        new ArgbEvaluator(),
                        ((ColorDrawable) drawable).getColor(),
                        getBackgroundColor());
                colorAnimator.addUpdateListener(animation ->
                        container.setBackgroundColor((Integer) animation.getAnimatedValue())
                );
                colorAnimator.setDuration(300);
                colorAnimator.start();
            } else {
                container.setBackgroundColor(getBackgroundColor());
            }
        }
    }

    @Override
    public void onClick() {
        circleView.touchCircle();
        if (!animating) {
            for (int i = 0; i < flagIcons.length; i ++) {
                if (imageIds[i] != 0 && iconTouchAnimators[i] != null) {
                    iconTouchAnimators[i].start();
                }
            }
        }
    }

    @Override
    public void onScroll(int scrollY) {
        statusBar.setTranslationY(
                (float) (
                        -(circleView.getMeasuredHeight() + statusBar.getMeasuredHeight())
                                * Math.min(1, 1.0 * scrollY / firstCardMarginTop)
                )
        );
        controlView.setTranslationY(statusBar.getTranslationY());
    }

    @Override
    public int getWeatherKind() {
        return weatherKind;
    }

    @Override
    public int[] getThemeColors() {
        return new int[] {
                daytime
                        ? ContextCompat.getColor(getContext(), R.color.lightPrimary_3)
                        : ContextCompat.getColor(getContext(), R.color.darkPrimary_1),
                ContextCompat.getColor(getContext(), R.color.lightPrimary_5),
                ContextCompat.getColor(getContext(), R.color.darkPrimary_1)
        };
    }

    @Override
    public int getBackgroundColor() {
        if (daytime) {
            return ContextCompat.getColor(getContext(), R.color.lightPrimary_4);
        } else {
            return ContextCompat.getColor(getContext(), R.color.darkPrimary_4);
        }
    }

    @Override
    public int getFirstCardMarginTop() {
        return firstCardMarginTop;
    }

    @Override
    public void OnWeatherIconChanging() {
        setFlagIconsImage();
        for (int i = 0; i < flagIcons.length; i ++) {
            if (imageIds[i] != 0 && iconTouchAnimators[i] != null) {
                iconTouchAnimators[i].start();
            }
        }
    }
}
