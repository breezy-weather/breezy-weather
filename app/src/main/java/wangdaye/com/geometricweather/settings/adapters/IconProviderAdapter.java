package wangdaye.com.geometricweather.settings.adapters;

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

import java.util.List;

import james.adaptiveicon.AdaptiveIcon;
import james.adaptiveicon.AdaptiveIconView;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.ImageHelper;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;

public class IconProviderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity mActivity;
    private final List<ResourceProvider> mProviderList;
    private final OnItemClickedListener mListener;

    // holder.

    private class ViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout mContainer;
        private final AdaptiveIconView mIcon;
        private final TextView mTitle;
        private final AppCompatImageButton mPreviewButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            mContainer = itemView.findViewById(R.id.item_icon_provider_container);
            mIcon = itemView.findViewById(R.id.item_icon_provider_clearIcon);
            mTitle = itemView.findViewById(R.id.item_icon_provider_title);
            mPreviewButton = itemView.findViewById(R.id.item_icon_provider_previewButton);
        }

        void onBindView() {
            ResourceProvider provider = mProviderList.get(getAdapterPosition());

            mContainer.setOnClickListener(v -> mListener.onItemClicked(provider, getAdapterPosition()));

            Drawable drawable = provider.getProviderIcon();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    && drawable instanceof AdaptiveIconDrawable) {
                mIcon.setIcon(new AdaptiveIcon(
                        ((AdaptiveIconDrawable) drawable).getForeground(),
                        ((AdaptiveIconDrawable) drawable).getBackground(),
                        0.5
                ));
                mIcon.setPath(AdaptiveIconView.PATH_CIRCLE);
            } else {
                mIcon.setIcon(new AdaptiveIcon(drawable, null, 1));
            }

            mTitle.setText(provider.getProviderName());

            mPreviewButton.setOnClickListener(v ->
                    IntentHelper.startPreviewIconActivity(mActivity, provider.getPackageName()));
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
            ImageHelper.load(itemView.getContext(), appStore, R.drawable.ic_play_store);
            appStore.setOnClickListener(v ->
                    mListener.onAppStoreItemClicked("Geometric Weather Icon"));

            ImageHelper.load(
                    itemView.getContext(),
                    gitHub,
                    DisplayUtils.isDarkMode(itemView.getContext())
                            ? R.drawable.ic_github_light
                            : R.drawable.ic_github_dark
            );
            gitHub.setOnClickListener(v ->
                    mListener.onGitHubItemClicked("https://github.com/WangDaYeeeeee/IconProvider-For-GeometricWeather"));

            ImageHelper.load(itemView.getContext(), chronus, R.drawable.ic_chronus);
            chronus.setOnClickListener(v ->
                    mListener.onAppStoreItemClicked("Chronus Icon"));
        }
    }

    // interface.

    public interface OnItemClickedListener {
        void onItemClicked(ResourceProvider provider, int adapterPosition);
        void onAppStoreItemClicked(String query);
        void onGitHubItemClicked(String query);
    }

    // adapter.

    public IconProviderAdapter(Activity activity,
                               @NonNull List<ResourceProvider> providerList,
                               @Nullable OnItemClickedListener l) {
        mActivity = activity;
        mProviderList = providerList;
        mListener = l;
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
        return mProviderList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position < mProviderList.size() ? 1 : -1;
    }
}
