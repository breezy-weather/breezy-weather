package wangdaye.com.geometricweather.UserInterface;

import android.app.Activity;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import wangdaye.com.geometricweather.Data.HefengResult;
import wangdaye.com.geometricweather.Data.HefengWeather;
import wangdaye.com.geometricweather.Data.JuheResult;
import wangdaye.com.geometricweather.Data.JuheWeather;
import wangdaye.com.geometricweather.Data.Location;
import wangdaye.com.geometricweather.Data.MyDatabaseHelper;
import wangdaye.com.geometricweather.Data.WeatherInfoToShow;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Service.RefreshWidgetClockDay;
import wangdaye.com.geometricweather.Widget.HandlerContainer;
import wangdaye.com.geometricweather.Widget.SafeHandler;

/**
 * Create the widget [clock + day] on the launcher.
 * */

public class CreateWidgetClockDayActivity extends Activity
        implements HandlerContainer{
    // widget
    private ImageView imageViewCard;

    private TextClock clock;
    private TextView dateText;
    private TextView weatherText;

    //data
    private List<Location> locationList;

    private String locationName;
    private JuheResult juheResult;
    private HefengResult hefengResult;
    private boolean isDay;
    private boolean showCard = false;
    private boolean blackText = false;

    private MyDatabaseHelper databaseHelper;

    private final int REFRESH_DATA_SUCCEED = 1;
    private final int REFRESH_DATA_FAILED = 0;

    // baidu location
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    // handler
    private SafeHandler<CreateWidgetClockDayActivity> safeHandler;

    //TAG
//    private final String TAG = "CreateWidgetDayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_create_widget_clock_day);
    }

    @Override
    protected void onStart() {
        super.onStart();

        this.safeHandler = new SafeHandler<>(this);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (5 < hour && hour < 19) {
            isDay = true;
        } else {
            isDay = false;
        }

        this.locationName = getString(R.string.local);

        ImageView imageViewWall = (ImageView) this.findViewById(R.id.create_widget_clock_day_wall);
        imageViewWall.setImageDrawable(WallpaperManager.getInstance(CreateWidgetClockDayActivity.this).getDrawable());

        RelativeLayout relativeLayoutWidgetContainer = (RelativeLayout) this.findViewById(R.id.widget_clock_day) ;
        this.imageViewCard = (ImageView) relativeLayoutWidgetContainer.findViewById(R.id.widget_clock_day_card);

        this.clock = (TextClock) findViewById(R.id.widget_clock_day_clock);
        this.dateText = (TextView) findViewById(R.id.widget_clock_day_date);
        this.weatherText = (TextView) findViewById(R.id.widget_clock_day_weather);

        this.initDatabaseHelper();
        this.readLocation();
        if (locationList.size() < 1) {
            locationList.add(new Location(getString(R.string.local)));
        }
        String[] items = new String[locationList.size()];
        for (int i = 0; i < locationList.size(); i ++) {
            items[i] = this.locationList.get(i).location;
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_text, items);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_text);
        Spinner spinnerCity = (Spinner) this.findViewById(R.id.create_widget_clock_day_spinner);
        spinnerCity.setAdapter(spinnerAdapter);
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                locationName = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                locationName = getString(R.string.local);
            }
        });

        Switch switchCard = (Switch) this.findViewById(R.id.create_widget_clock_day_switch_card);
        switchCard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    imageViewCard.setVisibility(View.VISIBLE);
                    showCard = true;
                    clock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
                    dateText.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
                    weatherText.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
                } else {
                    imageViewCard.setVisibility(View.GONE);
                    showCard = false;
                    if (! blackText) {
                        clock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                        dateText.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                        weatherText.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                    }
                }
            }
        });

        Switch switchText = (Switch) this.findViewById(R.id.create_widget_clock_day_switch_text);
        switchText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    blackText = true;
                    clock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
                    dateText.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
                    weatherText.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextDark));
                } else {
                    blackText = false;
                    if (! showCard) {
                        clock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                        dateText.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                        weatherText.setTextColor(ContextCompat.getColor(CreateWidgetClockDayActivity.this, R.color.colorTextLight));
                    }
                }
            }
        });

        final Button buttonDone = (Button) this.findViewById(R.id.create_widget_clock_day_done);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_clock_day_setting),
                        MODE_PRIVATE
                ).edit();
                editor.putString(getString(R.string.key_location), locationName);
                editor.putBoolean(getString(R.string.key_show_card), showCard);
                editor.putBoolean(getString(R.string.key_black_text), blackText);
                editor.apply();

                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                int appWidgetId = 0;
                if (extras != null) {
                    appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);
                }

                buttonDone.setText(getString(R.string.first_refresh_widget));
                buttonDone.setEnabled(true);

                RefreshWidgetClockDay.refreshUIFromLocalData(CreateWidgetClockDayActivity.this, isDay);
                refreshWidget();

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }

    private void initDatabaseHelper() {
        this.databaseHelper = new MyDatabaseHelper(CreateWidgetClockDayActivity.this,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                2);
    }

    private void readLocation() {
        this.locationList = new ArrayList<>();
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_LOCATION,
                null, null, null, null, null, null);

        if(cursor.moveToFirst()) {
            do {
                String location = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LOCATION));
                this.locationList.add(new Location(location));
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
    }

    private void refreshWidget() {
        if(this.locationName.equals(getString(R.string.local))) {
            mLocationClient = new LocationClient(this); // 声明LocationClient类
            mLocationClient.registerLocationListener( myListener ); // 注册监听函数

            this.initBaiduMap();
        } else {
            getWeather(locationName);
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
        WeatherInfoToShow info = null;
        if (locationName.replaceAll(" ", "").matches("[a-zA-Z]+")) {
            if (hefengResult != null) {
                if (hefengResult.heWeather.get(0).status.equals("ok")) {
                    info = HefengWeather.getWeatherInfoToShow(this, hefengResult, isDay);
                }
            }
        } else {
            if (juheResult != null) {
                if (juheResult.error_code.equals("0")) {
                    info = JuheWeather.getWeatherInfoToShow(this, juheResult, isDay);
                }
            }
        }
        if(this.juheResult == null && this.hefengResult == null) {
            Toast.makeText(this, getString(R.string.refresh_widget_error), Toast.LENGTH_SHORT).show();
        } else {
            RefreshWidgetClockDay.refreshUIFromInternet(this, info, isDay);
        }
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

            if (locationName == null) {
                Toast.makeText(CreateWidgetClockDayActivity.this,
                        getString(R.string.get_location_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                getWeather(locationName);
            }

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
