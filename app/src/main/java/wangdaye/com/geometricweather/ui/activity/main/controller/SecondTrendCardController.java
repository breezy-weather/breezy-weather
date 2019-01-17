package wangdaye.com.geometricweather.ui.activity.main.controller;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.widget.TextView;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class SecondTrendCardController extends AbstractMainItemController {

    private CardView card;

    private TextView title;
    private TextView subtitle;
    private TrendRecyclerView trendRecyclerView;

    @NonNull private WeatherView weatherView;

    public SecondTrendCardController(@NonNull Activity activity, @NonNull WeatherView weatherView) {
        super(activity, activity.findViewById(R.id.container_main_second_trend_card));

        this.card = view.findViewById(R.id.container_main_second_trend_card);
        this.title = view.findViewById(R.id.container_main_second_trend_card_title);
        this.subtitle = view.findViewById(R.id.container_main_second_trend_card_subtitle);
        this.trendRecyclerView = view.findViewById(R.id.container_main_second_trend_card_trendRecyclerView);

        this.weatherView = weatherView;
    }

    @Override
    public void onBindView(@NonNull Location location) {
        if (location.weather != null) {
            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorRoot));

            title.setTextColor(weatherView.getThemeColors()[0]);

            if (GeometricWeather.getInstance().getCardOrder().equals("daily_first")) {
                TrendViewController.setHourlyTrend(
                        context, title, subtitle, trendRecyclerView,
                        location.weather, location.history, weatherView.getThemeColors());
            } else {
                TrendViewController.setDailyTrend(
                        context, title, subtitle, trendRecyclerView,
                        location.weather, location.history, weatherView.getThemeColors());
            }
        }
    }
}
