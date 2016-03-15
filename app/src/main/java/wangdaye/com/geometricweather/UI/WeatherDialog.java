package wangdaye.com.geometricweather.UI;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import wangdaye.com.geometricweather.Data.JuheWeather;
import wangdaye.com.geometricweather.Data.WeatherInfoToShow;
import wangdaye.com.geometricweather.R;

public class WeatherDialog extends DialogFragment {
    // widget
    private CardView weatherCard;
    private ImageView[] weatherIcon;
    private TextView weatherText;
    private Button done;

    // animator
    private AnimatorSet[] animatorSetsIcon;

    // data
    private WeatherInfoToShow info;
    private int position;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_weather, null);
        builder.setView(view);

        this.initWidget(view);

        return builder.create();
    }

    private void initWidget(View view) {
        this.weatherIcon = new ImageView[3];
        weatherIcon[0] = (ImageView) view.findViewById(R.id.dialog_weather_icon_1);
        weatherIcon[1] = (ImageView) view.findViewById(R.id.dialog_weather_icon_2);
        weatherIcon[2] = (ImageView) view.findViewById(R.id.dialog_weather_icon_3);
        int[] imageId = JuheWeather.getWeatherIcon(info.weatherKind[position], MainActivity.isDay);
        for (int i = 0; i < 3; i ++) {
            if (imageId[i] != 0) {
                weatherIcon[i].setImageResource(imageId[i]);
                weatherIcon[i].setVisibility(View.VISIBLE);
            } else {
                weatherIcon[i].setVisibility(View.GONE);
            }
        }

        this.weatherText = (TextView) view.findViewById(R.id.dialog_weather_text);
        String text = info.weather[position] + " " + info.miniTemp[position] + "/" + info.maxiTemp[position] + "°"
                + "\n" + info.windDir[position] + info.windLevel[position];
        weatherText.setText(text);

        this.done = (Button) view.findViewById(R.id.dialog_weather_button);
        if (MainActivity.isDay) {
            done.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
        } else {
            done.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
        }
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        this.animatorSetsIcon = new AnimatorSet[3];
        final int[] animatorId = JuheWeather.getAnimatorId(info.weatherKind[position], MainActivity.isDay);
        for (int i = 0; i < 3; i ++) {
            if (animatorId[i] != 0) {
                animatorSetsIcon[i] = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), animatorId[i]);
                animatorSetsIcon[i].setTarget(weatherIcon[i]);
            }
        }

        this.weatherCard = (CardView) view.findViewById(R.id.dialog_weather_container);
        weatherCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 3; i ++) {
                    if (animatorId[i] != 0) {
                        animatorSetsIcon[i].start();
                    }
                }
            }
        });
    }

    public void setData(WeatherInfoToShow info, int position) {
        this.info = info;
        this.position = position;
    }
}
