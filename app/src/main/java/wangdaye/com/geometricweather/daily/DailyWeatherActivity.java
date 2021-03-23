package wangdaye.com.geometricweather.daily;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitSystemBarRecyclerView;
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitSystemBarViewPager;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Daily weather activity.
 * */

public class DailyWeatherActivity extends GeoActivity {

    private Toolbar mToolbar;
    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mIndicator;

    private String mFormattedId;
    private int mPosition;

    public static final String KEY_FORMATTED_LOCATION_ID = "FORMATTED_LOCATION_ID";
    public static final String KEY_CURRENT_DAILY_INDEX = "CURRENT_DAILY_INDEX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_daily);
        initData();
        initWidget();
    }

    private void initData() {
        mFormattedId = getIntent().getStringExtra(KEY_FORMATTED_LOCATION_ID);
        mPosition = getIntent().getIntExtra(KEY_CURRENT_DAILY_INDEX, 0);
    }

    private void initWidget() {
        mToolbar = findViewById(R.id.activity_weather_daily_toolbar);
        mToolbar.setNavigationOnClickListener(v -> finish());

        mTitle = findViewById(R.id.activity_weather_daily_title);
        mSubtitle = findViewById(R.id.activity_weather_daily_subtitle);
        mIndicator = findViewById(R.id.activity_weather_daily_indicator);
        if (!SettingsOptionManager.getInstance(this).getLanguage().isChinese()){
            mSubtitle.setVisibility(View.GONE);
        }

        final String formattedId = mFormattedId;
        AsyncHelper.runOnIO(emitter -> {
            Location location = null;

            if (!TextUtils.isEmpty(formattedId)) {
                location = DatabaseHelper.getInstance(this).readLocation(formattedId);
            }
            if (location == null) {
                location = DatabaseHelper.getInstance(this).readLocationList().get(0);
            }

            location.setWeather(DatabaseHelper.getInstance(this).readWeather(location));
            emitter.send(location, true);
        }, (AsyncHelper.Callback<Location>) (location, done) -> {
            if (location == null) {
                finish();
                return;
            }

            Weather weather = location.getWeather();
            if (weather == null) {
                finish();
                return;
            }

            selectPage(
                    weather.getDailyForecast().get(mPosition),
                    location.getTimeZone(),
                    mPosition,
                    weather.getDailyForecast().size()
            );

            List<View> viewList = new ArrayList<>(weather.getDailyForecast().size());
            List<String> titleList = new ArrayList<>(weather.getDailyForecast().size());

            for (int i = 0; i < weather.getDailyForecast().size(); i ++) {
                Daily d = weather.getDailyForecast().get(i);

                FitSystemBarRecyclerView recyclerView = new FitSystemBarRecyclerView(this);
                recyclerView.setFitSide(FitSystemBarRecyclerView.SIDE_BOTTOM);
                recyclerView.setClipToPadding(false);
                DailyWeatherAdapter dailyWeatherAdapter = new DailyWeatherAdapter(this, d, 3);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
                gridLayoutManager.setSpanSizeLookup(dailyWeatherAdapter.spanSizeLookup);
                recyclerView.setAdapter(dailyWeatherAdapter);
                recyclerView.setLayoutManager(gridLayoutManager);

                viewList.add(recyclerView);
                titleList.add(String.valueOf(i + 1));
            }

            FitSystemBarViewPager pager = findViewById(R.id.activity_weather_daily_pager);
            pager.setAdapter(new FitSystemBarViewPager.FitBottomSystemBarPagerAdapter(pager, viewList, titleList));
            pager.setPageMargin((int) DisplayUtils.dpToPx(this, 1));
            pager.setPageMarginDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorLine)));
            pager.setCurrentItem(mPosition);
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    // do nothing.
                }

                @Override
                public void onPageSelected(int position) {
                    selectPage(
                            weather.getDailyForecast().get(position),
                            location.getTimeZone(),
                            position,
                            weather.getDailyForecast().size()
                    );
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    // do nothing.
                }
            });
        });
    }

    @SuppressLint("SetTextI18n")
    private void selectPage(Daily daily, TimeZone timeZone, int position, int size) {
        mTitle.setText(daily.getDate(getString(R.string.date_format_widget_long)));
        mSubtitle.setText(daily.getLunar());

        mToolbar.setContentDescription(mTitle.getText() + ", " + mSubtitle.getText());

        if (timeZone != null && daily.isToday(timeZone)) {
            mIndicator.setText(getString(R.string.today));
        } else {
            mIndicator.setText((position + 1) + "/" + size);
        }
    }
}
