package wangdaye.com.geometricweather.main.adapter.main.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.main.adapter.DetailsAdapter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;

public class DetailsViewHolder extends AbstractMainCardViewHolder {

    private CardView card;

    private TextView title;
    private RecyclerView detailsRecyclerView;

    public DetailsViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.container_main_details, parent, false));

        this.card = itemView.findViewById(R.id.container_main_details);
        this.title = itemView.findViewById(R.id.container_main_details_title);
        this.detailsRecyclerView = itemView.findViewById(R.id.container_main_details_recyclerView);
    }

    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider, @NonNull MainThemePicker picker,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, picker,
                listAnimationEnabled, itemAnimationEnabled, firstCard);

        if (location.getWeather() != null) {
            card.setCardBackgroundColor(picker.getRootColor(context));

            title.setTextColor(picker.getWeatherThemeColors()[0]);

            detailsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            detailsRecyclerView.setAdapter(new DetailsAdapter(context, location.getWeather(), picker));
        }
    }
}
