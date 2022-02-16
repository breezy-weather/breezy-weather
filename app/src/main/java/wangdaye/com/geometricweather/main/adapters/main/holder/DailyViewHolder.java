package wangdaye.com.geometricweather.main.adapters.main.holder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.DailyTrendDisplay;
import wangdaye.com.geometricweather.common.basic.models.weather.Daily;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.main.adapters.main.MainTag;
import wangdaye.com.geometricweather.main.adapters.trend.DailyTrendAdapter;
import wangdaye.com.geometricweather.main.layouts.TrendHorizontalLinearLayoutManager;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.common.ui.adapters.TagAdapter;
import wangdaye.com.geometricweather.common.ui.decotarions.GridMarginsDecoration;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.main.widgets.TrendRecyclerViewScrollBar;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class DailyViewHolder extends AbstractMainCardViewHolder {

    private final CardView mCard;

    private final TextView mTitle;
    private final TextView mSubtitle;
    private final RecyclerView mTagView;

    private final TrendRecyclerView mTrendRecyclerView;
    private final DailyTrendAdapter mTrendAdapter;

    public DailyViewHolder(ViewGroup parent, MainThemeManager themeManager) {
        super(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.container_main_daily_trend_card, parent, false), themeManager);

        mCard = itemView.findViewById(R.id.container_main_daily_trend_card);
        mTitle = itemView.findViewById(R.id.container_main_daily_trend_card_title);
        mSubtitle = itemView.findViewById(R.id.container_main_daily_trend_card_subtitle);
        mTagView = itemView.findViewById(R.id.container_main_daily_trend_card_tagView);

        mTrendRecyclerView = itemView.findViewById(R.id.container_main_daily_trend_card_trendRecyclerView);
        mTrendRecyclerView.addItemDecoration(new TrendRecyclerViewScrollBar(parent.getContext(), themeManager));
        mTrendRecyclerView.setHasFixedSize(true);

        mTrendAdapter = new DailyTrendAdapter();
    }

    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled, firstCard);

        Weather weather = location.getWeather();
        assert weather != null;

        int weatherColor = themeManager.getWeatherThemeColors()[0];

        mCard.setCardBackgroundColor(themeManager.getRootColor(context));

        mTitle.setTextColor(weatherColor);

        if (TextUtils.isEmpty(weather.getCurrent().getDailyForecast())) {
            mSubtitle.setVisibility(View.GONE);
        } else {
            mSubtitle.setVisibility(View.VISIBLE);
            mSubtitle.setText(weather.getCurrent().getDailyForecast());
        }

        List<TagAdapter.Tag> tagList = getTagList(weather);
        if (tagList.size() < 2) {
            mTagView.setVisibility(View.GONE);
        } else {
            mTagView.setVisibility(View.VISIBLE);
            int decorCount = mTagView.getItemDecorationCount();
            for (int i = 0; i < decorCount; i++) {
                mTagView.removeItemDecorationAt(0);
            }
            mTagView.addItemDecoration(
                    new GridMarginsDecoration(
                            context.getResources().getDimension(R.dimen.little_margin),
                            context.getResources().getDimension(R.dimen.normal_margin),
                            mTagView
                    )
            );

            mTagView.setLayoutManager(new TrendHorizontalLinearLayoutManager(context));
            mTagView.setAdapter(
                    new TagAdapter(tagList, weatherColor, (checked, oldPosition, newPosition) -> {
                        setTrendAdapterByTag(location, (MainTag) tagList.get(newPosition));
                        return false;
                    }, themeManager, 0)
            );
        }

        mTrendRecyclerView.setLayoutManager(
                new TrendHorizontalLinearLayoutManager(
                        context,
                        DisplayUtils.isLandscape(context) ? 7 : 5
                )
        );
        mTrendRecyclerView.setAdapter(mTrendAdapter);
        mTrendRecyclerView.setKeyLineVisibility(
                SettingsManager.getInstance(context).isTrendHorizontalLinesEnabled());
        setTrendAdapterByTag(location, (MainTag) tagList.get(0));
    }

    private void setTrendAdapterByTag(Location location, MainTag tag) {
        switch (tag.getType()) {
            case TEMPERATURE:
                mTrendAdapter.temperature(
                        (GeoActivity) context,
                        mTrendRecyclerView,
                        location,
                        provider,
                        themeManager,
                        SettingsManager.getInstance(context).getTemperatureUnit()
                );
                break;

            case WIND:
                mTrendAdapter.wind(
                        (GeoActivity) context,
                        mTrendRecyclerView,
                        location,
                        themeManager,
                        SettingsManager.getInstance(context).getSpeedUnit()
                );
                break;

            case PRECIPITATION:
                mTrendAdapter.precipitation(
                        (GeoActivity) context,
                        mTrendRecyclerView,
                        location,
                        provider,
                        themeManager,
                        SettingsManager.getInstance(context).getPrecipitationUnit()
                );
                break;

            case AIR_QUALITY:
                mTrendAdapter.airQuality((GeoActivity) context, mTrendRecyclerView, location, themeManager);
                break;

            case UV_INDEX:
                mTrendAdapter.uv((GeoActivity) context, mTrendRecyclerView, location, themeManager);
                break;
        }
        mTrendAdapter.notifyDataSetChanged();
    }

    private List<TagAdapter.Tag> getTagList(Weather weather) {
        List<TagAdapter.Tag> tagList = new ArrayList<>();
        List<DailyTrendDisplay> displayList
                = SettingsManager.getInstance(context).getDailyTrendDisplayList();
        for (DailyTrendDisplay display : displayList) {
            switch (display) {
                case TAG_TEMPERATURE:
                    tagList.add(new MainTag(context.getString(R.string.tag_temperature), MainTag.Type.TEMPERATURE));
                    break;

                case TAG_AIR_QUALITY:
                    for (Daily daily : weather.getDailyForecast()) {
                        if (daily.getAirQuality().isValid()) {
                            tagList.add(new MainTag(context.getString(R.string.tag_aqi), MainTag.Type.AIR_QUALITY));
                            break;
                        }
                    }
                    break;

                case TAG_WIND:
                    for (Daily daily : weather.getDailyForecast()) {
                        if (daily.day().getWind().isValidSpeed()
                                && daily.night().getWind().isValidSpeed() ) {
                            tagList.add(new MainTag(context.getString(R.string.tag_wind), MainTag.Type.WIND));
                            break;
                        }
                    }
                    break;

                case TAG_UV_INDEX:
                    for (Daily daily : weather.getDailyForecast()) {
                        if (daily.getUV().isValid()) {
                            tagList.add(new MainTag(context.getString(R.string.tag_uv), MainTag.Type.UV_INDEX));
                            break;
                        }
                    }
                    break;

                case TAG_PRECIPITATION:
                    tagList.addAll(getPrecipitationTagList(weather));
                    break;
            }
        }
        if (tagList.size() == 0) {
            tagList.add(new MainTag(context.getString(R.string.tag_temperature), MainTag.Type.TEMPERATURE));
        }

        return tagList;
    }

    private List<TagAdapter.Tag> getPrecipitationTagList(Weather weather) {
        int precipitationCount = 0;
        for (Daily d : weather.getDailyForecast()) {
            if ((d.day().getWeatherCode().isPrecipitation() || d.night().getWeatherCode().isPrecipitation())
                    && (d.day().getPrecipitation().isValid() || d.night().getPrecipitation().isValid())) {
                precipitationCount ++;
            }
        }
        if (precipitationCount < 3) {
            return new ArrayList<>();
        } else {
            List<TagAdapter.Tag> list = new ArrayList<>();
            list.add(new MainTag(context.getString(R.string.tag_precipitation), MainTag.Type.PRECIPITATION));
            return list;
        }
    }
}