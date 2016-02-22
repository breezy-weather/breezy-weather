package wangdaye.com.geometricweather.Service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;

import java.util.Calendar;
import java.util.List;

import wangdaye.com.geometricweather.Activity.MainActivity;
import wangdaye.com.geometricweather.Data.JuheResult;
import wangdaye.com.geometricweather.Data.JuheWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Receiver.WidgetProviderWeek;
import wangdaye.com.geometricweather.Widget.HandlerContainer;
import wangdaye.com.geometricweather.Widget.SafeHandler;

/**
 * Created by WangDaYe on 2016/2/8.
 */

public class RefreshWidgetWeek extends Service
        implements HandlerContainer {
    // data
    private boolean showCard;
    private JuheResult juheResult;

    private final int REFRESH_DATA_SUCCEED = 1;
    private final int REFRESH_DATA_FAILED = 0;

    // baidu locaiton
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    // handler
    private SafeHandler<RefreshWidgetWeek> safeHandler;

    //TAG
//    private final String TAG = "RefreshWidgetWeek";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.safeHandler = new SafeHandler<>(this);

        this.refreshUIFromLocalData();
        this.refreshWidget();

        this.stopSelf(startId);
        return START_NOT_STICKY;
    }

    private void refreshWidget() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.sp_widget_week_setting),
                Context.MODE_PRIVATE);
        this.showCard = sharedPreferences.getBoolean(getString(R.string.key_show_card), false);
        String locationName = sharedPreferences.getString(
                getString(R.string.key_location),
                getString(R.string.local));

        if(locationName.equals(getString(R.string.local))) {
            mLocationClient = new LocationClient(this); // 声明LocationClient类
            mLocationClient.registerLocationListener( myListener ); // 注册监听函数

            this.initBaiduMap();
        } else {
            this.getWeather(locationName);
        }
    }

    private void getWeather(final String searchLocation) {
        Thread thread=new Thread(new Runnable()
        {
            @Override
            public void run()
            { // TODO Auto-generated method stub
                juheResult = JuheWeather.getRequest(searchLocation);
                Message message=new Message();
                if (juheResult == null) {
                    message.what = REFRESH_DATA_FAILED;
                } else {
                    message.what = REFRESH_DATA_SUCCEED;
                }
                safeHandler.sendMessage(message);
            }
        });
        thread.start();
    }

    private void initBaiduMap() {
        // initialize baidu location
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    private void refreshUI() {
        if(this.juheResult != null) {
            this.refreshUIFromInternet();
        } else {
            Toast.makeText(this, getString(R.string.refresh_widget_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshUIFromInternet() {
        boolean isDay;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (5 < hour && hour < 19) {
            isDay = true;
        } else {
            isDay = false;
        }

        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_week);
        List<JuheResult.Weather> weather = this.juheResult.result.data.weather;
        // set icon
        int[] imageId;
        if (isDay) {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(0).info.day.get(1)), true);
            views.setImageViewResource(R.id.widget_week_image_1, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(1).info.day.get(1)), true);
            views.setImageViewResource(R.id.widget_week_image_2, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(2).info.day.get(1)), true);
            views.setImageViewResource(R.id.widget_week_image_3, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(3).info.day.get(1)), true);
            views.setImageViewResource(R.id.widget_week_image_4, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(4).info.day.get(1)), true);
            views.setImageViewResource(R.id.widget_week_image_5, imageId[3]);
        } else {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(0).info.night.get(1)), false);
            views.setImageViewResource(R.id.widget_week_image_1, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(1).info.night.get(1)), false);
            views.setImageViewResource(R.id.widget_week_image_2, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(2).info.night.get(1)), false);
            views.setImageViewResource(R.id.widget_week_image_3, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(3).info.night.get(1)), false);
            views.setImageViewResource(R.id.widget_week_image_4, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(4).info.night.get(1)), false);
            views.setImageViewResource(R.id.widget_week_image_5, imageId[3]);
        }
        // temperature
        String temp;
        // 1
        temp = weather.get(0).info.night.get(2)
                + "/"
                + weather.get(0).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_week_temp_1, temp);
        // 2
        temp = weather.get(1).info.night.get(2)
                + "/"
                + weather.get(1).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_week_temp_2, temp);
        // 3
        temp = weather.get(2).info.night.get(2)
                + "/"
                + weather.get(2).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_week_temp_3, temp);
        // 4
        temp = weather.get(3).info.night.get(2)
                + "/"
                + weather.get(3).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_week_temp_4, temp);
        // 5
        temp = weather.get(4).info.night.get(2)
                + "/"
                + weather.get(4).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_week_temp_5, temp);
        // week
        String week;
        // 1
        week = this.juheResult.result.data.realtime.city_name;
        views.setTextViewText(R.id.widget_week_week_1, week);
        // 2
        week = getString(R.string.week) + weather.get(1).week;
        views.setTextViewText(R.id.widget_week_week_2, week);
        // 3
        week = getString(R.string.week) + weather.get(2).week;
        views.setTextViewText(R.id.widget_week_week_3, week);
        // 4
        week = getString(R.string.week) + weather.get(3).week;
        views.setTextViewText(R.id.widget_week_week_4, week);
        // 5
        week = getString(R.string.week) + weather.get(4).week;
        views.setTextViewText(R.id.widget_week_week_5, week);
        // set card and text color
        if (this.showCard) { // show card
            views.setViewVisibility(R.id.widget_week_card, View.VISIBLE);
            // week text
            views.setTextColor(R.id.widget_week_week_1, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_2, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_3, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_4, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_5, ContextCompat.getColor(this, R.color.colorTextDark));
            // temperature text
            views.setTextColor(R.id.widget_week_temp_1, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_2, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_3, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_4, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_5, ContextCompat.getColor(this, R.color.colorTextDark));
        } else { // do not show card
            views.setViewVisibility(R.id.widget_week_card, View.GONE);
            // week text
            views.setTextColor(R.id.widget_week_week_1, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_2, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_3, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_4, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_5, ContextCompat.getColor(this, R.color.colorTextLight));
            // temperature text
            views.setTextColor(R.id.widget_week_temp_1, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_2, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_3, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_4, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_5, ContextCompat.getColor(this, R.color.colorTextLight));
        }

        //Intent intent = new Intent("com.geometricweather.receiver.CLICK_WIDGET");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_week_button, pendingIntent);

        // refresh UI
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.updateAppWidget(new ComponentName(this, WidgetProviderWeek.class), views);

        SharedPreferences.Editor editor = getSharedPreferences(
                getString(R.string.sp_widget_week_setting), Context.MODE_PRIVATE).edit();
        editor.putBoolean(getString(R.string.key_saved_data), true);
        // weather
        if (isDay) {
            editor.putString(getString(R.string.key_weather_kind_today), JuheWeather.getWeatherKind(weather.get(0).info.day.get(1)));
            editor.putString(getString(R.string.key_weather_2), JuheWeather.getWeatherKind(weather.get(1).info.day.get(1)));
            editor.putString(getString(R.string.key_weather_3), JuheWeather.getWeatherKind(weather.get(2).info.day.get(1)));
            editor.putString(getString(R.string.key_weather_4), JuheWeather.getWeatherKind(weather.get(3).info.day.get(1)));
            editor.putString(getString(R.string.key_weather_5), JuheWeather.getWeatherKind(weather.get(4).info.day.get(1)));
        } else {
            editor.putString(getString(R.string.key_weather_kind_today), JuheWeather.getWeatherKind(weather.get(0).info.night.get(1)));
            editor.putString(getString(R.string.key_weather_2), JuheWeather.getWeatherKind(weather.get(1).info.night.get(1)));
            editor.putString(getString(R.string.key_weather_3), JuheWeather.getWeatherKind(weather.get(2).info.night.get(1)));
            editor.putString(getString(R.string.key_weather_4), JuheWeather.getWeatherKind(weather.get(3).info.night.get(1)));
            editor.putString(getString(R.string.key_weather_5), JuheWeather.getWeatherKind(weather.get(4).info.night.get(1)));
        }
        // week
        editor.putString(getString(R.string.key_city_time), this.juheResult.result.data.realtime.city_name);
        editor.putString(getString(R.string.key_week_2), getString(R.string.week) + weather.get(1).week);
        editor.putString(getString(R.string.key_week_3), getString(R.string.week) + weather.get(2).week);
        editor.putString(getString(R.string.key_week_4), getString(R.string.week) + weather.get(3).week);
        editor.putString(getString(R.string.key_week_5), getString(R.string.week) + weather.get(4).week);
        // temperature
        editor.putString(getString(R.string.key_temperature_today),
                weather.get(0).info.night.get(2) + "/" + weather.get(0).info.day.get(2) + "°");
        editor.putString(getString(R.string.key_temperature_2),
                weather.get(1).info.night.get(2) + "/" + weather.get(1).info.day.get(2) + "°");
        editor.putString(getString(R.string.key_temperature_3),
                weather.get(2).info.night.get(2) + "/" + weather.get(2).info.day.get(2) + "°");
        editor.putString(getString(R.string.key_temperature_4),
                weather.get(3).info.night.get(2) + "/" + weather.get(3).info.day.get(2) + "°");
        editor.putString(getString(R.string.key_temperature_5),
                weather.get(4).info.night.get(2) + "/" + weather.get(4).info.day.get(2) + "°");
        editor.apply();
    }

    private void refreshUIFromLocalData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.sp_widget_week_setting), Context.MODE_PRIVATE);
        if (! sharedPreferences.getBoolean(getString(R.string.key_saved_data), false)) {
            return;
        }

        String[] weekText = new String[5];
        weekText[0] = sharedPreferences.getString(getString(R.string.key_city_time), getString(R.string.wait_refresh));
        weekText[1] = sharedPreferences.getString(getString(R.string.key_week_2), getString(R.string.ellipsis));
        weekText[2] = sharedPreferences.getString(getString(R.string.key_week_3), getString(R.string.ellipsis));
        weekText[3] = sharedPreferences.getString(getString(R.string.key_week_4), getString(R.string.ellipsis));
        weekText[4] = sharedPreferences.getString(getString(R.string.key_week_5), getString(R.string.ellipsis));

        String[] weatherKind = new String[5];
        weatherKind[0] = sharedPreferences.getString(getString(R.string.key_weather_kind_today), "阴");
        weatherKind[1] = sharedPreferences.getString(getString(R.string.key_weather_2), "阴");
        weatherKind[2] = sharedPreferences.getString(getString(R.string.key_weather_3), "阴");
        weatherKind[3] = sharedPreferences.getString(getString(R.string.key_weather_4), "阴");
        weatherKind[4] = sharedPreferences.getString(getString(R.string.key_weather_5), "阴");

        String[] tempText = new String[5];
        tempText[0] = sharedPreferences.getString(getString(R.string.key_temperature_today), getString(R.string.ellipsis));
        tempText[1] = sharedPreferences.getString(getString(R.string.key_temperature_2), getString(R.string.ellipsis));
        tempText[2] = sharedPreferences.getString(getString(R.string.key_temperature_3), getString(R.string.ellipsis));
        tempText[3] = sharedPreferences.getString(getString(R.string.key_temperature_4), getString(R.string.ellipsis));
        tempText[4] = sharedPreferences.getString(getString(R.string.key_temperature_5), getString(R.string.ellipsis));

        boolean isDay;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (5 < hour && hour < 19) {
            isDay = true;
        } else {
            isDay = false;
        }

        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_week);
        // set icon
        int[] imageId;
        if (isDay) {
            imageId = JuheWeather.getWeatherIcon(weatherKind[0], true);
            views.setImageViewResource(R.id.widget_week_image_1, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(weatherKind[1], true);
            views.setImageViewResource(R.id.widget_week_image_2, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(weatherKind[2], true);
            views.setImageViewResource(R.id.widget_week_image_3, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(weatherKind[3], true);
            views.setImageViewResource(R.id.widget_week_image_4, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(weatherKind[4], true);
            views.setImageViewResource(R.id.widget_week_image_5, imageId[3]);
        } else {
            imageId = JuheWeather.getWeatherIcon(weatherKind[0], false);
            views.setImageViewResource(R.id.widget_week_image_1, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(weatherKind[1], false);
            views.setImageViewResource(R.id.widget_week_image_2, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(weatherKind[2], false);
            views.setImageViewResource(R.id.widget_week_image_3, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(weatherKind[3], false);
            views.setImageViewResource(R.id.widget_week_image_4, imageId[3]);
            imageId = JuheWeather.getWeatherIcon(weatherKind[4], false);
            views.setImageViewResource(R.id.widget_week_image_5, imageId[3]);
        }
        // temperature
        views.setTextViewText(R.id.widget_week_temp_1, tempText[0]);
        views.setTextViewText(R.id.widget_week_temp_2, tempText[1]);
        views.setTextViewText(R.id.widget_week_temp_3, tempText[2]);
        views.setTextViewText(R.id.widget_week_temp_4, tempText[3]);
        views.setTextViewText(R.id.widget_week_temp_5, tempText[4]);
        // week
        views.setTextViewText(R.id.widget_week_week_1, weekText[0]);
        views.setTextViewText(R.id.widget_week_week_2, weekText[1]);
        views.setTextViewText(R.id.widget_week_week_3, weekText[2]);
        views.setTextViewText(R.id.widget_week_week_4, weekText[3]);
        views.setTextViewText(R.id.widget_week_week_5, weekText[4]);
        // set card and text color
        if (this.showCard) { // show card
            views.setViewVisibility(R.id.widget_week_card, View.VISIBLE);
            // week text
            views.setTextColor(R.id.widget_week_week_1, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_2, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_3, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_4, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_5, ContextCompat.getColor(this, R.color.colorTextDark));
            // temperature text
            views.setTextColor(R.id.widget_week_temp_1, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_2, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_3, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_4, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_5, ContextCompat.getColor(this, R.color.colorTextDark));
        } else { // do not show card
            views.setViewVisibility(R.id.widget_week_card, View.GONE);
            // week text
            views.setTextColor(R.id.widget_week_week_1, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_2, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_3, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_4, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_5, ContextCompat.getColor(this, R.color.colorTextLight));
            // temperature text
            views.setTextColor(R.id.widget_week_temp_1, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_2, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_3, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_4, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_5, ContextCompat.getColor(this, R.color.colorTextLight));
        }

        //Intent intent = new Intent("com.geometricweather.receiver.CLICK_WIDGET");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_week_button, pendingIntent);

        // refresh UI
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.updateAppWidget(new ComponentName(this, WidgetProviderWeek.class), views);
    }

    // inner class
    private class MyLocationListener implements BDLocationListener {
        // baidu location listener
        @Override
        public void onReceiveLocation(BDLocation location) {
            String locationName = null;

            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");
                locationName = location.getCity();
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
                locationName = location.getCity();
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
                locationName = location.getCity();
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }

            getWeather(locationName);

            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
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
    }

    @Override
    public void handleMessage(Message message) {
        switch(message.what)
        {
            case REFRESH_DATA_SUCCEED:
                refreshUI();
                break;
            default:
                Toast.makeText(this,
                        getString(R.string.refresh_widget_error),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
