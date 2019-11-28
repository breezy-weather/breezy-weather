package wangdaye.com.geometricweather.main.ui.adapter.main.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;

import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;

public abstract class AbstractMainCardViewHolder extends AbstractMainViewHolder {

    protected @Nullable LinearLayout headerContainer;

    @SuppressLint("ObjectAnimatorBinding")
    public AbstractMainCardViewHolder(Context context, @NonNull View view,
                                      @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                                      @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                                      @Px float cardRadius, @Px float cardElevation,
                                      boolean itemAnimationEnabled) {
        super(context, view, provider, picker, itemAnimationEnabled);
        if (view instanceof CardView) {
            CardView card = (CardView) view;

            card.setRadius(cardRadius);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                card.setElevation(cardElevation);
            }

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
            params.setMargins((int) cardMarginsHorizontal, 0, (int) cardMarginsHorizontal, (int) cardMarginsVertical);
            card.setLayoutParams(params);

            View child = card.getChildAt(0);
            if (child instanceof LinearLayout) {
                headerContainer = (LinearLayout) child;
            }
        }
    }

    public @Nullable LinearLayout getHeaderContainer() {
        return headerContainer;
    }
}
