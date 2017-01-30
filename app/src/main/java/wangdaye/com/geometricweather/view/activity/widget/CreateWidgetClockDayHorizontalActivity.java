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

import wangdaye.com.geometricweather.basic.GeoWidgetConfigActivity;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Create widget clock day horizontal activity.
 * */

public class CreateWidgetClockDayHorizontalActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener {
    private ImageView widgetCard;
    private ImageView widgetIcon;
    private TextClock widgetClock;
    private TextView widgetTitle;
    private TextView widgetSubtitle;

    private CoordinatorLayout container;

    private Switch showCardSwitch;
    private Switch blackTextSwitch;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_clock_day_horizontal);
    }

    @SuppressLint("InflateParams")
    @Override
    public void initWidget() {
        View widgetView = LayoutInflater.from(this).inflate(R.layout.widget_clock_day_horizontal, null);
        ((ViewGroup) findViewById(R.id.activity_create_widget_clock_day_horizontal_widgetContainer)).addView(widgetView);

        this.widgetCard = (ImageView) widgetView.findViewById(R.id.widget_clock_day_card);
        widgetCard.setVisibility(View.GONE);

        this.widgetIcon = (ImageView) widgetView.findViewById(R.id.widget_clock_day_icon);
        this.widgetClock = (TextClock) widgetView.findViewById(R.id.widget_clock_day_clock);
        this.widgetTitle = (TextView) widgetView.findViewById(R.id.widget_clock_day_title);
        this.widgetSubtitle = (TextView) widgetView.findViewById(R.id.widget_clock_day_subtitle);

        ImageView wallpaper = (ImageView) findViewById(R.id.activity_create_widget_clock_day_horizontal_wall);
        wallpaper.setImageDrawable(WallpaperManager.getInstance(this).getDrawable());

        this.container = (CoordinatorLayout) findViewById(R.id.activity_create_widget_clock_day_horizontal_container);

        this.showCardSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_horizontal_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.blackTextSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_horizontal_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = (Button) findViewById(R.id.activity_create_widget_clock_day_horizontal_doneButton);
        doneButton.setOnClickListener(this);
    }

    @Override
    public void refreshWidgetView(Weather weather) {
        if (weather == null) {
            return;
        }
        getLocationNow().weather = weather;

        int[] imageId = WeatherHelper.getWeatherIcon(
                weather.realTime.weatherKind,
                TimeUtils.getInstance(this).getDayTime(this, weather, false).isDayTime());
        Glide.with(this)
                .load(imageId[3])
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(widgetIcon);
        widgetTitle.setText(weather.base.date.split("-", 2)[1] + " " + weather.dailyList.get(0).week);
        widgetSubtitle.setText(weather.base.city + " "
                + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, isFahrenheit()));
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
            case R.id.activity_create_widget_clock_day_horizontal_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_clock_day_horizontal_setting),
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

                ServiceHelper.startPollingService(this, false);
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
                widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextDark));
                widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextDark));
                widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextDark));
            } else {
                widgetCard.setVisibility(View.GONE);
                if (!blackTextSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextLight));
                    widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextLight));
                    widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextLight));
                }
            }
        }
    }

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextDark));
                widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextDark));
                widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextDark));
            } else {
                if (!showCardSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextLight));
                    widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextLight));
                    widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayHorizontalActivity.this, R.color.colorTextLight));
                }
            }
        }
    }
}