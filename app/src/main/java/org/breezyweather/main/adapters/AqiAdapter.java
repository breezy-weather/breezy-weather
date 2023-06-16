package org.breezyweather.main.adapters;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.index.PollutantIndex;
import org.breezyweather.common.basic.models.options.unit.AirQualityCOUnit;
import org.breezyweather.common.basic.models.options.unit.AirQualityUnit;
import org.breezyweather.common.basic.models.weather.AirQuality;
import org.breezyweather.common.ui.widgets.RoundProgress;
import org.breezyweather.R;
import org.breezyweather.main.utils.MainThemeColorProvider;

public class AqiAdapter extends RecyclerView.Adapter<AqiAdapter.ViewHolder> {

    private final boolean mLightTheme;
    private final List<AqiItem> mItemList;
    private final List<ViewHolder> mHolderList;

    private static class AqiItem {
        @ColorInt int color;
        float progress;
        float max;
        String title;
        String content;
        String talkBack;

        boolean executeAnimation;

        AqiItem(@ColorInt int color, float progress, float max, String title, String content,
                String talkBack, boolean executeAnimation) {
            this.color = color;
            this.progress = progress;
            this.max = max;
            this.title = title;
            this.content = content;
            this.talkBack = talkBack;
            this.executeAnimation = executeAnimation;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private @Nullable AqiItem mItem;
        private Boolean mLightTheme;
        private boolean mExecuteAnimation;
        @Nullable private AnimatorSet mAttachAnimatorSet;

        private final TextView mTitle;
        private final TextView mContent;
        private final RoundProgress mProgress;

        ViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.item_aqi_title);
            mContent = itemView.findViewById(R.id.item_aqi_content);
            mProgress = itemView.findViewById(R.id.item_aqi_progress);
        }

        void onBindView(boolean lightTheme, AqiItem item) {
            Context context = itemView.getContext();

            mItem = item;
            mLightTheme = lightTheme;
            mExecuteAnimation = item.executeAnimation;

            itemView.setContentDescription(item.talkBack);

            mTitle.setText(item.title);
            mTitle.setTextColor(MainThemeColorProvider.getColor(lightTheme, R.attr.colorTitleText));

            mContent.setText(item.content);
            mContent.setTextColor(MainThemeColorProvider.getColor(lightTheme, R.attr.colorBodyText));

            if (mExecuteAnimation) {
                mProgress.setProgress(0);
                mProgress.setProgressColor(ContextCompat.getColor(context, R.color.colorLevel_1));
                mProgress.setProgressBackgroundColor(MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorOutline));
            } else {
                mProgress.setProgress((int) (100.0 * item.progress / item.max));
                mProgress.setProgressColor(item.color);
                mProgress.setProgressBackgroundColor(
                        ColorUtils.setAlphaComponent(item.color, (int) (255 * 0.1))
                );
            }
        }

        void executeAnimation() {
            if (mExecuteAnimation && mItem != null) {
                mExecuteAnimation = false;

                ValueAnimator progressColor = ValueAnimator.ofObject(
                        new ArgbEvaluator(),
                        ContextCompat.getColor(itemView.getContext(), R.color.colorLevel_1),
                        mItem.color
                );
                progressColor.addUpdateListener(animation ->
                        mProgress.setProgressColor((Integer) animation.getAnimatedValue())
                );

                ValueAnimator backgroundColor = ValueAnimator.ofObject(
                        new ArgbEvaluator(),
                        MainThemeColorProvider.getColor(mLightTheme, com.google.android.material.R.attr.colorOutline),
                        ColorUtils.setAlphaComponent(mItem.color, (int) (255 * 0.1))
                );
                backgroundColor.addUpdateListener(animation ->
                        mProgress.setProgressBackgroundColor((Integer) animation.getAnimatedValue())
                );

                ValueAnimator aqiNumber = ValueAnimator.ofObject(new FloatEvaluator(), 0, mItem.progress);
                aqiNumber.addUpdateListener(animation ->
                        mProgress.setProgress(
                                100.0f * ((Float) animation.getAnimatedValue()) / mItem.max
                        )
                );

                mAttachAnimatorSet = new AnimatorSet();
                mAttachAnimatorSet.playTogether(progressColor, backgroundColor, aqiNumber);
                mAttachAnimatorSet.setInterpolator(new DecelerateInterpolator(3));
                mAttachAnimatorSet.setDuration((long) (mItem.progress / mItem.max * 5000));
                mAttachAnimatorSet.start();
            }
        }

        void cancelAnimation() {
            if (mAttachAnimatorSet != null && mAttachAnimatorSet.isRunning()) {
                mAttachAnimatorSet.cancel();
            }
            mAttachAnimatorSet = null;
        }
    }

    public AqiAdapter(Context context, Location location, boolean executeAnimation) {
        mLightTheme = MainThemeColorProvider.isLightTheme(context, location);

        mItemList = new ArrayList<>();
        if (location.getWeather() != null
                && location.getWeather().getCurrent().getAirQuality().isValid()) {

            AirQuality airQuality = location.getWeather().getCurrent().getAirQuality();
            // We use air quality index for the progress bar instead of concentration for more realistic bar
            if (airQuality.getPM25() != null) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getColor(context, PollutantIndex.PM25),
                                airQuality.getIndex(PollutantIndex.PM25),
                                PollutantIndex.getIndexExcessivePollution(),
                                "PM2.5",
                                AirQualityUnit.MUGPCUM.getValueText(context, airQuality.getPM25()),
                                context.getString(R.string.air_quality_content_desc_pm25)
                                        + ", "
                                        + AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.getPM25()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getPM10() != null) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getColor(context, PollutantIndex.PM10),
                                airQuality.getIndex(PollutantIndex.PM10),
                                PollutantIndex.getIndexExcessivePollution(),
                                "PM10",
                                AirQualityUnit.MUGPCUM.getValueText(context, airQuality.getPM10()),
                                context.getString(R.string.air_quality_content_desc_pm10)
                                        + ", " + AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.getPM10()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getO3() != null) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getColor(context, PollutantIndex.O3),
                                airQuality.getIndex(PollutantIndex.O3),
                                PollutantIndex.getIndexExcessivePollution(),
                                "O₃",
                                AirQualityUnit.MUGPCUM.getValueText(context, airQuality.getO3()),
                                context.getString(R.string.air_quality_content_desc_o3)
                                        + ", " + AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.getO3()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getNO2() != null) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getColor(context, PollutantIndex.NO2),
                                airQuality.getIndex(PollutantIndex.NO2),
                                PollutantIndex.getIndexExcessivePollution(),
                                "NO₂",
                                AirQualityUnit.MUGPCUM.getValueText(context, airQuality.getNO2()),
                                context.getString(R.string.air_quality_content_desc_no2)
                                        + ", " + AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.getNO2()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getSO2() != null && airQuality.getSO2() > 0) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getColor(context, PollutantIndex.SO2),
                                airQuality.getIndex(PollutantIndex.SO2),
                                PollutantIndex.getIndexExcessivePollution(),
                                "SO₂",
                                AirQualityUnit.MUGPCUM.getValueText(context, airQuality.getSO2()),
                                context.getString(R.string.air_quality_content_desc_so2)
                                        + ", " + AirQualityUnit.MUGPCUM.getValueVoice(context, airQuality.getSO2()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getCO() != null && airQuality.getCO() > 0) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getColor(context, PollutantIndex.CO),
                                airQuality.getIndex(PollutantIndex.CO),
                                PollutantIndex.getIndexExcessivePollution(),
                                "CO",
                                AirQualityCOUnit.MGPCUM.getValueText(context, airQuality.getCO()),
                                context.getString(R.string.air_quality_content_desc_co)
                                        + ", " + AirQualityCOUnit.MGPCUM.getValueVoice(context, airQuality.getCO()),
                                executeAnimation
                        )
                );
            }
        }

        mHolderList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aqi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(mLightTheme, mItemList.get(position));
        if (mItemList.get(position).executeAnimation) {
            mHolderList.add(holder);
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void executeAnimation() {
        for (int i = 0; i < mHolderList.size(); i++) {
            mHolderList.get(i).executeAnimation();
        }
    }

    public void cancelAnimation() {
        for (int i = 0; i < mHolderList.size(); i++) {
            mHolderList.get(i).cancelAnimation();
        }
        mHolderList.clear();
    }
}
