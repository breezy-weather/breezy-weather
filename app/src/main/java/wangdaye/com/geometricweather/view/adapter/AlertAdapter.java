package wangdaye.com.geometricweather.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Weather;

/**
 * Alert adapter.
 * */

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {
    // data
    private List<Weather.Alarm> alarmList;

    /** <br> life cycle. */

    public AlertAdapter(List<Weather.Alarm> list) {
        this.alarmList = list;
    }

    /** <br> UI. */

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(alarmList.get(position).description);
        holder.subtitle.setText(alarmList.get(position).publishTime);
        holder.content.setText(alarmList.get(position).content);
    }

    /** <br> data. */

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    /** <br> inner class. */

    class ViewHolder extends RecyclerView.ViewHolder {
        // widget
        TextView title;
        TextView subtitle;
        TextView content;

        ViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.item_alert_title);
            this.subtitle = (TextView) itemView.findViewById(R.id.item_alert_subtitle);
            this.content = (TextView) itemView.findViewById(R.id.item_alert_content);
        }
    }
}

