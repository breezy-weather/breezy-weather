package wangdaye.com.geometricweather.search.ui.adapter.source;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.databinding.ItemWeatherSourceBinding;

public class WeatherSourceAdapter extends RecyclerView.Adapter<WeatherSourceHolder> {

    private final List<WeatherSourceModel> mModelList;

    public WeatherSourceAdapter(List<WeatherSource> enabledSources) {
        mModelList = new ArrayList<>();
        for (WeatherSource source : WeatherSource.ACCU.getDeclaringClass().getEnumConstants()) {
            mModelList.add(new WeatherSourceModel(source, enabledSources.contains(source)));
        }
    }

    @NonNull
    @Override
    public WeatherSourceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WeatherSourceHolder(
                ItemWeatherSourceBinding.inflate(
                        LayoutInflater.from(parent.getContext())
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherSourceHolder holder, int position) {
        holder.onBind(mModelList.get(position));
    }

    @Override
    public int getItemCount() {
        return mModelList.size();
    }

    public List<WeatherSource> getValidWeatherSources() {
        List<WeatherSource> list = new ArrayList<>();
        for (WeatherSourceModel model : mModelList) {
            if (model.isEnabled()) {
                list.add(model.getSource());
            }
        }
        return list;
    }
}
