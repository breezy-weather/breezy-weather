package wangdaye.com.geometricweather.ui.activity.widget;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoWidgetConfigActivity;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.remoteView.WidgetTextUtils;

/**
 * Create widget text activity.
 * */

public class CreateWidgetTextActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener {

    private View[] widgetViews;
    private TextView widgetDate;
    private TextView widgetWeather;
    private TextView widgetTemperature;

    private CoordinatorLayout container;

    private Switch blackTextSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_text);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @Override
    public void initWidget() {
        setWidgetView();

        ImageView wallpaper = findViewById(R.id.activity_create_widget_text_wall);
        bindWallpaper(wallpaper);

        this.container = findViewById(R.id.activity_create_widget_text_container);
        
        this.blackTextSwitch = findViewById(R.id.activity_create_widget_text_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = findViewById(R.id.activity_create_widget_text_doneButton);
        doneButton.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshWidgetView(Weather weather) {
        if (weather == null) {
            return;
        }

        widgetWeather.setText(WidgetTextUtils.getWeather(weather));
        widgetTemperature.setText(WidgetTextUtils.getTemperature(weather, isFahrenheit()));

        if (blackTextSwitch.isChecked()) {
            widgetDate.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetWeather.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetTemperature.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
        } else {
            widgetDate.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetWeather.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetTemperature.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
        }
    }

    @SuppressLint("InflateParams")
    private void setWidgetView() {
        this.widgetViews = new View[] {
                LayoutInflater.from(this).inflate(R.layout.widget_text, null)};
        for (View widgetView : widgetViews) {
            ((ViewGroup) findViewById(R.id.activity_create_widget_text_widgetContainer)).addView(widgetView);
        }
        for (View widgetView : widgetViews) {
            widgetView.setVisibility(View.GONE);
        }
        
        this.widgetViews[0].setVisibility(View.VISIBLE);
        this.widgetDate = widgetViews[0].findViewById(R.id.widget_text_date);
        this.widgetWeather = widgetViews[0].findViewById(R.id.widget_text_weather);
        this.widgetTemperature = widgetViews[0].findViewById(R.id.widget_text_temperature);
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_text_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_text_setting),
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

                ServiceHelper.resetNormalService(this, false, true);
                finish();
                break;
        }
    }

    // on check changed listener(switch).

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            refreshWidgetView(getLocationNow().weather);
        }
    }
}