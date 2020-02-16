package wangdaye.com.geometricweather.main.adapter.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

public class LocationHolder extends RecyclerView.ViewHolder {

    private RelativeLayout locationItemContainer;
    private AppCompatImageView swipeIconStart;
    private AppCompatImageView swipeIconEnd;

    private LinearLayout locationItemView;
    private ImageView weatherIcon;
    private AppCompatImageView residentIcon;
    private TextView title;
    private TextView alerts;

    private TextView subtitle;
    private TextView source;

    protected LocationModel model;
    private int direction;
    private @ColorInt int swipeEndColor;

    protected LocationHolder(@NonNull ViewGroup parent, LocationAdapter.OnLocationItemClickListener listener) {
        super(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_location, parent, false));

        this.locationItemContainer = itemView.findViewById(R.id.item_location_container);
        this.swipeIconStart = itemView.findViewById(R.id.item_location_swipeIcon_start);
        this.swipeIconEnd = itemView.findViewById(R.id.item_location_swipeIcon_end);

        this.locationItemView = itemView.findViewById(R.id.item_location_item);
        this.weatherIcon = itemView.findViewById(R.id.item_location_weather_icon);
        this.residentIcon = itemView.findViewById(R.id.item_location_resident_icon);
        this.title = itemView.findViewById(R.id.item_location_title);
        this.alerts = itemView.findViewById(R.id.item_location_alerts);

        this.subtitle = itemView.findViewById(R.id.item_location_subtitle);
        this.source = itemView.findViewById(R.id.item_location_source);

        locationItemContainer.setOnClickListener(v -> listener.onClick(v, model.location.getFormattedId()));
    }

    @SuppressLint("SetTextI18n")
    protected void onBindView(Context context, LocationModel model,
                              ResourceProvider resourceProvider, MainThemePicker themePicker) {
        this.model = model;
        direction = 0;
        swipeEndColor = ContextCompat.getColor(context,
                model.location.isCurrentPosition() ? R.color.colorPrimary : R.color.colorTextAlert);

        if (model.currentPosition) {
            swipeIconEnd.setImageResource(R.drawable.ic_settings);
        } else {
            swipeIconEnd.setImageResource(model.residentPosition ? R.drawable.ic_tag_off : R.drawable.ic_tag_plus);
        }

        if (themePicker != null) {
            locationItemView.setBackgroundColor(themePicker.getRootColor(context));
        } else {
            locationItemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRoot));
        }

        residentIcon.setVisibility(model.residentPosition ? View.VISIBLE : View.GONE);

        if (model.weatherCode != null) {
            weatherIcon.setVisibility(View.VISIBLE);
            weatherIcon.setImageDrawable(
                    resourceProvider.getWeatherIcon(
                            model.weatherCode,
                            TimeManager.isDaylight(model.location)
                    )
            );
        } else {
            weatherIcon.setVisibility(View.GONE);
        }

        if (themePicker != null) {
            title.setTextColor(themePicker.getTextTitleColor(context));
        } else {
            title.setTextColor(ContextCompat.getColor(context, R.color.colorTextTitle));
        }
        title.setText(model.title);

        if (themePicker != null) {
            alerts.setTextColor(themePicker.getTextSubtitleColor(context));
        } else {
            alerts.setTextColor(ContextCompat.getColor(context, R.color.colorTextSubtitle));
        }
        if (!TextUtils.isEmpty(model.alerts)) {
            alerts.setVisibility(View.VISIBLE);
            alerts.setText(model.alerts);
        } else {
            alerts.setVisibility(View.GONE);
        }

        if (themePicker != null) {
            subtitle.setTextColor(themePicker.getTextContentColor(context));
        } else {
            subtitle.setTextColor(ContextCompat.getColor(context, R.color.colorTextContent));
        }
        subtitle.setText(model.subtitle);

        // source.
        source.setText("Powered by " + model.weatherSource.getSourceUrl());
        source.setTextColor(model.weatherSource.getSourceColor());

        drawSwipe(context, 0);
        drawDrag(context, false);
    }

    public void drawDrag(Context context, boolean elevate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            locationItemContainer.setElevation(DisplayUtils.dpToPx(context, elevate ? 10 : 0));
        }
    }

    public void drawSwipe(Context context, float dX) {
        if (itemView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (dX < 0 && direction >= 0) {
                direction = -1;
                locationItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.striking_red));
            } else if (dX > 0 && direction <= 0) {
                direction = 1;
                locationItemContainer.setBackgroundColor(swipeEndColor);
            }

            locationItemContainer.setTranslationX(0);
            locationItemView.setTranslationX(dX);
            swipeIconStart.setTranslationX((float) Math.max(0.5 * (dX + swipeIconEnd.getMeasuredWidth()), 0));
            swipeIconEnd.setTranslationX((float) Math.min(0.5 * (dX - swipeIconStart.getMeasuredWidth()), 0));
        } else {
            if (dX < 0 && direction >= 0) {
                direction = -1;
                locationItemContainer.setBackgroundColor(swipeEndColor);
            } else if (dX > 0 && direction <= 0) {
                direction = 1;
                locationItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.striking_red));
            }

            locationItemContainer.setTranslationX(0);
            locationItemView.setTranslationX(dX);
            swipeIconStart.setTranslationX((float) Math.min(0.5 * (dX - swipeIconStart.getMeasuredWidth()), 0));
            swipeIconEnd.setTranslationX((float) Math.max(0.5 * (dX + swipeIconEnd.getMeasuredWidth()), 0));
        }
    }
}
