package wangdaye.com.geometricweather.view.widget.weatherView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Week weather view.
 * */

public class WeekWeatherView extends FrameLayout implements View.OnClickListener {
    private TextView[] weekTexts;
    private ImageView[] weekIcons;

    private OnClickWeekContainerListener listener;

    /** <br> life cycle. */

    public WeekWeatherView(Context context) {
        super(context);
        this.initialize();
    }

    public WeekWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public WeekWeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WeekWeatherView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    @SuppressLint("InflateParams")
    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.container_week_weather, null);
        addView(view);
        this.setOnClickListener(this);

        RelativeLayout[] weekContainers = new RelativeLayout[]{
                (RelativeLayout) findViewById(R.id.container_week_weather_week_info_1),
                (RelativeLayout) findViewById(R.id.container_week_weather_week_info_2),
                (RelativeLayout) findViewById(R.id.container_week_weather_week_info_3),
                (RelativeLayout) findViewById(R.id.container_week_weather_week_info_4),
                (RelativeLayout) findViewById(R.id.container_week_weather_week_info_5),};
        for (RelativeLayout c : weekContainers) {
            c.setOnClickListener(this);
        }
        this.weekTexts = new TextView[] {
                (TextView) findViewById(R.id.container_week_weather_week_text_1),
                (TextView) findViewById(R.id.container_week_weather_week_text_2),
                (TextView) findViewById(R.id.container_week_weather_week_text_3),
                (TextView) findViewById(R.id.container_week_weather_week_text_4),
                (TextView) findViewById(R.id.container_week_weather_week_text_5),};
        this.weekIcons = new ImageView[] {
                (ImageView) findViewById(R.id.container_week_weather_icon_1),
                (ImageView) findViewById(R.id.container_week_weather_icon_2),
                (ImageView) findViewById(R.id.container_week_weather_icon_3),
                (ImageView) findViewById(R.id.container_week_weather_icon_4),
                (ImageView) findViewById(R.id.container_week_weather_icon_5),};
    }

    /** <br> data. */

    public void setData(Weather weather) {
        boolean isDay = TimeUtils.getInstance(getContext()).isDayTime();

        String firstWeekDay;
        String secondWeekDay;

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String[] dates = weather.base.date.split("-");
        if (year == Integer.parseInt(dates[0])
                && month == Integer.parseInt(dates[1])
                && day == Integer.parseInt(dates[2])) {
            firstWeekDay = getContext().getString(R.string.today);
            secondWeekDay = weather.dailyList.get(1).week;
        } else if (year == Integer.parseInt(dates[0])
                && month == Integer.parseInt(dates[1])
                && day == Integer.parseInt(dates[2]) + 1) {
            firstWeekDay = getContext().getString(R.string.yesterday);
            secondWeekDay = getContext().getString(R.string.today);
        } else {
            firstWeekDay = weather.dailyList.get(0).week;
            secondWeekDay = weather.dailyList.get(1).week;
        }

        for (int i = 0; i < Weather.DAILY_LENGTH; i ++) {
            if (i == 0) {
                weekTexts[i].setText(firstWeekDay);
            } else if (i == 1) {
                weekTexts[i].setText(secondWeekDay);
            } else {
                weekTexts[i].setText(weather.dailyList.get(i).week);
            }

            int[] imageId = WeatherHelper.getWeatherIcon(
                    isDay ? weather.dailyList.get(i).weatherKinds[0] : weather.dailyList.get(i).weatherKinds[1],
                    isDay);
            Glide.with(getContext())
                    .load(imageId[3])
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(weekIcons[i]);
        }
    }

    /** <br> interface. */

    public interface OnClickWeekContainerListener {
        void onClickWeekContainer(int position);
    }

    public void setOnClickWeekContainerListener(OnClickWeekContainerListener l) {
        this.listener = l;
    }

    @Override
    public void onClick(View v) {
        int position = 0;
        switch (v.getId()) {
            case R.id.container_week_weather_week_info_1:
                position = 0;
                break;

            case R.id.container_week_weather_week_info_2:
                position = 1;
                break;

            case R.id.container_week_weather_week_info_3:
                position = 2;
                break;

            case R.id.container_week_weather_week_info_4:
                position = 3;
                break;

            case R.id.container_week_weather_week_info_5:
                position = 4;
                break;
        }
        if (listener != null) {
            listener.onClickWeekContainer(position);
        }
    }
}
