package wangdaye.com.geometricweather.main.ui.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.widget.AppCompatImageView;

import java.text.DateFormat;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.Base;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class FirstCardHeaderController extends AbstractMainItemController
        implements View.OnClickListener {
    private View header;

    private AppCompatImageView timeIcon;
    private TextView refreshTime;
    private TextClock localTime;

    private TextView alert;
    private View line;

    @Nullable private Weather weather;

    public FirstCardHeaderController(@NonNull Activity activity, LinearLayout firstCardContainer,
                                     @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                                     @Px float cardMarginsVertical, @Px float cardMarginsHorizontal,
                                     @Px float cardRadius) {
        super(activity, activity.findViewById(R.id.container_main_daily_trend_card), provider, picker,
                cardMarginsVertical, cardMarginsHorizontal, cardRadius);

        this.header = LayoutInflater.from(firstCardContainer.getContext()).inflate(
                R.layout.container_main_first_card_header, firstCardContainer, false);
        firstCardContainer.addView(header, 0);

        this.timeIcon = header.findViewById(R.id.container_main_first_card_header_timeIcon);
        this.refreshTime = header.findViewById(R.id.container_main_first_card_header_timeText);
        this.localTime = header.findViewById(R.id.container_main_first_card_header_localTimeText);
        this.alert = header.findViewById(R.id.container_main_first_card_header_alert);
        this.line = header.findViewById(R.id.container_main_first_card_header_line);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(@NonNull Location location) {
        if (location.getWeather() != null) {
            weather = location.getWeather();

            header.setOnClickListener(this);
            if (weather.getAlertList().size() == 0) {
                timeIcon.setEnabled(false);
                timeIcon.setImageResource(R.drawable.ic_time);
            } else {
                timeIcon.setEnabled(true);
                timeIcon.setImageResource(R.drawable.ic_alert);
            }
            timeIcon.setSupportImageTintList(
                    ColorStateList.valueOf(picker.getTextContentColor(context))
            );
            timeIcon.setOnClickListener(this);

            refreshTime.setText(
                    context.getString(R.string.refresh_at)
                            + " "
                            + Base.getTime(context, weather.getBase().getUpdateDate())
            );
            refreshTime.setTextColor(picker.getTextContentColor(context));

            long time = System.currentTimeMillis();
            if (TimeZone.getDefault().getOffset(time) == location.getTimeZone().getOffset(time)) {
                // same time zone.
                localTime.setVisibility(View.GONE);
            } else {
                localTime.setVisibility(View.VISIBLE);
                localTime.setTimeZone(location.getTimeZone().getID());
                localTime.setTextColor(picker.getTextSubtitleColor(context));
                localTime.setFormat12Hour(
                        context.getString(R.string.date_format_widget_long) + ", h:mm aa"
                );
                localTime.setFormat24Hour(
                        context.getString(R.string.date_format_widget_long) + ", HH:mm"
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
                alert.setTextColor(picker.getTextSubtitleColor(context));

                line.setVisibility(View.VISIBLE);
                line.setBackgroundColor(picker.getRootColor(context));
            }
            alert.setOnClickListener(this);
        }
    }

    @Override
    public void onDestroy() {
        // do nothing.
    }

    // interface.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_main_first_card_header_timeIcon:
            case R.id.container_main_first_card_header_alert:
                if (weather != null) {
                    IntentHelper.startAlertActivity((GeoActivity) context, weather);
                }
                break;

            case R.id.container_main_first_card_header:
                IntentHelper.startManageActivityForResult((GeoActivity) context);
                break;
        }
    }
}
