package wangdaye.com.geometricweather.UserInterface;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import wangdaye.com.geometricweather.Data.JuheResult;
import wangdaye.com.geometricweather.Data.JuheWeather;
import wangdaye.com.geometricweather.Data.Location;
import wangdaye.com.geometricweather.Data.MyDatabaseHelper;
import wangdaye.com.geometricweather.Data.Weather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Receiver.WidgetProviderClockDay;
import wangdaye.com.geometricweather.Receiver.WidgetProviderClockDayCenter;
import wangdaye.com.geometricweather.Receiver.WidgetProviderClockDayWeek;
import wangdaye.com.geometricweather.Receiver.WidgetProviderDay;
import wangdaye.com.geometricweather.Receiver.WidgetProviderDayWeek;
import wangdaye.com.geometricweather.Receiver.WidgetProviderWeek;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ManageDialog.SetLocationListener {
    // widget
    public static FragmentManager fragmentManager;
    private WeatherFragment weatherFragment;
    private LiteWeatherFragment liteWeatherFragment;
    private static FrameLayout navHead;

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
        boolean watchedIntroduce = sharedPreferences.getBoolean(getString(R.string.key_watched_introduce), false);
        if (! watchedIntroduce) {
            this.requestPermission();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.key_watched_introduce), true);
            editor.apply();
            Intent intent = new Intent(this, IntroduceActivity.class);
            startActivity(intent);
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
        if (animatorSwitch) {
            this.weatherFragment.animatorCancel();
        } else {
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
                    sendNotification(weatherFragment.location, this);
                } else {
                    sendNotification(liteWeatherFragment.location, this);
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
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(MainActivity.this, AboutAppActivity.class);
            startActivity(intent);
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
            this.createApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResult) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
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
            weatherFragment = new WeatherFragment();
            weatherFragment.setLocation(locationList.get(0));
            changeFragment(weatherFragment);
        } else {
            liteWeatherFragment = new LiteWeatherFragment();
            liteWeatherFragment.setLocation(locationList.get(0));
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
                window.setNavigationBarColor(context.getResources().getColor(android.R.color.black));
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
                this.weatherFragment.setLocation(new Location(location));
                this.weatherFragment.refreshAll();
            } else {
                this.liteWeatherFragment.setLocation(new Location(location));
                this.liteWeatherFragment.refreshAll();
            }
        } else {
            if (animatorSwitch) {
                for (int i = 0; i < locationList.size(); i ++) {
                    if (locationList.get(i).location.equals(location)) {
                        this.weatherFragment.setLocation(locationList.get(i));
                        this.weatherFragment.refreshAll();
                        return;
                    }
                }
            } else {
                for (int i = 0; i < locationList.size(); i ++) {
                    if (locationList.get(i).location.equals(location)) {
                        this.liteWeatherFragment.setLocation(locationList.get(i));
                        this.liteWeatherFragment.refreshAll();
                        return;
                    }
                }
            }
        }
    }

// database

    private void initDatabaseHelper() {
        this.databaseHelper = new MyDatabaseHelper(MainActivity.this,
                MyDatabaseHelper.DATABASE_NAME,
                null,
                1);
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
    public static void writeTodayWeather(Context context, Location location) {
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
                1);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        // read yesterday weather.
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_WEATHER,
                null,
                MyDatabaseHelper.COLUMN_LOCATION + " = '" + location.juheResult.result.data.realtime.city_name
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
                new String[]{location.juheResult.result.data.realtime.city_name});

        // write weather data from today and yesterday.
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_LOCATION, location.juheResult.result.data.realtime.city_name);
        values.put(MyDatabaseHelper.COLUMN_WEATHER, location.juheResult.result.data.realtime.weatherNow.weatherInfo);
        values.put(MyDatabaseHelper.COLUMN_TEMP,
                location.juheResult.result.data.weather.get(0).info.night.get(2)
                        + "/"
                        + location.juheResult.result.data.weather.get(0).info.day.get(2));
        values.put(MyDatabaseHelper.COLUMN_TIME, location.juheResult.result.data.realtime.date);
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
    public static int[] readYesterdayWeather(Context context, Location location) {
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
                1);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        // read yesterday weather.
        Cursor cursor = database.query(MyDatabaseHelper.TABLE_WEATHER,
                null,
                MyDatabaseHelper.COLUMN_LOCATION + " = '" + location.juheResult.result.data.realtime.city_name
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

    public static void sendNotification(Location location, Context context) {
        if (MainActivity.locationList.get(0).location.equals(location.location)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if(! sharedPreferences.getBoolean(context.getString(R.string.key_notification_switch), false)) {
                return;
            }

            if (location.juheResult == null || ! location.juheResult.error_code.equals("0")) {
                return;
            }

            JuheResult.WeatherNow weatherNow = location.juheResult.result.data.realtime.weatherNow;
            List<JuheResult.Weather> weathers = location.juheResult.result.data.weather;

            String weatherKind = JuheWeather.getWeatherKind(weatherNow.weatherInfo);
            NotificationManager notificationManager
                    = (NotificationManager) context.getSystemService(MainActivity.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            // set level
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (sharedPreferences.getBoolean(context.getString(R.string.key_hide_notification_in_lockScreen), false)) {
                    builder.setVisibility(Notification.VISIBILITY_SECRET);
                } else {
                    builder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }
            }

            // small view
            builder.setSmallIcon(JuheWeather.getMiniWeatherIcon(weatherKind, MainActivity.isDay));
            RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.notification_base);
            int[] imageId = JuheWeather.getWeatherIcon(weatherKind, MainActivity.isDay);
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
            String[] time = location.juheResult.result.data.realtime.time.split(":");
            String text = location.juheResult.result.data.realtime.city_name
                    + "."
                    + time[0]
                    + ":"
                    + time[1];
            view.setTextViewText(R.id.notification_base_text_remark, text);
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
                    defaultColor = R.color.colorTextGrey2nd;
                    view.setTextColor(R.id.notification_base_text_details, ContextCompat.getColor(context, defaultColor));
                    view.setTextColor(R.id.notification_base_text_remark, ContextCompat.getColor(context, defaultColor));
                    break;
            }

            // big view
            RemoteViews viewBig = new RemoteViews(context.getPackageName(), R.layout.notification_big);
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
                    context.getString(R.string.today));
            viewBig.setTextViewText(R.id.notification_big_text_temp_1,
                    weathers.get(0).info.night.get(2)
                            + "/"
                            + weathers.get(0).info.day.get(2)
                            + "°");
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weathers.get(0).info.day.get(1)), MainActivity.isDay);
            viewBig.setImageViewResource(R.id.notification_big_image_1, imageId[3]);
            // 2
            viewBig.setTextViewText(R.id.notification_big_text_week_2,
                    context.getString(R.string.week) + weathers.get(1).week);
            viewBig.setTextViewText(R.id.notification_big_text_temp_2,
                    weathers.get(1).info.night.get(2)
                            + "/"
                            + weathers.get(1).info.day.get(2)
                            + "°");
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weathers.get(1).info.day.get(1)), MainActivity.isDay);
            viewBig.setImageViewResource(R.id.notification_big_image_2, imageId[3]);
            // 3
            viewBig.setTextViewText(
                    R.id.notification_big_text_week_3,
                    context.getString(R.string.week) + weathers.get(2).week);
            viewBig.setTextViewText(
                    R.id.notification_big_text_temp_3,
                    weathers.get(2).info.night.get(2)
                            + "/"
                            + weathers.get(2).info.day.get(2)
                            + "°");
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weathers.get(2).info.day.get(1)), MainActivity.isDay);
            viewBig.setImageViewResource(R.id.notification_big_image_3, imageId[3]);
            // 4
            viewBig.setTextViewText(
                    R.id.notification_big_text_week_4,
                    context.getString(R.string.week) + weathers.get(3).week);
            viewBig.setTextViewText(
                    R.id.notification_big_text_temp_4,
                    weathers.get(3).info.night.get(2)
                            + "/"
                            + weathers.get(3).info.day.get(2)
                            + "°");
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weathers.get(3).info.day.get(1)), MainActivity.isDay);
            viewBig.setImageViewResource(R.id.notification_big_image_4, imageId[3]);
            // 5
            viewBig.setTextViewText(
                    R.id.notification_big_text_week_5,
                    context.getString(R.string.week) + weathers.get(4).week);
            viewBig.setTextViewText(
                    R.id.notification_big_text_temp_5,
                    weathers.get(4).info.night.get(2)
                            + "/"
                            + weathers.get(4).info.day.get(2)
                            + "°");
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weathers.get(4).info.day.get(1)), MainActivity.isDay);
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
            // loading big view
            Notification notification = builder.build();
            notification.bigContentView = viewBig;
            // set clear flag
            if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_can_clear_switch), false)) {
                // the notification can be cleared
                notification.flags = Notification.FLAG_AUTO_CANCEL;
            } else {
                // the notification can not be cleared
                notification.flags = Notification.FLAG_ONGOING_EVENT;
            }
            // refresh notification
            notificationManager.notify(MainActivity.NOTIFICATION_ID, notification);
        }
    }

// widget

    public static void refreshWidgetDay(Location location, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_day_setting), Context.MODE_PRIVATE);

        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        String locationName = sharedPreferences.getString(
                context.getString(R.string.key_location),
                context.getString(R.string.local));

        if (! location.location.equals(locationName)) {
            return;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_day);

        JuheResult.WeatherNow weatherNow = location.juheResult.result.data.realtime.weatherNow;
        String weatherKind = JuheWeather.getWeatherKind(weatherNow.weatherInfo);
        int[] imageId = JuheWeather.getWeatherIcon(weatherKind, isDay);
        views.setImageViewResource(R.id.widget_day_image, imageId[3]);
        String weatherTextNow = weatherNow.weatherInfo
                + "\n"
                + weatherNow.temperature
                + "℃";
        views.setTextViewText(R.id.widget_day_weather, weatherTextNow);
        JuheResult.Weather weatherToday = location.juheResult.result.data.weather.get(0);
        String weatherTextTemp = weatherToday.info.day.get(2)
                + "°"
                + "\n"
                + weatherToday.info.night.get(2)
                + "°";
        views.setTextViewText(R.id.widget_day_temp, weatherTextTemp);
        String[] timeText = location.juheResult.result.data.realtime.time.split(":");
        String refreshText = location.juheResult.result.data.realtime.city_name
                + "."
                + timeText[0]
                + ":"
                + timeText[1];
        views.setTextViewText(R.id.widget_day_time, refreshText);

        if(showCard) { // show card
            views.setViewVisibility(R.id.widget_day_card, View.VISIBLE);
            views.setTextColor(R.id.widget_day_weather, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_day_temp, ContextCompat.getColor(context, R.color.colorTextDark));
        } else { // do not show card
            views.setViewVisibility(R.id.widget_day_card, View.GONE);
            views.setTextColor(R.id.widget_day_weather, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_day_temp, ContextCompat.getColor(context, R.color.colorTextLight));
        }

        //Intent intent = new Intent("com.geometricweather.receiver.CLICK_WIDGET");
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_day_button, pendingIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProviderDay.class), views);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                context.getString(R.string.sp_widget_day_setting), Context.MODE_PRIVATE).edit();
        editor.putBoolean(context.getString(R.string.key_saved_data), true);
        editor.putString(context.getString(R.string.key_weather_kind_today), weatherKind);
        editor.putString(context.getString(R.string.key_weather_today), weatherTextNow);
        editor.putString(context.getString(R.string.key_temperature_today), weatherTextTemp);
        editor.putString(context.getString(R.string.key_city_time), refreshText);
        editor.apply();
    }

    public static void refreshWidgetWeek(Location location, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_week_setting), Context.MODE_PRIVATE);

        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        String locationName = sharedPreferences.getString(
                context.getString(R.string.key_location),
                context.getString(R.string.local));
        if (! location.location.equals(locationName)) {
            return;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_week);
        List<JuheResult.Weather> weather = location.juheResult.result.data.weather;
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
        week = location.juheResult.result.data.realtime.city_name;
        views.setTextViewText(R.id.widget_week_week_1, week);
        // 2
        week = context.getString(R.string.week) + weather.get(1).week;
        views.setTextViewText(R.id.widget_week_week_2, week);
        // 3
        week = context.getString(R.string.week) + weather.get(2).week;
        views.setTextViewText(R.id.widget_week_week_3, week);
        // 4
        week = context.getString(R.string.week) + weather.get(3).week;
        views.setTextViewText(R.id.widget_week_week_4, week);
        // 5
        week = context.getString(R.string.week) + weather.get(4).week;
        views.setTextViewText(R.id.widget_week_week_5, week);
        // set card and text color
        if (showCard) { // show card
            views.setViewVisibility(R.id.widget_week_card, View.VISIBLE);
            // week text
            views.setTextColor(R.id.widget_week_week_1, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_2, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_3, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_4, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_week_5, ContextCompat.getColor(context, R.color.colorTextDark));
            // temperature text
            views.setTextColor(R.id.widget_week_temp_1, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_2, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_3, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_4, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_week_temp_5, ContextCompat.getColor(context, R.color.colorTextDark));
        } else { // do not show card
            views.setViewVisibility(R.id.widget_week_card, View.GONE);
            // week text
            views.setTextColor(R.id.widget_week_week_1, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_2, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_3, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_4, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_week_5, ContextCompat.getColor(context, R.color.colorTextLight));
            // temperature text
            views.setTextColor(R.id.widget_week_temp_1, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_2, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_3, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_4, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_week_temp_5, ContextCompat.getColor(context, R.color.colorTextLight));
        }

        //Intent intent = new Intent("com.geometricweather.receiver.CLICK_WIDGET");
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_week_button, pendingIntent);

        // refresh UI
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProviderWeek.class), views);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                context.getString(R.string.sp_widget_week_setting), Context.MODE_PRIVATE).edit();
        editor.putBoolean(context.getString(R.string.key_saved_data), true);
        // weather
        if (isDay) {
            editor.putString(context.getString(R.string.key_weather_kind_today), JuheWeather.getWeatherKind(weather.get(0).info.day.get(1)));
            editor.putString(context.getString(R.string.key_weather_2), JuheWeather.getWeatherKind(weather.get(1).info.day.get(1)));
            editor.putString(context.getString(R.string.key_weather_3), JuheWeather.getWeatherKind(weather.get(2).info.day.get(1)));
            editor.putString(context.getString(R.string.key_weather_4), JuheWeather.getWeatherKind(weather.get(3).info.day.get(1)));
            editor.putString(context.getString(R.string.key_weather_5), JuheWeather.getWeatherKind(weather.get(4).info.day.get(1)));
        } else {
            editor.putString(context.getString(R.string.key_weather_kind_today), JuheWeather.getWeatherKind(weather.get(0).info.night.get(1)));
            editor.putString(context.getString(R.string.key_weather_2), JuheWeather.getWeatherKind(weather.get(1).info.night.get(1)));
            editor.putString(context.getString(R.string.key_weather_3), JuheWeather.getWeatherKind(weather.get(2).info.night.get(1)));
            editor.putString(context.getString(R.string.key_weather_4), JuheWeather.getWeatherKind(weather.get(3).info.night.get(1)));
            editor.putString(context.getString(R.string.key_weather_5), JuheWeather.getWeatherKind(weather.get(4).info.night.get(1)));
        }
        // week
        editor.putString(context.getString(R.string.key_city_time), location.juheResult.result.data.realtime.city_name);
        editor.putString(context.getString(R.string.key_week_2), context.getString(R.string.week) + weather.get(1).week);
        editor.putString(context.getString(R.string.key_week_3), context.getString(R.string.week) + weather.get(2).week);
        editor.putString(context.getString(R.string.key_week_4), context.getString(R.string.week) + weather.get(3).week);
        editor.putString(context.getString(R.string.key_week_5), context.getString(R.string.week) + weather.get(4).week);
        // temperature
        editor.putString(context.getString(R.string.key_temperature_today),
                weather.get(0).info.night.get(2) + "/" + weather.get(0).info.day.get(2) + "°");
        editor.putString(context.getString(R.string.key_temperature_2),
                weather.get(1).info.night.get(2) + "/" + weather.get(1).info.day.get(2) + "°");
        editor.putString(context.getString(R.string.key_temperature_3),
                weather.get(2).info.night.get(2) + "/" + weather.get(2).info.day.get(2) + "°");
        editor.putString(context.getString(R.string.key_temperature_4),
                weather.get(3).info.night.get(2) + "/" + weather.get(3).info.day.get(2) + "°");
        editor.putString(context.getString(R.string.key_temperature_5),
                weather.get(4).info.night.get(2) + "/" + weather.get(4).info.day.get(2) + "°");
        editor.apply();
    }

    public static void refreshWidgetDayWeek(Location location, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_day_week_setting), Context.MODE_PRIVATE);

        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        String locationName = sharedPreferences.getString(
                context.getString(R.string.key_location),
                context.getString(R.string.local));
        if (! location.location.equals(locationName)) {
            return;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week);
        JuheResult.WeatherNow weatherNow = location.juheResult.result.data.realtime.weatherNow;

        String weatherKind = JuheWeather.getWeatherKind(weatherNow.weatherInfo);
        int imageId[] = JuheWeather.getWeatherIcon(weatherKind, isDay);
        views.setImageViewResource(R.id.widget_day_image, imageId[3]);

        String weatherTextNow = weatherNow.weatherInfo + "\n" + weatherNow.temperature + "℃";
        views.setTextViewText(R.id.widget_day_weather, weatherTextNow);

        JuheResult.Weather weatherToday = location.juheResult.result.data.weather.get(0);
        String weatherTextTemp = weatherToday.info.day.get(2)
                + "°"
                + "\n"
                + weatherToday.info.night.get(2)
                + "°";
        views.setTextViewText(R.id.widget_day_temp, weatherTextTemp);

        String[] timeText = location.juheResult.result.data.realtime.time.split(":");
        String refreshText = location.juheResult.result.data.realtime.city_name
                + "."
                + timeText[0]
                + ":"
                + timeText[1];
        views.setTextViewText(R.id.widget_day_time, refreshText);

        List<JuheResult.Weather> weather = location.juheResult.result.data.weather;
        // icon
        // 1
        if (isDay) {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(1).info.day.get(1)), true);
        } else {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(1).info.night.get(1)), false);
        }
        views.setImageViewResource(R.id.widget_4_day_image_1, imageId[3]);
        // 2
        if (isDay) {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(2).info.day.get(1)), true);
        } else {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(2).info.night.get(1)), false);
        }
        views.setImageViewResource(R.id.widget_4_day_image_2, imageId[3]);
        // 3
        if (isDay) {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(3).info.day.get(1)), true);
        } else {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(3).info.night.get(1)), false);
        }
        views.setImageViewResource(R.id.widget_4_day_image_3, imageId[3]);
        // 4
        if (isDay) {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(4).info.day.get(1)), true);
        } else {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(4).info.night.get(1)), false);
        }
        views.setImageViewResource(R.id.widget_4_day_image_4, imageId[3]);
        // temperature
        String temp;
        // 1
        temp = weather.get(1).info.night.get(2)
                + "/"
                + weather.get(1).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_4_day_temp_1, temp);
        // 2
        temp = weather.get(2).info.night.get(2)
                + "/"
                + weather.get(2).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_4_day_temp_2, temp);
        // 3
        temp = weather.get(3).info.night.get(2)
                + "/"
                + weather.get(3).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_4_day_temp_3, temp);
        // 4
        temp = weather.get(4).info.night.get(2)
                + "/"
                + weather.get(4).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_4_day_temp_4, temp);
        // week
        String week;
        // 1
        week = context.getString(R.string.week) + weather.get(1).week;
        views.setTextViewText(R.id.widget_4_day_week_1, week);
        // 2
        week = context.getString(R.string.week) + weather.get(2).week;
        views.setTextViewText(R.id.widget_4_day_week_2, week);
        // 3
        week = context.getString(R.string.week) + weather.get(3).week;
        views.setTextViewText(R.id.widget_4_day_week_3, week);
        // 4
        week = context.getString(R.string.week) + weather.get(4).week;
        views.setTextViewText(R.id.widget_4_day_week_4, week);

        if (showCard) { // show card
            // show card
            views.setViewVisibility(R.id.widget_day_week_card, View.VISIBLE);
            // today text
            views.setTextColor(R.id.widget_day_weather, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_day_temp, ContextCompat.getColor(context, R.color.colorTextDark));
            // week text
            views.setTextColor(R.id.widget_4_day_week_1, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_4_day_week_2, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_4_day_week_3, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_4_day_week_4, ContextCompat.getColor(context, R.color.colorTextDark));
            // temperature text
            views.setTextColor(R.id.widget_4_day_temp_1, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_4_day_temp_2, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_4_day_temp_3, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_4_day_temp_4, ContextCompat.getColor(context, R.color.colorTextDark));
        } else { // do not show card
            // do not show card
            views.setViewVisibility(R.id.widget_day_week_card, View.GONE);
            // today text
            views.setTextColor(R.id.widget_day_weather, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_day_temp, ContextCompat.getColor(context, R.color.colorTextLight));
            // week text
            views.setTextColor(R.id.widget_4_day_week_1, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_4_day_week_2, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_4_day_week_3, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_4_day_week_4, ContextCompat.getColor(context, R.color.colorTextLight));
            // temperature text
            views.setTextColor(R.id.widget_4_day_temp_1, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_4_day_temp_2, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_4_day_temp_3, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_4_day_temp_4, ContextCompat.getColor(context, R.color.colorTextLight));
        }

        //Intent intent = new Intent("com.geometricweather.receiver.CLICK_WIDGET");
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_day_week_button, pendingIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProviderDayWeek.class), views);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                context.getString(R.string.sp_widget_day_week_setting), Context.MODE_PRIVATE).edit();
        editor.putBoolean(context.getString(R.string.key_saved_data), true);
        editor.putString(context.getString(R.string.key_weather_kind_today), weatherKind);
        editor.putString(context.getString(R.string.key_weather_today), weatherTextNow);
        editor.putString(context.getString(R.string.key_temperature_today), weatherTextTemp);
        editor.putString(context.getString(R.string.key_city_time), refreshText);
        // weather
        if (isDay) {
            editor.putString(context.getString(R.string.key_weather_2), JuheWeather.getWeatherKind(weather.get(1).info.day.get(1)));
            editor.putString(context.getString(R.string.key_weather_3), JuheWeather.getWeatherKind(weather.get(2).info.day.get(1)));
            editor.putString(context.getString(R.string.key_weather_4), JuheWeather.getWeatherKind(weather.get(3).info.day.get(1)));
            editor.putString(context.getString(R.string.key_weather_5), JuheWeather.getWeatherKind(weather.get(4).info.day.get(1)));
        } else {
            editor.putString(context.getString(R.string.key_weather_2), JuheWeather.getWeatherKind(weather.get(1).info.night.get(1)));
            editor.putString(context.getString(R.string.key_weather_3), JuheWeather.getWeatherKind(weather.get(2).info.night.get(1)));
            editor.putString(context.getString(R.string.key_weather_4), JuheWeather.getWeatherKind(weather.get(3).info.night.get(1)));
            editor.putString(context.getString(R.string.key_weather_5), JuheWeather.getWeatherKind(weather.get(4).info.night.get(1)));
        }
        // week
        editor.putString(context.getString(R.string.key_week_2), context.getString(R.string.week) + weather.get(1).week);
        editor.putString(context.getString(R.string.key_week_3), context.getString(R.string.week) + weather.get(2).week);
        editor.putString(context.getString(R.string.key_week_4), context.getString(R.string.week) + weather.get(3).week);
        editor.putString(context.getString(R.string.key_week_5), context.getString(R.string.week) + weather.get(4).week);
        // temperature
        editor.putString(context.getString(R.string.key_temperature_2),
                weather.get(1).info.night.get(2) + "/" + weather.get(1).info.day.get(2) + "°");
        editor.putString(context.getString(R.string.key_temperature_3),
                weather.get(2).info.night.get(2) + "/" + weather.get(2).info.day.get(2) + "°");
        editor.putString(context.getString(R.string.key_temperature_4),
                weather.get(3).info.night.get(2) + "/" + weather.get(3).info.day.get(2) + "°");
        editor.putString(context.getString(R.string.key_temperature_5),
                weather.get(4).info.night.get(2) + "/" + weather.get(4).info.day.get(2) + "°");
        editor.apply();
    }

    public static void refreshWidgetClockDay(Location location, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_setting), Context.MODE_PRIVATE);

        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        String locationName = sharedPreferences.getString(
                context.getString(R.string.key_location),
                context.getString(R.string.local));
        if (! location.location.equals(locationName)) {
            return;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day);

        JuheResult.WeatherNow weatherNow = location.juheResult.result.data.realtime.weatherNow;
        String weatherKind = JuheWeather.getWeatherKind(weatherNow.weatherInfo);
        int[] imageId = JuheWeather.getWeatherIcon(weatherKind, isDay);
        views.setImageViewResource(R.id.widget_clock_day_image, imageId[3]);
        String[] solar = location.juheResult.result.data.realtime.date.split("-");
        String dateText = solar[1] + "-" + solar[2]
                + " " + context.getString(R.string.week) + location.juheResult.result.data.weather.get(0).week
                + " / "
                + location.juheResult.result.data.realtime.moon;
        views.setTextViewText(R.id.widget_clock_day_date, dateText);
        String weatherText = location.juheResult.result.data.realtime.city_name
                + " / "
                + weatherNow.weatherInfo + " " + weatherNow.temperature + "℃";
        views.setTextViewText(R.id.widget_clock_day_weather, weatherText);

        if(showCard) { // show card
            views.setViewVisibility(R.id.widget_clock_day_card, View.VISIBLE);
            views.setTextColor(R.id.widget_clock_day_clock, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_date, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_weather, ContextCompat.getColor(context, R.color.colorTextDark));
        } else { // do not show card
            views.setViewVisibility(R.id.widget_clock_day_card, View.GONE);
            views.setTextColor(R.id.widget_clock_day_clock, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_date, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_weather, ContextCompat.getColor(context, R.color.colorTextLight));
        }

        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(context, 0, intentClock, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_clock_button, pendingIntentClock);

        Intent intentWeather = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentWeather = PendingIntent.getActivity(context, 0, intentWeather, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_weather_button, pendingIntentWeather);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProviderClockDay.class), views);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_setting), Context.MODE_PRIVATE).edit();
        editor.putBoolean(context.getString(R.string.key_saved_data), true);
        editor.putString(context.getString(R.string.key_weather_kind_today), weatherKind);
        editor.putString(context.getString(R.string.key_weather_today), weatherText);
        editor.putString(context.getString(R.string.key_city_time), dateText);
        editor.apply();
    }

    public static void refreshWidgetClockDayCenter(Location location, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_center_setting), Context.MODE_PRIVATE);

        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        String locationName = sharedPreferences.getString(
                context.getString(R.string.key_location),
                context.getString(R.string.local));
        if (! location.location.equals(locationName)) {
            return;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_center);

        JuheResult.WeatherNow weatherNow = location.juheResult.result.data.realtime.weatherNow;
        String weatherKind = JuheWeather.getWeatherKind(weatherNow.weatherInfo);
        int[] imageId = JuheWeather.getWeatherIcon(weatherKind, isDay);
        views.setImageViewResource(R.id.widget_clock_day_center_image, imageId[3]);
        String[] solar = location.juheResult.result.data.realtime.date.split("-");
        String dateText = solar[1] + "-" + solar[2]
                + " " + context.getString(R.string.week) + location.juheResult.result.data.weather.get(0).week
                + " / "
                + location.juheResult.result.data.realtime.moon;
        views.setTextViewText(R.id.widget_clock_day_center_date, dateText);
        String weatherTextNow = weatherNow.weatherInfo
                + "\n"
                + weatherNow.temperature
                + "℃";
        views.setTextViewText(R.id.widget_clock_day_center_weather, weatherTextNow);
        JuheResult.Weather weatherToday = location.juheResult.result.data.weather.get(0);
        String weatherTextTemp = weatherToday.info.day.get(2)
                + "°"
                + "\n"
                + weatherToday.info.night.get(2)
                + "°";
        views.setTextViewText(R.id.widget_clock_day_center_temp, weatherTextTemp);
        String[] timeText = location.juheResult.result.data.realtime.time.split(":");
        String refreshText = location.juheResult.result.data.realtime.city_name
                + "."
                + timeText[0]
                + ":"
                + timeText[1];
        views.setTextViewText(R.id.widget_clock_day_center_time, refreshText);

        if(showCard) { // show card
            views.setViewVisibility(R.id.widget_clock_day_center_card, View.VISIBLE);
            views.setTextColor(R.id.widget_clock_day_center_clock, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_center_date, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_center_weather, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_center_temp, ContextCompat.getColor(context, R.color.colorTextDark));
        } else { // do not show card
            views.setViewVisibility(R.id.widget_clock_day_card, View.GONE);
            views.setTextColor(R.id.widget_clock_day_center_clock, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_center_date, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_center_weather, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_center_temp, ContextCompat.getColor(context, R.color.colorTextLight));
        }

        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(context, 0, intentClock, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_center_clock_button, pendingIntentClock);

        Intent intentWeather = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentWeather = PendingIntent.getActivity(context, 0, intentWeather, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_center_weather_button, pendingIntentWeather);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProviderClockDayCenter.class), views);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_setting), Context.MODE_PRIVATE).edit();
        editor.putBoolean(context.getString(R.string.key_saved_data), true);
        editor.putString(context.getString(R.string.key_week_2), dateText);
        editor.putString(context.getString(R.string.key_weather_today), weatherTextNow);
        editor.putString(context.getString(R.string.key_temperature_today), weatherTextTemp);
        editor.putString(context.getString(R.string.key_city_time), refreshText);
        editor.apply();
    }

    public static void refreshWidgetClockDayWeek(Location location, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_week_setting), Context.MODE_PRIVATE);

        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        String locationName = sharedPreferences.getString(
                context.getString(R.string.key_location),
                context.getString(R.string.local));
        if (! location.location.equals(locationName)) {
            return;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_week);

        JuheResult.WeatherNow weatherNow = location.juheResult.result.data.realtime.weatherNow;
        String weatherKindToday = JuheWeather.getWeatherKind(weatherNow.weatherInfo);
        int[] imageId = JuheWeather.getWeatherIcon(weatherKindToday, isDay);
        views.setImageViewResource(R.id.widget_clock_day_week_image, imageId[3]);
        String[] solar = location.juheResult.result.data.realtime.date.split("-");
        String dateText = solar[1] + "-" + solar[2]
                + " " + context.getString(R.string.week) + location.juheResult.result.data.weather.get(0).week
                + " / "
                + location.juheResult.result.data.realtime.moon;
        views.setTextViewText(R.id.widget_clock_day_week_date, dateText);
        String weatherText = location.juheResult.result.data.realtime.city_name
                + " / "
                + weatherNow.weatherInfo + " " + weatherNow.temperature + "℃";
        views.setTextViewText(R.id.widget_clock_day_week_weather, weatherText);

        List<JuheResult.Weather> weather = location.juheResult.result.data.weather;
        // icon
        if (isDay) {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(1).info.day.get(1)), true);
        } else {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(1).info.night.get(1)), false);
        }
        views.setImageViewResource(R.id.widget_clock_day_week_image_1, imageId[3]);
        // 2
        if (isDay) {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(2).info.day.get(1)), true);
        } else {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(2).info.night.get(1)), false);
        }
        views.setImageViewResource(R.id.widget_clock_day_week_image_2, imageId[3]);
        // 3
        if (isDay) {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(3).info.day.get(1)), true);
        } else {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(3).info.night.get(1)), false);
        }
        views.setImageViewResource(R.id.widget_clock_day_week_image_3, imageId[3]);
        // 4
        if (isDay) {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(4).info.day.get(1)), true);
        } else {
            imageId = JuheWeather.getWeatherIcon(JuheWeather.getWeatherKind(weather.get(4).info.night.get(1)), false);
        }
        views.setImageViewResource(R.id.widget_clock_day_week_image_4, imageId[3]);
        // temperature
        String temp;
        // 1
        temp = weather.get(1).info.night.get(2)
                + "/"
                + weather.get(1).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_clock_day_week_temp_1, temp);
        // 2
        temp = weather.get(2).info.night.get(2)
                + "/"
                + weather.get(2).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_clock_day_week_temp_2, temp);
        // 3
        temp = weather.get(3).info.night.get(2)
                + "/"
                + weather.get(3).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_clock_day_week_temp_3, temp);
        // 4
        temp = weather.get(4).info.night.get(2)
                + "/"
                + weather.get(4).info.day.get(2)
                + "°";
        views.setTextViewText(R.id.widget_clock_day_week_temp_4, temp);
        // week
        String week;
        // 1
        week = context.getString(R.string.week) + weather.get(1).week;
        views.setTextViewText(R.id.widget_clock_day_week_week_1, week);
        // 2
        week = context.getString(R.string.week) + weather.get(2).week;
        views.setTextViewText(R.id.widget_clock_day_week_week_2, week);
        // 3
        week = context.getString(R.string.week) + weather.get(3).week;
        views.setTextViewText(R.id.widget_clock_day_week_week_3, week);
        // 4
        week = context.getString(R.string.week) + weather.get(4).week;
        views.setTextViewText(R.id.widget_clock_day_week_week_4, week);

        if(showCard) { // show card
            views.setViewVisibility(R.id.widget_clock_day_week_card, View.VISIBLE);
            views.setTextColor(R.id.widget_clock_day_week_clock, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_week_date, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_week_weather, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_week_week_1, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_week_week_2, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_week_week_3, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_week_week_4, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_week_temp_1, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_week_temp_2, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_week_temp_3, ContextCompat.getColor(context, R.color.colorTextDark));
            views.setTextColor(R.id.widget_clock_day_week_temp_4, ContextCompat.getColor(context, R.color.colorTextDark));
        } else { // do not show card
            views.setViewVisibility(R.id.widget_clock_day_week_card, View.GONE);
            views.setTextColor(R.id.widget_clock_day_week_clock, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_week_date, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_week_weather, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_week_week_1, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_week_week_2, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_week_week_3, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_week_week_4, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_week_temp_1, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_week_temp_2, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_week_temp_3, ContextCompat.getColor(context, R.color.colorTextLight));
            views.setTextColor(R.id.widget_clock_day_week_temp_4, ContextCompat.getColor(context, R.color.colorTextLight));
        }

        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(context, 0, intentClock, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_week_clock_button, pendingIntentClock);

        Intent intentWeather = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentWeather = PendingIntent.getActivity(context, 0, intentWeather, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_week_weather_button, pendingIntentWeather);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProviderClockDayWeek.class), views);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_setting), Context.MODE_PRIVATE).edit();
        editor.putBoolean(context.getString(R.string.key_saved_data), true);
        editor.putString(context.getString(R.string.key_weather_kind_today), weatherKindToday);
        editor.putString(context.getString(R.string.key_city_time), dateText);
        editor.putString(context.getString(R.string.key_weather_today), weatherText);
        editor.putString(context.getString(R.string.key_week_2), context.getString(R.string.week) + weather.get(1).week);
        editor.putString(context.getString(R.string.key_week_3), context.getString(R.string.week) + weather.get(2).week);
        editor.putString(context.getString(R.string.key_week_4), context.getString(R.string.week) + weather.get(3).week);
        editor.putString(context.getString(R.string.key_week_5), context.getString(R.string.week) + weather.get(4).week);
        if (isDay) {
            editor.putString(context.getString(R.string.key_weather_2), JuheWeather.getWeatherKind(weather.get(1).info.day.get(1)));
            editor.putString(context.getString(R.string.key_weather_3), JuheWeather.getWeatherKind(weather.get(2).info.day.get(1)));
            editor.putString(context.getString(R.string.key_weather_4), JuheWeather.getWeatherKind(weather.get(3).info.day.get(1)));
            editor.putString(context.getString(R.string.key_weather_5), JuheWeather.getWeatherKind(weather.get(4).info.day.get(1)));
        } else {
            editor.putString(context.getString(R.string.key_weather_2), JuheWeather.getWeatherKind(weather.get(1).info.night.get(1)));
            editor.putString(context.getString(R.string.key_weather_3), JuheWeather.getWeatherKind(weather.get(2).info.night.get(1)));
            editor.putString(context.getString(R.string.key_weather_4), JuheWeather.getWeatherKind(weather.get(3).info.night.get(1)));
            editor.putString(context.getString(R.string.key_weather_5), JuheWeather.getWeatherKind(weather.get(4).info.night.get(1)));
        }
        editor.putString(context.getString(R.string.key_temperature_2), weather.get(1).info.night.get(2)
                + "/"
                + weather.get(1).info.day.get(2)
                + "°");
        editor.putString(context.getString(R.string.key_temperature_3), weather.get(2).info.night.get(2)
                + "/"
                + weather.get(2).info.day.get(2)
                + "°");
        editor.putString(context.getString(R.string.key_temperature_4), weather.get(3).info.night.get(2)
                + "/"
                + weather.get(3).info.day.get(2)
                + "°");
        editor.putString(context.getString(R.string.key_temperature_5), weather.get(4).info.night.get(2)
                + "/"
                + weather.get(4).info.day.get(2)
                + "°");
        editor.apply();
    }
}
