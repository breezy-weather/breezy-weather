package wangdaye.com.geometricweather.main.adapter.main.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Px;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;

public abstract class AbstractMainTrendCardViewHolder extends AbstractMainCardViewHolder {

    protected @Px float cardWidth;

    public AbstractMainTrendCardViewHolder(@NonNull View view) {
        super(view);
    }

    @CallSuper
    public void onBindView(GeoActivity activity, @NonNull Location location, @Px float cardWidth,
                           @NonNull ResourceProvider provider, @NonNull MainThemePicker picker,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, picker, listAnimationEnabled, itemAnimationEnabled, firstCard);
        this.cardWidth = cardWidth;
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated
    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider, @NonNull MainThemePicker picker,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        onBindView(activity, location, 0, provider, picker, listAnimationEnabled, itemAnimationEnabled, firstCard);
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated
    @Override
    public void onBindView(Context context, @NonNull Location location,
                           @NonNull ResourceProvider provider, @NonNull MainThemePicker picker,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        onBindView((GeoActivity) context, location, provider, picker,
                listAnimationEnabled, itemAnimationEnabled, false);
    }
}
