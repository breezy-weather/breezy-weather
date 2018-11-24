package wangdaye.com.geometricweather.data.entity.result.caiyun;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CaiYunForecastResult {

    /**
     * new : new
     * status : 0
     * precipitation : {"pubTime":"2018-11-23T20:32:59+08:00","headIconType":"rain_0","shortDescription":"","isRainOrSnow":2,"status":0,"description":"未来两小时不会下雨，放心出门吧","value":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],"weather":"1","isModify":false,"headDescription":"未来2小时无雨","isShow":false}
     */

    @SerializedName("new")
    public String newX;
    public int status;
    public Precipitation precipitation;

    public static class Precipitation {
        /**
         * pubTime : 2018-11-23T20:32:59+08:00
         * headIconType : rain_0
         * shortDescription :
         * isRainOrSnow : 2
         * status : 0
         * description : 未来两小时不会下雨，放心出门吧
         * value : [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
         * weather : 1
         * isModify : false
         * headDescription : 未来2小时无雨
         * isShow : false
         */

        public String pubTime;
        public String headIconType;
        public String shortDescription;
        public int isRainOrSnow;
        public int status;
        public String description;
        public String weather;
        public boolean isModify;
        public String headDescription;
        public boolean isShow;
        public List<Double> value;
    }
}
