package wangdaye.com.geometricweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.DefaultResourceProvider;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.common.ui.widgets.AnimatableIconView;

/**
 * Animatable icon dialog.
 * */
public class AnimatableIconDialog extends GeoDialog {

    private static final String KEY_WEATHER_CODE = "weather_code";
    private static final String KEY_DAYTIME = "daytime";
    private static final String KEY_RESOURCE_PROVIDER = "resource_provider";

    public static AnimatableIconDialog getInstance(WeatherCode code,
                                                   boolean daytime,
                                                   ResourceProvider provider) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_WEATHER_CODE, code.name());
        bundle.putBoolean(KEY_DAYTIME, daytime);
        bundle.putString(KEY_RESOURCE_PROVIDER, provider.getPackageName());

        AnimatableIconDialog dialog = new AnimatableIconDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_animatable_icon, container, false);
        initWidget(view);
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void initWidget(View view) {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }

        String codeName = bundle.getString(KEY_WEATHER_CODE);
        WeatherCode code = codeName == null ? WeatherCode.CLEAR : WeatherCode.valueOf(codeName);

        boolean daytime = bundle.getBoolean(KEY_DAYTIME, true);

        String providerPackageName = bundle.getString(KEY_RESOURCE_PROVIDER);
        ResourceProvider provider = providerPackageName == null
                ? new DefaultResourceProvider()
                : ResourcesProviderFactory.getNewInstance(providerPackageName);

        TextView titleView = view.findViewById(R.id.dialog_animatable_icon_title);
        titleView.setText(code.name() + (daytime ? "_DAY" : "_NIGHT"));

        AnimatableIconView iconView = view.findViewById(R.id.dialog_animatable_icon_icon);
        iconView.setAnimatableIcon(
                ResourceHelper.getWeatherIcons(provider, code, daytime),
                ResourceHelper.getWeatherAnimators(provider, code, daytime)
        );

        CoordinatorLayout container = view.findViewById(R.id.dialog_animatable_icon_container);
        container.setOnClickListener(v -> iconView.startAnimators());
    }
}
