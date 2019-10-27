package wangdaye.com.geometricweather.settings.adapter;

import android.app.Activity;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import james.adaptiveicon.AdaptiveIcon;
import james.adaptiveicon.AdaptiveIconView;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class IconProviderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private List<ResourceProvider> providerList;
    private OnItemClickedListener listener;

    // holder.

    private class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout container;
        private AdaptiveIconView icon;
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
            ResourceProvider provider = providerList.get(getAdapterPosition());

            container.setOnClickListener(v -> listener.onItemClicked(provider, getAdapterPosition()));

            Drawable drawable = provider.getProviderIcon();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    && drawable instanceof AdaptiveIconDrawable) {
                icon.setIcon(new AdaptiveIcon(
                        ((AdaptiveIconDrawable) drawable).getForeground(),
                        ((AdaptiveIconDrawable) drawable).getBackground(),
                        0.5
                ));
                icon.setPath(AdaptiveIconView.PATH_CIRCLE);
            } else {
                icon.setIcon(new AdaptiveIcon(drawable, null, 1));
            }

            title.setText(provider.getProviderName());

            previewButton.setOnClickListener(v ->
                    IntentHelper.startPreviewIconActivity(activity, provider.getPackageName()));
        }
    }

    private class GetMoreViewHolder extends RecyclerView.ViewHolder {


        private AppCompatImageView appStore;
        private AppCompatImageView gitHub;
        private AppCompatImageView chronus;

        GetMoreViewHolder(@NonNull View itemView) {
            super(itemView);

            appStore = itemView.findViewById(R.id.item_icon_provider_get_more_appStore);
            gitHub = itemView.findViewById(R.id.item_icon_provider_get_more_gitHub);
            chronus = itemView.findViewById(R.id.item_icon_provider_get_more_chronus);
        }

        void onBindView() {
            Glide.with(itemView.getContext())
                    .load(R.drawable.ic_play_store)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(appStore);
            appStore.setOnClickListener(v ->
                    listener.onAppStoreItemClicked("Geometric Weather Icon"));

            Glide.with(itemView.getContext())
                    .load(
                            DisplayUtils.isDarkMode(itemView.getContext())
                                    ? R.drawable.ic_github_light
                                    : R.drawable.ic_github_dark
                    ).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(gitHub);
            gitHub.setOnClickListener(v ->
                    listener.onGitHubItemClicked("https://github.com/WangDaYeeeeee/IconProvider-For-GeometricWeather"));

            Glide.with(itemView.getContext())
                    .load(R.drawable.ic_chronus)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(chronus);
            chronus.setOnClickListener(v ->
                    listener.onAppStoreItemClicked("Chronus Icon"));
        }
    }

    // adapter.

    public IconProviderAdapter(Activity activity,
                               @NonNull List<ResourceProvider> providerList,
                               @Nullable OnItemClickedListener l) {
        this.activity = activity;
        this.providerList = providerList;
        this.listener = l;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_icon_provider, parent, false)
            );
        } else {
            return new GetMoreViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_icon_provider_get_more, parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GetMoreViewHolder) {
            ((GetMoreViewHolder) holder).onBindView();
            return;
        }
        ((ViewHolder) holder).onBindView();
    }

    @Override
    public int getItemCount() {
        return providerList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position < providerList.size() ? 1 : -1;
    }

    public interface OnItemClickedListener {
        void onItemClicked(ResourceProvider helper, int adapterPosition);
        void onAppStoreItemClicked(String query);
        void onGitHubItemClicked(String query);
    }
}
