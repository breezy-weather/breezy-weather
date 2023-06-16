package org.breezyweather.main.adapters.main.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.TimeZone;

import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.weather.Daily;
import org.breezyweather.common.ui.widgets.horizontal.HorizontalViewPager2;
import org.breezyweather.common.utils.helpers.IntentHelper;
import org.breezyweather.theme.ThemeManager;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.theme.weatherView.WeatherViewController;
import org.breezyweather.R;
import org.breezyweather.main.adapters.HomePollenAdapter;
import org.breezyweather.main.adapters.HomePollenViewHolder;
import org.breezyweather.main.utils.MainThemeColorProvider;

public class AllergenViewHolder extends AbstractMainCardViewHolder {

    private final TextView mTitle;
    private final TextView mSubtitle;
    private final TextView mIndicator;
    private final HorizontalViewPager2 mPager;

    private @Nullable DailyPollenPageChangeCallback mCallback;

    private static class DailyPollenPagerAdapter extends HomePollenAdapter {

        public DailyPollenPagerAdapter(Location location) {
            super(location);
        }

        @NonNull
        @Override
        public HomePollenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            HomePollenViewHolder holder = super.onCreateViewHolder(parent, viewType);
            holder.itemView.setLayoutParams(
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );
            return holder;
        }
    }

    private class DailyPollenPageChangeCallback extends HorizontalViewPager2.OnPageChangeCallback {

        private final Context mContext;
        private final Location mLocation;

        DailyPollenPageChangeCallback(Context context, Location location) {
            mContext = context;
            mLocation = location;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onPageSelected(int position) {
            assert mLocation.getWeather() != null;

            TimeZone timeZone = mLocation.getTimeZone();
            Daily daily = mLocation.getWeather().getDailyForecast().get(position);

            if (daily.isToday(timeZone)) {
                mIndicator.setText(mContext.getString(R.string.short_today));
            } else {
                mIndicator.setText((position + 1) + "/" + mLocation.getWeather().getDailyForecast().size());
            }
        }
    }

    public AllergenViewHolder(ViewGroup parent) {
        super(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.container_main_pollen, parent, false)
        );

        mTitle = itemView.findViewById(R.id.container_main_pollen_title);
        mSubtitle = itemView.findViewById(R.id.container_main_pollen_subtitle);
        mIndicator = itemView.findViewById(R.id.container_main_pollen_indicator);
        mPager = itemView.findViewById(R.id.container_main_pollen_pager);

        mCallback = null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider,
                listAnimationEnabled, itemAnimationEnabled, firstCard);

        assert location.getWeather() != null;

        mTitle.setTextColor(
                ThemeManager
                        .getInstance(context)
                        .getWeatherThemeDelegate()
                        .getThemeColors(
                                context,
                                WeatherViewController.getWeatherKind(location.getWeather()),
                                location.isDaylight()
                        )[0]
        );
        mSubtitle.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorCaptionText));

        mPager.setAdapter(new DailyPollenPagerAdapter(location));
        mPager.setCurrentItem(0);

        mCallback = new DailyPollenPageChangeCallback(activity, location);
        mPager.registerOnPageChangeCallback(mCallback);

        itemView.setContentDescription(mTitle.getText());
        itemView.setOnClickListener(v -> IntentHelper.startAllergenActivity((GeoActivity) context, location));
    }

    @Override
    public void onRecycleView() {
        super.onRecycleView();
        if (mCallback != null) {
            mPager.unregisterOnPageChangeCallback(mCallback);
            mCallback = null;
        }
    }
}