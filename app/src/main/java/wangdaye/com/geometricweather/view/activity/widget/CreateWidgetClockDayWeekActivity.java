package wangdaye.com.geometricweather.view.activity.widget;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Calendar;

import wangdaye.com.geometricweather.basic.GeoWidgetConfigActivity;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Create widget clock day week activity.
 * */

public class CreateWidgetClockDayWeekActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener {
    // widget
    private ImageView widgetCard;
    private ImageView widgetIcon;
    private TextClock widgetClock;
    private TextView widgetTitle;
    private TextView widgetSubtitle;
    private TextView[] widgetWeeks;
    private ImageView[] widgetIcons;
    private TextView[] widgetTemps;

    private CoordinatorLayout container;

    private Switch showCardSwitch;
    private Switch blackTextSwitch;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_clock_day_week);
    }

    @SuppressLint("InflateParams")
    @Override
    public void initWidget() {
        View widgetView = LayoutInflater.from(this).inflate(R.layout.widget_clock_day_week, null);
        ((ViewGroup) findViewById(R.id.activity_create_widget_clock_day_week_widgetContainer)).addView(widgetView);

        this.widgetCard = (ImageView) widgetView.findViewById(R.id.widget_clock_day_week_card);
        widgetCard.setVisibility(View.GONE);

        this.widgetIcon = (ImageView) widgetView.findViewById(R.id.widget_clock_day_week_icon);
        this.widgetClock = (TextClock) widgetView.findViewById(R.id.widget_clock_day_week_clock);
        this.widgetTitle = (TextView) widgetView.findViewById(R.id.widget_clock_day_week_title);
        this.widgetSubtitle = (TextView) widgetView.findViewById(R.id.widget_clock_day_week_subtitle);

        this.widgetWeeks = new TextView[] {
                (TextView) widgetView.findViewById(R.id.widget_clock_day_week_week_1),
                (TextView) widgetView.findViewById(R.id.widget_clock_day_week_week_2),
                (TextView) widgetView.findViewById(R.id.widget_clock_day_week_week_3),
                (TextView) widgetView.findViewById(R.id.widget_clock_day_week_week_4),
                (TextView) widgetView.findViewById(R.id.widget_clock_day_week_week_5)};
        this.widgetIcons = new ImageView[] {
                (ImageView) widgetView.findViewById(R.id.widget_clock_day_week_icon_1),
                (ImageView) widgetView.findViewById(R.id.widget_clock_day_week_icon_2),
                (ImageView) widgetView.findViewById(R.id.widget_clock_day_week_icon_3),
                (ImageView) widgetView.findViewById(R.id.widget_clock_day_week_icon_4),
                (ImageView) widgetView.findViewById(R.id.widget_clock_day_week_icon_5)};
        this.widgetTemps = new TextView[] {
                (TextView) widgetView.findViewById(R.id.widget_clock_day_week_temp_1),
                (TextView) widgetView.findViewById(R.id.widget_clock_day_week_temp_2),
                (TextView) widgetView.findViewById(R.id.widget_clock_day_week_temp_3),
                (TextView) widgetView.findViewById(R.id.widget_clock_day_week_temp_4),
                (TextView) widgetView.findViewById(R.id.widget_clock_day_week_temp_5)};

        ImageView wallpaper = (ImageView) findViewById(R.id.activity_create_widget_clock_day_week_wall);
        wallpaper.setImageDrawable(WallpaperManager.getInstance(this).getDrawable());

        this.container = (CoordinatorLayout) findViewById(R.id.activity_create_widget_clock_day_week_container);

        this.showCardSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_week_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.blackTextSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_week_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = (Button) findViewById(R.id.activity_create_widget_clock_day_week_doneButton);
        doneButton.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshWidgetView(Weather weather) {
        if (weather == null) {
            return;
        }
        getLocationNow().weather = weather;

        boolean isDay = TimeUtils.getInstance(this).getDayTime(this, weather, false).isDayTime();

        int[] imageId = WeatherHelper.getWeatherIcon(weather.realTime.weatherKind, isDay);
        Glide.with(this)
                .load(imageId[3])
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(widgetIcon);

        widgetTitle.setText(weather.base.date.split("-", 2)[1] + " " + weather.dailyList.get(0).week);
        widgetSubtitle.setText(weather.base.city + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, isFahrenheit()));

        String firstWeekDay;
        String secondWeekDay;
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String[] weatherDates = weather.base.date.split("-");
        if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month
                && Integer.parseInt(weatherDates[2]) == day) {
            firstWeekDay = getString(R.string.today);
            secondWeekDay = weather.dailyList.get(1).week;
        } else if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month
                && Integer.parseInt(weatherDates[2]) == day - 1) {
            firstWeekDay = getString(R.string.yesterday);
            secondWeekDay = getString(R.string.today);
        } else {
            firstWeekDay = weather.dailyList.get(0).week;
            secondWeekDay = weather.dailyList.get(1).week;
        }

        for (int i = 0; i < 5; i ++) {
            if (i == 0) {
                widgetWeeks[i].setText(firstWeekDay);
            } else if (i == 1) {
                widgetWeeks[i].setText(secondWeekDay);
            } else {
                widgetWeeks[i].setText(weather.dailyList.get(i).week);
            }
            int[] imageIds = WeatherHelper.getWeatherIcon(
                    isDay ? weather.dailyList.get(i).weatherKinds[0] : weather.dailyList.get(i).weatherKinds[1],
                    isDay);
            Glide.with(this)
                    .load(imageIds[3])
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(widgetIcons[i]);
            widgetTemps[i].setText(ValueUtils.buildDailyTemp(weather.dailyList.get(i).temps, false, isFahrenheit()));
        }
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
            case R.id.activity_create_widget_clock_day_week_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_clock_day_week_setting),
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
                widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextDark));
                widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextDark));
                widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextDark));
                for (int i = 0; i < 5; i ++) {
                    widgetWeeks[i].setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextDark));
                    widgetTemps[i].setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextDark));
                }
            } else {
                widgetCard.setVisibility(View.GONE);
                if (!blackTextSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextLight));
                    widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextLight));
                    widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextLight));
                    for (int i = 0; i < 5; i ++) {
                        widgetWeeks[i].setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextLight));
                        widgetTemps[i].setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextLight));
                    }
                }
            }
        }
    }

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextDark));
                widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextDark));
                widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextDark));
                for (int i = 0; i < 5; i ++) {
                    widgetWeeks[i].setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextDark));
                    widgetTemps[i].setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextDark));
                }
            } else {
                if (!showCardSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextLight));
                    widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextLight));
                    widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextLight));
                    for (int i = 0; i < 5; i ++) {
                        widgetWeeks[i].setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextLight));
                        widgetTemps[i].setTextColor(ContextCompat.getColor(CreateWidgetClockDayWeekActivity.this, R.color.colorTextLight));
                    }
                }
            }
        }
    }
}