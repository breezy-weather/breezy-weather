package wangdaye.com.geometricweather.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Details adapter.
 * */

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder> {

    private List<Integer> iconList;
    private List<String[]> detailList;

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon;
        private TextView title;
        private TextView content;

        ViewHolder(View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.item_details_icon);
            this.title = itemView.findViewById(R.id.item_details_title);
            this.content = itemView.findViewById(R.id.item_details_content);
        }

        void onBindView(int iconId, String[] details) {
            icon.setImageResource(iconId);
            title.setText(details[0]);
            content.setText(details[1]);
        }
    }

    public DetailsAdapter(Context context, Weather weather) {
        this.iconList = new ArrayList<>();
        this.detailList = new ArrayList<>();

        if (!TextUtils.isEmpty(weather.index.simpleForecasts[1])) {
            iconList.add(R.drawable.ic_forecast);
            detailList.add(weather.index.simpleForecasts);
        }

        if (!TextUtils.isEmpty(weather.index.briefings[1])) {
            iconList.add(R.drawable.ic_nespaper);
            detailList.add(weather.index.briefings);
        }

        if (!TextUtils.isEmpty(weather.index.winds[1])) {
            iconList.add(R.drawable.ic_wind);
            detailList.add(weather.index.winds);
        }

        if (!TextUtils.isEmpty(weather.index.aqis[1])) {
            iconList.add(R.drawable.ic_pm);
            detailList.add(weather.index.aqis);
        }

        if (!TextUtils.isEmpty(weather.index.humidities[1])) {
            iconList.add(R.drawable.ic_leaf);
            detailList.add(new String[] {
                    context.getString(R.string.sensible_temp) + " : "
                            + ValueUtils.buildCurrentTemp(
                                    weather.realTime.sensibleTemp,
                            false,
                            GeometricWeather.getInstance().isFahrenheit()),
                    weather.index.humidities[1]});
        }

        if (!TextUtils.isEmpty(weather.index.uvs[1])) {
            iconList.add(R.drawable.ic_sun);
            detailList.add(weather.index.uvs);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.onBindView(iconList.get(position), detailList.get(position));
    }

    @Override
    public int getItemCount() {
        return detailList.size();
    }
}