package wangdaye.com.geometricweather.main.ui.adapter.main.holder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.main.ui.adapter.DetailsAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class DetailsViewHolder extends AbstractMainViewHolder {

    private CardView card;

    private TextView title;
    private RecyclerView detailsRecyclerView;

    @NonNull private WeatherView weatherView;

    public DetailsViewHolder(@NonNull Activity activity, ViewGroup parent, @NonNull WeatherView weatherView,
                             @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                             @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                             @Px float cardRadius, @Px float cardElevation) {
        super(activity, LayoutInflater.from(activity).inflate(R.layout.container_main_details, parent, false),
                provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius, cardElevation);

        this.card = itemView.findViewById(R.id.container_main_details);
        this.title = itemView.findViewById(R.id.container_main_details_title);
        this.detailsRecyclerView = itemView.findViewById(R.id.container_main_details_recyclerView);

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
