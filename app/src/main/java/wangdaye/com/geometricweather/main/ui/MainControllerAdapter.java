package wangdaye.com.geometricweather.main.ui;

import android.app.Activity;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

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

    @NonNull private Location location;
    private List<AbstractMainItemController> controllerList;

    private int headerCurrentTemperatureTextHeight;
    private int screenHeight;

    public MainControllerAdapter(@NonNull Activity activity,
                                 @NonNull WeatherView weatherView, @NonNull Location location,
                                 @NonNull ResourceProvider provider, @NonNull MainColorPicker picker) {
        this.location = location;

        this.controllerList = new ArrayList<>();
        if (location.getWeather() != null) {
            controllerList.add(new HeaderController(activity, weatherView, provider, picker));
            controllerList.add(new FirstTrendCardController(activity, weatherView, provider, picker));
            controllerList.add(new SecondTrendCardController(activity, weatherView, provider, picker));
            controllerList.add(new AqiController(activity, weatherView, provider, picker));
            controllerList.add(new DetailsController(activity, weatherView, provider, picker));
            controllerList.add(new SunMoonController(activity, weatherView, provider, picker));
            controllerList.add(new FooterController(activity, provider, picker));
        }

        this.headerCurrentTemperatureTextHeight = -1;
        this.screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
    }

    public void bindView() {
        for (int i = 0; i < controllerList.size(); i ++) {
            controllerList.get(i).onBindView(location);
        }
    }

    public void destroy() {
        for (int i = 0; i < controllerList.size(); i ++) {
            controllerList.get(i).onDestroy();
        }
        controllerList.clear();
    }

    public int getCurrentTemperatureTextHeight() {
        if (headerCurrentTemperatureTextHeight < 0) {
            if (controllerList.size() > 0) {
                AbstractMainItemController controller = controllerList.get(0);
                if (controller instanceof HeaderController) {
                    headerCurrentTemperatureTextHeight = ((HeaderController) controller)
                            .getCurrentTemperatureHeight();
                }
            }
        }
        return headerCurrentTemperatureTextHeight;
    }

    public void onScroll(int oldScrollY, int scrollY) {
        if (oldScrollY >= scrollY) {
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
}
