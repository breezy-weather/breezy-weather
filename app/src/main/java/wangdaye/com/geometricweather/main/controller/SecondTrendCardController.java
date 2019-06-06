package wangdaye.com.geometricweather.main.controller;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.main.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class SecondTrendCardController extends AbstractMainItemController {

    private CardView card;

    private TextView title;
    private TextView subtitle;
    private TrendRecyclerView trendRecyclerView;

    @NonNull private WeatherView weatherView;

    public SecondTrendCardController(@NonNull Activity activity, @NonNull WeatherView weatherView,
                                     @NonNull ResourceProvider provider, @NonNull MainColorPicker picker) {
        super(activity, activity.findViewById(R.id.container_main_second_trend_card), provider, picker);

        this.card = view.findViewById(R.id.container_main_second_trend_card);
        this.title = view.findViewById(R.id.container_main_second_trend_card_title);
        this.subtitle = view.findViewById(R.id.container_main_second_trend_card_subtitle);
        this.trendRecyclerView = view.findViewById(R.id.container_main_second_trend_card_trendRecyclerView);

        this.weatherView = weatherView;
    }

    @Override
    public void onBindView(@NonNull Location location) {
        if (SettingsOptionManager.getInstance(context).getCardOrder().equals("daily_first")) {
            if (!isDisplay("hourly_overview")) {
                view.setVisibility(View.GONE);
                return;
            } else {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if (!isDisplay("daily_overview")) {
                view.setVisibility(View.GONE);
                return;
            } else {
                view.setVisibility(View.VISIBLE);
            }
        }

        if (location.weather != null) {
            card.setCardBackgroundColor(picker.getRootColor(context));

            title.setTextColor(weatherView.getThemeColors(picker.isLightTheme())[0]);

            if (SettingsOptionManager.getInstance(context).getCardOrder().equals("daily_first")) {
                TrendViewController.setHourlyTrend(
                        context, title, subtitle, trendRecyclerView, provider, picker,
                        location.weather, location.history, weatherView.getThemeColors(picker.isLightTheme()));
            } else {
                TrendViewController.setDailyTrend(
                        context, title, subtitle, trendRecyclerView, provider, picker,
                        location.weather, location.history, weatherView.getThemeColors(picker.isLightTheme()));
            }
        }
    }

    @Override
    public void onDestroy() {
        trendRecyclerView.setAdapter(null);
    }
}
