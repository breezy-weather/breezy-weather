package wangdaye.com.geometricweather.remoteviews.ui;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Switch;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.BackgroundManager;
import wangdaye.com.geometricweather.remoteviews.presenter.TextWidgetIMP;

/**
 * Text widget config activity.
 * */

public class TextWidgetConfigActivity extends AbstractWidgetConfigActivity
        implements View.OnClickListener {

    private FrameLayout widgetContainer;

    private CoordinatorLayout container;

    private Switch blackTextSwitch;

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_create_widget_text);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @Override
    public void initView() {
        ImageView wallpaper = findViewById(R.id.activity_create_widget_text_wall);
        bindWallpaper(wallpaper);

        this.widgetContainer = findViewById(R.id.activity_create_widget_text_widgetContainer);

        this.container = findViewById(R.id.activity_create_widget_text_container);
        
        this.blackTextSwitch = findViewById(R.id.activity_create_widget_text_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = findViewById(R.id.activity_create_widget_text_doneButton);
        doneButton.setOnClickListener(this);
    }

    @Override
    public ViewGroup getWidgetContainer() {
        return widgetContainer;
    }

    @Override
    public RemoteViews getRemoteViews() {
        return TextWidgetIMP.getRemoteViews(
                this, getLocationNow(), getLocationNow().weather, blackTextSwitch.isChecked());
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_text_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_text_setting),
                        MODE_PRIVATE
                ).edit();
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

    // on check changed listener(switch).

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            updateHostView();
        }
    }
}