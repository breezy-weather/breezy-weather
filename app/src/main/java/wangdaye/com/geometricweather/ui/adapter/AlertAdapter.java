package wangdaye.com.geometricweather.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Alert;

/**
 * Alert adapter.
 * */

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    private List<Alert> alertList;

    class ViewHolder extends RecyclerView.ViewHolder {
        // widget
        TextView title;
        TextView subtitle;
        TextView content;

        ViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.item_alert_title);
            this.subtitle = itemView.findViewById(R.id.item_alert_subtitle);
            this.content = itemView.findViewById(R.id.item_alert_content);
        }
    }

    public AlertAdapter(List<Alert> list) {
        this.alertList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(alertList.get(position).description);
        holder.subtitle.setText(alertList.get(position).publishTime);
        holder.content.setText(alertList.get(position).content);
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }
}

