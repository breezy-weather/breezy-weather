package wangdaye.com.geometricweather.main.ui.adapter;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.PollenUnit;
import wangdaye.com.geometricweather.basic.model.weather.Pollen;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.ui.widget.RoundProgress;

/**
 * Pollen adapter.
 */

public class PollenAdapter extends RecyclerView.Adapter<PollenAdapter.ViewHolder> {

    private List<PollenItem> itemList;
    private List<ViewHolder> holderList;
    private MainColorPicker colorPicker;

    private class PollenItem {
        @ColorInt int color;
        float progress;
        float max;
        @DrawableRes int iconResId;
        String title;
        String content;

        boolean executeAnimation;

        PollenItem(@ColorInt int color, float progress, float max,
                   @DrawableRes int iconResId, String title, String content,
                   boolean executeAnimation) {
            this.color = color;
            this.progress = progress;
            this.max = max;
            this.iconResId = iconResId;
            this.title = title;
            this.content = content;
            this.executeAnimation = executeAnimation;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @Nullable PollenItem item;
        private boolean executeAnimation;
        @Nullable private AnimatorSet attachAnimatorSet;

        private AppCompatImageView icon;
        private TextView title;
        private TextView content;
        private RoundProgress progress;

        ViewHolder(View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.item_pollen_icon);
            this.title = itemView.findViewById(R.id.item_pollen_title);
            this.content = itemView.findViewById(R.id.item_pollen_content);
            this.progress = itemView.findViewById(R.id.item_pollen_progress);
        }

        void onBindView(PollenItem item) {
            Context context = itemView.getContext();

            this.item = item;
            this.executeAnimation = item.executeAnimation;

            icon.setImageResource(item.iconResId);
            icon.setSupportImageTintList(
                    ColorStateList.valueOf(colorPicker.getTextContentColor(context))
            );

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

    public PollenAdapter(Context context, @Nullable Weather weather,
                         MainColorPicker colorPicker, boolean executeAnimation) {
        this.itemList = new ArrayList<>();
        if (weather != null && weather.getDailyForecast().get(0).getPollen().isValid()) {
            Pollen pollen = weather.getDailyForecast().get(0).getPollen();
            PollenUnit unit = PollenUnit.PPCM;
            if (pollen.getGrassIndex() != null && pollen.getGrassLevel() != null) {
                itemList.add(
                        new PollenItem(
                                Pollen.getPollenColor(context, pollen.getGrassLevel()),
                                pollen.getGrassLevel(),
                                6,
                                R.drawable.ic_grass,
                                context.getString(R.string.grass),
                                unit.getPollenText(pollen.getGrassIndex()) + " - " + pollen.getGrassDescription(),
                                executeAnimation
                        )
                );
            }
            if (pollen.getRagweedIndex() != null && pollen.getRagweedLevel() != null) {
                itemList.add(
                        new PollenItem(
                                Pollen.getPollenColor(context, pollen.getRagweedLevel()),
                                pollen.getRagweedLevel(),
                                6,
                                R.drawable.ic_ragweed,
                                context.getString(R.string.ragweed),
                                unit.getPollenText(pollen.getRagweedIndex()) + " - " + pollen.getRagweedDescription(),
                                executeAnimation
                        )
                );
            }
            if (pollen.getTreeIndex() != null && pollen.getTreeLevel() != null) {
                itemList.add(
                        new PollenItem(
                                Pollen.getPollenColor(context, pollen.getTreeLevel()),
                                pollen.getTreeLevel(),
                                6,
                                R.drawable.ic_tree,
                                context.getString(R.string.tree),
                                unit.getPollenText(pollen.getTreeIndex()) + " - " + pollen.getTreeDescription(),
                                executeAnimation
                        )
                );
            }
            if (pollen.getMoldIndex() != null && pollen.getMoldLevel() != null) {
                itemList.add(
                        new PollenItem(
                                Pollen.getPollenColor(context, pollen.getMoldLevel()),
                                pollen.getMoldLevel(),
                                6,
                                R.drawable.ic_mold,
                                context.getString(R.string.mold),
                                unit.getPollenText(pollen.getMoldIndex()) + " - " + pollen.getMoldDescription(),
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pollen, parent, false);
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
