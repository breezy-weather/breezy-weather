package wangdaye.com.geometricweather.daily.adapter.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.PollenUnit;
import wangdaye.com.geometricweather.basic.model.weather.Pollen;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.daily.adapter.model.DailyPollen;

public class PollenHolder extends DailyWeatherAdapter.ViewHolder {

    private AppCompatImageView grassIcon;
    private TextView grassTitle;
    private TextView grassValue;

    private AppCompatImageView ragweedIcon;
    private TextView ragweedTitle;
    private TextView ragweedValue;

    private AppCompatImageView treeIcon;
    private TextView treeTitle;
    private TextView treeValue;

    private AppCompatImageView moldIcon;
    private TextView moldTitle;
    private TextView moldValue;

    private PollenUnit unit;

    public PollenHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_pollen, parent, false));
        grassIcon = itemView.findViewById(R.id.item_weather_daily_pollen_grassIcon);
        grassTitle = itemView.findViewById(R.id.item_weather_daily_pollen_grassTitle);
        grassValue = itemView.findViewById(R.id.item_weather_daily_pollen_grassValue);
        ragweedIcon = itemView.findViewById(R.id.item_weather_daily_pollen_ragweedIcon);
        ragweedTitle = itemView.findViewById(R.id.item_weather_daily_pollen_ragweedTitle);
        ragweedValue = itemView.findViewById(R.id.item_weather_daily_pollen_ragweedValue);
        treeIcon = itemView.findViewById(R.id.item_weather_daily_pollen_treeIcon);
        treeTitle = itemView.findViewById(R.id.item_weather_daily_pollen_treeTitle);
        treeValue = itemView.findViewById(R.id.item_weather_daily_pollen_treeValue);
        moldIcon = itemView.findViewById(R.id.item_weather_daily_pollen_moldIcon);
        moldTitle = itemView.findViewById(R.id.item_weather_daily_pollen_moldTitle);
        moldValue = itemView.findViewById(R.id.item_weather_daily_pollen_moldValue);

        unit = PollenUnit.PPCM;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Context context = itemView.getContext();
        Pollen pollen = ((DailyPollen) model).getPollen();

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