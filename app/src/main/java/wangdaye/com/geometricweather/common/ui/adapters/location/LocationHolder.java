package wangdaye.com.geometricweather.common.ui.adapters.location;

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
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.databinding.ItemLocationBinding;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;

public class LocationHolder extends RecyclerView.ViewHolder {

    private final ItemLocationBinding mBinding;
    private final ThemeManager mThemeManager;
    private final LocationAdapter.OnLocationItemClickListener mClickListener;
    private @Nullable final LocationAdapter.OnLocationItemDragListener mDragListener;

    protected LocationHolder(ItemLocationBinding binding,
                             ThemeManager themeManager,
                             @NonNull LocationAdapter.OnLocationItemClickListener clickListener,
                             @Nullable LocationAdapter.OnLocationItemDragListener dragListener) {
        super(binding.getRoot());
        mBinding = binding;
        mThemeManager = themeManager;
        mClickListener = clickListener;
        mDragListener = dragListener;
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    protected void onBindView(Context context, LocationModel model, ResourceProvider resourceProvider) {
        StringBuilder talkBackBuilder = new StringBuilder(model.subtitle);
        if (model.currentPosition) {
            talkBackBuilder.append(", ").append(context.getString(R.string.current_location));
        }
        talkBackBuilder.append(", ").append(
                context.getString(R.string.content_desc_powered_by).replace("$", model.weatherSource.getSourceVoice(context))
        );

        mBinding.container.swipe(0);
        mBinding.container.setIconResStart(R.drawable.ic_delete);
        if (model.currentPosition) {
            mBinding.container.setIconResEnd(R.drawable.ic_settings);
        } else {
            mBinding.container.setIconResEnd(
                    model.residentPosition ? R.drawable.ic_tag_off : R.drawable.ic_tag_plus);
        }
        mBinding.container.setBackgroundColorStart(ContextCompat.getColor(context, R.color.striking_red));
        mBinding.container.setBackgroundColorEnd(
                ContextCompat.getColor(
                        context,
                        model.location.isCurrentPosition()
                                ? R.color.colorPrimary
                                : R.color.colorTextAlert
                )
        );

        mBinding.item.setBackgroundColor(
                model.selected ? mThemeManager.getLineColor(context) : mThemeManager.getRootColor(context)
        );

        ImageViewCompat.setImageTintList(
                mBinding.sortButton,
                ColorStateList.valueOf(mThemeManager.getTextContentColor(context))
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

        mBinding.title.setTextColor(mThemeManager.getTextTitleColor(context));
        mBinding.title.setText(model.title);

        mBinding.alerts.setTextColor(mThemeManager.getTextSubtitleColor(context));
        if (!TextUtils.isEmpty(model.alerts)) {
            mBinding.alerts.setVisibility(View.VISIBLE);
            mBinding.alerts.setText(model.alerts);

            talkBackBuilder.append(", ").append(model.alerts);
        } else {
            mBinding.alerts.setVisibility(View.GONE);
        }

        mBinding.subtitle.setTextColor(mThemeManager.getTextContentColor(context));
        mBinding.subtitle.setText(model.subtitle);

        // binding.geoPosition.setText(model.latitude + ", " + model.longitude
        //         + " - " + model.timeZone.getDisplayName(false, TimeZone.SHORT));

        // source.
        mBinding.source.setText("Powered by " + model.weatherSource.getSourceUrl());
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
