package wangdaye.com.geometricweather.remoteviews.config;

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
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.background.BackgroundManager;
import wangdaye.com.geometricweather.remoteviews.presenter.DayWeekWidgetIMP;

/**
 * Day week widget config activity.
 * */

public class DayWeekWidgetConfigActivity extends AbstractWidgetConfigActivity
        implements View.OnClickListener {

    private FrameLayout widgetContainer;

    private CoordinatorLayout container;

    private Switch showCardSwitch;
    private Switch hideSubtitleSwitch;
    private Switch blackTextSwitch;

    private String viewTypeValueNow;
    private String[] viewTypes;
    private String[] viewTypeValues;

    private String subtitleDataValueNow;
    private String[] subtitleData;
    private String[] subtitleDataValues;

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_create_widget_day_week);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @Override
    public void initData() {
        super.initData();

        this.viewTypeValueNow = "rectangle";
        this.viewTypes = new String[] {
                getResources().getStringArray(R.array.widget_styles)[0],
                getResources().getStringArray(R.array.widget_styles)[1],
                getResources().getStringArray(R.array.widget_styles)[2]};
        this.viewTypeValues = new String[] {
                getResources().getStringArray(R.array.widget_style_values)[0],
                getResources().getStringArray(R.array.widget_style_values)[1],
                getResources().getStringArray(R.array.widget_style_values)[2]};

        this.subtitleDataValueNow = "time";
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
    public void initView() {
        ImageView wallpaper = findViewById(R.id.activity_create_widget_day_week_wall);
        bindWallpaper(wallpaper);

        this.widgetContainer = findViewById(R.id.activity_create_widget_day_week_widgetContainer);

        this.container = findViewById(R.id.activity_create_widget_day_week_container);

        AppCompatSpinner viewTypeSpinner = findViewById(R.id.activity_create_widget_day_week_styleSpinner);
        viewTypeSpinner.setOnItemSelectedListener(new ViewTypeSpinnerSelectedListener());
        viewTypeSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, viewTypes)
        );

        this.showCardSwitch = findViewById(R.id.activity_create_widget_day_week_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.hideSubtitleSwitch = findViewById(R.id.activity_create_widget_day_week_hideSubtitleSwitch);
        hideSubtitleSwitch.setOnCheckedChangeListener(new HideRefreshTimeSwitchCheckListener());

        AppCompatSpinner subtitleDataSpinner = findViewById(R.id.activity_create_widget_day_week_subtitleDataSpinner);
        subtitleDataSpinner.setOnItemSelectedListener(new SubtitleDataSpinnerSelectedListener());
        subtitleDataSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subtitleData)
        );

        this.blackTextSwitch = findViewById(R.id.activity_create_widget_day_week_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = findViewById(R.id.activity_create_widget_day_week_doneButton);
        doneButton.setOnClickListener(this);
    }

    @Override
    public ViewGroup getWidgetContainer() {
        return widgetContainer;
    }

    @Override
    public RemoteViews getRemoteViews() {
        return DayWeekWidgetIMP.getRemoteViews(
                this, getLocationNow(), getLocationNow().weather,
                viewTypeValueNow, showCardSwitch.isChecked(), blackTextSwitch.isChecked(),
                hideSubtitleSwitch.isChecked(), subtitleDataValueNow
        );
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_day_week_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_day_week_setting),
                        MODE_PRIVATE
                ).edit();
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

    // on item selected listener.

    private class ViewTypeSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (!viewTypeValueNow.equals(viewTypeValues[i])) {
                viewTypeValueNow = viewTypeValues[i];
                updateHostView();
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
                updateHostView();
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
            updateHostView();
        }
    }

    private class HideRefreshTimeSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

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
}