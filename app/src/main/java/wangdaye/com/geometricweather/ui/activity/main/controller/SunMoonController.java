package wangdaye.com.geometricweather.ui.activity.main.controller;

import android.animation.AnimatorSet;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.ui.widget.MoonPhaseView;
import wangdaye.com.geometricweather.ui.widget.SunMoonControlLayout;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

public class SunMoonController extends AbstractMainItemController {

    private CardView card;

    private TextView title;
    private TextView phaseText;
    private MoonPhaseView phaseView;
    private SunMoonControlLayout controlView;

    private TextView sunTxt;
    private RelativeLayout moonContainer;
    private TextView moonTxt;

    @NonNull private WeatherView weatherView;
    @Nullable private Weather weather;

    @Size(2) private int[] startTimes;
    @Size(2) private int[] endTimes;
    @Size(2) private int[] currentTimes;
    @Size(2) private int[] animCurrentTimes;
    private int phaseAngle;

    private boolean enable;
    private boolean executeEnterAnimation;
    @Size(3) private AnimatorSet[] attachAnimatorSets;

    public SunMoonController(@NonNull Activity activity, @NonNull WeatherView weatherView) {
        super(activity, activity.findViewById(R.id.container_main_sun_moon));

        this.card = view.findViewById(R.id.container_main_sun_moon);
        this.title = view.findViewById(R.id.container_main_sun_moon_title);
        this.phaseText = view.findViewById(R.id.container_main_sun_moon_phaseText);
        this.phaseView = view.findViewById(R.id.container_main_sun_moon_phaseView);
        this.controlView = view.findViewById(R.id.container_main_sun_moon_controlView);
        this.sunTxt = view.findViewById(R.id.container_main_sun_moon_sunrise_sunset);
        this.moonContainer = view.findViewById(R.id.container_main_sun_moon_moonContainer);
        this.moonTxt = view.findViewById(R.id.container_main_sun_moon_moonrise_moonset);

        this.weatherView = weatherView;
        this.executeEnterAnimation = true;
        this.attachAnimatorSets = new AnimatorSet[] {null, null, null};
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(@NonNull Location location) {
        if (!isDisplay("sunrise_sunset")) {
            enable = false;
            view.setVisibility(View.GONE);
            return;
        } else {
            enable = true;
            view.setVisibility(View.VISIBLE);
        }

        if (location.weather != null) {
            weather = location.weather;

            ensureTime(weather);
            ensurePhaseAngle(weather);

            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorRoot));

            title.setTextColor(weatherView.getThemeColors()[0]);

            if (TextUtils.isEmpty(weather.dailyList.get(0).moonPhase)) {
                phaseText.setVisibility(View.GONE);
                phaseView.setVisibility(View.GONE);
            } else {
                phaseText.setVisibility(View.VISIBLE);
                phaseText.setTextColor(ContextCompat.getColor(context, R.color.colorTextContent));
                phaseText.setText(WeatherHelper.getMoonPhaseName(context, weather.dailyList.get(0).moonPhase));
                phaseView.setVisibility(View.VISIBLE);
                phaseView.setColor();
            }

            controlView.loadIndicatorImage();
            if (executeEnterAnimation) {
                controlView.setTime(startTimes, endTimes, startTimes);
                controlView.setDayIndicatorRotation(0);
                controlView.setNightIndicatorRotation(0);
                phaseView.setSurfaceAngle(0);
            } else {
                controlView.setTime(startTimes, endTimes, currentTimes);
                controlView.setDayIndicatorRotation(0);
                controlView.setNightIndicatorRotation(0);
                phaseView.setSurfaceAngle(phaseAngle);
            }
            int[] themeColors = weatherView.getThemeColors();
            if (DisplayUtils.isDarkMode(context)) {
                controlView.setLineColors(new int[] {
                        themeColors[1],
                        ColorUtils.setAlphaComponent(themeColors[1], (int) (0.66 * 255)),
                        ColorUtils.setAlphaComponent(themeColors[1], (int) (0.33 * 255))});
            } else {
                controlView.setLineColors(new int[] {
                        themeColors[1],
                        ColorUtils.setAlphaComponent(themeColors[1], (int) (0.5 * 255)),
                        ColorUtils.setAlphaComponent(themeColors[1], (int) (0.2 * 255))});
            }
            controlView.ensureShader(context);

            sunTxt.setText(
                    weather.dailyList.get(0).astros[0] + "↑"
                            + " / "
                            + weather.dailyList.get(0).astros[1] + "↓");
            if (!TextUtils.isEmpty(weather.dailyList.get(0).astros[2])) {
                moonContainer.setVisibility(View.VISIBLE);
                moonTxt.setText(
                        weather.dailyList.get(0).astros[2] + "↑"
                                + " / "
                                + weather.dailyList.get(0).astros[3] + "↓");
            } else {
                moonContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onEnterScreen() {
        if (executeEnterAnimation && enable && weather != null) {
            executeEnterAnimation = false;

            ValueAnimator timeDay = ValueAnimator.ofObject(new IntEvaluator(), startTimes[0], currentTimes[0]);
            timeDay.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animCurrentTimes[0] = (Integer) animation.getAnimatedValue();
                    controlView.setTime(startTimes, endTimes, animCurrentTimes);
                }
            });

            double totalRotationDay = 360.0 * 7 * (currentTimes[0] - startTimes[0]) / (endTimes[0] - startTimes[0]);
            ValueAnimator rotateDay = ValueAnimator.ofObject(
                    new IntEvaluator(), 0, (int) (totalRotationDay - totalRotationDay % 360));
            rotateDay.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    controlView.setDayIndicatorRotation((Integer) animation.getAnimatedValue());
                }
            });

            attachAnimatorSets[0] = new AnimatorSet();
            attachAnimatorSets[0].playTogether(timeDay, rotateDay);
            attachAnimatorSets[0].setInterpolator(new OvershootInterpolator(1f));
            attachAnimatorSets[0].setDuration(getPathAnimatorDuration(0));
            attachAnimatorSets[0].start();

            ValueAnimator timeNight = ValueAnimator.ofObject(new IntEvaluator(), startTimes[1], currentTimes[1]);
            timeNight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    animCurrentTimes[1] = (Integer) animation.getAnimatedValue();
                    controlView.setTime(startTimes, endTimes, animCurrentTimes);
                }
            });

            double totalRotationNight = 360.0 * 4 * (currentTimes[1] - startTimes[1]) / (endTimes[1] - startTimes[1]);
            ValueAnimator rotateNight = ValueAnimator.ofObject(
                    new IntEvaluator(), 0, (int) (totalRotationNight - totalRotationNight % 360));
            rotateNight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    controlView.setNightIndicatorRotation(-1 * (Integer) animation.getAnimatedValue());
                }
            });

            attachAnimatorSets[1] = new AnimatorSet();
            attachAnimatorSets[1].playTogether(timeNight, rotateNight);
            attachAnimatorSets[1].setInterpolator(new OvershootInterpolator(1f));
            attachAnimatorSets[1].setDuration(getPathAnimatorDuration(1));
            attachAnimatorSets[1].start();

            if (phaseAngle > 0) {
                ValueAnimator moonAngle = ValueAnimator.ofObject(new IntEvaluator(), 0, phaseAngle);
                moonAngle.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        phaseView.setSurfaceAngle((Integer) animation.getAnimatedValue());
                    }
                });

                attachAnimatorSets[2] = new AnimatorSet();
                attachAnimatorSets[2].playTogether(moonAngle);
                attachAnimatorSets[2].setInterpolator(new DecelerateInterpolator());
                attachAnimatorSets[2].setDuration(getPhaseAnimatorDuration());
                attachAnimatorSets[2].start();
            }
        }
    }

    @Override
    public void onDestroy() {
        for (int i = 0; i < attachAnimatorSets.length; i ++) {
            if (attachAnimatorSets[i] != null && attachAnimatorSets[i].isRunning()) {
                attachAnimatorSets[i].cancel();
            }
            attachAnimatorSets[i] = null;
        }
    }

    private void ensureTime(@NonNull Weather weather) {
        Calendar calendar = Calendar.getInstance();
        int currentTime = SunMoonControlLayout.decodeTime(
                calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        int sunriseTime = SunMoonControlLayout.decodeTime(weather.dailyList.get(0).astros[0]);

        if (TextUtils.isEmpty(weather.dailyList.get(0).astros[2])
                || TextUtils.isEmpty(weather.dailyList.get(0).astros[3])
                || TextUtils.isEmpty(weather.dailyList.get(1).astros[2])
                || TextUtils.isEmpty(weather.dailyList.get(1).astros[3])) {
            if (currentTime < sunriseTime) {
                startTimes = new int[] {
                        sunriseTime,
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(0).astros[1]) - 24 * 60};
                endTimes = new int[] {
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(0).astros[1]),
                        sunriseTime};
                currentTimes = new int[] {currentTime, currentTime};
            } else {
                startTimes = new int[] {
                        sunriseTime,
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(0).astros[1])};
                endTimes = new int[] {
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(0).astros[1]),
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(1).astros[0]) + 24 * 60};
                currentTimes = new int[] {currentTime, currentTime};
            }
        } else {
            if (currentTime < sunriseTime) {
                startTimes = new int[] {
                        sunriseTime,
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(0).astros[2]) - 24 * 60};
                endTimes = new int[] {
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(0).astros[1]),
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(0).astros[3])};
                if (endTimes[1] < startTimes[1]) {
                    endTimes[1] += 24 * 60;
                }
                currentTimes = new int[] {currentTime, currentTime};
            } else {
                startTimes = new int[] {
                        sunriseTime,
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(0).astros[2])};
                endTimes = new int[] {
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(0).astros[1]),
                        SunMoonControlLayout.decodeTime(weather.dailyList.get(1).astros[3])};
                if (endTimes[1] < startTimes[1]) {
                    endTimes[1] += 24 * 60;
                }
                currentTimes = new int[] {currentTime, currentTime};
            }
        }

        animCurrentTimes = new int[] {currentTimes[0], currentTimes[1]};
    }

    private void ensurePhaseAngle(@NonNull Weather weather) {
        phaseAngle = WeatherHelper.getMoonPhaseAngle(weather.dailyList.get(0).moonPhase);
    }

    private long getPathAnimatorDuration(int index) {
        long duration = (long) Math.max(
                1000 + 3000.0 * (currentTimes[index] - startTimes[index]) / (endTimes[index] - startTimes[index]),
                0);
        return Math.min(duration, 4000);
    }

    private long getPhaseAnimatorDuration() {
        long duration = (long) Math.max(0, phaseAngle / 360.0 * 1000 + 1000);
        return Math.min(duration, 2000);
    }
}
