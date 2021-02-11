package wangdaye.com.geometricweather.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.models.options.unit.PollenUnit;
import wangdaye.com.geometricweather.basic.models.weather.Daily;
import wangdaye.com.geometricweather.basic.models.weather.Pollen;
import wangdaye.com.geometricweather.basic.models.weather.Weather;
import wangdaye.com.geometricweather.databinding.ItemPollenDailyBinding;

public class DailyPollenAdapter extends RecyclerView.Adapter<DailyPollenAdapter.ViewHolder> {

    private final Weather mWeather;
    private final PollenUnit mPollenUnit;

    public class ViewHolder extends RecyclerView.ViewHolder {

        ItemPollenDailyBinding binding;

        ViewHolder(ItemPollenDailyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint({"SetTextI18n", "RestrictedApi"})
        void onBindView(Daily daily) {
            Context context = itemView.getContext();
            Pollen pollen = daily.getPollen();

            binding.title.setText(daily.getDate(context.getString(R.string.date_format_widget_long)));
            binding.subtitle.setText(daily.day().getWeatherText() + " / " + daily.night().getWeatherText());

            binding.grassIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getGrassLevel())
            ));
            binding.grassTitle.setText(context.getString(R.string.grass));
            binding.grassValue.setText(mPollenUnit.getPollenText(context, pollen.getGrassIndex())
                    + " - " + pollen.getGrassDescription());

            binding.ragweedIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getRagweedLevel())
            ));
            binding.ragweedTitle.setText(context.getString(R.string.ragweed));
            binding.ragweedValue.setText(mPollenUnit.getPollenText(context, pollen.getRagweedIndex())
                    + " - " + pollen.getRagweedDescription());

            binding.treeIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getTreeLevel())
            ));
            binding.treeTitle.setText(context.getString(R.string.tree));
            binding.treeValue.setText(mPollenUnit.getPollenText(context, pollen.getTreeIndex())
                    + " - " + pollen.getTreeDescription());

            binding.moldIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getMoldLevel())
            ));
            binding.moldTitle.setText(context.getString(R.string.mold));
            binding.moldValue.setText(mPollenUnit.getPollenText(context, pollen.getMoldIndex())
                    + " - " + pollen.getMoldDescription());

            itemView.setContentDescription(binding.title.getText()
                    //
                    + ", " + context.getString(R.string.grass)
                    + " : " + mPollenUnit.getPollenVoice(context, pollen.getGrassIndex())
                    + " - " + pollen.getGrassDescription()
                    //
                    + ", " + context.getString(R.string.ragweed)
                    + " : " + mPollenUnit.getPollenVoice(context, pollen.getRagweedIndex())
                    + " - " + pollen.getRagweedDescription()
                    //
                    + ", " + context.getString(R.string.tree)
                    + " : " + mPollenUnit.getPollenVoice(context, pollen.getTreeIndex())
                    + " - " + pollen.getTreeDescription()
                    //
                    + ", " + context.getString(R.string.mold)
                    + " : " + mPollenUnit.getPollenVoice(context, pollen.getMoldIndex())
                    + " - " + pollen.getMoldDescription()
            );
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
        holder.onBindView(mWeather.getDailyForecast().get(position));
    }

    @Override
    public int getItemCount() {
        return mWeather.getDailyForecast().size();
    }
}
