package wangdaye.com.geometricweather.common.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options.unit.PollenUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.Pollen;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.databinding.ItemPollenDailyBinding;

public class DailyPollenAdapter extends RecyclerView.Adapter<DailyPollenAdapter.ViewHolder> {

    private final Weather mWeather;
    private final PollenUnit mPollenUnit;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ItemPollenDailyBinding binding;

        ViewHolder(ItemPollenDailyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint({"SetTextI18n", "RestrictedApi"})
        void onBindView(Daily daily, PollenUnit unit) {
            Context context = itemView.getContext();
            Pollen pollen = daily.getPollen();

            binding.title.setText(daily.getDate(context.getString(R.string.date_format_widget_long)));

            binding.grassIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getGrassLevel())
            ));
            binding.grassTitle.setText(context.getString(R.string.grass));
            binding.grassValue.setText(unit.getPollenText(context, pollen.getGrassIndex())
                    + " - " + pollen.getGrassDescription());

            binding.ragweedIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getRagweedLevel())
            ));
            binding.ragweedTitle.setText(context.getString(R.string.ragweed));
            binding.ragweedValue.setText(unit.getPollenText(context, pollen.getRagweedIndex())
                    + " - " + pollen.getRagweedDescription());

            binding.treeIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getTreeLevel())
            ));
            binding.treeTitle.setText(context.getString(R.string.tree));
            binding.treeValue.setText(unit.getPollenText(context, pollen.getTreeIndex())
                    + " - " + pollen.getTreeDescription());

            binding.moldIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getMoldLevel())
            ));
            binding.moldTitle.setText(context.getString(R.string.mold));
            binding.moldValue.setText(unit.getPollenText(context, pollen.getMoldIndex())
                    + " - " + pollen.getMoldDescription());

            itemView.setContentDescription(binding.title.getText()
                    //
                    + ", " + context.getString(R.string.grass)
                    + " : " + unit.getPollenVoice(context, pollen.getGrassIndex())
                    + " - " + pollen.getGrassDescription()
                    //
                    + ", " + context.getString(R.string.ragweed)
                    + " : " + unit.getPollenVoice(context, pollen.getRagweedIndex())
                    + " - " + pollen.getRagweedDescription()
                    //
                    + ", " + context.getString(R.string.tree)
                    + " : " + unit.getPollenVoice(context, pollen.getTreeIndex())
                    + " - " + pollen.getTreeDescription()
                    //
                    + ", " + context.getString(R.string.mold)
                    + " : " + unit.getPollenVoice(context, pollen.getMoldIndex())
                    + " - " + pollen.getMoldDescription()
            );
            itemView.setOnClickListener(v -> {
            });
        }
    }

    public DailyPollenAdapter(Weather weather) {
        this.mWeather = weather;
        this.mPollenUnit = PollenUnit.PPCM;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ItemPollenDailyBinding.inflate(
                        LayoutInflater.from(parent.getContext())
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(mWeather.getDailyForecast().get(position), mPollenUnit);
    }

    @Override
    public int getItemCount() {
        return mWeather.getDailyForecast().size();
    }
}
