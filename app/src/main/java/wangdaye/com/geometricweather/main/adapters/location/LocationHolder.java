package wangdaye.com.geometricweather.main.adapters.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.databinding.ItemLocationCardBinding;
import wangdaye.com.geometricweather.main.utils.MainThemeColorProvider;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;

public class LocationHolder extends RecyclerView.ViewHolder {

    private final ItemLocationCardBinding mBinding;
    private final LocationAdapter.OnLocationItemClickListener mClickListener;
    private @Nullable final LocationAdapter.OnLocationItemDragListener mDragListener;

    protected LocationHolder(ItemLocationCardBinding binding,
                             @NonNull LocationAdapter.OnLocationItemClickListener clickListener,
                             @Nullable LocationAdapter.OnLocationItemDragListener dragListener) {
        super(binding.getRoot());
        mBinding = binding;
        mClickListener = clickListener;
        mDragListener = dragListener;
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    protected void onBindView(Context context, LocationModel model, ResourceProvider resourceProvider) {
        boolean lightTheme = !DisplayUtils.isDarkMode(context);

        int elevatedSurfaceColor = DisplayUtils.getWidgetSurfaceColor(
                DisplayUtils.DEFAULT_CARD_LIST_ITEM_ELEVATION_DP,
                MainThemeColorProvider.getColor(lightTheme, androidx.appcompat.R.attr.colorPrimary),
                MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorSurface)
        );

        if (model.selected) {
            mBinding.getRoot().setStrokeWidth(
                    (int) DisplayUtils.dpToPx(context, 4)
            );
            mBinding.getRoot().setStrokeColor(
                    elevatedSurfaceColor
            );
        } else {
            mBinding.getRoot().setStrokeWidth(0);
        }

        StringBuilder talkBackBuilder = new StringBuilder(model.subtitle);
        if (model.currentPosition) {
            talkBackBuilder.append(", ").append(context.getString(R.string.current_location));
        }
        talkBackBuilder.append(", ").append(
                context.getString(R.string.content_desc_powered_by).replace("$", model.weatherSource.getVoice(context))
        );

        mBinding.container.swipe(0);
        mBinding.container.setIconResStart(R.drawable.ic_delete);
        if (model.currentPosition) {
            mBinding.container.setIconResEnd(R.drawable.ic_settings);
        } else {
            mBinding.container.setIconResEnd(
                    model.residentPosition ? R.drawable.ic_tag_off : R.drawable.ic_tag_plus);
        }

        mBinding.container.setBackgroundColorStart(
                MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorErrorContainer)
        );
        mBinding.container.setBackgroundColorEnd(
                model.location.isCurrentPosition()
                        ? MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorTertiaryContainer)
                        : MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorSecondaryContainer)
        );

        mBinding.container.setTintColorStart(
                MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorOnErrorContainer)
        );
        mBinding.container.setTintColorEnd(
                model.location.isCurrentPosition()
                        ? MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorOnTertiaryContainer)
                        : MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorOnSecondaryContainer)
        );

        mBinding.item.setBackgroundColor(
                model.selected ? DisplayUtils.blendColor(
                        ColorUtils.setAlphaComponent(elevatedSurfaceColor, (int) (255 * 0.5)),
                        MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorSurfaceVariant)
                ) : elevatedSurfaceColor
        );
        ImageViewCompat.setImageTintList(
                mBinding.sortButton,
                ColorStateList.valueOf(
                        MainThemeColorProvider.getColor(lightTheme, androidx.appcompat.R.attr.colorPrimary)
                )
        );
        if (mDragListener == null) {
            mBinding.sortButton.setVisibility(View.GONE);
            mBinding.content.setPaddingRelative(
                    context.getResources().getDimensionPixelSize(R.dimen.normal_margin), 0, 0, 0);
        } else {
            mBinding.sortButton.setVisibility(View.VISIBLE);
            mBinding.content.setPaddingRelative(0, 0, 0, 0);
        }

        mBinding.residentIcon.setVisibility(model.residentPosition ? View.VISIBLE : View.GONE);

        if (model.weatherCode != null) {
            mBinding.weatherIcon.setVisibility(View.VISIBLE);
            mBinding.weatherIcon.setImageDrawable(
                    resourceProvider.getWeatherIcon(
                            model.weatherCode,
                            model.location.isDaylight()
                    )
            );
        } else {
            mBinding.weatherIcon.setVisibility(View.GONE);
        }

        mBinding.title1.setTextColor(
                model.selected
                        ? MainThemeColorProvider.getColor(lightTheme, com.google.android.material.R.attr.colorOnPrimaryContainer)
                        : MainThemeColorProvider.getColor(lightTheme, R.attr.colorTitleText)
        );
        mBinding.title1.setText(model.title1);

        if (TextUtils.isEmpty(model.title2)) {
            mBinding.title2.setVisibility(View.GONE);
        } else {
            mBinding.title2.setVisibility(View.VISIBLE);
            mBinding.title2.setTextColor(
                    MainThemeColorProvider.getColor(lightTheme, R.attr.colorBodyText)
            );
            mBinding.title2.setText(model.title2);
        }

        mBinding.subtitle.setTextColor(
                MainThemeColorProvider.getColor(lightTheme, R.attr.colorCaptionText)
        );
        mBinding.subtitle.setText(model.subtitle);

        // source.
        mBinding.source.setText("Weather data by " + model.weatherSource.getSourceUrl());
        mBinding.source.setTextColor(model.weatherSource.getSourceColor());

        mBinding.container.setOnClickListener(v -> mClickListener.onClick(v, model.location.getFormattedId()));
        mBinding.sortButton.setOnTouchListener(mDragListener == null ? null : ((View v, MotionEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mDragListener.onDrag(this);
            }
            return false;
        }));

        if (mDragListener != null) {
            talkBackBuilder.append(", ").append(context.getString(
                    DisplayUtils.isRtl(context)
                            ? R.string.content_des_swipe_left_to_delete
                            : R.string.content_des_swipe_right_to_delete
            ));
        }

        itemView.setContentDescription(talkBackBuilder.toString());
    }
}
