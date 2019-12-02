package wangdaye.com.geometricweather.main.ui.adapter.main;

import android.animation.Animator;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.appearance.CardDisplay;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.AbstractMainCardViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.AbstractMainViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.AirQualityViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.AllergenViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.AstroViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.DailyViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.DetailsViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.FooterViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.HeaderViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.HourlyViewHolder;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class MainAdapter extends RecyclerView.Adapter<AbstractMainViewHolder> {

    private @NonNull GeoActivity activity;
    private @NonNull Location location;
    private @NonNull WeatherView weatherView;
    private @NonNull ResourceProvider provider;
    private @NonNull MainColorPicker picker;

    private @NonNull List<Integer> viewTypeList;
    private @Nullable Integer firstCardType;
    private @NonNull List<Animator> pendingAnimatorList;
    private int headerCurrentTemperatureTextHeight;
    private boolean listAnimationEnabled;
    private boolean itemAnimationEnabled;

    public MainAdapter(@NonNull GeoActivity activity, @NonNull Location location,
                       @NonNull WeatherView weatherView,
                       @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                       boolean listAnimationEnabled, boolean itemAnimationEnabled) {
        this.activity = activity;
        this.location = location;
        this.weatherView = weatherView;
        this.provider = provider;
        this.picker = picker;

        this.viewTypeList = new ArrayList<>();
        this.firstCardType = null;
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

    @NonNull
    @Override
    public AbstractMainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        float cardMarginsVertical = weatherView.getCardMarginsVertical(activity);
        float cardMarginsHorizontal = weatherView.getCardMarginsHorizontal(activity);
        float cardRadius = weatherView.getCardRadius(activity);
        float cardElevation = weatherView.getCardElevation(activity);

        AbstractMainViewHolder holder;
        switch (viewType) {
            case ViewType.HEADER:
                holder = new HeaderViewHolder(activity, parent, weatherView, provider, picker,
                        weatherView.getHeaderTextColor(activity), itemAnimationEnabled);
                break;

            case ViewType.DAILY:
                holder = new DailyViewHolder(activity, parent, weatherView, provider, picker,
                        cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation);
                break;

            case ViewType.HOURLY:
                holder = new HourlyViewHolder(activity, parent, weatherView, provider, picker,
                        cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation);
                break;

            case ViewType.AIR_QUALITY:
                holder = new AirQualityViewHolder(activity, parent, weatherView, provider, picker,
                        cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation, itemAnimationEnabled);
                break;

            case ViewType.ALLERGEN:
                holder = new AllergenViewHolder(activity, parent, weatherView, provider, picker,
                        cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation, itemAnimationEnabled);
                break;

            case ViewType.ASTRO:
                holder = new AstroViewHolder(activity, parent, weatherView, provider, picker,
                        cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation, itemAnimationEnabled);
                break;

            case ViewType.DETAILS:
                holder = new DetailsViewHolder(activity, parent, weatherView, provider, picker,
                        cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation);
                break;

            default: // FOOTER.
                holder = new FooterViewHolder(activity, parent, weatherView, provider, picker, cardMarginsVertical);
                break;
        }

        if (firstCardType != null && firstCardType == viewType
                && holder instanceof AbstractMainCardViewHolder) {
            LinearLayout container = ((AbstractMainCardViewHolder) holder).getHeaderContainer();
            if (container != null) {
                new FirstCardHeaderController(activity, location, picker).bind(container);
            }
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractMainViewHolder holder, int position) {
        holder.onBindView(location);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull AbstractMainViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onDestroy();
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
        firstCardType = null;
        for (int type : viewTypeList) {
            if (type == ViewType.DAILY
                    || type == ViewType.HOURLY
                    || type == ViewType.AIR_QUALITY
                    || type == ViewType.ALLERGEN
                    || type == ViewType.ASTRO
                    || type == ViewType.DETAILS) {
                firstCardType = type;
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
