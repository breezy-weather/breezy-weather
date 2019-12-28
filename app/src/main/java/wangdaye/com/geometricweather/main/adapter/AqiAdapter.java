package wangdaye.com.geometricweather.main.adapter;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.AirQualityUnit;
import wangdaye.com.geometricweather.basic.model.weather.AirQuality;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.ui.widget.RoundProgress;

/**
 * Aqi adapter.
 */

public class AqiAdapter extends RecyclerView.Adapter<AqiAdapter.ViewHolder> {

    private List<AqiItem> itemList;
    private List<ViewHolder> holderList;
    private MainThemePicker colorPicker;

    private class AqiItem {
        @ColorInt int color;
        float progress;
        float max;
        String title;
        String content;

        boolean executeAnimation;

        AqiItem(@ColorInt int color, float progress, float max, String title, String content,
                boolean executeAnimation) {
            this.color = color;
            this.progress = progress;
            this.max = max;
            this.title = title;
            this.content = content;
            this.executeAnimation = executeAnimation;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @Nullable AqiItem item;
        private boolean executeAnimation;
        @Nullable private AnimatorSet attachAnimatorSet;

        private TextView title;
        private TextView content;
        private RoundProgress progress;

        ViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.item_aqi_title);
            this.content = itemView.findViewById(R.id.item_aqi_content);
            this.progress = itemView.findViewById(R.id.item_aqi_progress);
        }

        void onBindView(AqiItem item) {
            Context context = itemView.getContext();

            this.item = item;
            this.executeAnimation = item.executeAnimation;

            title.setText(item.title);
            title.setTextColor(colorPicker.getTextContentColor(context));

            content.setText(item.content);
            content.setTextColor(colorPicker.getTextSubtitleColor(context));

            if (executeAnimation) {
                progress.setProgress(0);
                progress.setProgressColor(ContextCompat.getColor(context, R.color.colorLevel_1));
                progress.setProgressBackgroundColor(colorPicker.getLineColor(context));
            } else {
                progress.setProgress((int) (100.0 * item.progress / item.max));
                progress.setProgressColor(item.color);
                progress.setProgressBackgroundColor(
                        ColorUtils.setAlphaComponent(item.color, (int) (255 * 0.1))
                );
            }
        }

        void executeAnimation() {
            if (executeAnimation && item != null) {
                executeAnimation = false;

                ValueAnimator progressColor = ValueAnimator.ofObject(
                        new ArgbEvaluator(),
                        ContextCompat.getColor(itemView.getContext(), R.color.colorLevel_1),
                        item.color
                );
                progressColor.addUpdateListener(animation ->
                        progress.setProgressColor((Integer) animation.getAnimatedValue())
                );

                ValueAnimator backgroundColor = ValueAnimator.ofObject(
                        new ArgbEvaluator(),
                        colorPicker.getLineColor(itemView.getContext()),
                        ColorUtils.setAlphaComponent(item.color, (int) (255 * 0.1))
                );
                backgroundColor.addUpdateListener(animation ->
                        progress.setProgressBackgroundColor((Integer) animation.getAnimatedValue())
                );

                ValueAnimator aqiNumber = ValueAnimator.ofObject(new FloatEvaluator(), 0, item.progress);
                aqiNumber.addUpdateListener(animation ->
                        progress.setProgress(
                                100.0f * ((Float) animation.getAnimatedValue()) / item.max
                        )
                );

                attachAnimatorSet = new AnimatorSet();
                attachAnimatorSet.playTogether(progressColor, backgroundColor, aqiNumber);
                attachAnimatorSet.setInterpolator(new DecelerateInterpolator(3));
                attachAnimatorSet.setDuration((long) (item.progress / item.max * 5000));
                attachAnimatorSet.start();
            }
        }

        void cancelAnimation() {
            if (attachAnimatorSet != null && attachAnimatorSet.isRunning()) {
                attachAnimatorSet.cancel();
            }
            attachAnimatorSet = null;
        }
    }

    public AqiAdapter(Context context, @Nullable Weather weather,
                      MainThemePicker colorPicker, boolean executeAnimation) {
        this.itemList = new ArrayList<>();
        if (weather != null && weather.getCurrent().getAirQuality().isValid()) {
            AirQuality airQuality = weather.getCurrent().getAirQuality();
            if (airQuality.getPM25() != null) {
                itemList.add(
                        new AqiItem(
                                airQuality.getPm25Color(context),
                                airQuality.getPM25(),
                                250,
                                "PM2.5",
                                AirQualityUnit.MUGPCUM.getDensityText(airQuality.getPM25()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getPM10() != null) {
                itemList.add(
                        new AqiItem(
                                airQuality.getPm10Color(context),
                                airQuality.getPM10(),
                                420,
                                "PM10",
                                AirQualityUnit.MUGPCUM.getDensityText(airQuality.getPM10()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getSO2() != null) {
                itemList.add(
                        new AqiItem(
                                airQuality.getSo2Color(context),
                                airQuality.getSO2(),
                                1600,
                                "SO2",
                                AirQualityUnit.MUGPCUM.getDensityText(airQuality.getSO2()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getNO2() != null) {
                itemList.add(
                        new AqiItem(
                                airQuality.getNo2Color(context),
                                airQuality.getNO2(),
                                565,
                                "NO2",
                                AirQualityUnit.MUGPCUM.getDensityText(airQuality.getNO2()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getO3() != null) {
                itemList.add(
                        new AqiItem(
                                airQuality.getO3Color(context),
                                airQuality.getO3(),
                                800,
                                "O3",
                                AirQualityUnit.MUGPCUM.getDensityText(airQuality.getO3()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getCO() != null) {
                itemList.add(
                        new AqiItem(
                                airQuality.getCOColor(context),
                                airQuality.getCO(),
                                90,
                                "CO",
                                AirQualityUnit.MUGPCUM.getDensityText(airQuality.getCO()),
                                executeAnimation
                        )
                );
            }
        }

        this.holderList = new ArrayList<>();
        this.colorPicker = colorPicker;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aqi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(itemList.get(position));
        if (itemList.get(position).executeAnimation) {
            holderList.add(holder);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void executeAnimation() {
        for (int i = 0; i < holderList.size(); i++) {
            holderList.get(i).executeAnimation();
        }
    }

    public void cancelAnimation() {
        for (int i = 0; i < holderList.size(); i++) {
            holderList.get(i).cancelAnimation();
        }
        holderList.clear();
    }
}
