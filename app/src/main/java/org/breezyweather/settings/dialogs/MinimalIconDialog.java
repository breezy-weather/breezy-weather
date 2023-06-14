package org.breezyweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.breezyweather.common.basic.models.weather.WeatherCode;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.R;

public class MinimalIconDialog {

    public static void show(Context context,
                            WeatherCode code,
                            boolean daytime,
                            ResourceProvider provider) {
        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_minimal_icon, null, false);
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
