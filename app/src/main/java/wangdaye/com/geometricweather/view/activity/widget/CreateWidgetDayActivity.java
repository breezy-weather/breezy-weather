package wangdaye.com.geometricweather.view.activity.widget;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
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
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import wangdaye.com.geometricweather.basic.GeoWidgetConfigActivity;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Create widget day activity.
 * */

public class CreateWidgetDayActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    // widget
    private View[] widgetViews;
    private ImageView widgetCard;
    private ImageView widgetIcon;
    private TextView widgetTitle;
    private TextView widgetSubtitle;
    private TextView widgetTime;

    private CoordinatorLayout container;

    private Switch showCardSwitch;
    private Switch hideRefreshTimeSwitch;
    private Switch blackTextSwitch;

    // data
    private String viewTypeValueNow = "rectangle";
    private String[] viewTypes;
    private String[] viewTypeValues;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_day);
    }

    @Override
    public void initData() {
        super.initData();
        this.viewTypes = getResources().getStringArray(R.array.widget_styles);
        this.viewTypeValues = getResources().getStringArray(R.array.widget_style_values);
    }

    @Override
    public void initWidget() {
        setWidgetView(true);

        ImageView wallpaper = (ImageView) findViewById(R.id.activity_create_widget_day_wall);
        wallpaper.setImageDrawable(WallpaperManager.getInstance(this).getDrawable());

        this.container = (CoordinatorLayout) findViewById(R.id.activity_create_widget_day_container);

        AppCompatSpinner spinner = (AppCompatSpinner) findViewById(R.id.activity_create_widget_day_styleSpinner);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, viewTypes));

        this.showCardSwitch = (Switch) findViewById(R.id.activity_create_widget_day_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.hideRefreshTimeSwitch = (Switch) findViewById(R.id.activity_create_widget_day_hideRefreshTimeSwitch);
        hideRefreshTimeSwitch.setOnCheckedChangeListener(new HideRefreshTimeSwitchCheckListener());

        this.blackTextSwitch = (Switch) findViewById(R.id.activity_create_widget_day_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = (Button) findViewById(R.id.activity_create_widget_day_doneButton);
        doneButton.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
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

        switch (viewTypeValueNow) {
            case "rectangle":
                String[] texts = WidgetUtils.buildWidgetDayStyleText(weather, isFahrenheit());
                widgetTitle.setText(texts[0]);
                widgetSubtitle.setText(texts[1]);
                widgetTime.setText(weather.base.city + " " + weather.base.time);
                break;

            case "symmetry":
                widgetTitle.setText(weather.base.city + "\n" + ValueUtils.buildCurrentTemp(weather.realTime.temp, true, isFahrenheit()));
                widgetSubtitle.setText(weather.realTime.weather + "\n" + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, isFahrenheit()));
                widgetTime.setText(weather.dailyList.get(0).week + " " + weather.base.time);
                break;

            case "tile":
                widgetTitle.setText(weather.realTime.weather + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, isFahrenheit()));
                widgetSubtitle.setText(ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, isFahrenheit()));
                widgetTime.setText(weather.base.city + " " + weather.dailyList.get(0).week + " " + weather.base.time);
                break;

            case "mini":
                widgetTitle.setText(ValueUtils.buildCurrentTemp(weather.realTime.temp, false, isFahrenheit()));
                widgetTime.setText(weather.base.city + " " + weather.dailyList.get(0).week + " " + weather.base.time);
                break;

            case "pixel":
                widgetTitle.setText(ValueUtils.buildCurrentTemp(weather.realTime.temp, false, isFahrenheit()));
                widgetSubtitle.setText(weather.dailyList.get(0).date.split("-", 2)[1] + " " + weather.dailyList.get(0).week);
                break;
        }
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    /** <br> UI. */

    @SuppressLint("InflateParams")
    private void setWidgetView(boolean init) {
        if (init) {
            this.widgetViews = new View[] {
                    LayoutInflater.from(this).inflate(R.layout.widget_day_rectangle, null),
                    LayoutInflater.from(this).inflate(R.layout.widget_day_symmetry, null),
                    LayoutInflater.from(this).inflate(R.layout.widget_day_tile, null),
                    LayoutInflater.from(this).inflate(R.layout.widget_day_mini, null),
                    LayoutInflater.from(this).inflate(R.layout.widget_day_pixel, null)};
            for (View widgetView : widgetViews) {
                ((ViewGroup) findViewById(R.id.activity_create_widget_day_widgetContainer)).addView(widgetView);
            }
        }
        for (View widgetView : widgetViews) {
            widgetView.setVisibility(View.GONE);
        }

        switch (viewTypeValueNow) {
            case "rectangle":
                this.widgetViews[0].setVisibility(View.VISIBLE);

                this.widgetCard = (ImageView) widgetViews[0].findViewById(R.id.widget_day_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = (ImageView) widgetViews[0].findViewById(R.id.widget_day_icon);
                this.widgetTitle = (TextView) widgetViews[0].findViewById(R.id.widget_day_title);
                this.widgetSubtitle = (TextView) widgetViews[0].findViewById(R.id.widget_day_subtitle);
                this.widgetTime = (TextView) widgetViews[0].findViewById(R.id.widget_day_time);
                break;

            case "symmetry":
                this.widgetViews[1].setVisibility(View.VISIBLE);

                this.widgetCard = (ImageView) widgetViews[1].findViewById(R.id.widget_day_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = (ImageView) widgetViews[1].findViewById(R.id.widget_day_icon);
                this.widgetTitle = (TextView) widgetViews[1].findViewById(R.id.widget_day_title);
                this.widgetSubtitle = (TextView) widgetViews[1].findViewById(R.id.widget_day_subtitle);
                this.widgetTime = (TextView) widgetViews[1].findViewById(R.id.widget_day_time);
                break;

            case "tile":
                this.widgetViews[2].setVisibility(View.VISIBLE);

                this.widgetCard = (ImageView) widgetViews[2].findViewById(R.id.widget_day_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = (ImageView) widgetViews[2].findViewById(R.id.widget_day_icon);
                this.widgetTitle = (TextView) widgetViews[2].findViewById(R.id.widget_day_title);
                this.widgetSubtitle = (TextView) widgetViews[2].findViewById(R.id.widget_day_subtitle);
                this.widgetTime = (TextView) widgetViews[2].findViewById(R.id.widget_day_time);
                break;

            case "mini":
                this.widgetViews[3].setVisibility(View.VISIBLE);

                this.widgetCard = (ImageView) widgetViews[3].findViewById(R.id.widget_day_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = (ImageView) widgetViews[3].findViewById(R.id.widget_day_icon);
                this.widgetTitle = (TextView) widgetViews[3].findViewById(R.id.widget_day_title);
                this.widgetSubtitle = null;
                this.widgetTime = (TextView) widgetViews[3].findViewById(R.id.widget_day_time);
                break;

            case "pixel":
                this.widgetViews[4].setVisibility(View.VISIBLE);

                this.widgetCard = null;

                this.widgetIcon = (ImageView) widgetViews[4].findViewById(R.id.widget_day_icon);
                this.widgetTitle = (TextView) widgetViews[4].findViewById(R.id.widget_day_title);
                this.widgetSubtitle = (TextView) widgetViews[4].findViewById(R.id.widget_day_subtitle);
                this.widgetTime = null;
                break;
        }
    }

    /** <br> interface. */

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_day_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_day_setting),
                        MODE_PRIVATE)
                        .edit();
                editor.putString(getString(R.string.key_view_type), viewTypeValueNow);
                editor.putBoolean(getString(R.string.key_show_card), showCardSwitch.isChecked());
                editor.putBoolean(getString(R.string.key_hide_refresh_time), hideRefreshTimeSwitch.isChecked());
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

    // on item selected listener.

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (!viewTypeValueNow.equals(viewTypeValues[i])) {
            viewTypeValueNow = viewTypeValues[i];
            setWidgetView(false);
            refreshWidgetView(getLocationNow().weather);

            if (showCardSwitch.isChecked()) {
                if (widgetCard != null) {
                    widgetCard.setVisibility(View.VISIBLE);
                }
                widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
                if (widgetSubtitle != null) {
                    widgetSubtitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
                }
            } else {
                if (widgetCard != null) {
                    widgetCard.setVisibility(View.GONE);
                }
                if (!blackTextSwitch.isChecked()) {
                    widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
                    if (widgetSubtitle != null) {
                        widgetSubtitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
                    }
                }
            }

            if (widgetTime != null) {
                widgetTime.setVisibility(hideRefreshTimeSwitch.isChecked() ? View.GONE : View.VISIBLE);
            }

            if (blackTextSwitch.isChecked()) {
                widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
                if (widgetSubtitle != null) {
                    widgetSubtitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
                }
            } else {
                if (!showCardSwitch.isChecked()) {
                    widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
                    if (widgetSubtitle != null) {
                        widgetSubtitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
                    }
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing.
    }

    // on check changed listener(switch).

    private class ShowCardSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (widgetCard != null) {
                    widgetCard.setVisibility(View.VISIBLE);
                }
                widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetDayActivity.this, R.color.colorTextDark));
                if (widgetSubtitle != null) {
                    widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetDayActivity.this, R.color.colorTextDark));
                }
            } else {
                if (widgetCard != null) {
                    widgetCard.setVisibility(View.GONE);
                }
                if (!blackTextSwitch.isChecked()) {
                    widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetDayActivity.this, R.color.colorTextLight));
                    if (widgetSubtitle != null) {
                        widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetDayActivity.this, R.color.colorTextLight));
                    }
                }
            }
        }
    }

    private class HideRefreshTimeSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (widgetTime != null) {
                widgetTime.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            }
        }
    }

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetDayActivity.this, R.color.colorTextDark));
                if (widgetSubtitle != null) {
                    widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetDayActivity.this, R.color.colorTextDark));
                }
            } else {
                if (!showCardSwitch.isChecked()) {
                    widgetTitle.setTextColor(ContextCompat.getColor(CreateWidgetDayActivity.this, R.color.colorTextLight));
                    if (widgetSubtitle != null) {
                        widgetSubtitle.setTextColor(ContextCompat.getColor(CreateWidgetDayActivity.this, R.color.colorTextLight));
                    }
                }
            }
        }
    }
}