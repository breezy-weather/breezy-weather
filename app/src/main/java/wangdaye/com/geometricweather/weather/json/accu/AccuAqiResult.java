package wangdaye.com.geometricweather.weather.json.accu;

import java.util.Date;

/**
 * Accu aqi result.
 * */

public class AccuAqiResult {

    /**
     * Date : 2016-12-22T07:00:00+08:00
     * EpochDate : 1482390000
     * Index : 113
     * ParticulateMatter2_5 : 85.0
     * ParticulateMatter10 : 95.0
     * Ozone : 23.0
     * CarbonMonoxide : 0.9
     * NitrogenMonoxide : null
     * NitrogenDioxide : 40.0
     * SulfurDioxide : 23.0
     * Lead : null
     * SettingsWeatherSource : Breezometer
     */

    public Date Date;
    public long EpochDate;
    public int Index;
    public float ParticulateMatter2_5;
    public float ParticulateMatter10;
    public float Ozone;
    public float CarbonMonoxide;
    public float NitrogenMonoxide;
    public float NitrogenDioxide;
    public float SulfurDioxide;
    public float Lead;
    public String Source;
}
