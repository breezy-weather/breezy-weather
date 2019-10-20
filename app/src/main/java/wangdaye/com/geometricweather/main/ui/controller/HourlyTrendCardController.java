package wangdaye.com.geometricweather.main.ui.controller;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Base;
import wangdaye.com.geometricweather.basic.model.weather.Minutely;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.HourlyTrendAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.PrecipitationBar;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendHelper;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class HourlyTrendCardController extends AbstractMainItemController {

    private CardView card;

    private TextView title;
    private TextView subtitle;
    private TrendRecyclerView trendRecyclerView;

    private LinearLayout minutelyContainer;
    private TextView minutelyTitle;
    private PrecipitationBar precipitationBar;
    private TextView minutelyStartText;
    private TextView minutelyEndText;

    @NonNull private WeatherView weatherView;
    @Px private float cardMarginsVertical;
    @Px private float cardMarginsHorizontal;

    public HourlyTrendCardController(@NonNull Activity activity, @NonNull WeatherView weatherView,
                                     @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                                     @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                                     @Px float cardRadius) {
        super(activity, activity.findViewById(R.id.container_main_hourly_trend_card), provider, picker,
                cardMarginsVertical, cardMarginsHorizontal, cardRadius);

        this.card = view.findViewById(R.id.container_main_hourly_trend_card);
        this.title = view.findViewById(R.id.container_main_hourly_trend_card_title);
        this.subtitle = view.findViewById(R.id.container_main_hourly_trend_card_subtitle);
        this.trendRecyclerView = view.findViewById(R.id.container_main_hourly_trend_card_trendRecyclerView);
        this.minutelyContainer = view.findViewById(R.id.container_main_hourly_trend_card_minutely);
        this.minutelyTitle = view.findViewById(R.id.container_main_hourly_trend_card_minutelyTitle);
        this.precipitationBar = view.findViewById(R.id.container_main_hourly_trend_card_minutelyBar);
        this.minutelyStartText = view.findViewById(R.id.container_main_hourly_trend_card_minutelyStartText);
        this.minutelyEndText = view.findViewById(R.id.container_main_hourly_trend_card_minutelyEndText);

        this.weatherView = weatherView;
        this.cardMarginsVertical = cardMarginsVertical;
        this.cardMarginsHorizontal = cardMarginsHorizontal;

        minutelyContainer.setOnClickListener(v -> {

        });
    }

    @Override
    public void onBindView(@NonNull Location location) {
        if (location.getWeather() != null) {
            Weather weather = location.getWeather();
            TemperatureUnit unit = SettingsOptionManager.getInstance(context).getTemperatureUnit();

            card.setCardBackgroundColor(picker.getRootColor(context));

            title.setTextColor(weatherView.getThemeColors(picker.isLightTheme())[0]);

            if (TextUtils.isEmpty(weather.getCurrent().getHourlyForecast())) {
                subtitle.setVisibility(View.GONE);
            } else {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(weather.getCurrent().getHourlyForecast());
            }

            trendRecyclerView.setHasFixedSize(true);
            trendRecyclerView.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            trendRecyclerView.setAdapter(
                    new HourlyTrendAdapter(
                            (GeoActivity) context, trendRecyclerView,
                            cardMarginsVertical, cardMarginsHorizontal,
                            DisplayUtils.isTabletDevice(context) ? 7 : 5,
                            context.getResources().getDimensionPixelSize(R.dimen.hourly_trend_item_height),
                            weather,
                            weatherView.getThemeColors(picker.isLightTheme()),
                            provider,
                            picker,
                            unit
                    )
            );

            trendRecyclerView.setLineColor(picker.getLineColor(context));
            if (weather.getYesterday() == null) {
                trendRecyclerView.setData(
                        null, null, 0, 0, null, null);
            } else {
                int[] highestAndLowest = TrendHelper.getHighestAndLowestHourlyTemperature(weather);
                trendRecyclerView.setData(
                        (float) weather.getYesterday().getDaytimeTemperature(),
                        (float) weather.getYesterday().getNighttimeTemperature(),
                        highestAndLowest[0],
                        highestAndLowest[1],
                        Temperature.getShortTemperature(weather.getYesterday().getDaytimeTemperature(), unit),
                        Temperature.getShortTemperature(weather.getYesterday().getNighttimeTemperature(), unit)
                );
            }

            List<Minutely> minutelyList = weather.getMinutelyForecast();
            if (minutelyList.size() != 0 && needToShowMinutelyForecast(minutelyList)) {
                minutelyContainer.setVisibility(View.VISIBLE);

                minutelyTitle.setTextColor(picker.getTextContentColor(context));

                precipitationBar.setBackgroundColor(picker.getLineColor(context));
                precipitationBar.setPrecipitationColor(weatherView.getThemeColors(picker.isLightTheme())[0]);
                precipitationBar.setMinutelyList(minutelyList);

                int size = minutelyList.size();
                minutelyStartText.setText(Base.getTime(context, minutelyList.get(0).getDate()));
                minutelyStartText.setTextColor(picker.getTextSubtitleColor(context));
                minutelyEndText.setText(Base.getTime(context, minutelyList.get(size - 1).getDate()));
                minutelyEndText.setTextColor(picker.getTextSubtitleColor(context));
            } else {
                minutelyContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroy() {
        trendRecyclerView.setAdapter(null);
    }

    private static boolean needToShowMinutelyForecast(List<Minutely> minutelyList) {
        for (Minutely m : minutelyList) {
            if (m.isPrecipitation()) {
                return true;
            }
        }
        return false;
    }
}
