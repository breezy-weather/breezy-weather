package wangdaye.com.geometricweather.basic.model.weather;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;

/**
 * Moon phase.
 * */
public class MoonPhase {

    @Nullable private Integer angle;
    @Nullable private String description;

    public MoonPhase(@Nullable Integer angle, @Nullable String description) {
        this.angle = angle;
        this.description = description;
    }

    @Nullable
    public Integer getAngle() {
        return angle;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public boolean isValid() {
        return angle != null && description != null;
    }

    @Nullable
    public String getMoonPhase(Context context) {
        if (TextUtils.isEmpty(description)) {
            return context.getString(R.string.phase_new);
        }

        switch (description.toLowerCase()) {
            case "waxingcrescent":
            case "waxing crescent":
                return context.getString(R.string.phase_waxing_crescent);

            case "first":
            case "firstquarter":
            case "first quarter":
                return context.getString(R.string.phase_first);

            case "waxinggibbous":
            case "waxing gibbous":
                return context.getString(R.string.phase_waxing_gibbous);

            case "full":
            case "fullmoon":
            case "full moon":
                return context.getString(R.string.phase_full);

            case "waninggibbous":
            case "waning gibbous":
                return context.getString(R.string.phase_waning_gibbous);

            case "third":
            case "thirdquarter":
            case "third quarter":
            case "last":
            case "lastquarter":
            case "last quarter":
                return context.getString(R.string.phase_third);

            case "waningcrescent":
            case "waning crescent":
                return context.getString(R.string.phase_waning_crescent);

            default:
                return context.getString(R.string.phase_new);
        }
    }
}
