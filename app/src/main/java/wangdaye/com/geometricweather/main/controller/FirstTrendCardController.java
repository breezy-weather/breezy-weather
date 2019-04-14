package wangdaye.com.geometricweather.main.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class FirstTrendCardController extends AbstractMainItemController
        implements View.OnClickListener {

    private CardView card;
    
    private AppCompatImageView timeIcon;
    private TextView refreshTime;

    private TextView alert;
    private View line;

    private TextView title;
    private TextView subtitle;
    private TrendRecyclerView trendRecyclerView;
    
    @NonNull private WeatherView weatherView;
    @Nullable private Weather weather;
    
    public FirstTrendCardController(@NonNull Activity activity, @NonNull WeatherView weatherView) {
        super(activity, activity.findViewById(R.id.container_main_first_trend_card));

        this.card = view.findViewById(R.id.container_main_first_trend_card);
        this.timeIcon = view.findViewById(R.id.container_main_first_trend_card_timeIcon);
        this.refreshTime = view.findViewById(R.id.container_main_first_trend_card_timeText);
        this.alert = view.findViewById(R.id.container_main_first_trend_card_alert);
        this.line = view.findViewById(R.id.container_main_first_trend_card_line);
        this.title = view.findViewById(R.id.container_main_first_trend_card_title);
        this.subtitle = view.findViewById(R.id.container_main_first_trend_card_subtitle);
        this.trendRecyclerView = view.findViewById(R.id.container_main_first_trend_card_trendRecyclerView);
        
        this.weatherView = weatherView;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindView(@NonNull Location location) {
        if (GeometricWeather.getInstance().getCardOrder().equals("daily_first")) {
            if (!isDisplay("daily_overview")) {
                view.setVisibility(View.GONE);
                return;
            } else {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if (!isDisplay("hourly_overview")) {
                view.setVisibility(View.GONE);
                return;
            } else {
                view.setVisibility(View.VISIBLE);
            }
        }

        if (location.weather != null) {
            weather = location.weather;

            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorRoot));

            view.findViewById(R.id.container_main_first_trend_card_timeContainer).setOnClickListener(this);
            if (weather.alertList.size() == 0) {
                timeIcon.setEnabled(false);
                timeIcon.setImageResource(R.drawable.ic_time);
            } else {
                timeIcon.setEnabled(true);
                timeIcon.setImageResource(R.drawable.ic_alert);
            }
            timeIcon.setSupportImageTintList(context.getResources().getColorStateList(R.color.colorTextContent));
            timeIcon.setOnClickListener(this);
            
            refreshTime.setText(weather.base.time);
            refreshTime.setTextColor(ContextCompat.getColor(context, R.color.colorTextContent));

            if (weather.alertList.size() == 0) {
                alert.setVisibility(View.GONE);
                line.setVisibility(View.GONE);
            } else {
                alert.setVisibility(View.VISIBLE);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < weather.alertList.size(); i ++) {
                    builder.append(weather.alertList.get(i).description)
                            .append(", ")
                            .append(weather.alertList.get(i).publishTime);
                    if (i != weather.alertList.size() - 1) {
                        builder.append("\n");
                    }
                }
                alert.setText(builder.toString());
                line.setVisibility(View.VISIBLE);
                line.setBackgroundColor(ContextCompat.getColor(context, R.color.colorLine));
            }
            alert.setOnClickListener(this);

            title.setTextColor(weatherView.getThemeColors()[0]);

            if (GeometricWeather.getInstance().getCardOrder().equals("daily_first")) {
                TrendViewController.setDailyTrend(
                        context, title, subtitle, trendRecyclerView,
                        weather, location.history, weatherView.getThemeColors()
                );
            } else {
                TrendViewController.setHourlyTrend(
                        context, title, subtitle, trendRecyclerView,
                        weather, location.history, weatherView.getThemeColors()
                );
            }
        }
    }

    @Override
    public void onDestroy() {
        trendRecyclerView.setAdapter(null);
    }

    // interface.
    
    @Override
    public void onClick(View v) {
        GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
        if (activity != null) {
            switch (v.getId()) {
                case R.id.container_main_first_trend_card_timeIcon:
                case R.id.container_main_first_trend_card_alert:
                    if (weather != null) {
                        IntentHelper.startAlertActivity(activity, weather);
                    }
                    break;

                case R.id.container_main_first_trend_card_timeContainer:
                    IntentHelper.startManageActivityForResult(activity);
                    break;
            }
        }
    }
}
