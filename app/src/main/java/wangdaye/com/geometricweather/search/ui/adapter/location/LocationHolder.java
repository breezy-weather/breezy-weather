package wangdaye.com.geometricweather.search.ui.adapter.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.databinding.ItemLocationBinding;

public class LocationHolder extends RecyclerView.ViewHolder {

    private final ItemLocationBinding mBinding;
    private final LocationAdapter.OnLocationItemClickListener mClickListener;

    protected LocationHolder(ItemLocationBinding binding,
                             @NonNull LocationAdapter.OnLocationItemClickListener clickListener) {
        super(binding.getRoot());
        mBinding = binding;
        mClickListener = clickListener;
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    protected void onBindView(Context context, LocationModel model) {
        mBinding.container.swipe(0);

        mBinding.sortButton.setVisibility(View.GONE);
        mBinding.content.setPaddingRelative(
                context.getResources().getDimensionPixelSize(R.dimen.normal_margin), 0, 0, 0);

        mBinding.residentIcon.setVisibility(View.GONE);
        mBinding.weatherIcon.setVisibility(View.GONE);

        mBinding.title.setText(model.title);
        mBinding.alerts.setVisibility(View.GONE);
        mBinding.subtitle.setText(model.subtitle);

        // source.
        mBinding.source.setText("Powered by " + model.weatherSource.getSourceUrl());
        mBinding.source.setTextColor(model.weatherSource.getSourceColor());

        mBinding.container.setOnClickListener(v ->
                mClickListener.onClick(v, model.location.getFormattedId())
        );


        String talkBackBuilder = model.subtitle + ", " +
                context.getString(R.string.content_desc_powered_by)
                        .replace("$", model.weatherSource.getVoice(context));
        itemView.setContentDescription(talkBackBuilder);
    }
}
