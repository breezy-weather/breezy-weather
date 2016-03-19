package wangdaye.com.geometricweather.UI;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import wangdaye.com.geometricweather.Data.HefengWeather;
import wangdaye.com.geometricweather.Data.JuheWeather;
import wangdaye.com.geometricweather.Data.Location;
import wangdaye.com.geometricweather.Data.MyDatabaseHelper;
import wangdaye.com.geometricweather.Data.WeatherInfoToShow;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Service.NotificationService;
import wangdaye.com.geometricweather.Service.RefreshWidgetClockDay;
import wangdaye.com.geometricweather.Service.RefreshWidgetClockDayCenter;
import wangdaye.com.geometricweather.Service.RefreshWidgetClockDayWeek;
import wangdaye.com.geometricweather.Service.RefreshWidgetDay;
import wangdaye.com.geometricweather.Service.RefreshWidgetDayWeek;
import wangdaye.com.geometricweather.Service.RefreshWidgetWeek;
import wangdaye.com.geometricweather.Service.TimeService;
import wangdaye.com.geometricweather.Widget.HandlerContainer;
import wangdaye.com.geometricweather.Widget.SafeHandler;

/**
 * Main activity.
 * */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ManageDialog.SetLocationListener,
        HandlerContainer {
    // widget
    public static SafeHandler<MainActivity> safeHandler;

    public static FragmentManager fragmentManager;
    private WeatherFragment weatherFragment;

    private static FrameLayout navHead;
    private static FrameLayout backgroundPlate;
    public static Toolbar toolbar;

    // data
    public static boolean isDay;
    public static List<Location> locationList;
    public static Location lastLocation;
    private boolean started;

    public static boolean activityVisibility;

    private MyDatabaseHelper databaseHelper;

    private final int INTRODUCE_VERSION_CODE_NOW = 1;

    private final int LOCATION_PERMISSIONS_REQUEST_CODE = 1;
    private final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2;

    private final static int SETTINGS_ACTIVITY = 1;
    private final static int SHARE_ACTIVITY = 2;

    public static final int NOTIFICATION_ID = 7;

    private static final int REFRESH_TOTAL_DATA_SUCCEED = 1;
    private static final int REFRESH_HOURLY_DATA_SUCCEED = 2;
    private static final int REFRESH_TOTAL_DATA_FAILED = -1;
    private static final int REFRESH_HOURLY_DATA_FAILED = -2;

    // TAG
//    private static final String TAG = "MainActivity";

// life cycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.activityVisibility = true;

        this.setStatusBarTransParent();
        setContentView(R.layout.activity_main);

        safeHandler = new SafeHandler<>(this);
        this.initDatabaseHelper();
        this.initData();
        MainActivity.initNavigationBar(this, getWindow());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.key_timing_forecast_switch_today), false)
                || sharedPreferences.getBoolean(getString(R.string.key_timing_forecast_switch_tomorrow), false)) {
            Intent intent = new Intent(this, TimeService.class);
            startService(intent);
        }

        boolean watchedIntroduce = sharedPreferences.getBoolean(getString(R.string.key_watched_introduce), false);
        if (! watchedIntroduce) {
            this.requestPermission(LOCATION_PERMISSIONS_REQUEST_CODE);
        } else {
            int introduceVersionCode = sharedPreferences.getInt(getString(R.string.key_introduce_version_code), 0);
            if (introduceVersionCode < INTRODUCE_VERSION_CODE_NOW) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.key_introduce_version_code), INTRODUCE_VERSION_CODE_NOW);
                editor.apply();
                Intent intent = new Intent(this, IntroduceActivity.class);
                startActivity(intent);
            }
            createApp();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.activityVisibility = true;

        if (started) {
            this.weatherFragment.showCirclesView();
        }
        if (weatherFragment != null) {
            started = true;
        }
    }

    @Override
    protected void onStop() {
        MainActivity.activityVisibility = false;
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_ACTIVITY:
                initNavigationBar(this, getWindow());
                MainActivity.sendNotification(this, weatherFragment.location);
                break;
            case SHARE_ACTIVITY:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String oldUri = sharedPreferences.getString(getString(R.string.key_share_uri), "null");
                if (! oldUri.equals("null")) {
                    this.deleteSharePicture(Uri.parse(oldUri));
                }
                break;
        }
    }

// implement interface

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_collect) {
            for (int i = 0; i < MainActivity.locationList.size(); i ++) {
                if (lastLocation.location.equals(locationList.get(i).location)) {
                    WeatherFragment.isCollected = true;
                    Toast.makeText(this,
                            getString(R.string.collect_failed),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            MainActivity.locationList.add(lastLocation);
            this.writeLocation();
            WeatherFragment.isCollected = true;
            WeatherFragment.locationCollect.setImageResource(R.drawable.ic_collect_yes);
            Toast.makeText(this,
                    getString(R.string.collect_succeed),
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_share) {
            this.shareWeather();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    ManageDialog dialog = new ManageDialog();
                    dialog.show(getFragmentManager(), "ManageDialog");
                }
            };
            timer.schedule(timerTask, 400);
        } else if (id == R.id.nav_settings) {
            final Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    startActivityForResult(intent, SETTINGS_ACTIVITY);
                }
            };
            timer.schedule(timerTask, 400);
        } else if (id == R.id.nav_about) {
            final Intent intent = new Intent(MainActivity.this, AboutAppActivity.class);
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    startActivity(intent);
                }
            };
            timer.schedule(timerTask, 400);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

// request permission

    private void requestPermission(int permissionCode) {
        if (permissionCode == LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.INSTALL_LOCATION_PROVIDER)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSIONS_REQUEST_CODE);
                }
            } else {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.key_watched_introduce), true);
                editor.putInt(getString(R.string.key_introduce_version_code), INTRODUCE_VERSION_CODE_NOW);
                editor.apply();
                Intent intent = new Intent(this, IntroduceActivity.class);
                startActivity(intent);
                this.createApp();
            }
        } else if (permissionCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResult) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.key_watched_introduce), true);
                editor.putInt(getString(R.string.key_introduce_version_code), INTRODUCE_VERSION_CODE_NOW);
                editor.apply();
                Intent intent = new Intent(this, IntroduceActivity.class);
                startActivity(intent);
                this.createApp();
                break;
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                Toast.makeText(this,
                        getString(R.string.request_competence_succeed),
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permission, grantResult);
                break;
        }
    }

// fragment

    private static void changeFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

// initialize widget

    protected void createApp() {
        this.initWidget();
        fragmentManager = getFragmentManager();

        if (locationList.size() < 1) {
            locationList.add(new Location(getString(R.string.local)));
        }

        lastLocation = locationList.get(0);
        weatherFragment = new WeatherFragment();
        changeFragment(weatherFragment);
        setNavHead();
    }

    private void setStatusBarTransParent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static void initNavigationBar(Context context, Window window) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean navigationBarColorSwitch = sharedPreferences.getBoolean(
                context.getString(R.string.key_navigation_bar_color_switch), false);
        if(navigationBarColorSwitch && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (MainActivity.isDay) {
                window.setNavigationBarColor(ContextCompat.getColor(context, R.color.lightPrimary_3));
            } else {
                window.setNavigationBarColor(ContextCompat.getColor(context, R.color.darkPrimary_5));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setNavigationBarColor(ContextCompat.getColor(context, android.R.color.black));
            }
        }
    }

    private void initWidget() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navHeader = navigationView.getHeaderView(0);
        MainActivity.navHead = (FrameLayout) navHeader.findViewById(R.id.nav_header);

        backgroundPlate = (FrameLayout) findViewById(R.id.background_plate);
        setBackgroundPlateColor(this, true);
    }

    private void initData() {
        this.readLocation();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        MainActivity.isDay = sharedPreferences.getBoolean(getString(R.string.key_isDay), true);

        started = false;
        lastLocation = null;
    }

    public static void setNavHead() {
        if (isDay) {
            navHead.setBackgroundResource(R.drawable.nav_head_day);
        } else {
            navHead.setBackgroundResource(R.drawable.nav_head_night);
        }
    }

    public static void setBackgroundPlateColor(Context context, boolean isInit) {
        if (isInit) {
            Class<?> c;
            Object obj;
            Field field;
            int x, statusBarHeight = 0;
            try {
                c = Class.forName("com.android.internal.R$dimen");
                obj = c.newInstance();
                field = c.getField("status_bar_height");
                x = Integer.parseInt(field.get(obj).toString());
                statusBarHeight = context.getResources().getDimensionPixelSize(x);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            backgroundPlate.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            statusBarHeight
                    )
            );
        }
        if (isDay) {
            backgroundPlate.setBackgroundColor(ContextCompat.getColor(context, R.color.lightPrimary_5));
        } else {
            backgroundPlate.setBackgroundColor(ContextCompat.getColor(context, R.color.darkPrimary_5));
        }
    }

// refresh data

    public static void getTotalData(final String searchLocation, final boolean isLocation) {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                if (isLocation) {
                    lastLocation = new Location("本地");
                } else {
                    lastLocation = new Location(searchLocation);
                }
                if (searchLocation.replaceAll(" ", "").matches("[a-zA-Z]+")) {
                    lastLocation.hefengResult = HefengWeather.requestInternationalData(searchLocation);
                    Message message = new Message();
                    if (lastLocation.hefengResult == null) {
                        message.what = REFRESH_TOTAL_DATA_FAILED;
                    } else if (! lastLocation.hefengResult.heWeather.get(0).status.equals("ok")) {
                        message.what = REFRESH_TOTAL_DATA_FAILED;
                    } else {
                        message.what = REFRESH_TOTAL_DATA_SUCCEED;
                    }
                    safeHandler.sendMessage(message);
                } else {
                    lastLocation.juheResult = JuheWeather.getRequest(searchLocation);
                    Message message = new Message();
                    if (lastLocation.juheResult == null) {
                        message.what = REFRESH_TOTAL_DATA_FAILED;
                    } else if (! lastLocation.juheResult.error_code.equals("0")) {
                        message.what = REFRESH_TOTAL_DATA_FAILED;
                    } else {
                        message.what = REFRESH_TOTAL_DATA_SUCCEED;
                    }
                    safeHandler.sendMessage(message);
                }
            }
        });
        thread.start();
    }

    public static void getHourlyData(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                if (lastLocation.location.replaceAll(" ", "").matches("[a-zA-Z]+") && lastLocation.hefengResult == null) {
                    message.what = REFRESH_HOURLY_DATA_FAILED;
                } else if (lastLocation.location.replaceAll(" ", "").matches("[a-zA-Z]+") && ! lastLocation.hefengResult.heWeather.get(0).status.equals("ok")) {
                    message.what = REFRESH_HOURLY_DATA_FAILED;
                } else if (lastLocation.location.replaceAll(" ", "").matches("[a-zA-Z]+")) {
                    message.what = REFRESH_HOURLY_DATA_SUCCEED;
                } else if (lastLocation.juheResult == null) {
                    message.what = REFRESH_HOURLY_DATA_FAILED;
                } else if (! lastLocation.juheResult.error_code.equals("0")) {
                    message.what = REFRESH_HOURLY_DATA_FAILED;
                } else {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean useEnglish = sharedPreferences.getBoolean(context.getString(R.string.key_get_hourly_data_by_eng), false);
                    lastLocation.hefengResult = HefengWeather.requestHourlyData(lastLocation.juheResult.result.data.realtime.city_name, useEnglish);
                    if (lastLocation.hefengResult == null) {
                        message.what = REFRESH_HOURLY_DATA_FAILED;
                    } else if (! lastLocation.hefengResult.heWeather.get(0).status.equals("ok")) {
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

    public static boolean needChangeTime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (5 < hour && hour < 19 && ! MainActivity.isDay) {
            return true;
        } else if ((hour < 6 || hour > 18) && MainActivity.isDay) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSetLocation(String location, boolean isSearch) {
        if (location.equals(getString(R.string.search_null))) {
            Toast.makeText(this,
                    getString(R.string.search_null),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (isSearch) {
            lastLocation = new Location(location);
            this.weatherFragment.setLocation();
            this.weatherFragment.refreshAll();
        } else {
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).location.equals(location)) {
                    lastLocation = locationList.get(i);
                    this.weatherFragment.setLocation();
                    this.weatherFragment.refreshAll();
                    return;
                }
            }
        }
    }

// database

    private void initDatabaseHelper() {
        this.databaseHelper = new MyDatabaseHelper(MainActivity.this,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                MyDatabaseHelper.VERSION_CODE);
    }

    private void readLocation() {
        MainActivity.locationList = new ArrayList<>();
        MainActivity.locationList.clear();
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_LOCATION, null, null, null, null, null, null);

        if(cursor.moveToFirst()) {
            do {
                String location = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LOCATION));
                locationList.add(new Location(location));
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
    }

    private void writeLocation() {
        SQLiteDatabase database = this.databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_LOCATION, lastLocation.location);
        database.insert(MyDatabaseHelper.TABLE_LOCATION, null, values);
        values.clear();
        database.close();
    }

// notification

    public static void sendNotification(Context context, Location location) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences.getBoolean(context.getString(R.string.key_notification_switch), false)) {
            if (location.location.replaceAll(" ", "").matches("[a-zA-Z]+")) {
                NotificationService.refreshNotification(context, HefengWeather.getWeatherInfoToShow(context, location.hefengResult, isDay), false);
            } else {
                NotificationService.refreshNotification(context, JuheWeather.getWeatherInfoToShow(context, location.juheResult, isDay), false);
            }
        }
    }

// widget

    public static void refreshWidget(Context context, Location location, WeatherInfoToShow info, boolean isDay) {
        SharedPreferences sharedPreferences;
        String locationName;

        // day
        sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_day_setting), Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(context.getString(R.string.key_location), context.getString(R.string.local));
        if (location.location.equals(locationName)) {
            RefreshWidgetDay.refreshUIFromInternet(context, info, isDay);
        }

        // week
        sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_week_setting), Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(context.getString(R.string.key_location), context.getString(R.string.local));
        if (location.location.equals(locationName)) {
            RefreshWidgetWeek.refreshUIFromInternet(context, info, isDay);
        }

        // day + week
        sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_day_week_setting), Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(context.getString(R.string.key_location), context.getString(R.string.local));
        if (location.location.equals(locationName)) {
            RefreshWidgetDayWeek.refreshUIFromInternet(context, info, isDay);
        }

        // clock + day
        sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_setting), Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(context.getString(R.string.key_location), context.getString(R.string.local));
        if (location.location.equals(locationName)) {
            RefreshWidgetClockDay.refreshUIFromInternet(context, info, isDay);
        }

        // clock + day (center)
        sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_center_setting), Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(context.getString(R.string.key_location), context.getString(R.string.local));
        if (location.location.equals(locationName)) {
            RefreshWidgetClockDayCenter.refreshUIFromInternet(context, info, isDay);
        }

        // clock + day + week
        sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_week_setting), Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(context.getString(R.string.key_location), context.getString(R.string.local));
        if (location.location.equals(locationName)) {
            RefreshWidgetClockDayWeek.refreshUIFromInternet(context, info, isDay);
        }
    }

// share

    private void shareWeather() {
        if (weatherFragment.info == null) {
            Toast.makeText(this,
                    getString(R.string.share_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.share_image, null);

        ImageView background = (ImageView) view.findViewById(R.id.share_image_pic);
        ImageView icon = (ImageView) view.findViewById(R.id.share_image_icon);
        TextView location = (TextView) view.findViewById(R.id.share_image_location);
        TextView weather = (TextView) view.findViewById(R.id.share_image_weather);
        TextView temp = (TextView) view.findViewById(R.id.share_image_temp);
        TextView wind = (TextView) view.findViewById(R.id.share_image_wind);
        TextView air = (TextView) view.findViewById(R.id.share_image_air);
        TextView[] weekWeek = new TextView[] {
                (TextView) view.findViewById(R.id.share_image_week_1),
                (TextView) view.findViewById(R.id.share_image_week_2),
                (TextView) view.findViewById(R.id.share_image_week_3)
        };
        ImageView[] iconWeek = new ImageView[] {
                (ImageView) view.findViewById(R.id.share_image_weather_1),
                (ImageView) view.findViewById(R.id.share_image_weather_2),
                (ImageView) view.findViewById(R.id.share_image_weather_3)
        };
        TextView[] tempWeek = new TextView[] {
                (TextView) view.findViewById(R.id.share_image_temp_1),
                (TextView) view.findViewById(R.id.share_image_temp_2),
                (TextView) view.findViewById(R.id.share_image_temp_3)
        };

        if (isDay) {
            background.setImageResource(R.drawable.share_background_day);
        } else {
            background.setImageResource(R.drawable.share_background_night );
        }

        int[][] imageId = new int[][] {
                JuheWeather.getWeatherIcon(weatherFragment.info.weatherKindNow, isDay),
                JuheWeather.getWeatherIcon(weatherFragment.info.weatherKind[0], isDay),
                JuheWeather.getWeatherIcon(weatherFragment.info.weatherKind[1], isDay),
                JuheWeather.getWeatherIcon(weatherFragment.info.weatherKind[2], isDay)
        };
        for (int i = 0; i < 4; i ++) {
            if (imageId[i][3] != 0) {
                if (i == 0) {
                    icon.setImageResource(imageId[i][3]);
                } else {
                    iconWeek[i - 1].setImageResource(imageId[i][3]);
                }
            }
        }

        String text;
        location.setText(weatherFragment.info.location);
        text = weatherFragment.info.weatherNow + " " + weatherFragment.info.tempNow + "℃";
        weather.setText(text);
        text = "气温: " +  weatherFragment.info.miniTemp[0] + "/" + weatherFragment.info.maxiTemp[0] + "°";
        temp.setText(text);
        text = weatherFragment.info.windDir[0] + weatherFragment.info.windLevel[0];
        wind.setText(text);
        text = "污染: " + weatherFragment.info.pmInfo;
        air.setText(text);
        for (int i = 0; i < 3; i ++) {
            weekWeek[i].setText(weatherFragment.info.week[i + 1]);
            text = weatherFragment.info.miniTemp[i + 1] + "/" + weatherFragment.info.maxiTemp[i + 1] + "°";
            tempWeek[i].setText(text);
        }

        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(256, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(256, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String oldUri = sharedPreferences.getString(getString(R.string.key_share_uri), "null");
        if (! oldUri.equals("null")) {
            this.deleteSharePicture(Uri.parse(oldUri));
        }

        boolean shared = sharedPreferences.getBoolean(getString(R.string.key_shared), false);
        if (! shared && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.key_shared), true);
            editor.apply();
            this.requestPermission(WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.key_share_uri), uri.toString());
            editor.apply();
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(shareIntent, getResources().getText(R.string.action_share)), SHARE_ACTIVITY);
        }
    }

    private void deleteSharePicture(Uri uri) {
        if (uri != null) {
            getContentResolver().delete(uri, null, null);
        }
    }

// handler

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case REFRESH_TOTAL_DATA_SUCCEED:
                weatherFragment.setLocation();
                weatherFragment.refreshTotalDataSucceed();
                break;
            case REFRESH_TOTAL_DATA_FAILED:
                weatherFragment.setLocation();
                weatherFragment.refreshTotalDataFailed();
                break;
            case REFRESH_HOURLY_DATA_SUCCEED:
                weatherFragment.setLocation();
                weatherFragment.refreshHourlyDataSucceed();
                break;
            case REFRESH_HOURLY_DATA_FAILED:
                weatherFragment.setLocation();
                weatherFragment.refreshHourlyDataFailed();
                break;
        }
    }
}
