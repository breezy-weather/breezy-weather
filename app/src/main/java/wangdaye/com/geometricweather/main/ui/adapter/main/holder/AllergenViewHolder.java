package wangdaye.com.geometricweather.main.ui.adapter.main.holder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.PollenAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class AllergenViewHolder extends AbstractMainViewHolder {

    private CardView card;
    private TextView title;

    private RecyclerView recyclerView;
    private PollenAdapter adapter;
    private Button getMore;

    @NonNull private WeatherView weatherView;
    @Nullable private Weather weather;

    private boolean enable;

    public AllergenViewHolder(@NonNull Activity activity, ViewGroup parent, @NonNull WeatherView weatherView,
                              @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                              @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                              @Px float cardRadius, @Px float cardElevation,
                              boolean itemAnimationEnabled) {
        super(activity, LayoutInflater.from(activity).inflate(R.layout.container_main_pollen, parent, false),
                provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation, itemAnimationEnabled);

        this.card = itemView.findViewById(R.id.container_main_pollen);
        this.title = itemView.findViewById(R.id.container_main_pollen_title);
        this.recyclerView = itemView.findViewById(R.id.container_main_pollen_recyclerView);
        this.getMore = itemView.findViewById(R.id.container_main_pollen_more);

        this.weatherView = weatherView;
    }

    @Override
    public void onBindView(@NonNull Location location) {
        weather = location.getWeather();
        assert weather != null;

        enable = true;

        card.setCardBackgroundColor(picker.getRootColor(context));
        title.setTextColor(weatherView.getThemeColors(picker.isLightTheme())[0]);

        adapter = new PollenAdapter(context, weather, picker, itemAnimationEnabled);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        getMore.setTextColor(picker.getTextTitleColor(context));
    }

    @Override
    public void onEnterScreen() {
        if (itemAnimationEnabled && enable && weather != null) {
            adapter.executeAnimation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.cancelAnimation();
        }
    }
}