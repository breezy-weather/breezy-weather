package wangdaye.com.geometricweather.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.ui.widget.TagView;

public class WeatherSourceTagAdapter extends RecyclerView.Adapter<WeatherSourceTagAdapter.ViewHolder> {

    private List<WeatherSource> sourceList;
    private int checkedIndex;

    @Nullable private OnTagCheckedListener listener;

    class ViewHolder extends RecyclerView.ViewHolder {

        private TagView tagView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tagView = itemView.findViewById(R.id.item_weather_source_tag);
            tagView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemChecked(!tagView.isChecked(), getAdapterPosition());
                }
            });
        }

        void onBindView(WeatherSource source, boolean checked) {
            tagView.setCheckedBackgroundColor(source.getSourceColor());
            tagView.setUncheckedTextColor(source.getSourceColor());
            setChecked(checked);
        }

        public void setChecked(boolean checked) {
            tagView.setChecked(checked);
        }
    }

    public WeatherSourceTagAdapter(List<WeatherSource> sourceList) {
        this(sourceList, 0);
    }

    public WeatherSourceTagAdapter(List<WeatherSource> sourceList, int checkedIndex) {
        this.sourceList = sourceList;
        this.checkedIndex = checkedIndex;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_weather_source_tag, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(sourceList.get(position), position == checkedIndex);
    }

    @Override
    public int getItemCount() {
        return sourceList.size();
    }

    public interface OnTagCheckedListener {
        void onItemChecked(boolean checked, int position);
    }

    public void setOnTagCheckedListener(@Nullable OnTagCheckedListener l) {
        listener = l;
    }
}
