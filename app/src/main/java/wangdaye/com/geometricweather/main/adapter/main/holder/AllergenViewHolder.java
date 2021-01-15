package wangdaye.com.geometricweather.main.adapter.main.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.adapter.DailyPollenAdapter;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class AllergenViewHolder extends AbstractMainCardViewHolder {

    private final CardView card;
    private final TextView title;
    private final TextView subtitle;
    private final TextView indicator;
    private final ViewPager2 pager;

    private @Nullable DailyPollenPageChangeCallback callback;

    private static class DailyPollenPagerAdapter extends DailyPollenAdapter {

        public DailyPollenPagerAdapter(Weather weather) {
            super(weather);
        }

        @NonNull
        @Override
        public DailyPollenAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DailyPollenAdapter.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
            holder.itemView.setLayoutParams(
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );
            return holder;
        }
    }

    private class DailyPollenPageChangeCallback extends ViewPager2.OnPageChangeCallback {

        private final Context context;
        private final Location location;

        DailyPollenPageChangeCallback(Context context, Location location) {
            this.context = context;
            this.location = location;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onPageSelected(int position) {
            assert location.getWeather() != null;

            TimeZone timeZone = location.getTimeZone();
            Daily daily = location.getWeather().getDailyForecast().get(position);

            if (timeZone != null && daily.isToday(timeZone)) {
                indicator.setText(context.getString(R.string.today));
            } else {
                indicator.setText((position + 1) + "/" + location.getWeather().getDailyForecast().size());
            }
        }
    }

    public AllergenViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.container_main_pollen, parent, false));

        this.card = itemView.findViewById(R.id.container_main_pollen);
        this.title = itemView.findViewById(R.id.container_main_pollen_title);
        this.subtitle = itemView.findViewById(R.id.container_main_pollen_subtitle);
        this.indicator = itemView.findViewById(R.id.container_main_pollen_indicator);
        this.pager = itemView.findViewById(R.id.container_main_pollen_pager);

        this.callback = null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider,
                listAnimationEnabled, itemAnimationEnabled, firstCard);

        assert location.getWeather() != null;

        card.setCardBackgroundColor(themeManager.getRootColor(context));
        title.setTextColor(themeManager.getWeatherThemeColors()[0]);
        subtitle.setTextColor(themeManager.getTextSubtitleColor(context));

        pager.setAdapter(new DailyPollenPagerAdapter(location.getWeather()));
        pager.setCurrentItem(0);

        callback = new DailyPollenPageChangeCallback(activity, location);
        pager.registerOnPageChangeCallback(callback);

        itemView.setOnClickListener(v -> IntentHelper.startAllergenActivity((GeoActivity) context, location));
    }

    @Override
    public void onRecycleView() {
        super.onRecycleView();
        if (callback != null) {
            pager.unregisterOnPageChangeCallback(callback);
            callback = null;
        }
    }
}