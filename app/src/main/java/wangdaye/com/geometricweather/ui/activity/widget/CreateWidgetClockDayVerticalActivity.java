package wangdaye.com.geometricweather.ui.activity.widget;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.basic.GeoWidgetConfigActivity;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.service.PollingService;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Create widget clock day center activity.
 * */

public class CreateWidgetClockDayVerticalActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener {

    private View[] widgetViews;
    private ImageView widgetCard;
    private ImageView widgetIcon;
    private TextClock widgetClock;
    private TextView widgetTitle;
    private TextView widgetSubtitle;
    private TextView widgetTime;

    private CoordinatorLayout container;

    private Switch showCardSwitch;
    private Switch hideSubtitleSwitch;
    private Switch blackTextSwitch;

    // data
    private String viewTypeValueNow = "rectangle";
    private String[] viewTypes;
    private String[] viewTypeValues;

    private String subtitleDataValueNow = "time";
    private String[] subtitleData;
    private String[] subtitleDataValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_clock_day_vertical);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @Override
    public void initData() {
        super.initData();
        this.viewTypes = new String[] {
                getResources().getStringArray(R.array.widget_styles)[0],
                getResources().getStringArray(R.array.widget_styles)[1],
                getResources().getStringArray(R.array.widget_styles)[2],
                getResources().getStringArray(R.array.widget_styles)[3]};
        this.viewTypeValues = new String[] {
                getResources().getStringArray(R.array.widget_style_values)[0],
                getResources().getStringArray(R.array.widget_style_values)[1],
                getResources().getStringArray(R.array.widget_style_values)[2],
                getResources().getStringArray(R.array.widget_style_values)[3]};
        this.subtitleData = getResources().getStringArray(R.array.subtitle_data);
        this.subtitleDataValues = getResources().getStringArray(R.array.subtitle_data_values);
    }

    @Override
    public void initWidget() {
        setWidgetView(true);

        ImageView wallpaper = (ImageView) findViewById(R.id.activity_create_widget_clock_day_vertical_wall);
        wallpaper.setImageDrawable(WallpaperManager.getInstance(this).getDrawable());

        this.container = (CoordinatorLayout) findViewById(R.id.activity_create_widget_clock_day_vertical_container);

        AppCompatSpinner viewTypeSpinner
                = (AppCompatSpinner) findViewById(R.id.activity_create_widget_clock_day_vertical_styleSpinner);
        viewTypeSpinner.setOnItemSelectedListener(new ViewTypeSpinnerSelectedListener());
        viewTypeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, viewTypes));

        this.showCardSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_vertical_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.hideSubtitleSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_vertical_hideSubtitleSwitch);
        hideSubtitleSwitch.setOnCheckedChangeListener(new HideSubtitleSwitchCheckListener());

        AppCompatSpinner subtitleDataSpinner
                = (AppCompatSpinner) findViewById(R.id.activity_create_widget_clock_day_vertical_subtitleDataSpinner);
        subtitleDataSpinner.setOnItemSelectedListener(new SubtitleDataSpinnerSelectedListener());
        subtitleDataSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subtitleData));

        this.blackTextSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_vertical_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = (Button) findViewById(R.id.activity_create_widget_clock_day_vertical_doneButton);
        doneButton.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshWidgetView(Weather weather) {
        if (weather == null) {
            return;
        }
        getLocationNow().weather = weather;
        String iconStyle = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(
                        getString(R.string.key_widget_icon_style),
                        "material");

        int imageId = WeatherHelper.getWidgetNotificationIcon(
                weather.realTime.weatherKind,
                TimeManager.getInstance(this).getDayTime(this, weather, false).isDayTime(),
                iconStyle,
                blackTextSwitch.isChecked());
        Glide.with(this)
                .load(imageId)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(widgetIcon);

        switch (viewTypeValueNow) {
            case "rectangle":
                String[] texts = WidgetUtils.buildWidgetDayStyleText(weather, isFahrenheit());
                widgetTitle.setText(texts[0]);
                widgetSubtitle.setText(texts[1]);
                break;

            case "symmetry":
                widgetTitle.setText(weather.base.city + "\n" + ValueUtils.buildCurrentTemp(weather.realTime.temp, true, isFahrenheit()));
                widgetSubtitle.setText(weather.realTime.weather + "\n" + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, isFahrenheit()));
                break;

            case "tile":
                widgetTitle.setText(weather.realTime.weather + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, isFahrenheit()));
                widgetSubtitle.setText(ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, isFahrenheit()));
                break;

            case "mini":
                widgetTitle.setText(weather.realTime.weather);
                widgetSubtitle.setText(ValueUtils.buildCurrentTemp(weather.realTime.temp, false, isFahrenheit()));
                break;
        }
        setTimeText(weather, subtitleDataValueNow);
    }

    /** <br> UI. */

    @SuppressLint("InflateParams")
    private void setWidgetView(boolean init) {
        if (init) {
            this.widgetViews = new View[] {
                    LayoutInflater.from(this).inflate(R.layout.widget_clock_day_rectangle, null),
                    LayoutInflater.from(this).inflate(R.layout.widget_clock_day_symmetry, null),
                    LayoutInflater.from(this).inflate(R.layout.widget_clock_day_tile, null),
                    LayoutInflater.from(this).inflate(R.layout.widget_clock_day_mini, null)};
            for (View widgetView : widgetViews) {
                ((ViewGroup) findViewById(R.id.activity_create_widget_clock_day_vertical_widgetContainer)).addView(widgetView);
            }
        }
        for (View widgetView : widgetViews) {
            widgetView.setVisibility(View.GONE);
        }

        switch (viewTypeValueNow) {
            case "rectangle":
                this.widgetViews[0].setVisibility(View.VISIBLE);

                this.widgetCard = (ImageView) widgetViews[0].findViewById(R.id.widget_clock_day_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = (ImageView) widgetViews[0].findViewById(R.id.widget_clock_day_icon);
                this.widgetClock = (TextClock) widgetViews[0].findViewById(R.id.widget_clock_day_clock);
                this.widgetTitle = (TextView) widgetViews[0].findViewById(R.id.widget_clock_day_title);
                this.widgetSubtitle = (TextView) widgetViews[0].findViewById(R.id.widget_clock_day_subtitle);
                this.widgetTime = (TextView) widgetViews[0].findViewById(R.id.widget_clock_day_time);
                break;

            case "symmetry":
                widgetViews[1].setVisibility(View.VISIBLE);

                this.widgetCard = (ImageView) widgetViews[1].findViewById(R.id.widget_clock_day_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = (ImageView) widgetViews[1].findViewById(R.id.widget_clock_day_icon);
                this.widgetClock = (TextClock) widgetViews[1].findViewById(R.id.widget_clock_day_clock);
                this.widgetTitle = (TextView) widgetViews[1].findViewById(R.id.widget_clock_day_title);
                this.widgetSubtitle = (TextView) widgetViews[1].findViewById(R.id.widget_clock_day_subtitle);
                this.widgetTime = (TextView) widgetViews[1].findViewById(R.id.widget_clock_day_time);
                break;

            case "tile":
                widgetViews[2].setVisibility(View.VISIBLE);

                this.widgetCard = (ImageView) widgetViews[2].findViewById(R.id.widget_clock_day_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = (ImageView) widgetViews[2].findViewById(R.id.widget_clock_day_icon);
                this.widgetClock = (TextClock) widgetViews[2].findViewById(R.id.widget_clock_day_clock);
                this.widgetTitle = (TextView) widgetViews[2].findViewById(R.id.widget_clock_day_title);
                this.widgetSubtitle = (TextView) widgetViews[2].findViewById(R.id.widget_clock_day_subtitle);
                this.widgetTime = (TextView) widgetViews[2].findViewById(R.id.widget_clock_day_time);
                break;

            case "mini":
                widgetViews[3].setVisibility(View.VISIBLE);

                this.widgetCard = (ImageView) widgetViews[3].findViewById(R.id.widget_clock_day_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = (ImageView) widgetViews[3].findViewById(R.id.widget_clock_day_icon);
                this.widgetClock = (TextClock) widgetViews[3].findViewById(R.id.widget_clock_day_clock);
                this.widgetTitle = (TextView) widgetViews[3].findViewById(R.id.widget_clock_day_title);
                this.widgetSubtitle = (TextView) widgetViews[3].findViewById(R.id.widget_clock_day_subtitle);
                this.widgetTime = null;
                break;
        }
    }

    private void setTimeText(Weather weather, String subtitleDataValue) {
        if (weather == null || widgetTime == null) {
            return;
        }
        switch (subtitleDataValue) {
            case "time":
                switch (viewTypeValueNow) {
                    case "rectangle":
                        widgetTime.setText(weather.base.city + " " + weather.base.time);
                        break;

                    case "symmetry":
                        widgetTime.setText(weather.dailyList.get(0).week + " " + weather.base.time);
                        break;

                    case "tile":
                        widgetTime.setText(weather.base.city + " " + weather.dailyList.get(0).week + " " + weather.base.time);
                        break;
                }
                break;

            case "aqi":
                if (weather.aqi != null) {
                    widgetTime.setText(weather.aqi.quality + " (" + weather.aqi.aqi + ")");
                }
                break;

            case "wind":
                widgetTime.setText(weather.realTime.windLevel + " (" + weather.realTime.windDir + weather.realTime.windSpeed + ")");
                break;

            default:
                widgetTime.setText(
                        getString(R.string.feels_like) + " "
                                + ValueUtils.buildAbbreviatedCurrentTemp(
                                weather.realTime.sensibleTemp, GeometricWeather.getInstance().isFahrenheit()));
                break;
        }
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_clock_day_vertical_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_clock_day_vertical_setting),
                        MODE_PRIVATE)
                        .edit();
                editor.putString(getString(R.string.key_view_type), viewTypeValueNow);
                editor.putBoolean(getString(R.string.key_show_card), showCardSwitch.isChecked());
                editor.putBoolean(getString(R.string.key_hide_subtitle), hideSubtitleSwitch.isChecked());
                editor.putString(getString(R.string.key_subtitle_data), subtitleDataValueNow);
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

                ServiceHelper.startupService(this, PollingService.FORCE_REFRESH_TYPE_NORMAL_VIEW);
                finish();
                break;
        }
    }

    // on item selected listener.

    private class ViewTypeSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (!viewTypeValueNow.equals(viewTypeValues[i])) {
                viewTypeValueNow = viewTypeValues[i];
                setWidgetView(false);
                refreshWidgetView(getLocationNow().weather);

                if (showCardSwitch.isChecked()) {
                    widgetCard.setVisibility(View.VISIBLE);
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                    widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                    widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                    if (widgetTime != null) {
                        widgetTime.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                    }
                } else {
                    widgetCard.setVisibility(View.GONE);
                    if (!blackTextSwitch.isChecked()) {
                        widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                        widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                        widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                        if (widgetTime != null) {
                            widgetTime.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                        }
                    }
                }

                if (widgetTime != null) {
                    widgetTime.setVisibility(hideSubtitleSwitch.isChecked() ? View.GONE : View.VISIBLE);
                }

                if (blackTextSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                    widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                    widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                    if (widgetTime != null) {
                        widgetTime.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                    }
                } else {
                    if (!showCardSwitch.isChecked()) {
                        widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                        widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                        widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                        if (widgetTime != null) {
                            widgetTime.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                        }
                    }
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // do nothing.
        }
    }

    private class SubtitleDataSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (!subtitleDataValueNow.equals(subtitleDataValues[i])) {
                subtitleDataValueNow = subtitleDataValues[i];
                if (widgetTime != null) {
                    setTimeText(getLocationNow().weather, subtitleDataValueNow);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // do nothing.
        }
    }

    // on check changed listener(switch).

    private class ShowCardSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetCard.setVisibility(View.VISIBLE);
                widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                if (widgetTime != null) {
                    widgetTime.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                }
            } else {
                widgetCard.setVisibility(View.GONE);
                if (!blackTextSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                    widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                    widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                    if (widgetTime != null) {
                        widgetTime.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                    }
                }
            }
        }
    }

    private class HideSubtitleSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (widgetTime != null) {
                widgetTime.setVisibility(hideSubtitleSwitch.isChecked() ? View.GONE : View.VISIBLE);
            }
        }
    }

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                if (widgetTime != null) {
                    widgetTime.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextDark));
                }
            } else {
                if (!showCardSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                    widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                    widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                    if (widgetTime != null) {
                        widgetTime.setTextColor(ContextCompat.getColor(CreateWidgetClockDayVerticalActivity.this, R.color.colorTextLight));
                    }
                }
            }
        }
    }
}