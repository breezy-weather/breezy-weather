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
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.xw.repo.BubbleSeekBar;

import javax.inject.Inject;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitSystemBarNestedScrollView;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.settings.ConfigStore;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Abstract widget config activity.
 * */

public abstract class AbstractWidgetConfigActivity extends GeoActivity
        implements WeatherHelper.OnRequestWeatherListener {

    protected FrameLayout mTopContainer;
    protected ImageView mWallpaper;
    protected FrameLayout mWidgetContainer;

    protected NestedScrollView mScrollView;
    protected RelativeLayout mViewTypeContainer;
    protected RelativeLayout mCardStyleContainer;
    protected RelativeLayout mCardAlphaContainer;
    protected RelativeLayout mHideSubtitleContainer;
    protected RelativeLayout mSubtitleDataContainer;
    protected RelativeLayout mTextColorContainer;
    protected RelativeLayout mTextSizeContainer;
    protected RelativeLayout mClockFontContainer;
    protected RelativeLayout mHideLunarContainer;
    protected RelativeLayout mAlignEndContainer;

    private BottomSheetBehavior<?> mBottomSheetBehavior;
    private FitSystemBarNestedScrollView mBottomSheetScrollView;
    private TextInputLayout mSubtitleInputLayout;
    private TextInputEditText mSubtitleEditText;

    protected Location locationNow;

    @Inject WeatherHelper weatherHelper;
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

    protected boolean hideLunar;

    protected boolean alignEnd;

    private long mLastBackPressedTime = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        initData();
        readConfig();
        initView();
        updateHostView();

        if (locationNow.isCurrentPosition()) {
            if (locationNow.isUsable()) {
                weatherHelper.requestWeather(this, locationNow, this);
            } else {
                weatherHelper.requestWeather(
                        this,
                        Location.buildDefaultLocation(
                                SettingsManager.getInstance(this).getWeatherSource()
                        ),
                        this
                );
            }
        } else {
            weatherHelper.requestWeather(this, locationNow, this);
        }
    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            setBottomSheetState(true);
            return;
        }

        long time = System.currentTimeMillis();
        if (time - mLastBackPressedTime < 2000) {
            super.onBackPressed();
            return;
        }

        mLastBackPressedTime = time;
        SnackbarHelper.showSnackbar(getString(R.string.feedback_click_again_to_exit));
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
        locationNow = Location.copy(
                locationNow,
                DatabaseHelper.getInstance(this).readWeather(locationNow)
        );

        destroyed = false;

        Resources res = getResources();

        viewTypeValueNow = "rectangle";
        viewTypes = res.getStringArray(R.array.widget_styles);
        viewTypeValues = res.getStringArray(R.array.widget_style_values);

        cardStyleValueNow = "none";
        cardStyles = res.getStringArray(R.array.widget_card_styles);
        cardStyleValues = res.getStringArray(R.array.widget_card_style_values);

        cardAlpha = 100;

        hideSubtitle = false;

        subtitleDataValueNow = "time";
        String[] data = res.getStringArray(R.array.subtitle_data);
        String[] dataValues = res.getStringArray(R.array.subtitle_data_values);
        if (SettingsManager.getInstance(this).getLanguage().isChinese()) {
            subtitleData = new String[] {
                    data[0], data[1], data[2], data[3], data[4], data[5]
            };
            subtitleDataValues = new String[] {
                    dataValues[0], dataValues[1], dataValues[2], dataValues[3], dataValues[4], dataValues[5]
            };
        } else {
            subtitleData = new String[] {
                    data[0], data[1], data[2], data[3], data[5]
            };
            subtitleDataValues = new String[] {
                    dataValues[0], dataValues[1], dataValues[2], dataValues[3], dataValues[5]
            };
        }

        textColorValueNow = "light";
        textColors = res.getStringArray(R.array.widget_text_colors);
        textColorValues = res.getStringArray(R.array.widget_text_color_values);

        textSize = 100;

        clockFontValueNow = "light";
        clockFonts = res.getStringArray(R.array.clock_font);
        clockFontValues = res.getStringArray(R.array.clock_font_values);

        hideLunar = false;

        alignEnd = false;
    }

    private void readConfig() {
        ConfigStore config = ConfigStore.getInstance(this, getConfigStoreName());
        viewTypeValueNow = config.getString(getString(R.string.key_view_type), viewTypeValueNow);
        cardStyleValueNow = config.getString(getString(R.string.key_card_style), cardStyleValueNow);
        cardAlpha = config.getInt(getString(R.string.key_card_alpha), cardAlpha);
        hideSubtitle = config.getBoolean(getString(R.string.key_hide_subtitle), hideSubtitle);
        subtitleDataValueNow = config.getString(getString(R.string.key_subtitle_data), subtitleDataValueNow);
        textColorValueNow = config.getString(getString(R.string.key_text_color), textColorValueNow);
        textSize = config.getInt(getString(R.string.key_text_size), textSize);
        clockFontValueNow = config.getString(getString(R.string.key_clock_font), clockFontValueNow);
        hideLunar = config.getBoolean(getString(R.string.key_hide_lunar), hideLunar);
        alignEnd = config.getBoolean(getString(R.string.key_align_end), alignEnd);
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @CallSuper
    public void initView() {
        mWallpaper = findViewById(R.id.activity_widget_config_wall);
        bindWallpaper(true);

        mWidgetContainer = findViewById(R.id.activity_widget_config_widgetContainer);

        mTopContainer = findViewById(R.id.activity_widget_config_top);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int adaptiveWidth = DisplayUtils.getTabletListAdaptiveWidth(this, screenWidth);
        int paddingHorizontal = (screenWidth - adaptiveWidth) / 2;
        mTopContainer.setOnApplyWindowInsetsListener((v, insets) -> {
            mWidgetContainer.setPadding(paddingHorizontal, insets.getSystemWindowInsetTop(),
                    paddingHorizontal, 0);
            return insets;
        });

        mScrollView = findViewById(R.id.activity_widget_config_scrollView);

        mViewTypeContainer = findViewById(R.id.activity_widget_config_viewStyleContainer);
        mViewTypeContainer.setVisibility(View.GONE);
        AppCompatSpinner viewTypeSpinner = findViewById(R.id.activity_widget_config_styleSpinner);
        viewTypeSpinner.setOnItemSelectedListener(new ViewTypeSpinnerSelectedListener());
        viewTypeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, viewTypes));
        viewTypeSpinner.setSelection(indexValue(viewTypeValues, viewTypeValueNow), true);

        mCardStyleContainer = findViewById(R.id.activity_widget_config_showCardContainer);
        mCardStyleContainer.setVisibility(View.GONE);
        AppCompatSpinner cardStyleSpinner = findViewById(R.id.activity_widget_config_showCardSpinner);
        cardStyleSpinner.setOnItemSelectedListener(new CardStyleSpinnerSelectedListener());
        cardStyleSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cardStyles)
        );
        cardStyleSpinner.setSelection(indexValue(cardStyleValues, cardStyleValueNow), true);

        mCardAlphaContainer = findViewById(R.id.activity_widget_config_cardAlphaContainer);
        mCardAlphaContainer.setVisibility(View.GONE);
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
        cardAlphaSeekBar.setProgress(cardAlpha);

        mHideSubtitleContainer = findViewById(R.id.activity_widget_config_hideSubtitleContainer);
        mHideSubtitleContainer.setVisibility(View.GONE);
        Switch hideSubtitleSwitch = findViewById(R.id.activity_widget_config_hideSubtitleSwitch);
        hideSubtitleSwitch.setOnCheckedChangeListener(new HideSubtitleSwitchCheckListener());
        hideSubtitleSwitch.setChecked(hideSubtitle);

        mSubtitleDataContainer = findViewById(R.id.activity_widget_config_subtitleDataContainer);
        mSubtitleDataContainer.setVisibility(View.GONE);
        AppCompatSpinner subtitleDataSpinner = findViewById(R.id.activity_widget_config_subtitleDataSpinner);
        subtitleDataSpinner.setOnItemSelectedListener(new SubtitleDataSpinnerSelectedListener());
        subtitleDataSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subtitleData)
        );
        subtitleDataSpinner.setSelection(
                indexValue(subtitleDataValues, isCustomSubtitle() ? "custom" : subtitleDataValueNow),
                true
        );

        mTextColorContainer = findViewById(R.id.activity_widget_config_blackTextContainer);
        mTextColorContainer.setVisibility(View.GONE);
        AppCompatSpinner textStyleSpinner = findViewById(R.id.activity_widget_config_blackTextSpinner);
        textStyleSpinner.setOnItemSelectedListener(new TextColorSpinnerSelectedListener());
        textStyleSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, textColors)
        );
        textStyleSpinner.setSelection(indexValue(textColorValues, textColorValueNow), true);

        mTextSizeContainer = findViewById(R.id.activity_widget_config_textSizeContainer);
        mTextSizeContainer.setVisibility(View.GONE);
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
        textSizeSeekBar.setProgress(textSize);

        mClockFontContainer = findViewById(R.id.activity_widget_config_clockFontContainer);
        mClockFontContainer.setVisibility(View.GONE);
        AppCompatSpinner clockFontSpinner = findViewById(R.id.activity_widget_config_clockFontSpinner);
        clockFontSpinner.setOnItemSelectedListener(new ClockFontSpinnerSelectedListener());
        clockFontSpinner.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clockFonts)
        );
        clockFontSpinner.setSelection(indexValue(clockFontValues, cardStyleValueNow), true);

        mHideLunarContainer = findViewById(R.id.activity_widget_config_hideLunarContainer);
        mHideLunarContainer.setVisibility(View.GONE);
        Switch hideLunarSwitch = findViewById(R.id.activity_widget_config_hideLunarSwitch);
        hideLunarSwitch.setOnCheckedChangeListener(new HideLunarSwitchCheckListener());
        hideLunarSwitch.setChecked(hideLunar);

        mAlignEndContainer = findViewById(R.id.activity_widget_config_alignEndContainer);
        mAlignEndContainer.setVisibility(View.GONE);
        Switch alignEndSwitch = findViewById(R.id.activity_widget_config_alignEndSwitch);
        alignEndSwitch.setOnCheckedChangeListener(new AlignEndSwitchCheckListener());
        alignEndSwitch.setChecked(alignEnd);

        Button doneButton = findViewById(R.id.activity_widget_config_doneButton);
        doneButton.setOnClickListener(v -> {
            ConfigStore.getInstance(this, getConfigStoreName())
                    .edit()
                    .putString(getString(R.string.key_view_type), viewTypeValueNow)
                    .putString(getString(R.string.key_card_style), cardStyleValueNow)
                    .putInt(getString(R.string.key_card_alpha), cardAlpha)
                    .putBoolean(getString(R.string.key_hide_subtitle), hideSubtitle)
                    .putString(getString(R.string.key_subtitle_data), subtitleDataValueNow)
                    .putString(getString(R.string.key_text_color), textColorValueNow)
                    .putInt(getString(R.string.key_text_size), textSize)
                    .putString(getString(R.string.key_clock_font), clockFontValueNow)
                    .putBoolean(getString(R.string.key_hide_lunar), hideLunar)
                    .putBoolean(getString(R.string.key_align_end), alignEnd)
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

        mBottomSheetScrollView = findViewById(R.id.activity_widget_config_custom_scrollView);

        mSubtitleInputLayout = findViewById(R.id.activity_widget_config_subtitle_inputLayout);

        mSubtitleEditText = findViewById(R.id.activity_widget_config_subtitle_inputter);
        mSubtitleEditText.addTextChangedListener(new TextWatcher() {
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
        if (isCustomSubtitle()) {
            mSubtitleEditText.setText(subtitleDataValueNow);
        } else {
            mSubtitleEditText.setText("");
        }

        TextView subtitleCustomKeywords = findViewById(R.id.activity_widget_config_custom_subtitle_keywords);
        subtitleCustomKeywords.setText(getSubtitleCustomKeywords());

        LinearLayout scrollContainer = findViewById(R.id.activity_widget_config_scrollContainer);
        scrollContainer.post(() -> scrollContainer.setPaddingRelative(
                0, 0, 0, mSubtitleInputLayout.getMeasuredHeight()));

        AppBarLayout bottomSheet = findViewById(R.id.activity_widget_config_custom_subtitle);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheet.post(() -> {
            mBottomSheetBehavior.setPeekHeight(
                    mSubtitleInputLayout.getMeasuredHeight()
                            + mBottomSheetScrollView.getBottomWindowInset()
            );
            setBottomSheetState(isCustomSubtitle());
        });
    }

    public final void updateHostView() {
        mWidgetContainer.removeAllViews();

        View view = getRemoteViews().apply(getApplicationContext(), mWidgetContainer);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        mWidgetContainer.addView(view, params);
    }

    private void setBottomSheetState(boolean visible) {
        if (visible) {
            mBottomSheetBehavior.setHideable(false);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            mBottomSheetBehavior.setHideable(true);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    public abstract RemoteViews getRemoteViews();

    public Location getLocationNow() {
        return locationNow;
    }

    public abstract String getConfigStoreName();

    private int indexValue(String[] values, String current) {
        for (int i = 0; i < values.length; i ++) {
            if (values[i].equals(current)) {
                return i;
            }
        }
        return 0;
    }

    private boolean isCustomSubtitle() {
        for (String v : subtitleDataValues) {
            if (!v.equals("custom") && v.equals(subtitleDataValueNow)) {
                return false;
            }
        }
        return true;
    }

    private String getSubtitleCustomKeywords() {
        return getString(R.string.feedback_custom_subtitle_keyword_cw) +
                getString(R.string.feedback_custom_subtitle_keyword_ct) +
                getString(R.string.feedback_custom_subtitle_keyword_ctd) +
                getString(R.string.feedback_custom_subtitle_keyword_at) +
                getString(R.string.feedback_custom_subtitle_keyword_atd) +
                getString(R.string.feedback_custom_subtitle_keyword_cpb) +
                getString(R.string.feedback_custom_subtitle_keyword_cp) +
                getString(R.string.feedback_custom_subtitle_keyword_cwd) +
                getString(R.string.feedback_custom_subtitle_keyword_cuv) +
                getString(R.string.feedback_custom_subtitle_keyword_ch) +
                getString(R.string.feedback_custom_subtitle_keyword_cps) +
                getString(R.string.feedback_custom_subtitle_keyword_cv) +
                getString(R.string.feedback_custom_subtitle_keyword_cdp) +
                getString(R.string.feedback_custom_subtitle_keyword_al) +
                getString(R.string.feedback_custom_subtitle_keyword_als) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_l) +
                getString(R.string.feedback_custom_subtitle_keyword_lat) +
                getString(R.string.feedback_custom_subtitle_keyword_lon) +
                getString(R.string.feedback_custom_subtitle_keyword_ut) +
                getString(R.string.feedback_custom_subtitle_keyword_d) +
                getString(R.string.feedback_custom_subtitle_keyword_lc) +
                getString(R.string.feedback_custom_subtitle_keyword_w) +
                getString(R.string.feedback_custom_subtitle_keyword_ws) +
                getString(R.string.feedback_custom_subtitle_keyword_dd) +
                getString(R.string.feedback_custom_subtitle_keyword_hd) +
                getString(R.string.feedback_custom_subtitle_keyword_enter) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xdw) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xnw) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xdt) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xnt) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xdtd) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xntd) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xdp) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xnp) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xdwd) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xnwd) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xsr) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xss) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xmr) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xms) +
                "\n" +
                getString(R.string.feedback_custom_subtitle_keyword_xmp);
    }

    protected int isHideLunarContainerVisible() {
        return SettingsManager.getInstance(this).getLanguage().isChinese()
                ? View.VISIBLE
                : View.GONE;
    }

    // interface.

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(@NonNull Location requestLocation) {
        if (destroyed) {
            return;
        }

        locationNow = requestLocation;
        if (requestLocation.getWeather() == null) {
            requestWeatherFailed(requestLocation, false);
        } else {
            updateHostView();
        }
    }

    @Override
    public void requestWeatherFailed(@NonNull Location requestLocation, @NonNull Boolean apiLimitReached) {
        if (destroyed) {
            return;
        }
        locationNow = requestLocation;
        updateHostView();
        SnackbarHelper.showSnackbar(getString(R.string.feedback_get_weather_failed));
    }

    @SuppressLint("MissingPermission")
    private void bindWallpaper(boolean checkPermissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermissions) {
            boolean hasPermission = checkPermissions(0);
            if (!hasPermission) {
                return;
            }
        }

        try {
            WallpaperManager manager = WallpaperManager.getInstance(this);
            if (manager != null) {
                Drawable drawable = manager.getDrawable();
                if (drawable != null) {
                    mWallpaper.setImageDrawable(drawable);
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
    private boolean checkPermissions(int requestCode) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 0:
                bindWallpaper(false);
                if (textColorValueNow.equals("auto")) {
                    updateHostView();
                }
                break;

            case 1:
                bindWallpaper(false);
                updateHostView();
                break;
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

    private class HideLunarSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            hideLunar = isChecked;
            updateHostView();
        }
    }

    private class AlignEndSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            alignEnd = isChecked;
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
            setBottomSheetState(subtitleDataValues[i].equals("custom"));

            if (!subtitleDataValueNow.equals(subtitleDataValues[i])) {
                if (subtitleDataValues[i].equals("custom")) {
                    Editable editable = mSubtitleEditText.getText();
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
                    hasPermission = checkPermissions(1);
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
