package org.breezyweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Random;

import james.adaptiveicon.AdaptiveIcon;
import james.adaptiveicon.AdaptiveIconView;
import org.breezyweather.common.basic.models.weather.WeatherCode;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.R;

public class AdaptiveIconDialog {

    public static void show(Context context,
                            WeatherCode code,
                            boolean daytime,
                            ResourceProvider provider) {
        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_adaptive_icon, null, false);
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
        AdaptiveIconView iconView = view.findViewById(R.id.dialog_adaptive_icon_icon);
        iconView.setIcon(
                new AdaptiveIcon(
                        ResourceHelper.getShortcutsForegroundIcon(provider, code, daytime),
                        new ColorDrawable(Color.TRANSPARENT),
                        0.5
                )
        );
        iconView.setPath(new Random().nextInt(AdaptiveIconView.PATH_TEARDROP + 1));
    }
}
