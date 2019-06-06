package wangdaye.com.geometricweather.main.controller;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainColorPicker;
import wangdaye.com.geometricweather.main.adapter.AqiAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.ArcProgress;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class AqiController extends AbstractMainItemController {

    private CardView card;
    private TextView title;

    private ArcProgress progress;
    private RecyclerView recyclerView;
    private AqiAdapter adapter;

    @NonNull private WeatherView weatherView;
    @Nullable private Weather weather;

    private boolean enable;
    private boolean executeEnterAnimation;
    @Nullable private AnimatorSet attachAnimatorSet;

    public AqiController(@NonNull Activity activity, @NonNull WeatherView weatherView,
                         @NonNull ResourceProvider provider, @NonNull MainColorPicker picker) {
        super(activity, activity.findViewById(R.id.container_main_aqi), provider, picker);

        this.card = view.findViewById(R.id.container_main_aqi);
        this.title = view.findViewById(R.id.container_main_aqi_title);
        this.progress = view.findViewById(R.id.container_main_aqi_progress);
        this.recyclerView = view.findViewById(R.id.container_main_aqi_recyclerView);

        this.weatherView = weatherView;
        this.executeEnterAnimation = true;
    }

    @Override
    public void onBindView(@NonNull Location location) {
        if (!isDisplay("air_quality")) {
            enable = false;
            view.setVisibility(View.GONE);
            return;
        } else {
            enable = true;
            view.setVisibility(View.VISIBLE);
        }

        weather = location.weather;
        if (weather != null) {
            if (weather.aqi.aqi <= 0) {
                enable = false;
                view.setVisibility(View.GONE);
                return;
            }

            enable = true;
            view.setVisibility(View.VISIBLE);

            card.setCardBackgroundColor(picker.getRootColor(context));
            title.setTextColor(weatherView.getThemeColors(picker.isLightTheme())[0]);

            if (executeEnterAnimation) {
                progress.setProgress(0);
                progress.setText("0");
                progress.setProgressColor(ContextCompat.getColor(context, R.color.colorLevel_1));
                progress.setArcBackgroundColor(picker.getLineColor(context));
            } else {
                int aqiColor = WeatherHelper.getAqiColor(progress.getContext(), weather.aqi.aqi);
                progress.setProgress(weather.aqi.aqi);
                progress.setText(String.valueOf(weather.aqi.aqi));
                progress.setProgressColor(aqiColor);
                progress.setArcBackgroundColor(
                        ColorUtils.setAlphaComponent(aqiColor, (int) (255 * 0.1))
                );
            }
            progress.setTextColor(picker.getTextContentColor(context));
            progress.setBottomText(weather.aqi.quality);
            progress.setBottomTextColor(picker.getTextSubtitleColor(context));
            progress.ensureShadowShader(picker.isLightTheme());

            adapter = new AqiAdapter(context, weather, picker, executeEnterAnimation);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }

    @Override
    public void onEnterScreen() {
        if (executeEnterAnimation && enable && weather != null) {
            executeEnterAnimation = false;

            int aqiColor = WeatherHelper.getAqiColor(progress.getContext(), weather.aqi.aqi);

            ValueAnimator progressColor = ValueAnimator.ofObject(
                    new ArgbEvaluator(),
                    ContextCompat.getColor(context, R.color.colorLevel_1),
                    aqiColor
            );
            progressColor.addUpdateListener(animation ->
                    progress.setProgressColor((Integer) animation.getAnimatedValue())
            );

            ValueAnimator backgroundColor = ValueAnimator.ofObject(
                    new ArgbEvaluator(),
                    picker.getLineColor(context),
                    ColorUtils.setAlphaComponent(aqiColor, (int) (255 * 0.1))
            );
            backgroundColor.addUpdateListener(animation ->
                    progress.setArcBackgroundColor((Integer) animation.getAnimatedValue())
            );

            ValueAnimator aqiNumber = ValueAnimator.ofObject(new FloatEvaluator(), 0, weather.aqi.aqi);
            aqiNumber.addUpdateListener(animation -> {
                progress.setProgress((Float) animation.getAnimatedValue());
                progress.setText(Integer.toString((int) progress.getProgress()));
            });

            attachAnimatorSet = new AnimatorSet();
            attachAnimatorSet.playTogether(progressColor, backgroundColor, aqiNumber);
            attachAnimatorSet.setInterpolator(new DecelerateInterpolator());
            attachAnimatorSet.setDuration((long) (1500 + weather.aqi.aqi / 400f * 1500));
            attachAnimatorSet.start();

            adapter.executeAnimation();
        }
    }

    @Override
    public void onDestroy() {
        if (attachAnimatorSet != null && attachAnimatorSet.isRunning()) {
            attachAnimatorSet.cancel();
        }
        attachAnimatorSet = null;
        if (adapter != null) {
            adapter.cancelAnimation();
        }
    }
}