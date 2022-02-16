package wangdaye.com.geometricweather.main.adapters.main.holder;

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
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.common.ui.widgets.astro.MoonPhaseView;
import wangdaye.com.geometricweather.common.ui.widgets.astro.SunMoonView;

public class AstroViewHolder extends AbstractMainCardViewHolder {

    private final CardView mCard;

    private final TextView mTitle;
    private final TextView mPhaseText;
    private final MoonPhaseView mPhaseView;
    private final SunMoonView mSunMoonView;

    private final RelativeLayout mSunContainer;
    private final TextView mSunTxt;
    private final RelativeLayout mMoonContainer;
    private final TextView mMoonTxt;

    @Nullable private Weather mWeather;
    @Nullable private TimeZone mTimeZone;

    @Size(2) private float[] mStartTimes;
    @Size(2) private float[] mEndTimes;
    @Size(2) private float[] mCurrentTimes;
    @Size(2) private float[] mAnimCurrentTimes;
    private int mPhaseAngle;

    @Size(3) private final AnimatorSet[] mAttachAnimatorSets;

    public AstroViewHolder(ViewGroup parent, MainThemeManager themeManager) {
        super(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.container_main_sun_moon, parent, false), themeManager);

        mCard = itemView.findViewById(R.id.container_main_sun_moon);
        mTitle = itemView.findViewById(R.id.container_main_sun_moon_title);
        mPhaseText = itemView.findViewById(R.id.container_main_sun_moon_phaseText);
        mPhaseView = itemView.findViewById(R.id.container_main_sun_moon_phaseView);
        mSunMoonView = itemView.findViewById(R.id.container_main_sun_moon_controlView);
        mSunContainer = itemView.findViewById(R.id.container_main_sun_moon_sunContainer);
        mSunTxt = itemView.findViewById(R.id.container_main_sun_moon_sunrise_sunset);
        mMoonContainer = itemView.findViewById(R.id.container_main_sun_moon_moonContainer);
        mMoonTxt = itemView.findViewById(R.id.container_main_sun_moon_moonrise_moonset);

        mAttachAnimatorSets = new AnimatorSet[] {null, null, null};
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider,
                listAnimationEnabled, itemAnimationEnabled, firstCard);

        mWeather = location.getWeather();
        mTimeZone = location.getTimeZone();
        assert mWeather != null;

        int[] themeColors = themeManager.getWeatherThemeColors();
        StringBuilder talkBackBuilder = new StringBuilder(mTitle.getText());

        ensureTime(mWeather);
        ensurePhaseAngle(mWeather);

        mCard.setCardBackgroundColor(themeManager.getRootColor(context));

        mTitle.setTextColor(themeColors[0]);

        if (!mWeather.getDailyForecast().get(0).getMoonPhase().isValid()) {
            mPhaseText.setVisibility(View.GONE);
            mPhaseView.setVisibility(View.GONE);
        } else {
            mPhaseText.setVisibility(View.VISIBLE);
            mPhaseText.setTextColor(themeManager.getTextContentColor(context));
            mPhaseText.setText(mWeather.getDailyForecast().get(0).getMoonPhase().getMoonPhase(context));
            mPhaseView.setVisibility(View.VISIBLE);
            mPhaseView.setColor(
                    ContextCompat.getColor(context, R.color.colorTextContent_dark),
                    ContextCompat.getColor(context, R.color.colorTextContent_light),
                    themeManager.getTextContentColor(context)
            );

            talkBackBuilder.append(", ").append(mPhaseText.getText());
        }

        mSunMoonView.setSunDrawable(ResourceHelper.getSunDrawable(provider));
        mSunMoonView.setMoonDrawable(ResourceHelper.getMoonDrawable(provider));

        if (itemAnimationEnabled) {
            mSunMoonView.setTime(mStartTimes, mEndTimes, mStartTimes);
            mSunMoonView.setDayIndicatorRotation(0);
            mSunMoonView.setNightIndicatorRotation(0);
            mPhaseView.setSurfaceAngle(0);
        } else {
            mSunMoonView.post(() -> mSunMoonView.setTime(mStartTimes, mEndTimes, mCurrentTimes));
            mSunMoonView.setDayIndicatorRotation(0);
            mSunMoonView.setNightIndicatorRotation(0);
            mPhaseView.setSurfaceAngle(mPhaseAngle);
        }
        if (themeManager.isLightTheme()) {
            mSunMoonView.setColors(
                    themeColors[0],
                    ColorUtils.setAlphaComponent(themeColors[1], (int) (0.66 * 255)),
                    ColorUtils.setAlphaComponent(themeColors[1], (int) (0.33 * 255)),
                    themeManager.getRootColor(context),
                    themeManager.isLightTheme()
            );
        } else {
            mSunMoonView.setColors(
                    themeColors[2],
                    ColorUtils.setAlphaComponent(themeColors[2], (int) (0.5 * 255)),
                    ColorUtils.setAlphaComponent(themeColors[2], (int) (0.2 * 255)),
                    themeManager.getRootColor(context),
                    themeManager.isLightTheme()
            );
        }

        if (mWeather.getDailyForecast().get(0).sun().isValid()) {
            String sunriseTime = mWeather.getDailyForecast().get(0).sun().getRiseTime(context);
            String sunsetTime = mWeather.getDailyForecast().get(0).sun().getSetTime(context);

            mSunContainer.setVisibility(View.VISIBLE);
            mSunTxt.setText(sunriseTime + "↑" + "\n" + sunsetTime + "↓");

            assert sunriseTime != null && sunsetTime != null;
            talkBackBuilder
                    .append(", ")
                    .append(activity.getString(R.string.content_des_sunrise).replace("$", sunriseTime))
                    .append(", ")
                    .append(activity.getString(R.string.content_des_sunset).replace("$", sunsetTime));
        } else {
            mSunContainer.setVisibility(View.GONE);
        }
        if (mWeather.getDailyForecast().get(0).moon().isValid()) {
            String moonriseTime = mWeather.getDailyForecast().get(0).moon().getRiseTime(context);
            String moonsetTime = mWeather.getDailyForecast().get(0).moon().getSetTime(context);

            mMoonContainer.setVisibility(View.VISIBLE);
            mMoonTxt.setText(moonriseTime + "↑" + "\n" + moonsetTime + "↓");

            assert moonriseTime != null && moonsetTime != null;
            talkBackBuilder
                    .append(", ")
                    .append(activity.getString(R.string.content_des_moonrise).replace("$", moonriseTime))
                    .append(", ")
                    .append(activity.getString(R.string.content_des_moonset).replace("$", moonsetTime));
        } else {
            mMoonContainer.setVisibility(View.GONE);
        }

        itemView.setContentDescription(talkBackBuilder.toString());
    }

    @Override
    public void onEnterScreen() {
        if (itemAnimationEnabled && mWeather != null) {
            ValueAnimator timeDay = ValueAnimator.ofObject(new FloatEvaluator(), mStartTimes[0], mCurrentTimes[0]);
            timeDay.addUpdateListener(animation -> {
                mAnimCurrentTimes[0] = (Float) animation.getAnimatedValue();
                mSunMoonView.setTime(mStartTimes, mEndTimes, mAnimCurrentTimes);
            });

            double totalRotationDay = 360.0 * 7 * (mCurrentTimes[0] - mStartTimes[0]) / (mEndTimes[0] - mStartTimes[0]);
            ValueAnimator rotateDay = ValueAnimator.ofObject(
                    new FloatEvaluator(), 0, (int) (totalRotationDay - totalRotationDay % 360)
            );
            rotateDay.addUpdateListener(animation ->
                    mSunMoonView.setDayIndicatorRotation((Float) animation.getAnimatedValue())
            );

            mAttachAnimatorSets[0] = new AnimatorSet();
            mAttachAnimatorSets[0].playTogether(timeDay, rotateDay);
            mAttachAnimatorSets[0].setInterpolator(new OvershootInterpolator(1f));
            mAttachAnimatorSets[0].setDuration(getPathAnimatorDuration(0));
            mAttachAnimatorSets[0].start();

            ValueAnimator timeNight = ValueAnimator.ofObject(new FloatEvaluator(), mStartTimes[1], mCurrentTimes[1]);
            timeNight.addUpdateListener(animation -> {
                mAnimCurrentTimes[1] = (Float) animation.getAnimatedValue();
                mSunMoonView.setTime(mStartTimes, mEndTimes, mAnimCurrentTimes);
            });

            double totalRotationNight = 360.0 * 4 * (mCurrentTimes[1] - mStartTimes[1]) / (mEndTimes[1] - mStartTimes[1]);
            ValueAnimator rotateNight = ValueAnimator.ofObject(
                    new FloatEvaluator(), 0, (int) (totalRotationNight - totalRotationNight % 360)
            );
            rotateNight.addUpdateListener(animation ->
                    mSunMoonView.setNightIndicatorRotation(-1 * (Float) animation.getAnimatedValue())
            );

            mAttachAnimatorSets[1] = new AnimatorSet();
            mAttachAnimatorSets[1].playTogether(timeNight, rotateNight);
            mAttachAnimatorSets[1].setInterpolator(new OvershootInterpolator(1f));
            mAttachAnimatorSets[1].setDuration(getPathAnimatorDuration(1));
            mAttachAnimatorSets[1].start();

            if (mPhaseAngle > 0) {
                ValueAnimator moonAngle = ValueAnimator.ofObject(new FloatEvaluator(), 0, mPhaseAngle);
                moonAngle.addUpdateListener(animation ->
                        mPhaseView.setSurfaceAngle((Float) animation.getAnimatedValue())
                );

                mAttachAnimatorSets[2] = new AnimatorSet();
                mAttachAnimatorSets[2].playTogether(moonAngle);
                mAttachAnimatorSets[2].setInterpolator(new DecelerateInterpolator());
                mAttachAnimatorSets[2].setDuration(getPhaseAnimatorDuration());
                mAttachAnimatorSets[2].start();
            }
        }
    }

    @Override
    public void onRecycleView() {
        super.onRecycleView();
        for (int i = 0; i < mAttachAnimatorSets.length; i ++) {
            if (mAttachAnimatorSets[i] != null && mAttachAnimatorSets[i].isRunning()) {
                mAttachAnimatorSets[i].cancel();
            }
            mAttachAnimatorSets[i] = null;
        }
    }

    private void ensureTime(@NonNull Weather weather) {
        Daily today = weather.getDailyForecast().get(0);
        Daily tomorrow = weather.getDailyForecast().get(1);

        Calendar calendar = Calendar.getInstance();
        if (mTimeZone != null) {
            calendar.setTimeZone(mTimeZone);
        }
        int currentTime = SunMoonView.decodeTime(calendar);
        calendar.setTimeZone(TimeZone.getDefault());

        calendar.setTime(Objects.requireNonNull(today.sun().getRiseDate()));
        int sunriseTime = SunMoonView.decodeTime(calendar);
        
        calendar.setTime(Objects.requireNonNull(today.sun().getSetDate()));
        int sunsetTime = SunMoonView.decodeTime(calendar);

        mStartTimes = new float[2];
        mEndTimes = new float[2];
        mCurrentTimes = new float[] {currentTime, currentTime};

        // sun.
        mStartTimes[0] = sunriseTime;
        mEndTimes[0] = sunsetTime;

        // moon.
        if (!today.moon().isValid() || !tomorrow.moon().isValid()) {
            // do not have moonrise and moonset data.
            if (currentTime < sunriseTime) {
                // predawn. --> moon move from [sunset of yesterday] to [sunrise of today].
                calendar.setTime(Objects.requireNonNull(
                        today.sun().getSetDate()
                ));
                mStartTimes[1] = SunMoonView.decodeTime(calendar) - 24 * 60;
                mEndTimes[1] = sunriseTime;
            } else {
                // moon move from [sunset of today] to [sunrise of tomorrow]
                calendar.setTime(Objects.requireNonNull(
                        today.sun().getSetDate()
                ));
                mStartTimes[1] = SunMoonView.decodeTime(calendar);

                calendar.setTime(Objects.requireNonNull(
                        tomorrow.sun().getRiseDate()
                ));
                mEndTimes[1] = SunMoonView.decodeTime(calendar) + 24 * 60;
            }
        } else {
            // have moonrise and moonset data.
            if (currentTime < sunriseTime) {
                // predawn. --> moon move from [moonrise of yesterday] to [moonset of today].
                calendar.setTime(Objects.requireNonNull(
                        today.moon().getRiseDate()
                ));
                mStartTimes[1] = SunMoonView.decodeTime(calendar) - 24 * 60;

                calendar.setTime(Objects.requireNonNull(
                        today.moon().getSetDate()
                ));
                mEndTimes[1] = SunMoonView.decodeTime(calendar);
                if (mEndTimes[1] < mStartTimes[1]) {
                    mEndTimes[1] += 24 * 60;
                }
            } else {
                // moon move from [moonrise of today] to [moonset of tomorrow].
                calendar.setTime(Objects.requireNonNull(
                        today.moon().getRiseDate()
                ));
                mStartTimes[1] = SunMoonView.decodeTime(calendar);

                calendar.setTime(Objects.requireNonNull(
                        tomorrow.moon().getSetDate()
                ));
                mEndTimes[1] = SunMoonView.decodeTime(calendar);
                if (mEndTimes[1] < mStartTimes[1]) {
                    mEndTimes[1] += 24 * 60;
                }
            }
        }

        mAnimCurrentTimes = new float[] {mCurrentTimes[0], mCurrentTimes[1]};
    }

    private void ensurePhaseAngle(@NonNull Weather weather) {
        Integer angle = weather.getDailyForecast().get(0).getMoonPhase().getAngle();
        mPhaseAngle = angle == null ? 0 : angle;
    }

    private long getPathAnimatorDuration(int index) {
        long duration = (long) Math.max(
                1000 + 3000.0
                        * (mCurrentTimes[index] - mStartTimes[index])
                        / (mEndTimes[index] - mStartTimes[index]),
                0
        );
        return Math.min(duration, 4000);
    }

    private long getPhaseAnimatorDuration() {
        long duration = (long) Math.max(0, mPhaseAngle / 360.0 * 1000 + 1000);
        return Math.min(duration, 2000);
    }
}
