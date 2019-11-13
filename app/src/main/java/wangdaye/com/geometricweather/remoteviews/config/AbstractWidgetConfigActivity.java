package wangdaye.com.geometricweather.remoteviews.config;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
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
import androidx.core.widget.NestedScrollView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Switch;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.xw.repo.BubbleSeekBar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.insets.FitBottomSystemBarNestedScrollView;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Abstract widget config activity.
 * */

public abstract class AbstractWidgetConfigActivity extends GeoActivity
        implements WeatherHelper.OnRequestWeatherListener {

    protected FrameLayout topContainer;
    protected ImageView wallpaper;
    protected FrameLayout widgetContainer;

    protected CoordinatorLayout container;
    protected NestedScrollView scrollView;
    protected RelativeLayout viewTypeContainer;
    protected RelativeLayout cardStyleContainer;
    protected RelativeLayout cardAlphaContainer;
    protected RelativeLayout hideSubtitleContainer;
    protected RelativeLayout subtitleDataContainer;
    protected RelativeLayout textColorContainer;
    protected RelativeLayout textSizeContainer;
    protected RelativeLayout clockFontContainer;

    private BottomSheetBehavior bottomSheetBehavior;
    private FitBottomSystemBarNestedScrollView bottomSheetScrollView;
    private TextInputLayout subtitleInputLayout;
    private TextInputEditText subtitleInputter;

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

    protected int textSize;

    protected String clockFontValueNow;
    protected String[] clockFonts;
    protected String[] clockFontValues;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        initData();
        initView();
        updateHostView();

        if (locationNow.isCurrentPosition()) {
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // do nothing.
    }

    @CallSuper
    public void initData() {
        locationNow = DatabaseHelper.getInstance(this).readLocationList().get(0);
        locationNow.setWeather(DatabaseHelper.getInstance(this).readWeather(locationNow));

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
        boolean chinese = SettingsOptionManager.getInstance(this).getLanguage().getCode().startsWith("zh");
        String[] data = res.getStringArray(R.array.subtitle_data);
        String[] dataValues = res.getStringArray(R.array.subtitle_data_values);
        if (chinese) {
            this.subtitleData = new String[] {
                    data[0], data[1], data[2], data[3], data[4], data[5]
            };
            this.subtitleDataValues = new String[] {
                    dataValues[0], dataValues[1], dataValues[2], dataValues[3], dataValues[4], dataValues[5]
            };
        } else {
            this.subtitleData = new String[] {
                    data[0], data[1], data[2], data[3], data[5]
            };
            this.subtitleDataValues = new String[] {
                    dataValues[0], dataValues[1], dataValues[2], dataValues[3], dataValues[5]
            };
        }

        this.textColorValueNow = "light";
        this.textColors = res.getStringArray(R.array.widget_text_colors);
        this.textColorValues = res.getStringArray(R.array.widget_text_color_values);

        this.textSize = 100;

        this.clockFontValueNow = "light";
        this.clockFonts = res.getStringArray(R.array.clock_font);
        this.clockFontValues = res.getStringArray(R.array.clock_font_values);
    }

    @CallSuper
    public void initView() {
        this.wallpaper = findViewById(R.id.activity_widget_config_wall);
        bindWallpaper(true);

        this.widgetContainer = findViewById(R.id.activity_widget_config_widgetContainer);

        this.topContainer = findViewById(R.id.activity_widget_config_top);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            topContainer.setOnApplyWindowInsetsListener((v, insets) -> {
                widgetContainer.setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
                return insets;
            });
        }

        this.container = findViewById(R.id.activity_widget_config_container);

        this.scrollView = findViewById(R.id.activity_widget_config_scrollView);

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
        BubbleSeekBar cardAlphaSeekBar = findViewById(R.id.activity_widget_config_cardAlphaSeekBar);
        cardAlphaSeekBar.setCustomSectionTextArray((sectionCount, array) -> {
            array.clear();
            array.put(0, "0%");
            array.put(1, "20%");
            array.put(2, "40%");
            array.put(3, "60%");
            array.put(4, "80%");
            array.put(5, "100%");
            return array;
        });
        cardAlphaSeekBar.setOnProgressChangedListener(new CardAlphaChangedListener());

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
        textStyleSpinner.setOnItemSelectedListener(new TextColorSpinnerSelectedListener());
        textStyleSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, textColors)
        );

        this.textSizeContainer = findViewById(R.id.activity_widget_config_textSizeContainer);
        BubbleSeekBar textSizeSeekBar = findViewById(R.id.activity_widget_config_textSizeSeekBar);
        textSizeSeekBar.setCustomSectionTextArray((sectionCount, array) -> {
            array.clear();
            array.put(0, "0%");
            array.put(1, "100%");
            array.put(2, "200%");
            array.put(3, "300%");
            return array;
        });
        textSizeSeekBar.setOnProgressChangedListener(new TextSizeChangedListener());

        this.clockFontContainer = findViewById(R.id.activity_widget_config_clockFontContainer);
        AppCompatSpinner clockFontSpinner = findViewById(R.id.activity_widget_config_clockFontSpinner);
        clockFontSpinner.setOnItemSelectedListener(new ClockFontSpinnerSelectedListener());
        clockFontSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clockFonts)
        );

        Button doneButton = findViewById(R.id.activity_widget_config_doneButton);
        doneButton.setOnClickListener(v -> {
            getSharedPreferences(getSharedPreferencesName(), MODE_PRIVATE)
                    .edit()
                    .putString(getString(R.string.key_view_type), viewTypeValueNow)
                    .putString(getString(R.string.key_card_style), cardStyleValueNow)
                    .putInt(getString(R.string.key_card_alpha), cardAlpha)
                    .putBoolean(getString(R.string.key_hide_subtitle), hideSubtitle)
                    .putString(getString(R.string.key_subtitle_data), subtitleDataValueNow)
                    .putString(getString(R.string.key_text_color), textColorValueNow)
                    .putInt(getString(R.string.key_text_size), textSize)
                    .putString(getString(R.string.key_clock_font), clockFontValueNow)
                    .apply();

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

            PollingManager.resetNormalBackgroundTask(this, true);
            finish();
        });

        bottomSheetBehavior = BottomSheetBehavior.from(
                findViewById(R.id.activity_widget_config_custom_subtitle));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomSheetScrollView = findViewById(R.id.activity_widget_config_custom_scrollView);

        subtitleInputLayout = findViewById(R.id.activity_widget_config_subtitle_inputLayout);

        subtitleInputter = findViewById(R.id.activity_widget_config_subtitle_inputter);
        subtitleInputter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null) {
                    subtitleDataValueNow = editable.toString();
                } else {
                    subtitleDataValueNow = "";
                }
                updateHostView();
            }
        });

        LinearLayout scrollContainer = findViewById(R.id.activity_widget_config_scrollContainer);
        scrollContainer.post(() -> scrollContainer.setPaddingRelative(
                0, 0, 0, subtitleInputLayout.getMeasuredHeight()));
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
    public void requestWeatherSuccess(@NonNull Location requestLocation) {
        if (destroyed) {
            return;
        }

        locationNow = requestLocation;
        if (requestLocation.getWeather() == null) {
            requestWeatherFailed(requestLocation);
        } else {
            updateHostView();
        }
    }

    @Override
    public void requestWeatherFailed(@NonNull Location requestLocation) {
        if (destroyed) {
            return;
        }
        locationNow = requestLocation;
        updateHostView();
        SnackbarUtils.showSnackbar(this, getString(R.string.feedback_get_weather_failed));
    }

    private void bindWallpaper(boolean checkPermissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermissions) {
            boolean hasPermission = checkPermissions((requestCode, permission, grantResult) -> {
                bindWallpaper(false);
                if (textColorValueNow.equals("auto")) {
                    updateHostView();
                }
            });
            if (!hasPermission) {
                return;
            }
        }

        try {
            WallpaperManager manager = WallpaperManager.getInstance(this);
            if (manager != null) {
                Drawable drawable = manager.getDrawable();
                if (drawable != null) {
                    wallpaper.setImageDrawable(drawable);
                }
            }
        } catch (Exception ignore) {
            // do nothing.
        }
    }

    /**
     * @return true : already got permissions.
     *         false: request permissions.
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermissions(@NonNull OnRequestPermissionsResultListener l) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0, l);
            return false;
        }
        return true;
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
            if (subtitleDataValues[i].equals("custom")) {
                bottomSheetBehavior.setPeekHeight(
                        (int) (subtitleInputLayout.getMeasuredHeight() + bottomSheetScrollView.getInsetsBottom()),
                        true
                );
                bottomSheetBehavior.setHideable(false);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setPeekHeight(0);
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            if (!subtitleDataValueNow.equals(subtitleDataValues[i])) {
                if (subtitleDataValues[i].equals("custom")) {
                    Editable editable = subtitleInputter.getText();
                    if (editable != null) {
                        subtitleDataValueNow = editable.toString();
                    } else {
                        subtitleDataValueNow = "";
                    }
                } else {
                    subtitleDataValueNow = subtitleDataValues[i];
                }
                updateHostView();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // do nothing.
        }
    }

    private class TextColorSpinnerSelectedListener implements AppCompatSpinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (!textColorValueNow.equals(textColorValues[i])) {
                textColorValueNow = textColorValues[i];
                if (!textColorValueNow.equals("auto")) {
                    updateHostView();
                    return;
                }

                boolean hasPermission = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    hasPermission = checkPermissions((requestCode, permission, grantResult) -> {
                        bindWallpaper(false);
                        updateHostView();
                    });
                }
                if (hasPermission) {
                    updateHostView();
                }
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

    private class TextSizeChangedListener extends BubbleSeekBar.OnProgressChangedListenerAdapter {
        @Override
        public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            if (textSize != progress) {
                textSize = progress;
                updateHostView();
            }
        }
    }
}
