package wangdaye.com.geometricweather.main.ui.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.CardOrder;
import wangdaye.com.geometricweather.basic.model.weather.Base;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendRecyclerView;
import wangdaye.com.geometricweather.ui.widget.trendView.TrendViewController;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class FirstTrendCardController extends AbstractMainItemController
        implements View.OnClickListener {

    private CardView card;
    
    private AppCompatImageView timeIcon;
    private TextView refreshTime;
    private AppCompatImageView localTimeIcon;
    private TextClock localTime;

    private TextView alert;
    private View line;

    private TextView title;
    private TextView subtitle;
    private TrendRecyclerView trendRecyclerView;
    
    @NonNull private WeatherView weatherView;
    @Nullable private Weather weather;

    public FirstTrendCardController(@NonNull Activity activity, @NonNull WeatherView weatherView,
                                    @NonNull ResourceProvider provider, @NonNull MainColorPicker picker) {
        super(activity, activity.findViewById(R.id.container_main_first_trend_card), provider, picker);

        this.card = view.findViewById(R.id.container_main_first_trend_card);
        this.timeIcon = view.findViewById(R.id.container_main_first_trend_card_timeIcon);
        this.refreshTime = view.findViewById(R.id.container_main_first_trend_card_timeText);
        this.localTimeIcon = view.findViewById(R.id.container_main_first_trend_card_localTimeIcon);
        this.localTime = view.findViewById(R.id.container_main_first_trend_card_localTimeText);
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
        if (SettingsOptionManager.getInstance(context).getCardOrder() == CardOrder.DAILY_FIRST) {
            if (!isDisplay(SettingsOptionManager.CARD_DAILY_OVERVIEW)) {
                view.setVisibility(View.GONE);
                return;
            } else {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if (!isDisplay(SettingsOptionManager.CARD_HOURLY_OVERVIEW)) {
                view.setVisibility(View.GONE);
                return;
            } else {
                view.setVisibility(View.VISIBLE);
            }
        }

        if (location.getWeather() != null) {
            weather = location.getWeather();

            card.setCardBackgroundColor(picker.getRootColor(context));

            view.findViewById(R.id.container_main_first_trend_card_timeContainer).setOnClickListener(this);
            if (weather.getAlertList().size() == 0) {
                timeIcon.setEnabled(false);
                timeIcon.setImageResource(R.drawable.ic_time);
            } else {
                timeIcon.setEnabled(true);
                timeIcon.setImageResource(R.drawable.ic_alert);
            }
            timeIcon.setSupportImageTintList(
                    ColorStateList.valueOf(picker.getTextContentColor(context))
            );
            timeIcon.setOnClickListener(this);
            
            refreshTime.setText(Base.getTime(context, weather.getBase().getUpdateDate()));
            refreshTime.setTextColor(picker.getTextContentColor(context));

            long time = System.currentTimeMillis();
            if (TimeZone.getDefault().getOffset(time) == location.getTimeZone().getOffset(time)) {
                // same time zone.
                localTimeIcon.setVisibility(View.GONE);
                localTime.setVisibility(View.GONE);
            } else {
                localTimeIcon.setVisibility(View.VISIBLE);
                localTime.setVisibility(View.VISIBLE);

                localTimeIcon.setSupportImageTintList(
                        ColorStateList.valueOf(picker.getTextContentColor(context))
                );
                localTime.setTimeZone(location.getTimeZone().getID());
                localTime.setTextColor(picker.getTextContentColor(context));
            }

            if (weather.getAlertList().size() == 0) {
                alert.setVisibility(View.GONE);
                line.setVisibility(View.GONE);
            } else {
                alert.setVisibility(View.VISIBLE);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < weather.getAlertList().size(); i ++) {
                    builder.append(weather.getAlertList().get(i).getDescription())
                            .append(", ")
                            .append(
                                    DateFormat.getDateTimeInstance(
                                            DateFormat.LONG,
                                            DateFormat.DEFAULT
                                    ).format(weather.getAlertList().get(i).getDate())
                            );
                    if (i != weather.getAlertList().size() - 1) {
                        builder.append("\n");
                    }
                }
                alert.setText(builder.toString());
                alert.setTextColor(picker.getTextSubtitleColor(context));

                line.setVisibility(View.VISIBLE);
                line.setBackgroundColor(picker.getLineColor(context));
            }
            alert.setOnClickListener(this);

            title.setTextColor(weatherView.getThemeColors(picker.isLightTheme())[0]);

            if (SettingsOptionManager.getInstance(context).getCardOrder() == CardOrder.DAILY_FIRST) {
                TrendViewController.setDailyTrend(
                        (GeoActivity) context, title, subtitle, trendRecyclerView, provider, picker,
                        weather, weatherView.getThemeColors(picker.isLightTheme())
                );
            } else {
                TrendViewController.setHourlyTrend(
                        (GeoActivity) context, title, subtitle, trendRecyclerView, provider, picker,
                        weather, weatherView.getThemeColors(picker.isLightTheme())
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
        switch (v.getId()) {
            case R.id.container_main_first_trend_card_timeIcon:
            case R.id.container_main_first_trend_card_alert:
                if (weather != null) {
                    IntentHelper.startAlertActivity((GeoActivity) context, weather);
                }
                break;

            case R.id.container_main_first_trend_card_timeContainer:
                IntentHelper.startManageActivityForResult((GeoActivity) context);
                break;
        }
    }
}
