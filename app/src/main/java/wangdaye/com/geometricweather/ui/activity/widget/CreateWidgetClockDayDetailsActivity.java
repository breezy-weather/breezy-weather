package wangdaye.com.geometricweather.ui.activity.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoWidgetConfigActivity;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.manager.BackgroundManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayDetailsUtils;

/**
 * Create widget clock day details activity.
 * */

public class CreateWidgetClockDayDetailsActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener {

    private ImageView widgetCard;
    private ImageView widgetIcon;
    private RelativeLayout[] widgetClockContainers;
    private TextClock[] widgetClocks;
    private TextClock[] widgetClockAAs;
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

    private String clockFontValueNow = "light";
    private String[] clockFonts;
    private String[] clockFontValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_clock_day_details);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @Override
    public void initData() {
        super.initData();
        this.clockFonts = getResources().getStringArray(R.array.clock_font);
        this.clockFontValues = getResources().getStringArray(R.array.clock_font_values);
    }

    @SuppressLint("InflateParams")
    @Override
    public void initWidget() {
        View widgetView = LayoutInflater.from(this).inflate(R.layout.widget_clock_day_details, null);
        ((ViewGroup) findViewById(R.id.activity_create_widget_clock_day_details_widgetContainer)).addView(widgetView);

        this.widgetCard = widgetView.findViewById(R.id.widget_clock_day_card);
        widgetCard.setVisibility(View.GONE);

        this.widgetIcon = widgetView.findViewById(R.id.widget_clock_day_icon);
        this.widgetClockContainers = new RelativeLayout[] {
                widgetView.findViewById(R.id.widget_clock_day_clock_lightContainer),
                widgetView.findViewById(R.id.widget_clock_day_clock_normalContainer),
                widgetView.findViewById(R.id.widget_clock_day_clock_blackContainer)};
        this.widgetClocks = new TextClock[] {
                widgetView.findViewById(R.id.widget_clock_day_clock_light),
                widgetView.findViewById(R.id.widget_clock_day_clock_normal),
                widgetView.findViewById(R.id.widget_clock_day_clock_black)};
        this.widgetClockAAs = new TextClock[] {
                widgetView.findViewById(R.id.widget_clock_day_clock_aa_light),
                widgetView.findViewById(R.id.widget_clock_day_clock_aa_normal),
                widgetView.findViewById(R.id.widget_clock_day_clock_aa_black)};
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

        AppCompatSpinner clockFontSpinner = findViewById(R.id.activity_create_widget_clock_day_details_clockFontSpinner);
        clockFontSpinner.setOnItemSelectedListener(new ClockFontSpinnerSelectedListener());
        clockFontSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clockFonts));

        Button doneButton = findViewById(R.id.activity_create_widget_clock_day_details_doneButton);
        doneButton.setOnClickListener(this);
    }

    @Override
    public void refreshWidgetView(Weather weather) {
        if (weather == null) {
            return;
        }

        int imageId = WidgetClockDayDetailsUtils.getWeatherIconId(
                weather,
                TimeManager.getInstance(this).getDayTime(
                        this, weather, false).isDayTime(),
                isMinimalIcon(),
                blackTextSwitch.isChecked() || showCardSwitch.isChecked());
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

        for (int i = 0; i < clockFontValues.length; i ++) {
            if (clockFontValueNow.equals(clockFontValues[i])) {
                widgetClockContainers[i].setVisibility(View.VISIBLE);
            } else {
                widgetClockContainers[i].setVisibility(View.GONE);
            }
        }

        if (showCardSwitch.isChecked() || blackTextSwitch.isChecked()) {
            if (showCardSwitch.isChecked()) {
                widgetCard.setVisibility(View.VISIBLE);
            } else {
                widgetCard.setVisibility(View.GONE);
            }
            for (TextClock c : widgetClocks) {
                c.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            }
            for (TextClock c : widgetClockAAs) {
                c.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            }
            widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetLunar.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetSubtitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetTodayTemp.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetSensibleTemp.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetAQIHumidity.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetWind.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
        } else {
            widgetCard.setVisibility(View.GONE);
            for (TextClock c : widgetClocks) {
                c.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            }
            for (TextClock c : widgetClockAAs) {
                c.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            }
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
                editor.putString(getString(R.string.key_clock_font), clockFontValueNow);
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

                BackgroundManager.resetNormalBackgroundTask(this, true);
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

    // on item selected listener.

    private class ClockFontSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (!clockFontValueNow.equals(clockFontValues[i])) {
                clockFontValueNow = clockFontValues[i];
                refreshWidgetView(getLocationNow().weather);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // do nothing.
        }
    }
}