package wangdaye.com.geometricweather.settings.dialog;

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
import androidx.fragment.app.DialogFragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.util.List;

import cn.nekocode.rxlifecycle.LifecycleEvent;
import cn.nekocode.rxlifecycle.compact.RxLifecycleCompact;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.adapter.IconProviderAdapter;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class ProvidersPreviewerDialog extends DialogFragment {

    private CircularProgressView progress;
    private RecyclerView list;

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

            this.progress = view.findViewById(R.id.dialog_providers_previewer_progress);
            progress.setVisibility(View.VISIBLE);

            this.list = view.findViewById(R.id.dialog_providers_previewer_list);
            list.setLayoutManager(new LinearLayoutManager(context));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                list.addOnScrollListener(new RecyclerView.OnScrollListener() {

                    float elevation = DisplayUtils.dpToPx(context, 2);

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        if (!list.canScrollVertically(-1)) {
                            list.setTranslationZ(0);
                        } else {
                            list.setTranslationZ(elevation);
                        }
                    }
                });
            }
            list.setVisibility(View.GONE);

            Observable.create((ObservableOnSubscribe<List<ResourceProvider>>) emitter ->
                    emitter.onNext(
                            ResourcesProviderFactory.getProviderList(
                                    GeometricWeather.getInstance()
                            )
                    )
            ).compose(RxLifecycleCompact.bind(this).disposeObservableWhen(LifecycleEvent.DESTROY))
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(this::bindAdapter)
                    .subscribe();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void bindAdapter(List<ResourceProvider> providerList) {
        list.setAdapter(new IconProviderAdapter(
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
        list.startAnimation(show);
        list.setVisibility(View.VISIBLE);

        Animation out = new AlphaAnimation(1f, 0f);
        show.setDuration(300);
        show.setInterpolator(new FastOutSlowInInterpolator());
        progress.startAnimation(out);
        progress.setVisibility(View.GONE);
    }

    public interface OnIconProviderChangedListener {
        void onIconProviderChanged(String iconProvider);
    }

    public void setOnIconProviderChangedListener(@Nullable OnIconProviderChangedListener l) {
        listener = l;
    }
}
