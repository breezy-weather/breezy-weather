package org.breezyweather.main.adapters.main.holder;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import io.reactivex.rxjava3.disposables.Disposable;
import org.breezyweather.R;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.main.widgets.TextRelativeClock;
import org.breezyweather.theme.ThemeManager;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.theme.weatherView.WeatherView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RefreshTimeViewHolder extends AbstractMainViewHolder {
    private final LinearLayout mContainer;
    private final TextRelativeClock mRefreshTimeText;
    private @Nullable Disposable mDisposable;

    public RefreshTimeViewHolder(ViewGroup parent, WeatherView weatherView) {
        super(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.container_main_refresh_time, parent, false)
        );

        mContainer = itemView.findViewById(R.id.container_main_refresh_time);
        mRefreshTimeText = itemView.findViewById(R.id.container_main_refresh_time_text);
        mDisposable = null;

        mContainer.setOnClickListener(v -> weatherView.onClick());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(Context context, @NonNull Location location, @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled);

        int textColor = ThemeManager
                .getInstance(context)
                .getWeatherThemeDelegate()
                .getHeaderTextColor(context);
        mRefreshTimeText.setTextColor(textColor);

        if (location.getWeather() != null) {
            mRefreshTimeText.setDate(location.getWeather().getBase().getUpdateDate());
        }
    }

    @NotNull
    @Override
    protected Animator getEnterAnimator(List<Animator> pendingAnimatorList) {
        Animator a = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f);
        a.setDuration(300);
        a.setStartDelay(100);
        a.setInterpolator(new FastOutSlowInInterpolator());
        return a;
    }

    @Override
    public void onRecycleView() {
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
    }
}
