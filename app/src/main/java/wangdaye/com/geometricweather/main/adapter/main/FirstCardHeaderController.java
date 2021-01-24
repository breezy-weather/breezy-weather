package wangdaye.com.geometricweather.main.adapter.main;

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
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Base;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.main.MainModuleUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

public class FirstCardHeaderController
        implements View.OnClickListener {

    private GeoActivity activity;
    private View view;
    @Nullable private Weather weather;

    private @Nullable LinearLayout container;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    public FirstCardHeaderController(@NonNull GeoActivity activity, @NonNull Location location) {
        this.activity = activity;
        this.view = LayoutInflater.from(activity).inflate(R.layout.container_main_first_card_header, null);

        AppCompatImageView timeIcon = view.findViewById(R.id.container_main_first_card_header_timeIcon);
        TextView refreshTime = view.findViewById(R.id.container_main_first_card_header_timeText);
        TextClock localTime = view.findViewById(R.id.container_main_first_card_header_localTimeText);
        TextView alert = view.findViewById(R.id.container_main_first_card_header_alert);
        View line = view.findViewById(R.id.container_main_first_card_header_line);

        ThemeManager themeManager = ThemeManager.getInstance(activity);

        if (location.getWeather() != null) {
            this.weather = location.getWeather();

            view.setOnClickListener(v ->
                    IntentHelper.startManageActivityForResult(activity, MainActivity.MANAGE_ACTIVITY));
            view.setEnabled(!MainModuleUtils.isMultiFragmentEnabled(activity));

            if (weather.getAlertList().size() == 0) {
                timeIcon.setEnabled(false);
                timeIcon.setImageResource(R.drawable.ic_time);
            } else {
                timeIcon.setEnabled(true);
                timeIcon.setImageResource(R.drawable.ic_alert);
            }
            ImageViewCompat.setImageTintList(
                    timeIcon,
                    ColorStateList.valueOf(themeManager.getTextContentColor(activity))
            );
            timeIcon.setOnClickListener(this);

            refreshTime.setText(
                    activity.getString(R.string.refresh_at)
                            + " "
                            + Base.getTime(activity, weather.getBase().getUpdateDate())
            );
            refreshTime.setTextColor(themeManager.getTextContentColor(activity));

            long time = System.currentTimeMillis();
            if (TimeZone.getDefault().getOffset(time) == location.getTimeZone().getOffset(time)) {
                // same time zone.
                localTime.setVisibility(View.GONE);
            } else {
                localTime.setVisibility(View.VISIBLE);
                localTime.setTimeZone(location.getTimeZone().getID());
                localTime.setTextColor(themeManager.getTextSubtitleColor(activity));
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
                alert.setTextColor(themeManager.getTextSubtitleColor(activity));

                line.setVisibility(View.VISIBLE);
                line.setBackgroundColor(themeManager.getRootColor(activity));
            }
            alert.setOnClickListener(this);
        }
    }

    public void bind(LinearLayout firstCardContainer) {
        container = firstCardContainer;
        container.addView(view, 0);
    }

    public void unbind() {
        if (container != null) {
            container.removeViewAt(0);
            container = null;
        }
    }

    // interface.

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_main_first_card_header_timeIcon:
            case R.id.container_main_first_card_header_alert:
                if (weather != null) {
                    IntentHelper.startAlertActivity(activity, weather);
                }
                break;
        }
    }
}
