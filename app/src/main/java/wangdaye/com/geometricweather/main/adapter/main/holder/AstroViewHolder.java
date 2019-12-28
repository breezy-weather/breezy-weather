package wangdaye.com.geometricweather.main.adapter.main.holder;

import android.animation.AnimatorSet;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.astro.MoonPhaseView;
import wangdaye.com.geometricweather.ui.widget.astro.SunMoonView;

public class AstroViewHolder extends AbstractMainCardViewHolder {

    private CardView card;

    private TextView title;
    private TextView phaseText;
    private MoonPhaseView phaseView;
    private SunMoonView sunMoonView;

    private RelativeLayout sunContainer;
    private TextView sunTxt;
    private RelativeLayout moonContainer;
    private TextView moonTxt;

    @Nullable private Weather weather;
    @Nullable private TimeZone timeZone;

    @Size(2) private float[] startTimes;
    @Size(2) private float[] endTimes;
    @Size(2) private float[] currentTimes;
    @Size(2) private float[] animCurrentTimes;
    private int phaseAngle;

    @Size(3) private AnimatorSet[] attachAnimatorSets;

    public AstroViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.container_main_sun_moon, parent, false));

        this.card = itemView.findViewById(R.id.container_main_sun_moon);
        this.title = itemView.findViewById(R.id.container_main_sun_moon_title);
        this.phaseText = itemView.findViewById(R.id.container_main_sun_moon_phaseText);
        this.phaseView = itemView.findViewById(R.id.container_main_sun_moon_phaseView);
        this.sunMoonView = itemView.findViewById(R.id.container_main_sun_moon_controlView);
        this.sunContainer = itemView.findViewById(R.id.container_main_sun_moon_sunContainer);
        this.sunTxt = itemView.findViewById(R.id.container_main_sun_moon_sunrise_sunset);
        this.moonContainer = itemView.findViewById(R.id.container_main_sun_moon_moonContainer);
        this.moonTxt = itemView.findViewById(R.id.container_main_sun_moon_moonrise_moonset);

        this.attachAnimatorSets = new AnimatorSet[] {null, null, null};
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider, @NonNull MainThemePicker picker,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, picker,
                listAnimationEnabled, itemAnimationEnabled, firstCard);

        weather = location.getWeather();
        timeZone = location.getTimeZone();
        assert weather != null;

        int[] themeColors = picker.getWeatherThemeColors();

        ensureTime(weather);
        ensurePhaseAngle(weather);

        card.setCardBackgroundColor(picker.getRootColor(context));

        title.setTextColor(themeColors[0]);

        if (!weather.getDailyForecast().get(0).getMoonPhase().isValid()) {
            phaseText.setVisibility(View.GONE);
            phaseView.setVisibility(View.GONE);
        } else {
            phaseText.setVisibility(View.VISIBLE);
            phaseText.setTextColor(picker.getTextContentColor(context));
            phaseText.setText(weather.getDailyForecast().get(0).getMoonPhase().getMoonPhase(context));
            phaseView.setVisibility(View.VISIBLE);
            phaseView.setColor(
                    ContextCompat.getColor(context, R.color.colorTextContent_dark),
                    ContextCompat.getColor(context, R.color.colorTextContent_light),
                    picker.getTextContentColor(context)
            );
        }

        sunMoonView.setSunDrawable(ResourceHelper.getSunDrawable(provider));
        sunMoonView.setMoonDrawable(ResourceHelper.getMoonDrawable(provider));

        if (itemAnimationEnabled) {
            sunMoonView.setTime(startTimes, endTimes, startTimes);
            sunMoonView.setDayIndicatorRotation(0);
            sunMoonView.setNightIndicatorRotation(0);
            phaseView.setSurfaceAngle(0);
        } else {
            sunMoonView.post(() -> sunMoonView.setTime(startTimes, endTimes, currentTimes));
            sunMoonView.setDayIndicatorRotation(0);
            sunMoonView.setNightIndicatorRotation(0);
            phaseView.setSurfaceAngle(phaseAngle);
        }
        if (picker.isLightTheme()) {
            sunMoonView.setColors(
                    themeColors[0],
                    ColorUtils.setAlphaComponent(themeColors[1], (int) (0.66 * 255)),
                    ColorUtils.setAlphaComponent(themeColors[1], (int) (0.33 * 255)),
                    picker.getRootColor(context),
                    picker.isLightTheme()
            );
        } else {
            sunMoonView.setColors(
                    themeColors[2],
                    ColorUtils.setAlphaComponent(themeColors[2], (int) (0.5 * 255)),
                    ColorUtils.setAlphaComponent(themeColors[2], (int) (0.2 * 255)),
                    picker.getRootColor(context),
                    picker.isLightTheme()
            );
        }

        if (weather.getDailyForecast().get(0).sun().isValid()) {
            sunContainer.setVisibility(View.VISIBLE);
            sunTxt.setText(
                    weather.getDailyForecast().get(0).sun().getRiseTime(context) + "↑"
                            + "\n"
                            + weather.getDailyForecast().get(0).sun().getSetTime(context) + "↓"
            );
        } else {
            sunContainer.setVisibility(View.GONE);
        }
        if (weather.getDailyForecast().get(0).moon().isValid()) {
            moonContainer.setVisibility(View.VISIBLE);
            moonTxt.setText(
                    weather.getDailyForecast().get(0).moon().getRiseTime(context) + "↑"
                            + "\n"
                            + weather.getDailyForecast().get(0).moon().getSetTime(context) + "↓"
            );
        } else {
            moonContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEnterScreen() {
        if (itemAnimationEnabled && weather != null) {
            ValueAnimator timeDay = ValueAnimator.ofObject(new FloatEvaluator(), startTimes[0], currentTimes[0]);
            timeDay.addUpdateListener(animation -> {
                animCurrentTimes[0] = (Float) animation.getAnimatedValue();
                sunMoonView.setTime(startTimes, endTimes, animCurrentTimes);
            });

            double totalRotationDay = 360.0 * 7 * (currentTimes[0] - startTimes[0]) / (endTimes[0] - startTimes[0]);
            ValueAnimator rotateDay = ValueAnimator.ofObject(
                    new FloatEvaluator(), 0, (int) (totalRotationDay - totalRotationDay % 360)
            );
            rotateDay.addUpdateListener(animation ->
                    sunMoonView.setDayIndicatorRotation((Float) animation.getAnimatedValue())
            );

            attachAnimatorSets[0] = new AnimatorSet();
            attachAnimatorSets[0].playTogether(timeDay, rotateDay);
            attachAnimatorSets[0].setInterpolator(new OvershootInterpolator(1f));
            attachAnimatorSets[0].setDuration(getPathAnimatorDuration(0));
            attachAnimatorSets[0].start();

            ValueAnimator timeNight = ValueAnimator.ofObject(new FloatEvaluator(), startTimes[1], currentTimes[1]);
            timeNight.addUpdateListener(animation -> {
                animCurrentTimes[1] = (Float) animation.getAnimatedValue();
                sunMoonView.setTime(startTimes, endTimes, animCurrentTimes);
            });

            double totalRotationNight = 360.0 * 4 * (currentTimes[1] - startTimes[1]) / (endTimes[1] - startTimes[1]);
            ValueAnimator rotateNight = ValueAnimator.ofObject(
                    new FloatEvaluator(), 0, (int) (totalRotationNight - totalRotationNight % 360)
            );
            rotateNight.addUpdateListener(animation ->
                    sunMoonView.setNightIndicatorRotation(-1 * (Float) animation.getAnimatedValue())
            );

            attachAnimatorSets[1] = new AnimatorSet();
            attachAnimatorSets[1].playTogether(timeNight, rotateNight);
            attachAnimatorSets[1].setInterpolator(new OvershootInterpolator(1f));
            attachAnimatorSets[1].setDuration(getPathAnimatorDuration(1));
            attachAnimatorSets[1].start();

            if (phaseAngle > 0) {
                ValueAnimator moonAngle = ValueAnimator.ofObject(new FloatEvaluator(), 0, phaseAngle);
                moonAngle.addUpdateListener(animation ->
                        phaseView.setSurfaceAngle((Float) animation.getAnimatedValue())
                );

                attachAnimatorSets[2] = new AnimatorSet();
                attachAnimatorSets[2].playTogether(moonAngle);
                attachAnimatorSets[2].setInterpolator(new DecelerateInterpolator());
                attachAnimatorSets[2].setDuration(getPhaseAnimatorDuration());
                attachAnimatorSets[2].start();
            }
        }
    }

    @Override
    public void onRecycleView() {
        super.onRecycleView();
        for (int i = 0; i < attachAnimatorSets.length; i ++) {
            if (attachAnimatorSets[i] != null && attachAnimatorSets[i].isRunning()) {
                attachAnimatorSets[i].cancel();
            }
            attachAnimatorSets[i] = null;
        }
    }

    private void ensureTime(@NonNull Weather weather) {
        Daily today = weather.getDailyForecast().get(0);
        Daily tomorrow = weather.getDailyForecast().get(1);

        Calendar calendar = Calendar.getInstance();
        if (timeZone != null) {
            calendar.setTimeZone(timeZone);
        }
        int currentTime = SunMoonView.decodeTime(calendar);
        calendar.setTimeZone(TimeZone.getDefault());

        calendar.setTime(Objects.requireNonNull(today.sun().getRiseDate()));
        int sunriseTime = SunMoonView.decodeTime(calendar);
        
        calendar.setTime(Objects.requireNonNull(today.sun().getSetDate()));
        int sunsetTime = SunMoonView.decodeTime(calendar);

        startTimes = new float[2];
        endTimes = new float[2];
        currentTimes = new float[] {currentTime, currentTime};

        // sun.
        startTimes[0] = sunriseTime;
        endTimes[0] = sunsetTime;

        // moon.
        if (!today.moon().isValid() || !tomorrow.moon().isValid()) {
            // do not have moonrise and moonset data.
            if (currentTime < sunriseTime) {
                // predawn. --> moon move from [sunset of yesterday] to [sunrise of today].
                calendar.setTime(Objects.requireNonNull(
                        today.sun().getSetDate()
                ));
                startTimes[1] = SunMoonView.decodeTime(calendar) - 24 * 60;
                endTimes[1] = sunriseTime;
            } else {
                // moon move from [sunset of today] to [sunrise of tomorrow]
                calendar.setTime(Objects.requireNonNull(
                        today.sun().getSetDate()
                ));
                startTimes[1] = SunMoonView.decodeTime(calendar);

                calendar.setTime(Objects.requireNonNull(
                        tomorrow.sun().getRiseDate()
                ));
                endTimes[1] = SunMoonView.decodeTime(calendar) + 24 * 60;
            }
        } else {
            // have moonrise and moonset data.
            if (currentTime < sunriseTime) {
                // predawn. --> moon move from [moonrise of yesterday] to [moonset of today].
                calendar.setTime(Objects.requireNonNull(
                        today.moon().getRiseDate()
                ));
                startTimes[1] = SunMoonView.decodeTime(calendar) - 24 * 60;

                calendar.setTime(Objects.requireNonNull(
                        today.moon().getSetDate()
                ));
                endTimes[1] = SunMoonView.decodeTime(calendar);
                if (endTimes[1] < startTimes[1]) {
                    endTimes[1] += 24 * 60;
                }
            } else {
                // moon move from [moonrise of today] to [moonset of tomorrow].
                calendar.setTime(Objects.requireNonNull(
                        today.moon().getRiseDate()
                ));
                startTimes[1] = SunMoonView.decodeTime(calendar);

                calendar.setTime(Objects.requireNonNull(
                        tomorrow.moon().getSetDate()
                ));
                endTimes[1] = SunMoonView.decodeTime(calendar);
                if (endTimes[1] < startTimes[1]) {
                    endTimes[1] += 24 * 60;
                }
            }
        }

        animCurrentTimes = new float[] {currentTimes[0], currentTimes[1]};
    }

    private void ensurePhaseAngle(@NonNull Weather weather) {
        Integer angle = weather.getDailyForecast().get(0).getMoonPhase().getAngle();
        phaseAngle = angle == null ? 0 : angle;
    }

    private long getPathAnimatorDuration(int index) {
        long duration = (long) Math.max(
                1000 + 3000.0
                        * (currentTimes[index] - startTimes[index])
                        / (endTimes[index] - startTimes[index]),
                0
        );
        return Math.min(duration, 4000);
    }

    private long getPhaseAnimatorDuration() {
        long duration = (long) Math.max(0, phaseAngle / 360.0 * 1000 + 1000);
        return Math.min(duration, 2000);
    }
}
