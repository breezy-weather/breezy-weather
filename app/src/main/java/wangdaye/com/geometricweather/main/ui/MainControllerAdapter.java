package wangdaye.com.geometricweather.main.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.ui.controller.AbstractMainItemController;
import wangdaye.com.geometricweather.main.ui.controller.AqiController;
import wangdaye.com.geometricweather.main.ui.controller.DetailsController;
import wangdaye.com.geometricweather.main.ui.controller.FirstTrendCardController;
import wangdaye.com.geometricweather.main.ui.controller.FooterController;
import wangdaye.com.geometricweather.main.ui.controller.HeaderController;
import wangdaye.com.geometricweather.main.ui.controller.SecondTrendCardController;
import wangdaye.com.geometricweather.main.ui.controller.SunMoonController;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class MainControllerAdapter {

    private List<AbstractMainItemController> controllerList;
    private int headerCurrentTemperatureTextHeight;
    private int screenHeight;

    public MainControllerAdapter(@NonNull Context context) {
        this.controllerList = new ArrayList<>();

        this.headerCurrentTemperatureTextHeight = -1;
        this.screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    public void bind(@NonNull Activity activity,
                     @NonNull ViewGroup scrollContainer, @NonNull WeatherView weatherView,
                     @NonNull Location location,
                     @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                     int marginsHorizontal) {
        if (scrollContainer.getChildCount() != 0) {
            scrollContainer.removeAllViews();
        }
        if (location.getWeather() != null) {
            LayoutInflater inflater = LayoutInflater.from(scrollContainer.getContext());

            inflater.inflate(R.layout.container_main_header, scrollContainer, true);
            controllerList.add(new HeaderController(activity, weatherView, provider, picker));

            inflater.inflate(R.layout.container_main_first_trend_card, scrollContainer, true);
            controllerList.add(new FirstTrendCardController(activity, weatherView, provider, picker, marginsHorizontal));

            inflater.inflate(R.layout.container_main_second_trend_card, scrollContainer, true);
            controllerList.add(new SecondTrendCardController(activity, weatherView, provider, picker, marginsHorizontal));

            inflater.inflate(R.layout.container_main_aqi, scrollContainer, true);
            controllerList.add(new AqiController(activity, weatherView, provider, picker));

            inflater.inflate(R.layout.container_main_sun_moon, scrollContainer, true);
            controllerList.add(new SunMoonController(activity, weatherView, provider, picker));

            inflater.inflate(R.layout.container_main_details, scrollContainer, true);
            controllerList.add(new DetailsController(activity, weatherView, provider, picker));

            inflater.inflate(R.layout.container_main_footer, scrollContainer, true);
            controllerList.add(new FooterController(activity, provider, picker));
        }
        for (int i = 0; i < controllerList.size(); i ++) {
            controllerList.get(i).onBindView(location);
        }
        scrollContainer.post(() -> onScroll(0, 0));
    }

    public void unbind(@NonNull ViewGroup scrollContainer) {
        for (int i = 0; i < controllerList.size(); i ++) {
            controllerList.get(i).onDestroy();
        }
        controllerList.clear();
        if (scrollContainer.getChildCount() != 0) {
            scrollContainer.removeAllViews();
        }
    }

    public int getCurrentTemperatureTextHeight() {
        if (headerCurrentTemperatureTextHeight < 0) {
            if (controllerList.size() > 0) {
                AbstractMainItemController controller = controllerList.get(0);
                if (controller instanceof HeaderController) {
                    headerCurrentTemperatureTextHeight
                            = ((HeaderController) controller).getCurrentTemperatureHeight();
                }
            }
        }
        return headerCurrentTemperatureTextHeight;
    }

    public void onScroll(int oldScrollY, int scrollY) {
        if (oldScrollY > scrollY) {
            return;
        }
        for (int i = 0; i < controllerList.size(); i ++) {
            if ((oldScrollY + screenHeight <= controllerList.get(i).getTop()
                    && controllerList.get(i).getTop() < scrollY + screenHeight)
                    || controllerList.get(i).getTop() < screenHeight) {
                controllerList.get(i).onEnterScreen();
            }
        }
    }

    public View getFooter(Activity activity) {
        return activity.findViewById(R.id.container_main_footer);
    }
}
