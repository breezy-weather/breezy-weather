package wangdaye.com.geometricweather.main.adapter.main.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.main.adapter.main.FirstCardHeaderController;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;

public abstract class AbstractMainCardViewHolder extends AbstractMainViewHolder {

    private FirstCardHeaderController firstCardHeaderController;

    @SuppressLint("ObjectAnimatorBinding")
    public AbstractMainCardViewHolder(@NonNull View view) {
        super(view);
    }

    @CallSuper
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider, @NonNull MainThemePicker picker,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, picker, listAnimationEnabled, itemAnimationEnabled);

        CardView card = (CardView) itemView;

        card.setRadius(picker.getCardRadius(activity));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(picker.getCardElevation(activity));
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
        params.setMargins(
                picker.getCardMarginsHorizontal(activity),
                0,
                picker.getCardMarginsHorizontal(activity),
                picker.getCardMarginsVertical(activity)
        );
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
                           @NonNull ResourceProvider provider, @NonNull MainThemePicker picker,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        onBindView((GeoActivity) context, location, provider, picker,
                listAnimationEnabled, itemAnimationEnabled, false);
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
