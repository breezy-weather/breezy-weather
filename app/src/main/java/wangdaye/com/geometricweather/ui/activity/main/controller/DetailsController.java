package wangdaye.com.geometricweather.ui.activity.main.controller;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.ui.adapter.DetailsAdapter;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class DetailsController extends AbstractMainItemController {

    private CardView card;

    private TextView title;
    private RecyclerView detailsRecyclerView;

    @NonNull private WeatherView weatherView;

    public DetailsController(@NonNull Activity activity, @NonNull WeatherView weatherView) {
        super(activity, activity.findViewById(R.id.container_main_details));

        this.card = view.findViewById(R.id.container_main_details);
        this.title = view.findViewById(R.id.container_main_details_title);
        this.detailsRecyclerView = view.findViewById(R.id.container_main_details_recyclerView);

        this.weatherView = weatherView;
    }

    @Override
    public void onBindView(@NonNull Location location) {
        if (location.weather != null) {
            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorRoot));

            title.setTextColor(weatherView.getThemeColors()[0]);

            detailsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            detailsRecyclerView.setAdapter(new DetailsAdapter(context, location.weather));
        }
    }
}
