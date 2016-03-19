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

import wangdaye.com.geometricweather.Data.HefengResult;
import wangdaye.com.geometricweather.Data.HefengWeather;
import wangdaye.com.geometricweather.Data.WeatherInfoToShow;
import wangdaye.com.geometricweather.UI.MainActivity;
import wangdaye.com.geometricweather.Data.JuheResult;
import wangdaye.com.geometricweather.Data.JuheWeather;
import wangdaye.com.geometricweather.Data.MyDatabaseHelper;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Receiver.NotificationReceiver;
import wangdaye.com.geometricweather.Widget.HandlerContainer;
import wangdaye.com.geometricweather.Widget.SafeHandler;

/**
 * Send notification.
 * */

public class NotificationService extends Service implements HandlerContainer {
    // data
    private MyDatabaseHelper databaseHelper;

    private String location;
    private JuheResult juheResult;
    private HefengResult hefengResult;

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
        location = this.readLocation();
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
                if (searchLocation.replaceAll(" ", "").matches("[a-zA-Z]+")) {
                    hefengResult = HefengWeather.requestInternationalData(searchLocation);
                } else {
                    juheResult = JuheWeather.getRequest(searchLocation);
                }

                Message message=new Message();
                if (searchLocation.replaceAll(" ", "").matches("[a-zA-Z]+") && hefengResult == null) {
                    message.what = REFRESH_DATA_FAILED;
                } else if (searchLocation.replaceAll(" ", "").matches("[a-zA-Z]+") && ! hefengResult.heWeather.get(0).status.equals("ok")) {
                    message.what = REFRESH_DATA_FAILED;
                } else if (! searchLocation.replaceAll(" ", "").matches("[a-zA-Z]+") && juheResult == null) {
                    message.what = REFRESH_DATA_FAILED;
                } else if (! searchLocation.replaceAll(" ", "").matches("[a-zA-Z]+") && ! juheResult.error_code.equals("0")) {
                    message.what = REFRESH_DATA_FAILED;
                } else {
                    message.what = REFRESH_DATA_SUCCEED;
                }
                safeHandler.sendMessage(message);
            }
        });
        thread.start();
    }

    public static void refreshNotification(Context context, WeatherInfoToShow info, boolean isService) {
        // refresh the notification UI
        boolean isDay;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        isDay = 5 < hour && hour < 19;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // set level
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (sharedPreferences.getBoolean(context.getString(R.string.key_hide_notification_in_lockScreen), false)) {
                builder.setVisibility(Notification.VISIBILITY_SECRET);
            } else {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
        }
        boolean backgroundColor = sharedPreferences.getBoolean(context.getString(R.string.key_notification_background_color_switch), false);

        // small view
        builder.setSmallIcon(JuheWeather.getMiniWeatherIcon(info.weatherKindNow, isDay));
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.notification_base);
        int[] imageId = JuheWeather.getWeatherIcon(info.weatherKindNow, isDay);
        view.setImageViewResource(R.id.notification_base_image_today, imageId[3]);
        view.setTextViewText(R.id.notification_base_text_title, info.weatherNow + " " + info.tempNow + "℃");
        view.setTextViewText(R.id.notification_base_text_details, info.miniTemp[0] + "/" + info.maxiTemp[0] + "°");
        view.setTextViewText(R.id.notification_base_text_remark, info.location + "." + info.refreshTime);
        if (backgroundColor) {
            view.setViewVisibility(R.id.notification_background_base, View.VISIBLE);
        } else {
            view.setViewVisibility(R.id.notification_background_base, View.GONE);
        }
        builder.setContent(view);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        // text color
        String textColor = sharedPreferences.getString(context.getString(R.string.key_notification_text_color),
                context.getString(R.string.notification_text_color_default));
        switch (textColor) {
            case "dark":
                int dark = R.color.colorTextDark;
                view.setTextColor(R.id.notification_base_text_title, ContextCompat.getColor(context, dark));
                dark = R.color.colorTextDark2nd;
                view.setTextColor(R.id.notification_base_text_details, ContextCompat.getColor(context, dark));
                view.setTextColor(R.id.notification_base_text_remark, ContextCompat.getColor(context, dark));
                break;
            case "grey":
                int grey = R.color.colorTextGrey;
                view.setTextColor(R.id.notification_base_text_title, ContextCompat.getColor(context, grey));
                grey = R.color.colorTextGrey2nd;
                view.setTextColor(R.id.notification_base_text_details, ContextCompat.getColor(context, grey));
                view.setTextColor(R.id.notification_base_text_remark, ContextCompat.getColor(context, grey));
                break;
            case "light":
                int light = R.color.colorTextLight;
                view.setTextColor(R.id.notification_base_text_title, ContextCompat.getColor(context, light));
                light = R.color.colorTextLight2nd;
                view.setTextColor(R.id.notification_base_text_details, ContextCompat.getColor(context, light));
                view.setTextColor(R.id.notification_base_text_remark, ContextCompat.getColor(context, light));
                break;
            default:
                int defaultColor = R.color.colorTextGrey;
                view.setTextColor(R.id.notification_base_text_title, ContextCompat.getColor(context, defaultColor));
                view.setTextColor(R.id.notification_base_text_details, ContextCompat.getColor(context, defaultColor));
                view.setTextColor(R.id.notification_base_text_remark, ContextCompat.getColor(context, defaultColor));
                break;
        }

        // big view
        RemoteViews viewBig = new RemoteViews(context.getPackageName(), R.layout.notification_big);
        // today
        imageId = JuheWeather.getWeatherIcon(info.weatherKindNow, isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_today, imageId[3]);
        viewBig.setTextViewText(R.id.notification_big_text_title, info.weatherNow + " " + info.tempNow + "℃");
        viewBig.setTextViewText(R.id.notification_big_text_details, info.miniTemp[0] + "/" + info.maxiTemp[0] + "°");
        viewBig.setTextViewText(R.id.notification_big_text_remark, info.location + "." + info.refreshTime);
        // 1
        viewBig.setTextViewText(R.id.notification_big_text_week_1, context.getString(R.string.today));
        viewBig.setTextViewText(R.id.notification_big_text_temp_1, info.miniTemp[0] + "/" + info.maxiTemp[0] + "°");
        imageId = JuheWeather.getWeatherIcon(info.weatherKind[0], isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_1, imageId[3]);
        // 2
        viewBig.setTextViewText(R.id.notification_big_text_week_2, info.week[1]);
        viewBig.setTextViewText(R.id.notification_big_text_temp_2, info.miniTemp[1] + "/" + info.maxiTemp[1] + "°");
        imageId = JuheWeather.getWeatherIcon(info.weatherKind[1], isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_2, imageId[3]);
        // 3
        viewBig.setTextViewText(R.id.notification_big_text_week_3, info.week[2]);
        viewBig.setTextViewText(R.id.notification_big_text_temp_3, info.miniTemp[2] + "/" + info.maxiTemp[2] + "°");
        imageId = JuheWeather.getWeatherIcon(info.weatherKind[2], isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_3, imageId[3]);
        // 4
        viewBig.setTextViewText(R.id.notification_big_text_week_4, info.week[3]);
        viewBig.setTextViewText(R.id.notification_big_text_temp_4, info.miniTemp[3] + "/" + info.maxiTemp[3] + "°");
        imageId = JuheWeather.getWeatherIcon(info.weatherKind[3], isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_4, imageId[3]);
        // 5
        viewBig.setTextViewText(R.id.notification_big_text_week_5, info.week[4]);
        viewBig.setTextViewText(R.id.notification_big_text_temp_5, info.miniTemp[4] + "/" + info.maxiTemp[4] + "°");
        imageId = JuheWeather.getWeatherIcon(info.weatherKind[4], isDay);
        viewBig.setImageViewResource(R.id.notification_big_image_5, imageId[3]);
        // text color
        switch (textColor) {
            case "dark":
                int dark = R.color.colorTextDark;
                viewBig.setTextColor(R.id.notification_big_text_title, ContextCompat.getColor(context, dark));
                dark = R.color.colorTextDark2nd;
                viewBig.setTextColor(R.id.notification_big_text_details, ContextCompat.getColor(context, dark));
                viewBig.setTextColor(R.id.notification_big_text_remark, ContextCompat.getColor(context, dark));

                viewBig.setTextColor(R.id.notification_big_text_week_1, ContextCompat.getColor(context, dark));
                viewBig.setTextColor(R.id.notification_big_text_week_2, ContextCompat.getColor(context, dark));
                viewBig.setTextColor(R.id.notification_big_text_week_3, ContextCompat.getColor(context, dark));
                viewBig.setTextColor(R.id.notification_big_text_week_4, ContextCompat.getColor(context, dark));
                viewBig.setTextColor(R.id.notification_big_text_week_5, ContextCompat.getColor(context, dark));

                viewBig.setTextColor(R.id.notification_big_text_temp_1, ContextCompat.getColor(context, dark));
                viewBig.setTextColor(R.id.notification_big_text_temp_2, ContextCompat.getColor(context, dark));
                viewBig.setTextColor(R.id.notification_big_text_temp_3, ContextCompat.getColor(context, dark));
                viewBig.setTextColor(R.id.notification_big_text_temp_4, ContextCompat.getColor(context, dark));
                viewBig.setTextColor(R.id.notification_big_text_temp_5, ContextCompat.getColor(context, dark));
                break;
            case "grey":
                int grey = R.color.colorTextGrey;
                viewBig.setTextColor(R.id.notification_big_text_title, ContextCompat.getColor(context, grey));
                grey = R.color.colorTextGrey2nd;
                viewBig.setTextColor(R.id.notification_big_text_details, ContextCompat.getColor(context, grey));
                viewBig.setTextColor(R.id.notification_big_text_remark, ContextCompat.getColor(context, grey));

                viewBig.setTextColor(R.id.notification_big_text_week_1, ContextCompat.getColor(context, grey));
                viewBig.setTextColor(R.id.notification_big_text_week_2, ContextCompat.getColor(context, grey));
                viewBig.setTextColor(R.id.notification_big_text_week_3, ContextCompat.getColor(context, grey));
                viewBig.setTextColor(R.id.notification_big_text_week_4, ContextCompat.getColor(context, grey));
                viewBig.setTextColor(R.id.notification_big_text_week_5, ContextCompat.getColor(context, grey));

                viewBig.setTextColor(R.id.notification_big_text_temp_1, ContextCompat.getColor(context, grey));
                viewBig.setTextColor(R.id.notification_big_text_temp_2, ContextCompat.getColor(context, grey));
                viewBig.setTextColor(R.id.notification_big_text_temp_3, ContextCompat.getColor(context, grey));
                viewBig.setTextColor(R.id.notification_big_text_temp_4, ContextCompat.getColor(context, grey));
                viewBig.setTextColor(R.id.notification_big_text_temp_5, ContextCompat.getColor(context, grey));
                break;
            case "light":
                int light = R.color.colorTextLight;
                viewBig.setTextColor(R.id.notification_big_text_title, ContextCompat.getColor(context, light));
                light = R.color.colorTextLight2nd;
                viewBig.setTextColor(R.id.notification_big_text_details, ContextCompat.getColor(context, light));
                viewBig.setTextColor(R.id.notification_big_text_remark, ContextCompat.getColor(context, light));

                viewBig.setTextColor(R.id.notification_big_text_week_1, ContextCompat.getColor(context, light));
                viewBig.setTextColor(R.id.notification_big_text_week_2, ContextCompat.getColor(context, light));
                viewBig.setTextColor(R.id.notification_big_text_week_3, ContextCompat.getColor(context, light));
                viewBig.setTextColor(R.id.notification_big_text_week_4, ContextCompat.getColor(context, light));
                viewBig.setTextColor(R.id.notification_big_text_week_5, ContextCompat.getColor(context, light));

                viewBig.setTextColor(R.id.notification_big_text_temp_1, ContextCompat.getColor(context, light));
                viewBig.setTextColor(R.id.notification_big_text_temp_2, ContextCompat.getColor(context, light));
                viewBig.setTextColor(R.id.notification_big_text_temp_3, ContextCompat.getColor(context, light));
                viewBig.setTextColor(R.id.notification_big_text_temp_4, ContextCompat.getColor(context, light));
                viewBig.setTextColor(R.id.notification_big_text_temp_5, ContextCompat.getColor(context, light));
                break;
            default:
                int defaultColor = R.color.colorTextGrey;
                viewBig.setTextColor(R.id.notification_big_text_title, ContextCompat.getColor(context, defaultColor));
                defaultColor = R.color.colorTextGrey2nd;
                viewBig.setTextColor(R.id.notification_big_text_details, ContextCompat.getColor(context, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_remark, ContextCompat.getColor(context, defaultColor));

                viewBig.setTextColor(R.id.notification_big_text_week_1, ContextCompat.getColor(context, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_week_2, ContextCompat.getColor(context, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_week_3, ContextCompat.getColor(context, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_week_4, ContextCompat.getColor(context, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_week_5, ContextCompat.getColor(context, defaultColor));

                viewBig.setTextColor(R.id.notification_big_text_temp_1, ContextCompat.getColor(context, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_temp_2, ContextCompat.getColor(context, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_temp_3, ContextCompat.getColor(context, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_temp_4, ContextCompat.getColor(context, defaultColor));
                viewBig.setTextColor(R.id.notification_big_text_temp_5, ContextCompat.getColor(context, defaultColor));
                break;
        }
        if (backgroundColor) {
            viewBig.setViewVisibility(R.id.notification_background_big, View.VISIBLE);
        } else {
            viewBig.setViewVisibility(R.id.notification_background_big, View.GONE);
        }
        // loading big view
        Notification notification = builder.build();
        notification.bigContentView = viewBig;
        // sound and shock
        if (isService) {
            if(sharedPreferences.getBoolean(context.getString(R.string.key_notification_sound_switch), false)) {
                notification.defaults |= Notification.DEFAULT_SOUND;
            }
            if(sharedPreferences.getBoolean(context.getString(R.string.key_notification_shock_switch), false)) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }
        }
        // set clear flag
        if(sharedPreferences.getBoolean(context.getString(R.string.key_notification_can_clear_switch), false)) {
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
                MyDatabaseHelper.VERSION_CODE);
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

            StringBuilder sb = new StringBuilder(256);
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
                    sb.append(p.getId()).
                            append(" ").
                            append(p.getName()).
                            append(" ").
                            append(p.getRank());
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
                boolean isDay;
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                isDay = 5 < hour && hour < 19;

                if (location != null) {
                    if (location.replaceAll(" ", "").matches("[a-zA-Z]+")) {
                        WeatherInfoToShow info = HefengWeather.getWeatherInfoToShow(NotificationService.this, hefengResult, isDay);
                        refreshNotification(NotificationService.this, info, true);
                    } else {
                        WeatherInfoToShow info = JuheWeather.getWeatherInfoToShow(NotificationService.this, juheResult, isDay);
                        refreshNotification(NotificationService.this, info, true);
                    }
                }
                break;
            default:
                Toast.makeText(this,
                        getString(R.string.refresh_widget_error),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
