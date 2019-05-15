package wangdaye.com.geometricweather.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class IconProviderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Item> itemList;
    private OnItemClickedListener listener;

    // item.

    private interface Item {}

    private class ProviderItem implements Item {
        ResourceProvider provider;

        ProviderItem(ResourceProvider provider) {
            this.provider = provider;
        }
    }

    private class GetMoreItem implements Item {
        @DrawableRes int resId;
        @NonNull String title;
        @NonNull String query;

        GetMoreItem(@DrawableRes int resId, @NonNull String title, @NonNull String query) {
            this.resId = resId;
            this.title = title;
            this.query = query;
        }
    }

    // holder.

    private class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout container;
        private AppCompatImageView icon;
        private TextView title;
        private AppCompatImageButton previewButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.item_icon_provider_container);
            icon = itemView.findViewById(R.id.item_icon_provider_clearIcon);
            title = itemView.findViewById(R.id.item_icon_provider_title);
            previewButton = itemView.findViewById(R.id.item_icon_provider_previewButton);
        }

        void onBindView() {
            ResourceProvider helper = (
                    (ProviderItem) itemList.get(getAdapterPosition())
            ).provider;

            container.setOnClickListener(v -> listener.onItemClicked(helper, getAdapterPosition()));

            icon.setImageDrawable(helper.getProviderIcon());
            title.setText(helper.getProviderName());

            previewButton.setVisibility(View.VISIBLE);
            previewButton.setOnClickListener(v -> {
                if (GeometricWeather.getInstance().getTopActivity() != null) {
                    IntentHelper.startPreviewIconActivity(
                            GeometricWeather.getInstance().getTopActivity(),
                            helper.getPackageName()
                    );
                }
            });
        }

        void onBindGetMoreItemView(GetMoreItem item) {
            container.setOnClickListener(v -> listener.onGetMoreItemClicked(item.query));

            Glide.with(icon.getContext()).load(item.resId).into(icon);
            title.setText(item.title);

            previewButton.setVisibility(View.GONE);
            previewButton.setOnClickListener(null);
        }
    }

    // adapter.

    public IconProviderAdapter(@NonNull Context context,
                               @NonNull List<ResourceProvider> providerList,
                               @Nullable OnItemClickedListener l) {
        this.itemList = new ArrayList<>();
        for (ResourceProvider p : providerList) {
            itemList.add(new ProviderItem(p));
        }
        itemList.add(
                new GetMoreItem(
                        R.drawable.ic_play_store,
                        context.getString(R.string.get_more),
                        "GeometricWeather Icons"
                )
        );

        this.listener = l;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_icon_provider, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (itemList.get(position) instanceof GetMoreItem) {
            ((ViewHolder) holder).onBindGetMoreItemView((GetMoreItem) itemList.get(position));
            return;
        }
        ((ViewHolder) holder).onBindView();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public interface OnItemClickedListener {
        void onItemClicked(ResourceProvider helper, int adapterPosition);
        void onGetMoreItemClicked(String query);
    }
}
