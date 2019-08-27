package wangdaye.com.geometricweather.main.ui.controller;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.DetailsAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class DetailsController extends AbstractMainItemController {

    private CardView card;

    private TextView title;
    private RecyclerView detailsRecyclerView;

    @NonNull private WeatherView weatherView;

    public DetailsController(@NonNull Activity activity, @NonNull WeatherView weatherView,
                             @NonNull ResourceProvider provider, @NonNull MainColorPicker picker) {
        super(activity, activity.findViewById(R.id.container_main_details), provider, picker);

        this.card = view.findViewById(R.id.container_main_details);
        this.title = view.findViewById(R.id.container_main_details_title);
        this.detailsRecyclerView = view.findViewById(R.id.container_main_details_recyclerView);

        this.weatherView = weatherView;
    }

    @Override
    public void onBindView(@NonNull Location location) {
        if (!isDisplay(SettingsOptionManager.CARD_LIFE_DETAILS)) {
            view.setVisibility(View.GONE);
            return;
        } else {
            view.setVisibility(View.VISIBLE);
        }

        if (location.weather != null) {
            card.setCardBackgroundColor(picker.getRootColor(context));

            title.setTextColor(
                    weatherView.getThemeColors(picker.isLightTheme())[0]
            );

            detailsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            detailsRecyclerView.setAdapter(new DetailsAdapter(context, location.weather, picker));
        }
    }
}
