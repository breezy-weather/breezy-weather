package wangdaye.com.geometricweather.main.adapters.main.holder;

import android.annotation.SuppressLint;
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

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.weather.Base;
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly;
import wangdaye.com.geometricweather.common.basic.models.weather.Minutely;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.ui.adapters.TagAdapter;
import wangdaye.com.geometricweather.common.ui.decotarions.GridMarginsDecoration;
import wangdaye.com.geometricweather.common.ui.widgets.PrecipitationBar;
import wangdaye.com.geometricweather.common.ui.widgets.trend.TrendRecyclerView;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.main.adapters.main.MainTag;
import wangdaye.com.geometricweather.main.adapters.trend.HourlyTrendAdapter;
import wangdaye.com.geometricweather.main.layouts.TrendHorizontalLinearLayoutManager;
import wangdaye.com.geometricweather.main.utils.DayNightColorWrapper;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.main.widgets.TrendRecyclerViewScrollBar;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.weatherView.WeatherViewController;

public class HourlyViewHolder extends AbstractMainCardViewHolder {

    private final CardView mCard;

    private final TextView mTitle;
    private final TextView mSubtitle;
    private final RecyclerView mTagView;

    private final TrendRecyclerView mTrendRecyclerView;
    private final HourlyTrendAdapter mTrendAdapter;
    private final TrendRecyclerViewScrollBar mScrollBar;

    private final LinearLayout mMinutelyContainer;
    private final TextView mMinutelyTitle;
    private final PrecipitationBar mPrecipitationBar;
    private final TextView mMinutelyStartText;
    private final TextView mMinutelyEndText;

    @SuppressLint("NotifyDataSetChanged")
    public HourlyViewHolder(ViewGroup parent) {
        super(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.container_main_hourly_trend_card, parent, false)
        );

        mCard = itemView.findViewById(R.id.container_main_hourly_trend_card);
        mTitle = itemView.findViewById(R.id.container_main_hourly_trend_card_title);
        mSubtitle = itemView.findViewById(R.id.container_main_hourly_trend_card_subtitle);
        mTagView = itemView.findViewById(R.id.container_main_hourly_trend_card_tagView);

        mTrendRecyclerView = itemView.findViewById(R.id.container_main_hourly_trend_card_trendRecyclerView);
        mTrendRecyclerView.setHasFixedSize(true);

        mMinutelyContainer = itemView.findViewById(R.id.container_main_hourly_trend_card_minutely);
        mMinutelyTitle = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyTitle);
        mPrecipitationBar = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyBar);
        mMinutelyStartText = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyStartText);
        mMinutelyEndText = itemView.findViewById(R.id.container_main_hourly_trend_card_minutelyEndText);

        mTrendAdapter = new HourlyTrendAdapter();
        mScrollBar = new TrendRecyclerViewScrollBar();
        mTrendRecyclerView.addItemDecoration(mScrollBar);

        mMinutelyContainer.setOnClickListener(v -> {

        });

        DayNightColorWrapper.bind(
                mTrendRecyclerView,
                R.attr.colorOutline,
                (color, aBoolean) -> {
                    mTrendRecyclerView.setLineColor(color);
                    mTrendAdapter.notifyDataSetChanged();
                    return null;
                }
        );
    }

    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location, @NonNull ResourceProvider provider,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled, firstCard);

        DayNightColorWrapper.bind(itemView, R.attr.colorSurface, (color, animated) -> {
            mScrollBar.setColor(
                    color,
                    MainModuleUtils.isMainLightTheme(context, location.isDaylight())
            );
            mCard.setCardBackgroundColor(color);
            return null;
        });

        Weather weather = location.getWeather();
        assert weather != null;

        int weatherColor = ThemeManager
                .getInstance(context)
                .getWeatherThemeDelegate()
                .getThemeColors(
                        context,
                        WeatherViewController.getWeatherKind(weather),
                        location.isDaylight()
                )[0];

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
            DayNightColorWrapper.bind(mTagView, new Integer[0], (integers, aBoolean) -> {
                int[] colors = ThemeManager
                        .getInstance(context)
                        .getWeatherThemeDelegate()
                        .getThemeColors(
                                context,
                                WeatherViewController.getWeatherKind(weather),
                                location.isDaylight()
                        );

                mTagView.setAdapter(
                        new TagAdapter(
                                tagList,
                                ThemeManager
                                        .getInstance(context)
                                        .getThemeColor(context, R.attr.titleTextColor),
                                colors[1],
                                colors[2],
                                (checked, oldPosition, newPosition) -> {
                                    setTrendAdapterByTag(location, (MainTag) tagList.get(newPosition));
                                    return false;
                                },
                                0
                        )
                );
                return null;
            });
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

        List<Minutely> minutelyList = weather.getMinutelyForecast();
        if (minutelyList.size() != 0 && needToShowMinutelyForecast(minutelyList)) {
            mMinutelyContainer.setVisibility(View.VISIBLE);

            mPrecipitationBar.setMinutelyList(minutelyList);

            int size = minutelyList.size();
            mMinutelyStartText.setText(Base.getTime(context, minutelyList.get(0).getDate()));
            mMinutelyEndText.setText(Base.getTime(context, minutelyList.get(size - 1).getDate()));

            mMinutelyContainer.setContentDescription(
                    activity.getString(R.string.content_des_minutely_precipitation)
                            .replace("$1", Base.getTime(context, minutelyList.get(0).getDate()))
                            .replace("$2", Base.getTime(context, minutelyList.get(size - 1).getDate()))
            );
        } else {
            mMinutelyContainer.setVisibility(View.GONE);
        }

        DayNightColorWrapper.bind(mMinutelyContainer, new Integer[0], (colors, animated) -> {
            ThemeManager tm = ThemeManager.getInstance(context);

            mMinutelyTitle.setTextColor(tm.getThemeColor(context, R.attr.colorBodyText));

            mPrecipitationBar.setBackgroundColor(tm.getThemeColor(context, R.attr.colorOutline));
            mPrecipitationBar.setPrecipitationColor(
                    tm.getWeatherThemeDelegate().getThemeColors(
                            context,
                            WeatherViewController.getWeatherKind(weather),
                            location.isDaylight()
                    )[0]
            );

            mMinutelyStartText.setTextColor(tm.getThemeColor(context, R.attr.colorCaptionText));
            mMinutelyEndText.setTextColor(tm.getThemeColor(context, R.attr.colorCaptionText));

            return null;
        });
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
                        SettingsManager.getInstance(context).getTemperatureUnit()
                );
                break;

            case PRECIPITATION:
                mTrendAdapter.precipitation(
                        (GeoActivity) context, mTrendRecyclerView,
                        location,
                        provider,
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
