package wangdaye.com.geometricweather.settings.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.ui.adapter.IconProviderAdapter;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class ProvidersPreviewerDialog extends GeoDialogFragment {

    private CoordinatorLayout container;
    @Nullable private OnIconProviderChangedListener listener;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_providers_previewer, null, false);

        if (getActivity() != null) {
            this.container = view.findViewById(R.id.dialog_providers_previewer_container);

            RecyclerView recyclerView = view.findViewById(R.id.dialog_providers_previewer_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(
                    new IconProviderAdapter(
                            getActivity(),
                            ResourcesProviderFactory.getResourceHelperList(getActivity()),
                            new IconProviderAdapter.OnItemClickedListener() {
                                @Override
                                public void onItemClicked(ResourceProvider helper, int adapterPosition) {
                                    if (listener != null) {
                                        listener.onIconProviderChanged(helper.getPackageName());
                                    }
                                    dismiss();
                                }

                                @Override
                                public void onGetMoreItemClicked(String query) {
                                    IntentHelper.startAppStoreSearchActivity(getActivity(), query);
                                    dismiss();
                                }
                            }
                    )
            );
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    public interface OnIconProviderChangedListener {
        void onIconProviderChanged(String iconProvider);
    }

    public void setOnIconProviderChangedListener(@Nullable OnIconProviderChangedListener l) {
        listener = l;
    }
}
