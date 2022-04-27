package wangdaye.com.geometricweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.common.ui.widgets.AnimatableIconView;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;

public class AnimatableIconDialog extends GeoDialog {

    public static void show(Context context,
                            WeatherCode code,
                            boolean daytime,
                            ResourceProvider provider) {
        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_animatable_icon, null, false);
        initWidget(view, code, daytime, provider);

        new MaterialAlertDialogBuilder(context)
                .setTitle(code.name() + (daytime ? "_DAY" : "_NIGHT"))
                .setView(view)
                .show();
    }

    @SuppressLint("SetTextI18n")
    private static void initWidget(View view,
                                   WeatherCode code,
                                   boolean daytime,
                                   ResourceProvider provider) {
        AnimatableIconView iconView = view.findViewById(R.id.dialog_animatable_icon_icon);
        iconView.setAnimatableIcon(
                ResourceHelper.getWeatherIcons(provider, code, daytime),
                ResourceHelper.getWeatherAnimators(provider, code, daytime)
        );

        FrameLayout container = view.findViewById(R.id.dialog_animatable_icon_container);
        container.setOnClickListener(v -> iconView.startAnimators());
    }
}
