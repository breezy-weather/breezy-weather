package wangdaye.com.geometricweather.ui.activity.main.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.utils.ValueUtils;

public class FooterController extends AbstractMainItemController {

    private TextView text;

    public FooterController(@NonNull Activity activity) {
        super(activity, activity.findViewById(R.id.container_main_footer));
        this.text = view.findViewById(R.id.container_main_footer);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(@NonNull Location location) {
        text.setText("Powered by " + ValueUtils.getWeatherSource(context, location.source));
    }
}
