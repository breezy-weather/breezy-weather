package wangdaye.com.geometricweather.main.adapter;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainColorPicker;

/**
 * Details adapter.
 * */

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder> {

    private List<Index> indexList;
    private MainColorPicker colorPicker;

    private class Index {
        @DrawableRes int iconId;
        String title;
        String content;

        Index(@DrawableRes int iconId, String title, String content) {
            this.iconId = iconId;
            this.title = title;
            this.content = content;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private AppCompatImageView icon;
        private TextView title;
        private TextView content;

        ViewHolder(View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.item_details_icon);
            this.title = itemView.findViewById(R.id.item_details_title);
            this.content = itemView.findViewById(R.id.item_details_content);
        }

        void onBindView(Index index) {
            Context context = itemView.getContext();

            icon.setImageResource(index.iconId);
            icon.setSupportImageTintList(
                    ColorStateList.valueOf(colorPicker.getTextContentColor(context))
            );

            title.setText(index.title);
            title.setTextColor(colorPicker.getTextContentColor(context));

            content.setText(index.content);
            content.setTextColor(colorPicker.getTextSubtitleColor(context));
        }
    }

    public DetailsAdapter(Context context, Weather weather, MainColorPicker colorPicker) {
        this.indexList = new ArrayList<>();

        indexList.add(new Index(
                R.drawable.ic_wind,
                weather.index.currentWind,
                weather.index.dailyWind));

        indexList.add(new Index(
                R.drawable.ic_flower,
                weather.index.sensibleTemp,
                weather.index.humidity));

        if (!TextUtils.isEmpty(weather.index.uv)) {
            indexList.add(new Index(
                    R.drawable.ic_uv,
                    context.getString(R.string.uv_index),
                    weather.index.uv));
        }

        if (!TextUtils.isEmpty(weather.index.pressure)) {
            indexList.add(new Index(
                    R.drawable.ic_gauge,
                    context.getString(R.string.pressure),
                    weather.index.pressure));
        }

        if (!TextUtils.isEmpty(weather.index.visibility)) {
            indexList.add(new Index(
                    R.drawable.ic_eye,
                    context.getString(R.string.visibility),
                    weather.index.visibility));
        }

        if (!TextUtils.isEmpty(weather.index.dewPoint)) {
            indexList.add(new Index(
                    R.drawable.ic_water,
                    context.getString(R.string.dew_point),
                    weather.index.dewPoint));
        }

        this.colorPicker = colorPicker;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(indexList.get(position));
    }

    @Override
    public int getItemCount() {
        return indexList.size();
    }
}