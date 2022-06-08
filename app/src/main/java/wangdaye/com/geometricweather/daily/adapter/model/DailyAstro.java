package wangdaye.com.geometricweather.daily.adapter.model;

import java.util.TimeZone;

import wangdaye.com.geometricweather.common.basic.models.weather.Astro;
import wangdaye.com.geometricweather.common.basic.models.weather.MoonPhase;
import wangdaye.com.geometricweather.daily.adapter.DailyWeatherAdapter;

public class DailyAstro implements DailyWeatherAdapter.ViewModel {

    private TimeZone timeZone;
    private Astro sun;
    private Astro moon;
    private MoonPhase moonPhase;

    public DailyAstro(TimeZone timeZone, Astro sun, Astro moon, MoonPhase moonPhase) {
        this.timeZone = timeZone;
        this.sun = sun;
        this.moon = moon;
        this.moonPhase = moonPhase;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public Astro getSun() {
        return sun;
    }

    public void setSun(Astro sun) {
        this.sun = sun;
    }

    public Astro getMoon() {
        return moon;
    }

    public void setMoon(Astro moon) {
        this.moon = moon;
    }

    public MoonPhase getMoonPhase() {
        return moonPhase;
    }

    public void setMoonPhase(MoonPhase moonPhase) {
        this.moonPhase = moonPhase;
    }

    public static boolean isCode(int code) {
        return code == 7;
    }

    @Override
    public int getCode() {
        return 7;
    }
}
