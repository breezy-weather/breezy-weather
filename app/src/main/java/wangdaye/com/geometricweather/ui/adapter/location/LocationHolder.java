package wangdaye.com.geometricweather.ui.adapter.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.databinding.ItemLocationBinding;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

public class LocationHolder extends RecyclerView.ViewHolder {

    private ItemLocationBinding binding;

    protected LocationModel model;
    private ThemeManager themeManager;
    private int direction;
    private @ColorInt int swipeEndColor;

    protected LocationHolder(ItemLocationBinding binding,
                             LocationAdapter.OnLocationItemClickListener listener) {
        super(binding.getRoot());
        this.binding = binding;
        this.themeManager = ThemeManager.getInstance(binding.getRoot().getContext());

        binding.container.setOnClickListener(v -> listener.onClick(v, model.location.getFormattedId()));
    }

    @SuppressLint("SetTextI18n")
    protected void onBindView(Context context, LocationModel model, ResourceProvider resourceProvider) {
        this.model = model;
        direction = 0;
        swipeEndColor = ContextCompat.getColor(context,
                model.location.isCurrentPosition() ? R.color.colorPrimary : R.color.colorTextAlert);

        if (model.currentPosition) {
            binding.swipeIconEnd.setImageResource(R.drawable.ic_settings);
        } else {
            binding.swipeIconEnd.setImageResource(
                    model.residentPosition ? R.drawable.ic_tag_off : R.drawable.ic_tag_plus);
        }

        binding.item.setBackgroundColor(themeManager.getRootColor(context));

        binding.residentIcon.setVisibility(model.residentPosition ? View.VISIBLE : View.GONE);

        if (model.weatherCode != null) {
            binding.weatherIcon.setVisibility(View.VISIBLE);
            binding.weatherIcon.setImageDrawable(
                    resourceProvider.getWeatherIcon(
                            model.weatherCode,
                            TimeManager.isDaylight(model.location)
                    )
            );
        } else {
            binding.weatherIcon.setVisibility(View.GONE);
        }

        binding.title.setTextColor(themeManager.getTextTitleColor(context));
        binding.title.setText(model.title);

        binding.alerts.setTextColor(themeManager.getTextSubtitleColor(context));
        if (!TextUtils.isEmpty(model.alerts)) {
            binding.alerts.setVisibility(View.VISIBLE);
            binding.alerts.setText(model.alerts);
        } else {
            binding.alerts.setVisibility(View.GONE);
        }

        binding.subtitle.setTextColor(themeManager.getTextContentColor(context));
        binding.subtitle.setText(model.subtitle);

        // source.
        binding.source.setText("Powered by " + model.weatherSource.getSourceUrl());
        binding.source.setTextColor(model.weatherSource.getSourceColor());

        drawSwipe(context, 0);
        drawDrag(context, false);
    }

    public void drawDrag(Context context, boolean elevate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.container.setElevation(DisplayUtils.dpToPx(context, elevate ? 10 : 0));
        }
    }

    public void drawSwipe(Context context, float dX) {
        if (itemView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (dX < 0 && direction >= 0) {
                direction = -1;
                binding.container.setBackgroundColor(ContextCompat.getColor(context, R.color.striking_red));
            } else if (dX > 0 && direction <= 0) {
                direction = 1;
                binding.container.setBackgroundColor(swipeEndColor);
            }

            binding.container.setTranslationX(0);
            binding.item.setTranslationX(dX);
            binding.swipeIconStart.setTranslationX(
                    (float) Math.max(0.5 * (dX + binding.swipeIconEnd.getMeasuredWidth()), 0));
            binding.swipeIconEnd.setTranslationX(
                    (float) Math.min(0.5 * (dX - binding.swipeIconStart.getMeasuredWidth()), 0));
        } else {
            if (dX < 0 && direction >= 0) {
                direction = -1;
                binding.container.setBackgroundColor(swipeEndColor);
            } else if (dX > 0 && direction <= 0) {
                direction = 1;
                binding.container.setBackgroundColor(ContextCompat.getColor(context, R.color.striking_red));
            }

            binding.container.setTranslationX(0);
            binding.item.setTranslationX(dX);
            binding.swipeIconStart.setTranslationX(
                    (float) Math.min(0.5 * (dX - binding.swipeIconStart.getMeasuredWidth()), 0));
            binding.swipeIconEnd.setTranslationX(
                    (float) Math.max(0.5 * (dX + binding.swipeIconEnd.getMeasuredWidth()), 0));
        }
    }
}
