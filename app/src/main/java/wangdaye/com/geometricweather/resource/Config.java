package wangdaye.com.geometricweather.resource;

import androidx.annotation.NonNull;

public class Config {

    public boolean hasWeatherIcons;
    public boolean hasWeatherAnimators;
    public boolean hasMinimalIcons;
    public boolean hasShortcutIcons;
    public boolean hasSunMoonDrawables;

    public Config() {
        hasWeatherIcons = true;
        hasWeatherAnimators = false;
        hasMinimalIcons = true;
        hasShortcutIcons = true;
        hasSunMoonDrawables = true;
    }

    @NonNull
    @Override
    public String toString() {
        return "config : " + "\n"
                + "hasWeatherIcons = " + hasWeatherIcons + "\n"
                + "hasWeatherAnimators = " + hasWeatherAnimators + "\n"
                + "hasMinimalIcons = " + hasMinimalIcons + "\n"
                + "hasShortcutIcons = " + hasShortcutIcons + "\n"
                + "hasSunMoonDrawables = " + hasSunMoonDrawables + "\n";
    }
}
