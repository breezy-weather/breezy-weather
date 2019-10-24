package wangdaye.com.geometricweather.main.ui.adapter.main.holder;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.AqiAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.ArcProgress;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class AirQualityViewHolder extends AbstractMainViewHolder {

    private CardView card;
    private TextView title;

    private ArcProgress progress;
    private RecyclerView recyclerView;
    private AqiAdapter adapter;

    @NonNull private WeatherView weatherView;
    @Nullable private Weather weather;
    private int aqiIndex;

    private boolean enable;
    @Nullable private AnimatorSet attachAnimatorSet;

    public AirQualityViewHolder(@NonNull Activity activity, ViewGroup parent, @NonNull WeatherView weatherView,
                                @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                                @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                                @Px float cardRadius, @Px float cardElevation,
                                boolean itemAnimationEnabled) {
        super(activity, LayoutInflater.from(activity).inflate(R.layout.container_main_aqi, parent, false),
                provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation, itemAnimationEnabled);

        this.card = itemView.findViewById(R.id.container_main_aqi);
        this.title = itemView.findViewById(R.id.container_main_aqi_title);
        this.progress = itemView.findViewById(R.id.container_main_aqi_progress);
        this.recyclerView = itemView.findViewById(R.id.container_main_aqi_recyclerView);

        this.weatherView = weatherView;
    }

    @Override
    public void onBindView(@NonNull Location location) {
        weather = location.getWeather();
        assert weather != null;

        aqiIndex = weather.getCurrent().getAirQuality().getAqiIndex() == null
                ? 0
                : weather.getCurrent().getAirQuality().getAqiIndex();

        enable = true;
        itemView.setVisibility(View.VISIBLE);

        card.setCardBackgroundColor(picker.getRootColor(context));
        title.setTextColor(weatherView.getThemeColors(picker.isLightTheme())[0]);

        if (itemAnimationEnabled) {
            progress.setProgress(0);
            progress.setText("0");
            progress.setProgressColor(
                    ContextCompat.getColor(context, R.color.colorLevel_1),
                    picker.isLightTheme()
            );
            progress.setArcBackgroundColor(picker.getLineColor(context));
        } else {
            int aqiColor = weather.getCurrent().getAirQuality().getAqiColor(progress.getContext());
            progress.setProgress(aqiIndex);
            progress.setText(String.valueOf(aqiIndex));
            progress.setProgressColor(aqiColor, picker.isLightTheme());
            progress.setArcBackgroundColor(
                    ColorUtils.setAlphaComponent(aqiColor, (int) (255 * 0.1))
            );
        }
        progress.setTextColor(picker.getTextContentColor(context));
        progress.setBottomText(weather.getCurrent().getAirQuality().getAqiText());
        progress.setBottomTextColor(picker.getTextSubtitleColor(context));

        adapter = new AqiAdapter(context, weather, picker, itemAnimationEnabled);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public void onEnterScreen() {
        if (itemAnimationEnabled && enable && weather != null) {
            int aqiColor = weather.getCurrent().getAirQuality().getAqiColor(progress.getContext());

            ValueAnimator progressColor = ValueAnimator.ofObject(
                    new ArgbEvaluator(),
                    ContextCompat.getColor(context, R.color.colorLevel_1),
                    aqiColor
            );
            progressColor.addUpdateListener(animation -> progress.setProgressColor(
                    (Integer) animation.getAnimatedValue(), picker.isLightTheme()));

            ValueAnimator backgroundColor = ValueAnimator.ofObject(
                    new ArgbEvaluator(),
                    picker.getLineColor(context),
                    ColorUtils.setAlphaComponent(aqiColor, (int) (255 * 0.1))
            );
            backgroundColor.addUpdateListener(animation ->
                    progress.setArcBackgroundColor((Integer) animation.getAnimatedValue())
            );

            ValueAnimator aqiNumber = ValueAnimator.ofObject(new FloatEvaluator(), 0, aqiIndex);
            aqiNumber.addUpdateListener(animation -> {
                progress.setProgress((Float) animation.getAnimatedValue());
                progress.setText(Integer.toString((int) progress.getProgress()));
            });

            attachAnimatorSet = new AnimatorSet();
            attachAnimatorSet.playTogether(progressColor, backgroundColor, aqiNumber);
            attachAnimatorSet.setInterpolator(new DecelerateInterpolator());
            attachAnimatorSet.setDuration((long) (1500 + aqiIndex / 400f * 1500));
            attachAnimatorSet.start();

            adapter.executeAnimation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (attachAnimatorSet != null && attachAnimatorSet.isRunning()) {
            attachAnimatorSet.cancel();
        }
        attachAnimatorSet = null;
        if (adapter != null) {
            adapter.cancelAnimation();
        }
    }
}