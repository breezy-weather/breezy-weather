package wangdaye.com.geometricweather.main.ui.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Px;

import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;

public class FooterController extends AbstractMainItemController {

    private TextView text;

    public FooterController(@NonNull Activity activity,
                            @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                            @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                            @Px float cardRadius) {
        super(activity, activity.findViewById(R.id.container_main_footer), provider, picker,
                cardMarginsVertical, cardMarginsHorizontal, cardRadius);
        this.text = view.findViewById(R.id.container_main_footer_title);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(@NonNull Location location) {
        text.setText("Powered by " + location.getWeatherSource().getSourceUrl());
    }
}
