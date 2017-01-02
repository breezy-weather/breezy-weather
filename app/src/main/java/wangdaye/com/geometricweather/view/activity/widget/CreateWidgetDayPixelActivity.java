package wangdaye.com.geometricweather.view.activity.widget;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoWidgetConfigActivity;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Create widget day pixel activity.
 * */

public class CreateWidgetDayPixelActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener {
    // widget
    private ImageView widgetIcon;
    private TextView widgetTemp;
    private TextView widgetDate;

    private CoordinatorLayout container;

    private Switch blackTextSwitch;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_day_pixel);
    }

    @Override
    public void initWidget() {
        this.widgetIcon = (ImageView) findViewById(R.id.widget_day_pixel_icon);
        this.widgetTemp = (TextView) findViewById(R.id.widget_day_pixel_temp);
        this.widgetDate = (TextView) findViewById(R.id.widget_day_pixel_date);

        ImageView wallpaper = (ImageView) findViewById(R.id.activity_create_widget_day_pixel_wall);
        wallpaper.setImageDrawable(WallpaperManager.getInstance(this).getDrawable());

        this.container = (CoordinatorLayout) findViewById(R.id.activity_create_widget_day_pixel_container);

        this.blackTextSwitch = (Switch) findViewById(R.id.activity_create_widget_day_pixel_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = (Button) findViewById(R.id.activity_create_widget_day_pixel_doneButton);
        doneButton.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshWidgetView(Weather weather) {
        if (weather == null) {
            return;
        }

        int[] imageId = WeatherHelper.getWeatherIcon(
                weather.realTime.weatherKind,
                TimeUtils.getInstance(this).getDayTime(this, weather, false).isDayTime());
        widgetIcon.setImageResource(imageId[3]);

        widgetTemp.setText(weather.realTime.temp + "℃");
        widgetDate.setText(weather.base.date.split("-", 2)[1] + " " + weather.base.city);
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
            case R.id.activity_create_widget_day_pixel_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_day_pixel_setting),
                        MODE_PRIVATE)
                        .edit();
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

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetTemp.setTextColor(ContextCompat.getColor(CreateWidgetDayPixelActivity.this, R.color.colorTextDark));
                widgetDate.setTextColor(ContextCompat.getColor(CreateWidgetDayPixelActivity.this, R.color.colorTextDark));
            } else {
                widgetTemp.setTextColor(ContextCompat.getColor(CreateWidgetDayPixelActivity.this, R.color.colorTextLight));
                widgetDate.setTextColor(ContextCompat.getColor(CreateWidgetDayPixelActivity.this, R.color.colorTextLight));
            }
        }
    }
}