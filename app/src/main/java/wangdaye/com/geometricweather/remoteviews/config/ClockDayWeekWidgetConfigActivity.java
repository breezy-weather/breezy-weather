package wangdaye.com.geometricweather.remoteviews.config;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.appcompat.widget.AppCompatSpinner;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Switch;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.BackgroundManager;
import wangdaye.com.geometricweather.remoteviews.presenter.ClockDayWeekWidgetIMP;

/**
 * Clock day week widget config activity.
 * */

public class ClockDayWeekWidgetConfigActivity extends AbstractWidgetConfigActivity
        implements View.OnClickListener {

    private FrameLayout widgetContainer;

    private CoordinatorLayout container;

    private Switch showCardSwitch;
    private Switch blackTextSwitch;

    private String clockFontValueNow;
    private String[] clockFonts;
    private String[] clockFontValues;

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_create_widget_clock_day_week);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @Override
    public void initData() {
        super.initData();
        this.clockFontValueNow = "light";
        this.clockFonts = getResources().getStringArray(R.array.clock_font);
        this.clockFontValues = getResources().getStringArray(R.array.clock_font_values);
    }

    @SuppressLint("InflateParams")
    @Override
    public void initView() {
        ImageView wallpaper = findViewById(R.id.activity_create_widget_clock_day_week_wall);
        bindWallpaper(wallpaper);

        this.widgetContainer = findViewById(R.id.activity_create_widget_clock_day_week_widgetContainer);

        this.container = findViewById(R.id.activity_create_widget_clock_day_week_container);

        this.showCardSwitch = findViewById(R.id.activity_create_widget_clock_day_week_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.blackTextSwitch = findViewById(R.id.activity_create_widget_clock_day_week_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        AppCompatSpinner clockFontSpinner = findViewById(R.id.activity_create_widget_clock_day_week_clockFontSpinner);
        clockFontSpinner.setOnItemSelectedListener(new ClockFontSpinnerSelectedListener());
        clockFontSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clockFonts)
        );

        Button doneButton = findViewById(R.id.activity_create_widget_clock_day_week_doneButton);
        doneButton.setOnClickListener(this);
    }

    @Override
    public ViewGroup getWidgetContainer() {
        return widgetContainer;
    }

    @Override
    public RemoteViews getRemoteViews() {
        return ClockDayWeekWidgetIMP.getRemoteViews(
                this, getLocationNow(), getLocationNow().weather,
                showCardSwitch.isChecked(), blackTextSwitch.isChecked(), clockFontValueNow
        );
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_clock_day_week_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_clock_day_week_setting),
                        MODE_PRIVATE
                ).edit();
                editor.putBoolean(getString(R.string.key_show_card), showCardSwitch.isChecked());
                editor.putBoolean(getString(R.string.key_black_text), blackTextSwitch.isChecked());
                editor.putString(getString(R.string.key_clock_font), clockFontValueNow);
                editor.apply();

                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                int appWidgetId = 0;
                if (extras != null) {
                    appWidgetId = extras.getInt(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID
                    );
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
            updateHostView();
        }
    }

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            updateHostView();
        }
    }

    // on item selected listener.

    private class ClockFontSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (!clockFontValueNow.equals(clockFontValues[i])) {
                clockFontValueNow = clockFontValues[i];
                updateHostView();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // do nothing.
        }
    }
}