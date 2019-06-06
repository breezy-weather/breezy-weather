package wangdaye.com.geometricweather.remoteviews.config;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Switch;

import com.xw.repo.BubbleSeekBar;

import org.jetbrains.annotations.NotNull;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.BackgroundManager;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Abstract widget config activity.
 * */

public abstract class AbstractWidgetConfigActivity extends GeoActivity
        implements WeatherHelper.OnRequestWeatherListener {

    protected ImageView wallpaper;
    protected FrameLayout widgetContainer;

    protected CoordinatorLayout container;
    protected RelativeLayout viewTypeContainer;
    protected RelativeLayout cardStyleContainer;
    protected RelativeLayout cardAlphaContainer;
    protected RelativeLayout hideSubtitleContainer;
    protected RelativeLayout subtitleDataContainer;
    protected RelativeLayout textColorContainer;
    protected RelativeLayout clockFontContainer;

    protected Location locationNow;

    protected WeatherHelper weatherHelper;
    protected boolean destroyed;

    protected String viewTypeValueNow;
    protected String[] viewTypes;
    protected String[] viewTypeValues;

    protected String cardStyleValueNow;
    protected String[] cardStyles;
    protected String[] cardStyleValues;

    protected int cardAlpha;

    protected boolean hideSubtitle;

    protected String subtitleDataValueNow;
    protected String[] subtitleData;
    protected String[] subtitleDataValues;

    protected String textColorValueNow;
    protected String[] textColors;
    protected String[] textColorValues;

    protected String clockFontValueNow;
    protected String[] clockFonts;
    protected String[] clockFontValues;

    private final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        initData();
        initView();
        updateHostView();

        if (locationNow.isLocal()) {
            if (locationNow.isUsable()) {
                weatherHelper.requestWeather(this, locationNow, this);
            } else {
                weatherHelper.requestWeather(this, Location.buildDefaultLocation(), this);
            }
        } else {
            weatherHelper.requestWeather(this, locationNow, this);
        }
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @Override
    @Nullable
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context,
                             @NonNull AttributeSet attrs) {
        if (name.equals("ImageView")) {
            return new ImageView(context, attrs);
        }
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull String name, @NonNull Context context,
                             @NonNull AttributeSet attrs) {
        if (name.equals("ImageView")) {
            return new ImageView(context, attrs);
        }
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;
        weatherHelper.cancel();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        // do nothing.
    }

    @CallSuper
    public void initData() {
        locationNow = DatabaseHelper.getInstance(this).readLocationList().get(0);
        locationNow.weather = DatabaseHelper.getInstance(this).readWeather(locationNow);
        locationNow.history = DatabaseHelper.getInstance(this).readHistory(locationNow.weather);
        
        weatherHelper = new WeatherHelper();
        destroyed = false;
        
        Resources res = getResources();
        
        this.viewTypeValueNow = "rectangle";
        this.viewTypes = res.getStringArray(R.array.widget_styles);
        this.viewTypeValues = res.getStringArray(R.array.widget_style_values);

        this.cardStyleValueNow = "none";
        this.cardStyles = res.getStringArray(R.array.widget_card_styles);
        this.cardStyleValues = res.getStringArray(R.array.widget_card_style_values);

        this.cardAlpha = 100;

        this.hideSubtitle = false;

        this.subtitleDataValueNow = "time";
        int length = LanguageUtils.getLanguageCode(this).startsWith("zh") ? 5 : 4;
        this.subtitleData = new String[length];
        this.subtitleDataValues = new String[length];
        String[] data = res.getStringArray(R.array.subtitle_data);
        String[] dataValues = res.getStringArray(R.array.subtitle_data_values);
        for (int i = 0; i < length; i ++) {
            subtitleData[i] = data[i];
            subtitleDataValues[i] = dataValues[i];
        }

        this.textColorValueNow = "light";
        this.textColors = res.getStringArray(R.array.widget_text_colors);
        this.textColorValues = res.getStringArray(R.array.widget_text_color_values);

        this.clockFontValueNow = "light";
        this.clockFonts = res.getStringArray(R.array.clock_font);
        this.clockFontValues = res.getStringArray(R.array.clock_font_values);
    }

    @CallSuper
    public void initView() {
        ImageView wallpaper = findViewById(R.id.activity_widget_config_wall);
        bindWallpaper(wallpaper);

        this.widgetContainer = findViewById(R.id.activity_widget_config_widgetContainer);

        this.container = findViewById(R.id.activity_widget_config_container);

        this.viewTypeContainer = findViewById(R.id.activity_widget_config_viewStyleContainer);
        AppCompatSpinner viewTypeSpinner = findViewById(R.id.activity_widget_config_styleSpinner);
        viewTypeSpinner.setOnItemSelectedListener(new ViewTypeSpinnerSelectedListener());
        viewTypeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, viewTypes));

        this.cardStyleContainer = findViewById(R.id.activity_widget_config_showCardContainer);
        AppCompatSpinner cardStyleSpinner = findViewById(R.id.activity_widget_config_showCardSpinner);
        cardStyleSpinner.setOnItemSelectedListener(new CardStyleSpinnerSelectedListener());
        cardStyleSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cardStyles)
        );

        this.cardAlphaContainer = findViewById(R.id.activity_widget_config_cardAlphaContainer);
        BubbleSeekBar seekBar = findViewById(R.id.activity_widget_config_cardAlphaSeekBar);
        seekBar.setOnProgressChangedListener(new CardAlphaChangedListener());

        this.hideSubtitleContainer = findViewById(R.id.activity_widget_config_hideSubtitleContainer);
        Switch hideSubtitleSwitch = findViewById(R.id.activity_widget_config_hideSubtitleSwitch);
        hideSubtitleSwitch.setOnCheckedChangeListener(new HideSubtitleSwitchCheckListener());

        this.subtitleDataContainer = findViewById(R.id.activity_widget_config_subtitleDataContainer);
        AppCompatSpinner subtitleDataSpinner = findViewById(R.id.activity_widget_config_subtitleDataSpinner);
        subtitleDataSpinner.setOnItemSelectedListener(new SubtitleDataSpinnerSelectedListener());
        subtitleDataSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subtitleData)
        );

        this.textColorContainer = findViewById(R.id.activity_widget_config_blackTextContainer);
        AppCompatSpinner textStyleSpinner = findViewById(R.id.activity_widget_config_blackTextSpinner);
        textStyleSpinner.setOnItemSelectedListener(new TextStyleSpinnerSelectedListener());
        textStyleSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, textColors)
        );

        this.clockFontContainer = findViewById(R.id.activity_widget_config_clockFontContainer);
        AppCompatSpinner clockFontSpinner = findViewById(R.id.activity_widget_config_clockFontSpinner);
        clockFontSpinner.setOnItemSelectedListener(new ClockFontSpinnerSelectedListener());
        clockFontSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clockFonts)
        );

        Button doneButton = findViewById(R.id.activity_widget_config_doneButton);
        doneButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(
                    getSharedPreferencesName(),
                    MODE_PRIVATE
            ).edit();
            editor.putString(getString(R.string.key_view_type), viewTypeValueNow);
            editor.putString(getString(R.string.key_card_style), cardStyleValueNow);
            editor.putInt(getString(R.string.key_card_alpha), cardAlpha);
            editor.putBoolean(getString(R.string.key_hide_subtitle), hideSubtitle);
            editor.putString(getString(R.string.key_subtitle_data), subtitleDataValueNow);
            editor.putString(getString(R.string.key_text_color), textColorValueNow);
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
        });
    }

    public final void updateHostView() {
        widgetContainer.removeAllViews();

        View view = getRemoteViews().apply(getApplicationContext(), widgetContainer);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        widgetContainer.addView(view, params);
    }

    public abstract RemoteViews getRemoteViews();

    public Location getLocationNow() {
        return locationNow;
    }
    
    public abstract String getSharedPreferencesName();

    // interface.

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(@Nullable Weather weather, @Nullable History history,
                                      @NonNull Location requestLocation) {
        if (destroyed) {
            return;
        }
        if (weather == null) {
            requestWeatherFailed(requestLocation);
        } else {
            locationNow.weather = weather;
            updateHostView();
        }
    }

    @Override
    public void requestWeatherFailed(@NonNull Location requestLocation) {
        if (destroyed) {
            return;
        }
        locationNow.weather = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        updateHostView();
        SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));
    }

    public void bindWallpaper(ImageView imageView) {
        bindWallpaper(imageView, true);
    }

    private void bindWallpaper(ImageView imageView, boolean requestPermissionIfFailed) {
        try {
            WallpaperManager manager = WallpaperManager.getInstance(this);
            if (manager != null) {
                Drawable drawable = manager.getDrawable();
                if (drawable != null) {
                    imageView.setImageDrawable(drawable);
                }
            }
        } catch (Exception ignore) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestPermissionIfFailed) {
                requestReadExternalStoragePermission(imageView);
            }
        }
    }

    // permission.

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestReadExternalStoragePermission(ImageView imageView) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            wallpaper = imageView;
            this.requestPermissions(
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
            );
        } else {
            bindWallpaper(imageView, false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permission, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permission, grantResult);
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            bindWallpaper(wallpaper, false);
        }
    }

    // on check changed listener(switch).

    private class HideSubtitleSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            hideSubtitle = isChecked;
            updateHostView();
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

    private class CardStyleSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (!cardStyleValueNow.equals(cardStyleValues[i])) {
                cardStyleValueNow = cardStyleValues[i];
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

    private class TextStyleSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (!textColorValueNow.equals(textColorValues[i])) {
                textColorValueNow = textColorValues[i];
                updateHostView();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // do nothing.
        }
    }

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

    private class CardAlphaChangedListener extends BubbleSeekBar.OnProgressChangedListenerAdapter {
        @Override
        public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            if (cardAlpha != progress) {
                cardAlpha = progress;
                updateHostView();
            }
        }
    }
}
