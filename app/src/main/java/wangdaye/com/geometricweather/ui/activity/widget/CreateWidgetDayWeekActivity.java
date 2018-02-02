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
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.remoteView.WidgetDayWeekUtils;

/**
 * Create widget day week activity.
 * */

public class CreateWidgetDayWeekActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener {

    private View[] widgetViews;
    private ImageView widgetCard;
    private ImageView widgetIcon;
    private TextView widgetTitle;
    private TextView widgetSubtitle;
    private TextView widgetTime;
    private TextView[] widgetWeeks;
    private ImageView[] widgetIcons;
    private TextView[] widgetTemps;

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
        setContentView(R.layout.activity_create_widget_day_week);
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
                getResources().getStringArray(R.array.widget_styles)[2]};
        this.viewTypeValues = new String[] {
                getResources().getStringArray(R.array.widget_style_values)[0],
                getResources().getStringArray(R.array.widget_style_values)[1],
                getResources().getStringArray(R.array.widget_style_values)[2]};
        int length = LanguageUtils.getLanguageCode(this).startsWith("zh") ? 5 : 4;
        this.subtitleData = new String[length];
        this.subtitleDataValues = new String[length];
        String[] data = getResources().getStringArray(R.array.subtitle_data);
        String[] dataValues = getResources().getStringArray(R.array.subtitle_data_values);
        for (int i = 0; i < length; i ++) {
            subtitleData[i] = data[i];
            subtitleDataValues[i] = dataValues[i];
        }
    }

    @Override
    public void initWidget() {
        setWidgetView(true);

        ImageView wallpaper = findViewById(R.id.activity_create_widget_day_week_wall);
        bindWallpaper(wallpaper);

        this.container = findViewById(R.id.activity_create_widget_day_week_container);

        AppCompatSpinner viewTypeSpinner = findViewById(R.id.activity_create_widget_day_week_styleSpinner);
        viewTypeSpinner.setOnItemSelectedListener(new ViewTypeSpinnerSelectedListener());
        viewTypeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, viewTypes));

        this.showCardSwitch = findViewById(R.id.activity_create_widget_day_week_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.hideSubtitleSwitch = findViewById(R.id.activity_create_widget_day_week_hideSubtitleSwitch);
        hideSubtitleSwitch.setOnCheckedChangeListener(new HideRefreshTimeSwitchCheckListener());

        AppCompatSpinner subtitleDataSpinner = findViewById(R.id.activity_create_widget_day_week_subtitleDataSpinner);
        subtitleDataSpinner.setOnItemSelectedListener(new SubtitleDataSpinnerSelectedListener());
        subtitleDataSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subtitleData));

        this.blackTextSwitch = findViewById(R.id.activity_create_widget_day_week_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = findViewById(R.id.activity_create_widget_day_week_doneButton);
        doneButton.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshWidgetView(Weather weather) {
        if (weather == null) {
            return;
        }

        String iconStyle = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(
                        getString(R.string.key_widget_icon_style),
                        "material");
        boolean dayTime = TimeManager.getInstance(this).getDayTime(this, weather, false).isDayTime();

        int imageId = WidgetDayWeekUtils.getWeatherIconId(
                weather,
                TimeManager.getInstance(this).getDayTime(this, weather, false).isDayTime(),
                iconStyle,
                blackTextSwitch.isChecked());
        Glide.with(this)
                .load(imageId)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(widgetIcon);

        widgetTitle.setText(WidgetDayWeekUtils.getTitleText(weather, viewTypeValueNow, isFahrenheit()));
        widgetSubtitle.setText(WidgetDayWeekUtils.getSubtitleText(weather, viewTypeValueNow, isFahrenheit()));
        widgetTime.setText(WidgetDayWeekUtils.getTimeText(this, weather, viewTypeValueNow, subtitleDataValueNow));

        for (int i = 0; i < 5; i ++) {
            widgetWeeks[i].setText(WidgetDayWeekUtils.getWeek(this, weather, i));
            widgetTemps[i].setText(WidgetDayWeekUtils.getTemp(weather, isFahrenheit(), i));
            Glide.with(this)
                    .load(
                            WidgetDayWeekUtils.getIconId(
                                    weather, dayTime, iconStyle, blackTextSwitch.isChecked(), i))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(widgetIcons[i]);
        }

        if (showCardSwitch.isChecked() || blackTextSwitch.isChecked()) {
            if (showCardSwitch.isChecked()) {
                widgetCard.setVisibility(View.VISIBLE);
            } else {
                widgetCard.setVisibility(View.GONE);
            }
            widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetSubtitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            widgetTime.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            for (int j = 0; j < 5; j ++) {
                widgetWeeks[j].setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
                widgetTemps[j].setTextColor(ContextCompat.getColor(this, R.color.colorTextDark));
            }
        } else {
            widgetCard.setVisibility(View.GONE);
            widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetSubtitle.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            widgetTime.setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            for (int j = 0; j < 5; j ++) {
                widgetWeeks[j].setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
                widgetTemps[j].setTextColor(ContextCompat.getColor(this, R.color.colorTextLight));
            }
        }

        if (hideSubtitleSwitch.isChecked()) {
            widgetTime.setVisibility(View.GONE);
        } else {
            widgetTime.setVisibility(View.VISIBLE);
        }
    }

    /** <br> UI. */

    @SuppressLint("InflateParams")
    private void setWidgetView(boolean init) {
        if (init) {
            this.widgetViews = new View[] {
                    LayoutInflater.from(this).inflate(R.layout.widget_day_week_rectangle, null),
                    LayoutInflater.from(this).inflate(R.layout.widget_day_week_symmetry, null),
                    LayoutInflater.from(this).inflate(R.layout.widget_day_week_tile, null),};
            for (View widgetView : widgetViews) {
                ((ViewGroup) findViewById(R.id.activity_create_widget_day_week_widgetContainer)).addView(widgetView);
            }
        }
        for (View widgetView : widgetViews) {
            widgetView.setVisibility(View.GONE);
        }

        switch (viewTypeValueNow) {
            case "rectangle":
                this.widgetViews[0].setVisibility(View.VISIBLE);

                this.widgetCard = widgetViews[0].findViewById(R.id.widget_day_week_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = widgetViews[0].findViewById(R.id.widget_day_week_icon);
                this.widgetTitle = widgetViews[0].findViewById(R.id.widget_day_week_title);
                this.widgetSubtitle = widgetViews[0].findViewById(R.id.widget_day_week_subtitle);
                this.widgetTime = widgetViews[0].findViewById(R.id.widget_day_week_time);

                this.widgetWeeks = new TextView[] {
                        widgetViews[0].findViewById(R.id.widget_day_week_week_1),
                        widgetViews[0].findViewById(R.id.widget_day_week_week_2),
                        widgetViews[0].findViewById(R.id.widget_day_week_week_3),
                        widgetViews[0].findViewById(R.id.widget_day_week_week_4),
                        widgetViews[0].findViewById(R.id.widget_day_week_week_5)};
                this.widgetIcons = new ImageView[] {
                        widgetViews[0].findViewById(R.id.widget_day_week_icon_1),
                        widgetViews[0].findViewById(R.id.widget_day_week_icon_2),
                        widgetViews[0].findViewById(R.id.widget_day_week_icon_3),
                        widgetViews[0].findViewById(R.id.widget_day_week_icon_4),
                        widgetViews[0].findViewById(R.id.widget_day_week_icon_5)};
                this.widgetTemps = new TextView[] {
                        widgetViews[0].findViewById(R.id.widget_day_week_temp_1),
                        widgetViews[0].findViewById(R.id.widget_day_week_temp_2),
                        widgetViews[0].findViewById(R.id.widget_day_week_temp_3),
                        widgetViews[0].findViewById(R.id.widget_day_week_temp_4),
                        widgetViews[0].findViewById(R.id.widget_day_week_temp_5)};
                break;

            case "symmetry":
                widgetViews[1].setVisibility(View.VISIBLE);

                this.widgetCard = widgetViews[1].findViewById(R.id.widget_day_week_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = widgetViews[1].findViewById(R.id.widget_day_week_icon);
                this.widgetTitle = widgetViews[1].findViewById(R.id.widget_day_week_title);
                this.widgetSubtitle = widgetViews[1].findViewById(R.id.widget_day_week_subtitle);
                this.widgetTime = widgetViews[1].findViewById(R.id.widget_day_week_time);

                this.widgetWeeks = new TextView[] {
                        widgetViews[1].findViewById(R.id.widget_day_week_week_1),
                        widgetViews[1].findViewById(R.id.widget_day_week_week_2),
                        widgetViews[1].findViewById(R.id.widget_day_week_week_3),
                        widgetViews[1].findViewById(R.id.widget_day_week_week_4),
                        widgetViews[1].findViewById(R.id.widget_day_week_week_5)};
                this.widgetIcons = new ImageView[] {
                        widgetViews[1].findViewById(R.id.widget_day_week_icon_1),
                        widgetViews[1].findViewById(R.id.widget_day_week_icon_2),
                        widgetViews[1].findViewById(R.id.widget_day_week_icon_3),
                        widgetViews[1].findViewById(R.id.widget_day_week_icon_4),
                        widgetViews[1].findViewById(R.id.widget_day_week_icon_5)};
                this.widgetTemps = new TextView[] {
                        widgetViews[1].findViewById(R.id.widget_day_week_temp_1),
                        widgetViews[1].findViewById(R.id.widget_day_week_temp_2),
                        widgetViews[1].findViewById(R.id.widget_day_week_temp_3),
                        widgetViews[1].findViewById(R.id.widget_day_week_temp_4),
                        widgetViews[1].findViewById(R.id.widget_day_week_temp_5)};
                break;

            case "tile":
                widgetViews[2].setVisibility(View.VISIBLE);

                this.widgetCard = widgetViews[2].findViewById(R.id.widget_day_week_card);
                widgetCard.setVisibility(View.GONE);

                this.widgetIcon = widgetViews[2].findViewById(R.id.widget_day_week_icon);
                this.widgetTitle = widgetViews[2].findViewById(R.id.widget_day_week_title);
                this.widgetSubtitle = widgetViews[2].findViewById(R.id.widget_day_week_subtitle);
                this.widgetTime = widgetViews[2].findViewById(R.id.widget_day_week_time);

                this.widgetWeeks = new TextView[] {
                        widgetViews[2].findViewById(R.id.widget_day_week_week_1),
                        widgetViews[2].findViewById(R.id.widget_day_week_week_2),
                        widgetViews[2].findViewById(R.id.widget_day_week_week_3),
                        widgetViews[2].findViewById(R.id.widget_day_week_week_4),
                        widgetViews[2].findViewById(R.id.widget_day_week_week_5)};
                this.widgetIcons = new ImageView[] {
                        widgetViews[2].findViewById(R.id.widget_day_week_icon_1),
                        widgetViews[2].findViewById(R.id.widget_day_week_icon_2),
                        widgetViews[2].findViewById(R.id.widget_day_week_icon_3),
                        widgetViews[2].findViewById(R.id.widget_day_week_icon_4),
                        widgetViews[2].findViewById(R.id.widget_day_week_icon_5)};
                this.widgetTemps = new TextView[] {
                        widgetViews[2].findViewById(R.id.widget_day_week_temp_1),
                        widgetViews[2].findViewById(R.id.widget_day_week_temp_2),
                        widgetViews[2].findViewById(R.id.widget_day_week_temp_3),
                        widgetViews[2].findViewById(R.id.widget_day_week_temp_4),
                        widgetViews[2].findViewById(R.id.widget_day_week_temp_5)};
                break;
        }
    }

    /** <br> interface. */

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_day_week_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_day_week_setting),
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

                ServiceHelper.resetNormalService(this, false, true);
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
                refreshWidgetView(getLocationNow().weather);
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
            refreshWidgetView(getLocationNow().weather);
        }
    }

    private class HideRefreshTimeSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

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