package wangdaye.com.geometricweather.main.adapter;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.CloudCoverUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.RelativeHumidityUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Details adapter.
 * */

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder> {

    private List<Index> indexList;
    private MainThemePicker colorPicker;

    private class Index {
        @DrawableRes int iconId;
        String title;
        String content;

        Index(@DrawableRes int iconId, String title, String content) {
            this.iconId = iconId;
            this.title = title;
            this.content = content;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private AppCompatImageView icon;
        private TextView title;
        private TextView content;

        ViewHolder(View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.item_details_icon);
            this.title = itemView.findViewById(R.id.item_details_title);
            this.content = itemView.findViewById(R.id.item_details_content);
        }

        void onBindView(Index index) {
            Context context = itemView.getContext();

            icon.setImageResource(index.iconId);
            icon.setSupportImageTintList(
                    ColorStateList.valueOf(colorPicker.getTextContentColor(context))
            );

            title.setText(index.title);
            title.setTextColor(colorPicker.getTextContentColor(context));

            content.setText(index.content);
            content.setTextColor(colorPicker.getTextSubtitleColor(context));
        }
    }

    public DetailsAdapter(Context context, @NonNull Weather weather, MainThemePicker colorPicker) {
        this.indexList = new ArrayList<>();
        SettingsOptionManager settings = SettingsOptionManager.getInstance(context);

        SpeedUnit speedUnit = settings.getSpeedUnit();
        indexList.add(
                new Index(
                        R.drawable.ic_wind,
                        context.getString(R.string.live)
                                + " : "
                                + weather.getCurrent().getWind().getWindDescription(speedUnit),
                        context.getString(R.string.daytime)
                                + " : "
                                + weather.getDailyForecast().get(0).day().getWind().getWindDescription(speedUnit)
                                + "\n"
                                + context.getString(R.string.nighttime)
                                + " : "
                                + weather.getDailyForecast().get(0).night().getWind().getWindDescription(speedUnit)
                )
        );

        if (weather.getCurrent().getRelativeHumidity() != null) {
            indexList.add(
                    new Index(
                            R.drawable.ic_water_percent,
                            context.getString(R.string.humidity),
                            RelativeHumidityUnit.PERCENT.getRelativeHumidityText(
                                    weather.getCurrent().getRelativeHumidity()
                            )
                    )
            );
        }

        if (weather.getCurrent().getUV().isValid()) {
            indexList.add(
                    new Index(
                            R.drawable.ic_uv,
                            context.getString(R.string.uv_index),
                            weather.getCurrent().getUV().getUVDescription()
                    )
            );
        }

        if (weather.getCurrent().getPressure() != null) {
            indexList.add(
                    new Index(
                            R.drawable.ic_gauge,
                            context.getString(R.string.pressure),
                            settings.getPressureUnit().getPressureText(weather.getCurrent().getPressure())
                    )
            );
        }

        if (weather.getCurrent().getVisibility() != null) {
            indexList.add(
                    new Index(
                            R.drawable.ic_eye,
                            context.getString(R.string.visibility),
                            settings.getDistanceUnit().getDistanceText(weather.getCurrent().getVisibility())
                    )
            );
        }

        if (weather.getCurrent().getDewPoint() != null) {
            indexList.add(
                    new Index(
                            R.drawable.ic_water,
                            context.getString(R.string.dew_point),
                            settings.getTemperatureUnit().getTemperatureText(weather.getCurrent().getDewPoint())
                    )
            );
        }

        if (weather.getCurrent().getCloudCover() != null) {
            indexList.add(
                    new Index(
                            R.drawable.ic_cloud,
                            context.getString(R.string.cloud_cover),
                            CloudCoverUnit.PERCENT.getCloudCoverText(weather.getCurrent().getCloudCover())
                    )
            );
        }

        if (weather.getCurrent().getCeiling() != null) {
            indexList.add(
                    new Index(
                            R.drawable.ic_top,
                            context.getString(R.string.ceiling),
                            settings.getDistanceUnit().getDistanceText(weather.getCurrent().getCeiling())
                    )
            );
        }

        this.colorPicker = colorPicker;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(indexList.get(position));
    }

    @Override
    public int getItemCount() {
        return indexList.size();
    }
}