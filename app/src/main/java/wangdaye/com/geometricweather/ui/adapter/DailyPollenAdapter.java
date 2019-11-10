package wangdaye.com.geometricweather.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.PollenUnit;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Pollen;
import wangdaye.com.geometricweather.basic.model.weather.Weather;

public class DailyPollenAdapter extends RecyclerView.Adapter<DailyPollenAdapter.ViewHolder> {

    private Weather weather;
    private PollenUnit unit;

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView subtitle;

        AppCompatImageView grassIcon;
        TextView grassTitle;
        TextView grassValue;

        AppCompatImageView ragweedIcon;
        TextView ragweedTitle;
        TextView ragweedValue;

        AppCompatImageView treeIcon;
        TextView treeTitle;
        TextView treeValue;

        AppCompatImageView moldIcon;
        TextView moldTitle;
        TextView moldValue;

        ViewHolder(Context context, ViewGroup parent) {
            super(LayoutInflater.from(context).inflate(R.layout.item_pollen_daily, parent, false));
            title = itemView.findViewById(R.id.item_pollen_daily_title);
            subtitle = itemView.findViewById(R.id.item_pollen_daily_subtitle);
            grassIcon = itemView.findViewById(R.id.item_pollen_daily_grassIcon);
            grassTitle = itemView.findViewById(R.id.item_pollen_daily_grassTitle);
            grassValue = itemView.findViewById(R.id.item_pollen_daily_grassValue);
            ragweedIcon = itemView.findViewById(R.id.item_pollen_daily_ragweedIcon);
            ragweedTitle = itemView.findViewById(R.id.item_pollen_daily_ragweedTitle);
            ragweedValue = itemView.findViewById(R.id.item_pollen_daily_ragweedValue);
            treeIcon = itemView.findViewById(R.id.item_pollen_daily_treeIcon);
            treeTitle = itemView.findViewById(R.id.item_pollen_daily_treeTitle);
            treeValue = itemView.findViewById(R.id.item_pollen_daily_treeValue);
            moldIcon = itemView.findViewById(R.id.item_pollen_daily_moldIcon);
            moldTitle = itemView.findViewById(R.id.item_pollen_daily_moldTitle);
            moldValue = itemView.findViewById(R.id.item_pollen_daily_moldValue);
        }

        @SuppressLint("SetTextI18n")
        void onBindView(Daily daily) {
            Context context = itemView.getContext();
            Pollen pollen = daily.getPollen();

            title.setText(daily.getDate(context.getString(R.string.date_format_widget_long)));
            subtitle.setText(daily.day().getWeatherText() + " / " + daily.night().getWeatherText());

            grassIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getGrassLevel())
            ));
            grassTitle.setText(context.getString(R.string.grass));
            grassValue.setText(unit.getPollenText(pollen.getGrassIndex()) + " - " + pollen.getGrassDescription());

            ragweedIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getRagweedLevel())
            ));
            ragweedTitle.setText(context.getString(R.string.ragweed));
            ragweedValue.setText(unit.getPollenText(pollen.getRagweedIndex()) + " - " + pollen.getRagweedDescription());

            treeIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getTreeLevel())
            ));
            treeTitle.setText(context.getString(R.string.tree));
            treeValue.setText(unit.getPollenText(pollen.getTreeIndex()) + " - " + pollen.getTreeDescription());

            moldIcon.setSupportImageTintList(ColorStateList.valueOf(
                    Pollen.getPollenColor(itemView.getContext(), pollen.getMoldLevel())
            ));
            moldTitle.setText(context.getString(R.string.mold));
            moldValue.setText(unit.getPollenText(pollen.getMoldIndex()) + " - " + pollen.getMoldDescription());
        }
    }

    public DailyPollenAdapter(Weather weather) {
        this.weather = weather;
        this.unit = PollenUnit.PPCM;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(parent.getContext(), parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(weather.getDailyForecast().get(position));
    }

    @Override
    public int getItemCount() {
        return weather.getDailyForecast().size();
    }
}
