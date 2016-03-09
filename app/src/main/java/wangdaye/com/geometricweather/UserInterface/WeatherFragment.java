package wangdaye.com.geometricweather.UserInterface;

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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import wangdaye.com.geometricweather.Data.JuheWeather;
import wangdaye.com.geometricweather.Data.Location;
import wangdaye.com.geometricweather.Data.MyDatabaseHelper;
import wangdaye.com.geometricweather.Data.WeatherInfoToShow;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Widget.HandlerContainer;
import wangdaye.com.geometricweather.Widget.HourlyView;
import wangdaye.com.geometricweather.Widget.MySwipeRefreshLayout;
import wangdaye.com.geometricweather.Widget.RippleCardView;
import wangdaye.com.geometricweather.Widget.SafeHandler;
import wangdaye.com.geometricweather.Widget.TrendView;

/**
 * A fragment to show weather information.
 * */

public class WeatherFragment extends Fragment
        implements BDLocationListener, HandlerContainer {
    // widget
    private ImageView[] circles;
    private ImageView[] start;
    private ImageView[] weatherIcon;

    private ImageView[][] weekIcon;

    private TextView weatherTextLive;
    private TextView timeTextLive;
    private TextView locationTextLive;
    public static ImageView locationCollect;

    private TextView weekWeatherTitle;
    private TextView lifeInfoTitle;

    private TextView[] weekText;

    private TrendView weatherTrendView;
    private TextView poweredText;
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

    private MySwipeRefreshLayout swipeRefreshLayout;
    private RippleCardView weatherCard;
    private RippleCardView lifeCard;

    private GestureDetector gestureDetectorPage;
    private GestureDetector gestureDetectorTrendView;
    private GestureDetector gestureDetectorHourlyView;

    //animator
    public AnimatorSet[] animatorSetsShow;
    public AnimatorSet[] animatorSetsTouch;
    public AnimatorSet[] animatorSetsAnimal;
    public AnimatorSet[] animatorSetsIcon;

    // data
    private boolean refreshSucceed;
    private boolean freshData;
    private boolean iconRise;

    private WeatherInfoToShow info;

    private int[] maxiTemp;
    private int[] miniTemp;

    private boolean[] showIcon;
    private boolean showStar;

    public Location location;

    private final int REFRESH_TOTAL_DATA_SUCCEED = 1;
    private final int REFRESH_HOURLY_DATA_SUCCEED = 2;
    private final int REFRESH_TOTAL_DATA_FAILED = -1;
    private final int REFRESH_HOURLY_DATA_FAILED = -2;

    public static boolean isCollected;

    private MyDatabaseHelper databaseHelper;

    // baidu location
    public LocationClient mLocationClient;
    public BDLocationListener myListener;

    // handler
    private SafeHandler<WeatherFragment> safeHandler;

    // TAG
//    private final String TAG = "WeatherFragment";

// life cycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.weather_fragment, container, false);

        this.safeHandler = new SafeHandler<>(this);
        this.gestureDetectorPage = new GestureDetector(getActivity(), new PageOnGestureListener());
        this.gestureDetectorTrendView = new GestureDetector(getActivity(), new TrendViewOnGestureListener());
        this.gestureDetectorHourlyView = new GestureDetector(getActivity(), new HourlyViewOnGestureListener());
        this.initDatabaseHelper();
        this.setLocation();
        this.refreshSucceed = false;
        this.freshData = false;
        this.maxiTemp = new int[7];
        this.miniTemp = new int[7];

        this.setWindowTopColor();
        this.initWeatherView(mainView);
        this.initInformationContainer(mainView);

        if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+")
                && location.hefengResult != null
                && location.hefengResult.heWeather.get(0).status.equals("ok")) {
            return mainView;
        } else if (location.juheResult != null && location.juheResult.error_code.equals("0")) {
            return mainView;
        }

        this.refreshAll();
        return mainView;
    }

// initialize widget

    private void initWeatherView(View view) {
        this.iconRise = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // show star = ! hide star
        this.showStar = ! sharedPreferences.getBoolean(getString(R.string.key_hide_star), false);

        this.circles = new ImageView[5];
        circles[0] = (ImageView) view.findViewById(R.id.circle_1);
        circles[1] = (ImageView) view.findViewById(R.id.circle_2);
        circles[2] = (ImageView) view.findViewById(R.id.circle_3);
        circles[3] = (ImageView) view.findViewById(R.id.circle_4);
        circles[4] = (ImageView) view.findViewById(R.id.circle_5);

        this.start = new ImageView[2];
        start[0] = (ImageView) view.findViewById(R.id.start_1);
        start[1] = (ImageView) view.findViewById(R.id.start_2);

        this.weatherIcon = new ImageView[3];
        weatherIcon[0] = (ImageView) view.findViewById(R.id.weather_icon_1);
        weatherIcon[1] = (ImageView) view.findViewById(R.id.weather_icon_2);
        weatherIcon[2] = (ImageView) view.findViewById(R.id.weather_icon_3);

        this.animatorSetsShow = new AnimatorSet[5];
        animatorSetsShow[0] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_1);
        animatorSetsShow[1] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_2);
        animatorSetsShow[2] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_3);
        animatorSetsShow[3] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_4);
        animatorSetsShow[4] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_5);
        for (int i = 0; i < 5; i ++) {
            animatorSetsShow[i].setTarget(circles[i]);
        }

        if (MainActivity.isDay) {
            this.animatorSetsTouch = new AnimatorSet[4];
            animatorSetsTouch[0] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_day_1);
            animatorSetsTouch[1] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_day_2);
            animatorSetsTouch[2] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_day_3);
            animatorSetsTouch[3] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_day_4);
        } else {
            this.animatorSetsTouch = new AnimatorSet[4];
            animatorSetsTouch[0] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_night_1);
            animatorSetsTouch[1] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_night_2);
            animatorSetsTouch[2] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_night_3);
            animatorSetsTouch[3] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_night_4);
        }

        this.animatorSetsAnimal = new AnimatorSet[2];
        animatorSetsAnimal[0] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.start_shine_1);
        animatorSetsAnimal[1] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.start_shine_2);

        this.setModel();

        this.showCircles();
        this.showAnimals();

        this.weatherTextLive = (TextView) view.findViewById(R.id.weather_text_live);

        RelativeLayout touchLayout = (RelativeLayout) view.findViewById(R.id.touch_layout);
        touchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                touchCircles();
            }
        });
    }

    private void initInformationContainer(View view) {
        weatherCard = (RippleCardView) view.findViewById(R.id.base_info_card);
        weatherCard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetectorPage.onTouchEvent(event);
            }
        });
        LinearLayout.LayoutParams weatherCardLayoutParams = (LinearLayout.LayoutParams) weatherCard.getLayoutParams();
        weatherCard.viewStartX = weatherCard.getX() + weatherCardLayoutParams.leftMargin;
        weatherCard.viewStartY = weatherCard.getY();
        weatherCard.setVisibility(View.GONE);

        lifeCard = (RippleCardView) view.findViewById(R.id.life_info_card);
        lifeCard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetectorPage.onTouchEvent(event);
            }
        });
        LinearLayout.LayoutParams liferCardLayoutParams = (LinearLayout.LayoutParams) lifeCard.getLayoutParams();
        lifeCard.viewStartX = lifeCard.getX() + liferCardLayoutParams.leftMargin;
        lifeCard.viewStartY = lifeCard.getY();
        lifeCard.setVisibility(View.GONE);

        this.timeTextLive = (TextView) view.findViewById(R.id.time_text_live);
        this.locationTextLive = (TextView) view.findViewById(R.id.location_text_live);
        this.locationTextLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageDialog dialog = new ManageDialog();
                dialog.show(getFragmentManager(), "ManageDialog");
            }
        });

        locationCollect = (ImageView) view.findViewById(R.id.location_collect_icon);
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
                if (isCollected && MainActivity.locationList.size() > 1) {
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
                } else if (isCollected) {
                    Toast.makeText(getActivity(),
                            getString(R.string.location_list_cannot_be_null),
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

        this.weekWeatherTitle = (TextView) view.findViewById(R.id.week_weather_title);
        this.lifeInfoTitle = (TextView) view.findViewById(R.id.life_info_title);
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

        this.weekIcon = new ImageView[7][3];
        weekIcon[0][0] = (ImageView) view.findViewById(R.id.icon_image_1_1);
        weekIcon[0][1] = (ImageView) view.findViewById(R.id.icon_image_1_2);
        weekIcon[0][2] = (ImageView) view.findViewById(R.id.icon_image_1_3);
        weekIcon[1][0] = (ImageView) view.findViewById(R.id.icon_image_2_1);
        weekIcon[1][1] = (ImageView) view.findViewById(R.id.icon_image_2_2);
        weekIcon[1][2] = (ImageView) view.findViewById(R.id.icon_image_2_3);
        weekIcon[2][0] = (ImageView) view.findViewById(R.id.icon_image_3_1);
        weekIcon[2][1] = (ImageView) view.findViewById(R.id.icon_image_3_2);
        weekIcon[2][2] = (ImageView) view.findViewById(R.id.icon_image_3_3);
        weekIcon[3][0] = (ImageView) view.findViewById(R.id.icon_image_4_1);
        weekIcon[3][1] = (ImageView) view.findViewById(R.id.icon_image_4_2);
        weekIcon[3][2] = (ImageView) view.findViewById(R.id.icon_image_4_3);
        weekIcon[4][0] = (ImageView) view.findViewById(R.id.icon_image_5_1);
        weekIcon[4][1] = (ImageView) view.findViewById(R.id.icon_image_5_2);
        weekIcon[4][2] = (ImageView) view.findViewById(R.id.icon_image_5_3);
        weekIcon[5][0] = (ImageView) view.findViewById(R.id.icon_image_6_1);
        weekIcon[5][1] = (ImageView) view.findViewById(R.id.icon_image_6_2);
        weekIcon[5][2] = (ImageView) view.findViewById(R.id.icon_image_6_3);
        weekIcon[6][0] = (ImageView) view.findViewById(R.id.icon_image_7_1);
        weekIcon[6][1] = (ImageView) view.findViewById(R.id.icon_image_7_2);
        weekIcon[6][2] = (ImageView) view.findViewById(R.id.icon_image_7_3);

        this.poweredText = (TextView) view.findViewById(R.id.weather_trend_view_powered_text);
        this.trendContainer = (FrameLayout) view.findViewById(R.id.trend_view_container);
        this.weatherTrendView = (TrendView) view.findViewById(R.id.weather_trend_view);
        this.weatherTrendView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetectorTrendView.onTouchEvent(event);
            }
        });

        this.hourlyContainer = (FrameLayout) view.findViewById(R.id.hourly_view_container);
        this.weatherHourlyView = (HourlyView) view.findViewById(R.id.weather_hourly_view);
        this.weatherHourlyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetectorHourlyView.onTouchEvent(event);
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

        this.swipeRefreshLayout = (MySwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        this.swipeRefreshLayout.setColorSchemeColors(R.color.lightPrimary_3, R.color.darkPrimary_1);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+") && location.hefengResult != null) {
            this.refreshUI();
        } else if (location.juheResult != null) {
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

    public void setLocation() {
        this.location = MainActivity.lastLocation;
    }

    private void refreshData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.showStar = ! sharedPreferences.getBoolean(getString(R.string.key_hide_star), false);

        if (location.location.equals(getString(R.string.local))) {
            // get location
            this.initBaiduLocation();
        } else {
            this.getTotalData(location.location);
        }
    }

    private void getTotalData(final String searchLocation) {
        Thread thread=new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (! MainActivity.isNetConnected(getActivity())) {
                    Message message = new Message();
                    message.what = REFRESH_TOTAL_DATA_FAILED;
                    safeHandler.sendMessage(message);
                } else {
                    if (searchLocation.replaceAll(" ", "").matches("[a-zA-Z]+")) {
                        Log.d("WeatherFRAG", searchLocation);
                        location.hefengResult = HefengWeather.requestInternationalData(searchLocation);
                        Message message = new Message();
                        if (location.hefengResult == null) {
                            message.what = REFRESH_TOTAL_DATA_FAILED;
                        } else if (! location.hefengResult.heWeather.get(0).status.equals("ok")) {
                            message.what = REFRESH_TOTAL_DATA_FAILED;
                        } else {
                            message.what = REFRESH_TOTAL_DATA_SUCCEED;
                        }
                        safeHandler.sendMessage(message);
                    } else {
                        location.juheResult = JuheWeather.getRequest(searchLocation);
                        Message message = new Message();
                        if (location.juheResult == null) {
                            message.what = REFRESH_TOTAL_DATA_FAILED;
                        } else if (! location.juheResult.error_code.equals("0")) {
                            message.what = REFRESH_TOTAL_DATA_FAILED;
                        } else {
                            message.what = REFRESH_TOTAL_DATA_SUCCEED;
                        }
                        safeHandler.sendMessage(message);
                    }
                }
            }
        });
        thread.start();
    }

    private void getHourlyData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+") && location.hefengResult == null) {
                    message.what = REFRESH_HOURLY_DATA_FAILED;
                } else if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+") && ! location.hefengResult.heWeather.get(0).status.equals("ok")) {
                    message.what = REFRESH_HOURLY_DATA_FAILED;
                } else if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+")) {
                    message.what = REFRESH_HOURLY_DATA_SUCCEED;
                } else if (location.juheResult == null) {
                    message.what = REFRESH_HOURLY_DATA_FAILED;
                } else if (! location.juheResult.error_code.equals("0")) {
                    message.what = REFRESH_HOURLY_DATA_FAILED;
                } else {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    boolean useEnglish = sharedPreferences.getBoolean(getString(R.string.key_get_hourly_data_by_eng), false);
                    location.hefengResult = HefengWeather.requestHourlyData(location.juheResult.result.data.realtime.city_name, useEnglish);
                    if (location.hefengResult == null) {
                        message.what = REFRESH_HOURLY_DATA_FAILED;
                    } else if (! location.hefengResult.heWeather.get(0).status.equals("ok")) {
                        message.what = REFRESH_HOURLY_DATA_FAILED;
                    } else {
                        message.what = REFRESH_HOURLY_DATA_SUCCEED;
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
        if (refreshSucceed) {
            if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+") && location.hefengResult == null) {
                return;
            } else if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+") && ! location.hefengResult.heWeather.get(0).status.equals("ok")) {
                return;
            } else if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+")) {
                info = HefengWeather.getWeatherInfoToShow(getActivity(), location.hefengResult, MainActivity.isDay);
            } else if (location.juheResult == null) {
                return;
            } else if (! location.juheResult.error_code.equals("0")) {
                return;
            } else {
                info = JuheWeather.getWeatherInfoToShow(getActivity(), location.juheResult, MainActivity.isDay);
            }
        }

        if (info == null) {
            return;
        }

        if (MainActivity.needChangeTime()) {
            MainActivity.isDay = ! MainActivity.isDay;
            this.changeTime();
            MainActivity.setBackgroundPlateColor(getActivity(), false);
            MainActivity.setNavHead();
            MainActivity.initNavigationBar(getActivity(), getActivity().getWindow());
            this.setWindowTopColor();
        }

        this.weatherTextLive.setText(info.weatherNow + " " + info.tempNow + "℃");
        weatherTextLive.setVisibility(View.VISIBLE);
        this.timeTextLive.setText(info.refreshTime);
        this.locationTextLive.setText(info.location);

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

        final AnimatorSet[][] animatorSetsWeekIcon = new AnimatorSet[7][3];

        for (int i = 0; i < 7; i ++) {
            if (i == 0) {
                this.weekText[i].setText(getString(R.string.today));
            } else {
                this.weekText[i].setText(info.week[i]);
            }

            int[] imageId= JuheWeather.getWeatherIcon(info.weatherKind[i], MainActivity.isDay);
            final int[] animatorId = JuheWeather.getAnimatorId(info.weatherKind[i], MainActivity.isDay);

            for (int j = 0; j < 3; j ++) {
                if (imageId[j] != 0) {
                    weekIcon[i][j].setImageResource(imageId[j]);
                    weekIcon[i][j].setVisibility(View.VISIBLE);
                    animatorSetsWeekIcon[i][j] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), animatorId[j]);
                    animatorSetsWeekIcon[i][j].setTarget(weekIcon[i][j]);
                } else {
                    weekIcon[i][j].setVisibility(View.GONE);
                }
            }
            final int position = i;
            weekIcon[i][0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < 3; i ++) {
                        if (animatorId[i] != 0) {
                            animatorSetsWeekIcon[position][i].start();
                            Toast.makeText(getActivity(),
                                    info.weather[position],
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        for (int i = 0; i < 7; i ++) {
            maxiTemp[i] = Integer.parseInt(info.maxiTemp[i]);
            miniTemp[i] = Integer.parseInt(info.miniTemp[i]);
        }
        this.trendContainer.setAlpha(1);
        this.trendContainer.setVisibility(View.VISIBLE);
        this.hourlyContainer.setVisibility(View.GONE);
        int[] yesterdayTemp = MainActivity.readYesterdayWeather(getActivity(), info);
        this.weatherTrendView.setData(maxiTemp, miniTemp, yesterdayTemp);
        this.weatherTrendView.invalidate();

        if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+")) {
            poweredText.setText(getString(R.string.powered_by_hefeng));
        } else {
            poweredText.setText(getString(R.string.powered_by_juhe));
        }
        this.initLifeInfo(info);

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
        this.showAnimals();
        this.showCircles();
        this.showWeatherIcon();
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

            for (int i = 0; i < 4; i ++) {
                this.animatorSetsTouch[i].setTarget(circles[i]);
            } for (int i = 0; i < 2; i ++) {
                start[i].setVisibility(View.GONE);
            }
        } else {
            // night
            circles[0].setImageResource(R.drawable.circle_night_1);
            circles[1].setImageResource(R.drawable.circle_night_2);
            circles[2].setImageResource(R.drawable.circle_night_3);
            circles[3].setImageResource(R.drawable.circle_night_4);
            circles[4].setImageResource(R.drawable.rec_night);

            for (int i = 0; i < 4; i ++) {
                this.animatorSetsTouch[i].setTarget(circles[i]);
            }
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

        for (int i = 0; i < 2; i ++) {
            this.animatorSetsAnimal[i].setTarget(start[i]);
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
        this.animatorSetsTouch[3].addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (location.juheResult != null) {
                    for (int i = 0; i < 3; i ++) {
                        if (showIcon[i]) {
                            animatorSetsIcon[i].start();
                        }
                    }
                }
            }
        });
        for (int i = 0; i < 4; i ++) {
            this.animatorSetsTouch[i].start();
        }
    }

    public void showAnimals() {
        // show animals
        this.animatorSetsAnimal[0].addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animatorSetsAnimal[0].start();
            }
        });
        this.animatorSetsAnimal[1].addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animatorSetsAnimal[1].start();
            }
        });

        for (int i = 0; i < 2; i ++) {
            this.animatorSetsAnimal[i].start();
        }
    }

    public void showWeatherIcon() {
        if (refreshSucceed) {
            if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+") && location.hefengResult == null) {
                return;
            } else if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+") && ! location.hefengResult.heWeather.get(0).status.equals("ok")) {
                return;
            } else if (! location.location.replaceAll(" ", "").matches("[a-zA-Z]+") && location.juheResult == null) {
                return;
            } else if (! location.location.replaceAll(" ", "").matches("[a-zA-Z]+") && ! location.juheResult.error_code.equals("0")) {
                return;
            }
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
                    setWeatherIcon();

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
            this.setWeatherIcon();

            for (int i = 0; i < 3; i ++) {
                if (showIcon[i]) {
                    animatorSetsRise[i].setTarget(weatherIcon[i]);
                    animatorSetsRise[i].start();
                }
            }
        }
    }

    public void animatorCancel() {
        if (animatorSetsShow != null) {
            for (AnimatorSet anAnimatorSetsShow : animatorSetsShow) {
                if (anAnimatorSetsShow != null) {
                    anAnimatorSetsShow.pause();
                    anAnimatorSetsShow.cancel();
                }
            }
        }
        if (animatorSetsIcon != null) {
            for (AnimatorSet anAnimatorSetsIcon : animatorSetsIcon) {
                if (anAnimatorSetsIcon != null) {
                    anAnimatorSetsIcon.pause();
                    anAnimatorSetsIcon.cancel();
                }
            }
        }
        if (animatorSetsAnimal != null) {
            for (AnimatorSet anAnimatorSetsAnimal : animatorSetsAnimal) {
                if (anAnimatorSetsAnimal != null) {
                    anAnimatorSetsAnimal.pause();
                    anAnimatorSetsAnimal.cancel();
                }
            }
        }
        if (animatorSetsTouch != null) {
            for (AnimatorSet anAnimatorSetsTouch : animatorSetsTouch) {
                if (anAnimatorSetsTouch != null) {
                    anAnimatorSetsTouch.pause();
                    anAnimatorSetsTouch.cancel();
                }
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

                                animatorSetsTouch[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_day_1);
                                animatorSetsTouch[position].setTarget(circles[position]);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_1);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;

                            case 1:
                                circles[position].setImageResource(R.drawable.circle_day_2);

                                animatorSetsTouch[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_day_2);
                                animatorSetsTouch[position].setTarget(circles[position]);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_2);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;

                            case 2:
                                circles[position].setImageResource(R.drawable.circle_day_3);

                                animatorSetsTouch[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_day_3);
                                animatorSetsTouch[position].setTarget(circles[position]);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_3);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;

                            case 3:
                                circles[position].setImageResource(R.drawable.circle_day_4);

                                animatorSetsTouch[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_day_4);
                                animatorSetsTouch[position].setTarget(circles[position]);

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

                                animatorSetsTouch[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_night_1);
                                animatorSetsTouch[position].setTarget(circles[position]);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_1);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;

                            case 1:
                                circles[position].setImageResource(R.drawable.circle_night_2);

                                animatorSetsTouch[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_night_2);
                                animatorSetsTouch[position].setTarget(circles[position]);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_2);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;
                            case 2:
                                circles[position].setImageResource(R.drawable.circle_night_3);

                                animatorSetsTouch[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_night_3);
                                animatorSetsTouch[position].setTarget(circles[position]);

                                animatorSetsShow[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_show_3);
                                animatorSetsShow[position].setTarget(circles[position]);
                                animatorSetsShow[position].start();
                                break;
                            case 3:
                                circles[position].setImageResource(R.drawable.circle_night_4);

                                animatorSetsTouch[position] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.circle_touch_night_4);
                                animatorSetsTouch[position].setTarget(circles[position]);

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

    private void setWeatherIcon() {
        this.showIcon = new boolean[3];

        if (info == null) {
            for (int i = 0; i < showIcon.length; i ++) {
                showIcon[i] = false;
            }
            return;
        }

        int[] imageId = JuheWeather.getWeatherIcon(info.weatherKindNow, MainActivity.isDay);
        int[] animatorId = JuheWeather.getAnimatorId(info.weatherKindNow, MainActivity.isDay);
        this.animatorSetsIcon = new AnimatorSet[3];

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

    private void initLifeInfo(WeatherInfoToShow info) {
        windKind.setText(info.windTitle);
        windInfo.setText(info.windInfo);

        pmKind.setText(info.pmTitle);
        pmInfo.setText(info.pmInfo);

        waterKind.setText(info.humTitle);
        waterInfo.setText(info.humInfo);

        sunKind.setText(info.uvTitle);
        sunInfo.setText(info.uvInfo);

        wearKind.setText(info.dressTitle);
        wearInfo.setText(info.dressInfo);

        illKind.setText(info.coldTitle);
        illInfo.setText(info.coldInfo);

        airKind.setText(info.airTitle);
        airInfo.setText(info.airInfo);

        washCarKind.setText(info.washCarTitle);
        washCarInfo.setText(info.washCarInfo);

        sportKind.setText(info.exerciseTitle);
        sportInfo.setText(info.exerciseInfo);
    }

// database

    private void initDatabaseHelper() {
        this.databaseHelper = new MyDatabaseHelper(getActivity(),
                MyDatabaseHelper.DATABASE_NAME,
                null,
                2);
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

            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = REFRESH_TOTAL_DATA_FAILED;
                    safeHandler.sendMessage(message);
                }
            });
            thread.start();
        } else {
            this.getTotalData(location);
        }

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

    private class PageOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(e2.getX() - e1.getX()) < Math.abs(e2.getY() - e1.getY())) {
                return false;
            }
            weatherCard.getParent().requestDisallowInterceptTouchEvent(true);
            lifeCard.getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e2.getX() - e1.getX()) < Math.abs(e2.getY() - e1.getY())) {
                return false;
            }
            weatherCard.getParent().requestDisallowInterceptTouchEvent(true);
            lifeCard.getParent().requestDisallowInterceptTouchEvent(true);
            weatherCard.setX(weatherCard.viewStartX + (e2.getX() - e1.getX()));
            lifeCard.setX(lifeCard.viewStartX + (e2.getX() - e1.getX()));
            weatherCard.setVisibility(View.GONE);
            weatherCard.setX(weatherCard.viewStartX);
            weatherCard.setY(weatherCard.viewStartY);
            lifeCard.setVisibility(View.GONE);
            lifeCard.setX(weatherCard.viewStartX);
            lifeCard.setY(weatherCard.viewStartY);

            for (int i = 0; i < MainActivity.locationList.size(); i ++) {
                if (location.location.equals(MainActivity.locationList.get(i).location)) {
                    if (e2.getX() < e1.getX()) {
                        i ++;
                        if (i >= MainActivity.locationList.size()) {
                            i = 0;
                        }
                    } else {
                        i --;
                        if (i < 0) {
                            i = MainActivity.locationList.size() - 1;
                        }
                    }
                    location = MainActivity.locationList.get(i);
                    break;
                }
            }
            refreshAll();
            return true;
        }
    }

    private class TrendViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(e2.getX() - e1.getX()) < Math.abs(e2.getY() - e1.getY())) {
                return false;
            }
            weatherCard.getParent().requestDisallowInterceptTouchEvent(true);
            lifeCard.getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e2.getX() - e1.getX()) < Math.abs(e2.getY() - e1.getY())) {
                return false;
            }
            weatherCard.getParent().requestDisallowInterceptTouchEvent(true);
            lifeCard.getParent().requestDisallowInterceptTouchEvent(true);
            weatherCard.setX(weatherCard.viewStartX + (e2.getX() - e1.getX()));
            lifeCard.setX(lifeCard.viewStartX + (e2.getX() - e1.getX()));
            weatherCard.setVisibility(View.GONE);
            weatherCard.setX(weatherCard.viewStartX);
            weatherCard.setY(weatherCard.viewStartY);
            lifeCard.setVisibility(View.GONE);
            lifeCard.setX(weatherCard.viewStartX);
            lifeCard.setY(weatherCard.viewStartY);

            for (int i = 0; i < MainActivity.locationList.size(); i ++) {
                if (location.location.equals(MainActivity.locationList.get(i).location)) {
                    if (e2.getX() < e1.getX()) {
                        i ++;
                        if (i >= MainActivity.locationList.size()) {
                            i = 0;
                        }
                    } else {
                        i --;
                        if (i < 0) {
                            i = MainActivity.locationList.size() - 1;
                        }
                    }
                    location = MainActivity.locationList.get(i);
                    break;
                }
            }
            refreshAll();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (location.hefengResult == null || ! location.hefengResult.heWeather.get(0).status.equals("ok") || freshData) {
                getHourlyData();
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
            return true;
        }
    }

    private class HourlyViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(e2.getX() - e1.getX()) < Math.abs(e2.getY() - e1.getY())) {
                return false;
            }
            weatherCard.getParent().requestDisallowInterceptTouchEvent(true);
            lifeCard.getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e2.getX() - e1.getX()) < Math.abs(e2.getY() - e1.getY())) {
                return false;
            }
            weatherCard.getParent().requestDisallowInterceptTouchEvent(true);
            lifeCard.getParent().requestDisallowInterceptTouchEvent(true);
            weatherCard.setX(weatherCard.viewStartX + (e2.getX() - e1.getX()));
            lifeCard.setX(lifeCard.viewStartX + (e2.getX() - e1.getX()));
            weatherCard.setVisibility(View.GONE);
            weatherCard.setX(weatherCard.viewStartX);
            weatherCard.setY(weatherCard.viewStartY);
            lifeCard.setVisibility(View.GONE);
            lifeCard.setX(weatherCard.viewStartX);
            lifeCard.setY(weatherCard.viewStartY);

            for (int i = 0; i < MainActivity.locationList.size(); i ++) {
                if (location.location.equals(MainActivity.locationList.get(i).location)) {
                    if (e2.getX() < e1.getX()) {
                        i ++;
                        if (i >= MainActivity.locationList.size()) {
                            i = 0;
                        }
                    } else {
                        i --;
                        if (i < 0) {
                            i = MainActivity.locationList.size() - 1;
                        }
                    }
                    location = MainActivity.locationList.get(i);
                    break;
                }
            }
            refreshAll();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
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
            return true;
        }
    }

// handler

    @Override
    public void handleMessage(Message message) {
        switch(message.what)
        {
            case REFRESH_TOTAL_DATA_SUCCEED:
                this.refreshSucceed = true;
                this.freshData = true;

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

                swipeRefreshLayout.setRefreshing(false);
                refreshUI();

                if (info != null) {
                    MainActivity.writeWeatherInfo(getActivity(), location.location, info);
                    MainActivity.writeTodayWeather(getActivity(), info);
                    MainActivity.sendNotification(getActivity(), location);
                    MainActivity.refreshWidget(getActivity(), location, info, MainActivity.isDay);
                }

                break;
            case REFRESH_HOURLY_DATA_SUCCEED:
                freshData = false;
                if (location.hefengResult.heWeather.get(0).hourly_forecast == null) {
                    weatherHourlyView.setData(null, null);
                    weatherHourlyView.invalidate();
                } else if (location.hefengResult.heWeather.get(0).hourly_forecast.size() == 0) {
                    weatherHourlyView.setData(null, null);
                    weatherHourlyView.invalidate();
                } else {
                    int[] temp = new int[location.hefengResult.heWeather.get(0).hourly_forecast.size()];
                    float[] pop = new float[temp.length];
                    for (int i = 0; i <temp.length; i ++) {
                        temp[i] = Integer.parseInt(location.hefengResult.heWeather.get(0).hourly_forecast.get(i).tmp);
                        pop[i] = Float.parseFloat(location.hefengResult.heWeather.get(0).hourly_forecast.get(i).pop);
                        if (temp[i] > maxiTemp[0]) {
                            temp[i] = maxiTemp[0];
                        } else if (temp[i] < miniTemp[0]) {
                            temp[i] = miniTemp[0];
                        }
                    }
                    weatherHourlyView.setData(temp, pop);
                    weatherHourlyView.invalidate();
                }
                break;
            case REFRESH_TOTAL_DATA_FAILED:
                this.refreshSucceed = false;
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(
                        getActivity(),
                        getString(R.string.refresh_data_failed),
                        Toast.LENGTH_SHORT).show();

                info = MainActivity.readWeatherInfo(getActivity(), location.location);
                if (info != null) {
                    SharedPreferences.Editor editor1 = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                    int hour1 = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    if (5 < hour1 && hour1 < 19) {
                        editor1.putBoolean(getString(R.string.key_isDay), true);
                    } else {
                        editor1.putBoolean(getString(R.string.key_isDay), false);
                    } editor1.apply();

                    for (int i = 0; i < MainActivity.locationList.size(); i ++) {
                        if (MainActivity.locationList.get(i).location.equals(location.location)) {
                            MainActivity.locationList.remove(i);
                            MainActivity.locationList.add(i, location);
                            MainActivity.lastLocation = location;
                            break;
                        }
                    }

                    refreshUI();
                }

                break;
            case REFRESH_HOURLY_DATA_FAILED:
                Toast.makeText(
                        getActivity(),
                        getString(R.string.try_set_eng_location),
                        Toast.LENGTH_LONG).show();
                break;
        }
    }
}