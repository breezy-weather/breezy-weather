package wangdaye.com.geometricweather.ui.activity.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoWidgetConfigActivity;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayDetailsUtils;

/**
 * Create widget clock day details activity.
 * */

public class CreateWidgetClockDayDetailsActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener {

    private ImageView widgetCard;
    private ImageView widgetIcon;
    private TextClock widgetClock;
    private TextClock widgetClockAA;
    private TextClock widgetTitle;
    private TextView widgetLunar;
    private TextView widgetSubtitle;
    private TextView widgetTodayTemp;
    private TextView widgetSensibleTemp;
    private TextView widgetAQIHumidity;
    private TextView widgetWind;

    private CoordinatorLayout container;

    private Switch showCardSwitch;
    private Switch blackTextSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_clock_day_details);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @SuppressLint("InflateParams")
    @Override
    public void initWidget() {
        View widgetView = LayoutInflater.from(this).inflate(R.layout.widget_clock_day_details, null);
        ((ViewGroup) findViewById(R.id.activity_create_widget_clock_day_details_widgetContainer)).addView(widgetView);

        this.widgetCard = widgetView.findViewById(R.id.widget_clock_day_card);
        widgetCard.setVisibility(View.GONE);

        this.widgetIcon = widgetView.findViewById(R.id.widget_clock_day_icon);
        this.widgetClock = widgetView.findViewById(R.id.widget_clock_day_clock);
        this.widgetClockAA = widgetView.findViewById(R.id.widget_clock_day_clock_aa);
        this.widgetTitle = widgetView.findViewById(R.id.widget_clock_day_title);
        this.widgetLunar = widgetView.findViewById(R.id.widget_clock_day_lunar);
        this.widgetSubtitle = widgetView.findViewById(R.id.widget_clock_day_subtitle);
        this.widgetTodayTemp = widgetView.findViewById(R.id.widget_clock_day_todayTemp);
        this.widgetSensibleTemp = widgetView.findViewById(R.id.widget_clock_day_sensibleTemp);
        this.widgetAQIHumidity = widgetView.findViewById(R.id.widget_clock_day_aqiHumidity);
        this.widgetWind = widgetView.findViewById(R.id.widget_clock_day_wind);

        ImageView wallpaper = findViewById(R.id.activity_create_widget_clock_day_details_wall);
        bindWallpaper(wallpaper);

        this.container = findViewById(R.id.activity_create_widget_clock_day_details_container);

        this.showCardSwitch = findViewById(R.id.activity_create_widget_clock_day_details_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.blackTextSwitch = findViewById(R.id.activity_create_widget_clock_day_details_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = findViewById(R.id.activity_create_widget_clock_day_details_doneButton);
        doneButton.setOnClickListener(this);
    }

    @Override
    public void refreshWidgetView(Weather weather) {
        if (weather == null) {
            return;
        }

        String iconStyle = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(
                        getString(R.string.key_widget_icon_style),
                        "material");

        int imageId = WidgetClockDayDetailsUtils.getWeatherIconId(
                weather,
                TimeManager.getInstance(this).getDayTime(this, weather, false).isDayTime(),
                iconStyle,
                blackTextSwitch.isChecked());
        Glide.with(this)
                .load(imageId)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(widgetIcon);
        widgetLunar.setText(WidgetClockDayDetailsUtils.getLunarText(this));
        widgetSubtitle.setText(WidgetClockDayDetailsUtils.getSubtitleText(weather, isFahrenheit()));
        widgetTodayTemp.setText(WidgetClockDayDetailsUtils.getTodayTempText(this, weather, isFahrenheit()));
        widgetSensibleTemp.setText(WidgetClockDayDetailsUtils.getSensibleTempText(this, weather, isFahrenheit()));
        widgetAQIHumidity.setText(WidgetClockDayDetailsUtils.getAQIHumidityTempText(this, weather));
        widgetWind.setText(WidgetClockDayDetailsUtils.getWindText(this, weather));

        if (showCardSwitch.isChecked() || blackTextSwitch.isChecked()) {
            if (showCardSwitch.isChecked()) {
                widgetCard.setVisibility(View.VISIBLE);
            } else {
                widgetCard.setVisibility(View.GONE);
            }
            widgetClock.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetClockAA.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetLunar.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetSubtitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetTodayTemp.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetSensibleTemp.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetAQIHumidity.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetWind.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
        } else {
            widgetCard.setVisibility(View.GONE);
            widgetClock.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetClockAA.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetLunar.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetSubtitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetTodayTemp.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetSensibleTemp.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetAQIHumidity.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetWind.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
        }
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_clock_day_details_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_clock_day_details_setting),
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

                ServiceHelper.resetNormalService(this, false, true);
                finish();
                break;
        }
    }

    // on check changed listener(switch).

    private class ShowCardSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            refreshWidgetView(getLocationNow().weather);
        }
    }

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            refreshWidgetView(getLocationNow().weather);
        }
    }
}