package wangdaye.com.geometricweather.main.adapters.main;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;

import java.text.DateFormat;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Base;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.main.utils.DayNightColorWrapper;

public class FirstCardHeaderController
        implements View.OnClickListener {

    private final GeoActivity mActivity;
    private final View mView;
    private final String mFormattedId;

    private @Nullable LinearLayout mContainer;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    public FirstCardHeaderController(@NonNull GeoActivity activity, @NonNull Location location) {
        mActivity = activity;
        mView = LayoutInflater.from(activity).inflate(R.layout.container_main_first_card_header, null);
        mFormattedId = location.getFormattedId();

        AppCompatImageView timeIcon = mView.findViewById(R.id.container_main_first_card_header_timeIcon);
        TextView refreshTime = mView.findViewById(R.id.container_main_first_card_header_timeText);
        TextClock localTime = mView.findViewById(R.id.container_main_first_card_header_localTimeText);
        TextView alert = mView.findViewById(R.id.container_main_first_card_header_alert);
        View line = mView.findViewById(R.id.container_main_first_card_header_line);

        if (location.getWeather() != null) {
            Weather weather = location.getWeather();

            mView.setOnClickListener(v -> ((MainActivity) activity).setManagementFragmentVisibility(true));

            if (weather.getAlertList().size() == 0) {
                timeIcon.setEnabled(false);
                timeIcon.setImageResource(R.drawable.ic_time);
            } else {
                timeIcon.setEnabled(true);
                timeIcon.setImageResource(R.drawable.ic_alert);
            }
            timeIcon.setContentDescription(
                    activity.getString(R.string.content_desc_weather_alert_button)
                            .replace("$", "" + weather.getAlertList().size())
            );
            timeIcon.setOnClickListener(this);

            refreshTime.setText(
                    activity.getString(R.string.refresh_at)
                            + " "
                            + Base.getTime(activity, weather.getBase().getUpdateDate())
            );

            long time = System.currentTimeMillis();
            if (TimeZone.getDefault().getOffset(time) == location.getTimeZone().getOffset(time)) {
                // same time zone.
                localTime.setVisibility(View.GONE);
            } else {
                localTime.setVisibility(View.VISIBLE);
                localTime.setTimeZone(location.getTimeZone().getID());
                localTime.setFormat12Hour(
                        activity.getString(R.string.date_format_widget_long) + ", h:mm aa"
                );
                localTime.setFormat24Hour(
                        activity.getString(R.string.date_format_widget_long) + ", HH:mm"
                );
            }

            if (weather.getAlertList().size() == 0) {
                alert.setVisibility(View.GONE);
                line.setVisibility(View.GONE);
            } else {
                alert.setVisibility(View.VISIBLE);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < weather.getAlertList().size(); i ++) {
                    builder.append(weather.getAlertList().get(i).getDescription())
                            .append(", ")
                            .append(
                                    DateFormat.getDateTimeInstance(
                                            DateFormat.LONG,
                                            DateFormat.DEFAULT
                                    ).format(weather.getAlertList().get(i).getDate())
                            );
                    if (i != weather.getAlertList().size() - 1) {
                        builder.append("\n");
                    }
                }
                alert.setText(builder.toString());

                line.setVisibility(View.VISIBLE);
            }
            alert.setOnClickListener(this);
        }

        DayNightColorWrapper.bind(
                mView,
                new Integer[]{
                        R.attr.colorSurface,
                        R.attr.colorBodyText,
                        R.attr.colorCaptionText,
                },
                (colors, animated) -> {
                    line.setBackgroundColor(colors[0]);
                    ImageViewCompat.setImageTintList(timeIcon, ColorStateList.valueOf(colors[1]));
                    refreshTime.setTextColor(colors[1]);
                    localTime.setTextColor(colors[2]);
                    alert.setTextColor(colors[2]);
                    return null;
                }
        );
    }

    public void bind(LinearLayout firstCardContainer) {
        mContainer = firstCardContainer;
        mContainer.addView(mView, 0);
    }

    public void unbind() {
        if (mContainer != null) {
            mContainer.removeViewAt(0);
            mContainer = null;
        }
    }

    // interface.

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_main_first_card_header_timeIcon:
            case R.id.container_main_first_card_header_alert:
                IntentHelper.startAlertActivity(mActivity, mFormattedId);
                break;
        }
    }
}
