package wangdaye.com.geometricweather.view.activity.widget;

import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;

import wangdaye.com.geometricweather.basic.GeoWidgetConfigActivity;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Create widget clock day activity.
 * */

public class CreateWidgetClockDayActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener {
    // widget
    private ImageView widgetCard;
    private ImageView widgetIcon;
    private TextClock widgetClock;
    private TextView widgetDate;
    private TextView widgetWeather;

    private CoordinatorLayout container;

    private Switch showCardSwitch;
    private Switch blackTextSwitch;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_clock_day);
    }

    @Override
    public void initWidget() {
        this.widgetCard = (ImageView) findViewById(R.id.widget_clock_day_card);
        widgetCard.setVisibility(View.GONE);

        this.widgetIcon = (ImageView) findViewById(R.id.widget_clock_day_icon);
        this.widgetClock = (TextClock) findViewById(R.id.widget_clock_day_clock);
        this.widgetDate = (TextView) findViewById(R.id.widget_clock_day_date);
        this.widgetWeather = (TextView) findViewById(R.id.widget_clock_day_weather);

        ImageView wallpaper = (ImageView) findViewById(R.id.activity_create_widget_clock_day_wall);
        wallpaper.setImageDrawable(WallpaperManager.getInstance(this).getDrawable());

        this.container = (CoordinatorLayout) findViewById(R.id.activity_create_widget_clock_day_container);

        this.showCardSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.blackTextSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = (Button) findViewById(R.id.activity_create_widget_clock_day_doneButton);
        doneButton.setOnClickListener(this);
    }

    @Override
    public void refreshWidgetView(Weather weather) {
        if (weather == null) {
            return;
        }

        int[] imageId = WeatherHelper.getWeatherIcon(
                weather.realTime.weatherKind,
                TimeUtils.getInstance(this).getDayTime(this, weather, false).isDayTime());
        widgetIcon.setImageResource(imageId[3]);

        String[] solar = weather.base.date.split("-");
        String dateText = solar[1] + "-" + solar[2] + " " + weather.dailyList.get(0).week;
        widgetDate.setText(dateText);

        String weatherText = weather.base.city + " / " + weather.realTime.weather + " " + weather.realTime.temp + "℃";
        widgetWeather.setText(weatherText);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    /** <br> interface. */

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_clock_day_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_clock_day_setting),
                        MODE_PRIVATE)
                        .edit();
                editor.putBoolean(getString(R.string.key_show_card), showCardSwitch.isChecked());
                editor.putBoolean(getString(R.string.key_black_text), blackTextSwitch.isChecked());
                editor.apply();

                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                int appWidgetId = 0;
                if (extras != null) {
                    appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);
                }

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);

                ServiceHelper.startPollingService(this);
                finish();
                break;
        }
    }

    // on check changed listener(switch).

    private class ShowCardSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetCard.setVisibility(View.VISIBLE);
                widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
                widgetDate.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
                widgetWeather.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
            } else {
                widgetCard.setVisibility(View.GONE);
                if (!blackTextSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                    widgetDate.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                    widgetWeather.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                }
            }
        }
    }

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
                widgetDate.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
                widgetWeather.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
            } else {
                if (!showCardSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                    widgetDate.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                    widgetWeather.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                }
            }
        }
    }
}