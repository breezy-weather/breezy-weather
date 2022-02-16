package wangdaye.com.geometricweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.DefaultResourceProvider;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory;

/**
 * Adaptive icon dialog.
 * */
public class MinimalIconDialog extends GeoDialog {

    private static final String KEY_WEATHER_CODE = "weather_code";
    private static final String KEY_DAYTIME = "daytime";
    private static final String KEY_RESOURCE_PROVIDER = "resource_provider";

    public static MinimalIconDialog getInstance(WeatherCode code,
                                                boolean daytime,
                                                ResourceProvider provider) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_WEATHER_CODE, code.name());
        bundle.putBoolean(KEY_DAYTIME, daytime);
        bundle.putString(KEY_RESOURCE_PROVIDER, provider.getPackageName());

        MinimalIconDialog dialog = new MinimalIconDialog();
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
                R.layout.dialog_minimal_icon, container, false);
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


        AppCompatImageView xmlIcon = view.findViewById(R.id.dialog_minimal_icon_xmlIcon);
        xmlIcon.setImageDrawable(ResourceHelper.getMinimalXmlIcon(provider, code, daytime));

        TextView titleView = view.findViewById(R.id.dialog_minimal_icon_title);
        titleView.setText(code.name() + (daytime ? "_DAY" : "_NIGHT"));

        AppCompatImageView lightIconView = view.findViewById(R.id.dialog_minimal_icon_lightIcon);
        lightIconView.setImageDrawable(ResourceHelper.getWidgetNotificationIcon(
                provider, code, daytime, true, "light"));

        AppCompatImageView greyIconView = view.findViewById(R.id.dialog_minimal_icon_greyIcon);
        greyIconView.setImageDrawable(ResourceHelper.getWidgetNotificationIcon(
                provider, code, daytime, true, "grey"));

        AppCompatImageView darkIconView = view.findViewById(R.id.dialog_minimal_icon_darkIcon);
        darkIconView.setImageDrawable(ResourceHelper.getWidgetNotificationIcon(
                provider, code, daytime, true, "dark"));
    }
}
