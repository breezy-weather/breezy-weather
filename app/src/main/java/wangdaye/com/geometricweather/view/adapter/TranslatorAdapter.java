package wangdaye.com.geometricweather.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Translator;

/**
 * Translator adapter.
 * */

public class TranslatorAdapter extends RecyclerView.Adapter<TranslatorAdapter.ViewHolder> {
    // widget
    private Context context;

    // data
    private List<Translator> itemList;

    /** <br> life cycle. */

    public TranslatorAdapter(Context context) {
        this.context = context;
        this.itemList = Translator.buildTranslatorList();
    }

    /** <br> UI. */

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_translator, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(itemList.get(position).name);
        holder.subtitle.setText(itemList.get(position).email);
        Glide.with(context)
                .load(itemList.get(position).flagResId)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.flag);
    }

    /** <br> data. */

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /** <br> inner class. */

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        // widget
        TextView title;
        TextView subtitle;
        ImageView flag;

        ViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.item_translator_title);
            this.subtitle = (TextView) itemView.findViewById(R.id.item_translator_subtitle);
            this.flag = (ImageView) itemView.findViewById(R.id.item_translator_flag);
            itemView.findViewById(R.id.item_translator).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_translator:
                    context.startActivity(
                            new Intent(
                                    Intent.ACTION_SENDTO,
                                    Uri.parse("mailto:" + itemList.get(getAdapterPosition()).email)));
                    break;
            }
        }
    }
}
