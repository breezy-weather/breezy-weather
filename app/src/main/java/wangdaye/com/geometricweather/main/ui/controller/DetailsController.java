package wangdaye.com.geometricweather.main.ui.controller;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.DetailsAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class DetailsController extends AbstractMainItemController {

    private CardView card;

    private TextView title;
    private RecyclerView detailsRecyclerView;

    @NonNull private WeatherView weatherView;

    public DetailsController(@NonNull Activity activity, @NonNull WeatherView weatherView,
                             @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                             @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                             @Px float cardRadius) {
        super(activity, activity.findViewById(R.id.container_main_details), provider, picker,
                cardMarginsVertical, cardMarginsHorizontal, cardRadius);

        this.card = view.findViewById(R.id.container_main_details);
        this.title = view.findViewById(R.id.container_main_details_title);
        this.detailsRecyclerView = view.findViewById(R.id.container_main_details_recyclerView);

        this.weatherView = weatherView;
    }

    @Override
    public void onBindView(@NonNull Location location) {
        if (location.getWeather() != null) {
            card.setCardBackgroundColor(picker.getRootColor(context));

            title.setTextColor(
                    weatherView.getThemeColors(picker.isLightTheme())[0]
            );

            detailsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            detailsRecyclerView.setAdapter(new DetailsAdapter(context, location.getWeather(), picker));
        }
    }
}
