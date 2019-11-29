package wangdaye.com.geometricweather.daily;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.adapter.FitBottomSystemBarPagerAdapter;
import wangdaye.com.geometricweather.ui.widget.insets.FitBottomSystemBarViewPager;

/**
 * Daily weather activity.
 * */

public class DailyWeatherActivity extends GeoActivity {

    private CoordinatorLayout container;
    private TextView title;
    private TextView subtitle;
    private TextView indicator;

    private @Nullable Weather weather;
    private @Nullable TimeZone timeZone;
    private int position;

    public static final String KEY_FORMATTED_LOCATION_ID = "FORMATTED_LOCATION_ID";
    public static final String KEY_CURRENT_DAILY_INDEX = "CURRENT_DAILY_INDEX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_daily);
        initData();
        initWidget();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    private void initData() {
        Location location;
        String formattedId = getIntent().getStringExtra(KEY_FORMATTED_LOCATION_ID);
        if (TextUtils.isEmpty(formattedId)) {
            location = DatabaseHelper.getInstance(this).readLocationList().get(0);
        } else {
            location = DatabaseHelper.getInstance(this).readLocation(formattedId);
        }

        if (location != null) {
            weather = DatabaseHelper.getInstance(this).readWeather(location);
            timeZone = location.getTimeZone();
        }
        position = getIntent().getIntExtra(KEY_CURRENT_DAILY_INDEX, 0);
    }

    private void initWidget() {
        if (weather == null) {
            finish();
        }

        container = findViewById(R.id.activity_weather_daily_container);

        Toolbar toolbar = findViewById(R.id.activity_weather_daily_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        title = findViewById(R.id.activity_weather_daily_title);
        subtitle = findViewById(R.id.activity_weather_daily_subtitle);
        indicator = findViewById(R.id.activity_weather_daily_indicator);
        if (!SettingsOptionManager.getInstance(this).getLanguage().getCode().startsWith("zh")){
            subtitle.setVisibility(View.GONE);
        }
        selectPage(
                weather.getDailyForecast().get(position),
                position,
                weather.getDailyForecast().size()
        );

        List<View> viewList = new ArrayList<>(weather.getDailyForecast().size());
        List<String> titleList = new ArrayList<>(weather.getDailyForecast().size());
        for (int i = 0; i < weather.getDailyForecast().size(); i ++) {
            Daily d = weather.getDailyForecast().get(i);

            RecyclerView recyclerView = new RecyclerView(this);
            recyclerView.setClipToPadding(false);
            DailyWeatherAdapter dailyWeatherAdapter = new DailyWeatherAdapter(this, d, 3);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            gridLayoutManager.setSpanSizeLookup(dailyWeatherAdapter.spanSizeLookup);
            recyclerView.setAdapter(dailyWeatherAdapter);
            recyclerView.setLayoutManager(gridLayoutManager);

            viewList.add(recyclerView);
            titleList.add(String.valueOf(i + 1));
        }

        FitBottomSystemBarViewPager pager = findViewById(R.id.activity_weather_daily_pager);
        pager.setAdapter(new FitBottomSystemBarPagerAdapter(pager, viewList, titleList));
        pager.setCurrentItem(position);
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
                        position,
                        weather.getDailyForecast().size()
                );
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing.
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void selectPage(Daily daily, int position, int size) {
        title.setText(daily.getDate(getString(R.string.date_format_widget_long)));
        subtitle.setText(daily.getLunar());
        if (timeZone != null && daily.isToday(timeZone)) {
            indicator.setText(getString(R.string.today));
        } else {
            indicator.setText((position + 1) + "/" + size);
        }
    }
}
