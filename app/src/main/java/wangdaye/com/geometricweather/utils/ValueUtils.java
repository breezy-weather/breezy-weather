package wangdaye.com.geometricweather.utils;

import android.content.Context;
import android.text.TextUtils;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Value utils.
 * */

public class ValueUtils {

    public static String getCardDisplay(Context c, String[] values) {
        String[] options = c.getResources().getStringArray(R.array.card_display_options);

        StringBuilder builder = new StringBuilder();
        for (String v : values) {
            if (TextUtils.isEmpty(v)) {
                continue;
            }
            switch (v) {
                case SettingsOptionManager.CARD_DAILY_OVERVIEW:
                    builder.append(options[0]);
                    break;

                case SettingsOptionManager.CARD_HOURLY_OVERVIEW:
                    builder.append(options[1]);
                    break;

                case SettingsOptionManager.CARD_AIR_QUALITY:
                    builder.append(options[2]);
                    break;

                case SettingsOptionManager.CARD_LIFE_DETAILS:
                    builder.append(options[3]);
                    break;

                case SettingsOptionManager.CARD_SUNRISE_SUNSET:
                    builder.append(options[4]);
                    break;
            }
            builder.append(" ");
        }
        return builder.toString();
    }
}
