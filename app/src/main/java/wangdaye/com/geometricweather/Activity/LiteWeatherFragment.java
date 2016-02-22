package wangdaye.com.geometricweather.Activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;

import java.util.Calendar;
import java.util.List;

import wangdaye.com.geometricweather.Data.HefengWeather;
import wangdaye.com.geometricweather.Data.JuheResult;
import wangdaye.com.geometricweather.Data.JuheWeather;
import wangdaye.com.geometricweather.Data.Location;
import wangdaye.com.geometricweather.Data.MyDatabaseHelper;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Widget.HandlerContainer;
import wangdaye.com.geometricweather.Widget.HourlyView;
import wangdaye.com.geometricweather.Widget.RippleCardView;
import wangdaye.com.geometricweather.Widget.SafeHandler;
import wangdaye.com.geometricweather.Widget.TrendView;

/**
 * Created by WangDaYe on 2016/2/10.
 */

public class LiteWeatherFragment extends Fragment
        implements BDLocationListener, HandlerContainer {
    // widget
    private boolean iconRise;

    private ImageView[] circles;
    private ImageView[] start;
    private ImageView[] weatherIcon;

    private ImageView[] weekIcon;

    private TextView weatherTextLive;
    private TextView timeTextLive;
    private TextView locationTextLive;
    public static ImageView locationCollect;

    private TextView weekWeatherTitle;
    private TextView lifeInfoTitle;

    private TextView[] weekText;

    private TrendView weatherTrendView;
    private FrameLayout trendContainer;

    private HourlyView weatherHourlyView;
    private FrameLayout hourlyContainer;

    private TextView windKind;
    private TextView windInfo;
    private TextView pmKind;
    private TextView pmInfo;
    private TextView waterKind;
    private TextView waterInfo;
    private TextView sunKind;
    private TextView sunInfo;
    private TextView wearKind;
    private TextView wearInfo;
    private TextView illKind;
    private TextView illInfo;
    private TextView airKind;
    private TextView airInfo;
    private TextView washCarKind;
    private TextView washCarInfo;
    private TextView sportKind;
    private TextView sportInfo;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RippleCardView weatherCard;
    private RippleCardView lifeCard;

    //animator
    public AnimatorSet[] animatorSetsShow;
    public AnimatorSet[] animatorSetsIcon;

    // data
    private boolean freshData;

    private int[] maxiTemp;
    private int[] miniTemp;

    private boolean showStar;

    public Location location;

    private boolean[] showIcon;

    private final int REFRESH_JUHE_DATA_SUCCEED = 1;
    private final int REFRESH_HEFENG_DATA_SUCCEED = 2;
    private final int REFRESH_DATA_FAILED = 0;

    public static boolean isCollected;

    private MyDatabaseHelper databaseHelper;

    // baidu location
    public LocationClient mLocationClient;
    public BDLocationListener myListener;

    // handler
    private SafeHandler<LiteWeatherFragment> safeHandler;

    // TAG
//    private final String TAG = "LiteWeatherFragment";

// life cycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.lite_weather_fragment, container, false);

        this.safeHandler = new SafeHandler<>(this);
        initDatabaseHelper();

        this.freshData = false;
        this.maxiTemp = new int[7];
        this.miniTemp = new int[7];

        this.initWeatherView(mainView);
        this.setWindowTopColor();
        this.initInformationContainer(mainView);


        if (location.juheResult == null || ! location.juheResult.error_code.equals("0")) {
            this.swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            this.refreshData();
        }

        return mainView;
    }

// initialize widget

    @SuppressLint("SetTextI18n")
    private void initWeatherView(View view) {
        this.iconRise = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // show star = ! hide star
        this.showStar = ! sharedPreferences.getBoolean(getString(R.string.key_hide_star), false);

        this.circles = new ImageView[5];
        circles[0] = (ImageView) view.findViewById(R.id.circle_1_lite);
        circles[1] = (ImageView) view.findViewById(R.id.circle_2_lite);
        circles[2] = (ImageView) view.findViewById(R.id.circle_3_lite);
        circles[3] = (ImageView) view.findViewById(R.id.circle_4_lite);
        circles[4] = (ImageView) view.findViewById(R.id.circle_5_lite);

        this.start = new ImageView[2];
        start[0] = (ImageView) view.findViewById(R.id.start_1_lite);
        start[1] = (ImageView) view.findViewById(R.id.start_2_lite);

        this.weatherIcon = new ImageView[3];
        weatherIcon[0] = (ImageView) view.findViewById(R.id.weather_icon_1_lite);
        weatherIcon[1] = (ImageView) view.findViewById(R.id.weather_icon_2_lite);
        weatherIcon[2] = (ImageView) view.findViewById(R.id.weather_icon_3_lite);

        this.animatorSetsShow = new AnimatorSet[5];
        animatorSetsShow[0] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_1);
        animatorSetsShow[1] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_2);
        animatorSetsShow[2] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_3);
        animatorSetsShow[3] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_4);
        animatorSetsShow[4] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_5);
        for (int i = 0; i < 5; i ++) {
            animatorSetsShow[i].setTarget(circles[i]);
        }

        this.setModel();

        this.showCircles();

        this.weatherTextLive = (TextView) view.findViewById(R.id.weather_text_live_lite);

        RelativeLayout touchLayout = (RelativeLayout) view.findViewById(R.id.touch_layout_lite);
        touchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                touchCircles();
            }
        });
    }

    private void initInformationContainer(View view) {
        weatherCard = (RippleCardView) view.findViewById(R.id.base_info_card_lite);
        weatherCard.setVisibility(View.GONE);

        lifeCard = (RippleCardView) view.findViewById(R.id.life_info_card_lite);
        lifeCard.setVisibility(View.GONE);

        this.timeTextLive = (TextView) view.findViewById(R.id.time_text_live_lite);
        this.locationTextLive = (TextView) view.findViewById(R.id.location_text_live_lite);
        this.locationTextLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageDialog dialog = new ManageDialog();
                dialog.show(getFragmentManager(), "ManageDialog");
            }
        });

        locationCollect = (ImageView) view.findViewById(R.id.location_collect_icon_lite);
        isCollected = false;
        for (int i = 0; i < MainActivity.locationList.size(); i ++) {
            if (MainActivity.locationList.get(i).location.equals(location.location)) {
                locationCollect.setImageResource(R.drawable.ic_collect_yes);
                isCollected = true;
                break;
            }
        }
        if (! isCollected) {
            locationCollect.setImageResource(R.drawable.ic_collect_no);
        }
        locationCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCollected) {
                    for (int i = 0; i < MainActivity.locationList.size(); i ++) {
                        if (location.location.equals(MainActivity.locationList.get(i).location)) {
                            MainActivity.locationList.remove(i);
                            break;
                        }
                    }
                    deleteLocation();
                    locationCollect.setImageResource(R.drawable.ic_collect_no);
                    isCollected = false;
                    Toast.makeText(getActivity(),
                            getString(R.string.delete_succeed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    MainActivity.locationList.add(location);
                    writeLocation();
                    locationCollect.setImageResource(R.drawable.ic_collect_yes);
                    isCollected = true;
                    Toast.makeText(getActivity(),
                            getString(R.string.collect_succeed),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.weekWeatherTitle = (TextView) view.findViewById(R.id.week_weather_title_lite);
        this.lifeInfoTitle = (TextView) view.findViewById(R.id.life_info_title_lite);
        if (MainActivity.isDay) {
            weekWeatherTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
            lifeInfoTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
            locationTextLive.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
        } else {
            weekWeatherTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
            lifeInfoTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
            locationTextLive.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
        }

        this.weekText = new TextView[7];
        weekText[0] = (TextView) view.findViewById(R.id.week_text_1);
        weekText[1] = (TextView) view.findViewById(R.id.week_text_2);
        weekText[2] = (TextView) view.findViewById(R.id.week_text_3);
        weekText[3] = (TextView) view.findViewById(R.id.week_text_4);
        weekText[4] = (TextView) view.findViewById(R.id.week_text_5);
        weekText[5] = (TextView) view.findViewById(R.id.week_text_6);
        weekText[6] = (TextView) view.findViewById(R.id.week_text_7);

        this.weekIcon = new ImageView[7];
        weekIcon[0] = (ImageView) view.findViewById(R.id.icon_image_1);
        weekIcon[1] = (ImageView) view.findViewById(R.id.icon_image_2);
        weekIcon[2] = (ImageView) view.findViewById(R.id.icon_image_3);
        weekIcon[3] = (ImageView) view.findViewById(R.id.icon_image_4);
        weekIcon[4] = (ImageView) view.findViewById(R.id.icon_image_5);
        weekIcon[5] = (ImageView) view.findViewById(R.id.icon_image_6);
        weekIcon[6] = (ImageView) view.findViewById(R.id.icon_image_7);

        this.trendContainer = (FrameLayout) view.findViewById(R.id.trend_view_container);
        this.weatherTrendView = (TrendView) view.findViewById(R.id.weather_trend_view);
        this.weatherTrendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (location.hefengResult == null || ! location.hefengResult.heWeather.get(0).status.equals("ok") || freshData) {
                    getHefengWeather();
                }
                final AnimatorSet animatorSetShow = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.opaque);
                animatorSetShow.setTarget(hourlyContainer);
                final AnimatorSet animatorSetHide = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.trans);
                animatorSetHide.setTarget(trendContainer);
                animatorSetHide.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        trendContainer.setVisibility(View.GONE);
                        hourlyContainer.setVisibility(View.VISIBLE);
                        animatorSetShow.start();
                    }
                });
                animatorSetHide.start();
            }
        });

        this.hourlyContainer = (FrameLayout) view.findViewById(R.id.hourly_view_container);
        this.weatherHourlyView = (HourlyView) view.findViewById(R.id.weather_hourly_view);
        this.weatherHourlyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AnimatorSet animatorSetShow = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.opaque);
                animatorSetShow.setTarget(trendContainer);
                AnimatorSet animatorSetHide = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.trans);
                animatorSetHide.setTarget(hourlyContainer);
                animatorSetHide.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        hourlyContainer.setVisibility(View.GONE);
                        trendContainer.setVisibility(View.VISIBLE);
                        animatorSetShow.start();
                    }
                });
                animatorSetHide.start();
            }
        });
        hourlyContainer.setVisibility(View.GONE);

        windKind = (TextView) view.findViewById(R.id.detail_wind_kind);
        windInfo = (TextView) view.findViewById(R.id.detail_wind_info);
        pmKind = (TextView) view.findViewById(R.id.detail_pm_kind);
        pmInfo = (TextView) view.findViewById(R.id.detail_pm_info);
        waterKind = (TextView) view.findViewById(R.id.detail_water_kind);
        waterInfo = (TextView) view.findViewById(R.id.detail_water_info);
        sunKind = (TextView) view.findViewById(R.id.detail_sun_kind);
        sunInfo = (TextView) view.findViewById(R.id.detail_sun_info);
        wearKind = (TextView) view.findViewById(R.id.detail_wear_kind);
        wearInfo = (TextView) view.findViewById(R.id.detail_wear_info);
        illKind = (TextView) view.findViewById(R.id.detail_ill_kind);
        illInfo = (TextView) view.findViewById(R.id.detail_ill_info);
        airKind = (TextView) view.findViewById(R.id.detail_air_kind);
        airInfo = (TextView) view.findViewById(R.id.detail_air_info);
        washCarKind = (TextView) view.findViewById(R.id.detail_wash_car_kind);
        washCarInfo = (TextView) view.findViewById(R.id.detail_wash_car_info);
        sportKind = (TextView) view.findViewById(R.id.detail_sport_kind);
        sportInfo = (TextView) view.findViewById(R.id.detail_sport_info);

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_lite);
        this.swipeRefreshLayout.setColorSchemeColors(R.color.lightPrimary_3, R.color.darkPrimary_1);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        if (location.juheResult != null) {
            this.refreshUI();
        }
    }

// refresh data

    public void refreshAll() {
        this.swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        this.refreshData();
    }

    private void refreshData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.showStar = ! sharedPreferences.getBoolean(getString(R.string.key_hide_star), false);

        if (location.location.equals(getString(R.string.local))) {
            // get location
            this.initBaiduLocation();
        } else {
            this.getJuheWeather(location.location);
        }
    }

    public void setLocation(Location location) {
        this.location = location;
        MainActivity.lastLocation = location;
    }

    private void setModel() {
        // set model of this vies: day or night.
        if (MainActivity.isDay) {
            // day
            circles[0].setImageResource(R.drawable.circle_day_1);
            circles[1].setImageResource(R.drawable.circle_day_2);
            circles[2].setImageResource(R.drawable.circle_day_3);
            circles[3].setImageResource(R.drawable.circle_day_4);
            circles[4].setImageResource(R.drawable.rec_day);

            for (int i = 0; i < 2; i ++) {
                start[i].setVisibility(View.GONE);
            }
        } else {
            // night
            circles[0].setImageResource(R.drawable.circle_night_1);
            circles[1].setImageResource(R.drawable.circle_night_2);
            circles[2].setImageResource(R.drawable.circle_night_3);
            circles[3].setImageResource(R.drawable.circle_night_4);
            circles[4].setImageResource(R.drawable.rec_night);

            if (showStar) {
                for (int i = 0; i < 2; i ++) {
                    start[i].setVisibility(View.VISIBLE);
                }
            } else {
                for (int i = 0; i < 2; i ++) {
                    start[i].setVisibility(View.GONE);
                }
            }
        }
    }

    private void getJuheWeather(final String searchLocation) {
        Thread thread=new Thread(new Runnable()
        {
            @Override
            public void run()
            { // TODO Auto-generated method stub
                location.juheResult = JuheWeather.getRequest(searchLocation);
                Message message = new Message();
                if (location.juheResult == null || ! location.juheResult.error_code.equals("0")) {
                    message.what = REFRESH_DATA_FAILED;
                } else {
                    message.what = REFRESH_JUHE_DATA_SUCCEED;
                }
                safeHandler.sendMessage(message);
            }
        });
        thread.start();
    }

    private void getHefengWeather() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                if (location.juheResult == null || ! location.juheResult.error_code.equals("0")) {
                    message.what = REFRESH_DATA_FAILED;
                } else {
                    location.hefengResult = HefengWeather.request(location.juheResult.result.data.realtime.city_name);
                    if (location.hefengResult == null || ! location.hefengResult.heWeather.get(0).status.equals("ok")) {
                        message.what = REFRESH_DATA_FAILED;
                    } else {
                        message.what = REFRESH_HEFENG_DATA_SUCCEED;
                    }
                }
                safeHandler.sendMessage(message);
            }
        });
        thread.start();
    }

// refresh UI

    @SuppressLint("SetTextI18n")
    public void refreshUI() {
        if (location.juheResult == null || ! location.juheResult.error_code.equals("0")) {
            return;
        }

        if (MainActivity.needChangeTime()) {
            MainActivity.isDay = ! MainActivity.isDay;
            this.changeTime();
            MainActivity.setNavHead();
            MainActivity.initNavigationBar(getActivity(), getActivity().getWindow());
            this.setWindowTopColor();
        }

        this.weatherTextLive.setText(location.juheResult.result.data.realtime.weatherNow.weatherInfo
                + " "
                + location.juheResult.result.data.realtime.weatherNow.temperature
                + "℃");
        String[] time = location.juheResult.result.data.realtime.time.split(":");
        this.timeTextLive.setText(time[0] + ":" + time[1]);
        this.locationTextLive.setText(location.juheResult.result.data.realtime.city_name);

        if (MainActivity.isDay) {
            this.weekWeatherTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
            this.lifeInfoTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
            locationTextLive.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
        } else {
            this.weekWeatherTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
            this.lifeInfoTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
            locationTextLive.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
        }

        isCollected = false;
        for (int i = 0; i < MainActivity.locationList.size(); i ++) {
            if (MainActivity.locationList.get(i).location.equals(location.location)) {
                locationCollect.setImageResource(R.drawable.ic_collect_yes);
                isCollected = true;
                break;
            }
        }
        if (! isCollected) {
            locationCollect.setImageResource(R.drawable.ic_collect_no);
        }

        for (int i = 0; i < 7; i ++) {
            if (i == 0) {
                this.weekText[i].setText(getString(R.string.today));
            } else {
                this.weekText[i].setText(getString(R.string.week) + location.juheResult.result.data.weather.get(i).week);
            }

            String weatherKind;
            if (MainActivity.isDay) {
                weatherKind = JuheWeather.getWeatherKind(location.juheResult.result.data.weather.get(i).info.day.get(1));
            } else {
                weatherKind = JuheWeather.getWeatherKind(location.juheResult.result.data.weather.get(i).info.night.get(1));
            }
            int[] imageId= JuheWeather.getWeatherIcon(weatherKind, MainActivity.isDay);

            weekIcon[i].setImageResource(imageId[3]);

            final int position = i;
            weekIcon[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (MainActivity.isDay) {
                        Toast.makeText(getActivity(),
                                location.juheResult.result.data.weather.get(position).info.day.get(1),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(),
                                location.juheResult.result.data.weather.get(position).info.night.get(1),
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }

        for (int i = 0; i < 7; i ++) {
            maxiTemp[i] = Integer.parseInt(location.juheResult.result.data.weather.get(i).info.day.get(2));
            miniTemp[i] = Integer.parseInt(location.juheResult.result.data.weather.get(i).info.night.get(2));
        }
        this.trendContainer.setAlpha(1);
        this.trendContainer.setVisibility(View.VISIBLE);
        this.hourlyContainer.setVisibility(View.GONE);
        this.weatherTrendView.setData(maxiTemp, miniTemp);
        this.weatherTrendView.invalidate();

        this.initWindPower(location.juheResult.result.data.realtime.wind, location.juheResult.result.data.weather.get(0));
        this.initPm(location.juheResult.result.data.air.pm25);
        this.initWater(location.juheResult.result.data.realtime.weatherNow);
        JuheResult.LifeInfo lifeInfo = location.juheResult.result.data.life.lifeInfo;
        this.initSun(lifeInfo);
        this.initWear(lifeInfo);
        this.initIll(lifeInfo);
        this.initAir(lifeInfo);
        this.initWashCar(lifeInfo);
        this.initSport(lifeInfo);

        if (MainActivity.isDay) {
            this.windKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_5));
            this.pmKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_5));
            this.waterKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_5));
            this.sunKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_5));
            this.wearKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_5));
            this.illKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_5));
            this.airKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_5));
            this.washCarKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_5));
            this.sportKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_5));
        } else {
            this.windKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_5));
            this.pmKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_5));
            this.waterKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_5));
            this.sunKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_5));
            this.wearKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_5));
            this.illKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_5));
            this.airKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_5));
            this.washCarKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_5));
            this.sportKind.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_5));
        }

        final AnimatorSet[] animatorSetShowView = new AnimatorSet[2];
        animatorSetShowView[0] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.show_view);
        animatorSetShowView[1] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.show_view);
        animatorSetShowView[0].setTarget(this.weatherCard);
        animatorSetShowView[1].setTarget(this.lifeCard);

        animatorSetShowView[0].addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                lifeCard.setVisibility(View.VISIBLE);
                animatorSetShowView[1].start();
            }
        });
        animatorSetShowView[1].addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                showWeatherIcon();
            }
        });

        weatherCard.setVisibility(View.VISIBLE);
        animatorSetShowView[0].start();
    }

    public void showCirclesView() {
        this.showCircles();
        this.showWeatherIcon();
    }

    private void setWeatherIcon(JuheResult juheResult) {
        String weatherKind = JuheWeather.getWeatherKind(juheResult.result.data.realtime.weatherNow.weatherInfo);
        int[] imageId = JuheWeather.getWeatherIcon(weatherKind, MainActivity.isDay);
        int[] animatorId = JuheWeather.getAnimatorId(weatherKind, MainActivity.isDay);
        this.animatorSetsIcon = new AnimatorSet[3];

        this.showIcon = new boolean[3];

        for (int i = 0; i < 3; i ++) {
            if (imageId[i] != 0) {
                showIcon[i] = true;
                weatherIcon[i].setImageResource(imageId[i]);
                weatherIcon[i].setVisibility(View.VISIBLE);
                this.animatorSetsIcon[i] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), animatorId[i]);
                animatorSetsIcon[i].setTarget(weatherIcon[i]);
                animatorSetsIcon[i].start();
            } else {
                showIcon[i] = false;
                weatherIcon[i].setVisibility(View.GONE);
            }
        }
    }

    private void changeTime() {
        AnimatorSet[] animatorSetsGone = new AnimatorSet[5];
        animatorSetsGone[0] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_gone_1);
        animatorSetsGone[1] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_gone_2);
        animatorSetsGone[2] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_gone_3);
        animatorSetsGone[3] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_gone_4);
        animatorSetsGone[4] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_gone_5);

        if (MainActivity.isDay) {
            for (int i = 0; i < 2 ; i ++) {
                start[i].setVisibility(View.GONE);
            }
        } else {
            if (showStar) {
                for (int i = 0; i < 2 ; i ++) {
                    start[i].setVisibility(View.VISIBLE);
                }
            } else {
                for (int i = 0; i < 2 ; i ++) {
                    start[i].setVisibility(View.GONE);
                }
            }
        }

        for (int i = 0; i < 5; i ++) {
            final int position = i;
            animatorSetsGone[i].setTarget(circles[i]);
            animatorSetsGone[i].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (MainActivity.isDay) {
                        switch (position) {
                            case 0:
                                circles[position].setImageResource(R.drawable.circle_day_1);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_1);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;

                            case 1:
                                circles[position].setImageResource(R.drawable.circle_day_2);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_2);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;

                            case 2:
                                circles[position].setImageResource(R.drawable.circle_day_3);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_3);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;

                            case 3:
                                circles[position].setImageResource(R.drawable.circle_day_4);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_4);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;
                            case 4:
                                circles[position].setImageResource(R.drawable.rec_day);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_5);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;
                        }
                    } else {
                        switch (position) {
                            case 0:
                                circles[position].setImageResource(R.drawable.circle_night_1);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_1);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;

                            case 1:
                                circles[position].setImageResource(R.drawable.circle_night_2);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_2);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;
                            case 2:
                                circles[position].setImageResource(R.drawable.circle_night_3);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_3);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;
                            case 3:
                                circles[position].setImageResource(R.drawable.circle_night_4);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_4);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;
                            case 4:
                                circles[position].setImageResource(R.drawable.rec_night);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_5);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;
                        }
                    }
                }
            });
        } for (int i = 0; i < 5; i ++) {
            animatorSetsGone[i].start();
        }
    }

    private void setWindowTopColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription taskDescription;
            Bitmap topIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            if (MainActivity.isDay) {
                taskDescription = new ActivityManager.TaskDescription(
                        getString(R.string.app_name),
                        topIcon,
                        ContextCompat.getColor(getActivity(), R.color.lightPrimary_5));
            } else {
                taskDescription = new ActivityManager.TaskDescription(
                        getString(R.string.app_name),
                        topIcon,
                        ContextCompat.getColor(getActivity(), R.color.darkPrimary_5));
            }
            getActivity().setTaskDescription(taskDescription);
            topIcon.recycle();
        }
    }

    public void showCircles() {
        // show circles
        for (int i = 0; i < 5; i ++) {
            this.animatorSetsShow[i].start();
        }
    }

    public void touchCircles() {
        // feedback user's touch
        if (location.juheResult != null) {
            for (int i = 0; i < 3; i ++) {
                if (showIcon[i]) {
                    animatorSetsIcon[i].start();
                }
            }
        }
    }

    public void showWeatherIcon() {
        if (location.juheResult == null || ! location.juheResult.error_code.equals("0")) {
            return;
        }

        final AnimatorSet[] animatorSetsRise = new AnimatorSet[3];
        animatorSetsRise[0] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.weather_icon_rise);
        animatorSetsRise[1] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.weather_icon_rise);
        animatorSetsRise[2] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.weather_icon_rise);

        if (this.iconRise) {
            // hide weather icon at first.
            AnimatorSet[] animatorSetsFall = new AnimatorSet[3];
            animatorSetsFall[0] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.weather_icon_fall);
            animatorSetsFall[1] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.weather_icon_fall);
            animatorSetsFall[2] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.weather_icon_fall);

            animatorSetsFall[2].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    setWeatherIcon(location.juheResult);

                    for (int i = 0; i < 3; i ++) {
                        if (showIcon[i]) {
                            animatorSetsRise[i].setTarget(weatherIcon[i]);
                            animatorSetsRise[i].start();
                        }
                    }
                }
            });

            for (int i = 0; i < 3; i ++) {
                animatorSetsFall[i].setTarget(weatherIcon[i]);
                animatorSetsFall[i].start();
            }
        } else {
            // just show weather.
            this.iconRise = true;
            this.setWeatherIcon(location.juheResult);

            for (int i = 0; i < 3; i ++) {
                if (showIcon[i]) {
                    animatorSetsRise[i].setTarget(weatherIcon[i]);
                    animatorSetsRise[i].start();
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void initWindPower(JuheResult.Wind windInfo, JuheResult.Weather weatherInfo) {
        // wind power
        windKind.setText(weatherInfo.info.day.get(3) + "（" + getString(R.string.live) + windInfo.direct + "）");
        this.windInfo.setText(weatherInfo.info.day.get(4) + "（" + getString(R.string.live) + windInfo.power + "）");
    }

    @SuppressLint("SetTextI18n")
    private void initPm(JuheResult.Pm25 pmInfo) {
        // pm2.5 & pm10
        pmKind.setText(getString(R.string.pm_25) + " : " + pmInfo.pm25 + " , " + getString(R.string.pm_10) + " : " + pmInfo.pm10);
        this.pmInfo.setText(getString(R.string.pm_level) + ":" +  pmInfo.quality);
    }

    @SuppressLint("SetTextI18n")
    private void initWater(JuheResult.WeatherNow weatherNowInfo) {
        // humidity
        waterKind.setText(getString(R.string.humidity));
        waterInfo.setText(weatherNowInfo.humidity);
    }

    @SuppressLint("SetTextI18n")
    private void initSun(JuheResult.LifeInfo lifeInfo) {
        // uv
        sunKind.setText(getString(R.string.uv) + "-" + lifeInfo.ziwaixian.get(0));
        sunInfo.setText(lifeInfo.ziwaixian.get(1));
    }

    @SuppressLint("SetTextI18n")
    private void initWear(JuheResult.LifeInfo lifeInfo) {
        // dressing index
        wearKind.setText(getString(R.string.dressing_index) + "-" + lifeInfo.chuanyi.get(0));
        wearInfo.setText(lifeInfo.chuanyi.get(1));
    }

    @SuppressLint("SetTextI18n")
    private void initIll(JuheResult.LifeInfo lifeInfo) {
        // cold index
        illKind.setText(getString(R.string.cold_index) + "-" + lifeInfo.chuanyi.get(0));
        illInfo.setText(lifeInfo.ganmao.get(1));
    }

    @SuppressLint("SetTextI18n")
    private void initAir(JuheResult.LifeInfo lifeInfo) {
        // air index
        airKind.setText(getString(R.string.air_index) + "-" + lifeInfo.wuran.get(0));
        airInfo.setText(lifeInfo.wuran.get(1));
    }

    @SuppressLint("SetTextI18n")
    private void initWashCar(JuheResult.LifeInfo lifeInfo) {
        // wash car index
        washCarKind.setText(getString(R.string.wash_car_index) + "-" + lifeInfo.xiche.get(0));
        washCarInfo.setText(lifeInfo.xiche.get(1));
    }

    @SuppressLint("SetTextI18n")
    private void initSport(JuheResult.LifeInfo lifeInfo) {
        // exercise index
        sportKind.setText(getString(R.string.exercise_index) + "-" + lifeInfo.yundong.get(0));
        sportInfo.setText(lifeInfo.yundong.get(1));
    }

// database

    private void initDatabaseHelper() {
        this.databaseHelper = new MyDatabaseHelper(getActivity(),
                MyDatabaseHelper.DATABASE_NAME,
                null,
                1);
    }

    private void writeLocation() {
        SQLiteDatabase database = this.databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_LOCATION, location.location);
        database.insert(MyDatabaseHelper.TABLE_LOCATION, null, values);
        values.clear();
        database.close();
    }

    private void deleteLocation() {
        SQLiteDatabase database = this.databaseHelper.getWritableDatabase();
        String[] location = new String[] {this.location.location};
        database.delete(
                MyDatabaseHelper.TABLE_LOCATION,
                MyDatabaseHelper.COLUMN_LOCATION + " = ?",
                location);
        database.close();
    }

// baidu location

    private void initBaiduLocation(){
        myListener = this;
        mLocationClient = new LocationClient(getActivity());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=0;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(false);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        //Receive Location
        String location = null;

        StringBuffer sb = new StringBuffer(256);
        sb.append("time : ");
        sb.append(bdLocation.getTime());
        sb.append("\nerror code : ");
        sb.append(bdLocation.getLocType());
        sb.append("\nlatitude : ");
        sb.append(bdLocation.getLatitude());
        sb.append("\nlontitude : ");
        sb.append(bdLocation.getLongitude());
        sb.append("\nradius : ");
        sb.append(bdLocation.getRadius());
        if (bdLocation.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
            location = bdLocation.getCity();
        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
            location = bdLocation.getCity();
        } else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
            sb.append("\ndescribe : ");
            sb.append("离线定位成功，离线定位结果也是有效的");
            location = bdLocation.getCity();
        } else if (bdLocation.getLocType() == BDLocation.TypeServerError) {
            sb.append("\ndescribe : ");
            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
            sb.append("\ndescribe : ");
            sb.append("网络不同导致定位失败，请检查网络是否通畅");
        } else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {
            sb.append("\ndescribe : ");
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }

        if(location == null) {
            Toast.makeText(getActivity(),
                    getString(R.string.get_location_failed),
                    Toast.LENGTH_SHORT).show();
        }
        this.getJuheWeather(location);

        sb.append("\nlocationdescribe : ");
        sb.append(bdLocation.getLocationDescribe());// 位置语义化信息
        List<Poi> list = bdLocation.getPoiList();// POI数据
        if (list != null) {
            sb.append("\npoilist size = : ");
            sb.append(list.size());
            for (Poi p : list) {
                sb.append("\npoi= : ");
                sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
            }
        }
        Log.i("BaiduLocationApiDem", sb.toString());
        mLocationClient.stop();
    }

// handler

    @Override
    public void handleMessage(Message message) {
        switch(message.what)
        {
            case REFRESH_JUHE_DATA_SUCCEED:
                freshData = true;
                swipeRefreshLayout.setRefreshing(false);
                refreshUI();
                MainActivity.sendNotification(location, getActivity());
                MainActivity.refreshWidgetDay(location, getActivity());
                MainActivity.refreshWidgetWeek(location, getActivity());
                MainActivity.refreshWidgetDayWeek(location, getActivity());
                MainActivity.refreshWidgetClockDay(location, getActivity());
                MainActivity.refreshWidgetClockDayCenter(location, getActivity());
                MainActivity.refreshWidgetClockDayWeek(location, getActivity());

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if (5 < hour && hour < 19) {
                    editor.putBoolean(getString(R.string.key_isDay), true);
                } else {
                    editor.putBoolean(getString(R.string.key_isDay), false);
                } editor.apply();

                for (int i = 0; i < MainActivity.locationList.size(); i ++) {
                    if (MainActivity.locationList.get(i).location.equals(location.location)) {
                        MainActivity.locationList.remove(i);
                        MainActivity.locationList.add(i, location);
                        MainActivity.lastLocation = location;
                        break;
                    }
                }
                break;
            case REFRESH_HEFENG_DATA_SUCCEED:
                freshData = false;
                int[] temp = new int[8];
                float[] pop = new float[8];
                int j = location.hefengResult.heWeather.get(0).hourly_forecast.size() - 1;
                for (int i = temp.length - 1; i > -1; i --) {
                    if (j > -1) {
                        temp[i] = Integer.parseInt(location.hefengResult.heWeather.get(0).hourly_forecast.get(j).tmp);
                        pop[i] = Float.parseFloat(location.hefengResult.heWeather.get(0).hourly_forecast.get(j).pop);
                        if (temp[i] > maxiTemp[0]) {
                            temp[i] = maxiTemp[0];
                        } else if (temp[i] < miniTemp[0]) {
                            temp[i] = miniTemp[0];
                        }
                    } else {
                        temp[i] = 0;
                        pop[i] = 0;
                    }
                    j --;
                }
                weatherHourlyView.setData(temp, pop);
                weatherHourlyView.invalidate();
                break;
            default:
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(
                        getActivity(),
                        getString(R.string.refresh_data_failed),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }
}