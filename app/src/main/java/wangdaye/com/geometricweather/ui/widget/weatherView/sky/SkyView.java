package wangdaye.com.geometricweather.ui.widget.weatherView.sky;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Sky realTimeWeather view.
 * */

public class SkyView extends FrameLayout
        implements WeatherIconControlView.OnWeatherIconChangingListener {
    // widget
    private WeatherIconControlView controlView;
    private CircularSkyView circularSkyView;
    private FrameLayout starContainer;
    private ImageView[] flagIcons;

    // data
    private int[] imageIds;
    private boolean animating = false;

    // animator
    private AnimatorSet[] iconTouchAnimators;
    private AnimatorSet[] starShineAnimators;

    /** <br> life cycle. */

    public SkyView(Context context) {
        super(context);
        this.initialize();
    }

    public SkyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public SkyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SkyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    @SuppressLint("InflateParams")
    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.container_sky_view, null);
        addView(view);

        this.controlView = (WeatherIconControlView) findViewById(R.id.container_sky_view_controller);
        controlView.setOnWeatherIconChangingListener(this);

        this.circularSkyView = (CircularSkyView) findViewById(R.id.container_sky_view_circularSkyView);

        this.starContainer = (FrameLayout) findViewById(R.id.container_sky_view_starContainer);
        if (TimeUtils.getInstance(getContext()).isDayTime()) {
            starContainer.setAlpha(0);
        } else {
            starContainer.setAlpha(1);
        }

        this.flagIcons = new ImageView[] {
                (ImageView) findViewById(R.id.container_sky_view_icon_1),
                (ImageView) findViewById(R.id.container_sky_view_icon_2),
                (ImageView) findViewById(R.id.container_sky_view_icon_3)};
        imageIds = new int[3];
        iconTouchAnimators = new AnimatorSet[3];

        ImageView[] starts = new ImageView[] {
                (ImageView) findViewById(R.id.container_sky_view_star_1),
                (ImageView) findViewById(R.id.container_sky_view_star_2)};
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
    }

    /** <br> UI. */

    public void reset() {
        circularSkyView.showCircle(TimeUtils.getInstance(getContext()).isDayTime());
        changeStarAlPha();
    }

    public void showCircles() {
        circularSkyView.showCircle(TimeUtils.getInstance(getContext()).isDayTime());
        changeStarAlPha();
    }

    public void onClickSky() {
        circularSkyView.touchCircle();
        if (!animating) {
            for (int i = 0; i < flagIcons.length; i ++) {
                if (imageIds[i] != 0 && iconTouchAnimators[i] != null) {
                    iconTouchAnimators[i].start();
                }
            }
        }
    }

    public void setWeather(Weather weather) {
        boolean isDay = TimeUtils.getInstance(getContext()).isDayTime();

        int[] animatorIds = WeatherHelper.getAnimatorId(weather.realTime.weatherKind, isDay);
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

        imageIds = WeatherHelper.getWeatherIcon(weather.realTime.weatherKind, isDay);

        this.showCircles();
        controlView.showWeatherIcon();
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

    /** <br> anim. */

    private void changeStarAlPha() {
        starContainer.clearAnimation();

        StarAlphaAnimation animation = new StarAlphaAnimation(
                starContainer.getAlpha(), TimeUtils.getInstance(getContext()).isDayTime() ? 0 : 1);
        animation.setDuration(500);
        starContainer.startAnimation(animation);
    }

    private class StarAlphaAnimation extends Animation {
        // data
        private float startAlpha;
        private float endAlpha;

        // life cycle.

        StarAlphaAnimation(float startAlpha, float endAlpha) {
            this.startAlpha = startAlpha;
            this.endAlpha = endAlpha;
        }

        // anim.

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            starContainer.setAlpha(startAlpha + (endAlpha - startAlpha) * interpolatedTime);
        }
    }

    /** <br> listener. */

    private AnimatorListenerAdapter[] starShineAnimatorListeners = new AnimatorListenerAdapter[] {
            // 1.
            new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    starShineAnimators[0].start();
                }
            },
            // 2.
            new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    starShineAnimators[1].start();
                }
            }
    };

    /** <br> interface. */

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
