package wangdaye.com.geometricweather.main.ui.adapter.main.holder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.daily.DailyAirQualityAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.daily.DailyTemperatureAdapter;
import wangdaye.com.geometricweather.main.ui.adapter.daily.DailyWindAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class DailyViewHolder extends AbstractMainViewHolder {

    private CardView card;

    private TextView title;
    private TextView subtitle;
    private TrendRecyclerView trendRecyclerView;
    
    @NonNull private WeatherView weatherView;
    @Px private float cardMarginsVertical;
    @Px private float cardMarginsHorizontal;

    private static final int VIEW_TYPE = 0;

    public DailyViewHolder(@NonNull Activity activity, ViewGroup parent, @NonNull WeatherView weatherView,
                           @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                           @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                           @Px float cardRadius, @Px float cardElevation) {
        super(activity,
                LayoutInflater.from(activity).inflate(R.layout.container_main_daily_trend_card, parent, false),
                provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation, false);

        this.card = itemView.findViewById(R.id.container_main_daily_trend_card);
        this.title = itemView.findViewById(R.id.container_main_daily_trend_card_title);
        this.subtitle = itemView.findViewById(R.id.container_main_daily_trend_card_subtitle);
        this.trendRecyclerView = itemView.findViewById(R.id.container_main_daily_trend_card_trendRecyclerView);
        
        this.weatherView = weatherView;
        this.cardMarginsVertical = cardMarginsVertical;
        this.cardMarginsHorizontal = cardMarginsHorizontal;
    }

    @SuppressLint({"RestrictedApi", "SetTextI18n"})
    @Override
    public void onBindView(@NonNull Location location) {
        Weather weather = location.getWeather();
        assert weather != null;

        card.setCardBackgroundColor(picker.getRootColor(context));

        title.setTextColor(weatherView.getThemeColors(picker.isLightTheme())[0]);

        if (TextUtils.isEmpty(weather.getCurrent().getDailyForecast())) {
            subtitle.setVisibility(View.GONE);
        } else {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(weather.getCurrent().getDailyForecast());
        }

        trendRecyclerView.setHasFixedSize(true);
        trendRecyclerView.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        if (VIEW_TYPE == 0) {
            trendRecyclerView.setAdapter(
                    new DailyTemperatureAdapter(
                            (GeoActivity) context, trendRecyclerView,
                            cardMarginsVertical, cardMarginsHorizontal,
                            DisplayUtils.isTabletDevice(context) ? 7 : 5,
                            context.getResources().getDimensionPixelSize(R.dimen.daily_trend_item_height),
                            weather,
                            weatherView.getThemeColors(picker.isLightTheme()),
                            provider,
                            picker,
                            SettingsOptionManager.getInstance(context).getTemperatureUnit()
                    )
            );
        } else if (VIEW_TYPE == 1) {
            trendRecyclerView.setAdapter(
                    new DailyWindAdapter(
                            (GeoActivity) context, trendRecyclerView,
                            cardMarginsVertical, cardMarginsHorizontal,
                            DisplayUtils.isTabletDevice(context) ? 7 : 5,
                            context.getResources().getDimensionPixelSize(R.dimen.daily_trend_item_height),
                            weather,
                            weatherView.getThemeColors(picker.isLightTheme()),
                            picker,
                            SettingsOptionManager.getInstance(context).getSpeedUnit()
                    )
            );
        } else if (VIEW_TYPE == 2) {
            trendRecyclerView.setAdapter(
                    new DailyAirQualityAdapter(
                            (GeoActivity) context, trendRecyclerView,
                            cardMarginsVertical, cardMarginsHorizontal,
                            DisplayUtils.isTabletDevice(context) ? 7 : 5,
                            context.getResources().getDimensionPixelSize(R.dimen.daily_trend_item_height),
                            weather,
                            weatherView.getThemeColors(picker.isLightTheme()),
                            provider,
                            picker
                    )
            );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        trendRecyclerView.setAdapter(null);
    }
}
