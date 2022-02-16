package wangdaye.com.geometricweather.main.adapters.main.holder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.weather.Base;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Minutely;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.main.adapters.main.MainTag;
import wangdaye.com.geometricweather.main.adapters.trend.HourlyTrendAdapter;
import wangdaye.com.geometricweather.main.layouts.TrendHorizontalLinearLayoutManager;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.common.ui.adapters.TagAdapter;
import wangdaye.com.geometricweather.common.ui.decotarions.GridMarginsDecoration;
import wangdaye.com.geometricweather.common.ui.widgets.PrecipitationBar;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.main.widgets.TrendRecyclerViewScrollBar;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class HourlyViewHolder extends AbstractMainCardViewHolder {

    private final CardView mCard;

    private final TextView mTitle;
    private final TextView mSubtitle;
    private final RecyclerView mTagView;

    private final TrendRecyclerView mTrendRecyclerView;
    private final HourlyTrendAdapter mTrendAdapter;

    private final LinearLayout mMinutelyContainer;
    private final TextView mMinutelyTitle;
    private final PrecipitationBar mPrecipitationBar;
    private final TextView mMinutelyStartText;
    private final TextView mMinutelyEndText;

    public HourlyViewHolder(ViewGroup parent, MainThemeManager themeManager) {
        super(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.container_main_hourly_trend_card, parent, false), themeManager);

        mCard = itemView.findViewById(R.id.container_main_hourly_trend_card);
        mTitle = itemView.findViewById(R.id.container_main_hourly_trend_card_title);
        mSubtitle = itemView.findViewById(R.id.container_main_hourly_trend_card_subtitle);
        mTagView = itemView.findViewById(R.id.container_main_hourly_trend_card_tagView);

        mTrendRecyclerView = itemView.findViewById(R.id.container_main_hourly_trend_card_trendRecyclerView);
        mTrendRecyclerView.addItemDecoration(new TrendRecyclerViewScrollBar(parent.getContext(), themeManager));
        mTrendRecyclerView.setHasFixedSize(true);

        mMinutelyContainer = itemView.findViewById(R.id.container_main_hourly_trend_card_minutely);
        mMinutelyTitle = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyTitle);
        mPrecipitationBar = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyBar);
        mMinutelyStartText = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyStartText);
        mMinutelyEndText = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyEndText);

        mTrendAdapter = new HourlyTrendAdapter();

        mMinutelyContainer.setOnClickListener(v -> {

        });
    }

    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location, @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled, firstCard);

        Weather weather = location.getWeather();
        assert weather != null;

        int weatherColor = themeManager.getWeatherThemeColors()[0];

        mCard.setCardBackgroundColor(themeManager.getRootColor(context));

        mTitle.setTextColor(weatherColor);

        if (TextUtils.isEmpty(weather.getCurrent().getHourlyForecast())) {
            mSubtitle.setVisibility(View.GONE);
        } else {
            mSubtitle.setVisibility(View.VISIBLE);
            mSubtitle.setText(weather.getCurrent().getHourlyForecast());
        }

        List<TagAdapter.Tag> tagList = getTagList(weather, location.getWeatherSource());
        if (tagList.size() < 2) {
            mTagView.setVisibility(View.GONE);
        } else {
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

        // trendScrollBar = new TrendRecyclerViewScrollBar(context);
        // trendRecyclerView.addItemDecoration(trendScrollBar);

        List<Minutely> minutelyList = weather.getMinutelyForecast();
        if (minutelyList.size() != 0 && needToShowMinutelyForecast(minutelyList)) {
            mMinutelyContainer.setVisibility(View.VISIBLE);

            mMinutelyTitle.setTextColor(themeManager.getTextContentColor(context));

            mPrecipitationBar.setBackgroundColor(themeManager.getLineColor(context));
            mPrecipitationBar.setPrecipitationColor(themeManager.getWeatherThemeColors()[0]);
            mPrecipitationBar.setMinutelyList(minutelyList);

            int size = minutelyList.size();
            mMinutelyStartText.setText(Base.getTime(context, minutelyList.get(0).getDate()));
            mMinutelyStartText.setTextColor(themeManager.getTextSubtitleColor(context));
            mMinutelyEndText.setText(Base.getTime(context, minutelyList.get(size - 1).getDate()));
            mMinutelyEndText.setTextColor(themeManager.getTextSubtitleColor(context));

            mMinutelyContainer.setContentDescription(
                    activity.getString(R.string.content_des_minutely_precipitation)
                            .replace("$1", Base.getTime(context, minutelyList.get(0).getDate()))
                            .replace("$2", Base.getTime(context, minutelyList.get(size - 1).getDate()))
            );
        } else {
            mMinutelyContainer.setVisibility(View.GONE);
        }
    }

    private static boolean needToShowMinutelyForecast(List<Minutely> minutelyList) {
        for (Minutely m : minutelyList) {
            if (m.isPrecipitation()) {
                return true;
            }
        }
        return false;
    }

    private void setTrendAdapterByTag(Location location, MainTag tag) {
        switch (tag.getType()) {
            case TEMPERATURE:
                mTrendAdapter.temperature(
                        (GeoActivity) context, mTrendRecyclerView,
                        location,
                        provider,
                        themeManager,
                        SettingsManager.getInstance(context).getTemperatureUnit()
                );
                break;

            case PRECIPITATION:
                mTrendAdapter.precipitation(
                        (GeoActivity) context, mTrendRecyclerView,
                        location,
                        provider,
                        themeManager,
                        SettingsManager.getInstance(context).getPrecipitationUnit()
                );
                break;
        }
    }

    private List<TagAdapter.Tag> getTagList(Weather weather, WeatherSource source) {
        List<TagAdapter.Tag> tagList = new ArrayList<>();
        tagList.add(new MainTag(context.getString(R.string.tag_temperature), MainTag.Type.TEMPERATURE));
        // tagList.addAll(getPrecipitationTagList(weather));
        return tagList;
    }

    private List<TagAdapter.Tag> getPrecipitationTagList(Weather weather) {
        int precipitationCount = 0;
        for (Hourly h : weather.getHourlyForecast()) {
            if (h.getWeatherCode().isPrecipitation() && h.getPrecipitation().isValid()) {
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
