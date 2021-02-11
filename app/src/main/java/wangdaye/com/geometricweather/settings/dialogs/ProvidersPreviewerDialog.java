package wangdaye.com.geometricweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.GeoDialog;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.resource.providers.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.adapters.IconProviderAdapter;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpters.AsyncHelper;
import wangdaye.com.geometricweather.utils.helpters.IntentHelper;

public class ProvidersPreviewerDialog extends GeoDialog {

    private CircularProgressView mProgress;
    private RecyclerView mList;

    @Nullable private OnIconProviderChangedListener listener;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_providers_previewer, null, false);

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
                    ResourcesProviderFactory.getProviderList(GeometricWeather.getInstance())
            ), this::bindAdapter);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void bindAdapter(List<ResourceProvider> providerList) {
        mList.setAdapter(new IconProviderAdapter(
                getActivity(),
                providerList,
                new IconProviderAdapter.OnItemClickedListener() {
                    @Override
                    public void onItemClicked(ResourceProvider helper, int adapterPosition) {
                        if (listener != null) {
                            listener.onIconProviderChanged(helper.getPackageName());
                        }
                        dismiss();
                    }

                    @Override
                    public void onAppStoreItemClicked(String query) {
                        if (getActivity() != null) {
                            IntentHelper.startAppStoreSearchActivity((GeoActivity) getActivity(), query);
                            dismiss();
                        }
                    }

                    @Override
                    public void onGitHubItemClicked(String query) {
                        if (getActivity() != null) {
                            IntentHelper.startWebViewActivity((GeoActivity) getActivity(), query);
                            dismiss();
                        }
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

    @Override
    public View getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_providers_previewer_container);
    }

    public interface OnIconProviderChangedListener {
        void onIconProviderChanged(String iconProvider);
    }

    public void setOnIconProviderChangedListener(@Nullable OnIconProviderChangedListener l) {
        listener = l;
    }
}
