package wangdaye.com.geometricweather.main.ui.adapter.daily.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.main.ui.adapter.daily.DailyWeatherAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.daily.model.Header;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

public class HeaderHolder extends DailyWeatherAdapter.ViewHolder {

    private TextView title;
    private TextView subtitle;

    public HeaderHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_daily_header, parent, false));
        title = itemView.findViewById(R.id.item_weather_daily_header_title);
        subtitle = itemView.findViewById(R.id.item_weather_daily_header_subtitle);
    }

    @Override
    public void onBindView(DailyWeatherAdapter.ViewModel model, int position) {
        Header header = (Header) model;

        title.setText(header.getDaily().getDate(itemView.getContext().getString(R.string.date_format_widget_long)));
        title.setTextColor(header.getWeatherColor());

        if (SettingsOptionManager.getInstance(itemView.getContext()).getLanguage().getCode().startsWith("zh")) {
            subtitle.setText(header.getDaily().getLunar());
        } else {
            subtitle.setVisibility(View.GONE);
        }
    }
}
