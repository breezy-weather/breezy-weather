package wangdaye.com.geometricweather.main.adapters.main.holder;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.ui.widgets.ArcProgress;
import wangdaye.com.geometricweather.main.adapters.AqiAdapter;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.weatherView.WeatherViewController;

public class AirQualityViewHolder extends AbstractMainCardViewHolder {

    private final CardView mCard;
    private final TextView mTitle;

    private final ArcProgress mProgress;
    private final RecyclerView mRecyclerView;
    private AqiAdapter mAdapter;

    @Nullable private Weather mWeather;
    @Nullable private TimeZone mTimeZone;
    private int mAqiIndex;

    private boolean mEnable;
    @Nullable private AnimatorSet mAttachAnimatorSet;

    public AirQualityViewHolder(ViewGroup parent) {
        super(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.container_main_aqi, parent, false)
        );

        mCard = itemView.findViewById(R.id.container_main_aqi);
        mTitle = itemView.findViewById(R.id.container_main_aqi_title);
        mProgress = itemView.findViewById(R.id.container_main_aqi_progress);
        mRecyclerView = itemView.findViewById(R.id.container_main_aqi_recyclerView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider,
                listAnimationEnabled, itemAnimationEnabled, firstCard);

        mWeather = location.getWeather();
        mTimeZone = location.getTimeZone();
        assert mWeather != null;

        mAqiIndex = mWeather.getCurrent().getAirQuality().getAqiIndex() == null
                ? 0
                : mWeather.getCurrent().getAirQuality().getAqiIndex();

        mEnable = true;

        mCard.setCardBackgroundColor(MainThemeColorProvider.getColor(location, R.attr.colorSurface));

        mTitle.setTextColor(
                ThemeManager
                        .getInstance(context)
                        .getWeatherThemeDelegate()
                        .getThemeColors(
                                context,
                                WeatherViewController.getWeatherKind(mWeather),
                                location.isDaylight()
                        )[0]
        );

        if (itemAnimationEnabled) {
            mProgress.setProgress(0);
            mProgress.setText(String.format("%d", 0));
            mProgress.setProgressColor(
                    ContextCompat.getColor(context, R.color.colorLevel_1),
                    MainModuleUtils.isHomeLightTheme(context, location.isDaylight())
            );
            mProgress.setArcBackgroundColor(MainThemeColorProvider.getColor(location, R.attr.colorOutline));
        } else {
            int aqiColor = mWeather.getCurrent().getAirQuality().getAqiColor(mProgress.getContext());
            mProgress.setProgress(mAqiIndex);
            mProgress.setText(String.format("%d", mAqiIndex));

            mProgress.setProgressColor(
                    aqiColor,
                    MainModuleUtils.isHomeLightTheme(context, location.isDaylight())
            );
            mProgress.setArcBackgroundColor(
                    ColorUtils.setAlphaComponent(aqiColor, (int) (255 * 0.1))
            );
        }

        mProgress.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText));
        mProgress.setBottomText(mWeather.getCurrent().getAirQuality().getAqiText());
        mProgress.setBottomTextColor(MainThemeColorProvider.getColor(location, R.attr.colorCaptionText));
        mProgress.setContentDescription(mAqiIndex + ", " + mWeather.getCurrent().getAirQuality().getAqiText());

        mAdapter = new AqiAdapter(context, location, itemAnimationEnabled);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onEnterScreen() {
        if (itemAnimationEnabled && mEnable && mWeather != null) {
            int aqiColor = mWeather.getCurrent().getAirQuality().getAqiColor(mProgress.getContext());

            ValueAnimator progressColor = ValueAnimator.ofObject(
                    new ArgbEvaluator(),
                    ContextCompat.getColor(context, R.color.colorLevel_1),
                    aqiColor
            );
            progressColor.addUpdateListener(animation -> mProgress.setProgressColor(
                    (Integer) animation.getAnimatedValue(),
                    MainModuleUtils.isHomeLightTheme(context, mWeather.isDaylight(mTimeZone))
            ));

            ValueAnimator backgroundColor = ValueAnimator.ofObject(
                    new ArgbEvaluator(),
                    MainThemeColorProvider.getColor(
                            mWeather.isDaylight(mTimeZone),
                            R.attr.colorOutline
                    ),
                    ColorUtils.setAlphaComponent(aqiColor, (int) (255 * 0.1))
            );
            backgroundColor.addUpdateListener(animation ->
                    mProgress.setArcBackgroundColor((Integer) animation.getAnimatedValue())
            );

            ValueAnimator aqiNumber = ValueAnimator.ofObject(new FloatEvaluator(), 0, mAqiIndex);
            aqiNumber.addUpdateListener(animation -> {
                mProgress.setProgress((Float) animation.getAnimatedValue());
                mProgress.setText(String.format("%d", (int) mProgress.getProgress()));
            });

            mAttachAnimatorSet = new AnimatorSet();
            mAttachAnimatorSet.playTogether(progressColor, backgroundColor, aqiNumber);
            mAttachAnimatorSet.setInterpolator(new DecelerateInterpolator());
            mAttachAnimatorSet.setDuration((long) (1500 + mAqiIndex / 400f * 1500));
            mAttachAnimatorSet.start();

            mAdapter.executeAnimation();
        }
    }

    @Override
    public void onRecycleView() {
        super.onRecycleView();
        if (mAttachAnimatorSet != null && mAttachAnimatorSet.isRunning()) {
            mAttachAnimatorSet.cancel();
        }
        mAttachAnimatorSet = null;
        if (mAdapter != null) {
            mAdapter.cancelAnimation();
        }
    }
}