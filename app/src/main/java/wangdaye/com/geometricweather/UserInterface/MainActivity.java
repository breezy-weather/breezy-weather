package wangdaye.com.geometricweather.UserInterface;

import android.annotation.SuppressLint;
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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import wangdaye.com.geometricweather.Data.HefengWeather;
import wangdaye.com.geometricweather.Data.JuheWeather;
import wangdaye.com.geometricweather.Data.Location;
import wangdaye.com.geometricweather.Data.MyDatabaseHelper;
import wangdaye.com.geometricweather.Data.Weather;
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

/**
 * Main activity.
 * */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ManageDialog.SetLocationListener {
    // widget
    public static FragmentManager fragmentManager;
    private WeatherFragment weatherFragment;
    private LiteWeatherFragment liteWeatherFragment;

    private static FrameLayout navHead;
    private static FrameLayout backgroundPlate;

    // data
    private boolean animatorSwitch;

    private MyDatabaseHelper databaseHelper;

    public static boolean isDay;
    public static List<Location> locationList;
    public static Location lastLocation;
    private boolean started;

    private final static int LOCATION_PERMISSIONS_REQUEST_CODE = 1;
    private final static int SETTINGS_ACTIVITY = 1;
    public static final int NOTIFICATION_ID = 7;

    // TAG
//    private static final String TAG = "MainActivity";

// life cycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setStatusBarTransParent();
        setContentView(R.layout.activity_main);

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
            this.requestPermission();
        } else {
            createApp();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (started && animatorSwitch) {
            this.weatherFragment.showCirclesView();
        } else if (started) {
            this.liteWeatherFragment.showCirclesView();
        }

        if (weatherFragment != null || liteWeatherFragment != null) {
            started = true;
        }
    }

    @Override
    protected void onStop() {
        if (started && animatorSwitch) {
            this.weatherFragment.animatorCancel();
        } else if (started) {
            this.liteWeatherFragment.animatorCancel();
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_ACTIVITY:
                initNavigationBar(this, getWindow());

                if (animatorSwitch) {
                    MainActivity.sendNotification(this, weatherFragment.location);
                } else {
                    MainActivity.sendNotification(this, liteWeatherFragment.location);
                } break;
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
            if (animatorSwitch) {
                WeatherFragment.isCollected = true;
                WeatherFragment.locationCollect.setImageResource(R.drawable.ic_collect_yes);
            } else {
                LiteWeatherFragment.isCollected = true;
                LiteWeatherFragment.locationCollect.setImageResource(R.drawable.ic_collect_yes);
            }
            Toast.makeText(this,
                    getString(R.string.collect_succeed),
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_manage) {
            ManageDialog dialog = new ManageDialog();
            dialog.show(getFragmentManager(), "ManageDialog");
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_ACTIVITY);
            return true;
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(MainActivity.this, AboutAppActivity.class);
            startActivity(intent);
            return true;
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

    private void requestPermission() {
        // request competence
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
            editor.apply();
            Intent intent = new Intent(this, IntroduceActivity.class);
            startActivity(intent);
            this.createApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResult) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.key_watched_introduce), true);
                editor.apply();
                Intent intent = new Intent(this, IntroduceActivity.class);
                startActivity(intent);
                this.createApp();
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

        if (animatorSwitch) {
            lastLocation = locationList.get(0);
            weatherFragment = new WeatherFragment();
            changeFragment(weatherFragment);
        } else {
            lastLocation = locationList.get(0);
            liteWeatherFragment = new LiteWeatherFragment();
            changeFragment(liteWeatherFragment);
        }

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        this.animatorSwitch = sharedPreferences.getBoolean(getString(R.string.key_more_animator_switch), false);

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
            if (animatorSwitch) {
                lastLocation = new Location(location);
                this.weatherFragment.setLocation();
                this.weatherFragment.refreshAll();
            } else {
                lastLocation = new Location(location);
                this.liteWeatherFragment.setLocation();
                this.liteWeatherFragment.refreshAll();
            }
        } else if (animatorSwitch) {
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).location.equals(location)) {
                    lastLocation = locationList.get(i);
                    this.weatherFragment.setLocation();
                    this.weatherFragment.refreshAll();
                    return;
                }
            }
        } else {
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).location.equals(location)) {
                    lastLocation = locationList.get(i);
                    this.liteWeatherFragment.setLocation();
                    this.liteWeatherFragment.refreshAll();
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
                2);
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

    @SuppressLint("SimpleDateFormat")
    public static void writeTodayWeather(Context context, WeatherInfoToShow info) {
        // get yesterday date.
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date date = cal.getTime();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String yesterdayDate = simpleDateFormat.format(date);

        Weather yesterdayWeather = null;
        boolean haveYesterdayData = false;

        // init database.
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(context,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                2);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        // read yesterday weather.
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_WEATHER,
                null,
                MyDatabaseHelper.COLUMN_LOCATION + " = '" + info.location
                        + "' AND "
                        + MyDatabaseHelper.COLUMN_TIME + " = '" + yesterdayDate + "'",
                null,
                null,
                null,
                null);
        if(cursor.moveToFirst()) {
            do {
                String locationText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LOCATION));
                String weatherText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER));
                String tempText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TEMP));
                String timeText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TIME));
                yesterdayWeather = new Weather(locationText, weatherText, tempText, timeText);
                haveYesterdayData = true;
            } while (cursor.moveToNext());
        }
        cursor.close();

        // delete all weather data from param location.
        database.delete(MyDatabaseHelper.TABLE_WEATHER,
                MyDatabaseHelper.COLUMN_LOCATION + " = ?",
                new String[]{info.location});

        // write weather data from today and yesterday.
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_LOCATION, info.location);
        values.put(MyDatabaseHelper.COLUMN_WEATHER, info.weatherNow);
        values.put(MyDatabaseHelper.COLUMN_TEMP, info.miniTemp[0] + "/" + info.maxiTemp[0]);
        values.put(MyDatabaseHelper.COLUMN_TIME, info.date);
        database.insert(MyDatabaseHelper.TABLE_WEATHER, null, values);
        values.clear();
        if (haveYesterdayData) {
            values.put(MyDatabaseHelper.COLUMN_LOCATION, yesterdayWeather.location);
            values.put(MyDatabaseHelper.COLUMN_WEATHER, yesterdayWeather.weather);
            values.put(MyDatabaseHelper.COLUMN_TEMP, yesterdayWeather.temp);
            values.put(MyDatabaseHelper.COLUMN_TIME, yesterdayWeather.time);
            database.insert(MyDatabaseHelper.TABLE_WEATHER, null, values);
            values.clear();
        }
        database.close();
    }

    @SuppressLint("SimpleDateFormat")
    public static int[] readYesterdayWeather(Context context, WeatherInfoToShow info) {
        // get yesterday date.
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date date = cal.getTime();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String yesterdayDate = simpleDateFormat.format(date);

        Weather yesterdayWeather = null;
        boolean haveYesterdayData = false;

        // init database.
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(context,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                2);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        // read yesterday weather.
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_WEATHER,
                null,
                MyDatabaseHelper.COLUMN_LOCATION + " = '" + info.location
                        + "' AND "
                        + MyDatabaseHelper.COLUMN_TIME + " = '" + yesterdayDate + "'",
                null,
                null,
                null,
                null);
        if(cursor.moveToFirst()) {
            do {
                String locationText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LOCATION));
                String weatherText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER));
                String tempText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TEMP));
                String timeText = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TIME));
                yesterdayWeather = new Weather(locationText, weatherText, tempText, timeText);
                haveYesterdayData = true;
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        if (haveYesterdayData) {
            String[] yesterdayTemp = yesterdayWeather.temp.split("/");
            return new int[] {Integer.parseInt(yesterdayTemp[0]), Integer.parseInt(yesterdayTemp[1])};
        } else {
            return null;
        }
    }

    public static void writeWeatherInfo(Context context, String location, WeatherInfoToShow info) {
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(context,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                2);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        database.delete(MyDatabaseHelper.TABLE_INFO,
                MyDatabaseHelper.COLUMN_REAL_LOCATION + " = ?",
                new String[]{location});

        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_REAL_LOCATION, location);
        values.put(MyDatabaseHelper.COLUMN_DATE, info.date);
        values.put(MyDatabaseHelper.COLUMN_MOON, info.moon);
        values.put(MyDatabaseHelper.COLUMN_REFRESH_TIME, info.refreshTime);
        values.put(MyDatabaseHelper.COLUMN_LOCATION, info.location);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_NOW, info.weatherNow);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_NOW, info.weatherKindNow);
        values.put(MyDatabaseHelper.COLUMN_TEMP_NOW, info.tempNow);
        values.put(MyDatabaseHelper.COLUMN_WEEK_1, info.week[0]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_2, info.week[1]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_3, info.week[2]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_4, info.week[3]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_5, info.week[4]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_6, info.week[5]);
        values.put(MyDatabaseHelper.COLUMN_WEEK_7, info.week[6]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_1, info.weather[0]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_2, info.weather[1]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_3, info.weather[2]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_4, info.weather[3]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_5, info.weather[4]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_6, info.weather[5]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_7, info.weather[6]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_1, info.weatherKind[0]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_2, info.weatherKind[1]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_3, info.weatherKind[2]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_4, info.weatherKind[3]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_5, info.weatherKind[4]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_6, info.weatherKind[5]);
        values.put(MyDatabaseHelper.COLUMN_WEATHER_KIND_7, info.weatherKind[6]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_1, info.maxiTemp[0]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_2, info.maxiTemp[1]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_3, info.maxiTemp[2]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_4, info.maxiTemp[3]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_5, info.maxiTemp[4]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_6, info.maxiTemp[5]);
        values.put(MyDatabaseHelper.COLUMN_MAXI_TEMP_7, info.maxiTemp[6]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_1, info.miniTemp[0]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_2, info.miniTemp[1]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_3, info.miniTemp[2]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_4, info.miniTemp[3]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_5, info.miniTemp[4]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_6, info.miniTemp[5]);
        values.put(MyDatabaseHelper.COLUMN_MINI_TEMP_7, info.miniTemp[6]);
        values.put(MyDatabaseHelper.COLUMN_WIND_TITLE, info.windTitle);
        values.put(MyDatabaseHelper.COLUMN_WIND_INFO, info.windInfo);
        values.put(MyDatabaseHelper.COLUMN_PM_TITLE, info.pmTitle);
        values.put(MyDatabaseHelper.COLUMN_PM_INFO, info.pmInfo);
        values.put(MyDatabaseHelper.COLUMN_HUM_TITLE, info.humTitle);
        values.put(MyDatabaseHelper.COLUMN_HUM_INFO, info.humInfo);
        values.put(MyDatabaseHelper.COLUMN_UV_TITLE, info.uvTitle);
        values.put(MyDatabaseHelper.COLUMN_UV_INFO, info.uvInfo);
        values.put(MyDatabaseHelper.COLUMN_DRESS_TITLE, info.dressTitle);
        values.put(MyDatabaseHelper.COLUMN_DRESS_INFO, info.dressInfo);
        values.put(MyDatabaseHelper.COLUMN_COLD_TITLE, info.coldTitle);
        values.put(MyDatabaseHelper.COLUMN_COLD_INFO, info.coldInfo);
        values.put(MyDatabaseHelper.COLUMN_AIR_TITLE, info.airTitle);
        values.put(MyDatabaseHelper.COLUMN_AIR_INFO, info.airInfo);
        values.put(MyDatabaseHelper.COLUMN_WASH_CAR_TITLE, info.washCarTitle);
        values.put(MyDatabaseHelper.COLUMN_WASH_CAR_INFO, info.washCarInfo);
        values.put(MyDatabaseHelper.COLUMN_EXERCISE_TITLE, info.exerciseTitle);
        values.put(MyDatabaseHelper.COLUMN_EXERCISE_INFO, info.exerciseInfo);
        database.insert(MyDatabaseHelper.TABLE_INFO, null, values);
        values.clear();
        database.close();
    }

    public static WeatherInfoToShow readWeatherInfo(Context context, String location) {
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(context,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                2);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        WeatherInfoToShow info = new WeatherInfoToShow();
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_INFO,
                null,
                MyDatabaseHelper.COLUMN_REAL_LOCATION + " = '" + location + "'",
                null,
                null,
                null,
                null);
        if(cursor.moveToFirst()) {
            do {
                info.date = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DATE));
                info.moon = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MOON));
                info.refreshTime = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_REFRESH_TIME));
                info.location = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LOCATION));
                info.weatherNow = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_NOW));
                info.weatherKindNow = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_NOW));
                info.tempNow = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TEMP_NOW));
                info.week = new String[7];
                info.week[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_1));
                info.week[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_2));
                info.week[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_3));
                info.week[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_4));
                info.week[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_5));
                info.week[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_6));
                info.week[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEEK_7));
                info.weather = new String[7];
                info.weather[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_1));
                info.weather[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_2));
                info.weather[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_3));
                info.weather[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_4));
                info.weather[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_5));
                info.weather[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_6));
                info.weather[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_7));
                info.weatherKind = new String[7];
                info.weatherKind[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_1));
                info.weatherKind[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_2));
                info.weatherKind[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_3));
                info.weatherKind[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_4));
                info.weatherKind[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_5));
                info.weatherKind[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_6));
                info.weatherKind[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WEATHER_KIND_7));
                info.maxiTemp = new String[7];
                info.maxiTemp[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_1));
                info.maxiTemp[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_2));
                info.maxiTemp[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_3));
                info.maxiTemp[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_4));
                info.maxiTemp[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_5));
                info.maxiTemp[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_6));
                info.maxiTemp[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MAXI_TEMP_7));
                info.miniTemp = new String[7];
                info.miniTemp[0] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_1));
                info.miniTemp[1] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_2));
                info.miniTemp[2] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_3));
                info.miniTemp[3] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_4));
                info.miniTemp[4] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_5));
                info.miniTemp[5] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_6));
                info.miniTemp[6] = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_MINI_TEMP_7));
                info.windTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_TITLE));
                info.windInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WIND_INFO));
                info.pmTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PM_TITLE));
                info.pmInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PM_INFO));
                info.humTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_HUM_TITLE));
                info.humInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_HUM_INFO));
                info.uvTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_UV_TITLE));
                info.uvInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_UV_INFO));
                info.dressTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DRESS_TITLE));
                info.dressInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DRESS_INFO));
                info.coldTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLD_TITLE));
                info.coldInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLD_INFO));
                info.airTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_AIR_TITLE));
                info.airInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_AIR_INFO));
                info.washCarTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WASH_CAR_TITLE));
                info.washCarInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_WASH_CAR_INFO));
                info.exerciseTitle = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_EXERCISE_TITLE));
                info.exerciseInfo = cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_EXERCISE_INFO));
            } while (cursor.moveToNext());
        } else {
            return null;
        }
        cursor.close();
        database.close();

        return info;
    }

// check internet

    public static boolean isNetConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return mNetworkInfo != null && mNetworkInfo.isAvailable();
    }

// bitmap

    public static Bitmap readBitMap(Context context, int resId){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is,null,opt);
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
}
