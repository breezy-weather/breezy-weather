package wangdaye.com.geometricweather.main.ui.adapter.main;

import android.animation.Animator;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.AbstractMainViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.AirQualityViewHolder;
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
    private @Nullable FirstCardHeaderController firstCardHeaderController;
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
        this.firstCardHeaderController = null;
        this.pendingAnimatorList = new ArrayList<>();
        this.headerCurrentTemperatureTextHeight = -1;
        this.listAnimationEnabled = listAnimationEnabled;
        this.itemAnimationEnabled = itemAnimationEnabled;

        if (location.getWeather() != null) {
            Weather weather = location.getWeather();
            String[] cardDisplayValues = SettingsOptionManager.getInstance(activity).getCardDisplayValues();
            viewTypeList.add(ViewType.HEADER);
            if (isDisplay(SettingsOptionManager.CARD_DAILY_OVERVIEW, cardDisplayValues)) {
                viewTypeList.add(ViewType.DAILY);
            }
            if (isDisplay(SettingsOptionManager.CARD_HOURLY_OVERVIEW, cardDisplayValues)) {
                viewTypeList.add(ViewType.HOURLY);
            }
            if (isDisplay(SettingsOptionManager.CARD_AIR_QUALITY, cardDisplayValues)
                    && weather.getCurrent().getAirQuality().isValid()) {
                viewTypeList.add(ViewType.AIR_QUALITY);
            }
            if (isDisplay(SettingsOptionManager.CARD_SUNRISE_SUNSET, cardDisplayValues)
                    && weather.getDailyForecast().size() != 0
                    && weather.getDailyForecast().get(0).sun().isValid()) {
                viewTypeList.add(ViewType.ASTRO);
            }
            if (isDisplay(SettingsOptionManager.CARD_LIFE_DETAILS, cardDisplayValues)) {
                viewTypeList.add(ViewType.DETAILS);
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
                        cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation, itemAnimationEnabled);
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

            case ViewType.ASTRO:
                holder = new AstroViewHolder(activity, parent, weatherView, provider, picker,
                        cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation, itemAnimationEnabled);
                break;

            case ViewType.DETAILS:
                holder = new DetailsViewHolder(activity, parent, weatherView, provider, picker,
                        cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation);
                break;

            default: // FOOTER.
                holder = new FooterViewHolder(activity, parent, provider, picker,
                        cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation);
                break;
        }

        if (firstCardType != null && firstCardType == viewType && holder.getContainer() != null) {
            firstCardHeaderController = new FirstCardHeaderController(
                    activity, holder.getContainer(), location, picker);
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
                    || type == ViewType.ASTRO
                    || type == ViewType.DETAILS) {
                firstCardType = type;
                return;
            }
        }
    }

    public void dragSort(RecyclerView recyclerView,
                         RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();
        viewTypeList.add(to, viewTypeList.remove(from));

        notifyItemMoved(from, to);

        if (from == 1 || to == 1) {
            if (firstCardHeaderController != null) {
                firstCardHeaderController.unbind();
            }
            ensureFirstCard();
            if (firstCardType == null) {
                firstCardHeaderController = null;
            } else {
                AbstractMainViewHolder holder = null;
                for (int i = 0; i < viewTypeList.size(); i ++) {
                    if (viewTypeList.get(i).equals(firstCardType)) {
                        holder = (AbstractMainViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                        break;
                    }
                }
                if (holder != null && holder.getContainer() != null) {
                    firstCardHeaderController.bind(holder.getContainer());
                } else {
                    firstCardHeaderController = null;
                }
            }
        }
    }

    public int getCurrentTemperatureTextHeight(RecyclerView recyclerView) {
        if (headerCurrentTemperatureTextHeight < 0 && getItemCount() > 0) {
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

    private static boolean isDisplay(String targetValue, String[] displayValues) {
        for(String s : displayValues){
            if(s.equals(targetValue))
                return true;
        }
        return false;
    }
}
