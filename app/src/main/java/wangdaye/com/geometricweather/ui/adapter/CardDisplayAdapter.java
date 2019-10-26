package wangdaye.com.geometricweather.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.appearance.CardDisplay;

public class CardDisplayAdapter extends RecyclerView.Adapter<CardDisplayAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        RelativeLayout container;
        TextView title;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.item_card_display);
            title = itemView.findViewById(R.id.item_card_display_title);
            deleteButton = itemView.findViewById(R.id.item_card_display_deleteBtn);

            container.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        }

        void onBindView(CardDisplay cardDisplay) {

        }

        @Override
        public void onClick(View v) {

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
