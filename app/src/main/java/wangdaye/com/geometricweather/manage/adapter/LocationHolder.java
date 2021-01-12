package wangdaye.com.geometricweather.manage.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.databinding.ItemLocationBinding;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

public class LocationHolder extends RecyclerView.ViewHolder {

    private final ItemLocationBinding binding;
    private final ThemeManager themeManager;
    private final LocationAdapter.OnLocationItemClickListener clickListener;
    private @Nullable final LocationAdapter.OnLocationItemDragListener dragListener;

    protected LocationHolder(ItemLocationBinding binding,
                             @NonNull LocationAdapter.OnLocationItemClickListener clickListener,
                             @Nullable LocationAdapter.OnLocationItemDragListener dragListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.themeManager = ThemeManager.getInstance(binding.getRoot().getContext());
        this.clickListener = clickListener;
        this.dragListener = dragListener;
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    protected void onBindView(Context context, LocationModel model, ResourceProvider resourceProvider) {
        binding.container.setIconResStart(R.drawable.ic_delete);
        if (model.currentPosition) {
            binding.container.setIconResEnd(R.drawable.ic_settings);
        } else {
            binding.container.setIconResEnd(
                    model.residentPosition ? R.drawable.ic_tag_off : R.drawable.ic_tag_plus);
        }
        binding.container.setBackgroundColorStart(ContextCompat.getColor(context, R.color.striking_red));
        binding.container.setBackgroundColorEnd(
                ContextCompat.getColor(
                        context,
                        model.location.isCurrentPosition()
                                ? R.color.colorPrimary
                                : R.color.colorTextAlert
                )
        );

        binding.item.setBackgroundColor(
                model.selected ? themeManager.getLineColor(context) : themeManager.getRootColor(context)
        );

        ImageViewCompat.setImageTintList(
                binding.sortButton,
                ColorStateList.valueOf(themeManager.getTextContentColor(context))
        );
        if (dragListener == null) {
            binding.sortButton.setVisibility(View.GONE);
            binding.content.setPaddingRelative(
                    context.getResources().getDimensionPixelSize(R.dimen.normal_margin), 0, 0, 0);
        } else {
            binding.sortButton.setVisibility(View.VISIBLE);
            binding.content.setPaddingRelative(0, 0, 0, 0);
        }

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

        // binding.geoPosition.setText(model.latitude + ", " + model.longitude
        //         + " - " + model.timeZone.getDisplayName(false, TimeZone.SHORT));

        // source.
        binding.source.setText("Powered by " + model.weatherSource.getSourceUrl());
        binding.source.setTextColor(model.weatherSource.getSourceColor());

        binding.container.setOnClickListener(v -> clickListener.onClick(v, model.location.getFormattedId()));
        binding.sortButton.setOnTouchListener(dragListener == null ? null : ((View v, MotionEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dragListener.onDrag(this);
            }
            return false;
        }));
    }
}
