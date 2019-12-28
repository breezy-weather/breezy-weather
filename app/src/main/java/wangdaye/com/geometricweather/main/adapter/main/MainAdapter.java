package wangdaye.com.geometricweather.main.adapter.main;

import android.animation.Animator;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.appearance.CardDisplay;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.main.adapter.main.holder.AbstractMainCardViewHolder;
import wangdaye.com.geometricweather.main.adapter.main.holder.AbstractMainTrendCardViewHolder;
import wangdaye.com.geometricweather.main.adapter.main.holder.AbstractMainViewHolder;
import wangdaye.com.geometricweather.main.adapter.main.holder.AirQualityViewHolder;
import wangdaye.com.geometricweather.main.adapter.main.holder.AllergenViewHolder;
import wangdaye.com.geometricweather.main.adapter.main.holder.AstroViewHolder;
import wangdaye.com.geometricweather.main.adapter.main.holder.DailyViewHolder;
import wangdaye.com.geometricweather.main.adapter.main.holder.DetailsViewHolder;
import wangdaye.com.geometricweather.main.adapter.main.holder.FooterViewHolder;
import wangdaye.com.geometricweather.main.adapter.main.holder.HeaderViewHolder;
import wangdaye.com.geometricweather.main.adapter.main.holder.HourlyViewHolder;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

public class MainAdapter extends RecyclerView.Adapter<AbstractMainViewHolder> {

    private @NonNull GeoActivity activity;
    private @NonNull Location location;
    private @NonNull ResourceProvider provider;
    private @NonNull MainThemePicker picker;
    private @Px float itemWidth;

    private @NonNull List<Integer> viewTypeList;
    private @Nullable Integer firstCardPosition;
    private @NonNull List<Animator> pendingAnimatorList;
    private int headerCurrentTemperatureTextHeight;
    private boolean listAnimationEnabled;
    private boolean itemAnimationEnabled;

    public MainAdapter(@NonNull GeoActivity activity, @NonNull Location location,
                       @NonNull ResourceProvider provider, @NonNull MainThemePicker picker, @Px float itemWidth,
                       boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        reset(activity, location, provider, picker, itemWidth, listAnimationEnabled, itemAnimationEnabled);
    }

    public void reset(@NonNull GeoActivity activity, @NonNull Location location,
                      @NonNull ResourceProvider provider, @NonNull MainThemePicker picker, @Px float itemWidth,
                      boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        this.activity = activity;
        this.location = location;
        this.provider = provider;
        this.picker = picker;
        this.itemWidth = itemWidth;

        this.viewTypeList = new ArrayList<>();
        this.firstCardPosition = null;
        this.pendingAnimatorList = new ArrayList<>();
        this.headerCurrentTemperatureTextHeight = -1;
        this.listAnimationEnabled = listAnimationEnabled;
        this.itemAnimationEnabled = itemAnimationEnabled;

        if (location.getWeather() != null) {
            Weather weather = location.getWeather();
            List<CardDisplay> cardDisplayList = SettingsOptionManager.getInstance(activity).getCardDisplayList();
            viewTypeList.add(ViewType.HEADER);
            for (CardDisplay c : cardDisplayList) {
                if (c == CardDisplay.CARD_AIR_QUALITY
                        && !weather.getCurrent().getAirQuality().isValid()) {
                    continue;
                }
                if (c == CardDisplay.CARD_ALLERGEN
                        && !weather.getDailyForecast().get(0).getPollen().isValid()) {
                    continue;
                }
                if (c == CardDisplay.CARD_SUNRISE_SUNSET
                        && (weather.getDailyForecast().size() == 0
                        || !weather.getDailyForecast().get(0).sun().isValid())) {
                    continue;
                }
                viewTypeList.add(getViewType(c));
            }
            viewTypeList.add(ViewType.FOOTER);

            ensureFirstCard();
        }
    }

    public void setNullWeather() {
        this.viewTypeList = new ArrayList<>();
        ensureFirstCard();
    }

    @NonNull
    @Override
    public AbstractMainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ViewType.HEADER:
                return new HeaderViewHolder(parent);

            case ViewType.DAILY:
                return new DailyViewHolder(parent);

            case ViewType.HOURLY:
                return new HourlyViewHolder(parent);

            case ViewType.AIR_QUALITY:
                return new AirQualityViewHolder(parent);

            case ViewType.ALLERGEN:
                return new AllergenViewHolder(parent);

            case ViewType.ASTRO:
                return new AstroViewHolder(parent);

            case ViewType.DETAILS:
                return new DetailsViewHolder(parent);

            default: // FOOTER.
                return new FooterViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractMainViewHolder holder, int position) {
        if (holder instanceof AbstractMainTrendCardViewHolder) {
            ((AbstractMainTrendCardViewHolder) holder).onBindView(
                    activity,
                    location,
                    itemWidth,
                    provider,
                    picker,
                    listAnimationEnabled,
                    itemAnimationEnabled,
                    firstCardPosition != null && firstCardPosition == position
            );
        } else if (holder instanceof AbstractMainCardViewHolder) {
            ((AbstractMainCardViewHolder) holder).onBindView(
                    activity,
                    location,
                    provider,
                    picker,
                    listAnimationEnabled,
                    itemAnimationEnabled,
                    firstCardPosition != null && firstCardPosition == position
            );
        } else {
            holder.onBindView(activity, location, provider, picker, listAnimationEnabled, itemAnimationEnabled);
        }
    }

    @Override
    public void onViewRecycled(@NonNull AbstractMainViewHolder holder) {
        holder.onRecycleView();
    }

    @Override
    public int getItemCount() {
        return viewTypeList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return viewTypeList.get(position);
    }

    private void ensureFirstCard() {
        firstCardPosition = null;
        for (int i = 0; i < getItemCount(); i ++) {
            int type = getItemViewType(i);
            if (type == ViewType.DAILY
                    || type == ViewType.HOURLY
                    || type == ViewType.AIR_QUALITY
                    || type == ViewType.ALLERGEN
                    || type == ViewType.ASTRO
                    || type == ViewType.DETAILS) {
                firstCardPosition = i;
                return;
            }
        }
    }

    public int getCurrentTemperatureTextHeight(RecyclerView recyclerView) {
        if (headerCurrentTemperatureTextHeight <= 0 && getItemCount() > 0) {
            AbstractMainViewHolder holder = (AbstractMainViewHolder) recyclerView.findViewHolderForAdapterPosition(0);
            if (holder instanceof HeaderViewHolder) {
                headerCurrentTemperatureTextHeight
                        = ((HeaderViewHolder) holder).getCurrentTemperatureHeight();
            }
        }
        return headerCurrentTemperatureTextHeight;
    }

    public void onScroll(RecyclerView recyclerView) {
        AbstractMainViewHolder holder;
        for (int i = 0; i < getItemCount(); i ++) {
            holder = (AbstractMainViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null && holder.getTop() < recyclerView.getMeasuredHeight()) {
                holder.enterScreen(pendingAnimatorList, listAnimationEnabled);
            }
        }
    }

    private static int getViewType(CardDisplay cardDisplay) {
        switch (cardDisplay) {
            case CARD_DAILY_OVERVIEW:
                return ViewType.DAILY;

            case CARD_HOURLY_OVERVIEW:
                return ViewType.HOURLY;

            case CARD_AIR_QUALITY:
                return ViewType.AIR_QUALITY;

            case CARD_ALLERGEN:
                return ViewType.ALLERGEN;

            case CARD_SUNRISE_SUNSET:
                return ViewType.ASTRO;

            default: // CARD_LIFE_DETAILS.
                return ViewType.DETAILS;
        }
    }
}
