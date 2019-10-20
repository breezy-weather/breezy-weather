package wangdaye.com.geometricweather.main.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.ui.controller.AbstractMainItemController;
import wangdaye.com.geometricweather.main.ui.controller.AqiController;
import wangdaye.com.geometricweather.main.ui.controller.DetailsController;
import wangdaye.com.geometricweather.main.ui.controller.DailyTrendCardController;
import wangdaye.com.geometricweather.main.ui.controller.FirstCardHeaderController;
import wangdaye.com.geometricweather.main.ui.controller.FooterController;
import wangdaye.com.geometricweather.main.ui.controller.HeaderController;
import wangdaye.com.geometricweather.main.ui.controller.HourlyTrendCardController;
import wangdaye.com.geometricweather.main.ui.controller.SunMoonController;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;

public class MainControllerAdapter {

    private List<AbstractMainItemController> controllerList;
    private @Nullable FirstCardHeaderController firstCardHeaderController;
    private int headerCurrentTemperatureTextHeight;
    private int screenHeight;

    public MainControllerAdapter(@NonNull Context context) {
        this.controllerList = new ArrayList<>();
        this.firstCardHeaderController = null;
        this.headerCurrentTemperatureTextHeight = -1;
        this.screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    public void bind(@NonNull Activity activity,
                     @NonNull ViewGroup scrollContainer, @NonNull WeatherView weatherView,
                     @NonNull Location location,
                     @NonNull ResourceProvider provider, @NonNull MainColorPicker picker) {
        float cardMarginsVertical = weatherView.getCardMarginsVertical(activity);
        float cardMarginsHorizontal = weatherView.getCardMarginsHorizontal(activity);
        float cardRadius = weatherView.getCardRadius(activity);

        if (scrollContainer.getChildCount() != 0) {
            scrollContainer.removeAllViews();
        }
        if (location.getWeather() != null) {
            LayoutInflater inflater = LayoutInflater.from(scrollContainer.getContext());
            String[] cardDisplayValues = SettingsOptionManager.getInstance(activity).getCardDisplayValues();

            inflater.inflate(R.layout.container_main_header, scrollContainer, true);
            controllerList.add(new HeaderController(
                    activity, weatherView, provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius
            ));

            if (isDisplay(SettingsOptionManager.CARD_DAILY_OVERVIEW, cardDisplayValues)) {
                inflater.inflate(R.layout.container_main_daily_trend_card, scrollContainer, true);
                controllerList.add(new DailyTrendCardController(
                        activity, weatherView, provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius
                ));
            }

            if (isDisplay(SettingsOptionManager.CARD_HOURLY_OVERVIEW, cardDisplayValues)) {
                inflater.inflate(R.layout.container_main_hourly_trend_card, scrollContainer, true);
                controllerList.add(new HourlyTrendCardController(
                        activity, weatherView, provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius
                ));
            }

            if (isDisplay(SettingsOptionManager.CARD_AIR_QUALITY, cardDisplayValues)) {
                inflater.inflate(R.layout.container_main_aqi, scrollContainer, true);
                controllerList.add(new AqiController(
                        activity, weatherView, provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius
                ));
            }

            if (isDisplay(SettingsOptionManager.CARD_SUNRISE_SUNSET, cardDisplayValues)) {
                inflater.inflate(R.layout.container_main_sun_moon, scrollContainer, true);
                controllerList.add(new SunMoonController(
                        activity, weatherView, provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius
                ));
            }

            if (isDisplay(SettingsOptionManager.CARD_LIFE_DETAILS, cardDisplayValues)) {
                inflater.inflate(R.layout.container_main_details, scrollContainer, true);
                controllerList.add(new DetailsController(
                        activity, weatherView, provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius
                ));
            }

            inflater.inflate(R.layout.container_main_footer, scrollContainer, true);
            controllerList.add(new FooterController(
                    activity, provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius
            ));
        }
        if (controllerList.size() > 1) {
            LinearLayout viewGroup = controllerList.get(1).getContainer();
            if (viewGroup != null) {
                firstCardHeaderController = new FirstCardHeaderController(
                        activity, viewGroup, provider, picker, cardMarginsVertical, cardMarginsHorizontal, cardRadius
                );
                firstCardHeaderController.onBindView(location);
            }
        }
        for (int i = 0; i < controllerList.size(); i ++) {
            controllerList.get(i).onBindView(location);
        }
        scrollContainer.post(() -> onScroll(0));
    }

    public void unbind(@NonNull ViewGroup scrollContainer) {
        for (int i = 0; i < controllerList.size(); i ++) {
            controllerList.get(i).onDestroy();
        }
        controllerList.clear();
        if (firstCardHeaderController != null) {
            firstCardHeaderController.onDestroy();
        }
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

    public void onScroll(int scrollY) {
        for (int i = 0; i < controllerList.size(); i ++) {
            if (controllerList.get(i).getTop() < scrollY + screenHeight
                    || controllerList.get(i).getTop() < screenHeight) {
                controllerList.get(i).enterScreen();
            }
        }
    }

    public View getFooter(Activity activity) {
        return activity.findViewById(R.id.container_main_footer);
    }

    private static boolean isDisplay(String targetValue, String[] displayValues) {
        for(String s : displayValues){
            if(s.equals(targetValue))
                return true;
        }
        return false;
    }
}
