package wangdaye.com.geometricweather.main.ui.adapter.main.holder;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class FooterViewHolder extends AbstractMainViewHolder {

    private TextView title;
    private Button editButton;

    @NonNull private WeatherView weatherView;

    public FooterViewHolder(@NonNull Activity activity, ViewGroup parent, @NonNull WeatherView weatherView,
                            @NonNull ResourceProvider provider, @NonNull MainColorPicker picker,
                            @Px float cardMarginsVertical) {
        super(activity, LayoutInflater.from(activity).inflate(R.layout.container_main_footer, parent, false),
                provider, picker, false);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
        if (cardMarginsVertical != 0) {
            params.setMargins(0, (int) -cardMarginsVertical, 0, 0);
        }
        itemView.setLayoutParams(params);

        this.title = itemView.findViewById(R.id.container_main_footer_title);
        this.editButton = itemView.findViewById(R.id.container_main_footer_editButton);

        this.weatherView = weatherView;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(@NonNull Location location) {
        title.setTextColor(weatherView.getHeaderTextColor(title.getContext()));
        title.setText("* Powered by " + location.getWeatherSource().getSourceUrl());

        editButton.setTextColor(weatherView.getHeaderTextColor(editButton.getContext()));
        editButton.setOnClickListener(v -> IntentHelper.startCardDisplayManageActivityForResult(
                (Activity) context,
                MainActivity.CARD_MANAGE_ACTIVITY
        ));
    }

    @Override
    protected Animator getEnterAnimator(List<Animator> pendingAnimatorList) {
        Animator a = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f);
        a.setDuration(450);
        a.setInterpolator(new FastOutSlowInInterpolator());
        a.setStartDelay(pendingAnimatorList.size() * 150);
        return a;
    }
}
