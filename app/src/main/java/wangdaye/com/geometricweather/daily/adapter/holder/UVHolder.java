package wangdaye.com.geometricweather.daily.adapter.holder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.models.weather.UV;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.daily.adapter.model.DailyUV;

public class UVHolder extends DailyWeatherAdapter.ViewHolder {

    private final AppCompatImageView mIcon;
    private final TextView mTitle;

    public UVHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_uv, parent, false));
        mIcon = itemView.findViewById(R.id.item_weather_daily_uv_icon);
        mTitle = itemView.findViewById(R.id.item_weather_daily_uv_title);
    }

    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Context context = itemView.getContext();
        UV uv = ((DailyUV) model).getUv();

        ImageViewCompat.setImageTintList(mIcon, ColorStateList.valueOf(uv.getUVColor(context)));
        mTitle.setText(uv.getUVDescription());

        itemView.setContentDescription(context.getString(R.string.uv_index) + ", " + mTitle.getText());
    }
}