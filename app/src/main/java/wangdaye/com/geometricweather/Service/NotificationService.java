package wangdaye.com.geometricweather.Service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import wangdaye.com.geometricweather.Data.GsonResult;
import wangdaye.com.geometricweather.Data.JuheWeather;
import wangdaye.com.geometricweather.Data.MyDatabaseHelper;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Receiver.NotificationReceiver;
import wangdaye.com.geometricweather.Widget.HandlerContainer;
import wangdaye.com.geometricweather.Widget.SafeHandler;

/**
 * Created by WangDaYe on 2016/2/8.
 */

public class NotificationService extends Service implements HandlerContainer{
    // data
    private MyDatabaseHelper databaseHelper;

    private GsonResult gsonResult;

    private final int REFRESH_DATA_SUCCEED = 1;
    private final int REFRESH_DATA_FAILED = 0;

    private final static int NOTIFICATION_ID = 7;

    // baidu location
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    // handler
    private SafeHandler<NotificationService> safeHandler;

    // TAG
//    private final static String TAG = "NotificationService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public  int onStartCommand(Intent intent, int flags, int startId) {
        this.safeHandler = new SafeHandler<>(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationSwitchOn = sharedPreferences.getBoolean(getString(R.string.key_notification_switch), false);
        boolean autoRefresh = sharedPreferences.getBoolean(getString(R.string.key_notification_auto_refresh_switch), false);

        if(notificationSwitchOn && autoRefresh) {
            this.sendNotification();

            int hour;
            String hourSave = sharedPreferences.getString(getString(R.string.key_notification_time),
                    getString(R.string.notification_refresh_time_default));
            switch (hourSave) {
                case "1hour":
                    hour = 1;
                    break;
                case "2hours":
                    hour = 2;
                    break;
                case "3hours":
                    hour = 3;
                    break;
                case "4hours":
                    hour = 4;
                    break;
                default:
                    hour = 2;
                    break;
            }

            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            final long time = 1000 * 60 * 60;
            long triggerAtTime = SystemClock.elapsedRealtime() + time * hour;
            Intent intentAlarm = new Intent(NotificationService.this, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(NotificationService.this, 0, intentAlarm, 0);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pendingIntent);
            this.stopSelf();
            return START_NOT_STICKY;
        } else {
            this.stopSelf();
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void sendNotification() {
        this.initDatabaseHelper();
        String location = this.readLocation();
        if(location.equals(getString(R.string.local))) {
            mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
            mLocationClient.registerLocationListener( myListener ); // 注册监听函数

            initBaiduMap();
        } else {
            getWeather(location);
        }
    }

    private void getWeather(final String searchLocation) {
        Thread thread=new Thread(new Runnable()
        {
            @Override
            public void run()
            { // TODO Auto-generated method stub
                gsonResult = JuheWeather.getRequest(searchLocation);
                Message message=new Message();
                if (gsonResult == null) {
                    message.what = REFRESH_DATA_FAILED;
                } else {
                    message.what = REFRESH_DATA_SUCCEED;
                }
                safeHandler.sendMessage(message);
            }
        });
        thread.start();
    }

    private void refreshUI() {
        // refresh the notification UI
        if(gsonResult == null) {
            Toast.makeText(this,
                    getString(R.string.send_notification_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isDay;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (5 < hour && hour < 19) {
            isDay = true;
        } else {
            isDay = false;
        }

        GsonResult.WeatherNow weatherNow = gsonResult.result.data.realtime.weatherNow;
        List<GsonResult.Weather> weathers = gsonResult.result.data.weather;

        String weatherKind = JuheWeather.getWeatherKind(weatherNow.weatherInfo);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // set level
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (sharedPreferences.getBoolean(getString(R.string.key_hide_notification_in_lockScreen), false)) {
                builder.setVisibility(Notification.VISIBILITY_SECRET);
            } else {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
        }

        // small view
        builder.setSmallIcon(JuheWeather.getMiniWeatherIcon(weatherKind, isDay));
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.notification_base);
        int[] imageId = JuheWeather.getWeatherIcon(weatherKind, isDay);
        view.setImageViewResource(R.id.notification_base_image_today, imageId[3]);
        view.setTextViewText(R.id.notification_base_text_title,
                weatherNow.weatherInfo
                        + " "
                        + weatherNow.temperature + "℃");
        view.setTextViewText(R.id.notification_base_text_details,
                weathers.get(0).info.night.get(2)
                        + "/"
                        + weathers.get(0).info.day.get(2)
                        + "°");
        String[] time = gsonResult.result.data.realtime.time.split(":");
        String text = gsonResult.result.data.realtime.city_name
                + "."
                + time[0]
                + ":"
                + time[1];
        view.setTextViewText(R.id.notification_base_text_remark, text);
        builder.setContent(view);

        Intent intent = new Intent(NotificationService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        // text color
        String textColor = sharedPreferences.getString(getString(R.string.key_notification_text_color),
                getString(R.string.notification_text_color_default));
        switch (textColor) {
            case "dark":
                int dark = R.color.colorTextDark;
                view.setTextColor(R.id.notification_base_text_title, ContextCompat.getColor(this, dark));
                dark = R.color.colorTextDark2nd;
                view.setTextColor(R.id.notification_base_text_details, ContextCompat.getColor(this, dark));
                view.setTextColor(R.id.notification_base_text_remark, ContextCompat.getColor(this, dark));
                break;
            case "grey":
                int grey = R.color.colorTextGrey;
                view.setTextColor(R.id.notification_base_text_title, ContextCompat.getColor(this, grey));
                grey = R.color.colorTextGrey2nd;
                view.setTextColor(R.id.notification_base_text_details, ContextCompat.getColor(this, grey));
                view.setTextColor(R.id.notification_base_text_remark, ContextCompat.getColor(this, grey));
                break;
            case "light":
                int light = R.color.colorTextLight;
                view.setTextColor(R.id.notification_base_text_title, ContextCompat.getColor(this, light));
                light = R.color.colorTextLight2nd;
                view.setTextColor(R.id.notification_base_text_details, ContextCompat.getColor(this, light));
                view.setTextColor(R.id.notification_base_text_remark, ContextCompat.getColor(this, light));
                break;
            default:
                int defaultColor = R.color.colorTextGrey;
                view.setTextColor(R.id.notification_base_text_title, ContextCompat.getColor(this, defaultColor));
                view.setTextColor(R.id.notification_base_text_details, ContextCompat.getColor(this, defaultColor));
                view.setTextColor(R.id.notification_base_text_remark, ContextCompat.getColor(this, defaultColor));
                break;
        }

        // big view
        RemoteViews viewBig = new RemoteViews(getPackageName(), R.layout.notification_big);
        // today
        imageId = JuheWeather.getWeatherIcon(weatherKind, isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_today, imageId[3]);
        viewBig.setTextViewText(R.id.notification_big_text_title,
                weatherNow.weatherInfo
                        + " "
                        + weatherNow.temperature + "℃");
        viewBig.setTextViewText(R.id.notification_big_text_details,
                weathers.get(0).info.night.get(2)
                        + "/"
                        + weathers.get(0).info.day.get(2)
                        + "°");
        viewBig.setTextViewText(R.id.notification_big_text_remark, text);
        // 1
        viewBig.setTextViewText(R.id.notification_big_text_week_1,
                getString(R.string.today));
        viewBig.setTextViewText(R.id.notification_big_text_temp_1,
                weathers.get(0).info.night.get(2)
                + "/"
                + weathers.get(0).info.day.get(2)
                + "°");
        imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weathers.get(0).info.day.get(1)), isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_1, imageId[3]);
        // 2
        viewBig.setTextViewText(R.id.notification_big_text_week_2,
                getString(R.string.week) + weathers.get(1).week);
        viewBig.setTextViewText(R.id.notification_big_text_temp_2,
                weathers.get(1).info.night.get(2)
                        + "/"
                        + weathers.get(1).info.day.get(2)
                        + "°");
        imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weathers.get(1).info.day.get(1)), isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_2, imageId[3]);
        // 3
        viewBig.setTextViewText(
                R.id.notification_big_text_week_3,
                getString(R.string.week) + weathers.get(2).week);
        viewBig.setTextViewText(
                R.id.notification_big_text_temp_3,
                weathers.get(2).info.night.get(2)
                        + "/"
                        + weathers.get(2).info.day.get(2)
                        + "°");
        imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weathers.get(2).info.day.get(1)), isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_3, imageId[3]);
        // 4
        viewBig.setTextViewText(
                R.id.notification_big_text_week_4,
                getString(R.string.week) + weathers.get(3).week);
        viewBig.setTextViewText(
                R.id.notification_big_text_temp_4,
                weathers.get(3).info.night.get(2)
                        + "/"
                        + weathers.get(3).info.day.get(2)
                        + "°");
        imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weathers.get(3).info.day.get(1)), isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_4, imageId[3]);
        // 5
        viewBig.setTextViewText(
                R.id.notification_big_text_week_5,
                getString(R.string.week) + weathers.get(4).week);
        viewBig.setTextViewText(
                R.id.notification_big_text_temp_5,
                weathers.get(4).info.night.get(2)
                        + "/"
                        + weathers.get(4).info.day.get(2)
                        + "°");
        imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weathers.get(4).info.day.get(1)), isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_5, imageId[3]);
        // text color
        switch (textColor) {
            case "dark":
                int dark = R.color.colorTextDark;
                viewBig.setTextColor(R.id.notification_big_text_title, ContextCompat.getColor(this, dark));
                dark = R.color.colorTextDark2nd;
                viewBig.setTextColor(R.id.notification_big_text_details, ContextCompat.getColor(this, dark));
                viewBig.setTextColor(R.id.notification_big_text_remark, ContextCompat.getColor(this, dark));

                viewBig.setTextColor(R.id.notification_big_text_week_1, ContextCompat.getColor(this, dark));
                viewBig.setTextColor(R.id.notification_big_text_week_2, ContextCompat.getColor(this, dark));
                viewBig.setTextColor(R.id.notification_big_text_week_3, ContextCompat.getColor(this, dark));
                viewBig.setTextColor(R.id.notification_big_text_week_4, ContextCompat.getColor(this, dark));
                viewBig.setTextColor(R.id.notification_big_text_week_5, ContextCompat.getColor(this, dark));

                viewBig.setTextColor(R.id.notification_big_text_temp_1, ContextCompat.getColor(this, dark));
                viewBig.setTextColor(R.id.notification_big_text_temp_2, ContextCompat.getColor(this, dark));
                viewBig.setTextColor(R.id.notification_big_text_temp_3, ContextCompat.getColor(this, dark));
                viewBig.setTextColor(R.id.notification_big_text_temp_4, ContextCompat.getColor(this, dark));
                viewBig.setTextColor(R.id.notification_big_text_temp_5, ContextCompat.getColor(this, dark));
                break;
            case "grey":
                int grey = R.color.colorTextGrey;
                viewBig.setTextColor(R.id.notification_big_text_title, ContextCompat.getColor(this, grey));
                grey = R.color.colorTextGrey2nd;
                viewBig.setTextColor(R.id.notification_big_text_details, ContextCompat.getColor(this, grey));
                viewBig.setTextColor(R.id.notification_big_text_remark, ContextCompat.getColor(this, grey));

                viewBig.setTextColor(R.id.notification_big_text_week_1, ContextCompat.getColor(this, grey));
                viewBig.setTextColor(R.id.notification_big_text_week_2, ContextCompat.getColor(this, grey));
                viewBig.setTextColor(R.id.notification_big_text_week_3, ContextCompat.getColor(this, grey));
                viewBig.setTextColor(R.id.notification_big_text_week_4, ContextCompat.getColor(this, grey));
                viewBig.setTextColor(R.id.notification_big_text_week_5, ContextCompat.getColor(this, grey));

                viewBig.setTextColor(R.id.notification_big_text_temp_1, ContextCompat.getColor(this, grey));
                viewBig.setTextColor(R.id.notification_big_text_temp_2, ContextCompat.getColor(this, grey));
                viewBig.setTextColor(R.id.notification_big_text_temp_3, ContextCompat.getColor(this, grey));
                viewBig.setTextColor(R.id.notification_big_text_temp_4, ContextCompat.getColor(this, grey));
                viewBig.setTextColor(R.id.notification_big_text_temp_5, ContextCompat.getColor(this, grey));
                break;
            case "light":
                int light = R.color.colorTextLight;
                viewBig.setTextColor(R.id.notification_big_text_title, ContextCompat.getColor(this, light));
                light = R.color.colorTextLight2nd;
                viewBig.setTextColor(R.id.notification_big_text_details, ContextCompat.getColor(this, light));
                viewBig.setTextColor(R.id.notification_big_text_remark, ContextCompat.getColor(this, light));

                viewBig.setTextColor(R.id.notification_big_text_week_1, ContextCompat.getColor(this, light));
                viewBig.setTextColor(R.id.notification_big_text_week_2, ContextCompat.getColor(this, light));
                viewBig.setTextColor(R.id.notification_big_text_week_3, ContextCompat.getColor(this, light));
                viewBig.setTextColor(R.id.notification_big_text_week_4, ContextCompat.getColor(this, light));
                viewBig.setTextColor(R.id.notification_big_text_week_5, ContextCompat.getColor(this, light));

                viewBig.setTextColor(R.id.notification_big_text_temp_1, ContextCompat.getColor(this, light));
                viewBig.setTextColor(R.id.notification_big_text_temp_2, ContextCompat.getColor(this, light));
                viewBig.setTextColor(R.id.notification_big_text_temp_3, ContextCompat.getColor(this, light));
                viewBig.setTextColor(R.id.notification_big_text_temp_4, ContextCompat.getColor(this, light));
                viewBig.setTextColor(R.id.notification_big_text_temp_5, ContextCompat.getColor(this, light));
                break;
            default:
                int defaultColor = R.color.colorTextGrey;
                viewBig.setTextColor(R.id.notification_big_text_title, ContextCompat.getColor(this, defaultColor));
                defaultColor = R.color.colorTextGrey2nd;
                viewBig.setTextColor(R.id.notification_big_text_details, ContextCompat.getColor(this, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_remark, ContextCompat.getColor(this, defaultColor));

                viewBig.setTextColor(R.id.notification_big_text_week_1, ContextCompat.getColor(this, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_week_2, ContextCompat.getColor(this, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_week_3, ContextCompat.getColor(this, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_week_4, ContextCompat.getColor(this, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_week_5, ContextCompat.getColor(this, defaultColor));

                viewBig.setTextColor(R.id.notification_big_text_temp_1, ContextCompat.getColor(this, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_temp_2, ContextCompat.getColor(this, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_temp_3, ContextCompat.getColor(this, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_temp_4, ContextCompat.getColor(this, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_temp_5, ContextCompat.getColor(this, defaultColor));
                break;
        }
        // loading big view
        Notification notification = builder.build();
        notification.bigContentView = viewBig;
        // sound and shock
        if(sharedPreferences.getBoolean(getString(R.string.key_notification_sound_switch), false)) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }
        if(sharedPreferences.getBoolean(getString(R.string.key_notification_shock_switch), false)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        // set clear flag
        if(sharedPreferences.getBoolean(getString(R.string.key_notification_can_clear_switch), false)) {
            // the notification can be cleared
            notification.flags = Notification.FLAG_AUTO_CANCEL;
        } else {
            // the notificaiton can not be cleared
            notification.flags = Notification.FLAG_ONGOING_EVENT;
        }
        // refresh notification
        notificationManager.notify(NotificationService.NOTIFICATION_ID, notification);
    }

    private void initDatabaseHelper() {
        this.databaseHelper = new MyDatabaseHelper(this,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                1);
    }

    private String readLocation() {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_LOCATION, null, null, null, null, null, null);
        String location;
        if(cursor.moveToFirst()) {
            location = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LOCATION));
        } else {
            location = getString(R.string.local);
        }
        if (location == null || location.equals("")) {
            location = getString(R.string.local);
        }
        cursor.close();
        database.close();
        return location;
    }

    private void initBaiduMap() {
        // initialize baidu location
        mLocationClient.registerLocationListener( myListener );//注册监听函数
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
                Log.d("百度定位", "定位至" + location.getCity());
                locationName = location.getCity();
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                Toast.makeText(NotificationService.this, "离线定位成功", Toast.LENGTH_SHORT).show();
                locationName = location.getCity();
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                Toast.makeText(NotificationService.this, "服务端网络定位失败", Toast.LENGTH_SHORT).show();
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                Toast.makeText(NotificationService.this, "网络连接失败，请检查网络手否连通", Toast.LENGTH_SHORT).show();
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                Toast.makeText(NotificationService.this, "无法获取信息，或因网络不通或缺失权限所致", Toast.LENGTH_SHORT).show();
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
