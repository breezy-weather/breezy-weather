package wangdaye.com.geometricweather.main.adapters;

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
import wangdaye.com.geometricweather.basic.models.options.unit.AirQualityCOUnit;
import wangdaye.com.geometricweather.basic.models.options.unit.AirQualityUnit;
import wangdaye.com.geometricweather.basic.models.weather.AirQuality;
import wangdaye.com.geometricweather.basic.models.weather.Weather;
import wangdaye.com.geometricweather.ui.widgets.RoundProgress;
import wangdaye.com.geometricweather.utils.managers.ThemeManager;

/**
 * Aqi adapter.
 */

public class AqiAdapter extends RecyclerView.Adapter<AqiAdapter.ViewHolder> {

    private final List<AqiItem> mItemList;
    private final List<ViewHolder> mHolderList;
    private final ThemeManager mThemeManager;

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

    class ViewHolder extends RecyclerView.ViewHolder {
        private @Nullable AqiItem mItem;
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

        void onBindView(AqiItem item) {
            Context context = itemView.getContext();

            mItem = item;
            mExecuteAnimation = item.executeAnimation;

            itemView.setContentDescription(item.talkBack);

            mTitle.setText(item.title);
            mTitle.setTextColor(mThemeManager.getTextContentColor(context));

            mContent.setText(item.content);
            mContent.setTextColor(mThemeManager.getTextSubtitleColor(context));

            if (mExecuteAnimation) {
                mProgress.setProgress(0);
                mProgress.setProgressColor(ContextCompat.getColor(context, R.color.colorLevel_1));
                mProgress.setProgressBackgroundColor(mThemeManager.getLineColor(context));
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
                        mThemeManager.getLineColor(itemView.getContext()),
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

    public AqiAdapter(Context context, @Nullable Weather weather, boolean executeAnimation) {
        mItemList = new ArrayList<>();
        if (weather != null && weather.getCurrent().getAirQuality().isValid()) {
            AirQuality airQuality = weather.getCurrent().getAirQuality();
            if (airQuality.getPM25() != null) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getPm25Color(context),
                                airQuality.getPM25(),
                                250,
                                "PM2.5",
                                AirQualityUnit.MUGPCUM.getDensityText(context, airQuality.getPM25()),
                                context.getString(R.string.content_des_pm25)
                                        + ", " + AirQualityUnit.MUGPCUM.getDensityVoice(context, airQuality.getPM25()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getPM10() != null) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getPm10Color(context),
                                airQuality.getPM10(),
                                420,
                                "PM10",
                                AirQualityUnit.MUGPCUM.getDensityText(context, airQuality.getPM10()),
                                context.getString(R.string.content_des_pm10)
                                        + ", " + AirQualityUnit.MUGPCUM.getDensityVoice(context, airQuality.getPM10()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getSO2() != null) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getSo2Color(context),
                                airQuality.getSO2(),
                                1600,
                                "SO₂",
                                AirQualityUnit.MUGPCUM.getDensityText(context, airQuality.getSO2()),
                                context.getString(R.string.content_des_so2)
                                        + ", " + AirQualityUnit.MUGPCUM.getDensityVoice(context, airQuality.getSO2()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getNO2() != null) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getNo2Color(context),
                                airQuality.getNO2(),
                                565,
                                "NO₂",
                                AirQualityUnit.MUGPCUM.getDensityText(context, airQuality.getNO2()),
                                context.getString(R.string.content_des_no2)
                                        + ", " + AirQualityUnit.MUGPCUM.getDensityVoice(context, airQuality.getNO2()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getO3() != null) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getO3Color(context),
                                airQuality.getO3(),
                                800,
                                "O₃",
                                AirQualityUnit.MUGPCUM.getDensityText(context, airQuality.getO3()),
                                context.getString(R.string.content_des_o3)
                                        + ", " + AirQualityUnit.MUGPCUM.getDensityVoice(context, airQuality.getO3()),
                                executeAnimation
                        )
                );
            }
            if (airQuality.getCO() != null) {
                mItemList.add(
                        new AqiItem(
                                airQuality.getCOColor(context),
                                airQuality.getCO(),
                                90,
                                "CO",
                                AirQualityCOUnit.MGPCUM.getDensityText(context, airQuality.getCO()),
                                context.getString(R.string.content_des_co)
                                        + ", " + AirQualityCOUnit.MGPCUM.getDensityVoice(context, airQuality.getCO()),
                                executeAnimation
                        )
                );
            }
        }

        mHolderList = new ArrayList<>();
        mThemeManager = ThemeManager.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aqi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(mItemList.get(position));
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
