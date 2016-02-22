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
import android.provider.AlarmClock;
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
import wangdaye.com.geometricweather.Receiver.WidgetProviderClockDay;
import wangdaye.com.geometricweather.Widget.HandlerContainer;
import wangdaye.com.geometricweather.Widget.SafeHandler;

/**
 * Created by WangDaYe on 2016/2/15.
 */
public class RefreshWidgetClockDay extends Service
        implements HandlerContainer {
    // data
    private boolean showCard;
    private JuheResult juheResult;

    private final int REFRESH_DATA_SUCCEED = 1;
    private final int REFRESH_DATA_FAILED = 0;

    // baidu location
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    // handler
    private SafeHandler<RefreshWidgetClockDay> safeHandler;

    //TAG
//    private final String TAG = "RefreshWidgetClockDay";

    // 成员方法
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
                getString(R.string.sp_widget_clock_day_setting), Context.MODE_PRIVATE);

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
        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_clock_day);

        JuheResult.WeatherNow weatherNow = this.juheResult.result.data.realtime.weatherNow;
        String weatherKind = JuheWeather.getWeatherKind(weatherNow.weatherInfo);
        int[] imageId = JuheWeather.getWeatherIcon(weatherKind, isDay);
        views.setImageViewResource(R.id.widget_clock_day_image, imageId[3]);
        String[] solar = this.juheResult.result.data.realtime.date.split("-");
        String dateText = solar[1] + "-" + solar[2]
                + " " + getString(R.string.week) + this.juheResult.result.data.weather.get(0).week
                + " / "
                + this.juheResult.result.data.realtime.moon;
        views.setTextViewText(R.id.widget_clock_day_date, dateText);
        String weatherText = this.juheResult.result.data.realtime.city_name
                + " / "
                + weatherNow.weatherInfo + " " + weatherNow.temperature + "℃";
        views.setTextViewText(R.id.widget_clock_day_weather, weatherText);

        if(this.showCard) { // show card
            views.setViewVisibility(R.id.widget_clock_day_card, View.VISIBLE);
            views.setTextColor(R.id.widget_clock_day_clock, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_date, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_weather, ContextCompat.getColor(this, R.color.colorTextDark));
        } else { // do not show card
            views.setViewVisibility(R.id.widget_clock_day_card, View.GONE);
            views.setTextColor(R.id.widget_clock_day_clock, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_date, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_weather, ContextCompat.getColor(this, R.color.colorTextLight));
        }

        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(this, 0, intentClock, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_clock_button, pendingIntentClock);

        Intent intentWeather = new Intent(this, MainActivity.class);
        PendingIntent pendingIntentWeather = PendingIntent.getActivity(this, 0, intentWeather, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_weather_button, pendingIntentWeather);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.updateAppWidget(new ComponentName(this, WidgetProviderClockDay.class), views);

        SharedPreferences.Editor editor = getSharedPreferences(
                getString(R.string.sp_widget_clock_day_setting), Context.MODE_PRIVATE).edit();
        editor.putBoolean(getString(R.string.key_saved_data), true);
        editor.putString(getString(R.string.key_weather_kind_today), weatherKind);
        editor.putString(getString(R.string.key_weather_today), weatherText);
        editor.putString(getString(R.string.key_city_time), dateText);
        editor.apply();
    }

    private void refreshUIFromLocalData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.sp_widget_clock_day_setting), Context.MODE_PRIVATE);
        if (! sharedPreferences.getBoolean(getString(R.string.key_saved_data), false)) {
            return;
        }
        String weatherKindToday = sharedPreferences.getString(getString(R.string.key_weather_kind_today), "阴");
        String weatherText = sharedPreferences.getString(getString(R.string.key_weather_today), getString(R.string.ellipsis));
        String dateText = sharedPreferences.getString(getString(R.string.key_city_time), getString(R.string.wait_refresh));

        boolean isDay;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (5 < hour && hour < 19) {
            isDay = true;
        } else {
            isDay = false;
        }

        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_clock_day);
        int[] imageId = JuheWeather.getWeatherIcon(weatherKindToday, isDay);
        views.setImageViewResource(R.id.widget_clock_day_image, imageId[3]);
        views.setTextViewText(R.id.widget_clock_day_date, dateText);
        views.setTextViewText(R.id.widget_clock_day_weather, weatherText);

        if(this.showCard) { // show card
            views.setViewVisibility(R.id.widget_clock_day_card, View.VISIBLE);
            views.setTextColor(R.id.widget_clock_day_clock, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_date, ContextCompat.getColor(this, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_weather, ContextCompat.getColor(this, R.color.colorTextDark));
        } else { // do not show card
            views.setViewVisibility(R.id.widget_clock_day_card, View.GONE);
            views.setTextColor(R.id.widget_clock_day_clock, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_date, ContextCompat.getColor(this, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_weather, ContextCompat.getColor(this, R.color.colorTextLight));
        }

        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(this, 0, intentClock, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_clock_button, pendingIntentClock);

        Intent intentWeather = new Intent(this, MainActivity.class);
        PendingIntent pendingIntentWeather = PendingIntent.getActivity(this, 0, intentWeather, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_weather_button, pendingIntentWeather);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.updateAppWidget(new ComponentName(this, WidgetProviderClockDay.class), views);
    }

    // inner class
    private class MyLocationListener implements BDLocationListener {
        // baidu location listener
        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            String locationName = null;

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

// handler

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
