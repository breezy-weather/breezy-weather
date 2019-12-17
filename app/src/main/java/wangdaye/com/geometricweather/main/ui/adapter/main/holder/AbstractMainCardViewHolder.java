package wangdaye.com.geometricweather.main.ui.adapter.main.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.main.FirstCardHeaderController;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public abstract class AbstractMainCardViewHolder extends AbstractMainViewHolder {

    private FirstCardHeaderController firstCardHeaderController;

    @SuppressLint("ObjectAnimatorBinding")
    public AbstractMainCardViewHolder(@NonNull View view) {
        super(view);
    }

    @CallSuper
    public void onBindView(GeoActivity activity, @NonNull Location location, WeatherView weatherView,
                           @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                           @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                           @Px float cardRadius, @Px float cardElevation,
                           boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, picker, itemAnimationEnabled);

        CardView card = (CardView) itemView;

        card.setRadius(cardRadius);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(cardElevation);
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
        params.setMargins((int) cardMarginsHorizontal, 0, (int) cardMarginsHorizontal, (int) cardMarginsVertical);
        card.setLayoutParams(params);

        if (firstCard) {
            firstCardHeaderController = new FirstCardHeaderController(activity, location, picker);
            firstCardHeaderController.bind((LinearLayout) card.getChildAt(0));
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated
    @Override
    public void onBindView(Context context, @NonNull Location location,
                           @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                           boolean itemAnimationEnabled) {
        // do nothing.
    }

    @Override
    public void onRecycleView() {
        super.onRecycleView();
        if (firstCardHeaderController != null) {
            firstCardHeaderController.unbind();
            firstCardHeaderController = null;
        }
    }
}
