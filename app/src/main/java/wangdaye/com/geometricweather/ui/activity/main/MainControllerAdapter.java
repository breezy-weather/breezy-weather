package wangdaye.com.geometricweather.ui.activity.main;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.ui.activity.main.controller.AbstractMainItemController;
import wangdaye.com.geometricweather.ui.activity.main.controller.AqiController;
import wangdaye.com.geometricweather.ui.activity.main.controller.DetailsController;
import wangdaye.com.geometricweather.ui.activity.main.controller.FirstTrendCardController;
import wangdaye.com.geometricweather.ui.activity.main.controller.FooterController;
import wangdaye.com.geometricweather.ui.activity.main.controller.HeaderController;
import wangdaye.com.geometricweather.ui.activity.main.controller.SecondTrendCardController;
import wangdaye.com.geometricweather.ui.activity.main.controller.SunMoonController;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

class MainControllerAdapter {

    @NonNull private Location location;
    private List<AbstractMainItemController> controllerList;

    private int headerCurrentTemperatureTextHeight;
    private int screenHeight;

    MainControllerAdapter(@NonNull Activity activity,
                          @NonNull WeatherView weatherView, @NonNull Location location) {
        this.location = location;

        this.controllerList = new ArrayList<>();
        if (location.weather != null) {
            controllerList.add(new HeaderController(activity, weatherView));
            controllerList.add(new FirstTrendCardController(activity, weatherView));
            controllerList.add(new SecondTrendCardController(activity, weatherView));
            controllerList.add(new AqiController(activity, weatherView));
            controllerList.add(new DetailsController(activity, weatherView));
            controllerList.add(new SunMoonController(activity, weatherView));
            controllerList.add(new FooterController(activity));
        }

        this.headerCurrentTemperatureTextHeight = -1;
        this.screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
    }

    void bindView() {
        for (int i = 0; i < controllerList.size(); i ++) {
            controllerList.get(i).onBindView(location);
        }
    }

    void destroy() {
        for (int i = 0; i < controllerList.size(); i ++) {
            controllerList.get(i).onDestroy();
        }
        controllerList.clear();
    }

    int getCurrentTemperatureTextHeight() {
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

    void onScroll(int oldScrollY, int scrollY) {
        if (oldScrollY >= scrollY) {
            return;
        }
        for (int i = 0; i < controllerList.size(); i ++) {
            if (oldScrollY + screenHeight <= controllerList.get(i).getTop()
                    && controllerList.get(i).getTop() < scrollY + screenHeight) {
                controllerList.get(i).onEnterScreen();
                break;
            }
        }
    }
}
