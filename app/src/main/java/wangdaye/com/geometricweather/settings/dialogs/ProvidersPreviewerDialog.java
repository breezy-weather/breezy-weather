package wangdaye.com.geometricweather.settings.dialogs;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.adapters.IconProviderAdapter;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;

public class ProvidersPreviewerDialog extends GeoDialog {

    private CircularProgressView mProgress;
    private RecyclerView mList;

    public static final String ACTION_RESOURCE_PROVIDER_CHANGED
            = "com.wangdaye.geometricweather.RESOURCE_PROVIDER_CHANGED";
    public static final String KEY_PACKAGE_NAME = "package_name";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_providers_previewer, container, false);

        if (getActivity() != null) {
            Context context = getActivity();

            TextView title = view.findViewById(R.id.dialog_providers_previewer_title);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                title.setTranslationZ(0);
            }

            mProgress = view.findViewById(R.id.dialog_providers_previewer_progress);
            mProgress.setVisibility(View.VISIBLE);

            mList = view.findViewById(R.id.dialog_providers_previewer_list);
            mList.setLayoutManager(new LinearLayoutManager(context));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mList.addOnScrollListener(new RecyclerView.OnScrollListener() {

                    final float elevation = DisplayUtils.dpToPx(context, 2);

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        if (!mList.canScrollVertically(-1)) {
                            mList.setTranslationZ(0);
                        } else {
                            mList.setTranslationZ(elevation);
                        }
                    }
                });
            }
            mList.setVisibility(View.GONE);

            AsyncHelper.runOnIO(emitter -> emitter.send(
                    ResourcesProviderFactory.getProviderList(GeometricWeather.getInstance()),
                    true
            ), (AsyncHelper.Callback<List<ResourceProvider>>) (resourceProviders, done) -> bindAdapter(
                    resourceProviders
            ));
        }
        return view;
    }

    private void bindAdapter(List<ResourceProvider> providerList) {
        mList.setAdapter(new IconProviderAdapter(
                getActivity(),
                providerList,
                new IconProviderAdapter.OnItemClickedListener() {
                    @Override
                    public void onItemClicked(ResourceProvider provider, int adapterPosition) {
                        Intent intent = new Intent(ACTION_RESOURCE_PROVIDER_CHANGED);
                        intent.putExtra(KEY_PACKAGE_NAME, provider.getPackageName());
                        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);

                        dismiss();
                    }

                    @Override
                    public void onAppStoreItemClicked(String query) {
                        IntentHelper.startAppStoreSearchActivity(requireContext(), query);
                        dismiss();
                    }

                    @Override
                    public void onGitHubItemClicked(String query) {
                        IntentHelper.startWebViewActivity(requireContext(), query);
                        dismiss();
                    }
                }
        ));

        Animation show = new AlphaAnimation(0f, 1f);
        show.setDuration(300);
        show.setInterpolator(new FastOutSlowInInterpolator());
        mList.startAnimation(show);
        mList.setVisibility(View.VISIBLE);

        Animation out = new AlphaAnimation(1f, 0f);
        show.setDuration(300);
        show.setInterpolator(new FastOutSlowInInterpolator());
        mProgress.startAnimation(out);
        mProgress.setVisibility(View.GONE);
    }
}
